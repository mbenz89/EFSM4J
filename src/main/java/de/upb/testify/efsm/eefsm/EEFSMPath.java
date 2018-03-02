package de.upb.testify.efsm.eefsm;

import de.upb.testify.efsm.EFSMPath;
import de.upb.testify.efsm.IEFSMContext;
import org.jgrapht.GraphPath;

/**
 * @author Manuel Benz
 * created on 02.03.18
 */
public class EEFSMPath<State, Input> extends EFSMPath<State, Input, ETransition<State, Input, ?>> {

  protected EEFSMPath(EEFSM eefsm) {
    super(eefsm);
  }

  protected EEFSMPath(EEFSM eefsm, EFSMPath<State, Input, ETransition<State, Input, ?>> basePath) {
    super(eefsm, basePath);
  }

  protected EEFSMPath(EEFSM eefsm, GraphPath<State, ETransition<State, Input, ?>> basePath) {
    super(eefsm, basePath);
  }

  @Override
  public boolean isFeasible(IEFSMContext context) {
    if (!(context instanceof EEFSMContext)) {
      throw new IllegalArgumentException("Context not of type EEFSMContext: " + context);
    }
    return super.isFeasible(context);
  }
}
