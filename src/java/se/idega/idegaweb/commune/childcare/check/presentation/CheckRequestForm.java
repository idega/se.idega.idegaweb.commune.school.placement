package se.idega.idegaweb.commune.childcare.check.presentation;

import java.util.*;

import se.idega.idegaweb.commune.presentation.*;
import se.idega.idegaweb.commune.childcare.check.data.*;
import se.idega.idegaweb.commune.childcare.check.business.*;

import com.idega.idegaweb.*;
import com.idega.presentation.*;
import com.idega.presentation.text.*;
import com.idega.presentation.ui.*;
import com.idega.builder.data.IBPage;
import com.idega.block.school.data.*;
import com.idega.block.school.business.*;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Anders Lindman
 * @version 1.0
 */

public class CheckRequestForm extends CommuneBlock {

  private final static String IW_BUNDLE_IDENTIFIER = "se.idega.idegaweb.commune.childcare.check";

  private final static int ACTION_VIEW_FORM = 1;
  private final static int ACTION_FORM_SUBMITTED = 2;

  private final static String PARAM_VIEW_FORM = "chk_view_form";
  private final static String PARAM_FORM_SUBMITTED = "chk_form_submit";
  private final static String PARAM_MOTHER_TONGUE_MOTHER_CHILD = "chk_mt_mc";
  private final static String PARAM_MOTHER_TONGUE_FATHER_CHILD = "chk_mt_fc";
  private final static String PARAM_MOTHER_TONGUE_PARENTS = "chk_mt_p";
  private final static String PARAM_CHILD_CARE_TYPE = "chk_cct";
  private final static String PARAM_WORK_SITUATION_1 = "chk_ws_1";
  private final static String PARAM_WORK_SITUATION_2 = "chk_ws_2";

  private boolean isError = false;
  private String errorMessage = null;
  private boolean paramErrorMotherTongueMC = false;
  private boolean paramErrorMotherTongueFC = false;
  private boolean paramErrorMotherTongueP = false;

  private IBPage formResponsePage = null;

  public CheckRequestForm() {
  }

  public void main(IWContext iwc){
    this.setResourceBundle(getResourceBundle(iwc));

    try{
      int action = parseAction(iwc);
      switch(action){
        case ACTION_VIEW_FORM:
          viewForm(iwc);
          break;
        case ACTION_FORM_SUBMITTED:
          formSubmitted(iwc);
          break;
        default:
          break;
      }
    } catch (Exception e) {
      super.add(new ExceptionWrapper(e,this));
    }
  }

  private int parseAction(IWContext iwc){
    int action = ACTION_VIEW_FORM;

    if(iwc.isParameterSet(PARAM_FORM_SUBMITTED)){
      action = ACTION_FORM_SUBMITTED;
    }

    return action;
  }

