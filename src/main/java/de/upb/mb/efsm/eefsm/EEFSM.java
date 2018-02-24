package de.upb.mb.efsm.eefsm;

import de.upb.mb.efsm.EFSM;
import de.upb.mb.efsm.EFSMBuilder;

import java.util.Set;

/**
 * @author Manuel Benz
 * created on 22.02.18
 */
public class EEFSM<State, Input, ContextObject> extends EFSM<State, Input, EEFSMContext<ContextObject>, ETransition<State, Input, ContextObject>> {
  protected EEFSM(Set<State> states, State initialState, EEFSMContext initalContext, Set<ETransition<State, Input, ContextObject>> transitions) {
    super(states, initialState, initalContext, transitions);
  }

  public static <State, Input, ContextObject> EFSMBuilder<State, Input, EEFSMContext<ContextObject>, ETransition<State, Input, ContextObject>, EEFSM<State, Input, ContextObject>> builder() {
    return new EFSMBuilder(EEFSM.class);
  }
}
