package de.upb.testify.efsm.eefsm;

import de.upb.testify.efsm.Configuration;
import de.upb.testify.efsm.EFSMDebugger;
import de.upb.testify.efsm.Param;
import de.upb.testify.efsm.State;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Manuel Benz
 * created on 26.02.18
 */
class EFSMDebuggerTest {

  private WhiteBoardExample example;

  @BeforeEach
  void setUp() {
    example = new WhiteBoardExample();
  }

  @Test
  void start() {
    EEFSM<State, Param, Object> eefsm = example.eefsm;
    EFSMDebugger<State, ETransition<State, Param, Object>> debugger = EFSMDebugger.startDebugger(eefsm, true, state -> state.toString(), t -> "");

    eefsm.transition(example.HcEntry);
    eefsm.transition(example.oSta1Entry);
    eefsm.transition(example.oR1Entry);
    eefsm.transition(example.UIClick);
    eefsm.transition(example.HEntry);
    eefsm.transition(example.oC2Entry);
    eefsm.transition(example.oSta2Entry);
    eefsm.transition(example.oR2Entry);
    eefsm.transition(example.oSto1Entry);
    eefsm.transition();
    eefsm.transition(example.UIyClick);
    eefsm.transition(example.HyEntry);
    eefsm.transition(example.EvtBack);
    eefsm.transition(example.oC1Entry);
    eefsm.transition(example.HcEntry);
    eefsm.transition(example.oSta1Entry);
    eefsm.transition(example.oR1Entry);
    eefsm.transition(example.UIxClick);
    eefsm.transition(example.HxEntry);

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