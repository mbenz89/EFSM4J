package de.upb.testify.efsm.eefsm;

import de.upb.testify.efsm.Configuration;
import de.upb.testify.efsm.EFSMPath;
import de.upb.testify.efsm.PathExpressionBasedFeasiblePathAlgorithm;
import de.upb.testify.efsm.Param;
import de.upb.testify.efsm.State;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pathexpression.IRegEx;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.util.stream.Stream;

/**
 * @author Manuel Benz
 * created on 19.03.18
 */
class PathExpressionBasedFeasiblePathAlgorithmTest {


  private PathExpressionBasedFeasiblePathAlgorithm<State, Param, EEFSMContext<Object>, ETransition<State, Param, Object>> getAlgo(EEFSM<State, Param, Object> eefsm) {
    return new PathExpressionBasedFeasiblePathAlgorithm<State, Param, EEFSMContext<Object>, ETransition<State, Param, Object>>(eefsm) {
      @Override
      protected Stream<EFSMPath<State, Param, EEFSMContext<Object>, ETransition<State, Param, Object>>> expressionToPath(Configuration<State, EEFSMContext<Object>> config, IRegEx<ETransition<State, Param, Object>> pathExpression) {
        throw new NotImplementedException();
      }
    };
  }

  @Test
  void c1ToHx() {
    BasicInterComponentExample example = new BasicInterComponentExample();
    PathExpressionBasedFeasiblePathAlgorithm<State, Param, EEFSMContext<Object>, ETransition<State, Param, Object>> sfp = getAlgo(example.eefsm);
    Assertions.assertTrue(sfp.pathExists(example.Hx));
  }


  @Test
  void c1ToHxWithOnStop() {
    BasicInterComponentExample example = new BasicInterComponentExample();
    EEFSM<State, Param, Object> eefsm = example.eefsm;
    PathExpressionBasedFeasiblePathAlgorithm<State, Param, EEFSMContext<Object>, ETransition<State, Param, Object>> sfp = getAlgo(eefsm);
    EEFSMContext<Object> context = new EEFSMContext<>();
    context.union(example.oR2, example.Hc);

    eefsm.forceConfiguration(new Configuration(example.oSto1, context));

    Assertions.assertTrue(sfp.pathExists(example.Hx));
  }


  @Test
  void infeasiblePath() {
    InterComponentExampleInfeasible example = new InterComponentExampleInfeasible();
    EEFSM<State, Param, Object> eefsm = example.eefsm;
    PathExpressionBasedFeasiblePathAlgorithm<State, Param, EEFSMContext<Object>, ETransition<State, Param, Object>> sfp = getAlgo(eefsm);
    Assertions.assertFalse(sfp.pathExists(example.Hx));
  }


  @Test
  void largeEFSMPE() throws IOException {
    LargeInterComponentExample example = new LargeInterComponentExample();
    EEFSM<State, Param, Object> e = example.eefsm;

    //new EFSMDotExporter(e).writeOut(Paths.get("./target/large.dot"));

    PathExpressionBasedFeasiblePathAlgorithm<State, Param, EEFSMContext<Object>, ETransition<State, Param, Object>> sfp = getAlgo(e);
    Assertions.assertTrue(sfp.pathExists(example.example6.oR1));
    Assertions.assertTrue(sfp.pathExists(example.example3.oD1));
    Assertions.assertTrue(sfp.pathExists(example.tgt));
  }


  @Test
  void largeEFSMPE2() {
    LargeInterComponentExample example = new LargeInterComponentExample();
    EEFSM<State, Param, Object> e = example.eefsm;

    PathExpressionBasedFeasiblePathAlgorithm<State, Param, EEFSMContext<Object>, ETransition<State, Param, Object>> sfp = getAlgo(e);
    Assertions.assertTrue(sfp.pathExists(example.example1.oR2));
    Assertions.assertTrue(sfp.pathExists(example.example2.oR2));
    Assertions.assertTrue(sfp.pathExists(example.example3.oR2));
    Assertions.assertTrue(sfp.pathExists(example.example4.oR2));
    Assertions.assertTrue(sfp.pathExists(example.example5.oR2));
    Assertions.assertTrue(sfp.pathExists(example.example6.oR2));
  }

  @Test
  void largeEFSMPE_Infeasible() {
    LargeInterComponentExample example = new LargeInterComponentExample();
    EEFSM<State, Param, Object> e = example.eefsm;

    PathExpressionBasedFeasiblePathAlgorithm<State, Param, EEFSMContext<Object>, ETransition<State, Param, Object>> sfp = getAlgo(e);
    e.forceConfiguration(new Configuration<>(example.example6.oR2, new EEFSMContext<>()));
    Assertions.assertFalse(sfp.pathExists(example.example1.oR1));
  }

  @Test
  void mediumEFSMPE() {
    MediumInterComponentExample example = new MediumInterComponentExample();
    EEFSM<State, Param, Object> eefsm = example.eefsm;

    PathExpressionBasedFeasiblePathAlgorithm<State, Param, EEFSMContext<Object>, ETransition<State, Param, Object>> sfp = getAlgo(eefsm);
    Assertions.assertTrue(sfp.pathExists(example.example1.oR1));
    Assertions.assertTrue(sfp.pathExists(example.example1.UIy));
    Assertions.assertTrue(sfp.pathExists(example.example1.Hf));
    Assertions.assertTrue(sfp.pathExists(example.example1.oR2));
    Assertions.assertTrue(sfp.pathExists(example.example2.oC1));
    Assertions.assertTrue(sfp.pathExists(example.example2.Hc));
    Assertions.assertTrue(sfp.pathExists(example.example2.oSta1));
    Assertions.assertTrue(sfp.pathExists(example.example2.oR1));
    Assertions.assertTrue(sfp.pathExists(example.example2.UI));
    Assertions.assertTrue(sfp.pathExists(example.example2.oR2));
  }

}