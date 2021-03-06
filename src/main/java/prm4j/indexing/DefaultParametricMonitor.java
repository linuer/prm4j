/*
 * Copyright (c) 2012, 2013 Mateusz Parzonka
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Mateusz Parzonka - initial API and implementation
 */
package prm4j.indexing;

import prm4j.Globals;
import prm4j.api.BaseEvent;
import prm4j.api.Event;
import prm4j.api.MatchHandler;
import prm4j.api.ParametricMonitor;
import prm4j.indexing.binding.Binding;
import prm4j.indexing.binding.BindingStore;
import prm4j.indexing.binding.DefaultBindingStore;
import prm4j.indexing.binding.LinkedListBindingFactory;
import prm4j.indexing.model.EventContext;
import prm4j.indexing.model.FindMaxArgs;
import prm4j.indexing.model.JoinArgs;
import prm4j.indexing.model.ParameterNode;
import prm4j.indexing.model.UpdateChainingsArgs;
import prm4j.indexing.monitor.AbstractMonitor;
import prm4j.indexing.monitor.DeadMonitor;
import prm4j.indexing.monitor.Monitor;
import prm4j.indexing.monitor.MonitorSet;
import prm4j.indexing.monitor.ParametricMonitorLogger;
import prm4j.indexing.node.DefaultNodeStore;
import prm4j.indexing.node.Node;
import prm4j.indexing.node.NodeManager;
import prm4j.indexing.node.NodeStore;
import prm4j.indexing.node.NullNode;
import prm4j.spec.Spec;
import static prm4j.indexing.IndexingUtils.*;

public class DefaultParametricMonitor implements ParametricMonitor {

    protected final Monitor monitorPrototype;
    protected BindingStore bindingStore;
    protected NodeStore nodeStore;
    protected final EventContext eventContext;
    protected long timestamp = 0L;
    protected final NodeManager nodeManager;

    protected boolean activated = false;

    protected final ParametricMonitorLogger logger;

    /**
     * Creates a DefaultParametricMonitor using default {@link BindingStore} and {@link NodeStore} implementations (and
     * configurations).
     * 
     * @param parameterTree
     * @param eventContext
     * @param spec
     */
    public DefaultParametricMonitor(ParameterNode parameterTree, EventContext eventContext, Spec spec) {
	this.eventContext = eventContext;
	bindingStore = new DefaultBindingStore(new LinkedListBindingFactory(), spec.getFullParameterSet());
	monitorPrototype = spec.getMonitorPrototype();
	nodeManager = new NodeManager();
	nodeStore = new DefaultNodeStore(parameterTree, nodeManager);
	logger = Globals.LOGGING ? new ParametricMonitorLogger(bindingStore, nodeManager) : null;
    }

    /**
     * Creates a DefaultParametricMonitor which externally configurable BindingStore and NodeStore.
     * 
     * @param bindingStore
     * @param nodeStore
     * @param monitorPrototype
     * @param eventContext
     * @param nodeManager
     * @param activated
     */
    public DefaultParametricMonitor(BindingStore bindingStore, NodeStore nodeStore, Monitor monitorPrototype,
	    EventContext eventContext, NodeManager nodeManager, boolean activated) {
	this.bindingStore = bindingStore;
	this.nodeStore = nodeStore;
	this.monitorPrototype = monitorPrototype;
	this.eventContext = eventContext;
	this.nodeManager = nodeManager;
	this.activated = activated;
	logger = Globals.LOGGING ? new ParametricMonitorLogger(bindingStore, nodeManager) : null;
    }

