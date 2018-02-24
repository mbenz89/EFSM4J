package de.upb.mb.efsm;

import com.google.common.collect.Sets;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Set;

import static de.upb.mb.efsm.Example.state0;
import static de.upb.mb.efsm.Example.state1;
import static de.upb.mb.efsm.Example.state2;
import static de.upb.mb.efsm.Example.trans1;
import static de.upb.mb.efsm.Example.trans2;
import static de.upb.mb.efsm.Example.trans3;
import static de.upb.mb.efsm.Example.uninitialized;

/**
 * @author Manuel Benz
 * created on 20.02.18
 */
class EFSMBuilderTest {

  @Test
  public void checkConsistency() {
    EFSM<State, Boolean, Set<Object>, Transition<State, Boolean, Set<Object>>> efsm = Example.efsm();

    Assertions.assertEquals(state0, efsm.getConfiguration().getState());
    Assertions.assertEquals(Collections.singleton(uninitialized), efsm.getConfiguration().getContext());

    Assertions.assertEquals(Sets.newHashSet(state0, state1, state2), efsm.getStates());
    Assertions.assertEquals(Sets.newHashSet(trans1, trans2, trans3), efsm.getTransitons());
  }

}