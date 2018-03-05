package de.upb.testify.efsm.eefsm;

import de.upb.testify.efsm.Configuration;
import de.upb.testify.efsm.Param;
import de.upb.testify.efsm.State;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Iterator;

/**
 * @author Manuel Benz
 * created on 02.03.18
 */
class EEFSMFPAlgoTest {

  private BasicInterComponentExample example;
  private boolean debugger = false;

  @BeforeEach
  void setUp() {
    example = new BasicInterComponentExample();
  }

  @Test
  void c1ToHx() {
    EEFSMFPAlgo<State, Param, Object> sfp = new EEFSMFPAlgo<>(example.eefsm);

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
  void c1ToHxWithOnStop1() {
    EEFSM<State, Param, Object> eefsm = example.eefsm;
    EEFSMFPAlgo<State, Param, Object> sfp = new EEFSMFPAlgo<>(eefsm);

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
}