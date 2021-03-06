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
package prm4j.indexing.binding;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

public abstract class AbstractBinding extends WeakReference<Object> implements Binding {

    private final int hashCode;
    private Binding next;

    public AbstractBinding(Object boundObject, int hashCode, ReferenceQueue<Object> q) {
	super(boundObject, q);
	this.hashCode = hashCode;
    }

    /**
     * {@inheritDoc}
     * 
     * The key used in the {@link BindingStore} is the object.
     */
    @Override
    public Object getKey() {
	return get();
    }

    @Override
    public int hashCode() {
	return hashCode;
    }

    @Override
    public Binding next() {
	return next;
    }

    @Override
    public void setNext(Binding next) {
	this.next = next;
    }

    @Override
    public String toString() {
	return "Binding(" + hashCode + ")=" + get();
    }

}
