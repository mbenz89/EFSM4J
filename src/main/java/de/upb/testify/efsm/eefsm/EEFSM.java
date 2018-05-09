package de.upb.testify.efsm.eefsm;

import de.upb.testify.efsm.EFSM;
import de.upb.testify.efsm.EFSMBuilder;
import org.jgrapht.Graph;

/** @author Manuel Benz created on 22.02.18 */
public class EEFSM<State, Input, ContextObject>
    extends EFSM<
        State, Input, EEFSMContext<ContextObject>, ETransition<State, Input, ContextObject>> {
  protected EEFSM(
      Graph<State, ETransition<State, Input, ContextObject>> baseGraph,
      State initialState,
      EEFSMContext initalContext) {
    super(baseGraph, initialState, initalContext);
  }

  public static <State, Input, ContextObject>
      EFSMBuilder<
              State, Input, EEFSMContext<ContextObject>, ETransition<State, Input, ContextObject>,
              EEFSM<State, Input, ContextObject>>
          builder() {
    return new EFSMBuilder(EEFSM.class);
  }

  @Override
  protected EFSM<
          State, Input, EEFSMContext<ContextObject>, ETransition<State, Input, ContextObject>>
      snapshot(State state, EEFSMContext<ContextObject> context) {
    return super.snapshot(state, context);
  }

  @Override
  protected EFSM<
          State, Input, EEFSMContext<ContextObject>, ETransition<State, Input, ContextObject>>
      snapshot() {
    return super.snapshot();
  }
}
