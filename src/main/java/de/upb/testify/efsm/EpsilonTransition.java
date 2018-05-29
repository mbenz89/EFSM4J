package de.upb.testify.efsm;

import java.util.Collections;
import java.util.Set;

/**
 * A {@see SimpleTransition} that has no operation und thus neither output nor context changes
 *
 * @author Manuel Benz created on 20.02.18
 */
public class EpsilonTransition<State, Parameter, Context> extends SimpleTransition<State, Parameter, Context> {
  @Override
  protected Set<Parameter> operation(Parameter input, Context context) {
    return Collections.emptySet();
  }

  @Override
  public boolean hasOperation() {
    return false;
  }
}
