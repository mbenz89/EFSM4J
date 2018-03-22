package de.upb.testify.efsm.eefsm;

import de.upb.testify.efsm.Configuration;
import de.upb.testify.efsm.EFSMDotExporter;
import de.upb.testify.efsm.EFSMPath;
import de.upb.testify.efsm.IFeasiblePathAlgo;
import de.upb.testify.efsm.Param;
import de.upb.testify.efsm.State;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Iterator;

/**
 * @author Manuel Benz
 * created on 02.03.18
 */
abstract class AbstractEFSMFPAlgoTest {

  protected boolean debugger = false;

  protected abstract IFeasiblePathAlgo<State, Param, EEFSMContext<Object>, ETransition<State, Param, Object>> getAlgo(EEFSM<State, Param, Object> eefsm);


  @Test
  void c1ToHx() {
    BasicInterComponentExample example = new BasicInterComponentExample();
    IFeasiblePathAlgo<State, Param, EEFSMContext<Object>, ETransition<State, Param, Object>> sfp = getAlgo(example.eefsm);

    EFSMPath<State, Param, EEFSMContext<Object>, ETransition<State, Param, Object>> path = sfp.getPath(example.Hx);

    Assertions.assertNotNull(path);
//    Assertions.assertEquals(15, path.getLength());
    Assertions.assertEquals(example.oC1, path.getSrc());
    Assertions.assertEquals(example.Hx, path.getTgt());

    if (debugger) {
      EFSMDebuggerTest.debugThis(example, path);
    }
  }


  @Test
  void c1ToHxWithOnStop() {
    BasicInterComponentExample example = new BasicInterComponentExample();
    EEFSM<State, Param, Object> eefsm = example.eefsm;
    IFeasiblePathAlgo<State, Param, EEFSMContext<Object>, ETransition<State, Param, Object>> sfp = getAlgo(eefsm);

    EFSMPath<State, Param, EEFSMContext<Object>, ETransition<State, Param, Object>> path = sfp.getPath(example.Hx);

    Assertions.assertNotNull(path);
    // Assertions.assertEquals(18, path.getLength());
    Assertions.assertEquals(example.oC1, path.getSrc());
    Assertions.assertEquals(example.Hx, path.getTgt());
    Assertions.assertTrue(path.isFeasible(eefsm.getConfiguration().getContext()));

    // lets transition into oSto 1 and then calculate a new path
    Iterator<Param> iter = path.getInputsToTrigger();
    while (iter.hasNext()) {
      Configuration configuration = eefsm.transitionAndDrop(iter.next());
      if (configuration.getState().equals(example.oR2)) {
        break;
      }
    }

    eefsm.transition(example.oSto1Entry);

    EFSMPath<State, Param, EEFSMContext<Object>, ETransition<State, Param, Object>> path2 = sfp.getPath(example.Hx);

    Assertions.assertNotNull(path2);
    //   Assertions.assertEquals(20, path2.getLength());
    Assertions.assertEquals(example.oSto1, path2.getSrc());
    Assertions.assertEquals(example.Hx, path2.getTgt());
    Assertions.assertTrue(path2.isFeasible(eefsm.getConfiguration().getContext()));


    if (debugger) {
      EFSMDebuggerTest.debugThis(example, path);
    }
  }


  @Test
  void infeasiblePath() {
    InterComponentExampleInfeasible example = new InterComponentExampleInfeasible();
    EEFSM<State, Param, Object> eefsm = example.eefsm;
    IFeasiblePathAlgo<State, Param, EEFSMContext<Object>, ETransition<State, Param, Object>> sfp = getAlgo(eefsm);
    EFSMPath<State, Param, EEFSMContext<Object>, ETransition<State, Param, Object>> path = sfp.getPath(example.Hx);
    Assertions.assertNull(path);
  }


