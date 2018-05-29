package de.upb.testify.efsm.eefsm;

import java.util.List;

import de.upb.testify.efsm.Configuration;
import de.upb.testify.efsm.IFeasiblePathAlgo;

/**
 * @author Manuel Benz created on 29.05.18
 */
public interface IEEFSMFeasiblePathAlgo<State, Input, ContextObject>
    extends IFeasiblePathAlgo<State, Input, EEFSMContext<ContextObject>, ETransition<State, Input, ContextObject>> {
  @Override
  EEFSMPath<State, Input, ContextObject> getPath(State tgt);

  @Override
  EEFSMPath<State, Input, ContextObject> getPath(Configuration<State, EEFSMContext<ContextObject>> config, State tgt);

  @Override
  List<EEFSMPath<State, Input, ContextObject>> getPaths(State tgt);

  @Override
  List<EEFSMPath<State, Input, ContextObject>> getPaths(Configuration<State, EEFSMContext<ContextObject>> config, State tgt);
}
