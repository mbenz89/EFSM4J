package de.upb.mb.efsm;

/**
 * simple transitions are those transitions that have no input parameter guard and no domain guard, gPi = NIL and gD = NIL.
 *
 * @author Manuel Benz
 * created on 20.02.18
 */
public abstract class SimpleTransition<State, Parameter, Context> extends Transition<State, Parameter, Context> {

  @Override
  protected boolean inputGuard(Parameter input) {
    return true;
  }

  @Override
  protected boolean domainGuard(Context context) {
    return true;
  }

  @Override
  public boolean isSimpleTransition() {
    return true;
  }

  @Override
  public boolean isDGTransition() {
    return false;
  }

  @Override
  public boolean isPGTransition() {
    return false;
  }

  @Override
  public boolean isPGDGTransition() {
    return false;
  }
}
