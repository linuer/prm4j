/*
 * Copyright (c) 2012 Mateusz Parzonka, Eric Bodden
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Mateusz Parzonka - initial API and implementation
 */
package prm4j.logic;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import prm4j.api.Parameter;
import prm4j.indexing.BaseEvent;

/**
 * Immutable self-calculating data object.
 */
public class ParametricPropertyImpl implements ParametricProperty {

    private final FiniteSpec finiteSpec;
    private final Set<BaseEvent> creationEvents;
    private final Map<BaseEvent, Set<Set<BaseEvent>>> propertyEnableSets;
    private final Map<BaseEvent, Set<Set<Parameter<?>>>> parameterEnableSets;
    private final Map<MonitorState, Set<Set<BaseEvent>>> statePropertyCoEnableSets;
    private final Map<MonitorState, Set<Set<Parameter<?>>>> stateParameterCoEnableSets;
    private Set<BaseEvent> disablingEvents;
    private Map<BaseEvent, List<ParameterSet>> enablingInstances;
    private Map<BaseEvent, List<ParameterSetTuple>> joinableInstances;
    private Map<ParameterSet, Set<ParameterSetTuple>> chainableInstances;
    private Map<ParameterSet, Set<ParameterSet>> monitorSets;

    public ParametricPropertyImpl(FiniteSpec finiteSpec) {
	this.finiteSpec = finiteSpec;
	creationEvents = calculateCreationEvents();
	calculateRelations();
	propertyEnableSets = Collections.unmodifiableMap(new PropertyEnableSetCalculator().calculateEnableSets());
	parameterEnableSets = Collections.unmodifiableMap(toMap2SetOfSetOfParameters(propertyEnableSets));
	// TODO statePropertyCoEnableSets
	statePropertyCoEnableSets = Collections.unmodifiableMap(new HashMap<MonitorState, Set<Set<BaseEvent>>>());
	stateParameterCoEnableSets = Collections.unmodifiableMap(toMap2SetOfSetOfParameters(statePropertyCoEnableSets));
    }

    /**
     * Creation events are events for which the successor of the initial state is:
     * <ul>
     * <li>not a dead state</li>
     * <li>not the initial state itself (self-loop)</li>
     * </ul>
     *
     * @return the creation events
     */
    private Set<BaseEvent> calculateCreationEvents() {
	Set<BaseEvent> creationSymbols = new HashSet<BaseEvent>();
	MonitorState initialState = finiteSpec.getInitialState();
	for (BaseEvent symbol : finiteSpec.getBaseEvents()) {
	    MonitorState successor = initialState.getSuccessor(symbol);
	    if (successor == null || !successor.equals(initialState)) {
		creationSymbols.add(symbol);
	    }
	}
	return creationSymbols;
    }

    private void calculateRelations() {
  	Set<ParameterSetTuple> temp = new HashSet<ParameterSetTuple>(); // line 2
  	for (BaseEvent baseEvent : finiteSpec.getBaseEvents()) { // line 3
  	    Set<Parameter<?>> parameters = baseEvent.getParameters(); // line 4
  	    for (BaseEvent baseEvent2 : finiteSpec.getBaseEvents()) {
	    }
	}

      }

    private class PropertyEnableSetCalculator {

	private final Map<BaseEvent, Set<Set<BaseEvent>>> enableSets;
	private final Map<MonitorState, Set<Set<BaseEvent>>> stateToSeenBaseEvents;

	public PropertyEnableSetCalculator() {
	    enableSets = new HashMap<BaseEvent, Set<Set<BaseEvent>>>();
	    stateToSeenBaseEvents = new HashMap<MonitorState, Set<Set<BaseEvent>>>();
	    for (BaseEvent baseEvent : finiteSpec.getBaseEvents()) {
		enableSets.put(baseEvent, new HashSet<Set<BaseEvent>>());
	    }
	    for (MonitorState state : finiteSpec.getStates()) {
		stateToSeenBaseEvents.put(state, new HashSet<Set<BaseEvent>>());
	    }
	}

	public Map<BaseEvent, Set<Set<BaseEvent>>> calculateEnableSets() {
	    computeEnableSets(finiteSpec.getInitialState(), new HashSet<BaseEvent>());
	    return enableSets;
	}

