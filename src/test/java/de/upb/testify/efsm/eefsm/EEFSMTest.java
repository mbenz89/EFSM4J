package de.upb.testify.efsm.eefsm;

import de.upb.testify.efsm.Configuration;
import de.upb.testify.efsm.EFSMDotExporter;
import de.upb.testify.efsm.Param;
import de.upb.testify.efsm.State;
import de.upb.testify.efsm.Super;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Set;

/**
 * @author Manuel Benz
 * created on 22.02.18
 */
class EEFSMTest {

  private WhiteBoardExample example;

  @BeforeEach
  void setUp() {
    example = new WhiteBoardExample();
  }

  @Test
  void toDot() throws IOException {
    EFSMDotExporter exporter = new EFSMDotExporter(example.eefsm);
    exporter.writeOut(Paths.get("./target/eefsm.dot"));
  }

  @Test
  void canTramsition() {
    EEFSM<State, Param, Object> eefsm = example.eefsm;
    Assertions.assertTrue(eefsm.canTransition(example.HcEntry));
  }


  @Test
  void transition() {
    EEFSM<State, Param, Object> eefsm = example.eefsm;
    eefsm.transition(example.HcEntry);
    Assertions.assertEquals(new Configuration(example.Hc, new EEFSMContext<>()), eefsm.getConfiguration());
  }

  @Test
  void transitionAndDrop() {
    EEFSM<State, Param, Object> eefsm = example.eefsm;
    Assertions.assertEquals(new Configuration(example.Hc, new EEFSMContext<>()), eefsm.transitionAndDrop(example.HcEntry));
  }

  @Test
  void cannotTransition() {
    Assertions.assertFalse(example.eefsm.canTransition());
  }

  @Test
  void failTransition() {
    EEFSM<State, Param, Object> eefsm = example.eefsm;
    Configuration<State, EEFSMContext<Object>> before = eefsm.getConfiguration();
    Set<Param> transition = eefsm.transition();
    Assertions.assertNull(transition);
    Assertions.assertEquals(before, eefsm.getConfiguration());
  }

  @Test
  void addToContext() {
    EEFSM<State, Param, Object> eefsm = example.eefsm;
    eefsm.transition(example.HcEntry);
    eefsm.transition(example.oSta1Entry);

    Assertions.assertEquals(new Configuration<>(example.oSta1, new EEFSMContext<>(example.Hc)), eefsm.getConfiguration());
  }

  @Test
  void elementOfContext() {
    EEFSM<State, Param, Object> eefsm = example.eefsm;
    eefsm.transition(example.HcEntry);
    eefsm.transition(example.oSta1Entry);
    eefsm.transition(example.oR1Entry);

    Assertions.assertEquals(new Configuration<>(example.oR1, new EEFSMContext<>(example.Hc)), eefsm.getConfiguration());
    Assertions.assertTrue(eefsm.canTransition(example.UIClick));
    Assertions.assertFalse(eefsm.canTransition());

    Assertions.assertEquals(new Configuration<>(example.UI, new EEFSMContext<>(example.Hc)), eefsm.transitionAndDrop(example.UIClick));
  }

  @Test
  void notElementOfContext() {
    EEFSM<State, Param, Object> eefsm = example.eefsm;
    eefsm.transition(example.HcEntry);
    eefsm.transition(example.oSta1Entry);
    eefsm.transition(example.oR1Entry);
    eefsm.transition(example.UIClick);
    eefsm.transition(example.HEntry);
    eefsm.transition(example.oC2Entry);
    eefsm.transition(example.oSta2Entry);
    eefsm.transition(example.oR2Entry);
    eefsm.transition(example.UIyClick);
    eefsm.transition(example.HyEntry);
    eefsm.transition(example.EvtBack);

    EEFSMContext<Super> context = new EEFSMContext<>(example.Hc, example.Le);
    Assertions.assertEquals(new Configuration<>(example.B2, context), eefsm.getConfiguration());

    Assertions.assertEquals(!context.notElementOf(example.oSto1), context.elementOf(example.oSto1));
    Assertions.assertTrue(context.notElementOf(example.oSto1));
    Assertions.assertTrue(eefsm.canTransition(example.oR1Entry));

    Assertions.assertEquals(new Configuration<>(example.oR1, context), eefsm.transitionAndDrop(example.oR1Entry));
  }

  @Test
  void removeFromContext() {
    EEFSM<State, Param, Object> eefsm = example.eefsm;
    eefsm.transition(example.HcEntry);
    eefsm.transition(example.oSta1Entry);
    eefsm.transition(example.oR1Entry);
    eefsm.transition(example.UIClick);
    eefsm.transition(example.HEntry);
    eefsm.transition(example.oC2Entry);
    eefsm.transition(example.oSta2Entry);
    eefsm.transition(example.oR2Entry);


    EEFSMContext<State> context = new EEFSMContext<>(example.Hc);
    Assertions.assertEquals(new Configuration<>(example.oR2, context), eefsm.getConfiguration());
    context.union(example.oR2);
    Assertions.assertEquals(new Configuration<>(example.oSto1, context), eefsm.transitionAndDrop(example.oSto1Entry));
    Assertions.assertTrue(eefsm.canTransition(example.oD1Entry));
    Assertions.assertTrue(eefsm.canTransition());

    context.remove(example.oR2);
    context.union(example.oSto1);

    Assertions.assertEquals(new Configuration<>(example.oR2, context), eefsm.transitionAndDrop());
  }
}