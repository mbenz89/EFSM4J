package de.upb.testify.efsm.eefsm;

import de.upb.testify.efsm.ContextVar;
import de.upb.testify.efsm.EFSMBuilder;
import de.upb.testify.efsm.Param;
import de.upb.testify.efsm.State;

/**
 * @author Manuel Benz
 * created on 22.02.18
 */
public class WhiteBoardExample {

  public EEFSM<State, Param, Object> eefsm;

  // loi enable is the only context var that is not a state
  ContextVar Le = new ContextVar("Le");

  State oC1 = new State("oC1");
  State oSta1 = new State("oSta1");
  State Hc = new State("Hc");
  State oR1 = new State("oR1");
  State UIx = new State("UIx");
  State Hx = new State("Hx");
  State UI = new State("UI");
  State H = new State("H");
  State oSto1 = new State("oSto1");
  State oD1 = new State("oD1");
  State oC2 = new State("oC2");
  State oSta2 = new State("oSta2");
  State oR2 = new State("oR2");
  State UIy = new State("UIy");
  State UIf = new State("UIf");
  State Hy = new State("Hy");
  State Hf = new State("Hf");
  State B2 = new State("B2");

  Param HcEntry = new Param("HcEntry");
  Param oSta1Entry = new Param("oSta1Entry");
  Param oR1Entry = new Param("oR1Entry");
  Param UIxClick = new Param("UIxClick");
  Param HxEntry = new Param("HxEntry");
  Param UIClick = new Param("UIClick");
  Param HEntry = new Param("HEntry");
  Param oC2Entry = new Param("oC2Entry");
  Param oSta2Entry = new Param("oSta2Entry");
  Param oR2Entry = new Param("oR2Entry");
  Param oD1Entry = new Param("oD1Entry");
  Param oSto1Entry = new Param("oSto1Entry");
  Param UIyClick = new Param("UIyClick");
  Param UIfClick = new Param("UIfClick");
  Param HyEntry = new Param("HyEntry");
  Param HfEntry = new Param("HfEntry");
  Param EvtBack = new Param("EvtBack");
  Param oC1Entry = new Param("oC1Entry");

