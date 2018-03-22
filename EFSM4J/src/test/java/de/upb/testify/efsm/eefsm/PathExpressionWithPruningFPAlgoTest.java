package de.upb.testify.efsm.eefsm;

import de.upb.testify.efsm.IFeasiblePathAlgo;
import de.upb.testify.efsm.Param;
import de.upb.testify.efsm.PathExpressionWithPruningFPAlgo;
import de.upb.testify.efsm.State;
import org.junit.jupiter.api.Disabled;

/**
 * @author Manuel Benz
 * created on 19.03.18
 */
class PathExpressionWithPruningFPAlgoTest extends AbstractEFSMFPAlgoTest {

  @Override
  protected IFeasiblePathAlgo<State, Param, EEFSMContext<Object>, ETransition<State, Param, Object>> getAlgo(EEFSM<State, Param, Object> eefsm) {
    return new PathExpressionWithPruningFPAlgo<>(eefsm);
  }

  @Override
  void c1ToHx() {
    super.c1ToHx();
  }

  @Override
  void c1ToHxWithOnStop() {
    super.c1ToHxWithOnStop();
  }

  @Override
  void infeasiblePath() {
    super.infeasiblePath();
  }

  @Override
  @Disabled
  void largeEFSMPE() {
    super.largeEFSMPE();
  }

  @Override
  @Disabled
  void largeEFSMPE2() {
    super.largeEFSMPE2();
  }

  @Override
  @Disabled
  void largeEFSMPE_InfeasibleAfterPath() {
    super.largeEFSMPE_InfeasibleAfterPath();
  }
}