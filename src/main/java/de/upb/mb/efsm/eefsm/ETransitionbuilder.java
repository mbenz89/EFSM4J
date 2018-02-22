package de.upb.mb.efsm.eefsm;

/**
 * @author Manuel Benz
 * created on 22.02.18
 */
public class ETransitionbuilder<State, Input, ContextObject> {


  private ContextObject[] removeFromContext = null;
  private ContextObject expetedContext = null;
  private Input expectedInput = null;
  private boolean elementOf = false;
  private ContextObject[] addToContext = null;

  public ETransitionbuilder() {
  }

  public ETransitionbuilder<State, Input, ContextObject> fireOnInput(Input expectedInput) {
    this.expectedInput = expectedInput;
    return this;
  }

  public ETransitionbuilder<State, Input, ContextObject> fireIfInContext(ContextObject o) {
    this.expetedContext = o;
    this.elementOf = true;
    return this;
  }

  public ETransitionbuilder<State, Input, ContextObject> fireIfNotInContext(ContextObject o) {
    this.expetedContext = o;
    this.elementOf = false;
    return this;
  }

  public ETransitionbuilder<State, Input, ContextObject> addToContext(ContextObject... addToContext) {
    this.addToContext = addToContext;
    return this;
  }


  public ETransitionbuilder<State, Input, ContextObject> removeFromContext(ContextObject... removeFromContext) {
    this.removeFromContext = removeFromContext;
    return this;
  }

  public ETransition<State, Input, ContextObject> build() {
    return new ETransition<>(expectedInput, expetedContext, elementOf, addToContext, removeFromContext);
  }
}
