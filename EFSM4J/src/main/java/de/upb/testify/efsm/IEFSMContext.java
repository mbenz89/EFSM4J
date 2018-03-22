package de.upb.testify.efsm;

/**
 * @author Manuel Benz
 * created on 26.02.18
 */
public interface IEFSMContext<Context> {

  Context snapshot();
}
