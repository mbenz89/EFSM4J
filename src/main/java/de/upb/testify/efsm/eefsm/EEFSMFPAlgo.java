package de.upb.testify.efsm.eefsm;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import de.upb.testify.efsm.Configuration;
import de.upb.testify.efsm.EFSMPath;
import de.upb.testify.efsm.JGraphBasedFPALgo;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.FloydWarshallShortestPaths;

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

/**
 * @author Manuel Benz
 * created on 02.03.18
 */
public class EEFSMFPAlgo<State, Input, ContextObject> extends JGraphBasedFPALgo<State, Input, EEFSMContext<ContextObject>, ETransition<State, Input, ContextObject>> {

  private final ShortestPathAlgorithm<State, ETransition<State, Input, ContextObject>> shortestPaths;
  private final EEFSM<State, Input, ContextObject> eefsm;
  private final Multimap<ContextObject, ETransition<State, Input, ContextObject>> contextToAdders;
  private final Multimap<ContextObject, ETransition<State, Input, ContextObject>> contextToRemovers;

  public EEFSMFPAlgo(EEFSM<State, Input, ContextObject> eefsm) {
    super(eefsm);
    this.eefsm = eefsm;
    shortestPaths = new FloydWarshallShortestPaths<>(this.baseGraph);
    contextToAdders = MultimapBuilder.hashKeys().arrayListValues().build();
    contextToRemovers = MultimapBuilder.hashKeys().arrayListValues().build();

    for (ETransition<State, Input, ContextObject> transition : eefsm.getTransitons()) {
      transition.getContextAdditions().ifPresent(context -> {
        for (ContextObject obj : context) {
          contextToAdders.put(obj, transition);
        }
      });

      transition.getContextRemovals().ifPresent(context -> {
        for (ContextObject obj : context) {
          contextToRemovers.put(obj, transition);
        }
      });
    }
  }

  @Override
  public EEFSMPath<State, Input, ContextObject> getPath(State tgt) {
    return getPath(eefsm.getConfiguration(), tgt);
  }

  @Override
  public EEFSMPath<State, Input, ContextObject> getPath(Configuration<State, EEFSMContext<ContextObject>> config, State tgt) {
    // check if there is any path first
    GraphPath<State, ETransition<State, Input, ContextObject>> path = shortestPaths.getPath(config.getState(), tgt);
    if (path == null) {
      return null;
    }

    return backTrack(config.getContext(), new EEFSMPath(eefsm, path), 0);
  }


  /**
   * @param context
   * @param curSolution
   * @param index       Count of transitions that were already traversed and for which the domain guards are already satisfied in the current path
   * @return
   */
  private EEFSMPath<State, Input, ContextObject> backTrack(EEFSMContext<ContextObject> context, EEFSMPath<State, Input, ContextObject> curSolution, int index) {
    if (curSolution.isFeasible(context)) {
      return curSolution;
    }
    // FIXME what if it is never feasible?

    // we start at a point in the path that
    List<ETransition<State, Input, ContextObject>> transitions = curSolution.getTransitions();
    ListIterator<ETransition<State, Input, ContextObject>> reverseIter = transitions.listIterator(transitions.size() - index);

    while (reverseIter.hasPrevious()) {
      ETransition<State, Input, ContextObject> previous = reverseIter.previous();
      index++;

      if (previous.hasDomainGuard()) {
        ContextObject expectedContext = previous.getExpectedContext();
        Collection<ETransition<State, Input, ContextObject>> solvers;
        if (previous.isElementOfGuard()) {
          solvers = contextToAdders.get(expectedContext);
        } else {
          solvers = contextToRemovers.get(expectedContext);
        }

        if (solvers == null) {
          // FIXME we have to handle this correctly
          throw new IllegalStateException("Not satisfiable");
        }

        for (ETransition<State, ?, ?> intermediate : solvers) {
          EEFSMPath<State, Input, ContextObject> result = pathOverIntermediate(curSolution, index, intermediate);
          if (result != null) {
            result = backTrack(context, result, index);
            if (result != null) {
              return result;
            }
          }
        }
      }
    }

    return null;
  }

  private EEFSMPath<State, Input, ContextObject> pathOverIntermediate(EEFSMPath<State, Input, ContextObject> curSolution, int index, ETransition<State, ?, ?> intermediate) {
    State intermediateSrc = intermediate.getSrc();
    State intermediateTgt = intermediate.getTgt();

    GraphPath intermediateToPrevious = shortestPaths.getPath(intermediateTgt, curSolution.getTransitionAt(curSolution.getLength() - index).getSrc());

    if (intermediateToPrevious != null) {
      GraphPath srcToIntermediate = shortestPaths.getPath(curSolution.getSrc(), intermediateSrc);

      if (srcToIntermediate != null) {
        // glue all the sub-parts together
        EEFSMPath<State, Input, ContextObject> newSolution = new EEFSMPath(eefsm, srcToIntermediate);
        newSolution.appendTransition(intermediate);
        newSolution.appendPath(intermediateToPrevious);

        EFSMPath previousToTgt = curSolution.subPath(curSolution.getLength() - index, curSolution.getLength());
        newSolution.appendPath(previousToTgt);

        return newSolution;
      }
    }
    return null;
  }

}
