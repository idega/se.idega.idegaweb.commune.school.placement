package se.idega.idegaweb.commune.childcare.check.presentation;

import is.idega.block.family.business.FamilyLogic;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Iterator;

import se.idega.idegaweb.commune.care.business.CareBusiness;
import se.idega.idegaweb.commune.childcare.business.ChildCareSession;
import se.idega.idegaweb.commune.childcare.check.business.CheckBusiness;
import se.idega.idegaweb.commune.presentation.CitizenChildren;
import se.idega.idegaweb.commune.presentation.CommuneBlock;
import se.idega.idegaweb.commune.school.business.SchoolCommuneBusiness;

import com.idega.block.school.business.SchoolBusiness;
import com.idega.block.school.data.SchoolType;
import com.idega.core.location.data.Address;
import com.idega.core.location.data.PostalCode;
import com.idega.presentation.ExceptionWrapper;
import com.idega.presentation.IWContext;
import com.idega.presentation.Table;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.HiddenInput;
import com.idega.presentation.ui.RadioButton;
import com.idega.presentation.ui.SubmitButton;
import com.idega.presentation.ui.TextArea;
import com.idega.user.data.User;
import com.idega.util.PersonalIDFormatter;
import com.idega.util.text.Name;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author Anders Lindman
 * @version 1.0
 */

public class CheckRequestForm extends CommuneBlock {

	private final static int ACTION_VIEW_FORM = 1;
	private final static int ACTION_FORM_SUBMITTED = 2;

	private final static String PARAM_FORM_SUBMITTED = "chk_form_submit";
	private final static String PARAM_MOTHER_TONGUE_MOTHER_CHILD = "chk_mt_mc";
	private final static String PARAM_MOTHER_TONGUE_FATHER_CHILD = "chk_mt_fc";
	private final static String PARAM_MOTHER_TONGUE_PARENTS = "chk_mt_p";
	private final static String PARAM_CHILD_CARE_TYPE = "chk_cct";
	private final static String PARAM_WORK_SITUATION = "chk_ws_";
	private final static String PARAM_WORK_SITUATION_1 = "chk_ws_1";
	private final static String PARAM_WORK_SITUATION_2 = "chk_ws_2";
	private final static String PARAM_CHILDCARE_THIS = "ccs_childcare_this";
	private final static String PARAM_CHILDCARE_OTHER = "ccs_childcare_other";

	//variable for use as admin
	private boolean _useAsAdmin = false;
	private boolean _isFreeTimeType = true;
	
	private boolean isError = false;
	private boolean paramErrorChildCateType = false;
	private boolean paramErrorWorkSituation1 = false;
	private boolean paramErrorWorkSituation2 = false;

	private User child;
	
	private boolean _createChoices = false;

	public CheckRequestForm() {}

	public void main(IWContext iwc) {
		this.setResourceBundle(getResourceBundle(iwc));

		if (getUser(iwc)) {
			try {
				int action = parseAction(iwc);
				switch (action) {
					case ACTION_VIEW_FORM :
						viewForm(iwc);
						break;
					case ACTION_FORM_SUBMITTED :
						formSubmitted(iwc);
						break;
					default :
						break;
				}
			} catch (Exception e) {
				super.add(new ExceptionWrapper(e, this));
			}
		} else {
			super.add(this.getErrorText(localize("child.no_child_found", "No child selected")));
		}
	}

	private boolean getUser(IWContext iwc) {
		if (!createChoices()) {
			if (iwc.isParameterSet(CitizenChildren.getChildIDParameterName())) {
				try {
					child = getCheckBusiness(iwc).getUserById(Integer.parseInt(iwc.getParameter(CitizenChildren.getChildIDParameterName())));
					return true;
				} catch (Exception e) {
					return false;
				}
			} else if (iwc.isParameterSet(CitizenChildren.getChildSSNParameterName())) {
				try {
					child = getCheckBusiness(iwc).getUserByPersonalId(iwc.getParameter(CitizenChildren.getChildSSNParameterName()));
					return true;
				} catch (Exception e) {
					return false;
				}
			} 
			else {
				try {
					child = getCheckBusiness(iwc).getUserById(getChildCareSession(iwc).getChildID());
					if (child != null)
						return true;
					return false;
				}
				catch (Exception e) {
					return false;
				}
			}
		}
		else {
			try {
				child = getCheckBusiness(iwc).getUserById(getChildCareSession(iwc).getChildID());
				if (child != null)
					return true;
				return false;
			}
			catch (Exception e) {
				return false;
			}
		}
	}

