package de.upb.testify.efsm.eefsm;

import com.google.common.base.Stopwatch;
import de.upb.testify.efsm.IFeasiblePathAlgo;
import de.upb.testify.efsm.Param;
import de.upb.testify.efsm.State;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.Duration;

/** @author Manuel Benz created on 26.03.18 */
class GraphExplosionFeasiblePathAlgorithmTest extends AbstractEEFSMFPAlgoTest {

  @Override
  protected IEEFSMFeasiblePathAlgo<State, Param, Object> getAlgo(
      EEFSM<State, Param, Object> eefsm) {
    return new GraphExplosionFeasiblePathAlgorithm(eefsm);
  }

  @Test
  @Disabled
  void doItOften() {
    LargeInterComponentExample example = new LargeInterComponentExample();
    EEFSM<State, Param, Object> e = example.eefsm;
    IFeasiblePathAlgo<State, Param, EEFSMContext<Object>, ETransition<State, Param, Object>> sfp =
        getAlgo(e);
    for (int i = 0; i < 300; i++) {
      sfp.getPath(example.example6.oR2);
    }
  }

  @Test
  void MediumEFSMsingleSourceShortestPath() {
    MediumInterComponentExample example = new MediumInterComponentExample();
    EEFSM<State, Param, Object> e = example.eefsm;

    final GraphExplosionFeasiblePathAlgorithm<State, Param, Object> sfp =
        new GraphExplosionFeasiblePathAlgorithm<>(e);

    Stopwatch sw = Stopwatch.createStarted();

    final GraphExplosionFeasiblePathAlgorithm<State, Param, Object>.SingleSourceShortestPath sssp =
        sfp.getSingleSourceShortestPath();

    final Duration computationTime = sw.elapsed();
    sw.reset().start();

    Assertions.assertNotNull(sssp.getPath(example.example1.oR1));
    Assertions.assertNotNull(sssp.getPath(example.example1.UIy));
    Assertions.assertNotNull(sssp.getPath(example.example1.Hf));
    Assertions.assertNotNull(sssp.getPath(example.example1.oR2));
    Assertions.assertNotNull(sssp.getPath(example.example2.oC1));
    Assertions.assertNotNull(sssp.getPath(example.example2.Hc));
    Assertions.assertNotNull(sssp.getPath(example.example2.oSta1));
    Assertions.assertNotNull(sssp.getPath(example.example2.oR1));
    Assertions.assertNotNull(sssp.getPath(example.example2.UI));
    Assertions.assertNotNull(sssp.getPath(example.example2.oR2));

    Assertions.assertTrue(sssp.getLength(example.example1.oR1) > 0);
    Assertions.assertTrue(sssp.getLength(example.example1.UIy) > 0);
    Assertions.assertTrue(sssp.getLength(example.example1.Hf) > 0);
    Assertions.assertTrue(sssp.getLength(example.example1.oR2) > 0);
    Assertions.assertTrue(sssp.getLength(example.example2.oC1) > 0);
    Assertions.assertTrue(sssp.getLength(example.example2.Hc) > 0);
    Assertions.assertTrue(sssp.getLength(example.example2.oSta1) > 0);
    Assertions.assertTrue(sssp.getLength(example.example2.oR1) > 0);
    Assertions.assertTrue(sssp.getLength(example.example2.UI) > 0);
    Assertions.assertTrue(sssp.getLength(example.example2.oR2) > 0);

    final Duration queryTime = sw.elapsed();

    Assertions.assertTrue(
        computationTime.compareTo(queryTime) > 0,
        String.format(
            "Computation time (%s) smaller than query time (%s)", computationTime, queryTime));
  }
}
