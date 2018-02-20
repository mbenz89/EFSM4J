package de.upb.mb.efsm;

import java.util.Set;

/**
 * simple transitions are those transitions that have no input parameter guard and no domain guard, gPi = NIL and gD = NIL.
 *
 * @author Manuel Benz
 * created on 20.02.18
 */
public abstract class SimpleTransition<State, Parameter, Variable> extends Transition<State, Parameter, Variable> {

  @Override
  protected boolean inputGuard(Parameter input, Set<Variable> context) {
    return true;
  }

  @Override
  protected boolean domainGuard(Set<Variable> context) {
    return true;
  }
}