	private int parseAction(IWContext iwc) {
		int action = ACTION_VIEW_FORM;

		if (iwc.isParameterSet(PARAM_FORM_SUBMITTED)) {
			action = ACTION_FORM_SUBMITTED;
		}

		return action;
	}

	private void viewForm(IWContext iwc) throws Exception {
		boolean hasCheck = getCareBusiness(iwc).hasGrantedCheck(child);
		if (hasCheck) {
			if (createChoices() && getResponsePage() != null)
				iwc.forwardToIBPage(getParentPage(), getResponsePage());
			else
				add(getLocalizedHeader("check.already_has_check","Child already has a check granted."));
		}
		else
			add(getForm(iwc));
	}

	private void formSubmitted(IWContext iwc) throws Exception {
		int paramChildCareType = -1;
		int paramWorkSituation1 = -1;
		int paramWorkSituation2 = -1;
		int checkFee = getCheckBusiness(iwc).getCheckFee();
		int checkAmount = getCheckBusiness(iwc).getCheckAmount();
		String paramMTMC = iwc.isParameterSet(PARAM_MOTHER_TONGUE_MOTHER_CHILD) ? iwc.getParameter(PARAM_MOTHER_TONGUE_MOTHER_CHILD) : "";
		String paramMTFC = iwc.isParameterSet(PARAM_MOTHER_TONGUE_FATHER_CHILD) ? iwc.getParameter(PARAM_MOTHER_TONGUE_FATHER_CHILD) : "";
		String paramMTP = iwc.isParameterSet(PARAM_MOTHER_TONGUE_PARENTS) ? iwc.getParameter(PARAM_MOTHER_TONGUE_PARENTS) : "";
		int checkID = getCheckBusiness(iwc).hasChildApprovedCheck(((Integer)child.getPrimaryKey()).intValue());
		boolean showErrors = true;
		if (createChoices()) {
			if (checkID != -1)
				showErrors = false;
		}
		
		try {
			paramChildCareType = Integer.parseInt(iwc.getParameter(PARAM_CHILD_CARE_TYPE));
		} catch (NumberFormatException ne) {
			this.isError = true;
			this.paramErrorChildCateType = true;
		}

		try {
			paramWorkSituation1 = Integer.parseInt(iwc.getParameter(PARAM_WORK_SITUATION_1));
		} catch (NumberFormatException ne) {
			this.isError = true;
			this.paramErrorWorkSituation1 = true;
		}

		try {
			paramWorkSituation2 = Integer.parseInt(iwc.getParameter(PARAM_WORK_SITUATION_2));
		} catch (NumberFormatException ne) {
			this.isError = true;
			this.paramErrorWorkSituation2 = true;
		}

		if (showErrors) {
			if (isError) {
				viewForm(iwc);
				return;
			}
		}

		try {
			if (showErrors){
				User user = null;
				if (_useAsAdmin){
					user = getCustodian(iwc);
					if (user == null)
						user = iwc.getCurrentUser();	
				}
				else{
					user = iwc.getCurrentUser();
				}
					
				checkID = getCheckBusiness(iwc).createCheck(paramChildCareType, paramWorkSituation1, paramWorkSituation2, paramMTMC, paramMTFC, paramMTP, ((Integer) child.getPrimaryKey()).intValue(), getCheckBusiness(iwc).getMethodUser(), checkAmount, checkFee, user, "", false, false, false, false, false);
			}
			
			if (createChoices()) {
				String childcareThisSchool = iwc.getParameter(PARAM_CHILDCARE_THIS);
				if (childcareThisSchool != null) {
					Name name = new Name(child.getFirstName(), child.getMiddleName(), child.getLastName());
					Object[] arguments = { name.getName(iwc.getApplicationSettings().getDefaultLocale(), true) };
					getSchoolCommuneBusiness(iwc).getSchoolChoiceBusiness().setChildcarePreferences(iwc.getCurrentUser(),((Integer)child.getPrimaryKey()).intValue(), Boolean.valueOf(childcareThisSchool).booleanValue(), iwc.getParameter(PARAM_CHILDCARE_OTHER), localize("check.student_childcare_other","Application for childcare outside of chosen school"), MessageFormat.format(localize("check.student_childcare_other_body","The following student has applied for childcare outside of the chosen school"), arguments));
				}
			}
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}

		if (getResponsePage() != null && !iwc.isInEditMode()) {
			iwc.forwardToIBPage(getParentPage(), getResponsePage());
		} else {
			add(getText("Submit OK"));
		}
	}