	private void computeEnableSets(MonitorState state, Set<BaseEvent> seenBaseEvents) {
	    if (state == null)
		throw new NullPointerException("state may not be null!");
	    for (BaseEvent baseEvent : finiteSpec.getBaseEvents()) {
		if (state.getSuccessor(baseEvent) != null) {
		    final Set<BaseEvent> seenBaseEventsWithoutSelfloop = new HashSet<BaseEvent>(seenBaseEvents);
		    seenBaseEventsWithoutSelfloop.remove(baseEvent);
		    enableSets.get(baseEvent).add(seenBaseEventsWithoutSelfloop);
		    final Set<BaseEvent> nextSeenBaseEvents = new HashSet<BaseEvent>(seenBaseEvents);
		    nextSeenBaseEvents.add(baseEvent);
		    if (!stateToSeenBaseEvents.get(state).contains(nextSeenBaseEvents)) {
			stateToSeenBaseEvents.get(state).add(nextSeenBaseEvents);
			computeEnableSets(state.getSuccessor(baseEvent), nextSeenBaseEvents);
		    }
		}
	    }
	}

    }

    private static <T> Map<T, Set<Set<Parameter<?>>>> toMap2SetOfSetOfParameters(
	    Map<T, Set<Set<BaseEvent>>> baseEvent2setOfSetOfBaseEvent) {
	Map<T, Set<Set<Parameter<?>>>> result = new HashMap<T, Set<Set<Parameter<?>>>>();
	for (Entry<T, Set<Set<BaseEvent>>> entry : baseEvent2setOfSetOfBaseEvent.entrySet()) {
	    result.put(entry.getKey(), toParameterSets(entry.getValue()));
	}
	return result;
    }

    private static Set<Set<Parameter<?>>> toParameterSets(Set<Set<BaseEvent>> propertyEnableSet) {
	Set<Set<Parameter<?>>> result = new HashSet<Set<Parameter<?>>>();
	for (Set<BaseEvent> set : propertyEnableSet) {
	    Set<Parameter<?>> parameterSet = new HashSet<Parameter<?>>();
	    for (BaseEvent baseEvent : set) {
		parameterSet.addAll(baseEvent.getParameters());
	    }
	    result.add(parameterSet);
	}
	return result;
    }

    /**
     * Calculate the largest set of parameters which is needed to trigger a match.
     *
     * @return
     */
    public Set<Parameter<?>> getLongestMatchingInstance() {
	int maxSize = -1;
	Set<Parameter<?>> maxSet = null;
	for (Set<Parameter<?>> set : stateParameterCoEnableSets.get(finiteSpec.getInitialState())) {
	    if (maxSize < set.size()) {
		maxSize = set.size();
		maxSet = set;
	    }
	}
	return Collections.unmodifiableSet(maxSet);
    }

    @Override
    public Set<BaseEvent> getCreationEvents() {
	return creationEvents;
    }

    @Override
    public Set<BaseEvent> getDisablingEvents() {
	return disablingEvents;
    }

    @Override
    public Map<BaseEvent, List<ParameterSet>> getEnablingInstances() {
	return enablingInstances;
    }

    @Override
    public Map<BaseEvent, List<ParameterSetTuple>> getJoinableInstances() {
	return joinableInstances;
    }

    @Override
    public Map<ParameterSet, Set<ParameterSetTuple>> getChainableSubinstances() {
	return chainableInstances;
    }

    @Override
    public Map<ParameterSet, Set<ParameterSet>> getMonitorSets() {
	return monitorSets;
    }

    Map<BaseEvent, Set<Set<BaseEvent>>> getPropertyEnableSets() {
	return propertyEnableSets;
    }

    Map<BaseEvent, Set<Set<Parameter<?>>>> getParameterEnableSets() {
	return parameterEnableSets;
    }

    Map<MonitorState, Set<Set<BaseEvent>>> getStatePropertyCoEnableSets() {
	return statePropertyCoEnableSets;
    }

    Map<MonitorState, Set<Set<Parameter<?>>>> getStateParameterCoEnableSets() {
	return stateParameterCoEnableSets;
    }

    MonitorState getInitialState() {
	return finiteSpec.getInitialState();
    }

    Set<? extends BaseEvent> getBaseEvents() {
	return finiteSpec.getBaseEvents();
    }

}