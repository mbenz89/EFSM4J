package de.upb.testify.efsm;

import org.jgrapht.ListenableGraph;
import org.jgrapht.graph.DefaultListenableGraph;
import org.jgrapht.graph.DirectedMultigraph;

import java.util.Set;

/**
 * @author Manuel Benz
 * created on 20.02.18
 */
public class EFSM<State, Parameter, Context extends IEFSMContext<Context>, Transition extends de.upb.testify.efsm.Transition<State, Parameter, Context>> {
  private final Context context;
  private ListenableGraph<State, Transition> baseGraph;
  private State curState;

  protected EFSM(Set<State> states,
                 State initialState,
                 Context initalContext,
                 Set<Transition> transitions) {
    this.curState = initialState;
    this.context = initalContext;
    baseGraph = new DefaultListenableGraph<>(new DirectedMultigraph<State, Transition>((src, tgt) -> {
      throw new IllegalStateException("Edges should not be added without a transition object. We cannot infer a specific transition object due to generics.");
    }), true);

    for (State state : states) {
      baseGraph.addVertex(state);
    }

    for (Transition transition : transitions) {
      baseGraph.addEdge(transition.getSrc(), transition.getTgt(), transition);
    }
  }

  public boolean canTransition(Parameter input) {
    for (Transition transition : baseGraph.outgoingEdgesOf(curState)) {
      if (transition.isFeasible(input, context)) {
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
      if (transition.isFeasible(input, context)) {
        curState = transition.getTgt();
        return transition.take(input, context);
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
    return transition(null);
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
    return transitionAndDrop(null);
  }

  public Configuration<State, Context> getConfiguration() {
    // this should be immutabable or at least changes should not infer with the state of this machine
    return new Configuration(curState, context.snapshot());
  }

  public Set<State> getStates() {
    return baseGraph.vertexSet();
  }

  public Set<Transition> getTransitons() {
    return baseGraph.edgeSet();
  }

  public Set<Transition> transitionsOutOf(State state) {
    return baseGraph.outgoingEdgesOf(curState);
  }

  public Set<Transition> transitionsInTo(State state) {
    return baseGraph.incomingEdgesOf(curState);
  }

  protected ListenableGraph<State, Transition> getBaseGraph() {
    return baseGraph;
  }

}
