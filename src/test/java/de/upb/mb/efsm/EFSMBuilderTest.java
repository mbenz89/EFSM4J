package de.upb.mb.efsm;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static de.upb.mb.efsm.Objects.state0;
import static de.upb.mb.efsm.Objects.state1;
import static de.upb.mb.efsm.Objects.state2;
import static de.upb.mb.efsm.Objects.trans1;
import static de.upb.mb.efsm.Objects.trans2;
import static de.upb.mb.efsm.Objects.trans3;
import static de.upb.mb.efsm.Objects.uninitialized;

/**
 * @author Manuel Benz
 * created on 20.02.18
 */
class EFSMBuilderTest {

  @Test
  public void checkConsistency() {
    EFSM<State, Boolean, Variable> efsm = Objects.efsm();

    Assertions.assertEquals(state0, efsm.getConfiguration().getCurState());
    Assertions.assertEquals(Collections.singleton(uninitialized), efsm.getConfiguration().getContext());

    Assertions.assertEquals(Arrays.asList(state0, state1, state2), efsm.getStates());
    Assertions.assertEquals(Arrays.asList(trans1, trans2, trans3), efsm.getTransitons());
  }

}