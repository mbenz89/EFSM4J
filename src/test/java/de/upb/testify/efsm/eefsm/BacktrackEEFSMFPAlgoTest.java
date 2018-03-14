package de.upb.testify.efsm.eefsm;

import de.upb.testify.efsm.Configuration;
import de.upb.testify.efsm.ContextVar;
import de.upb.testify.efsm.EFSMBuilder;
import de.upb.testify.efsm.EFSMDotExporter;
import de.upb.testify.efsm.EFSMPath;
import de.upb.testify.efsm.IFeasiblePathAlgo;
import de.upb.testify.efsm.PEAllPath;
import de.upb.testify.efsm.PEBasedFPAlgo;
import de.upb.testify.efsm.Param;
import de.upb.testify.efsm.PathExpressionWithPruningFPAlgo;
import de.upb.testify.efsm.State;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Iterator;

/**
 * @author Manuel Benz
 * created on 02.03.18
 */
class BacktrackEEFSMFPAlgoTest {

  private boolean debugger = false;


  @Test
  void c1ToHxBacktrack() {
    BasicInterComponentExample example = new BasicInterComponentExample();
    BacktrackEEFSMFPAlgo<State, Param, Object> sfp = new BacktrackEEFSMFPAlgo<>(example.eefsm);

    EEFSMPath<State, Param, Object> path = sfp.getPath(example.Hx);

    Assertions.assertNotNull(path);
    Assertions.assertEquals(15, path.getLength());
    Assertions.assertEquals(example.oC1, path.getSrc());
    Assertions.assertEquals(example.Hx, path.getTgt());

    if (debugger) {
      EFSMDebuggerTest.debugThis(example, path);
    }
  }

  @Test
  void c1ToHxPEAll() {
    BasicInterComponentExample example = new BasicInterComponentExample();
    PEAllPath<State, Param, EEFSMContext<Object>, ETransition<State, Param, Object>> sfp = new PEAllPath<>(example.eefsm,20);

    EFSMPath<State, Param, EEFSMContext<Object>, ETransition<State, Param, Object>> path = sfp.getPath(example.Hx);

    Assertions.assertNotNull(path);
    //  Assertions.assertEquals(18, path.getLength());
    Assertions.assertEquals(example.oC1, path.getSrc());
    Assertions.assertEquals(example.Hx, path.getTgt());


    System.out.println(path);
    if (debugger) {
      EFSMDebuggerTest.debugThis(example, path);
    }
  }
  @Test
  void c1ToHxPEContext() {
    BasicInterComponentExample example = new BasicInterComponentExample();
    PEBasedFPAlgo<State, Param, EEFSMContext<Object>, ETransition<State, Param, Object>> sfp = new PathExpressionWithPruningFPAlgo<>(example.eefsm,20);

    EFSMPath<State, Param, EEFSMContext<Object>, ETransition<State, Param, Object>> path = sfp.getPath(example.Hx);

    Assertions.assertNotNull(path);
    //  Assertions.assertEquals(18, path.getLength());
    Assertions.assertEquals(example.oC1, path.getSrc());
    Assertions.assertEquals(example.Hx, path.getTgt());


    System.out.println(path);
    if (debugger) {
      EFSMDebuggerTest.debugThis(example, path);
    }
  }

  @Test
  void c1ToHxAllPAth() {
    BasicInterComponentExample example = new BasicInterComponentExample();
    AllPath<State, Param, Object> sfp = new AllPath<>(example.eefsm);

    EFSMPath<State, Param, EEFSMContext<Object>, ETransition<State, Param, Object>> path = sfp.getPath(example.Hx);

    Assertions.assertNotNull(path);
    //  Assertions.assertEquals(18, path.getLength());
    Assertions.assertEquals(example.oC1, path.getSrc());
    Assertions.assertEquals(example.Hx, path.getTgt());


    System.out.println(path);
    if (debugger) {
      EFSMDebuggerTest.debugThis(example, path);
    }
  }

