package de.upb.testify.efsm.eefsm;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
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
  public EEFSMPath<State, Input, ContextObject> getPath(State src, State tgt) {
    // check if there is any path first
    GraphPath<State, ETransition<State, Input, ContextObject>> path = shortestPaths.getPath(src, tgt);
    if (path == null) {
      return null;
    }

    return backTrack(new EEFSMPath(eefsm, path), 0);
  }

  /**
   * @param srcToTarget
   * @param index       Count of transitions that were already traversed and for which the domain guards are already satisfied in the current path
   * @return
   */
  private EEFSMPath<State, Input, ContextObject> backTrack(EEFSMPath<State, Input, ContextObject> srcToTarget, int index) {
    // FIXME The context has to come from the outside
    EEFSMContext curContext = eefsm.getConfiguration().getContext().snapshot();
    if (srcToTarget.isFeasible(curContext)) {
      return srcToTarget;
    }

    // we start at a point in the path that
    List<ETransition<State, Input, ContextObject>> transitions = srcToTarget.getTransitions();
    ListIterator<ETransition<State, Input, ContextObject>> reverseIter = transitions.listIterator(transitions.size() - index);

    while (reverseIter.hasPrevious()) {
      ETransition<State, Input, ContextObject> previous = reverseIter.previous();
      index++;

      if (previous.hasDomainGuard()) {
        ContextObject expectedContext = previous.getExpectedContext();
        if (previous.isElementOfGuard()) {
          Collection<ETransition<State, Input, ContextObject>> adders = contextToAdders.get(expectedContext);
          if (adders == null) {
            throw new IllegalStateException("Not satisfiable");
          }

          for (ETransition<State, ?, ?> adder : adders) {
            State intermediate = adder.getSrc();
            GraphPath<State, ETransition<State, Input, ContextObject>> intermediateToPrevious = shortestPaths.getPath(intermediate, previous.getSrc());
            if (intermediateToPrevious != null) {
              GraphPath<State, ETransition<State, Input, ContextObject>> srcToIntermediate = shortestPaths.getPath(srcToTarget.getSrc(), intermediate);
              if (srcToIntermediate != null) {
                EEFSMPath<State, Input, ContextObject> newResult = new EEFSMPath(eefsm, srcToIntermediate);
                newResult.appendPath(intermediateToPrevious);
                EFSMPath<State, Input, EEFSMContext<ContextObject>, ETransition<State, Input, ContextObject>> previousToTgt = srcToTarget.subPath(srcToTarget.getLength() - index, srcToTarget.getLength());
                newResult.appendPath(previousToTgt);
                EEFSMPath<State, Input, ContextObject> result = backTrack(newResult, index);
                if (result != null) {
                  return result;
                }
              }
            }
          }


        } else {

        }
      }

    }

    return null;
  }

}