  private void viewForm(IWContext iwc)throws Exception{
    Table nameTable = new Table(2,2);
    nameTable.setWidth(400);
    nameTable.setCellspacing(2);
    nameTable.setCellpadding(4);
    nameTable.setColor(1,1,getBackgroundColor());
    nameTable.setColor(2,1,getBackgroundColor());
    nameTable.add(getLocalizedSmallText("check.last_name","Last name"),1,1);
    nameTable.add(getLocalizedSmallText("check.first_name","First name"),2,1);
    nameTable.add(getText("Mickelin"),1,2);
    nameTable.add(getText("Henrik"),2,2);
    add(nameTable);

    Table addressTable = new Table(2,2);
    addressTable.setWidth(400);
    addressTable.setCellspacing(2);
    addressTable.setCellpadding(4);
    addressTable.setColor(1,1,getBackgroundColor());
    addressTable.setColor(2,1,getBackgroundColor());
    addressTable.add(getLocalizedSmallText("check.street","Street address"),1,1);
    addressTable.add(getLocalizedSmallText("check.postnumber.city","Postnumber and city"),2,1);
    addressTable.add(getText("Odenvägen 2C"),1,2);
    addressTable.add(getText("133 38 SALTSJÖBADEN"),2,2);
    add(addressTable);

    add(new Break(2));

    if(this.isError){
      add(getErrorText(this.errorMessage));
      add(new Break(2));
    }

    Form f = new Form();
    Table formTable = new Table(1,1);
    formTable.setWidth(600);
    formTable.setCellspacing(0);
    formTable.setCellpadding(14);
    formTable.setColor(getBackgroundColor());

    formTable.add(getLocalizedHeader("check.request_regarding","The request regards"));
    formTable.add(new Break(2));
    Table childCareTypeTable = new Table(1,2);
    childCareTypeTable.setWidth("100%");
    childCareTypeTable.setCellspacing(0);
    childCareTypeTable.setCellpadding(4);

    SchoolTypeBusiness schoolTypeBusiness = (SchoolTypeBusiness)com.idega.business.IBOLookup.getServiceInstance(iwc,SchoolTypeBusiness.class);
    Collection childCareTypes = schoolTypeBusiness.findAllSchoolTypesInCategory(SchoolType.CHILDCARE);

    DropdownMenu typeChoice = new DropdownMenu(PARAM_CHILD_CARE_TYPE);
    Iterator iter = childCareTypes.iterator();
    while(iter.hasNext()){
      SchoolType st = (SchoolType)iter.next();
      typeChoice.addMenuElement(st.getPrimaryKey().toString(),localize(st.getLocalizationKey(),st.getName()));
    }
    childCareTypeTable.add(typeChoice,1,1);
    formTable.add(childCareTypeTable);
    formTable.add(new Break());

    formTable.add(getLocalizedHeader("check.custodians","Custodians"));
    formTable.add(new Break(2));
    Table custodianTable = new Table(4,5);
    custodianTable.setWidth("100%");
    custodianTable.setCellspacing(0);
    custodianTable.setCellpadding(3);
    custodianTable.add(getLocalizedSmallText("check.last_and_first_name","Last and first name"),1,1);
    custodianTable.add(getLocalizedSmallText("check.phone_daytime","Phone daytime"),2,1);
    custodianTable.add(getLocalizedSmallText("check.civil_status","Civil status"),3,1);
    custodianTable.add(getText("Mickelin Maria Cecilia"),1,2);
    custodianTable.add(getText("08-633 54 67"),2,2);
    custodianTable.add(getText("Gift"),3,2);
    DropdownMenu workSituationChoice = new DropdownMenu(PARAM_WORK_SITUATION_1);
    workSituationChoice.addMenuElement(1,"Arbetar");
    workSituationChoice.addMenuElement(2,"Studerar");
    workSituationChoice.addMenuElement(3,"Arbetssökande");
    String paramWorkSituation1 = iwc.getParameter(PARAM_WORK_SITUATION_1);
    if(paramWorkSituation1!=null){
      workSituationChoice.setSelectedElement(paramWorkSituation1);
    }
    custodianTable.add(workSituationChoice,4,2);

    custodianTable.add(getLocalizedSmallText("check.last_and_first_name","Last and first name"),1,4);
    custodianTable.add(getLocalizedSmallText("check.phone_daytime","Phone daytime"),2,4);
    custodianTable.add(getLocalizedSmallText("check.civil_status","Civil status"),3,4);
    custodianTable.add(getText("Mickelin Harry"),1,5);
    custodianTable.add(getText("0709-432133"),2,5);
    custodianTable.add(getText("Gift"),3,5);
    workSituationChoice = new DropdownMenu(PARAM_WORK_SITUATION_2);
    workSituationChoice.addMenuElement(1,"Arbetar");
    workSituationChoice.addMenuElement(2,"Studerar");
    workSituationChoice.addMenuElement(3,"Arbetssökande");
    String paramWorkSituation2 = iwc.getParameter(PARAM_WORK_SITUATION_2);
    if(paramWorkSituation2!=null){
      workSituationChoice.setSelectedElement(paramWorkSituation2);
    }
    custodianTable.add(workSituationChoice,4,5);
    formTable.add(custodianTable);
    formTable.add(new Break(2));

    formTable.add(getLocalizedHeader("check.mother_tongue","Mother tongue"));
    formTable.add(new Break(2));
    Table motherTongueTable = new Table(3,2);
    motherTongueTable.setWidth("100%");
    motherTongueTable.setCellspacing(0);
    motherTongueTable.setCellpadding(4);
    String title = localize("check.mother_child","Mother - child");
    if(this.paramErrorMotherTongueMC){
      motherTongueTable.add(getSmallErrorText(title),1,1);
    }else{
      motherTongueTable.add(getSmallText(title),1,1);
    }
    title = localize("check.father_child","Father - child");
    if(this.paramErrorMotherTongueFC){
      motherTongueTable.add(getSmallErrorText(title),2,1);
    }else{
      motherTongueTable.add(getSmallText(title),2,1);
    }
    title = localize("check.parents","Parents");
    if(this.paramErrorMotherTongueP){
      motherTongueTable.add(getSmallErrorText(title),3,1);
    }else{
      motherTongueTable.add(getSmallText(title),3,1);
    }
    TextInput mtmc = new TextInput(PARAM_MOTHER_TONGUE_MOTHER_CHILD);
    String paramMotherTongueMC = iwc.getParameter(PARAM_MOTHER_TONGUE_MOTHER_CHILD);
    if(paramMotherTongueMC!=null){
      mtmc.setValue(paramMotherTongueMC);
    }
    motherTongueTable.add(mtmc,1,2);
    TextInput mtfc = new TextInput(PARAM_MOTHER_TONGUE_FATHER_CHILD);
    String paramMotherTongueFC = iwc.getParameter(PARAM_MOTHER_TONGUE_FATHER_CHILD);
    if(paramMotherTongueFC!=null){
      mtfc.setValue(paramMotherTongueFC);
    }
    motherTongueTable.add(mtfc,2,2);
    TextInput mtp = new TextInput(PARAM_MOTHER_TONGUE_PARENTS);
    String paramMotherTongueP = iwc.getParameter(PARAM_MOTHER_TONGUE_PARENTS);
    if(paramMotherTongueP!=null){
      mtp.setValue(paramMotherTongueP);
    }
    motherTongueTable.add(mtp,3,2);
    formTable.add(motherTongueTable);
    formTable.add(new Break(2));

    Table submitTable = new Table(1,1);
    submitTable.setWidth("100%");
    submitTable.setAlignment(1,1,"right");
    Link submitButton = getLocalizedLink("check.send_request","Send request");
    submitButton.setAsImageButton(true);
    submitButton.setToFormSubmit(f);
    submitTable.add(submitButton,1,1);
    formTable.add(submitTable);
    formTable.add(new Break());

    f.add(formTable);
    f.addParameter(PARAM_FORM_SUBMITTED,"true");
    add(f);
  }