	private Form getForm(IWContext iwc) throws Exception {
		Form f = new Form();
		f.add(new HiddenInput(CitizenChildren.getChildIDParameterName(), ((Integer) child.getPrimaryKey()).toString()));

		Table formTable = new Table();
		formTable.setWidth(getWidth());
		formTable.setCellspacing(0);
		formTable.setCellpadding(0);
		int row = 1;
		boolean showCheckForm = true;
		if (createChoices()) {
			int checkID = getCheckBusiness(iwc).hasChildApprovedCheck(((Integer)child.getPrimaryKey()).intValue());
			if (checkID != -1)
				showCheckForm = false;	
		}

		formTable.add(getLocalizedHeader("check.application_for", "Application for"), 1, row++);
		formTable.add(getChildTable(iwc), 1, row++);
		formTable.setHeight(row++, 12);
	
		if (createChoices()) {
			formTable.add(getLocalizedHeader("check.child_care", "Child care options"), 1, row++);
			formTable.add(getChildCareTable(), 1, row++);
			formTable.setHeight(row++, 12);
		}

		if (showCheckForm) {
			if (!createChoices()) {
				formTable.add(getChildcareTypeTable(iwc), 1, row++);
				formTable.setHeight(row++, 12);
			}
			else
				formTable.add(new HiddenInput(PARAM_CHILD_CARE_TYPE, "3"), 1, row++);

			formTable.add(getLocalizedHeader("check.custodians", "Custodians"), 1, row++);
			formTable.add(getCustodianTable(iwc), 1, row++);
			formTable.setHeight(row++, 12);
		}
		
		SubmitButton submitButton = (SubmitButton) getButton(new SubmitButton(localize("check.send_request", "Send request")));
		formTable.add(submitButton, 1, row);

		f.add(formTable);
		f.addParameter(PARAM_FORM_SUBMITTED, "true");
		return f;
	}

	private Table getChildTable(IWContext iwc) throws Exception {
		Table childTable = new Table();
		childTable.setCellpadding(2);
		childTable.setCellspacing(0);
		childTable.setWidth(1, "170");
		int row = 1;

		childTable.add(getSmallHeader(localize("check.name", "Name") + ":"), 1, row);
		Name name = new Name(child.getFirstName(), child.getMiddleName(), child.getLastName());
		childTable.add(getSmallText(name.getName(iwc.getApplicationSettings().getDefaultLocale(), true)), 2, row++);

		childTable.add(getSmallHeader(localize("check.personal_id", "Personal ID") + ":"), 1, row);
		childTable.add(getSmallText(PersonalIDFormatter.format(child.getPersonalID(), iwc.getCurrentLocale())), 2, row++);

		Address address = getCheckBusiness(iwc).getUserAddress(child);
		PostalCode code = getCheckBusiness(iwc).getUserPostalCode(child);
		if (address != null) {
			childTable.add(getSmallHeader(localize("check.address", "Address") + ":"), 1, row);
			childTable.add(getSmallText(address.getStreetAddress()), 2, row);
			if ( code != null )
				childTable.add(getSmallText(", " + code.getPostalCode() + " " + code.getName()), 2, row);
		}
		row++;

		return childTable;
	}
	
