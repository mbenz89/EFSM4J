package de.upb.testify.efsm;

import org.jgrapht.ListenableGraph;

/**
 * @author Manuel Benz
 * created on 02.03.18
 */
public abstract class JGraphBasedFPALgo<State, Transition extends de.upb.testify.efsm.Transition<State, ?, ?>> implements IFeasiblePathAlgo<State, Transition> {

  protected final ListenableGraph baseGraph;

  public JGraphBasedFPALgo(EFSM efsm) {
    baseGraph = efsm.getBaseGraph();
  }

}