  @Test
  void largeEFSMPE() {
    LargeInterComponentExample example = new LargeInterComponentExample();
    EEFSM<State, Param, Object> e = example.eefsm;

    if (debugger) {
      try {
        EFSMDotExporter d = new EFSMDotExporter(e);
        d.writeOut(Paths.get("target/largeEFSM.dot"));
      } catch (IOException e1) {
        e1.printStackTrace();
      }
    }

    IFeasiblePathAlgo<State, Param, EEFSMContext<Object>, ETransition<State, Param, Object>> sfp = getAlgo(e);

    EFSMPath<State, Param, EEFSMContext<Object>, ETransition<State, Param, Object>> path = sfp.getPath(example.example6.oR1);

    Assertions.assertNotNull(path);
    Assertions.assertEquals(example.example1.initialState, path.getSrc());
    Assertions.assertEquals(example.example6.oR1, example.example6.oR1);
    Assertions.assertTrue(path.isFeasible(example.initialContext));

    e.transition(path);

    Configuration<State, EEFSMContext<Object>> config = e.getConfiguration();
    Assertions.assertEquals(example.example6.oR1, config.getState());
    Assertions.assertTrue(config.getContext().elementOf(example.additionalContext));

    path = sfp.getPath(example.tgt);

    Assertions.assertNotNull(path);
    Assertions.assertEquals(example.example6.oR1, path.getSrc());
    Assertions.assertEquals(example.tgt, path.getTgt());
    Assertions.assertTrue(path.isFeasible(config.getContext()));

    e.transition(path);

    Assertions.assertEquals(e.getConfiguration().getState(), example.tgt);

    e.reset();

    path = sfp.getPath(example.tgt);

    Assertions.assertNotNull(path);
    Assertions.assertEquals(example.example1.initialState, path.getSrc());
    Assertions.assertEquals(example.tgt, path.getTgt());
    Assertions.assertTrue(path.isFeasible(example.initialContext));

    e.transition(path);

    Assertions.assertEquals(e.getConfiguration().getState(), example.tgt);
  }

  @Test
  void largeEFSMPE2() {
    LargeInterComponentExample example = new LargeInterComponentExample();
    EEFSM<State, Param, Object> e = example.eefsm;


    IFeasiblePathAlgo<State, Param, EEFSMContext<Object>, ETransition<State, Param, Object>> sfp = getAlgo(e);

    EFSMPath<State, Param, EEFSMContext<Object>, ETransition<State, Param, Object>> path = sfp.getPath(example.example6.oR2);

    Assertions.assertNotNull(path);
    Assertions.assertEquals(example.example1.initialState, path.getSrc());
    Assertions.assertEquals(example.example6.oR2, path.getTgt());
    Assertions.assertTrue(path.isFeasible(example.initialContext), () -> "path: " + path);

    Iterator<Param> inputsToTrigger = path.getInputsToTrigger();
    while (inputsToTrigger.hasNext()) {
      e.transition(inputsToTrigger.next());
    }

    Assertions.assertEquals(example.example6.oR2, e.getConfiguration().getState());
  }

  @Test
  void largeEFSMPE_InfeasibleAfterPath() {
    LargeInterComponentExample example = new LargeInterComponentExample();
    EEFSM<State, Param, Object> e = example.eefsm;


    IFeasiblePathAlgo<State, Param, EEFSMContext<Object>, ETransition<State, Param, Object>> sfp = getAlgo(e);

    EFSMPath<State, Param, EEFSMContext<Object>, ETransition<State, Param, Object>> path = sfp.getPath(example.example6.oR2);


    Assertions.assertNotNull(path);
    Assertions.assertEquals(example.example1.initialState, path.getSrc());
    Assertions.assertEquals(example.example6.oR2, path.getTgt());
    Assertions.assertTrue(path.isFeasible(example.initialContext));

    Iterator<Param> inputsToTrigger = path.getInputsToTrigger();
    while (inputsToTrigger.hasNext()) {
      e.transition(inputsToTrigger.next());
    }


    path = sfp.getPath(example.example1.oR1);

    Assertions.assertNull(path);
  }

  @Test
  void mediumEFSMPE() {
    MediumInterComponentExample example = new MediumInterComponentExample();
    EEFSM<State, Param, Object> eefsm = example.eefsm;

    IFeasiblePathAlgo<State, Param, EEFSMContext<Object>, ETransition<State, Param, Object>> sfp = getAlgo(eefsm);
    Assertions.assertNotNull(sfp.getPath(example.example1.oR1));
    Assertions.assertNotNull(sfp.getPath(example.example1.UIy));
    Assertions.assertNotNull(sfp.getPath(example.example1.Hf));
    Assertions.assertNotNull(sfp.getPath(example.example1.oR2));
    Assertions.assertNotNull(sfp.getPath(example.example2.oC1));
    Assertions.assertNotNull(sfp.getPath(example.example2.Hc));
    Assertions.assertNotNull(sfp.getPath(example.example2.oSta1));
    Assertions.assertNotNull(sfp.getPath(example.example2.oR1));
    Assertions.assertNotNull(sfp.getPath(example.example2.UI));
    Assertions.assertNotNull(sfp.getPath(example.example2.oR2));
  }
}