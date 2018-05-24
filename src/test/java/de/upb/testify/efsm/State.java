package de.upb.testify.efsm;

/** @author Manuel Benz created on 20.02.18 */
public class State extends Super implements Comparable<State> {

  public State(String id) {
    super(id);
  }

  @Override
  public int compareTo(State o) {
    return getId().compareTo(o.getId());
  }
}
