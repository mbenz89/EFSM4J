package de.upb.mb.efsm;

/**
 * gPi transitions are those transitions that have input parameter guard but not a domain guard, gPi =? NIL and gD = NIL.
 *
 * @author Manuel Benz
 * created on 20.02.18
 */
public abstract class PGTransition<State, Parameter, Context> extends Transition<State, Parameter, Context> {

  @Override
  protected boolean domainGuard(Context context) {
    return true;
  }

  @Override
  public boolean isPGTransition() {
    return true;
  }

  @Override
  public boolean isDGTransition() {
    return false;
  }

  @Override
  public boolean isPGDGTransition() {
    return false;
  }

  @Override
  public boolean isSimpleTransition() {
    return false;
  }
}
