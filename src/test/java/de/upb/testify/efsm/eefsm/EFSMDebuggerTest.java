package de.upb.testify.efsm.eefsm;

import de.upb.testify.efsm.Configuration;
import de.upb.testify.efsm.EFSMDebugger;
import de.upb.testify.efsm.EFSMPath;
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
public  class EFSMDebuggerTest {

  private BasicInterComponentExample example;

  public static void debugThis(BasicInterComponentExample example, EFSMPath<State, Param, EEFSMContext<Object>,ETransition<State,Param,Object>> path) {
    EEFSM<State, Param, Object> eefsm = example.eefsm;
    Assertions.assertTrue(path.isFeasible(example.initialContext));

    EFSMDebugger<State, ETransition<State, Param, Object>> debugger = EFSMDebugger.startDebugger(eefsm, true, state -> state.toString(), t -> t.toString());
    debugger.highlightPath(path);

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

  @BeforeEach
  void setUp() {
    example = new BasicInterComponentExample();
  }

  @Test
  @Disabled
  void staticPath() {
    EEFSMPath path = new EEFSMPath<>(example.eefsm);
    path.append(example.oC1ToHc);
    path.append(example.hCToOsta1);
    path.append(example.oSta1ToOr1);
    path.append(example.oR1ToUi);
    path.append(example.uiToH);
    path.append(example.hToOc2);
    path.append(example.oC2ToOSta2);
    path.append(example.oSta2ToOr2);
    path.append(example.oR2ToOSto1);
    path.append(example.oSto1ToOR2);
    path.append(example.oR2ToUIy);
    path.append(example.uiYToHy);
    path.append(example.hyToOr2);
    path.append(example.oR2ToB2);
    path.append(example.b2ToOC1);
    path.append(example.oC1ToHc);
    path.append(example.hCToOsta1);
    path.append(example.oSta1ToOr1);
    path.append(example.oR1ToUix);
    path.append(example.uiXToHx);

    debugThis(example, path);
  }
}