  private void formSubmitted(IWContext iwc)throws Exception{
    int paramWorkSituation1 = Integer.parseInt(iwc.getParameter(PARAM_WORK_SITUATION_1));
    int paramWorkSituation2 = Integer.parseInt(iwc.getParameter(PARAM_WORK_SITUATION_2));
    String paramMTMC = iwc.getParameter(PARAM_MOTHER_TONGUE_MOTHER_CHILD);
    String paramMTFC = iwc.getParameter(PARAM_MOTHER_TONGUE_FATHER_CHILD);
    String paramMTP = iwc.getParameter(PARAM_MOTHER_TONGUE_PARENTS);
    int paramChildCareType = Integer.parseInt(iwc.getParameter(PARAM_CHILD_CARE_TYPE));

    if(paramMTMC.trim().equals("")){
      this.isError = true;
      this.paramErrorMotherTongueMC = true;
    }
    if(paramMTFC.trim().equals("")){
      this.isError = true;
      this.paramErrorMotherTongueFC = true;
    }
    if(paramMTP.trim().equals("")){
      this.isError = true;
      this.paramErrorMotherTongueP = true;
    }
    if(isError){
      this.errorMessage = localize("check.incomplete_input","You must fill in the information marked with red text.");
      viewForm(iwc);
      return;
    }
    getCheckBusiness(iwc).createCheck(
        paramChildCareType,
        paramWorkSituation1,
        paramWorkSituation2,
        paramMTMC,
        paramMTFC,
        paramMTP,
        123, //Child id
        1, //Method
        2800, //Amount
        1200); // Check fee

    if(this.formResponsePage!=null){
      iwc.forwardToIBPage(getParentPage(), this.formResponsePage);
    }else{
      add(getText("Submit OK"));
    }
  }

  private CheckBusiness getCheckBusiness(IWContext iwc)throws Exception{
    return (CheckBusiness)com.idega.business.IBOLookup.getServiceInstance(iwc,CheckBusiness.class);
  }

  public IBPage getFormResponsePage(){
    return this.formResponsePage;
  }

  public void setFormResponsePage(IBPage page){
    this.formResponsePage = page;
  }
}
