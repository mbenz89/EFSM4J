package de.upb.testify.efsm;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

/**
 * @author Manuel Benz
 * created on 26.02.18
 */
public class Context extends HashSet<Object> implements IEFSMContext<Context> {

  public Context(Collection<?> c) {
    super(c);
  }

  public Context(Object... o) {
    this(Arrays.asList(o));
  }

  @Override
  public Context snapshot() {
    return new Context(this);
  }


}
