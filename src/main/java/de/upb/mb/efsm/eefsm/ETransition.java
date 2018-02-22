package de.upb.mb.efsm.eefsm;

import de.upb.mb.efsm.Transition;

import java.util.Set;

/**
 * @author Manuel Benz
 * created on 22.02.18
 */
public class ETransition<State, Input> extends Transition<State, Input, EEFSMContext> {

  private final Input expectedInput;
  private final EEFSMContext.ContextObject expectedContext;
  private final boolean elementOf;
  private final EEFSMContext.ContextObject[] addToContext;
  private final EEFSMContext.ContextObject[] removeFromContext;

  public ETransition(Input expectedInput, EEFSMContext.ContextObject expectedContext, boolean elementOf, EEFSMContext.ContextObject[] addToContext, EEFSMContext.ContextObject[] removeFromContext) {
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
  protected boolean domainGuard(EEFSMContext eefsmContext) {
    if (expectedContext == null || (elementOf ? eefsmContext.elementOf(expectedContext) : eefsmContext.notElementOf(expectedContext))) {
      return true;
    }
    return false;
  }

  @Override
  protected Set<Input> operation(Input input, EEFSMContext eefsmContext) {
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
}
