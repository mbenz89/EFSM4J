package de.upb.mb.efsm;

import java.util.Collections;
import java.util.Set;

/**
 * A {@see SimpleTransition} that has no operation und thus neither output nor context changes
 *
 * @author Manuel Benz
 * created on 20.02.18
 */
public class EpsilonTransition<State, Parameter, Variable> extends SimpleTransition<State, Parameter, Variable> {
  @Override
  protected Set<Parameter> operation(Parameter input, Set<Variable> context) {
    return Collections.emptySet();
  }
}
