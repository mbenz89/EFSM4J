package de.upb.testify.efsm.eefsm;

import de.upb.testify.efsm.ContextVar;
import de.upb.testify.efsm.EFSMBuilder;
import de.upb.testify.efsm.Param;
import de.upb.testify.efsm.State;

/**
 * @author Manuel Benz
 * created on 22.02.18
 */
public class BasicInterComponentExample {

  public EEFSM<State, Param, Object> eefsm;

  // loi enable is the only context var that is not a state
  ContextVar Le;

  State oC1;
  State oSta1;
  State Hc;
  State oR1;
  State UIx;
  State Hx;
  State UI;
  State H;
  State oSto1;
  State oD1;
  State oC2;
  State oSta2;
  State oR2;
  State UIy;
  State UIf;
  State Hy;
  State Hf;
  State B2;

  Param HcEntry;
  Param oSta1Entry;
  Param oR1Entry;
  Param UIxClick;
  Param HxEntry;
  Param UIClick;
  Param HEntry;
  Param oC2Entry;
  Param oSta2Entry;
  Param oR2Entry;
  Param oD1Entry;
  Param oSto1Entry;
  Param UIyClick;
  Param UIfClick;
  Param HyEntry;
  Param HfEntry;
  Param EvtBack;
  Param oC1Entry;

  ETransition<State, Param, Object> oC1ToHc;
  ETransition<State, Param, Object> hCToOsta1;
  ETransition<State, Param, Object> oC1ToOsta1;
  ETransition<State, Param, Object> oSta1ToOr1;
  ETransition<State, Param, Object> oR1ToUix;
  ETransition<State, Param, Object> uiXToHx;
  ETransition<State, Param, Object> hXToOr1;
  ETransition<State, Param, Object> oR1ToUi;
  ETransition<State, Param, Object> uiToH;
  ETransition<State, Param, Object> hToOc2;
  ETransition<State, Param, Object> oC2ToOSta2;
  ETransition<State, Param, Object> oSta2ToOr2;
  ETransition<State, Param, Object> oR2ToOSto1;
  ETransition<State, Param, Object> oSto1ToOD1;
  ETransition<State, Param, Object> oSto1ToOR2;
  ETransition<State, Param, Object> oR2ToUIy;
  ETransition<State, Param, Object> uiYToHy;
  ETransition<State, Param, Object> hyToOr2;
  ETransition<State, Param, Object> oR2ToUIF;
  ETransition<State, Param, Object> uiFToHf;
  ETransition<State, Param, Object> hfToOr2;
  ETransition<State, Param, Object> oR2ToB2;
  ETransition<State, Param, Object> b2ToOC1;
  ETransition<State, Param, Object> b2ToOr1;


  State initialState;
  EEFSMContext<Object> initialContext = new EEFSMContext();
  State loiState;

  public BasicInterComponentExample() {
    this(0);
  }

