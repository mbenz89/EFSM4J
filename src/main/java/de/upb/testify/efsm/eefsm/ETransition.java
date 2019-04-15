package de.upb.testify.efsm.eefsm;

import de.upb.testify.efsm.Transition;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/** @author Manuel Benz created on 22.02.18 */
public class ETransition<State, Input, ContextObject>
    extends Transition<State, Input, EEFSMContext<ContextObject>> {

  public static final String ℂ = "\u2102";
  protected final Input expectedInput;
  protected final ContextObject[] addToContext;
  protected final ContextObject[] removeFromContext;
  protected final ContextObject[] inContext;
  protected final ContextObject[] notInContext;

  private final int cachedHash;

  protected ETransition(
      Input expectedInput,
      ContextObject[] inContext,
      ContextObject[] notInContext,
      ContextObject[] addToContext,
      ContextObject[] removeFromContext) {
    this.expectedInput = expectedInput;
    // ensure that these arrays are not empty, so that later checks against null ensure there are
    // not operations
    this.inContext = sanitize(inContext);
    this.notInContext = sanitize(notInContext);
    this.addToContext = sanitize(addToContext);
    this.removeFromContext = sanitize(removeFromContext);

    cachedHash =
        new HashCodeBuilder(17, 37)
            .append(expectedInput)
            .append(addToContext)
            .append(removeFromContext)
            .append(inContext)
            .append(notInContext)
            .append(getSrc())
            .append(getTgt())
            .toHashCode();
  }

  /**
   * Returns null if the given array is null or empty, the given array without null values
   * otherwise.
   *
   * @param array
   * @param <T>
   * @return
   */
  private static <T> T[] sanitize(T[] array) {
    if (array == null || array.length == 0) {
      return null;
    }

    final T[] res = (T[]) Arrays.stream(array).filter(t -> t != null).toArray();

    if (res.length == 0) {
      return null;
    }

    return res;
  }

  public Input getExpectedInput() {
    return expectedInput;
  }

  public Optional<ContextObject[]> getContextAdditions() {
    return Optional.ofNullable(addToContext);
  }

  public Optional<ContextObject[]> getContextRemovals() {
    return Optional.ofNullable(removeFromContext);
  }

  public Optional<ContextObject[]> getInContext() {
    return Optional.ofNullable(inContext);
  }

  public Optional<ContextObject[]> getNotInContext() {
    return Optional.ofNullable(notInContext);
  }

  @Override
  protected boolean inputGuard(Input input) {
    // we accept epsilon (expectedInput=null) by checking if expected input is input
    return Objects.equals(expectedInput, input);
  }

  @Override
  protected boolean domainGuard(EEFSMContext<ContextObject> eefsmContext) {
    return (inContext == null || eefsmContext.elementOf(inContext))
            && (notInContext == null || eefsmContext.notElementOf(notInContext));
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
      builder.append(Arrays.toString(inContext) + " \u2208 " + ℂ);
    }

    if (inContext == null && notInContext == null) {
      builder.append("-");
    } else if (inContext != null && notInContext != null) {
      builder.append(" ^ ");
    }

    if (notInContext != null) {
      builder.append(" " + Arrays.toString(notInContext) + " \u2209 " + ℂ);
    }
    //

    builder.append("\n");
    builder.append(String.join("", Collections.nCopies(builder.length() + 5, "-")) + "\n");
    if (addToContext != null || removeFromContext != null) {
      builder.append(ℂ + " = ");
      if (addToContext != null) {
        builder.append("(" + ℂ + " \u222A " + Arrays.toString(addToContext) + ")");
      }
      if (removeFromContext != null) {
        builder.append(
            (addToContext == null ? ℂ : "") + " \u2216 " + Arrays.toString(removeFromContext));
      }
    } else {
      builder.append("-");
    }

    builder.append("\n");

    return builder.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;

    if (o == null || getClass() != o.getClass()) return false;

    ETransition<?, ?, ?> that = (ETransition<?, ?, ?>) o;

    return new EqualsBuilder()
        .append(expectedInput, that.expectedInput)
        .append(addToContext, that.addToContext)
        .append(removeFromContext, that.removeFromContext)
        .append(inContext, that.inContext)
        .append(notInContext, that.notInContext)
        .append(getSrc(), that.getSrc())
        .append(getTgt(), that.getTgt())
        .isEquals();
  }

  @Override
  public int hashCode() {
    return cachedHash;
  }
}
