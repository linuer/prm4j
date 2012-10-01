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
package prm4j.indexing.treebased.impl;

import prm4j.indexing.Event;
import prm4j.indexing.IndexingStrategy;
import prm4j.indexing.treebased.BindingStore;
import prm4j.indexing.treebased.LowLevelBinding;
import prm4j.indexing.treebased.Node;
import prm4j.indexing.treebased.NodeStore;

public class FastIndexingStrategy<A> implements IndexingStrategy<A> {

    private BindingStore<A> bindingStore;
    private NodeStore<A> nodeStore;

    @Override
    public void processEvent(Event<A> event) {
	final LowLevelBinding<A>[] bindings = bindingStore.getBindings(event.getBoundObjects());
	final Node<A> instanceNode = nodeStore.getNode(bindings);

	// TODO

    }

}
