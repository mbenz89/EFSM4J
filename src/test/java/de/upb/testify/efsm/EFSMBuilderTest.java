package de.upb.testify.efsm;

import com.google.common.collect.Sets;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

/** @author Manuel Benz created on 20.02.18 */
class EFSMBuilderTest {

  private Example example;

  @BeforeEach
  void setUp() {
    example = new Example();
  }

  @Test
  public void checkConsistency() {
    EFSM<State, Boolean, Context, Transition<State, Boolean, Context>> efsm = example.efsm;

    Assertions.assertEquals(example.state0, efsm.getConfiguration().getState());
    Assertions.assertEquals(
        Collections.singleton(example.uninitialized), efsm.getConfiguration().getContext());

    Assertions.assertEquals(
        Sets.newHashSet(example.state0, example.state1, example.state2), efsm.getStates());
    Assertions.assertEquals(
        Sets.newHashSet(example.trans1, example.trans2, example.trans3), efsm.getTransitons());
  }
}
