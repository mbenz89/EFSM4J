package de.upb.testify.efsm.eefsm;

import de.upb.testify.efsm.IEFSMContext;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * @author Manuel Benz
 * created on 22.02.18
 */
public class EEFSMContext<ContextObject> implements IEFSMContext<EEFSMContext<ContextObject>> {

  private final HashSet<ContextObject> internalSet = new HashSet<>();
  private boolean hashInvalidated = true;
  private int cachedHash;

  public EEFSMContext(ContextObject... o) {
    union(o);
  }

  public EEFSMContext(Collection<ContextObject> col) {
    internalSet.addAll(col);
  }

  public boolean elementOf(ContextObject o) {
    return internalSet.contains(o);
  }

  public boolean notElementOf(ContextObject o) {
    return !internalSet.contains(o);
  }

  public void union(ContextObject o) {
    internalSet.add(o);
    hashInvalidated = true;
  }

  public void union(ContextObject... o) {
    Collections.addAll(internalSet, o);
    hashInvalidated = true;
  }

  public void union(Collection<ContextObject> o) {
    internalSet.addAll(o);
    hashInvalidated = true;
  }

  public void union(EEFSMContext c) {
    internalSet.addAll(c.internalSet);
    hashInvalidated = true;
  }

  public void remove(ContextObject o) {
    internalSet.remove(o);
    hashInvalidated = true;
  }

  public void remove(ContextObject... o) {
    internalSet.removeAll(Arrays.asList(o));
    hashInvalidated = true;
  }

  public void remove(Collection<ContextObject> o) {
    internalSet.removeAll(o);
    hashInvalidated = true;
  }

  public void remove(EEFSMContext c) {
    internalSet.removeAll(c.internalSet);
    hashInvalidated = true;
  }

  @Override
  public String toString() {
    return "{" + internalSet.stream().map(ContextObject::toString).collect(Collectors.joining(", ")) + "}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EEFSMContext<?> that = (EEFSMContext<?>) o;
    return com.google.common.base.Objects.equal(internalSet, that.internalSet);
  }

  @Override
  public int hashCode() {
    if (hashInvalidated) {
      cachedHash = com.google.common.base.Objects.hashCode(internalSet);
      hashInvalidated = false;
    }

    return cachedHash;
  }

  @Override
  public EEFSMContext<ContextObject> snapshot() {
    return new EEFSMContext<>(internalSet);
  }
}
