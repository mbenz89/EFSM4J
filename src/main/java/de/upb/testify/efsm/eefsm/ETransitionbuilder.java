package de.upb.testify.efsm.eefsm;

import java.util.ArrayList;
import java.util.Arrays;

/** @author Manuel Benz created on 22.02.18 */
public class ETransitionbuilder<State, Input, ContextObject> {

  private Input expectedInput = null;
  private ContextObject expetedContext = null;
  private boolean elementOf = false;
  private ArrayList<ContextObject> addToContext = null;
  private ArrayList<ContextObject> removeFromContext = null;

  public ETransitionbuilder() {
    this.addToContext = new ArrayList<>();
    this.removeFromContext = new ArrayList<>();
  }

  /**
   * Copy constructor
   *
   * @param base
   */
  public ETransitionbuilder(ETransition<State, Input, ContextObject> base) {
    this.expectedInput = base.expectedInput;
    this.expetedContext = base.expectedContext;
    this.elementOf = base.elementOf;
    this.addToContext = new ArrayList(Arrays.asList(base.addToContext));
    this.removeFromContext = new ArrayList(Arrays.asList(base.removeFromContext));
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
    this.addToContext.addAll(Arrays.asList(addToContext));
    return this;
  }

  public ETransitionbuilder<State, Input, ContextObject> removeFromContext(ContextObject... removeFromContext) {
    this.removeFromContext.addAll(Arrays.asList(removeFromContext));
    return this;
  }

  public ETransition<State, Input, ContextObject> build() {
    return new ETransition<>(expectedInput, expetedContext, elementOf, (ContextObject[]) addToContext.toArray(),
        (ContextObject[]) removeFromContext.toArray());
  }
}
