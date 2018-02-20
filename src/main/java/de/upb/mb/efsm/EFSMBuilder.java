package de.upb.mb.efsm;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Manuel Benz
 * created on 20.02.18
 */
public class EFSMBuilder<State, Parameter, Variable> {
  private Set<State> states = new HashSet<>();
  private State initialState;
  private Set<Transition<State, Parameter, Variable>> transitions = new HashSet<>();
  private Set<Variable> initialContext = new HashSet<>();

  public EFSMBuilder() {

  }

  public EFSMBuilder<State, Parameter, Variable> withState(State... s) {
    if (s != null) {
      states.addAll(Arrays.asList(s));
    }
    return this;
  }

  public EFSMBuilder<State, Parameter, Variable> withInitialState(State s) {
    if (initialState != null) {
      throw new IllegalStateException("Initial state already set");
    }
    initialState = s;
    states.add(initialState);
    return this;
  }

  public EFSMBuilder<State, Parameter, Variable> withTransition(State src, State tgt, Transition<State, Parameter, Variable> t) {
    states.add(src);
    states.add(tgt);

    t.setSrc(src);
    t.setTgt(tgt);

    transitions.add(t);
    return this;
  }

  public EFSMBuilder<State, Parameter, Variable> withInitialContext(Variable... context) {
    if (context != null) {
      initialContext.addAll(Arrays.asList(context));
    }

    return this;
  }

  public EFSM<State, Parameter, Variable> build() {
    if (initialState == null) {
      throw new IllegalStateException("Initial state not set");
    }

    Multimap<State, Transition<State, Parameter, Variable>> srcToTransitions = MultimapBuilder.hashKeys(states.size()).arrayListValues().build();
    Multimap<State, Transition<State, Parameter, Variable>> tgtToTransitions = MultimapBuilder.hashKeys(states.size()).arrayListValues().build();

    for (Transition<State, Parameter, Variable> transition : transitions) {
      srcToTransitions.put(transition.getSrc(), transition);
      tgtToTransitions.put(transition.getTgt(), transition);
    }

    return new EFSM<>(states, initialState, transitions, initialContext, srcToTransitions, tgtToTransitions);
  }
}