  ETransition<State, Param, Object> oC1ToHc = new ETransitionbuilder<State, Param, Object>().fireOnInput(HcEntry).build();
  ETransition<State, Param, Object> hCToOsta1 = new ETransitionbuilder<State, Param, Object>().fireOnInput(oSta1Entry).addToContext(Hc).build();
  ETransition<State, Param, Object> oC1ToOsta1 = new ETransitionbuilder<State, Param, Object>().fireOnInput(oSta1Entry).build();
  ETransition<State, Param, Object> oSta1ToOr1 = new ETransitionbuilder<State, Param, Object>().fireOnInput(oR1Entry).build();
  ETransition<State, Param, Object> oR1ToUix = new ETransitionbuilder<State, Param, Object>().fireOnInput(UIxClick).fireIfInContext(Le).build();
  ETransition<State, Param, Object> uiXToHx = new ETransitionbuilder<State, Param, Object>().fireOnInput(HxEntry).build();
  ETransition<State, Param, Object> hXToOr1 = new ETransitionbuilder<State, Param, Object>().build();
  ETransition<State, Param, Object> oR1ToUi = new ETransitionbuilder<State, Param, Object>().fireOnInput(UIClick).fireIfInContext(Hc).build();
  ETransition<State, Param, Object> uiToH = new ETransitionbuilder<State, Param, Object>().fireOnInput(HEntry).build();
  ETransition<State, Param, Object> hToOc2 = new ETransitionbuilder<State, Param, Object>().fireOnInput(oC2Entry).build();
  ETransition<State, Param, Object> oC2ToOSta2 = new ETransitionbuilder<State, Param, Object>().fireOnInput(oSta2Entry).build();
  ETransition<State, Param, Object> oSta2ToOr2 = new ETransitionbuilder<State, Param, Object>().fireOnInput(oR2Entry).build();
  ETransition<State, Param, Object> oR2ToOSto1 = new ETransitionbuilder<State, Param, Object>().fireOnInput(oSto1Entry).addToContext(oR2).build();
  ETransition<State, Param, Object> oSto1ToOD1 = new ETransitionbuilder<State, Param, Object>().fireOnInput(oD1Entry).build();
  ETransition<State, Param, Object> oSto1ToOR2 = new ETransitionbuilder<State, Param, Object>().fireIfInContext(oR2).addToContext(oSto1).removeFromContext(oR2).build();
  ETransition<State, Param, Object> oR2ToUIy = new ETransitionbuilder<State, Param, Object>().fireOnInput(UIyClick).addToContext(Le).build();
  ETransition<State, Param, Object> uiYToHy = new ETransitionbuilder<State, Param, Object>().fireOnInput(HyEntry).build();
  ETransition<State, Param, Object> hYToB2 = new ETransitionbuilder<State, Param, Object>().fireOnInput(EvtBack).build();
  ETransition<State, Param, Object> oR2ToUIF = new ETransitionbuilder<State, Param, Object>().fireOnInput(UIfClick).build();
  ETransition<State, Param, Object> uiFToHf = new ETransitionbuilder<State, Param, Object>().fireOnInput(HfEntry).build();
  ETransition<State, Param, Object> hFToB2 = new ETransitionbuilder<State, Param, Object>().fireOnInput(EvtBack).build();
  ETransition<State, Param, Object> b2ToOC1 = new ETransitionbuilder<State, Param, Object>().fireOnInput(oC1Entry).fireIfInContext(oSto1).removeFromContext(oSto1, Hc).build();
  ETransition<State, Param, Object> b2ToOr1 = new ETransitionbuilder<State, Param, Object>().fireOnInput(oR1Entry).fireIfNotInContext(oSto1).build();


  State initialState = oC1;
  EEFSMContext<Object> initialContext = new EEFSMContext();
  State loiState = Hx;

  public WhiteBoardExample() {
    EFSMBuilder<State, Param, EEFSMContext<Object>, ETransition<State, Param, Object>, EEFSM<State, Param, Object>> builder = EEFSM.builder();

    eefsm = builder.withState(oC1, oSta1, Hc, oR1, UIx, Hx, UI, H, oSto1, oD1, oC2, oSta2, oR2, UIy, UIf, Hy, Hf, B2)
        .withInitialState(initialState)
        .withInitialContext(initialContext)
        .withTransition(oC1, Hc, oC1ToHc)
        .withTransition(Hc, oSta1, hCToOsta1)
        .withTransition(oC1, oSta1, oC1ToOsta1)
        .withTransition(oSta1, oR1, oSta1ToOr1)
        .withTransition(oR1, UIx, oR1ToUix)
        .withTransition(UIx, Hx, uiXToHx)
        .withTransition(Hx, oR1, hXToOr1)
        .withTransition(oR1, UI, oR1ToUi)
        .withTransition(UI, H, uiToH)
        .withTransition(H, oC2, hToOc2)
        .withTransition(oC2, oSta2, oC2ToOSta2)
        .withTransition(oSta2, oR2, oSta2ToOr2)
        .withTransition(oR2, oSto1, oR2ToOSto1)
        .withTransition(oSto1, oD1, oSto1ToOD1)
        .withTransition(oSto1, oR2, oSto1ToOR2)
        .withTransition(oR2, UIy, oR2ToUIy)
        .withTransition(UIy, Hy, uiYToHy)
        .withTransition(Hy, B2, hYToB2)
        .withTransition(oR2, UIf, oR2ToUIF)
        .withTransition(UIf, Hf, uiFToHf)
        .withTransition(Hf, B2, hFToB2)
        .withTransition(B2, oC1, b2ToOC1)
        .withTransition(B2, oR1, b2ToOr1)
        .build();
  }
}
