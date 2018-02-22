package de.upb.mb.efsm;

import com.google.common.base.Objects;
import org.jgrapht.graph.DirectedMultigraph;

import java.util.Set;

/**
 * @author Manuel Benz
 * created on 20.02.18
 */
public class EFSM<State, Parameter, Context> {
  private final Context context;
  private DirectedMultigraph<State, Transition<State, Parameter, Context>> baseGraph;
  private State curState;

  protected EFSM(Set<State> states,
                 State initialState,
                 Context initalContext,
                 Set<Transition<State, Parameter, Context>> transitions) {
    this.curState = initialState;
    this.context = initalContext;
    baseGraph = new DirectedMultigraph<>((src, tgt) -> {
      EpsilonTransition<State, Parameter, Context> e = new EpsilonTransition<>();
      e.setSrc(src);
      e.setTgt(tgt);
      return e;
    });

    for (State state : states) {
      baseGraph.addVertex(state);
    }

    for (Transition<State, Parameter, Context> transition : transitions) {
      baseGraph.addEdge(transition.getSrc(), transition.getTgt(), transition);
    }
  }

  public boolean canTransfer(Parameter input) {
    for (Transition<State, Parameter, Context> transition : baseGraph.outgoingEdgesOf(curState)) {
      if (transition.isFeasible(input, context)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Checks if the given input leads to a new configuration, returns the output for the transition taken.
   *
   * @param input
   * @return The output of the taken transition or null if the input is not accepted in the current configuration
   */
  public Set<Parameter> transfer(Parameter input) {
    for (Transition<State, Parameter, Context> transition : baseGraph.outgoingEdgesOf(curState)) {
      if (transition.isFeasible(input, context)) {
        curState = transition.getTgt();
        return transition.take(input, context);
      }
    }

    return null;
  }

  /**
   * Checks if the given input leads to a new configuration, returns the new configuration or null otherwise.
   *
   * @param input
   * @return The configuration after taking one of the posible transitions for the given input or null if the input is not accepted in the current configuration
   */
  public Configuration transferAndDrop(Parameter input) {
    if (transfer(input) != null) {
      return getConfiguration();
    } else {
      return null;
    }
  }

  public Configuration getConfiguration() {
    return new Configuration(curState, context);
  }

  public Set<State> getStates() {
    return baseGraph.vertexSet();
  }

  public Set<Transition<State, Parameter, Context>> getTransitons() {
    return baseGraph.edgeSet();
  }

  public Set<Transition<State, Parameter, Context>> transitionOutOf(State state) {
    return baseGraph.outgoingEdgesOf(curState);
  }

  public Set<Transition<State, Parameter, Context>> transitionInTo(State state) {
    return baseGraph.incomingEdgesOf(curState);
  }

  public DirectedMultigraph<State, Transition<State, Parameter, Context>> getBaseGraph() {
    return baseGraph;
  }

  public class Configuration {
    private final State curState;
    private final Context context;

    public Configuration(State curState, Context context) {
      this.curState = curState;
      this.context = context;
    }

    public State getState() {
      return curState;
    }

    public Context getContext() {
      return context;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      Configuration that = (Configuration) o;
      return Objects.equal(curState, that.curState) &&
          Objects.equal(context, that.context);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(curState, context);
    }
  }
}
