package de.upb.testify.efsm.eefsm;

import de.upb.testify.efsm.EFSMPath;
import org.jgrapht.GraphPath;

/**
 * @author Manuel Benz
 * created on 02.03.18
 */
public class EEFSMPath<State, Input, ContextObject> extends EFSMPath<State, Input, EEFSMContext<ContextObject>, ETransition<State, Input, ContextObject>> {

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
  protected void appendTransition(ETransition t) {
    super.appendTransition(t);
  }

  @Override
  protected void prependTransation(ETransition t) {
    super.prependTransation(t);
  }

  @Override
  protected void appendPath(EFSMPath<State, Input, EEFSMContext<ContextObject>, ETransition<State, Input, ContextObject>> other) {
    super.appendPath(other);
  }

  @Override
  protected void appendPath(GraphPath<State, ETransition<State, Input, ContextObject>> other) {
    super.appendPath(other);
  }

  @Override
  protected void prependPath(EFSMPath<State, Input, EEFSMContext<ContextObject>, ETransition<State, Input, ContextObject>> other) {
    super.prependPath(other);
  }

  @Override
  protected void prependPath(GraphPath<State, ETransition<State, Input, ContextObject>> other) {
    super.prependPath(other);
  }
}
