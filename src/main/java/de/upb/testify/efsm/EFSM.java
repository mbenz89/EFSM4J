package de.upb.testify.efsm;

import org.apache.commons.lang3.tuple.Pair;
import org.jgrapht.ListenableGraph;
import org.jgrapht.graph.DefaultListenableGraph;
import org.jgrapht.graph.DirectedMultigraph;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author Manuel Benz
 * created on 20.02.18
 */
public class EFSM<State, Parameter, Context extends IEFSMContext<Context>, Transition extends de.upb.testify.efsm.Transition<State, Parameter, Context>> {
  public static final String PROP_CONFIGURATION = "PROP_CONFIGURATION";
  private final Context initialContext;
  private final State initialState;
  private final PropertyChangeSupport pcs;
  protected State curState;
  protected Context curContext;
  private ListenableGraph<State, Transition> baseGraph;

  protected EFSM(Set<State> states,
                 State initialState,
                 Context initalContext,
                 Set<Transition> transitions) {
    this.curState = this.initialState = initialState;
    this.curContext = initalContext.snapshot();
    this.initialContext = initalContext.snapshot();

    baseGraph = new DefaultListenableGraph<>(new DirectedMultigraph<State, Transition>((src, tgt) -> {
      throw new IllegalStateException("Edges should not be added without a transition object. We cannot infer a specific transition object due to generics.");
    }), true);

    for (State state : states) {
      baseGraph.addVertex(state);
    }

    for (Transition transition : transitions) {
      baseGraph.addEdge(transition.getSrc(), transition.getTgt(), transition);
    }

    this.pcs = new PropertyChangeSupport(this);
  }

  private EFSM(EFSM<State, Parameter, Context, Transition> base, State initialState, Context initialContext) {
    this.initialContext = initialContext.snapshot();
    this.curContext = initialContext.snapshot();
    this.curState = this.initialState = initialState;
    this.baseGraph = base.baseGraph;
    // we do not want to delegate any events of this to the original listeners
    this.pcs = null;
  }

  public boolean canTransition(Parameter input) {
    for (Transition transition : baseGraph.outgoingEdgesOf(curState)) {
      if (transition.isFeasible(input, curContext)) {
        return true;
      }
    }

    return false;
  }

  public boolean canTransition() {
    return canTransition(null);
  }

  /**
   * Checks if the given input leads to a new configuration, returns the output for the transition taken.
   *
   * @param input
   * @return The output for the taken transition or null if the input is not accepted in the current configuration
   */
  public Set<Parameter> transition(Parameter input) {
    for (Transition transition : baseGraph.outgoingEdgesOf(curState)) {
      if (transition.isFeasible(input, curContext)) {
        Configuration<State, Context> prevConfig = null;
        if (pcs != null) {
          prevConfig = getConfiguration();
        }
        curState = transition.getTgt();
        Set<Parameter> output = transition.take(input, curContext);
        if (pcs != null) {
          pcs.firePropertyChange(PROP_CONFIGURATION, prevConfig, Pair.of(getConfiguration(), transition));
        }
        return output;
      }
    }

    return null;
  }

  /**
   * Checks if the empty input leads to a new configuration, returns the output for the transition taken.
   *
   * @return The output for the taken transition or null if the input is not accepted in the current configuration
   */
  public Set<Parameter> transition() {
    return transition((Parameter) null);
  }


  public List<Set<Parameter>> transition(EFSMPath<State, Parameter, Context, Transition> path) {
    List<Set<Parameter>> res = new ArrayList<>(path.getLength());
    Iterator<Parameter> inputsToTrigger = path.getInputsToTrigger();
    while (inputsToTrigger.hasNext()) {
      Set<Parameter> out = transition(inputsToTrigger.next());
      if (out == null) {
        return null;
      } else {
        res.add(out);
      }
    }
    return res;
  }


  public Configuration transitionAndDrop(EFSMPath<State, Parameter, Context, Transition> path) {
    return transition(path) == null ? null : getConfiguration();
  }

  /**
   * Checks if the given input leads to a new configuration, returns the new configuration or null otherwise.
   *
   * @param input
   * @return The configuration after taking one of the possible transitions for the given input or null if the input is not accepted in the current configuration
   */
  public Configuration transitionAndDrop(Parameter input) {
    if (transition(input) != null) {
      return getConfiguration();
    } else {
      return null;
    }
  }

  /**
   * Checks if the empty input leads to a new configuration, returns the new configuration or null otherwise.
   *
   * @return The configuration after taking one of the possible transitions for the empty input or null if the input is not accepted in the current configuration
   */
  public Configuration transitionAndDrop() {
    return transitionAndDrop((Parameter) null);
  }

  public Configuration<State, Context> getConfiguration() {
    // this should be immutabable or at least changes should not infer with the state of this machine
    return new Configuration(curState, curContext.snapshot());
  }

  public Configuration<State, Context> getInitialConfiguration() {
    return new Configuration(initialState, initialContext.snapshot());
  }


  public Set<State> getStates() {
    return baseGraph.vertexSet();
  }

  public Set<Transition> getTransitons() {
    return baseGraph.edgeSet();
  }

  public Set<Transition> transitionsOutOf(State state) {
    return baseGraph.outgoingEdgesOf(state);
  }

  public Set<Transition> transitionsInTo(State state) {
    return baseGraph.incomingEdgesOf(state);
  }

  public void reset() {
    forceConfiguration(new Configuration<>(initialState, initialContext.snapshot()));
  }

  protected ListenableGraph<State, Transition> getBaseGraph() {
    return baseGraph;
  }


  public void addPropertyChangeListener(PropertyChangeListener listener) {
    this.pcs.addPropertyChangeListener(listener);
  }

  public void removePropertyChangeListener(PropertyChangeListener listener) {
    this.pcs.removePropertyChangeListener(listener);
  }

  /**
   * This will track changes to the base graph but not configuration changes to the efsm. Also, no property listeners are copied.
   * Shouldn't be that expensive since only the context and state change
   *
   * @return
   */
  protected EFSM<State, Parameter, Context, Transition> snapshot(State initialState, Context initialContext) {
    return new EFSM<>(this, initialState, initialContext);
  }

  /**
   * This will track changes to the base graph but not configuration changes to the efsm. Also, no property listeners are copied.
   * Shouldn't be that expensive since only the context and state change
   *
   * @return
   */
  protected EFSM<State, Parameter, Context, Transition> snapshot() {
    return snapshot(this.curState, this.curContext);
  }

  public void forceConfiguration(Configuration<State, Context> config) {
    Configuration<State, Context> prefConfig = null;
    if (pcs != null) {
      prefConfig = getConfiguration();
    }
    this.curState = config.getState();
    this.curContext = config.getContext().snapshot();
    if (pcs != null) {
      this.pcs.firePropertyChange(PROP_CONFIGURATION, prefConfig, getConfiguration());
    }
  }
}