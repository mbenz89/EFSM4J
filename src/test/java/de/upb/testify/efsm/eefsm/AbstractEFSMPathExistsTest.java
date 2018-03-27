package de.upb.testify.efsm.eefsm;

import de.upb.testify.efsm.Configuration;
import de.upb.testify.efsm.IFeasiblePathAlgo;
import de.upb.testify.efsm.Param;
import de.upb.testify.efsm.State;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * @author Manuel Benz
 * created on 26.03.18
 */
public abstract class AbstractEFSMPathExistsTest {
  protected abstract IFeasiblePathAlgo<State, Param, EEFSMContext<Object>, ETransition<State, Param, Object>> getAlgo(EEFSM<State, Param, Object> eefsm);

  @Test
  void c1ToHx() {
    BasicInterComponentExample example = new BasicInterComponentExample();
    IFeasiblePathAlgo<State, Param, EEFSMContext<Object>, ETransition<State, Param, Object>> sfp = getAlgo(example.eefsm);
    Assertions.assertTrue(sfp.pathExists(example.Hx));
  }

  @Test
  void c1ToHxWithOnStop() {
    BasicInterComponentExample example = new BasicInterComponentExample();
    EEFSM<State, Param, Object> eefsm = example.eefsm;
    IFeasiblePathAlgo<State, Param, EEFSMContext<Object>, ETransition<State, Param, Object>> sfp = getAlgo(eefsm);
    EEFSMContext<Object> context = new EEFSMContext<>();
    context.union(example.oR2, example.Hc);

    eefsm.forceConfiguration(new Configuration(example.oSto1, context));

    Assertions.assertTrue(sfp.pathExists(example.Hx));
  }

  @Test
  void infeasiblePath() {
    InterComponentExampleInfeasible example = new InterComponentExampleInfeasible();
    EEFSM<State, Param, Object> eefsm = example.eefsm;
    IFeasiblePathAlgo<State, Param, EEFSMContext<Object>, ETransition<State, Param, Object>> sfp = getAlgo(eefsm);
    Assertions.assertFalse(sfp.pathExists(example.Hx));
  }

  @Test
  void largeEFSMPE() throws IOException {
    LargeInterComponentExample example = new LargeInterComponentExample();
    EEFSM<State, Param, Object> e = example.eefsm;

    //new EFSMDotExporter(e).writeOut(Paths.get("./target/large.dot"));

    IFeasiblePathAlgo<State, Param, EEFSMContext<Object>, ETransition<State, Param, Object>> sfp = getAlgo(e);
    Assertions.assertTrue(sfp.pathExists(example.example6.oR1));
    Assertions.assertTrue(sfp.pathExists(example.example3.oD1));
    Assertions.assertTrue(sfp.pathExists(example.tgt));
  }

  @Test
  void largeEFSMPE2() {
    LargeInterComponentExample example = new LargeInterComponentExample();
    EEFSM<State, Param, Object> e = example.eefsm;

    IFeasiblePathAlgo<State, Param, EEFSMContext<Object>, ETransition<State, Param, Object>> sfp = getAlgo(e);
    Assertions.assertTrue(sfp.pathExists(example.example1.oR2));
    Assertions.assertTrue(sfp.pathExists(example.example2.oR2));
    Assertions.assertTrue(sfp.pathExists(example.example3.oR2));
    Assertions.assertTrue(sfp.pathExists(example.example4.oR2));
    Assertions.assertTrue(sfp.pathExists(example.example5.oR2));
    Assertions.assertTrue(sfp.pathExists(example.example6.oR2));
  }

  @Test
  void largeEFSMPE_Infeasible() {
    LargeInterComponentExample example = new LargeInterComponentExample();
    EEFSM<State, Param, Object> e = example.eefsm;

    IFeasiblePathAlgo<State, Param, EEFSMContext<Object>, ETransition<State, Param, Object>> sfp = getAlgo(e);
    e.forceConfiguration(new Configuration<>(example.example6.oR2,
        new EEFSMContext<>(example.example1.Hc, example.example2.Hc, example.example3.Hc,
            example.example4.Hc, example.example5.Hc, example.example6.Hc, example.example1.Le,
            example.example2.Le, example.example3.oSto1, example.example5.oSto1, example.additionalContext)));
    Assertions.assertFalse(sfp.pathExists(example.example1.oR1));
  }

  @Test
  void mediumEFSMPE() {
    MediumInterComponentExample example = new MediumInterComponentExample();
    EEFSM<State, Param, Object> eefsm = example.eefsm;

    IFeasiblePathAlgo<State, Param, EEFSMContext<Object>, ETransition<State, Param, Object>> sfp = getAlgo(eefsm);
    Assertions.assertTrue(sfp.pathExists(example.example1.oR1));
    Assertions.assertTrue(sfp.pathExists(example.example1.UIy));
    Assertions.assertTrue(sfp.pathExists(example.example1.Hf));
    Assertions.assertTrue(sfp.pathExists(example.example1.oR2));
    Assertions.assertTrue(sfp.pathExists(example.example2.oC1));
    Assertions.assertTrue(sfp.pathExists(example.example2.Hc));
    Assertions.assertTrue(sfp.pathExists(example.example2.oSta1));
    Assertions.assertTrue(sfp.pathExists(example.example2.oR1));
    Assertions.assertTrue(sfp.pathExists(example.example2.UI));
    Assertions.assertTrue(sfp.pathExists(example.example2.oR2));
  }
}
