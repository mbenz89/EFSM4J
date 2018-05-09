package de.upb.testify.efsm;

import com.google.common.collect.Lists;
import org.jgrapht.GraphPath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;

/** @author Manuel Benz created on 02.03.18 */
public class EFSMPath<
        State,
        Parameter,
        Context extends IEFSMContext<Context>,
        Transition extends de.upb.testify.efsm.Transition<State, Parameter, Context>>
    implements Iterable<Transition> {

  private final LinkedList<Transition> transitions;
  /** our snapshot for validation */
  private final EFSM<State, Parameter, Context, Transition> efsm;

  protected EFSMPath(EFSM<State, Parameter, Context, Transition> efsm) {
    this.efsm = efsm;
    transitions = new LinkedList<>();
  }

  protected EFSMPath(
      EFSM<State, Parameter, Context, Transition> efsm,
      EFSMPath<State, Parameter, Context, Transition> basePath) {
    this.efsm = efsm;
    this.transitions = new LinkedList<>(basePath.transitions);
  }

  protected EFSMPath(
      EFSM<State, Parameter, Context, Transition> efsm, GraphPath<State, Transition> basePath) {
    this.efsm = efsm;
    transitions = new LinkedList<>(basePath.getEdgeList());
  }

  protected EFSMPath(
      EFSM<State, Parameter, Context, Transition> efsm, Collection<Transition> transitions) {
    this.efsm = efsm;
    this.transitions = new LinkedList<>(transitions);
  }

  protected EFSMPath(EFSM<State, Parameter, Context, Transition> efsm, Transition... transitions) {
    this.efsm = efsm;
    this.transitions = new LinkedList<>(Arrays.asList(transitions));
  }

  public EFSMPath(
      EFSM<State, Parameter, Context, Transition> efsm,
      EFSMPath<State, Parameter, Context, Transition> basePath,
      Transition t) {
    this(efsm, basePath);
    transitions.add(t);
  }

  protected void append(Transition t) {
    if (!transitions.isEmpty()) {
      Transition last = transitions.getLast();
      if (last.getTgt() != t.getSrc()) {
        throw new IllegalArgumentException(
            "The given transition does not connect to the last transition of this path");
      }
    }

    transitions.addLast(t);
  }

  protected void append(EFSMPath<State, Parameter, Context, Transition> other) {
    append(other.transitions);
  }

  protected void append(GraphPath<State, Transition> other) {
    append(new LinkedList<>(other.getEdgeList()));
  }

  private void append(LinkedList<Transition> other) {
    if (other.isEmpty()) {
      return;
    }

    ensureConnects(this.transitions, other);

    this.transitions.addAll(other);
  }

  protected void prepend(Transition t) {
    if (!transitions.isEmpty()) {
      Transition first = transitions.getFirst();
      if (first.getSrc() != t.getTgt()) {
        throw new IllegalArgumentException(
            "The given transition does not connect to the first transition of this path");
      }
    }

    transitions.addFirst(t);
  }

  protected void prepend(EFSMPath<State, Parameter, Context, Transition> other) {
    prepend(other.transitions);
  }

  protected void prepend(GraphPath<State, Transition> other) {
    prepend(new LinkedList<>(other.getEdgeList()));
  }

  private void prepend(LinkedList<Transition> other) {
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

  public Transition getTransitionAt(int index) {
    return transitions.get(index);
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

  public boolean contains(Transition t) {
    return transitions.contains(t);
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

  public Iterator<Parameter> getInputsToTrigger() {
    return transitions.stream().map(t -> t.getExpectedInput()).iterator();
  }

  public boolean isFeasible(Context context) {
    // we just create a snapshot of the original efsm and check if we can transition from the path's
    // src to target in the given context
    EFSM<State, Parameter, Context, Transition> snapshot = efsm.snapshot(getSrc(), context);
    for (Transition transition : transitions) {
      // do not use transition and drop here, it is too expensive. also access curstate directly to
      // omit building a configuratin
      Set<Parameter> out = snapshot.transition(transition.getExpectedInput());
      if (out == null || !snapshot.curState.equals(transition.getTgt())) {
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

  @Override
  public Iterator<Transition> iterator() {
    return transitions.iterator();
  }

  @Override
  public void forEach(Consumer<? super Transition> action) {
    transitions.forEach(action);
  }

  @Override
  public Spliterator<Transition> spliterator() {
    return transitions.spliterator();
  }

  @Override
  public String toString() {
    return transitions.toString();
  }
}
