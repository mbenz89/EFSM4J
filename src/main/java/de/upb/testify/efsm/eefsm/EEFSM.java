package de.upb.testify.efsm.eefsm;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jgrapht.Graph;

import de.upb.testify.efsm.Configuration;
import de.upb.testify.efsm.EFSM;
import de.upb.testify.efsm.EFSMBuilder;

/** @author Manuel Benz created on 22.02.18 */
public class EEFSM<State, Input, ContextObject>
    extends EFSM<State, Input, EEFSMContext<ContextObject>, ETransition<State, Input, ContextObject>> {
  protected EEFSM(Graph<State, ETransition<State, Input, ContextObject>> baseGraph, State initialState,
      EEFSMContext initalContext) {
    super(baseGraph, initialState, initalContext);
  }

  public static <State, Input, ContextObject> EFSMBuilder<State, Input, EEFSMContext<ContextObject>,
      ETransition<State, Input, ContextObject>, EEFSM<State, Input, ContextObject>> builder() {
    return new EFSMBuilder(EEFSM.class);
  }

  @Override
  protected EEFSM<State, Input, ContextObject> snapshot(State initialState, EEFSMContext<ContextObject> initialContext) {
    return new EEFSM<>(this.getBaseGraph(), initialState, initialContext);
  }

  @Override
  protected EEFSM<State, Input, ContextObject> snapshot() {
    return snapshot(curState, curContext);
  }

  public List<Set<Input>> transition(EEFSMPath<State, Input, ContextObject> path) {
    List<Set<Input>> res = new ArrayList<>(path.getLength());
    Iterator<Input> inputsToTrigger = path.getInputsToTrigger();
    while (inputsToTrigger.hasNext()) {
      Set<Input> out = transition(inputsToTrigger.next());
      if (out == null) {
        return null;
      } else {
        res.add(out);
      }
    }
    return res;
  }

  public Configuration transitionAndDrop(EEFSMPath<State, Input, ContextObject> path) {
    return transition(path) == null ? null : getConfiguration();
  }

  protected State getCurrentState() {
    return curState;
  }
}
