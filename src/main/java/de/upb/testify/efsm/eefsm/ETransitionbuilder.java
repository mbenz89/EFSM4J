package de.upb.testify.efsm.eefsm;

import com.google.common.base.Preconditions;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** @author Manuel Benz created on 22.02.18 */
public class ETransitionbuilder<State, Input, ContextObject> {

  protected Input expectedInput = null;
  protected Set<ContextObject> addToContext;
  protected Set<ContextObject> removeFromContext;
  protected Set<ContextObject> inContext;
  protected Set<ContextObject> notInContext;

  public ETransitionbuilder() {
    this.addToContext = new HashSet<>();
    this.removeFromContext = new HashSet<>();
    this.inContext = new HashSet<>();
    this.notInContext = new HashSet<>();
  }

  /**
   * Copy constructor
   *
   * @param base
   */
  public ETransitionbuilder(ETransition<State, Input, ContextObject> base) {
    this.expectedInput = base.expectedInput;
    this.inContext = setFromArray(base.inContext);
    this.notInContext = setFromArray(base.notInContext);
    this.addToContext = setFromArray(base.addToContext);
    this.removeFromContext = setFromArray(base.removeFromContext);
  }

  private static <ContextObject> void checkContradiction(
      Collection<ContextObject> existing, Collection<ContextObject> toBeAdded) {
    Preconditions.checkArgument(
        Collections.disjoint(existing, toBeAdded),
        "Contradiction of context guard found. At least one of the following context objects have to be in and must not be in the context: "
            + toBeAdded);
  }

  private static <T> Set<T> setFromArray(T[] array) {
    return array == null ? new HashSet<>() : new HashSet<>(Arrays.asList(array));
  }

  public ETransitionbuilder<State, Input, ContextObject> fireOnInput(Input expectedInput) {
    this.expectedInput = expectedInput;
    return this;
  }

  public ETransitionbuilder<State, Input, ContextObject> fireIfInContext(
      ContextObject... contextVariables) {
    final List<ContextObject> toBeAdded = Arrays.asList(contextVariables);
    checkContradiction(notInContext, toBeAdded);
    inContext.addAll(toBeAdded);
    return this;
  }

  public ETransitionbuilder<State, Input, ContextObject> fireIfNotInContext(
      ContextObject... contextVariables) {
    final List<ContextObject> toBeAdded = Arrays.asList(contextVariables);
    checkContradiction(inContext, toBeAdded);
    notInContext.addAll(toBeAdded);
    return this;
  }

  public ETransitionbuilder<State, Input, ContextObject> addToContext(
      ContextObject... addToContext) {
    this.addToContext.addAll(Arrays.asList(addToContext));
    return this;
  }

  public ETransitionbuilder<State, Input, ContextObject> removeFromContext(
      ContextObject... removeFromContext) {
    this.removeFromContext.addAll(Arrays.asList(removeFromContext));
    return this;
  }

  public ETransition<State, Input, ContextObject> build() {
    return new ETransition<>(
        expectedInput,
        (ContextObject[]) inContext.toArray(),
        (ContextObject[]) notInContext.toArray(),
        (ContextObject[]) addToContext.toArray(),
        (ContextObject[]) removeFromContext.toArray());
  }

  /**
   * Merges in the other builder so that all context operations are united. The expected input is
   * only overwritten if the expected of this transition is null.
   *
   * @param other
   * @return
   */
  public ETransitionbuilder<State, Input, ContextObject> merge(
      ETransitionbuilder<State, Input, ContextObject> other) {
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
