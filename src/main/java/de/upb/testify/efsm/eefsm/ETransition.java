package de.upb.testify.efsm.eefsm;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import de.upb.testify.efsm.Transition;

/** @author Manuel Benz created on 22.02.18 */
public class ETransition<State, Input, ContextObject> extends Transition<State, Input, EEFSMContext<ContextObject>> {

  public static final String ℂ = "\u2102";
  protected final Input expectedInput;
  protected final ContextObject[] addToContext;
  protected final ContextObject[] removeFromContext;
  protected final ContextObject[] inContext;
  protected final ContextObject[] notInContext;

  protected ETransition(Input expectedInput, ContextObject[] inContext, ContextObject[] notInContext,
      ContextObject[] addToContext, ContextObject[] removeFromContext) {
    this.expectedInput = expectedInput;
    // ensure that these arrays are not empty, so that later checks against null ensure there are
    // not operations
    this.inContext = sanitize(inContext);
    this.notInContext = sanitize(notInContext);
    this.addToContext = sanitize(addToContext);
    this.removeFromContext = sanitize(removeFromContext);
  }

  /**
   * Returns null if the given array is null or empty, the given array otherwise.
   *
   * @param array
   * @param <T>
   * @return
   */
  private <T> T[] sanitize(T[] array) {
    return array == null || array.length == 0 ? null : array;
  }

  @Override
  protected Input getExpectedInput() {
    return expectedInput;
  }

  protected Optional<ContextObject[]> getContextAdditions() {
    return Optional.ofNullable(addToContext);
  }

  protected Optional<ContextObject[]> getContextRemovals() {
    return Optional.ofNullable(removeFromContext);
  }

  protected Optional<ContextObject[]> getInContext() {
    return Optional.ofNullable(inContext);
  }

  protected Optional<ContextObject[]> getNotInContext() {
    return Optional.ofNullable(notInContext);
  }

  @Override
  protected boolean inputGuard(Input input) {
    // we accept epsilon (expectedInput=null) by checking if expected input is input
    if (expectedInput == input || (expectedInput != null && expectedInput.equals(input))) {
      return true;
    }
    return false;
  }

  @Override
  protected boolean domainGuard(EEFSMContext<ContextObject> eefsmContext) {
    if ((inContext != null && !eefsmContext.elementOf(inContext))
        || (notInContext != null && !eefsmContext.notElementOf(notInContext))) {
      return false;
    }

    return true;
  }

  @Override
  protected Set<Input> operation(Input input, EEFSMContext<ContextObject> eefsmContext) {
    if (addToContext != null) {
      eefsmContext.union(addToContext);
    }
    if (removeFromContext != null) {
      eefsmContext.remove(removeFromContext);
    }
    return null;
  }

  @Override
  public boolean hasOperation() {
    return addToContext != null || removeFromContext != null;
  }

  @Override
  public boolean hasDomainGuard() {
    return inContext != null || notInContext != null;
  }

  @Override
  public boolean hasParameterGuard() {
    return expectedInput != null;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();

    builder.append(Objects.toString(expectedInput, "-") + " \uFF0F ");
    if (inContext != null) {
      builder.append(Objects.toString(inContext) + " \u2208 " + ℂ);
    }
    if (notInContext != null) {
      builder.append(" " + Objects.toString(notInContext) + " \u2209 " + ℂ);
    }
    //

    if (inContext == null && notInContext == null) {
      builder.append("-");
    }
    builder.append("\n");
    builder.append(String.join("", Collections.nCopies(builder.length() + 5, "-")) + "\n");
    if (addToContext != null || removeFromContext != null) {
      builder.append(ℂ + " = ");
      if (addToContext != null) {
        builder.append("(" + ℂ + " \u222A " + Arrays.toString(addToContext) + ")");
      }
      if (removeFromContext != null) {
        builder.append((addToContext == null ? ℂ : "") + " \u2216 " + Arrays.toString(removeFromContext));
      }
    } else {
      builder.append("-");
    }

    builder.append("\n");

    return builder.toString();
  }
}
