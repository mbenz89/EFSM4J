package de.upb.testify.efsm.eefsm;

/** @author Manuel Benz created on 23.05.18 */
public interface IETransitionBuilder<State, Input, ContextObject> {
  IETransitionBuilder<State, Input, ContextObject> fireOnInput(Input expectedInput);

  IETransitionBuilder<State, Input, ContextObject> fireIfInContext(ContextObject o);

  IETransitionBuilder<State, Input, ContextObject> fireIfNotInContext(ContextObject o);

  IETransitionBuilder<State, Input, ContextObject> addToContext(ContextObject... addToContext);

  IETransitionBuilder<State, Input, ContextObject> removeFromContext(
      ContextObject... removeFromContext);
}
