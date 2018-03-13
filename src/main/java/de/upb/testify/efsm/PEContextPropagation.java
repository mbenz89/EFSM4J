package de.upb.testify.efsm;

import pathexpression.IRegEx;

import java.util.Collection;

/**
 * @author Manuel Benz
 * created on 13.03.18
 */
public class PEContextPropagation<State, Parameter, Context extends IEFSMContext<Context>, Transition extends de.upb.testify.efsm.Transition<State, Parameter, Context>> extends PEBasedFPAlgo<State, Parameter, Context, Transition> {
  public PEContextPropagation(EFSM<State, Parameter, Context, Transition> efsm) {
    super(efsm);
  }

  public PEContextPropagation(EFSM<State, Parameter, Context, Transition> efsm, int maxDepth) {
    super(efsm, maxDepth);
  }

  @Override
  protected Collection<EFSMPath<State, Parameter, Context, Transition>> expressionToPath(Configuration<State, Context> config, IRegEx<Transition> pathExpression) {
    return null;
  }
}
