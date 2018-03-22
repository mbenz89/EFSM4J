package de.upb.testify.efsm.eefsm;

import de.upb.testify.efsm.IFeasiblePathAlgo;
import de.upb.testify.efsm.Param;
import de.upb.testify.efsm.State;
import org.junit.jupiter.api.Disabled;

/**
 * @author Manuel Benz
 * created on 19.03.18
 */
class BacktrackEEFSMFPAlgoTest extends AbstractEFSMFPAlgoTest {

  @Override
  protected IFeasiblePathAlgo<State, Param, EEFSMContext<Object>, ETransition<State, Param, Object>> getAlgo(EEFSM<State, Param, Object> eefsm) {
    return new BacktrackEEFSMFPAlgo<>(eefsm);
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