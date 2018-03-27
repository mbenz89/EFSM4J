package de.upb.testify.efsm;

/**
 * @author Manuel Benz
 * created on 26.02.18
 */
public interface IEFSMContext<Context> {

  /**
   * Creates a hard copy of this context which can live and change independently
   * @return
   */
  Context snapshot();
}
