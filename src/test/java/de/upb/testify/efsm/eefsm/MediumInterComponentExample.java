package de.upb.testify.efsm.eefsm;

import de.upb.testify.efsm.EFSMBuilder;
import de.upb.testify.efsm.Param;
import de.upb.testify.efsm.State;

/** @author Manuel Benz created on 20.03.18 */
public class MediumInterComponentExample {

  BasicInterComponentExample example1 = new BasicInterComponentExample();
  BasicInterComponentExample example2 = new BasicInterComponentExample(2);
  Param e2Entry;
  EEFSMContext<Object> initialContext = example1.initialContext;

  EEFSM<State, Param, Object> eefsm;

  public MediumInterComponentExample() {
    EFSMBuilder<State, Param, EEFSMContext<Object>, ETransition<State, Param, Object>, EEFSM<State, Param, Object>> builder
        = EEFSM.builder();
    builder.withEFSM(example1.eefsm);
    builder.withEFSM(example2.eefsm);
    e2Entry = new Param("e2Entry");
    builder.withTransition(example1.Hf, example2.oC1, new ETransition<>(e2Entry, example1.Le, true, null, null));

    eefsm = builder.build(example1.initialState, example2.initialContext);
  }
}
