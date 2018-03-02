package de.upb.testify.efsm.eefsm;

import de.upb.testify.efsm.Param;
import de.upb.testify.efsm.State;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Manuel Benz
 * created on 02.03.18
 */
class EEFSMFPAlgoTest {

  private WhiteBoardExample example;

  @BeforeEach
  void setUp() {
    example = new WhiteBoardExample();
  }

  @Test
  void findsPathBetweenoC1AndHx() {
    EEFSMFPAlgo<State, Param, Object> sfp = new EEFSMFPAlgo<>(example.eefsm);

    EEFSMPath<State, Param, Object> path = sfp.getPath(example.initialContext, example.oC1, example.Hx);

    Assertions.assertNotNull(path);
    Assertions.assertEquals(example.oC1, path.getSrc());
    Assertions.assertEquals(example.Hx, path.getTgt());


  }
}