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

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import prm4j.AbstractTest;
import prm4j.api.Parameter;
import prm4j.api.Symbol;
import prm4j.api.fsm.FSM;
import prm4j.api.fsm.FSMSpec;

public class StatefulSpecProcessorTest extends AbstractTest {

    @Test
    public void accessors_unsafeMapIterator() throws Exception {
	FSM<Void> fsm = new FSM_unsafeMapIterator().fsm;
	StatefulSpecProcessor ssp = new StatefulSpecProcessor(new FSMSpec<Void>(fsm));

	assertEquals(ssp.getInitialState(), fsm.getInitialState());
	assertEquals(ssp.getSymbols(), fsm.getAlphabet().getSymbols());
    }

    @Test
    public void getPropertyEnableSets_unsafeMapIterator() throws Exception {
	FSM_unsafeMapIterator u = new FSM_unsafeMapIterator();
	FSM<Void> fsm = u.fsm;
	StatefulSpecProcessor ssp = new StatefulSpecProcessor(new FSMSpec<Void>(fsm));

	Map<Symbol, Set<Set<Symbol>>> actual = ssp.getPropertyEnableSets();

	Map<Symbol, Set<Set<Symbol>>> expected = new HashMap<Symbol, Set<Set<Symbol>>>();
	for (Symbol symbol : u.alphabet.getSymbols()) {
	    expected.put(symbol, new HashSet<Set<Symbol>>());
	}
	expected.get(u.createColl).add(Collections.<Symbol> emptySet());
	expected.get(u.createIter).add(asSet(u.createColl));
	expected.get(u.createIter).add(asSet(u.createColl, u.updateMap));
	expected.get(u.useIter).add(asSet(u.createColl, u.createIter));
	expected.get(u.useIter).add(asSet(u.createColl, u.createIter, u.updateMap));
	expected.get(u.updateMap).add(asSet(u.createColl));
	expected.get(u.updateMap).add(asSet(u.createColl, u.createIter));
	expected.get(u.updateMap).add(asSet(u.createColl, u.createIter, u.useIter));

	assertEquals(expected, actual);
    }

    @Test
    public void getParameterEnableSets_unsafeMapIterator() throws Exception {
	FSM_unsafeMapIterator u = new FSM_unsafeMapIterator();
	FSM<Void> fsm = u.fsm;
	StatefulSpecProcessor ssp = new StatefulSpecProcessor(new FSMSpec<Void>(fsm));

	Map<Symbol, Set<Set<Parameter<?>>>> actual = ssp.getParameterEnableSets();

	Map<Symbol, Set<Set<Parameter<?>>>> expected = new HashMap<Symbol, Set<Set<Parameter<?>>>>();
	for (Symbol symbol : u.alphabet.getSymbols()) {
	    expected.put(symbol, new HashSet<Set<Parameter<?>>>());
	}
	expected.get(u.createColl).add(Collections.<Parameter<?>> emptySet());
	expected.get(u.createIter).add(asSet(u.m, u.c));
	expected.get(u.useIter).add(asSet(u.m, u.c, u.i));
	expected.get(u.updateMap).add(asSet(u.m, u.c));
	expected.get(u.updateMap).add(asSet(u.m, u.c, u.i));

	assertEquals(expected, actual);
    }

    @Test
    public void getStatePropertyCoEnableSets_unsafeMapIterator() throws Exception {
	FSM_unsafeMapIterator u = new FSM_unsafeMapIterator();
	FSM<Void> fsm = u.fsm;
	StatefulSpecProcessor ssp = new StatefulSpecProcessor(new FSMSpec<Void>(fsm));

	Map<MonitorState<?>, Set<Set<Symbol>>> actual = ssp.getStatePropertyCoEnableSets();

	Map<MonitorState<?>, Set<Set<Symbol>>> expected = new HashMap<MonitorState<?>, Set<Set<Symbol>>>();
	for (MonitorState<?> state : u.fsm.getStates()){
	    expected.put(state, new HashSet<Set<Symbol>>());
	}
	expected.get(u.initial).add(asSet(u.createColl, u.createIter, u.updateMap, u.useIter));
	expected.get(u.s1).add(asSet(u.createIter, u.useIter, u.updateMap));
	expected.get(u.s2).add(asSet(u.useIter, u.updateMap));
	expected.get(u.s3).add(asSet(u.useIter));
	expected.get(u.error).add(Collections.<Symbol> emptySet());

	assertEquals(expected, actual);
    }

}
