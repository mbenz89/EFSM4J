package de.upb.testify.efsm.eefsm;

import de.upb.testify.efsm.IFeasiblePathAlgo;
import de.upb.testify.efsm.Param;
import de.upb.testify.efsm.State;

/**
 * @author Manuel Benz
 * created on 26.03.18
 */
public class GraphExplosionFeasiblePathExistsTest extends AbstractEFSMPathExistsTest {
  @Override
  protected IFeasiblePathAlgo<State, Param, EEFSMContext<Object>, ETransition<State, Param, Object>> getAlgo(EEFSM<State, Param, Object> eefsm) {
    return new GraphExplosionFeasiblePathAlgorithm(eefsm);
  }
}
