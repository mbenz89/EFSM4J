package de.upb.testify.efsm.eefsm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** @author Manuel Benz created on 22.02.18 */
public class ETransitionbuilder<State, Input, ContextObject> {

  private Input expectedInput = null;
  private List<ContextObject> addToContext;
  private List<ContextObject> removeFromContext;
  private List<ContextObject> inContext;
  private List<ContextObject> notInContext;

  public ETransitionbuilder() {
    this.addToContext = new ArrayList<>();
    this.removeFromContext = new ArrayList<>();
    this.inContext = new ArrayList<>();
    this.notInContext = new ArrayList<>();
  }

  /**
   * Copy constructor
   *
   * @param base
   */
  public ETransitionbuilder(ETransition<State, Input, ContextObject> base) {
    this.expectedInput = base.expectedInput;
    this.inContext = listFromArray(base.inContext);
    this.notInContext = listFromArray(base.notInContext);
    this.addToContext = listFromArray(base.addToContext);
    this.removeFromContext = listFromArray(base.removeFromContext);
  }

  private List<ContextObject> listFromArray(ContextObject[] array) {
    return array == null ? new ArrayList<>() : new ArrayList<>(Arrays.asList(array));
  }

  public ETransitionbuilder<State, Input, ContextObject> fireOnInput(Input expectedInput) {
    this.expectedInput = expectedInput;
    return this;
  }

  public ETransitionbuilder<State, Input, ContextObject> fireIfInContext(ContextObject... contextVariables) {
    inContext.addAll(Arrays.asList(contextVariables));
    return this;
  }

  public ETransitionbuilder<State, Input, ContextObject> fireIfNotInContext(ContextObject... contextVariables) {
    notInContext.addAll(Arrays.asList(contextVariables));
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
    return new ETransition<>(expectedInput, (ContextObject[]) inContext.toArray(), (ContextObject[]) notInContext.toArray(),
        (ContextObject[]) addToContext.toArray(), (ContextObject[]) removeFromContext.toArray());
  }

  /**
   * Merges in the other builder so that all context operations are united. The expected input is only overwritten if the
   * expected of this transition is null.
   * 
   * @param other
   * @return
   */
  public ETransitionbuilder<State, Input, ContextObject> merge(ETransitionbuilder<State, Input, ContextObject> other) {
    addToContext.addAll(other.addToContext);
    removeFromContext.addAll(other.removeFromContext);
    inContext.addAll(other.inContext);
    notInContext.addAll(other.notInContext);

    if (expectedInput == null) {
      expectedInput = other.expectedInput;
    }
    return this;
  }
}