    @Override
    public synchronized void processEvent(Event event) {

	final BaseEvent baseEvent = event.getBaseEvent();

	// wait for a creation event to activate monitoring
	if (!activated) {
	    if (eventContext.isCreationEvent(baseEvent)) {
		activated = true;
	    } else {
		return;
	    }
	}

	// uncompressed representation of bindings
	final Binding[] bindings = bindingStore.getBindings(event.getBoundObjects());
	// selects a subset of bindings from the uncompressed bindings representation
	final int[] parameterMask = baseEvent.getParameterMask();
	// node associated to the current bindings. May be NullNode if binding is encountered the first time
	Node instanceNode = nodeStore.getNode(bindings, parameterMask);
	// monitor associated with the instance node. May be null if the instance node is a NullNode
	Monitor instanceMonitor = instanceNode.getMonitor();

	if (instanceMonitor == null) {
	    // direct update phase
	    for (MonitorSet monitorSet : instanceNode.getMonitorSets()) {
		if (monitorSet != null) {
		    monitorSet.processUpdate(event);
		}
	    }
	    findMaxPhase: for (FindMaxArgs findMaxArgs : eventContext.getFindMaxArgs(baseEvent)) {
		Monitor maxMonitor = nodeStore.getNode(bindings, findMaxArgs.nodeMask).getMonitor();
		if (maxMonitor != null) {
		    // time check
		    final long maxMonitorTimestamp = maxMonitor.getTimestamp();
		    for (int[] disableMask : findMaxArgs.disableMasks) {
			final Node subInstanceNode = nodeStore.getNode(bindings, disableMask);
			if (subInstanceNode.getTimestamp() > maxMonitorTimestamp
				|| (subInstanceNode.getMonitor() != null && subInstanceNode.getMonitor().getTimestamp() < maxMonitorTimestamp)) {
			    continue findMaxPhase;
			}
		    }

		    // DefineTo
		    instanceMonitor = maxMonitor.copy(toCompressedBindings(bindings, parameterMask));
		    if (instanceNode == NullNode.instance) {
			instanceNode = nodeStore.getOrCreateNode(bindings, parameterMask);
		    }
		    instanceNode.setMonitor(instanceMonitor);
		    instanceMonitor.process(event);

		    // update-chainings phase
		    updateChainings(bindings, instanceNode);
		    break findMaxPhase;
		}
	    }

	    joinPhase: for (JoinArgs joinArgs : eventContext.getJoinArgs(baseEvent)) { // 43

		// if node does not exist there can't be any joinable monitors
		final Node compatibleNode = nodeStore.getNode(bindings, joinArgs.nodeMask);
		if (compatibleNode == NullNode.instance) {
		    continue joinPhase;
		}

		// calculate bindings array which is to be joined with the whole monitor set
		final Binding[] joinableBindings = createJoinableBindings(bindings, joinArgs.extensionPattern);

		// join is performed in monitor set
		compatibleNode.getMonitorSet(joinArgs.monitorSetId).join(nodeStore, event, joinableBindings, joinArgs);

	    }

	    monitorCreation: if (instanceMonitor == null && eventContext.isCreationEvent(baseEvent)) {

		if (eventContext.isDisableEvent(baseEvent)) { // 2
		    instanceMonitor = new DeadMonitor(timestamp);
		    nodeStore.getOrCreateNode(bindings, parameterMask).setMonitor(instanceMonitor);
		} else {
		    // check for existing monitors
		    for (int[] existingMonitorMask : eventContext.getExistingMonitorMasks(baseEvent)) {
			Node someNode = nodeStore.getNode(bindings, existingMonitorMask);
			if (someNode.getMonitor() != null) {
			    break monitorCreation;
			}
		    }
		    // DefineNew
		    instanceMonitor = monitorPrototype.copy(toCompressedBindings(bindings, parameterMask), timestamp);
		    if (instanceNode == NullNode.instance) {
			instanceNode = nodeStore.getOrCreateNode(bindings, parameterMask);
		    }
		    instanceNode.setMonitor(instanceMonitor);
		    instanceMonitor.process(event);

		    // update-chainings phase
		    updateChainings(bindings, instanceNode);
		}
	    }
	    nodeStore.getNode(bindings, parameterMask).setTimestamp(timestamp);
	} else {
	    // update phase
	    instanceMonitor.process(event);
	    for (MonitorSet monitorSet : instanceNode.getMonitorSets()) {
		if (monitorSet != null) {
		    monitorSet.processUpdate(event);
		}
	    }
	}

	// we increment the timestamp at the end (deviating from the algorithm) because we use it to count thenumber of
	// events
	nodeManager.tryToClean(timestamp);
	if (logger != null) {
	    logger.log(timestamp);
	}
	timestamp++; // 40
    }

    private void updateChainings(final Binding[] bindings, Node instanceNode) {
	for (UpdateChainingsArgs updateChainingsArgs : instanceNode.getParameterNode().getUpdateChainingsArgs()) {
	    nodeStore.getOrCreateNode(bindings, updateChainingsArgs.nodeMask)
		    .getMonitorSet(updateChainingsArgs.monitorSetId).add(instanceNode.getNodeRef());
	}
    }

    /**
     * Returns an array of bindings containing "gaps" enabling efficient joins by filling these gaps.
     * 
     * @param bindings
     * @param extensionPattern
     *            allows transformation of the bindings to joinable bindings
     * @return joinable bindings
     */
    static Binding[] createJoinableBindings(Binding[] bindings, int[] extensionPattern) {
	final Binding[] joinableBindings = new Binding[extensionPattern.length];
	for (int i = 0; i < extensionPattern.length; i++) {
	    final int e = extensionPattern[i];
	    if (e >= 0) {
		joinableBindings[i] = bindings[e];
	    }
	}
	return joinableBindings;
    }

    @Override
    public void reset() {
	activated = false;
	timestamp = 0L;
	if (logger != null) {
	    logger.reset();
	}
	bindingStore.reset();
	nodeStore.reset();
	nodeManager.reset();
	AbstractMonitor.reset();
	DeadMonitor.reset();
	MatchHandler.reset();
    }

}
