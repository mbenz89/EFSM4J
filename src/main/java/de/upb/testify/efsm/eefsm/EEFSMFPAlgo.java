package de.upb.testify.efsm.eefsm;

import de.upb.testify.efsm.EFSMPath;
import de.upb.testify.efsm.JGraphBasedFPALgo;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.FloydWarshallShortestPaths;

/**
 * @author Manuel Benz
 * created on 02.03.18
 */
public class EEFSMFPAlgo<State> extends JGraphBasedFPALgo<State, ETransition<State, ?, ?>> {

  private final ShortestPathAlgorithm<State, ETransition<State, ?, ?>> shortestPaths;
  private final EEFSM<State, ?, ?> eefsm;

  public EEFSMFPAlgo(EEFSM<State, ?, ?> eefsm) {
    super(eefsm);
    this.eefsm = eefsm;
    shortestPaths = new FloydWarshallShortestPaths<>(this.baseGraph);
  }

  @Override
  public EFSMPath getPath(State src, State tgt) {
    EEFSMContext curContext = eefsm.getConfiguration().getContext().snapshot();

    // check if there is any path first
    GraphPath<State, ETransition<State, ?, ?>> path = shortestPaths.getPath(src, tgt);
    if (path == null) {
      return null;
    }

    // maybe we are lucky and the path is valid already
    EEFSMPath<State, ?> resultPath = new EEFSMPath(eefsm, path);
    if (resultPath.isFeasible(curContext)) {
      return resultPath;
    }

    // if we weren't lucky, there have to be domain constraints on the way which we need to find
    return fixpoint(path);
  }

  private EFSMPath fixpoint(GraphPath<State, ETransition<State, ?, ?>> path) {

    return null;
  }


}
