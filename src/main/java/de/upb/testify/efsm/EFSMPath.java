package de.upb.testify.efsm;

import com.google.common.collect.Lists;
import org.jgrapht.GraphPath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Manuel Benz
 * created on 02.03.18
 */
public class EFSMPath<State, Parameter, Context extends IEFSMContext<Context>, Transition extends de.upb.testify.efsm.Transition<State, Parameter, Context>> {

  private final LinkedList<Transition> transitions;
  /**
   * our snapshot for validation
   */
  private final EFSM<State, Parameter, Context, Transition> efsm;

  protected EFSMPath(EFSM<State, Parameter, Context, Transition> efsm) {
    this.efsm = efsm;
    transitions = new LinkedList<>();
  }

  protected EFSMPath(EFSM<State, Parameter, Context, Transition> efsm, EFSMPath<State, Parameter, Context, Transition> basePath) {
    this.efsm = efsm;
    this.transitions = new LinkedList<>(basePath.transitions);
  }

  protected EFSMPath(EFSM<State, Parameter, Context, Transition> efsm, GraphPath<State, Transition> basePath) {
    this.efsm = efsm;
    transitions = new LinkedList<>(basePath.getEdgeList());
  }

  private EFSMPath(EFSM<State, Parameter, Context, Transition> efsm, List<Transition> transitions) {
    this.efsm = efsm;
    this.transitions = new LinkedList<>(transitions);
  }

  protected void appendTransition(Transition t) {
    if (!transitions.isEmpty()) {
      Transition last = transitions.getLast();
      if (last.getTgt() != t.getSrc()) {
        throw new IllegalArgumentException("The given transition does not connect to the last transition of this path");
      }
    }

    transitions.addLast(t);
  }

  protected void prependTransation(Transition t) {
    if (!transitions.isEmpty()) {
      Transition first = transitions.getFirst();
      if (first.getSrc() != t.getTgt()) {
        throw new IllegalArgumentException("The given transition does not connect to the first transition of this path");
      }
    }

    transitions.addFirst(t);
  }

  protected void appendPath(EFSMPath<State, Parameter, Context, Transition> other) {
    appendPath(other.transitions);
  }

  protected void appendPath(GraphPath<State, Transition> other) {
    appendPath(new LinkedList<>(other.getEdgeList()));
  }


  private void appendPath(LinkedList<Transition> other) {
    if (other.isEmpty()) {
      return;
    }

    ensureConnects(this.transitions, other);

    this.transitions.addAll(other);
  }

  protected void prependPath(EFSMPath<State, Parameter, Context, Transition> other) {
    prependPath(other.transitions);
  }

  protected void prependPath(GraphPath<State, Transition> other) {
    prependPath(new LinkedList<>(other.getEdgeList()));
  }


  private void prependPath(LinkedList<Transition> other) {
    if (other.isEmpty()) {
      return;
    }

    ensureConnects(other, this.transitions);

    this.transitions.addAll(0, other);
  }

  private void ensureConnects(LinkedList<Transition> head, LinkedList<Transition> tail) {
    if (!transitions.isEmpty()) {
      Transition last = head.getLast();
      Transition first = tail.getFirst();
      if (last.getTgt() != first.getSrc()) {
        throw new IllegalArgumentException("The given paths do not connect");
      }
    }
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

    states.add(transitions.getLast().getTgt());

    return states;
  }

  public boolean isEmpty() {
    return transitions.isEmpty();
  }

  public State getSrc() {
    if (transitions.isEmpty()) {
      return null;
    }
    return transitions.getFirst().getSrc();
  }

  public State getTgt() {
    if (transitions.isEmpty()) {
      return null;
    }
    return transitions.getLast().getTgt();
  }

  public int getLength() {
    return transitions.size();
  }

  public List<Parameter> getInputsToTrigger() {
    return transitions.stream().map(t -> t.getExpectedInput()).collect(Collectors.toList());
  }

  public boolean isFeasible(Context context) {
    EFSM<State, Parameter, Context, Transition> snapshot = efsm.snapshot(getSrc(), context);
    snapshot.forceConfiguration(new Configuration(getSrc(), context));
    for (Transition transition : transitions) {
      Configuration configuration = snapshot.transitionAndDrop(transition.getExpectedInput());
      if (configuration == null || !configuration.getState().equals(transition.getTgt())) {
        return false;
      }
    }
    return true;
  }

  public EFSMPath<State, Parameter, Context, Transition> subPath(int src, int tgt) {
    int size = transitions.size();
    if (src < 0 || src >= size || tgt < src || tgt > size) {
      throw new IndexOutOfBoundsException();
    }
    return new EFSMPath(efsm, transitions.subList(src, tgt));
  }

  public EFSM<State, Parameter, Context, Transition> getEfsm() {
    return efsm;
  }
}
