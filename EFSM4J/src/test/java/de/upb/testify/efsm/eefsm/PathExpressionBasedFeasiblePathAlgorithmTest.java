package de.upb.testify.efsm.eefsm;

import de.upb.testify.efsm.Configuration;
import de.upb.testify.efsm.EFSMPath;
import de.upb.testify.efsm.Param;
import de.upb.testify.efsm.PathExpressionBasedFeasiblePathAlgorithm;
import de.upb.testify.efsm.State;
import pathexpression.IRegEx;

import java.util.stream.Stream;

/**
 * @author Manuel Benz
 * created on 19.03.18
 */
class PathExpressionBasedFeasiblePathAlgorithmTest extends AbstractEFSMPathExistsTest {


  @Override
  protected PathExpressionBasedFeasiblePathAlgorithm<State, Param, EEFSMContext<Object>, ETransition<State, Param, Object>> getAlgo(EEFSM<State, Param, Object> eefsm) {
    return new PathExpressionBasedFeasiblePathAlgorithm<State, Param, EEFSMContext<Object>, ETransition<State, Param, Object>>(eefsm) {
      @Override
      protected Stream<EFSMPath<State, Param, EEFSMContext<Object>, ETransition<State, Param, Object>>> expressionToPath(Configuration<State, EEFSMContext<Object>> config, IRegEx<ETransition<State, Param, Object>> pathExpression) {
        throw new UnsupportedOperationException();
      }
    };
  }

}