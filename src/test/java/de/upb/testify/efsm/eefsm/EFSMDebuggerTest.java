package de.upb.testify.efsm.eefsm;

import de.upb.testify.efsm.Configuration;
import de.upb.testify.efsm.EFSMDebugger;
import de.upb.testify.efsm.Param;
import de.upb.testify.efsm.State;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Iterator;

/**
 * @author Manuel Benz
 * created on 26.02.18
 */
class EFSMDebuggerTest {

  private BasicInterComponentExample example;

  @BeforeEach
  void setUp() {
    example = new BasicInterComponentExample();
  }

  @Test
  @Disabled
  void staticPath() {
    EEFSMPath path = new EEFSMPath<>(example.eefsm);
    path.appendTransition(example.oC1ToHc);
    path.appendTransition(example.hCToOsta1);
    path.appendTransition(example.oSta1ToOr1);
    path.appendTransition(example.oR1ToUi);
    path.appendTransition(example.uiToH);
    path.appendTransition(example.hToOc2);
    path.appendTransition(example.oC2ToOSta2);
    path.appendTransition(example.oSta2ToOr2);
    path.appendTransition(example.oR2ToOSto1);
    path.appendTransition(example.oSto1ToOR2);
    path.appendTransition(example.oR2ToUIy);
    path.appendTransition(example.uiYToHy);
    path.appendTransition(example.hyToOr2);
    path.appendTransition(example.b2ToOC1);
    path.appendTransition(example.oC1ToHc);
    path.appendTransition(example.hCToOsta1);
    path.appendTransition(example.oSta1ToOr1);
    path.appendTransition(example.oR1ToUix);
    path.appendTransition(example.uiXToHx);

    debugThis(example, path);
  }


  public static void debugThis(BasicInterComponentExample example, EEFSMPath<State, Param, Object> path) {
    EEFSM<State, Param, Object> eefsm = example.eefsm;
    Assertions.assertTrue(path.isFeasible(example.initialContext));

    EFSMDebugger<State, ETransition<State, Param, Object>> debugger = EFSMDebugger.startDebugger(eefsm, true, state -> state.toString(), t -> "");

    Iterator<Param> iterator = path.getInputsToTrigger();
    while (iterator.hasNext()) {
      Assertions.assertNotNull(eefsm.transition(iterator.next()), "Transition failed in configuration: " + eefsm.getConfiguration());
    }

    Assertions.assertEquals(new Configuration<>(example.Hx, new EEFSMContext<>(example.Le, example.Hc)), eefsm.getConfiguration());


    while (true) {
      try {
        Thread.sleep(2000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
}