  public BasicInterComponentExample(int version) {
    EFSMBuilder<State, Param, EEFSMContext<Object>, ETransition<State, Param, Object>, EEFSM<State, Param, Object>> builder = EEFSM.builder();

    String v = version == 0 ? "" : "_" + version;


    oC1 = new State("onCreate1" + v);
    oSta1 = new State("onStart1" + v);
    Hc = new State("HandlerGCreation" + v);
    oR1 = new State("onResume1" + v);
    Hx = new State("HandlerX" + v);
    UIx = new State("UiXClicked" + v);
    UI = new State("UiGClicked" + v);
    H = new State("HandlerG" + v);
    oSto1 = new State("onStop1" + v);
    oD1 = new State("onDestroy1" + v);
    oC2 = new State("onCreate2" + v);
    oSta2 = new State("onStart2" + v);
    oR2 = new State("onRresume2" + v);
    UIy = new State("UiYClicked" + v);
    UIf = new State("UiFClicked" + v);
    Hy = new State("HandlerY" + v);
    Hf = new State("HandlerF" + v);
    B2 = new State("Back2" + v);

    initialState = oC1;
    loiState = Hx;

    HcEntry = new Param("HcEntry" + v);
    oC1ToHc = new ETransitionbuilder<State, Param, Object>().fireOnInput(HcEntry).build();
    oSta1Entry = new Param("oSta1Entry" + v);
    hCToOsta1 = new ETransitionbuilder<State, Param, Object>().fireOnInput(oSta1Entry).addToContext(Hc).build();
    oC1ToOsta1 = new ETransitionbuilder<State, Param, Object>().fireOnInput(oSta1Entry).build();
    oR1Entry = new Param("oR1Entry" + v);
    oSta1ToOr1 = new ETransitionbuilder<State, Param, Object>().fireOnInput(oR1Entry).build();
    UIxClick = new Param("UIxClick" + v);
    Le = new ContextVar("Le" + v);
    oR1ToUix = new ETransitionbuilder<State, Param, Object>().fireOnInput(UIxClick).fireIfInContext(Le).build();
    HxEntry = new Param("HxEntry" + v);
    uiXToHx = new ETransitionbuilder<State, Param, Object>().fireOnInput(HxEntry).build();
    hXToOr1 = new ETransitionbuilder<State, Param, Object>().build();
    UIClick = new Param("UIClick" + v);
    oR1ToUi = new ETransitionbuilder<State, Param, Object>().fireOnInput(UIClick).fireIfInContext(Hc).build();
    HEntry = new Param("HEntry" + v);
    uiToH = new ETransitionbuilder<State, Param, Object>().fireOnInput(HEntry).build();
    oC2Entry = new Param("oC2Entry" + v);
    hToOc2 = new ETransitionbuilder<State, Param, Object>().fireOnInput(oC2Entry).build();
    oSta2Entry = new Param("oSta2Entry" + v);
    oC2ToOSta2 = new ETransitionbuilder<State, Param, Object>().fireOnInput(oSta2Entry).build();
    oR2Entry = new Param("oR2Entry" + v);
    oSta2ToOr2 = new ETransitionbuilder<State, Param, Object>().fireOnInput(oR2Entry).build();
    oSto1Entry = new Param("oSto1Entry" + v);
    oR2ToOSto1 = new ETransitionbuilder<State, Param, Object>().fireOnInput(oSto1Entry).addToContext(oR2).build();
    oD1Entry = new Param("oD1Entry" + v);
    oSto1ToOD1 = new ETransitionbuilder<State, Param, Object>().fireOnInput(oD1Entry).build();
    oSto1ToOR2 = new ETransitionbuilder<State, Param, Object>().fireIfInContext(oR2).addToContext(oSto1).removeFromContext(oR2).build();
    UIyClick = new Param("UIyClick" + v);
    oR2ToUIy = new ETransitionbuilder<State, Param, Object>().fireOnInput(UIyClick).addToContext(Le).build();
    HyEntry = new Param("HyEntry" + v);
    uiYToHy = new ETransitionbuilder<State, Param, Object>().fireOnInput(HyEntry).build();
    hyToOr2 = new ETransitionbuilder<State, Param, Object>().build();
    UIfClick = new Param("UIfClick" + v);
    oR2ToUIF = new ETransitionbuilder<State, Param, Object>().fireOnInput(UIfClick).build();
    HfEntry = new Param("HfEntry" + v);
    uiFToHf = new ETransitionbuilder<State, Param, Object>().fireOnInput(HfEntry).build();
    hfToOr2 = new ETransitionbuilder<State, Param, Object>().build();
    EvtBack = new Param("EvtBack" + v);
    oR2ToB2 = new ETransitionbuilder<State, Param, Object>().fireOnInput(EvtBack).build();
    oC1Entry = new Param("oC1Entry" + v);
    b2ToOC1 = new ETransitionbuilder<State, Param, Object>().fireOnInput(oC1Entry).fireIfInContext(oSto1).removeFromContext(oSto1, Hc).build();
    b2ToOr1 = new ETransitionbuilder<State, Param, Object>().fireOnInput(oR1Entry).fireIfNotInContext(oSto1).build();


    changeSomething();

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
        .withTransition(Hy, oR2, hyToOr2)
        .withTransition(oR2, UIf, oR2ToUIF)
        .withTransition(UIf, Hf, uiFToHf)
        .withTransition(Hf, oR2, hfToOr2)
        .withTransition(oR2, B2, oR2ToB2)
        .withTransition(B2, oC1, b2ToOC1)
        .withTransition(B2, oR1, b2ToOr1)
        .build();
  }

  protected void changeSomething() {

  }
}
