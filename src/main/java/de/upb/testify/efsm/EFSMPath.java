package de.upb.testify.efsm;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.jgrapht.GraphPath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Manuel Benz
 * created on 02.03.18
 */
public class EFSMPath<State, Parameter, Transition extends de.upb.testify.efsm.Transition<State, Parameter, ?>> {

  private final ArrayList<Transition> transitions;
  /**
   * our snapshot for validation
   */
  private final EFSM<State, Parameter, ?, Transition> efsmSnapshot;

  protected EFSMPath(EFSM<State, Parameter, ?, Transition> efsm) {
    this.efsmSnapshot = efsm.snapshot();
    transitions = new ArrayList<>();
  }

  protected EFSMPath(EFSM<State, Parameter, ?, Transition> efsm, EFSMPath<State, Parameter, Transition> basePath) {
    this.efsmSnapshot = efsm.snapshot();
    this.transitions = new ArrayList<>(basePath.transitions);
  }

  protected EFSMPath(EFSM<State, Parameter, ?, Transition> efsm, GraphPath<State, Transition> basePath) {
    this.efsmSnapshot = efsm.snapshot();
    transitions = new ArrayList<>(basePath.getEdgeList());
  }

  protected void addTransition(Transition t) {
    if (!transitions.isEmpty()) {
      Transition last = Iterables.getLast(transitions);
      if (last.getTgt() != t.getSrc()) {
        throw new IllegalArgumentException("The given transition does not connect to the last transition of this path");
      }
    }

    transitions.add(t);
  }

  protected void addPath(EFSMPath<State, Parameter, Transition> other) {
    if (other.isEmpty()) {
      return;
    }

    if (!transitions.isEmpty()) {
      Transition last = Iterables.getLast(transitions);
      Transition first = Iterables.getFirst(other.transitions, null);
      if (last.getTgt() != first.getSrc()) {
        throw new IllegalArgumentException("The given path does not connect to the last transition of this path");
      }
    }

    transitions.addAll(other.transitions);
  }

  public List<Transition> getTransitions() {
    if (transitions.isEmpty()) {
      return Collections.EMPTY_LIST;
    }

    return Collections.unmodifiableList(transitions);
  }

  public List<State> getStates() {
    if (transitions.isEmpty()) {
      return Collections.emptyList();
    }

    ArrayList<State> states = Lists.newArrayListWithCapacity(transitions.size() + 1);
    for (Transition transition : transitions) {
      states.add(transition.getSrc());
    }

    states.add(Iterables.getLast(transitions).getTgt());

    return states;
  }

  public boolean isEmpty() {
    return transitions.isEmpty();
  }

  public State getSrc() {
    Transition first = Iterables.getFirst(transitions, null);
    return first == null ? null : first.getSrc();
  }

  public State getTgt() {
    Transition last = Iterables.getLast(transitions, null);
    return last == null ? null : last.getTgt();
  }

  public List<Parameter> getInputsToTrigger() {
    return transitions.stream().map(t -> t.getExpectedInput()).collect(Collectors.toList());
  }

  public boolean isFeasible(IEFSMContext context) {
    efsmSnapshot.forceConfiguration(new Configuration(getSrc(), context));
    for (Transition transition : transitions) {
      Configuration configuration = efsmSnapshot.transitionAndDrop(transition.getExpectedInput());
      if (configuration == null || !configuration.getState().equals(transition.getTgt())) {
        return false;
      }
    }
    return true;
  }
}
