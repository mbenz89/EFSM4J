package de.upb.mb.efsm;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Manuel Benz
 * created on 20.02.18
 */
public class EFSMBuilder<State, Parameter, Variable, Transition extends de.upb.mb.efsm.Transition<State, Parameter, Variable>> {
  private Set<State> states = new HashSet<>();
  private State initialState;
  private Set<Transition> transitions = new HashSet<>();
  private Set<Variable> initialContext = new HashSet<>();

  public EFSMBuilder() {

  }

  public EFSMBuilder<State, Parameter, Variable, Transition> withState(State s) {
    states.add(s);
    return this;
  }

  public EFSMBuilder<State, Parameter, Variable, Transition> withInitialState(State s) {
    if (initialState != null) {
      throw new IllegalStateException("Initial state already set");
    }
    initialState = s;
    states.add(initialState);
    return this;
  }

  public EFSMBuilder<State, Parameter, Variable, Transition> withTransition(State src, State tgt, Transition t) {
    t.setSrc(src);
    t.setTgt(tgt);
    transitions.add(t);
    return this;
  }


  public EFSM<State, Parameter, Variable, Transition> build() {
    if (initialState == null) {
      throw new IllegalStateException("Initial state not set");
    }

    Multimap<State, Transition> srcToTransitions = MultimapBuilder.hashKeys(states.size()).arrayListValues().build();
    Multimap<State, Transition> tgtToTransitions = MultimapBuilder.hashKeys(states.size()).arrayListValues().build();

    for (Transition transition : transitions) {
      srcToTransitions.put(transition.getSrc(), transition);
      tgtToTransitions.put(transition.getTgt(), transition);
    }

    return new EFSM<>(states, initialState, transitions, initialContext, srcToTransitions, tgtToTransitions);
  }
}
