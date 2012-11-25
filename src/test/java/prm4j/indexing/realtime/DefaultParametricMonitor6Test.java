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
package prm4j.indexing.realtime;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import prm4j.api.fsm.FSMSpec;

public class DefaultParametricMonitor6Test extends AbstractParametricMonitorTest {

    FSM_unsafeMapIterator fsm;

    BoundObject m1;
    BoundObject c1;
    BoundObject c2;
    BoundObject c3;
    BoundObject i1;
    BoundObject i2;
    BoundObject i3;

    @Before
    public void init() {
	fsm = new FSM_unsafeMapIterator();
	createDefaultParametricMonitor(new FSMSpec(fsm.fsm));

	m1 = new BoundObject("m1");
	c1 = new BoundObject("c1");
	c2 = new BoundObject("c2");
	c3 = new BoundObject("c3");
	i1 = new BoundObject("i1");
	i2 = new BoundObject("i2");
	i3 = new BoundObject("i3");
    }

    @Test
    public void multipleOpenTraces_monitorSetSizeGrowsLinearly() throws Exception {

	for (int i = 0; i < 1000; i++) {
	    final ParametricInstance instance = instance(m1, _, _);
	    pm.processEvent(instance.createEvent(fsm.updateMap));
	    pm.processEvent(instance.createEvent(fsm.updateMap));
	    pm.processEvent(instance.createEvent(fsm.updateMap));
	    pm.processEvent(instance.createEvent(fsm.createColl));
	    pm.processEvent(instance.createEvent(fsm.createIter));
	    pm.processEvent(instance.createEvent(fsm.useIter));
	    pm.processEvent(instance.createEvent(fsm.useIter));
	    pm.processEvent(instance.createEvent(fsm.useIter));
	    pm.processEvent(instance.createEvent(fsm.updateMap));
	    pm.processEvent(instance.createEvent(fsm.updateMap));
	    pm.processEvent(instance.createEvent(fsm.updateMap));
	    assertEquals(i + 1, getNode(m1, _, _).getMonitorSet(0).getSize());
	}
    }

    @Test
    public void multipleAcceptingTraces_monitorSetHasAlwaysSize1() throws Exception {

	for (int i = 0; i < 1000; i++) {
	    final ParametricInstance instance = instance(m1, _, _);
	    pm.processEvent(instance.createEvent(fsm.updateMap));
	    pm.processEvent(instance.createEvent(fsm.updateMap));
	    pm.processEvent(instance.createEvent(fsm.updateMap));
	    pm.processEvent(instance.createEvent(fsm.createColl));
	    pm.processEvent(instance.createEvent(fsm.createIter));
	    pm.processEvent(instance.createEvent(fsm.useIter));
	    pm.processEvent(instance.createEvent(fsm.useIter));
	    pm.processEvent(instance.createEvent(fsm.useIter));
	    pm.processEvent(instance.createEvent(fsm.updateMap));
	    pm.processEvent(instance.createEvent(fsm.updateMap));
	    pm.processEvent(instance.createEvent(fsm.updateMap));
	    pm.processEvent(instance.createEvent(fsm.useIter)); // reaching error states terminates monitor
	    assertEquals(1, getNode(m1, _, _).getMonitorSet(0).getSize());
	}
    }

    @Test
    public void multipleAcceptingTraces_monitorWillGetCleanedUpAfterTerminationAndNotCreatedAgain() throws Exception {

	for (int i = 0; i < 1000; i++) {
	    final ParametricInstance instance = instance(m1, _, _);
	    pm.processEvent(instance.createEvent(fsm.createColl));
	    pm.processEvent(instance.createEvent(fsm.createIter));
	    pm.processEvent(instance.createEvent(fsm.updateMap));
	    pm.processEvent(instance.createEvent(fsm.useIter)); // reaching error states terminates monitor
	    pm.processEvent(instance.createEvent(fsm.updateMap)); // terminated monitor got cleaned up
	    pm.processEvent(instance.createEvent(fsm.createColl)); // does not create another monitor
	    pm.processEvent(instance.createEvent(fsm.createIter)); // "
	    pm.processEvent(instance.createEvent(fsm.updateMap)); // "
	    pm.processEvent(instance.createEvent(fsm.useIter)); // "
	    assertEquals(0, getNode(m1, _, _).getMonitorSet(0).getSize());
	}
    }

    @Test
    public void multipleAcceptingAndOpenTraces_monitorSetGrowsAsExpected() throws Exception {

	for (int i = 0; i < 1000; i++) {
	    final ParametricInstance instance = instance(m1, _, _);
	    pm.processEvent(instance.createEvent(fsm.updateMap));
	    pm.processEvent(instance.createEvent(fsm.updateMap));
	    pm.processEvent(instance.createEvent(fsm.updateMap));
	    pm.processEvent(instance.createEvent(fsm.createColl));
	    pm.processEvent(instance.createEvent(fsm.createIter));
	    pm.processEvent(instance.createEvent(fsm.useIter));
	    pm.processEvent(instance.createEvent(fsm.useIter));
	    pm.processEvent(instance.createEvent(fsm.useIter));
	    pm.processEvent(instance.createEvent(fsm.updateMap));
	    pm.processEvent(instance.createEvent(fsm.updateMap));
	    pm.processEvent(instance.createEvent(fsm.updateMap));
	    if (i % 2 == 0) {
		pm.processEvent(instance.createEvent(fsm.useIter)); // reaching error states terminates monitor
	    }
	    assertEquals(i / 2 + 1, getNode(m1, _, _).getMonitorSet(0).getSize());
	}
    }

}