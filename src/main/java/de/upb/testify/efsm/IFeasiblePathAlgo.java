package de.upb.testify.efsm;

/**
 * @author Manuel Benz
 * created on 02.03.18
 */
public interface IFeasiblePathAlgo<State extends Comparable<State>, Parameter, Context extends IEFSMContext<Context>, Transition extends de.upb.testify.efsm.Transition<State, Parameter, Context>> {
  /**
   * Returns a path between the given state, or null if no path exists.
   * If a path is valid depends on the semantics of the underlying implementation, e.g., feasible path.
   *
   * @param tgt
   * @return
   * @config The configuration from which a path should be calculated
   */
  EFSMPath<State, Parameter, Context, Transition> getPath(Configuration<State, Context> config, State tgt);

  /**
   * Returns a path between the current state of the efsm, or null if no path exists.
   * If a path is valid depends on the semantics of the underlying implementation, e.g., feasible path.
   * The path will always asume the current configuration of the efsm as start.
   *
   * @param tgt
   * @return
   */
  EFSMPath<State, Parameter, Context, Transition> getPath(State tgt);
}
