package de.upb.mb.efsm;

import com.google.common.collect.Sets;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Set;

/**
 * @author Manuel Benz
 * created on 24.02.18
 */
class EFSMTest {

  private Example example;

  @BeforeEach
  void setUp() {
    example = new Example();
  }

  @Test
  void exampleTest1() {
    EFSM<State, Boolean, Set<Object>, Transition<State, Boolean, Set<Object>>> efsm = example.efsm;
    Configuration<State, Set<Object>> configuration = efsm.getConfiguration();

    Assertions.assertEquals(example.state0, configuration.getState());
    Assertions.assertEquals(Sets.newHashSet(example.uninitialized), configuration.getContext());
    Assertions.assertTrue(efsm.canTransition());
    // should alway transition no matter of the input
    Assertions.assertTrue(efsm.canTransition(true));
    Assertions.assertEquals(Collections.emptySet(), efsm.transition());

    configuration = efsm.getConfiguration();

    Assertions.assertEquals(example.state1, configuration.getState());
    Assertions.assertEquals(Sets.newHashSet(example.initialized), configuration.getContext());
    Assertions.assertTrue(efsm.canTransition(true));
    Assertions.assertFalse(efsm.canTransition(false));

    Assertions.assertEquals(Collections.emptySet(), efsm.transition(true));

    configuration = efsm.getConfiguration();

    Assertions.assertEquals(example.state2, configuration.getState());
    Assertions.assertEquals(Sets.newHashSet(example.running), configuration.getContext());
    Assertions.assertTrue(efsm.canTransition(true));
    Assertions.assertTrue(efsm.canTransition(false));
    Assertions.assertTrue(efsm.canTransition());

    configuration = efsm.transitionAndDrop();

    Assertions.assertEquals(example.state1, configuration.getState());
    Assertions.assertEquals(Sets.newHashSet(example.initialized), configuration.getContext());
  }

}