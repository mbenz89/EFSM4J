package de.upb.testify.efsm.eefsm;

import de.upb.testify.efsm.EFSMDebugger;
import de.upb.testify.efsm.EFSMPath;
import de.upb.testify.efsm.Param;
import de.upb.testify.efsm.State;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Iterator;

/** @author Manuel Benz created on 26.02.18 */
public class EFSMDebuggerTest {

  public static void debugThis(EEFSMPath<State, Param, Object> path) {
    final EEFSM<State, Param, Object> efsm = path.getEefsm();
    EFSMDebugger<State, ETransition<State, Param, Object>> debugger =
        EFSMDebugger.startDebugger(efsm, true, state -> state.toString(), t -> t.toString());
    debugger.highlightPath(path);
    debugger.highlightStates(path.getTgt());

    Iterator<Param> iterator = path.getInputsToTrigger();
    while (iterator.hasNext()) {
      Assertions.assertNotNull(
          efsm.transition(iterator.next()),
          "Transition failed in configuration: " + efsm.getConfiguration());
    }
  }

  @Test
  @Disabled
  void staticPath() {
    BasicInterComponentExample example = new BasicInterComponentExample();
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

    debugThis(path);
  }

  @Test
  @Disabled
  void largeEFSMComputed() {
    LargeInterComponentExample example = new LargeInterComponentExample();
    EFSMPath<State, Param, EEFSMContext<Object>, ETransition<State, Param, Object>> path =
        new GraphExplosionFeasiblePathAlgorithm<>(example.eefsm).getPath(example.tgt);

    debugThis((EEFSMPath<State, Param, Object>) path);
  }

  @Test
  @Disabled
  void resetTest() {
    LargeInterComponentExample example = new LargeInterComponentExample();
    EFSMPath<State, Param, EEFSMContext<Object>, ETransition<State, Param, Object>> path =
        new GraphExplosionFeasiblePathAlgorithm<>(example.eefsm).getPath(example.tgt);

    debugThis((EEFSMPath<State, Param, Object>) path);

    ((EEFSMPath<State, Param, Object>) path).getEefsm().reset();

    debugThis((EEFSMPath<State, Param, Object>) path);
  }
}
