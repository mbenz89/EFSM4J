package de.upb.testify.efsm.eefsm;

import de.upb.testify.efsm.ContextVar;
import de.upb.testify.efsm.EFSMBuilder;
import de.upb.testify.efsm.Param;
import de.upb.testify.efsm.State;

/**
 * @author Manuel Benz
 * created on 19.03.18
 */
public class LargeInterComponentExample {
  BasicInterComponentExample example1 = new BasicInterComponentExample();
  BasicInterComponentExample example2 = new BasicInterComponentExample(2);
  BasicInterComponentExample example3 = new BasicInterComponentExample(3);
  BasicInterComponentExample example4 = new BasicInterComponentExample(4);
  BasicInterComponentExample example5 = new BasicInterComponentExample(5);
  BasicInterComponentExample example6 = new BasicInterComponentExample(6);

  ContextVar additionalContext = new ContextVar("add");
  State tgt = new State("tgt");
  EEFSMContext<Object> initialContext = example1.initialContext;

  EEFSM<State, Param, Object> eefsm;

  public LargeInterComponentExample() {
    EFSMBuilder<State, Param, EEFSMContext<Object>, ETransition<State, Param, Object>, EEFSM<State, Param, Object>> builder = EEFSM.builder();
    builder.withInitialState(example1.initialState).withInitialContext(example1.initialContext);
    builder.withEFSM(example1.eefsm);
    builder.withEFSM(example2.eefsm);
    builder.withTransition(example1.Hf, example2.oC1, new ETransition<>(new Param("e2Entry"), example1.Le, true, null, null));
    builder.withEFSM(example3.eefsm);
    builder.withTransition(example2.Hf, example3.oC1, new ETransition<>(new Param("e3Entry"), example2.Le, true, null, null));
    builder.withTransition(example3.oR1, example2.oR2, new ETransition<>(example3.EvtBack, null, true, null, null));
    builder.withEFSM(example4.eefsm);
    builder.withTransition(example3.Hf, example4.oC1, new ETransition<>(new Param("e4Entry"), example3.Hc, true, null, null));
    builder.withTransition(example4.oR1, example2.oR2, new ETransition<>(example4.EvtBack, null, true, null, null));
    builder.withState(tgt);
    builder.withTransition(example3.oD1, tgt, new ETransition<>(new Param("tgtEntry"), additionalContext, true, null, null));
    builder.withEFSM(example5.eefsm);
    builder.withTransition(example4.Hf, example5.oC1, new ETransition<>(new Param("e5Entry"), example1.Le, true, null, null));
    builder.withTransition(example5.oR1, example4.oR2, new ETransition<>(example5.EvtBack, null, true, null, null));
    builder.withEFSM(example6.eefsm);
    builder.withTransition(example5.Hf, example6.oC1, new ETransition<>(new Param("e6Entry"), example1.Le, true, new ContextVar[] {additionalContext}, null));
    builder.withTransition(example6.oR1, example5.oR2, new ETransition<>(example6.EvtBack, null, true, null, null));

    eefsm = builder.build();
  }

}