	private Table getChildcareTypeTable(IWContext iwc) throws Exception {
		Table childCareTypeTable = new Table();
		childCareTypeTable.setCellpadding(2);
		childCareTypeTable.setCellspacing(0);
		childCareTypeTable.setWidth(1, "170");
		int row = 1;

		childCareTypeTable.add(getSmallHeader(localize("check.request_regarding", "The request regards") + ":"), 1, row);
		SchoolBusiness schBuiz = getSchoolCommuneBusiness(iwc).getSchoolBusiness();
		Collection childCareTypes= null;
		if (_isFreeTimeType){
			childCareTypes = schBuiz.findAllSchoolTypesInCategoryFreeTime(schBuiz.getChildCareSchoolCategory());			
		}else {
			childCareTypes = schBuiz.findAllSchoolTypesInCategory(schBuiz.getChildCareSchoolCategory(),false);	
		}
				
		
		DropdownMenu typeChoice = (DropdownMenu) getStyledInterface(new DropdownMenu(PARAM_CHILD_CARE_TYPE));
		Iterator iter = childCareTypes.iterator();
		
		while (iter.hasNext()) {
			SchoolType st = (SchoolType) iter.next();
			typeChoice.addMenuElement(st.getPrimaryKey().toString(), localize(getSchoolCommuneBusiness(iwc).getLocalizedSchoolTypeKey(st),st.getSchoolTypeName()));
		}
		childCareTypeTable.add(typeChoice, 2, row);

		if (this.paramErrorChildCateType) {
			childCareTypeTable.add(getSmallErrorText(localize("check.child_care_type_error", "You must select child care type")), 2, 1);
		}
		
		return childCareTypeTable;
	}

	private User getCustodian(IWContext iwc) throws Exception{
		Collection coll = getMemberFamilyLogic(iwc).getCustodiansFor(child);
		User parent = null;
		if (coll != null) {
			//int row = 1;
			//int parentNumber = 1;
			Iterator iter = coll.iterator();
			while (iter.hasNext()) {
				parent = (User) iter.next();
				if (parent.getGenderID() == 2){
					return parent;			
				}
			}			
		}
		return parent;
	}
	private Table getCustodianTable(IWContext iwc) throws Exception {
		Table custodianTable = new Table();
		custodianTable.setCellpadding(2);
		custodianTable.setCellspacing(0);
		custodianTable.setWidth(1, "170");

		Collection coll = getMemberFamilyLogic(iwc).getCustodiansFor(child);
		if (coll != null) {
			int row = 1;
			int parentNumber = 1;
			Iterator iter = coll.iterator();
			while (iter.hasNext()) {
				User parent = (User) iter.next();

				custodianTable.add(getLocalizedSmallHeader("check.name", "Last and first name"), 1, row);
				Name name = new Name(parent.getFirstName(), parent.getMiddleName(), parent.getLastName());
				custodianTable.add(getSmallText(name.getName(iwc.getApplicationSettings().getDefaultLocale(), true)), 2, row++);
				
				custodianTable.add(getLocalizedSmallHeader("check.civil_status", "Civil status"), 1, row);
				if ( getMemberFamilyLogic(iwc).hasPersonGotSpouse(parent) )
					custodianTable.add(getSmallText(localize("check.married", "Married")), 2, row++);
				else
					custodianTable.add(getSmallText(localize("check.un_married", "UnMarried")), 2, row++);

				if ((row == 1 && this.paramErrorWorkSituation1) || (row == 2 && this.paramErrorWorkSituation2))
					custodianTable.add(getSmallErrorText(localize("check.social_status", "Social status")), 1, row);
				else
					custodianTable.add(getLocalizedSmallHeader("check.social_status", "Social status"), 1, row);
				custodianTable.add(getWorkSituationMenu(iwc, PARAM_WORK_SITUATION + String.valueOf(parentNumber)), 2, row);
				Link infoLink = new Link(this.getInformationIcon(localize("check.work_situation_information", "Information about the work situation.")));
				infoLink.setToOpenAlert(localize("check.work_situation_message", "Information about the work situation..."));
				custodianTable.add(Text.getNonBrakingSpace(), 2, row);
				custodianTable.add(infoLink, 2, row++);
				
				if (iter.hasNext()) {
					custodianTable.setHeight(row++, 6);
					parentNumber++;
				}
			}
			if ( coll.size() == 1 ) {
				custodianTable.add(new HiddenInput(PARAM_WORK_SITUATION+"2","-1"),2,row-1);
			}
		} else {
			custodianTable.add(getErrorText(localize("child.no_custodians_found", "No custodians found")));
		}
		

		return custodianTable;
	}

