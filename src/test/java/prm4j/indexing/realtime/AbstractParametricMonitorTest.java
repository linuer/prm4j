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
import static org.junit.Assert.fail;

import java.util.Arrays;

import org.junit.After;

import prm4j.AbstractTest;
import prm4j.api.ParametricMonitor;
import prm4j.api.Symbol;
import prm4j.indexing.BaseMonitor;
import prm4j.indexing.StatefulMonitor;
import prm4j.indexing.staticdata.StaticDataConverter;
import prm4j.spec.FiniteParametricProperty;
import prm4j.spec.FiniteSpec;

public class AbstractParametricMonitorTest extends AbstractTest {

    public final static BoundObject _ = null;
    protected FiniteParametricProperty fpp;
    protected StaticDataConverter converter;
    protected BindingStore bindingStore;
    protected NodeStore nodeStore;
    protected BaseMonitor prototypeMonitor;
    protected ParametricMonitor pm;

    public void createDefaultParametricMonitorWithAwareComponents(FiniteSpec finiteSpec) {
	fpp = new FiniteParametricProperty(finiteSpec);
	converter = new StaticDataConverter(fpp);
	bindingStore = new DefaultBindingStore(finiteSpec.getFullParameterSet(), 1);
	nodeStore = new DefaultNodeStore(converter.getMetaTree());
	prototypeMonitor = new StatefulMonitor(fpp.getInitialState());
	pm = new DefaultParametricMonitor(bindingStore, nodeStore, prototypeMonitor, converter.getEventContext());
    }

    protected Node getNode(Object... boundObjects) {
	return nodeStore.getNode(bindingStore.getBindings(boundObjects));
    }

    protected void assertChaining(Object[] from, Object[] to) {
	Node fromNode = getNode(from);
	Node toNode = getNode(to);
	boolean contained = false;
	for (MonitorSet monitorSet : fromNode.getMonitorSets()) {
	    if (monitorSet != null && monitorSet.contains(toNode.getMonitor())) {
		if (contained) {
		    fail("to-monitor is contained in multiple monitor sets!");
		}
		contained = true;
	    }
	}
	if (!contained) {
	    fail(Arrays.toString(to) + " was not contained in any monitor set of " + Arrays.toString(from) + "!");
	}
    }

    protected void assertTrace(AwareBaseMonitor monitor, Symbol... symbols) {
	assertEquals(Arrays.asList(symbols), monitor.getBaseEventTrace());
    }

    protected void assertTrace(Object[] boundObjects, Symbol... symbols) {
	if (getNode(boundObjects) == null)
	    throw new NullPointerException("Node could not be found for " + Arrays.toString(boundObjects));
	if (getNode(boundObjects).getMonitor() == null)
	    throw new NullPointerException("No monitor stored for node " + Arrays.toString(boundObjects));
	assertEquals(Arrays.asList(symbols),
		((AwareBaseMonitor) getNode(boundObjects).getMonitor()).getBaseEventTrace());
    }

    @After
    public void cleanup() {
	converter = null;
	bindingStore = null;
	nodeStore = null;
	prototypeMonitor = null;
	pm = null;
    }

}