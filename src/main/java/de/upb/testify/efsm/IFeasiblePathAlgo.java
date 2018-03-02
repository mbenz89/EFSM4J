package de.upb.testify.efsm;

/**
 * @author Manuel Benz
 * created on 02.03.18
 */
public interface IFeasiblePathAlgo<State, Transition extends de.upb.testify.efsm.Transition<State, ?, ?>> {
  /**
   * Returns a path between he given States, or null if no path exists.
   * If a path is valid depends on the semantics of the underlying implementation, e.g., feasible path.
   *
   * @param src
   * @param tgt
   * @return
   */
  EFSMPath<State, Transition> getPath(State src, State tgt);
}
