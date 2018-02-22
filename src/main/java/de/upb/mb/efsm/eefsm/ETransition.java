package de.upb.mb.efsm.eefsm;

import de.upb.mb.efsm.Transition;

import java.util.Arrays;
import java.util.Collections;
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
    this.addToContext = addToContext;
    this.removeFromContext = removeFromContext;
  }

  @Override
  protected boolean inputGuard(Input input) {
    if (expectedInput == null || expectedInput.equals(input)) {
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
  public boolean isDGTransition() {
    return expectedInput == null && expectedContext != null;
  }

  @Override
  public boolean isPGTransition() {
    return expectedInput != null && expectedContext == null;
  }

  @Override
  public boolean isPGDGTransition() {
    return expectedInput != null && expectedContext != null;
  }

  @Override
  public boolean isSimpleTransition() {
    return expectedInput == null && expectedContext == null;
  }

  @Override
  public boolean isEpsilonTransition() {
    return isSimpleTransition() && addToContext == null && removeFromContext == null;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();

    builder.append((expectedInput == null ? "-" : expectedInput) + " \uFF0F " + (expectedContext == null ? "-" : expectedContext));
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

    return builder.toString();
  }
}
