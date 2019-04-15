package de.upb.testify.efsm.eefsm;

import de.upb.testify.efsm.EFSMPath;
import org.jgrapht.GraphPath;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

/** @author Manuel Benz created on 02.03.18 */
public class EEFSMPath<State, Input, ContextObject>
    extends EFSMPath<
        State, Input, EEFSMContext<ContextObject>, ETransition<State, Input, ContextObject>> {

  private final EEFSM<State, Input, ContextObject> eefsm;

  protected EEFSMPath(EEFSM<State, Input, ContextObject> eefsm) {
    super();
    this.eefsm = eefsm;
  }

  protected EEFSMPath(
      EEFSM<State, Input, ContextObject> eefsm, EEFSMPath<State, Input, ContextObject> basePath) {
    super(basePath);
    this.eefsm = eefsm;
  }

  protected EEFSMPath(
      EEFSM<State, Input, ContextObject> eefsm,
      GraphPath<State, ETransition<State, Input, ContextObject>> basePath) {
    super(basePath);
    this.eefsm = eefsm;
  }

  protected EEFSMPath(
      EEFSM<State, Input, ContextObject> eefsm,
      List<ETransition<State, Input, ContextObject>> basePath) {
    super(basePath);
    this.eefsm = eefsm;
  }

  @Override
  protected void append(ETransition t) {
    super.append(t);
  }

  @Override
  protected void prepend(ETransition t) {
    super.prepend(t);
  }

  @Override
  protected void append(
      EFSMPath<State, Input, EEFSMContext<ContextObject>, ETransition<State, Input, ContextObject>>
          other) {
    super.append(other);
  }

  @Override
  protected void append(GraphPath<State, ETransition<State, Input, ContextObject>> other) {
    super.append(other);
  }

  @Override
  protected void prepend(
      EFSMPath<State, Input, EEFSMContext<ContextObject>, ETransition<State, Input, ContextObject>>
          other) {
    super.prepend(other);
  }

  @Override
  protected void prepend(GraphPath<State, ETransition<State, Input, ContextObject>> other) {
    super.prepend(other);
  }

  public Iterator<Input> getInputsToTrigger() {
    return transitions.stream().map(t -> t.getExpectedInput()).iterator();
  }

  public boolean isFeasible(EEFSMContext<ContextObject> context) {
    // we just create a snapshot of the original efsm and check if we can transition from the path's
    // src to target in the given context
    final EEFSM<State, Input, ContextObject> eefsm = this.eefsm.snapshot(getSrc(), context);
    for (ETransition<State, Input, ContextObject> transition : transitions) {
      // do not use transition and drop here, it is too expensive. also access curstate directly to
      // omit building a configuration
      Set<Input> out = eefsm.transition(transition.getExpectedInput());
      if (out == null || !eefsm.getCurrentState().equals(transition.getTgt())) {
        return false;
      }
    }
    return true;
  }

  public EEFSM<State, Input, ContextObject> getEefsm() {
    return eefsm;
  }
}
