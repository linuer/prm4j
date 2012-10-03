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
package prm4j.logic.treebased;

import prm4j.indexing.treebased.MonitorSet;
import prm4j.indexing.treebased.Node;
import prm4j.indexing.treebased.NodeMap;

/**
 * Every {@link Node} is equipped with a MetaNode, containing factory methods and providing statically computed
 * algorithm logic.
 *
 * @param <A>
 *            the type of the auxiliary data usable by base monitors
 */
public interface MetaNode<A> {

    public ChainingData[] getChainingData();

    public MonitorSet<A> createMonitorSet();

    public Node<A> createNode();

    public Node<A> createNode(int parameterId);

    public NodeMap<A> createNodeMap();

}