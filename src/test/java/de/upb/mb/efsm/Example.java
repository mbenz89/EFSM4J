package de.upb.mb.efsm;

import java.util.Set;

/**
 * @author Manuel Benz
 * created on 20.02.18
 */
public class Example {
  State state0 = new State("0");
  State state1 = new State("1");
  State state2 = new State("2");

  Object uninitialized = new Object();
  Object initialized = new Object();
  Object running = new Object();

  SimpleTransition<State, Boolean, Context> trans1 = new SimpleTransition<State, Boolean, Context>() {
    @Override
    protected Set<Boolean> operation(Boolean input, Context context) {
      context.remove(uninitialized);
      context.add(initialized);
      return null;
    }

    @Override
    public boolean hasOperation() {
      return true;
    }
  };

  PGDGTransition<State, Boolean, Context> trans2 = new PGDGTransition<State, Boolean, Context>() {

    @Override
    protected boolean inputGuard(Boolean input) {
      return input != null && input;
    }

    @Override
    protected boolean domainGuard(Context context) {
      return context.contains(initialized);
    }

    @Override
    protected Set<Boolean> operation(Boolean input, Context context) {
      context.remove(initialized);
      context.add(running);
      return null;
    }

    @Override
    public boolean hasOperation() {
      return true;
    }

  };
  DGTransition<State, Boolean, Context> trans3 = new DGTransition<State, Boolean, Context>() {
    @Override
    protected boolean domainGuard(Context context) {
      return context.contains(running);
    }

    @Override
    protected Set<Boolean> operation(Boolean input, Context context) {
      context.remove(running);
      context.add(initialized);
      return null;
    }

    @Override
    public boolean hasOperation() {
      return true;
    }
  };
  EFSM<State, Boolean, Context, Transition<State, Boolean, Context>> efsm;
  private EFSMBuilder<State, Boolean, Context,
      Transition<State, Boolean, Context>,
      EFSM<State, Boolean, Context, Transition<State, Boolean, Context>>> builder = new EFSMBuilder(EFSM.class);

  public Example() {
    efsm = builder.withInitialState(state0)
        .withInitialContext(new Context(uninitialized))
        .withTransition(state0, state1, trans1)
        .withTransition(state1, state2, trans2)
        .withTransition(state2, state1, trans3).build();
  }
}
