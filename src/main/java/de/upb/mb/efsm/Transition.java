package de.upb.mb.efsm;

import java.util.Collections;
import java.util.Set;


/**
 * @author Manuel Benz
 * created on 20.02.18
 */
abstract class Transition<State, Parameter, Context> {

  private State src;
  private State tgt;

  public boolean isFeasible(Parameter input,Context context) {
    return inputGuard(input, context) && domainGuard(context);
  }

  /**
   * Assumes {@link Transition#isFeasible(Object, Context)} was called and returned true to work properly
   *
   * @return A set of output values
   */
  public Set<Parameter> take(Parameter input, Context context) {
    Set<Parameter> apply = operation(input, context);

    if (apply == null) {
      return Collections.emptySet();
    }

    return apply;
  }

  /**
   * Tries to take the transition by calling {@link Transition#isFeasible(Object, Context)} itself and returning null if the transition cannot be taken.
   *
   * @param input
   * @param context
   * @return An (potentially empty) set of output values or null if the transition is infeasible.
   */
  public Set<Parameter> tryTake(Parameter input, Context context) {
    if (isFeasible(input, context)) {
      return take(input, context);
    } else {
      return null;
    }
  }

  public State getSrc() {
    return src;
  }

  protected void setSrc(State src) {
    this.src = src;
  }

  public State getTgt() {
    return tgt;
  }

  protected void setTgt(State tgt) {
    this.tgt = tgt;
  }

  protected abstract boolean inputGuard(Parameter input, Context context);

  protected abstract boolean domainGuard(Context context);

  protected abstract Set<Parameter> operation(Parameter input, Context context);
}