	private DropdownMenu getWorkSituationMenu(IWContext iwc, String parameterName) {
		DropdownMenu workSituationChoice = (DropdownMenu) getStyledInterface(new DropdownMenu(parameterName));
		workSituationChoice.addMenuElement(1, localize("check.working", "Working"));
		workSituationChoice.addMenuElement(2, localize("check.studying", "Studying"));
		workSituationChoice.addMenuElement(3, localize("check.seeking_work", "Seeking work"));
		workSituationChoice.addMenuElement(4, localize("check.parental_leave", "Parental leave"));

		String paramWorkSituation = iwc.getParameter(parameterName);
		if (paramWorkSituation != null) {
			workSituationChoice.setSelectedElement(paramWorkSituation);
		}
		return (DropdownMenu) getStyledInterface(workSituationChoice);
	}

	private Table getChildCareTable() {
		Table childcareTable = new Table();
		childcareTable.setCellpadding(2);
		childcareTable.setCellspacing(0);
		childcareTable.setWidth(2, "400");
		int row = 1;
		
		RadioButton childcareThis = getRadioButton(PARAM_CHILDCARE_THIS, "true");
		childcareThis.setSelected();
		RadioButton childcareOther = getRadioButton(PARAM_CHILDCARE_THIS, "false");
		TextArea otherInput = (TextArea) getStyledInterface(new TextArea(PARAM_CHILDCARE_OTHER));
		otherInput.setWidth(Table.HUNDRED_PERCENT);
		otherInput.setRows(4);
		
		childcareTable.add(childcareThis,1, row);
		childcareTable.add(getSmallHeader(localize("check.childcare_this_school","Get childcare in chosen school")), 2, row++);

		childcareTable.add(childcareOther,1, row);
		childcareTable.add(getSmallHeader(localize("check.childcare_other","Get childcare elsewhere, specify") + ":"), 2, row++);
		childcareTable.add(otherInput, 2, row);
		
		return childcareTable;
	}

	private CareBusiness getCareBusiness(IWContext iwc) throws Exception {
		return (CareBusiness) com.idega.business.IBOLookup.getServiceInstance(iwc, CareBusiness.class);
	}
	
	private CheckBusiness getCheckBusiness(IWContext iwc) throws Exception {
		return (CheckBusiness) com.idega.business.IBOLookup.getServiceInstance(iwc, CheckBusiness.class);
	}

	private ChildCareSession getChildCareSession(IWContext iwc) throws Exception {
		return (ChildCareSession) com.idega.business.IBOLookup.getSessionInstance(iwc, ChildCareSession.class);
	}

	private FamilyLogic getMemberFamilyLogic(IWContext iwc) throws Exception {
		return (FamilyLogic) com.idega.business.IBOLookup.getServiceInstance(iwc, FamilyLogic.class);
	}

	private SchoolCommuneBusiness getSchoolCommuneBusiness(IWContext iwc) throws Exception {
		return (SchoolCommuneBusiness) com.idega.business.IBOLookup.getServiceInstance(iwc, SchoolCommuneBusiness.class);
	}
	
	/**
	 * Returns the createChoices.
	 * @return boolean
	 */
	public boolean createChoices() {
		return _createChoices;
	}

	/**
	 * Sets the createChoices.
	 * @param createChoices The createChoices to set
	 */
	public void setCreateChoices(boolean createChoices) {
		_createChoices = createChoices;
	}
	
	/**
	 * @param isUseAsAdmin
	 *          The isUseAsAdmin to set.
	 */
	public void setUseAsAdmin(boolean isUseAsAdmin) {
		this._useAsAdmin = isUseAsAdmin;
	}
	
	/**
	 * @param isFreeTimeType
	 *          The isFreeTimeType to set.
	 */
	public void setIsFreeTimeType(boolean isFreeTimeType) {
		this._isFreeTimeType = isFreeTimeType;
	}
}