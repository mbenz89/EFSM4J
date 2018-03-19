package de.upb.testify.efsm.eefsm;

import de.upb.testify.efsm.Configuration;
import de.upb.testify.efsm.EFSMPath;
import de.upb.testify.efsm.PEBasedFPAlgo;
import de.upb.testify.efsm.Param;
import de.upb.testify.efsm.State;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pathexpression.IRegEx;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.stream.Stream;

/**
 * @author Manuel Benz
 * created on 19.03.18
 */
class PEBasedFPAlgoTest {


  private PEBasedFPAlgo<State, Param, EEFSMContext<Object>, ETransition<State, Param, Object>> getAlgo(EEFSM<State, Param, Object> eefsm) {
    return new PEBasedFPAlgo<State, Param, EEFSMContext<Object>, ETransition<State, Param, Object>>(eefsm) {
      @Override
      protected Stream<EFSMPath<State, Param, EEFSMContext<Object>, ETransition<State, Param, Object>>> expressionToPath(Configuration<State, EEFSMContext<Object>> config, IRegEx<ETransition<State, Param, Object>> pathExpression) {
        throw new NotImplementedException();
      }
    };
  }

  @Test
  void c1ToHx() {
    BasicInterComponentExample example = new BasicInterComponentExample();
    PEBasedFPAlgo<State, Param, EEFSMContext<Object>, ETransition<State, Param, Object>> sfp = getAlgo(example.eefsm);
    Assertions.assertTrue(sfp.pathExists(example.Hx));
  }


  @Test
  void c1ToHxWithOnStop() {
    BasicInterComponentExample example = new BasicInterComponentExample();
    EEFSM<State, Param, Object> eefsm = example.eefsm;
    PEBasedFPAlgo<State, Param, EEFSMContext<Object>, ETransition<State, Param, Object>> sfp = getAlgo(eefsm);
    EEFSMContext<Object> context = new EEFSMContext<>();
    context.union(example.oR2, example.Hc);

    eefsm.forceConfiguration(new Configuration(example.oSto1Entry, context));

    Assertions.assertTrue(sfp.pathExists(example.Hx));
  }


  @Test
  void infeasiblePath() {
    InterComponentExampleInfeasible example = new InterComponentExampleInfeasible();
    EEFSM<State, Param, Object> eefsm = example.eefsm;
    PEBasedFPAlgo<State, Param, EEFSMContext<Object>, ETransition<State, Param, Object>> sfp = getAlgo(eefsm);
    Assertions.assertFalse(sfp.pathExists(example.Hx));
  }


  @Test
  void largeEFSMPE() {
    LargeInterComponentExample example = new LargeInterComponentExample();
    EEFSM<State, Param, Object> e = example.eefsm;


    PEBasedFPAlgo<State, Param, EEFSMContext<Object>, ETransition<State, Param, Object>> sfp = getAlgo(e);
    Assertions.assertTrue(sfp.pathExists(example.tgt));
  }


  @Test
  void largeEFSMPE2() {
    LargeInterComponentExample example = new LargeInterComponentExample();
    EEFSM<State, Param, Object> e = example.eefsm;

    PEBasedFPAlgo<State, Param, EEFSMContext<Object>, ETransition<State, Param, Object>> sfp = getAlgo(e);
    Assertions.assertTrue(sfp.pathExists(example.example6.oR2));
  }

  @Test
  void largeEFSMPE3() {
    LargeInterComponentExample example = new LargeInterComponentExample();
    EEFSM<State, Param, Object> e = example.eefsm;

    PEBasedFPAlgo<State, Param, EEFSMContext<Object>, ETransition<State, Param, Object>> sfp = getAlgo(e);
    e.forceConfiguration(new Configuration<>(example.example6.oR2, new EEFSMContext<>()));
    Assertions.assertFalse(sfp.pathExists(example.example1.oR1));
  }
}