  @Test
  void c1ToHxWithOnStopAllPath() {
    BasicInterComponentExample example = new BasicInterComponentExample();
    EEFSM<State, Param, Object> eefsm = example.eefsm;
    AllPath<State, Param, Object> sfp = new AllPath<>(eefsm);

    EEFSMPath<State, Param, Object> path = sfp.getPath(example.Hx);

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

    EEFSMPath<State, Param, Object> path2 = sfp.getPath(example.Hx);

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
  void c1ToHxWithOnStopBackTrack() {
    BasicInterComponentExample example = new BasicInterComponentExample();
    EEFSM<State, Param, Object> eefsm = example.eefsm;
    BacktrackEEFSMFPAlgo<State, Param, Object> sfp = new BacktrackEEFSMFPAlgo<>(eefsm);

    EEFSMPath<State, Param, Object> path = sfp.getPath(example.Hx);

    Assertions.assertNotNull(path);
    Assertions.assertEquals(15, path.getLength());
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

    EEFSMPath<State, Param, Object> path2 = sfp.getPath(example.Hx);

    Assertions.assertNotNull(path2);
    Assertions.assertEquals(10, path2.getLength());
    Assertions.assertEquals(example.oSto1, path2.getSrc());
    Assertions.assertEquals(example.Hx, path2.getTgt());
    Assertions.assertTrue(path2.isFeasible(eefsm.getConfiguration().getContext()));


    if (debugger) {
      EFSMDebuggerTest.debugThis(example, path);
    }
  }

  @Test
  void c1ToHxWithOnStopPe() {
    BasicInterComponentExample example = new BasicInterComponentExample();
    EEFSM<State, Param, Object> eefsm = example.eefsm;
    PathExpressionWithPruningFPAlgo<State, Param, EEFSMContext<Object>, ETransition<State, Param, Object>> sfp = new PathExpressionWithPruningFPAlgo<>(eefsm);

    EFSMPath<State, Param, EEFSMContext<Object>, ETransition<State, Param, Object>> path = sfp.getPath(example.Hx);

    Assertions.assertNotNull(path);
    // Assertions.assertEquals(15, path.getLength());
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
    // Assertions.assertEquals(10, path2.getLength());
    Assertions.assertEquals(example.oSto1, path2.getSrc());
    Assertions.assertEquals(example.Hx, path2.getTgt());
    Assertions.assertTrue(path2.isFeasible(eefsm.getConfiguration().getContext()));


    if (debugger) {
      EFSMDebuggerTest.debugThis(example, path);
    }
  }

  @Test
  void infeasibleAllPath() {
    InterComponentExampleInfeasible example = new InterComponentExampleInfeasible();
    EEFSM<State, Param, Object> eefsm = example.eefsm;
    AllPath<State, Param, Object> sfp = new AllPath<>(eefsm);
    EEFSMPath<State, Param, Object> path = sfp.getPath(example.Hx);
    Assertions.assertNull(path);
  }

  @Test
  void infeasibleBacktrack() {
    InterComponentExampleInfeasible example = new InterComponentExampleInfeasible();
    EEFSM<State, Param, Object> eefsm = example.eefsm;
    BacktrackEEFSMFPAlgo<State, Param, Object> sfp = new BacktrackEEFSMFPAlgo<>(eefsm);
    EEFSMPath<State, Param, Object> path = sfp.getPath(example.Hx);
    Assertions.assertNull(path);
  }

  @Test
  void infeasiblePE() {
    InterComponentExampleInfeasible example = new InterComponentExampleInfeasible();
    EEFSM<State, Param, Object> eefsm = example.eefsm;
    PEAllPath<State, Param, EEFSMContext<Object>, ETransition<State, Param, Object>> sfp = new PEAllPath<>(eefsm);

    EFSMPath<State, Param, EEFSMContext<Object>, ETransition<State, Param, Object>> path = sfp.getPath(example.Hx);
    Assertions.assertNull(path);
  }

  @Test
  @Disabled
  void largeEFSMPE() {
    BasicInterComponentExample example1 = new BasicInterComponentExample();
    BasicInterComponentExample example2 = new BasicInterComponentExample();
    BasicInterComponentExample example3 = new BasicInterComponentExample();
    BasicInterComponentExample example4 = new BasicInterComponentExample();
    BasicInterComponentExample example5 = new BasicInterComponentExample();
    BasicInterComponentExample example6 = new BasicInterComponentExample();

    ContextVar additionalContext = new ContextVar("add");
    State tgt = new State("tgt");


    EFSMBuilder<State, Param, EEFSMContext<Object>, ETransition<State, Param, Object>, EEFSM<State, Param, Object>> builder = EEFSM.builder();
    builder.withInitialState(example1.initialState).withInitialContext(example1.initialContext);
    builder.withEFSM(example1.eefsm);
    builder.withEFSM(example2.eefsm);
    builder.withTransition(example1.Hf, example2.oC1, new ETransition<>(new Param("e2Entry"), example1.Le, true, null, null));
    builder.withEFSM(example3.eefsm);
    builder.withTransition(example2.Hf, example3.oC1, new ETransition<>(new Param("e3Entry"), example2.Le, true, null, null));
    builder.withTransition(example3.oR1, example2.oR2, new ETransition<>(example3.EvtBack, null, true, null, null));
    builder.withEFSM(example4.eefsm);
    builder.withTransition(example3.Hf, example4.oC1, new ETransition<>(new Param("e4Entry"), example3.Hc, true, null, null));
    builder.withTransition(example4.oR1, example2.oR2, new ETransition<>(example4.EvtBack, null, true, null, null));
    builder.withState(tgt);
    builder.withTransition(example3.oD1, tgt, new ETransition<>(new Param("tgtEntry"), additionalContext, true, null, null));
    builder.withEFSM(example5.eefsm);
    builder.withTransition(example4.Hf, example5.oC1, new ETransition<>(new Param("e5Entry"), example1.Le, true, null, null));
    builder.withTransition(example5.oR1, example4.oR2, new ETransition<>(example5.EvtBack, null, true, null, null));
    builder.withEFSM(example6.eefsm);
    builder.withTransition(example5.Hf, example6.oC1, new ETransition<>(new Param("e6Entry"), example1.Le, true, new ContextVar[] {additionalContext}, null));
    builder.withTransition(example6.oR1, example5.oR2, new ETransition<>(example6.EvtBack, null, true, null, null));

    EEFSM<State, Param, Object> e = builder.build();

    if (debugger) {
      try {
        EFSMDotExporter d = new EFSMDotExporter(e);
        d.writeOut(Paths.get("target/largeEFSM.dot"));
      } catch (IOException e1) {
        e1.printStackTrace();
      }
    }

    IFeasiblePathAlgo<State, Param, EEFSMContext<Object>, ETransition<State, Param, Object>> sfp = new PathExpressionWithPruningFPAlgo<>(e,40);

    EFSMPath<State, Param, EEFSMContext<Object>, ETransition<State, Param, Object>> path = sfp.getPath(tgt);

    Assertions.assertNotNull(path);
    Assertions.assertTrue(path.isFeasible(example1.initialContext));

    Iterator<Param> inputsToTrigger = path.getInputsToTrigger();
    while (inputsToTrigger.hasNext()) {
      e.transition(inputsToTrigger.next());
    }

    Assertions.assertTrue(e.getConfiguration().getState().equals(tgt));

    e.reset();

    path = sfp.getPath(example6.oR2);

    inputsToTrigger = path.getInputsToTrigger();
    while (inputsToTrigger.hasNext()) {
      e.transition(inputsToTrigger.next());
    }

    Assertions.assertTrue(e.getConfiguration().getState().equals(example6.oR2));

    path = sfp.getPath(example1.oR1);

    Assertions.assertNull(path);
  }
}