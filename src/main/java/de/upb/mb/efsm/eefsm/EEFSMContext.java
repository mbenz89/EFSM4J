package de.upb.mb.efsm.eefsm;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * @author Manuel Benz
 * created on 22.02.18
 */
public class EEFSMContext<ContextObject> {

  private final HashSet<ContextObject> internalSet = new HashSet<>();

  public EEFSMContext(ContextObject... o) {
    union(o);
  }

  public boolean elementOf(ContextObject o) {
    return internalSet.contains(o);
  }

  public boolean notElementOf(ContextObject o) {
    return !internalSet.contains(o);
  }

  public void union(ContextObject o) {
    internalSet.add(o);
  }

  public void union(ContextObject... o) {
    internalSet.addAll(Arrays.asList(o));
  }

  public void union(Collection<ContextObject> o) {
    internalSet.addAll(o);
  }

  public void union(EEFSMContext c) {
    internalSet.addAll(c.internalSet);
  }

  public void remove(ContextObject o) {
    internalSet.remove(o);
  }

  public void remove(ContextObject... o) {
    internalSet.removeAll(Arrays.asList(o));
  }

  public void remove(Collection<ContextObject> o) {
    internalSet.removeAll(o);
  }

  public void remove(EEFSMContext c) {
    internalSet.removeAll(c.internalSet);
  }

  @Override
  public String toString() {
    return "{" + internalSet.stream().map(ContextObject::toString).collect(Collectors.joining(", ")) + "}";
  }
}
