package de.upb.testify.efsm;

import org.jgrapht.ListenableGraph;

/**
 * @author Manuel Benz
 * created on 02.03.18
 */
public abstract class JGraphBasedFPALgo<State, Parameter, Context extends IEFSMContext<Context>, Transition extends de.upb.testify.efsm.Transition<State, Parameter, Context>> implements IFeasiblePathAlgo<State, Parameter, Context, Transition> {

  protected final ListenableGraph baseGraph;

  public JGraphBasedFPALgo(EFSM<State, Parameter, Context, Transition> efsm) {
    baseGraph = efsm.getBaseGraph();
  }

}
