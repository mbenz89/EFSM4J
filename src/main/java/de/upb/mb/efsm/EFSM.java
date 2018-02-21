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
public class EFSM<State, Parameter, Context> {
  private final Set<State> states;
  private final Set<Transition<State, Parameter, Context>> transitons;
  private final Multimap<State, Transition<State, Parameter, Context>> srcToTransitions;
  private final Multimap<State, Transition<State, Parameter, Context>> tgtToTransitions;

  private final Context context;
  private State curState;

  protected EFSM(Set<State> states,
                 State initialState,
                 Context initalContext,
                 Set<Transition<State, Parameter, Context>> transitions,
                 Multimap<State, Transition<State, Parameter, Context>> srcToTransitions,
                 Multimap<State, Transition<State, Parameter, Context>> tgtToTransitions) {
    this.states = new HashSet<>(states);
    this.curState = initialState;
    this.transitons = new HashSet<>(transitions);
    this.context = initalContext;
    this.srcToTransitions = srcToTransitions;
    this.tgtToTransitions = tgtToTransitions;
  }

  public boolean canTransfer(Parameter input) {
    for (Transition<State, Parameter, Context> transition : srcToTransitions.get(curState)) {
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
    for (Transition<State, Parameter, Context> transition : srcToTransitions.get(curState)) {
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

  public Set<Transition<State, Parameter, Context>> getTransitons() {
    return Collections.unmodifiableSet(transitons);
  }

  public class Configuration {
    private final State curState;
    private final Context context;

    public Configuration(State curState, Context context) {
      this.curState = curState;
      this.context = context;
    }

    public State getCurState() {
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
