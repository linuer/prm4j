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

import prm4j.indexing.AbstractBaseMonitor;
import prm4j.indexing.staticdata.MetaNode;

public interface Node {

    public MetaNode getNodeContext();

    /**
     *
     * @return monitor matching the node's binding or <code>null</code>, if the queried path is no prefix of a final
     *         path.
     */
    public AbstractBaseMonitor getMonitor();

    public void setMonitor(AbstractBaseMonitor monitor);

    public NodeMap getNodeMap();

    /**
     * Returns a monitor set which represents a (sometimes not real) subset of instances which are more informative than
     * the instance represented by this node.
     *
     * @param setId
     *            the id of the parameter set. <br>
     *            Ids <i>should</i> be in the range [0, n].<br>
     *            Id 0 <b>must</b> select the set of all instances which are strictly more informative than this node.
     *            <p>
     *            example: this node is a1b1, set 0 can denote all a1b1c and a1b1d, where 1 denote all a1b1d and set 2
     *            all a1b1d
     * @return
     */
    public MonitorSet getMonitorSet(int parameterSetId);

    public MonitorSet[] getMonitorSets();

    /**
     * The node is used in {@link NodeMap}s with similar functionality as map entries.
     *
     * @return TODO comment
     */
    public Node next();

}