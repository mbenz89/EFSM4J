package de.upb.mb.efsm;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Manuel Benz
 * created on 20.02.18
 */
public class EFSMBuilder<State, Parameter, Context, EFSMType extends EFSM<State, Parameter, Context>> {
  private final Class<EFSMType> efsmTypeClass;
  private Set<State> states = new HashSet<>();
  private State initialState;
  private Set<Transition<State, Parameter, Context>> transitions = new HashSet<>();
  private Context initialContext;

  public EFSMBuilder(Class<EFSMType> efsmTypeClass) {
    this.efsmTypeClass = efsmTypeClass;
  }

  public EFSMBuilder<State, Parameter, Context, EFSMType> withState(State... s) {
    if (s != null) {
      states.addAll(Arrays.asList(s));
    }
    return this;
  }

  public EFSMBuilder<State, Parameter, Context, EFSMType> withInitialState(State s) {
    if (initialState != null) {
      throw new IllegalStateException("Initial state already set");
    }
    initialState = s;
    states.add(initialState);
    return this;
  }

  public EFSMBuilder<State, Parameter, Context, EFSMType> withTransition(State src, State tgt, Transition<State, Parameter, Context> t) {
    states.add(src);
    states.add(tgt);

    t.setSrc(src);
    t.setTgt(tgt);

    transitions.add(t);
    return this;
  }

  public EFSMBuilder<State, Parameter, Context, EFSMType> withInitialContext(Context initialContext) {
    this.initialContext = initialContext;
    return this;
  }

  public EFSMType build() {
    if (initialState == null) {
      throw new IllegalStateException("Initial state not set");
    }
    if (initialContext == null) {
      throw new IllegalStateException("Context must be initialized");
    }

    try {
      Constructor<EFSMType> constructor = getConstructor();
      if (constructor == null) {
        throw new RuntimeException();
      }
      constructor.setAccessible(true);
      return constructor.newInstance(states, initialState, initialContext, transitions);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

  }

  private Constructor<EFSMType> getConstructor() {
    for (Constructor<?> constructor : efsmTypeClass.getDeclaredConstructors()) {
      Class<?>[] parameterTypes = constructor.getParameterTypes();
      if (parameterTypes.length != 4) {
        continue;
      }

      if (parameterTypes[0].isAssignableFrom(states.getClass()) && parameterTypes[1].isAssignableFrom(initialState.getClass()) && parameterTypes[2].isAssignableFrom(initialContext.getClass()) && parameterTypes[3].isAssignableFrom(transitions.getClass())) {
        return (Constructor<EFSMType>) constructor;
      }
    }
    return null;
  }

}
