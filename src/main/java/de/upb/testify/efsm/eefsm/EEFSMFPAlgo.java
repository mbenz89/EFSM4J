package de.upb.testify.efsm.eefsm;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import de.upb.testify.efsm.Configuration;
import de.upb.testify.efsm.EFSMPath;
import de.upb.testify.efsm.JGraphBasedFPALgo;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.interfaces.KShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.KShortestPaths;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Manuel Benz
 * created on 02.03.18
 */
public class EEFSMFPAlgo<State, Input, ContextObject> extends JGraphBasedFPALgo<State, Input, EEFSMContext<ContextObject>, ETransition<State, Input, ContextObject>> {

  private final KShortestPathAlgorithm<State, ETransition<State, Input, ContextObject>> shortestPaths;
  private final EEFSM<State, Input, ContextObject> eefsm;
  private final Multimap<ContextObject, ETransition<State, Input, ContextObject>> contextToAdders;
  private final Multimap<ContextObject, ETransition<State, Input, ContextObject>> contextToRemovers;

  private final int K = 5;

  public EEFSMFPAlgo(EEFSM<State, Input, ContextObject> eefsm) {
    super(eefsm);
    this.eefsm = eefsm;
    shortestPaths = new KShortestPaths(this.baseGraph, K);
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
    List<GraphPath<State, ETransition<State, Input, ContextObject>>> paths = shortestPaths.getPaths(config.getState(), tgt);
    if (paths.isEmpty()) {
      return null;
    }

    return backTrack(config.getContext(), toEFSMPath(paths), 0);
  }

  private List<EEFSMPath<State, Input, ContextObject>> toEFSMPath(List<GraphPath<State, ETransition<State, Input, ContextObject>>> paths) {
    return paths.stream().map(p -> new EEFSMPath<>(eefsm, p)).collect(Collectors.toList());
  }

  /**
   * @param context
   * @param possibleSolutions
   * @param depth             Count of transitions that were already traversed and for which the domain guards are already satisfied in the current path (counting from the end of the path)
   * @return
   */
  private EEFSMPath<State, Input, ContextObject> backTrack(EEFSMContext<ContextObject> context, List<EEFSMPath<State, Input, ContextObject>> possibleSolutions, int depth) {
    for (EEFSMPath<State, Input, ContextObject> solution : possibleSolutions) {
      if (solution.isFeasible(context)) {
        return solution;
      }
    }

    return possibleSolutions.parallelStream().flatMap(possibleSolution -> {
      List<ETransition<State, Input, ContextObject>> transitions = possibleSolution.getTransitions();

      int solutionDepth = depth;
      ListIterator<ETransition<State, Input, ContextObject>> reverseIter = transitions.listIterator(transitions.size() - depth);

      while (reverseIter.hasPrevious()) {
        ETransition<State, Input, ContextObject> previous = reverseIter.previous();
        solutionDepth++;

        if (previous.hasDomainGuard()) {
          ContextObject expectedContext = previous.getExpectedContext();
          Collection<ETransition<State, Input, ContextObject>> solvers;
          if (previous.isElementOfGuard()) {
            solvers = contextToAdders.get(expectedContext);
          } else {
            solvers = contextToRemovers.get(expectedContext);
          }

          if (solvers == null) {
            return Stream.empty();
          }

          for (ETransition<State, Input, ContextObject> intermediate : solvers) {
            EEFSMPath<State, Input, ContextObject> result;

            // if the intermediate we need to take is already in the not-yet evaluated sub-path, we do not add it again
            if (possibleSolution.subPath(0, possibleSolution.getLength() - solutionDepth).contains(intermediate)) {
              result = backTrack(context, possibleSolutions, solutionDepth);
            } else {
              result = backTrack(context, k2PathsOverIntermediate(possibleSolution, solutionDepth, intermediate), solutionDepth);
            }

            if (result != null) {
              return Stream.of(result);
            }
          }
        }
      }

      return Stream.empty();
    }).findAny().orElse(null);
  }

  private List<EEFSMPath<State, Input, ContextObject>> k2PathsOverIntermediate(EEFSMPath<State, Input, ContextObject> curSolution, int index, ETransition<State, ?, ?> intermediate) {
    State intermediateSrc = intermediate.getSrc();
    State intermediateTgt = intermediate.getTgt();

    int curLength = curSolution.getLength();
    int curIndex = curLength - index;

    List<GraphPath<State, ETransition<State, Input, ContextObject>>> intermediateToCurs = shortestPaths.getPaths(intermediateTgt, curSolution.getTransitionAt(curIndex).getSrc());
    EFSMPath curToTarget = curSolution.subPath(curIndex, curLength);

    List<EEFSMPath<State, Input, ContextObject>> res = new ArrayList<>();

    for (GraphPath<State, ETransition<State, Input, ContextObject>> intermediateToCur : intermediateToCurs) {
      List<GraphPath<State, ETransition<State, Input, ContextObject>>> srcToIntermediates = shortestPaths.getPaths(curSolution.getSrc(), intermediateSrc);

      for (GraphPath<State, ETransition<State, Input, ContextObject>> srcToIntermediate : srcToIntermediates) {
        // glue all the sub-parts together
        EEFSMPath<State, Input, ContextObject> newSolution = new EEFSMPath(eefsm, srcToIntermediate);
        newSolution.append(intermediate);
        newSolution.append(intermediateToCur);
        newSolution.append(curToTarget);
        res.add(newSolution);
      }
    }

    return res;
  }
}
