package de.upb.testify.efsm.eefsm;

/**
 * @author Manuel Benz
 * created on 05.03.18
 */
public class InterComponentExampleInfeasible extends BasicInterComponentExample {

  @Override
  protected void changeSomething() {
    // we remove the addtion of le context variable in the transtiiton from or2 to uly to make reacheing hx impossible
    oR2ToUIy = new ETransitionbuilder().fireOnInput(UIyClick).build();
  }
}
