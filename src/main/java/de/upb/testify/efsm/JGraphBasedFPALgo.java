package de.upb.testify.efsm;

import org.jgrapht.ListenableGraph;

import java.util.List;

/** @author Manuel Benz created on 02.03.18 */
public abstract class JGraphBasedFPALgo<
        State,
        Parameter,
        Context extends IEFSMContext<Context>,
        Transition extends de.upb.testify.efsm.Transition<State, Parameter, Context>>
    implements IFeasiblePathAlgo<State, Parameter, Context, Transition> {

  protected final ListenableGraph<State, Transition> baseGraph;
  protected final EFSM<State, Parameter, Context, Transition> efsm;

  public JGraphBasedFPALgo(EFSM<State, Parameter, Context, Transition> efsm) {
    this.efsm = efsm;
    baseGraph = efsm.getBaseGraph();
  }

  @Override
  public EFSMPath<State, Parameter, Context, Transition> getPath(State tgt) {
    return getPath(efsm.getConfiguration(), tgt);
  }

  @Override
  public List<? extends EFSMPath<State, Parameter, Context, Transition>> getPaths(State tgt) {
    return getPaths(efsm.getConfiguration(), tgt);
  }

  @Override
  public boolean pathExists(State tgt) {
    return pathExists(efsm.getConfiguration(), tgt);
  }
}
