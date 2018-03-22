package de.upb.testify.efsm;

import java.util.List;

/**
 * @author Manuel Benz
 * created on 02.03.18
 */
public interface IFeasiblePathAlgo<State, Parameter, Context extends IEFSMContext<Context>, Transition extends de.upb.testify.efsm.Transition<State, Parameter, Context>> {
  /**
   * Returns a feasible path between the current state of the efsm and the given target state, or null if no path exists.
   * If a path is feasible depends on the semantics of the underlying efsm implementation.
   * The algorithm will assume the current configuration of the efsm as start configuration.
   * <p>
   * Note: The implementation of this interface has to decide if any feasible path or the shortest feasible path will be returned.
   *
   * @param tgt
   * @return A feasible path or null if non exists
   */
  EFSMPath<State, Parameter, Context, Transition> getPath(State tgt);

  /**
   * Returns a feasible path between the given state of the efsm and the given target state, or null if no path exists.
   * If a path is feasible depends on the semantics of the underlying efsm implementation.
   * The algorithm will assume given configuration of the efsm as start configuration.
   * <p>
   * Note: The implementation of this interface has to decide if any feasible path or the shortest feasible path will be returned.
   *
   * @param tgt
   * @return A feasible path or null if non exists
   * @config The configuration from which a path should be calculated
   */
  EFSMPath<State, Parameter, Context, Transition> getPath(Configuration<State, Context> config, State tgt);


  /**
   * Returns a set feasible path between the given state of the efsm and the given target state, or null if no path exists.
   * If a path is feasible depends on the semantics of the underlying efsm implementation.
   * The algorithm will assume the current configuration of the efsm as start configuration.
   * <p>
   * Note: The implementation of this interface has to decide which paths to returen, e.g., all feasible path or a subset.
   *
   * @param tgt
   * @return A set of feasible path (not necessarily all) or null if non exists
   */
  List<EFSMPath<State, Parameter, Context, Transition>> getPaths(State tgt);


  /**
   * Returns a set feasible path between the given state of the efsm and the given target state, or null if no path exists.
   * If a path is feasible depends on the semantics of the underlying efsm implementation.
   * The algorithm will assume given configuration of the efsm as start configuration.
   * <p>
   * Note: The implementation of this interface has to decide which paths to returen, e.g., all feasible path or a subset
   *
   * @param tgt
   * @return A set of feasible path (not necessarily all) or null if non exists
   * @config The configuration from which a path should be calculated
   */
  List<EFSMPath<State, Parameter, Context, Transition>> getPaths(Configuration<State, Context> config, State tgt);

  boolean pathExists(State tgt);

  boolean pathExists(Configuration<State, Context> config, State tgt);


}
