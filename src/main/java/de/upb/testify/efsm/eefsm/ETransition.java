package de.upb.testify.efsm.eefsm;

import de.upb.testify.efsm.Transition;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * @author Manuel Benz
 * created on 22.02.18
 */
public class ETransition<State, Input, ContextObject> extends Transition<State, Input, EEFSMContext<ContextObject>> {

  public static final String ℂ = "\u2102";
  private final Input expectedInput;
  private final ContextObject expectedContext;
  private final boolean elementOf;
  private final ContextObject[] addToContext;
  private final ContextObject[] removeFromContext;

  public ETransition(Input expectedInput, ContextObject expectedContext, boolean elementOf, ContextObject[] addToContext, ContextObject[] removeFromContext) {
    this.expectedInput = expectedInput;
    this.expectedContext = expectedContext;
    this.elementOf = elementOf;
    // ensure that these arrays are not empty, so that later checks against null ensure there are not operations
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

  protected ContextObject getExpectedContext() {
    return expectedContext;
  }

  protected boolean isElementOfGuard() {
    return elementOf;
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
    if (expectedContext == null || (elementOf ? eefsmContext.elementOf(expectedContext) : eefsmContext.notElementOf(expectedContext))) {
      return true;
    }
    return false;
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
    return expectedContext != null;
  }

  @Override
  public boolean hasParameterGuard() {
    return expectedInput != null;
  }


  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();

    builder.append(Objects.toString(expectedInput, "-") + " \uFF0F " + Objects.toString(expectedContext, "-"));
    if (expectedContext != null) {
      builder.append((elementOf ? " \u2208 " : " \u2209 ") + ℂ);
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
//FIXME return builder
    // return builder.toString();
    return Objects.toString(expectedInput, "-");
  }
}
