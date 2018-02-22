package de.upb.mb.efsm.eefsm;

/**
 * @author Manuel Benz
 * created on 22.02.18
 */
public class ETransitionbuilder<State, Input> {


  private EEFSMContext.ContextObject[] removeFromContext = null;
  private EEFSMContext.ContextObject expetedContext = null;
  private Input expectedInput = null;
  private boolean elementOf = false;
  private EEFSMContext.ContextObject[] addToContext = null;

  public ETransitionbuilder() {
  }

  public ETransitionbuilder<State, Input> fireOnInput(Input expectedInput) {
    this.expectedInput = expectedInput;
    return this;
  }

  public ETransitionbuilder<State, Input> fireIfInContext(EEFSMContext.ContextObject o) {
    this.expetedContext = o;
    this.elementOf = true;
    return this;
  }

  public ETransitionbuilder<State, Input> fireIfNotInContext(EEFSMContext.ContextObject o) {
    this.expetedContext = o;
    this.elementOf = false;
    return this;
  }

  public ETransitionbuilder<State, Input> addToContext(EEFSMContext.ContextObject... addToContext) {
    this.addToContext = addToContext;
    return this;
  }


  public ETransitionbuilder<State, Input> removeFromContext(EEFSMContext.ContextObject... removeFromContext) {
    this.removeFromContext = removeFromContext;
    return this;
  }

  public ETransition<State, Input> build() {
    return new ETransition<>(expectedInput, expetedContext, elementOf, addToContext, removeFromContext);
  }
}
