package de.upb.testify.efsm;

import java.util.List;

/** @author Manuel Benz created on 02.03.18 */
public interface IFeasiblePathAlgo<
    State,
    Parameter,
    Context extends IEFSMContext<Context>,
    Transition extends de.upb.testify.efsm.Transition<State, Parameter, Context>> {
  /**
   * Returns a feasible path between the current state of the efsm and the given target state, or
   * null if no path exists. If a path is feasible depends on the semantics of the underlying efsm
   * implementation. The algorithm will assume the current configuration of the efsm as start
   * configuration.
   *
   * <p>Note: The implementation of this interface has to decide if any feasible path or the
   * shortest feasible path will be returned.
   *
   * @param tgt
   * @return A feasible path or null if non exists
   */
  EFSMPath<State, Parameter, Context, Transition> getPath(State tgt);

  /**
   * Returns a feasible path between the given state of the efsm and the given target state, or null
   * if no path exists. If a path is feasible depends on the semantics of the underlying efsm
   * implementation. The algorithm will assume given configuration of the efsm as start
   * configuration.
   *
   * <p>Note: The implementation of this interface has to decide if any feasible path or the
   * shortest feasible path will be returned.
   *
   * @param tgt
   * @return A feasible path or null if non exists
   * @config The configuration from which a path should be calculated
   */
  EFSMPath<State, Parameter, Context, Transition> getPath(
      Configuration<State, Context> config, State tgt);

  /**
   * Returns a set feasible path between the given state of the efsm and the given target state, or
   * null if no path exists. If a path is feasible depends on the semantics of the underlying efsm
   * implementation. The algorithm will assume the current configuration of the efsm as start
   * configuration.
   *
   * <p>Note: The implementation of this interface has to decide which paths to returen, e.g., all
   * feasible path or a subset.
   *
   * @param tgt
   * @return A set of feasible path (not necessarily all) or null if non exists
   */
  List<? extends EFSMPath<State, Parameter, Context, Transition>> getPaths(State tgt);

  /**
   * Returns a set feasible path between the given state of the efsm and the given target state, or
   * null if no path exists. If a path is feasible depends on the semantics of the underlying efsm
   * implementation. The algorithm will assume given configuration of the efsm as start
   * configuration.
   *
   * <p>Note: The implementation of this interface has to decide which paths to returen, e.g., all
   * feasible path or a subset
   *
   * @param tgt
   * @return A set of feasible path (not necessarily all) or null if non exists
   * @config The configuration from which a path should be calculated
   */
  List<? extends EFSMPath<State, Parameter, Context, Transition>> getPaths(
      Configuration<State, Context> config, State tgt);

  boolean pathExists(State tgt);

  boolean pathExists(Configuration<State, Context> config, State tgt);

  interface SingleSourceShortestPath<
      State,
      Parameter,
      Context extends IEFSMContext<Context>,
      Transition extends de.upb.testify.efsm.Transition<State, Parameter, Context>> {

    /**
     * Returns a set feasible path between the source state of this {@link SingleSourceShortestPath}
     * instance and the given target state, or null if no path exists. If a path is feasible depends
     * on the semantics of the underlying efsm implementation.
     *
     * <p>Note: The implementation of this interface has to decide which paths to returen, e.g., all
     * feasible path or a subset.
     *
     * @param tgt
     * @return A set of feasible path (not necessarily all) or null if non exists
     */
    List<? extends EFSMPath<State, Parameter, Context, Transition>> getPaths(State tgt);

    /**
     * Returns a feasible path between the source state of this {@link SingleSourceShortestPath}
     * instance and the given target state, or null if no path exists. If a path is feasible depends
     * on the semantics of the underlying efsm implementation.
     *
     * <p>Note: The implementation of this interface has to decide if any feasible path or the
     * shortest feasible path will be returned.
     *
     * <p>The implementation of this method should correspond to {@link
     * SingleSourceShortestPath#getLength(Object)}, i.e. should return the path for which {@link
     * SingleSourceShortestPath#getLength(Object)} would return the its length.
     *
     * @param tgt
     * @return A feasible path or null if non exists
     */
    EFSMPath<State, Parameter, Context, Transition> getPath(State tgt);

    /**
     * Returns the length of a feasible path between the source state of this {@link
     * SingleSourceShortestPath} instance and the given target state, or -1 if no path exists. If a
     * path is feasible depends on the semantics of the underlying efsm implementation.
     *
     * <p>Note: The implementation of this interface has to decide if the lenght of any feasible
     * path or the shortest feasible path will be returned.
     *
     * <p>The implementation of this method should correspond to {@link
     * SingleSourceShortestPath#getPath(Object)}, i.e. should return the length of the path that
     * would be returned by {@link SingleSourceShortestPath#getPath(Object)}.
     *
     * @param tgt
     * @return A feasible path or null if non exists
     */
    int getLength(State tgt);

    /** @return The source configuration of this {@link SingleSourceShortestPath} instance */
    Configuration<State, Context> getSource();
  }
}
