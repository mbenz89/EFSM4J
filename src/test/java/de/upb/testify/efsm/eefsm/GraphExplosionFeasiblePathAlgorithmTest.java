package de.upb.testify.efsm.eefsm;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import de.upb.testify.efsm.IFeasiblePathAlgo;
import de.upb.testify.efsm.Param;
import de.upb.testify.efsm.State;

/** @author Manuel Benz created on 26.03.18 */
class GraphExplosionFeasiblePathAlgorithmTest extends AbstractEFSMFPAlgoTest {

  @Override
  protected IFeasiblePathAlgo<State, Param, EEFSMContext<Object>, ETransition<State, Param, Object>>
      getAlgo(EEFSM<State, Param, Object> eefsm) {
    return new GraphExplosionFeasiblePathAlgorithm<>(eefsm);
  }

  @Test
  @Disabled
  void doItOften() {
    LargeInterComponentExample example = new LargeInterComponentExample();
    EEFSM<State, Param, Object> e = example.eefsm;
    IFeasiblePathAlgo<State, Param, EEFSMContext<Object>, ETransition<State, Param, Object>> sfp = getAlgo(e);
    for (int i = 0; i < 300; i++) {
      sfp.getPath(example.example6.oR2);
    }
  }
}
