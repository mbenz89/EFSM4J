package de.upb.mb.efsm;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Manuel Benz
 * created on 20.02.18
 */
public class EFSMBuilder<State, Parameter, Context> {
  private Set<State> states = new HashSet<>();
  private State initialState;
  private Set<Transition<State, Parameter, Context>> transitions = new HashSet<>();
  private Context initialContext;

  public EFSMBuilder() {

  }

  public EFSMBuilder<State, Parameter, Context> withState(State... s) {
    if (s != null) {
      states.addAll(Arrays.asList(s));
    }
    return this;
  }

  public EFSMBuilder<State, Parameter, Context> withInitialState(State s) {
    if (initialState != null) {
      throw new IllegalStateException("Initial state already set");
    }
    initialState = s;
    states.add(initialState);
    return this;
  }

  public EFSMBuilder<State, Parameter, Context> withTransition(State src, State tgt, Transition<State, Parameter, Context> t) {
    states.add(src);
    states.add(tgt);

    t.setSrc(src);
    t.setTgt(tgt);

    transitions.add(t);
    return this;
  }

  public EFSMBuilder<State, Parameter, Context> withInitialContext(Context initialContext) {
    this.initialContext = initialContext;
    return this;
  }

  public EFSM<State, Parameter, Context> build() {
    if (initialState == null) {
      throw new IllegalStateException("Initial state not set");
    }
    if (initialContext == null) {
      throw new IllegalStateException("Context must be initialized");
    }

    return new EFSM<>(states, initialState, initialContext, transitions);
  }
}
