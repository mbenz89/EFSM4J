package de.upb.mb.efsm;

import java.util.Set;

/**
 * @author Manuel Benz
 * created on 20.02.18
 */
public class Objects {
  static State state0 = new State("0");
  static State state1 = new State("1");
  static State state2 = new State("2");

  static Variable uninitialized = new Variable("uninitialized");
  static Variable initialized = new Variable("initialized");
  static Variable running = new Variable("running");

  static SimpleTransition<State, Boolean, Variable> trans1 = new SimpleTransition<State, Boolean, Variable>() {
    @Override
    protected Set<Boolean> operation(Boolean input, Set<Variable> context) {
      context.remove(uninitialized);
      context.add(initialized);
      return null;
    }
  };
  static PGDGTransition<State, Boolean, Variable> trans2 = new PGDGTransition<State, Boolean, Variable>() {

    @Override
    protected boolean inputGuard(Boolean input, Set<Variable> context) {
      return input;
    }

    @Override
    protected boolean domainGuard(Set<Variable> context) {
      return context.contains(initialized);
    }

    @Override
    protected Set<Boolean> operation(Boolean input, Set<Variable> context) {
      context.remove(initialized);
      context.add(running);
      return null;
    }
  };
  static DGTransition<State, Boolean, Variable> trans3 = new DGTransition<State, Boolean, Variable>() {
    @Override
    protected boolean domainGuard(Set<Variable> context) {
      return context.contains(running);
    }

    @Override
    protected Set<Boolean> operation(Boolean input, Set<Variable> context) {
      context.remove(running);
      context.add(initialized);
      return null;
    }
  };
  private static EFSMBuilder<State, Boolean, Variable> builder = new EFSMBuilder<>();

  static EFSM<State, Boolean, Variable> efsm() {
    return builder.withInitialState(state0)
        .withInitialContext(uninitialized)
        .withTransition(state0, state1, trans1)
        .withTransition(state1, state2, trans2)
        .withTransition(state2, state1, trans3).build();
  }
}
