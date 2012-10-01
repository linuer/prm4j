/*
 * Copyright (c) 2012 Mateusz Parzonka, Eric Bodden
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Eric Bodden - initial API and implementation
 * Mateusz Parzonka - adapted API and implementation
 */
package prm4j.api.fsm;

import prm4j.api.Alphabet;
import prm4j.api.MatchHandler;
import prm4j.api.Symbol;
import prm4j.logic.MonitorState;

/**
 * @param <A>
 *            the type of the auxiliary data usable by base monitors
 */
public class FSMState<A> implements MonitorState<A> {

    private FSMState<A>[] successorTable;
    private final boolean isFinal;
    private final String label;// for display purposes only
    private final Alphabet alphabet;
    private final MatchHandler matchHandler;

    @SuppressWarnings("unchecked")
    public FSMState(Alphabet alphabet, boolean isFinal, MatchHandler matchHandler, String label) {
	this.isFinal = isFinal;
	this.label = label;
	this.alphabet = alphabet;
	this.matchHandler = matchHandler;
	this.successorTable = new FSMState[alphabet.size()];
    }

    public void addTransition(Symbol symbol, FSMState<A> successor) {
	assert this.successorTable[symbol.getIndex()] == null : "successor already set";
	if (!this.alphabet.getSymbols().contains(symbol)) {
	    throw new IllegalArgumentException("Symbol for transition is not contained in alphabet!");
	}
	this.successorTable[symbol.getIndex()] = successor;
    }

    @Override
    public MonitorState<A> getSuccessor(Symbol symbol) {
	return this.successorTable[symbol.getIndex()];
    }

    @Override
    public String toString() {
	return this.label;
    }

    @Override
    public boolean isFinal() {
	return this.isFinal;
    }

    @Override
    public MatchHandler getMatchHandler() {
	return this.matchHandler;
    }

}