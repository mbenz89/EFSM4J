package de.upb.mb.efsm;

import java.util.Set;

/**
 * gD transitions are those transitions that have a domain guard but not an input parameter guard, gD ?= NIL and gPi = NIL.
 *
 * @author Manuel Benz
 * created on 20.02.18
 */
public abstract class DGTransition<State, Parameter, Variable> extends Transition<State, Parameter, Variable> {


  @Override
  protected boolean inputGuard(Parameter input, Set<Variable> context) {
    return true;
  }
}
