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
    EFSMPath<State, Param, EEFSMContext<Object>, ETransition<State, Param, Object>> path = sfp.getPath(example.tgt);

    Assertions.assertNotNull(path);
    Assertions.assertTrue(path.isFeasible(example.initialContext));

    Iterator<Param> inputsToTrigger = path.getInputsToTrigger();
    while (inputsToTrigger.hasNext()) {
      e.transition(inputsToTrigger.next());
    }

    Assertions.assertTrue(e.getConfiguration().getState().equals(example.tgt));
  }

  @Test
  void largeEFSMPE2() {
    LargeInterComponentExample example = new LargeInterComponentExample();
    EEFSM<State, Param, Object> e = example.eefsm;


    IFeasiblePathAlgo<State, Param, EEFSMContext<Object>, ETransition<State, Param, Object>> sfp = getAlgo(e);

    EFSMPath<State, Param, EEFSMContext<Object>, ETransition<State, Param, Object>> path = sfp.getPath(example.example6.oR2);

    Iterator<Param> inputsToTrigger = path.getInputsToTrigger();
    while (inputsToTrigger.hasNext()) {
      e.transition(inputsToTrigger.next());
    }

    Assertions.assertTrue(e.getConfiguration().getState().equals(example.example6.oR2));
  }

  @Test
  void largeEFSMPE_InfeasibleAfterPath() {
    LargeInterComponentExample example = new LargeInterComponentExample();
    EEFSM<State, Param, Object> e = example.eefsm;


    IFeasiblePathAlgo<State, Param, EEFSMContext<Object>, ETransition<State, Param, Object>> sfp = getAlgo(e);

    EFSMPath<State, Param, EEFSMContext<Object>, ETransition<State, Param, Object>> path = sfp.getPath(example.example6.oR2);

    Iterator<Param> inputsToTrigger = path.getInputsToTrigger();
    while (inputsToTrigger.hasNext()) {
      e.transition(inputsToTrigger.next());
    }

    Assertions.assertTrue(e.getConfiguration().getState().equals(example.example6.oR2));

    path = sfp.getPath(example.example1.oR1);

    Assertions.assertNull(path);
  }
}