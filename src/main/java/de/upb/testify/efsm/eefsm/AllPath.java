package de.upb.testify.efsm.eefsm;

import de.upb.testify.efsm.Configuration;
import de.upb.testify.efsm.JGraphBasedFPALgo;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;

import java.util.List;

/**
 * @author Manuel Benz
 * created on 06.03.18
 */
public class AllPath<State, Input, ContextObject> extends JGraphBasedFPALgo<State, Input, EEFSMContext<ContextObject>, ETransition<State, Input, ContextObject>> {

  private final AllDirectedPaths<State, ETransition<State, Input, ContextObject>> allDirectedPaths;

  public AllPath(EEFSM<State, Input, ContextObject> efsm) {
    super(efsm);
    allDirectedPaths = new AllDirectedPaths<>(baseGraph);
  }

  @Override
  public EEFSMPath<State, Input, ContextObject> getPath(Configuration<State, EEFSMContext<ContextObject>> config, State tgt) {
    List<GraphPath<State, ETransition<State, Input, ContextObject>>> allPaths = allDirectedPaths.getAllPaths(config.getState(), tgt, false, 20);
    for (GraphPath<State, ETransition<State, Input, ContextObject>> allPath : allPaths) {
      EEFSMPath<State, Input, ContextObject> eefsmPath = new EEFSMPath((EEFSM) efsm, allPath);
      if (eefsmPath.isFeasible(config.getContext())) {
        return eefsmPath;
      }
    }

    return null;
  }

  @Override
  public EEFSMPath<State, Input, ContextObject> getPath(State tgt) {
    return (EEFSMPath<State, Input, ContextObject>) super.getPath(tgt);
  }
}
