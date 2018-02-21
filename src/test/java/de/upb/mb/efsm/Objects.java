package de.upb.mb.efsm;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * @author Manuel Benz
 * created on 20.02.18
 */
public class Objects {
  static State state0 = new State("0");
  static State state1 = new State("1");
  static State state2 = new State("2");

  static Object uninitialized = new Object();
  static Object initialized = new Object();
  static Object running = new Object();

  static SimpleTransition<State, Boolean, Set<Object>> trans1 = new SimpleTransition<State, Boolean, Set<Object>>() {
    @Override
    protected Set<Boolean> operation(Boolean input, Set<Object> context) {
      context.remove(uninitialized);
      context.add(initialized);
      return null;
    }
  };
  static PGDGTransition<State, Boolean, Set<Object>> trans2 = new PGDGTransition<State, Boolean, Set<Object>>() {

    @Override
    protected boolean inputGuard(Boolean input, Set<Object> context) {
      return input;
    }

    @Override
    protected boolean domainGuard(Set<Object> context) {
      return context.contains(initialized);
    }

    @Override
    protected Set<Boolean> operation(Boolean input, Set<Object> context) {
      context.remove(initialized);
      context.add(running);
      return null;
    }
  };
  static DGTransition<State, Boolean, Set<Object>> trans3 = new DGTransition<State, Boolean, Set<Object>>() {
    @Override
    protected boolean domainGuard(Set<Object> context) {
      return context.contains(running);
    }

    @Override
    protected Set<Boolean> operation(Boolean input, Set<Object> context) {
      context.remove(running);
      context.add(initialized);
      return null;
    }
  };
  private static EFSMBuilder<State, Boolean, Set<Object>> builder = new EFSMBuilder<>();

  static EFSM<State, Boolean, Set<Object>> efsm() {
    return builder.withInitialState(state0)
        .withInitialContext(Sets.newHashSet(uninitialized))
        .withTransition(state0, state1, trans1)
        .withTransition(state1, state2, trans2)
        .withTransition(state2, state1, trans3).build();
  }
}
