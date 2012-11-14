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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;

import org.junit.After;

import prm4j.AbstractTest;
import prm4j.api.Symbol;
import prm4j.indexing.staticdata.StaticDataConverter;
import prm4j.spec.FiniteParametricProperty;
import prm4j.spec.FiniteSpec;

public class AbstractDefaultParametricMonitorTest extends AbstractTest {

    protected StaticDataConverter converter;
    protected AwareDefaultBindingStore bindingStore;
    protected AwareDefaultNodeStore nodeStore;
    protected AwareBaseMonitor prototypeMonitor;
    protected DefaultParametricMonitor pm;

    protected AwareBaseMonitor monitor; // working variable

    public void createDefaultParametricMonitorWithAwareComponents(FiniteSpec finiteSpec) {
	converter = new StaticDataConverter(new FiniteParametricProperty(finiteSpec));
	bindingStore = new AwareDefaultBindingStore(finiteSpec.getFullParameterSet(), 1);
	nodeStore = new AwareDefaultNodeStore(converter.getMetaTree());
	prototypeMonitor = new AwareBaseMonitor(finiteSpec.getInitialState());
	pm = new DefaultParametricMonitor(bindingStore, nodeStore, prototypeMonitor, converter.getEventContext());
    }

    protected AwareBaseMonitor popNextUpdatedMonitor() {
	if (prototypeMonitor.getUpdatedMonitors().isEmpty())
	    fail("There were no more updated monitors!");
	return prototypeMonitor.getUpdatedMonitors().pop();
    }

    protected AwareBaseMonitor popNextCreatedMonitor() {
	if (prototypeMonitor.getCreatedMonitors().isEmpty())
	    fail("There were no more created monitors!");
	return prototypeMonitor.getCreatedMonitors().pop();
    }

    protected Node popNextRetrievedNode() {
	if (nodeStore.getRetrievedNodes().isEmpty())
	    fail("There were no more retrieved nodes!");
	return nodeStore.getRetrievedNodes().pop().get();
    }

    protected void assertBoundObjects(AwareBaseMonitor monitor, Object... boundObjects) {
	LowLevelBinding[] bindings = monitor.getLowLevelBindings();
	Object[] monitorBoundObjects = new Object[bindings.length];
	for (int i = 0; i < bindings.length; i++) {
	    monitorBoundObjects[i] = bindings[i].get();
	}
	assertArrayEquals(boundObjects, monitorBoundObjects);
    }

    protected void assertNoMoreUpdatedMonitors() {
	assertTrue("There were more updated monitors: " + prototypeMonitor.getUpdatedMonitors(), prototypeMonitor
		.getUpdatedMonitors().isEmpty());
    }

    protected void assertNoMoreCreatedMonitors() {
	assertTrue("There were more created monitors: " + prototypeMonitor.getCreatedMonitors(), prototypeMonitor
		.getCreatedMonitors().isEmpty());
    }

    protected void assertNoMoreRetrievedNodes() {
	assertTrue("There were more retrieved nodes: " + nodeStore.getRetrievedNodes(), nodeStore.getRetrievedNodes()
		.isEmpty());
    }

    protected void assertTrace(AwareBaseMonitor monitor, Symbol... symbols) {
	assertEquals(Arrays.asList(symbols), monitor.getBaseEventTrace());
    }

    @After
    public void cleanup() {
	converter = null;
	bindingStore = null;
	nodeStore = null;
	prototypeMonitor = null;
	pm = null;
	monitor = null;
    }

}
