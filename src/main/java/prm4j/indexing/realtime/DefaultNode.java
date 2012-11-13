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

import prm4j.indexing.BaseMonitor;
import prm4j.indexing.map.MinimalMap;
import prm4j.indexing.staticdata.MetaNode;

public class DefaultNode extends MinimalMap<LowLevelBinding, Node> implements Node {

    private final MetaNode metaNode;
    private final MonitorSet[] monitorSets;
    private BaseMonitor monitor;

    private final LowLevelBinding key;
    private final int hashCode;
    private Node nextNode;

    public DefaultNode(MetaNode metaNode, LowLevelBinding key, int hashCode) {
	this.metaNode = metaNode;
	this.key = key;
	this.hashCode = hashCode;
	monitorSets = new MonitorSet[metaNode.getMonitorSetCount()];
    }

    @Override
    public MetaNode getMetaNode() {
	return metaNode;
    }

    @Override
    public BaseMonitor getMonitor() {
	return monitor;
    }

    @Override
    public Node getNode(LowLevelBinding binding) {
	return get(binding);
    }

    @Override
    public void setMonitor(BaseMonitor monitor) {
	this.monitor = monitor;
    }

    @Override
    public MonitorSet getMonitorSet(int parameterSetId) {
	// lazy creation
	MonitorSet monitorSet = monitorSets[parameterSetId];
	if (monitorSet == null) {
	    monitorSet = new MonitorSet();
	    monitorSets[parameterSetId] = monitorSet;
	}
	return monitorSet;
    }

    @Override
    public MonitorSet[] getMonitorSets() {
	return monitorSets;
    }

    @Override
    public int getHashCode() {
	return hashCode;
    }

    @Override
    public LowLevelBinding getKey() {
	return key;
    }

    @Override
    public Node next() {
	return nextNode;
    }

    @Override
    public void setNext(Node nextNode) {
	this.nextNode = nextNode;

    }

    @Override
    protected Node[] createTable(int size) {
	return new Node[size];
    }

    @Override
    protected Node createEntry(LowLevelBinding key, int hashCode) {
	return getMetaNode().createNode(key.getParameterId(), key, hashCode);
    }


}
