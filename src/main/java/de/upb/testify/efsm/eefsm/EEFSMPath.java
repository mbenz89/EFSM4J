package de.upb.testify.efsm.eefsm;

import de.upb.testify.efsm.EFSMPath;
import org.jgrapht.GraphPath;

/**
 * @author Manuel Benz
 * created on 02.03.18
 */
public class EEFSMPath<State extends Comparable<State>, Input, ContextObject> extends EFSMPath<State, Input, EEFSMContext<ContextObject>, ETransition<State, Input, ContextObject>> {

  protected EEFSMPath(EEFSM<State, Input, ContextObject> eefsm) {
    super(eefsm);
  }

  protected EEFSMPath(EEFSM<State, Input, ContextObject> eefsm, EEFSMPath<State, Input, ContextObject> basePath) {
    super(eefsm, basePath);
  }

  protected EEFSMPath(EEFSM<State, Input, ContextObject> eefsm, GraphPath<State, ETransition<State, Input, ContextObject>> basePath) {
    super(eefsm, basePath);
  } 


  @Override
  public boolean isFeasible(EEFSMContext<ContextObject> context) {
    if (!(context instanceof EEFSMContext)) {
      throw new IllegalArgumentException("Context not of type EEFSMContext: " + context);
    }
    return super.isFeasible(context);
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
  protected void append(EFSMPath<State, Input, EEFSMContext<ContextObject>, ETransition<State, Input, ContextObject>> other) {
    super.append(other);
  }

  @Override
  protected void append(GraphPath<State, ETransition<State, Input, ContextObject>> other) {
    super.append(other);
  }

  @Override
  protected void prepend(EFSMPath<State, Input, EEFSMContext<ContextObject>, ETransition<State, Input, ContextObject>> other) {
    super.prepend(other);
  }

  @Override
  protected void prepend(GraphPath<State, ETransition<State, Input, ContextObject>> other) {
    super.prepend(other);
  }
}
