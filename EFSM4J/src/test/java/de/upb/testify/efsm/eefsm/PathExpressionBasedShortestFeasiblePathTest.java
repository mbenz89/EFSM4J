package de.upb.testify.efsm.eefsm;

import de.upb.testify.efsm.IFeasiblePathAlgo;
import de.upb.testify.efsm.Param;
import de.upb.testify.efsm.PathExpressionBasedShortestFeasiblePath;
import de.upb.testify.efsm.State;

/**
 * @author Manuel Benz
 * created on 21.03.18
 */
class PathExpressionBasedShortestFeasiblePathTest extends AbstractEFSMFPAlgoTest {

  @Override
  protected IFeasiblePathAlgo<State, Param, EEFSMContext<Object>, ETransition<State, Param, Object>> getAlgo(EEFSM<State, Param, Object> eefsm) {
    return new PathExpressionBasedShortestFeasiblePath<>(eefsm);
  }
}