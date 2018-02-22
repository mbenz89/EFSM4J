package de.upb.mb.efsm.eefsm;

import de.upb.mb.efsm.EFSM;
import de.upb.mb.efsm.EFSMBuilder;
import de.upb.mb.efsm.Transition;

import java.util.Set;

/**
 * @author Manuel Benz
 * created on 22.02.18
 */
public class EEFSM<State, Input> extends EFSM<State, Input, EEFSMContext> {
  protected EEFSM(Set<State> states, State initialState, EEFSMContext initalContext, Set<Transition<State, Input, EEFSMContext>> transitions) {
    super(states, initialState, initalContext, transitions);
  }

  public static <State, Input> EFSMBuilder<State, Input, EEFSMContext, EEFSM<State, Input>> builder() {
    return new EFSMBuilder(EEFSM.class);
  }
}
