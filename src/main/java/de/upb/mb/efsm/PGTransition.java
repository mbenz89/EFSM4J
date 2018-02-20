package de.upb.mb.efsm;

import java.util.Set;

/**
 * gPi transitions are those transitions that have input parameter guard but not a domain guard, gPi =? NIL and gD = NIL.
 *
 * @author Manuel Benz
 * created on 20.02.18
 */
public abstract class PGTransition<State, Parameter, Variable> extends Transition<State, Parameter, Variable> {
  public PGTransition(State src, State tgt) {
    super(src, tgt);
  }

  @Override
  protected boolean domainGuard(Set<Variable> context) {
    return true;
  }
}
