package de.upb.mb.efsm.eefsm;

import de.upb.mb.efsm.EFSMBuilder;
import de.upb.mb.efsm.Param;
import de.upb.mb.efsm.State;

/**
 * @author Manuel Benz
 * created on 22.02.18
 */
public class WhiteBoardExample {

  public EEFSM<State, Param, Object> eefsm;

  public WhiteBoardExample() {
    EFSMBuilder<State, Param, EEFSMContext<Object>, EEFSM<State, Param, Object>> builder = EEFSM.builder();

   // builder.
  }


}
