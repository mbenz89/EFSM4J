package de.upb.testify.efsm.eefsm;

import de.upb.testify.efsm.EFSMDebugger;
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
    EFSMDebugger.startDebugger(example.eefsm, false, state -> state.toString(), t -> "");
    while (true) {
      try {
        Thread.sleep(2000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
}