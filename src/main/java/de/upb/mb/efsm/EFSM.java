package de.upb.mb.efsm;

import com.google.common.base.Objects;
import com.google.common.collect.Multimap;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Manuel Benz
 * created on 20.02.18
 */
public class EFSM<State, Parameter, Variable> {
  private final Set<State> states;
  private final Set<Transition<State, Parameter, Variable>> transitons;
  private final Multimap<State, Transition<State, Parameter, Variable>> srcToTransitions;
  private final Multimap<State, Transition<State, Parameter, Variable>> tgtToTransitions;

  private final Set<Variable> context;
  private State curState;

  protected EFSM(Set<State> states,
                 State initialState,
                 Set<Transition<State, Parameter, Variable>> transitions,
                 Set<Variable> initalContext,
                 Multimap<State, Transition<State, Parameter, Variable>> srcToTransitions,
                 Multimap<State, Transition<State, Parameter, Variable>> tgtToTransitions) {
    this.states = new HashSet<>(states);
    this.curState = initialState;
    this.transitons = new HashSet<>(transitions);
    this.context = new HashSet<>(initalContext);
    this.srcToTransitions = srcToTransitions;
    this.tgtToTransitions = tgtToTransitions;
  }

  public boolean canTransfer(Parameter input) {
    for (Transition<State, Parameter, Variable> transition : srcToTransitions.get(curState)) {
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
    for (Transition<State, Parameter, Variable> transition : srcToTransitions.get(curState)) {
      if (transition.isFeasible(input, context)) {
        curState = transition.getTgt();
        return transition.take(input, context);
      }
    }

    return null;
  }

  public Configuration getConfiguration() {
    return new Configuration(curState, context);
  }

  public Set<State> getStates() {
    return Collections.unmodifiableSet(states);
  }

  public Set<Transition<State, Parameter, Variable>> getTransitons() {
    return Collections.unmodifiableSet(transitons);
  }

  public class Configuration {
    private final State curState;
    private final Set<Variable> context;

    public Configuration(State curState, Set<Variable> context) {
      this.curState = curState;
      this.context = context;
    }

    public State getCurState() {
      return curState;
    }

    public Set<Variable> getContext() {
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
