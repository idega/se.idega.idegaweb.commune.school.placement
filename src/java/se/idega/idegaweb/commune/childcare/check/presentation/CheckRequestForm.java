package se.idega.idegaweb.commune.childcare.check.presentation;

import com.idega.block.school.business.*;
import com.idega.block.school.data.*;
import com.idega.builder.data.*;
import com.idega.core.data.*;
import com.idega.presentation.*;
import com.idega.presentation.text.*;
import com.idega.presentation.ui.*;
import com.idega.user.Converter;
import com.idega.user.data.*;
import is.idega.idegaweb.member.business.*;
import java.rmi.*;
import java.util.*;
import se.idega.idegaweb.commune.childcare.check.business.*;
import se.idega.idegaweb.commune.presentation.*;

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
	private final static String PARAM_WORK_SITUATION = "chk_ws_";
	private final static String PARAM_WORK_SITUATION_1 = "chk_ws_1";
	private final static String PARAM_WORK_SITUATION_2 = "chk_ws_2";

	private boolean isError = false;
	private String errorMessage = null;
	private boolean paramErrorMotherTongueMC = false;
	private boolean paramErrorMotherTongueFC = false;
	private boolean paramErrorMotherTongueP = false;
	private boolean paramErrorChildCateType = false;
	private boolean paramErrorWorkSituation1 = false;
	private boolean paramErrorWorkSituation2 = false;

	private IBPage formResponsePage = null;
	private User child;

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
		if (iwc.isParameterSet(CitizenChildren.getChildIDParameterName())) {
			try {
				child = getCheckBusiness(iwc).getUserById(iwc, Integer.parseInt(iwc.getParameter(CitizenChildren.getChildIDParameterName())));
				return true;
			} catch (Exception e) {
				return false;
			}
		} else if (iwc.isParameterSet(CitizenChildren.getChildSSNParameterName())) {
			try {
				child = getCheckBusiness(iwc).getUserByPersonalId(iwc, iwc.getParameter(CitizenChildren.getChildSSNParameterName()));
				return true;
			} catch (Exception e) {
				return false;
			}
		} else {
			return false;
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
		add(getChildTable(iwc));
		add(new Break(2));

		if (this.isError) {
			add(getErrorText(this.errorMessage));
			add(new Break(2));
		}

		add(getForm(iwc));
	}

	private void formSubmitted(IWContext iwc) throws Exception {
		int paramChildCareType = -1;
		int paramWorkSituation1 = -1;
		int paramWorkSituation2 = -1;
		String paramMTMC = iwc.getParameter(PARAM_MOTHER_TONGUE_MOTHER_CHILD);
		String paramMTFC = iwc.getParameter(PARAM_MOTHER_TONGUE_FATHER_CHILD);
		String paramMTP = iwc.getParameter(PARAM_MOTHER_TONGUE_PARENTS);

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

		if (paramMTMC.trim().equals("")) {
			this.isError = true;
			this.paramErrorMotherTongueMC = true;
		}
		if (paramMTFC.trim().equals("")) {
			this.isError = true;
			this.paramErrorMotherTongueFC = true;
		}
		if (paramMTP.trim().equals("")) {
			this.isError = true;
			this.paramErrorMotherTongueP = true;
		}
		if (isError) {
			this.errorMessage = localize("check.incomplete_input", "You must fill in the information marked with red text.");
			viewForm(iwc);
			return;
		}

		try {
			getCheckBusiness(iwc).createCheck(paramChildCareType, paramWorkSituation1, paramWorkSituation2, paramMTMC, paramMTFC, paramMTP, ((Integer) child.getPrimaryKey()).intValue(), 1, 2800, 1200, Converter.convertToNewUser(iwc.getUser()), "", false, false, false, false, false);
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
		formTable.setWidth(600);
		formTable.setCellspacing(0);
		formTable.setCellpadding(14);
		formTable.setColor(getBackgroundColor());
		int row = 1;

		formTable.add(getLocalizedHeader("check.request_regarding", "The request regards"), 1, row);
		formTable.add(new Break(2), 1, row);
		formTable.add(getChildcareTypeTable(iwc), 1, row++);

		formTable.add(getLocalizedHeader("check.custodians", "Custodians"), 1, row);
		formTable.add(new Break(2), 1, row);
		formTable.add(getCustodianTable(iwc), 1, row++);

		formTable.add(getLocalizedHeader("check.mother_tongue", "Mother tongue"), 1, row);
		formTable.add(new Break(2), 1, row);
		formTable.add(getMotherTongueTable(iwc), 1, row++);

		SubmitButton submitButton = new SubmitButton(localize("check.send_request", "Send request"));
		submitButton.setAsImageButton(true);
		formTable.setAlignment(1, row, Table.HORIZONTAL_ALIGN_RIGHT);
		formTable.add(submitButton, 1, row);

		f.add(formTable);
		f.addParameter(PARAM_FORM_SUBMITTED, "true");
		return f;
	}

	private Table getChildTable(IWContext iwc) throws Exception {
		Table childTable = new Table(1, 1);
		childTable.setCellpaddingAndCellspacing(0);

		Table nameTable = new Table(2, 2);
		nameTable.setWidth(400);
		nameTable.setCellspacing(2);
		nameTable.setCellpadding(4);
		nameTable.setColor(1, 1, getBackgroundColor());
		nameTable.setColor(2, 1, getBackgroundColor());
		nameTable.add(getLocalizedSmallText("check.last_name", "Last name"), 1, 1);
		nameTable.add(getLocalizedSmallText("check.first_name", "First name"), 2, 1);
		nameTable.add(getText(child.getLastName()), 1, 2);
		nameTable.add(getText(child.getFirstName()), 2, 2);
		childTable.add(nameTable, 1, 1);

		Address address = getCheckBusiness(iwc).getUserAddress(iwc, child);
		if (address != null) {
			Table addressTable = new Table(2, 2);
			addressTable.setWidth(400);
			addressTable.setCellspacing(2);
			addressTable.setCellpadding(4);
			addressTable.setColor(1, 1, getBackgroundColor());
			addressTable.setColor(2, 1, getBackgroundColor());
			addressTable.add(getLocalizedSmallText("check.street", "Street address"), 1, 1);
			addressTable.add(getLocalizedSmallText("check.postnumber.city", "Postnumber and city"), 2, 1);
			addressTable.add(getText(address.getStreetName() + " " + address.getStreetNumber()), 1, 2);
			addressTable.add(getText(getCheckBusiness(iwc).getUserPostalCode(iwc, child) + " " + address.getStreetNumber()), 1, 2);

			childTable.setRows(2);
			childTable.add(addressTable, 1, 2);
		}

		return childTable;
	}

	private Table getChildcareTypeTable(IWContext iwc) throws RemoteException {
		Table childCareTypeTable = new Table();
		childCareTypeTable.setWidth("100%");
		childCareTypeTable.setCellspacing(0);
		childCareTypeTable.setCellpadding(4);

		SchoolTypeBusiness schoolTypeBusiness = (SchoolTypeBusiness) com.idega.business.IBOLookup.getServiceInstance(iwc, SchoolTypeBusiness.class);
		Collection childCareTypes = schoolTypeBusiness.findAllSchoolTypesForChildCare();

		DropdownMenu typeChoice = new DropdownMenu(PARAM_CHILD_CARE_TYPE);
		Iterator iter = childCareTypes.iterator();
		while (iter.hasNext()) {
			SchoolType st = (SchoolType) iter.next();
			typeChoice.addMenuElement(st.getPrimaryKey().toString(), localize(st.getLocalizationKey(), st.getName()));
		}
		childCareTypeTable.add(typeChoice, 1, 1);

		if (this.paramErrorChildCateType) {
			childCareTypeTable.add(getSmallErrorText(localize("check.child_care_type_error", "You must select child care type")), 2, 1);
		}

		return childCareTypeTable;
	}

	private Table getCustodianTable(IWContext iwc) throws Exception {
		Table custodianTable = new Table();
		custodianTable.setWidth("100%");
		custodianTable.setCellspacing(0);
		custodianTable.setCellpadding(3);

		Collection coll = getMemberFamilyLogic(iwc).getCustodiansFor(child);
		if (coll != null) {
			int row = 1;
			Iterator iter = coll.iterator();
			while (iter.hasNext()) {
				User parent = (User) iter.next();

				custodianTable.add(getLocalizedSmallText("check.last_and_first_name", "Last and first name"), 1, row);
				custodianTable.add(getLocalizedSmallText("check.phone_daytime", "Phone daytime"), 2, row);
				custodianTable.add(getLocalizedSmallText("check.civil_status", "Civil status"), 3, row);

				if ((row == 1 && this.paramErrorWorkSituation1) || (row == 2 && this.paramErrorWorkSituation2))
					custodianTable.add(getSmallErrorText(localize("check.social_status", "Social status")), 4, row++);
				else
					custodianTable.add(getLocalizedSmallText("check.social_status", "Social status"), 4, row++);

				custodianTable.add(getText(parent.getNameLastFirst()), 1, row);
				custodianTable.add(getText("08-633 54 67"), 2, row);
				custodianTable.add(getText("Gift"), 3, row);
				custodianTable.add(getWorkSituationMenu(iwc, PARAM_WORK_SITUATION + String.valueOf(row)), 4, row++);

				if (iter.hasNext()) {
					row++;
				}
			}
		} else {
			custodianTable.add(getErrorText(localize("child.no_custodians_found", "No custodians found")));
		}

		return custodianTable;
	}

	private Table getMotherTongueTable(IWContext iwc) {
		Table motherTongueTable = new Table(3, 2);
		motherTongueTable.setWidth("100%");
		motherTongueTable.setCellspacing(0);
		motherTongueTable.setCellpadding(4);

		String title = localize("check.mother_child", "Mother - child");
		if (this.paramErrorMotherTongueMC) {
			motherTongueTable.add(getSmallErrorText(title), 1, 1);
		} else {
			motherTongueTable.add(getSmallText(title), 1, 1);
		}

		title = localize("check.father_child", "Father - child");
		if (this.paramErrorMotherTongueFC) {
			motherTongueTable.add(getSmallErrorText(title), 2, 1);
		} else {
			motherTongueTable.add(getSmallText(title), 2, 1);
		}

		title = localize("check.parents", "Parents");
		if (this.paramErrorMotherTongueP) {
			motherTongueTable.add(getSmallErrorText(title), 3, 1);
		} else {
			motherTongueTable.add(getSmallText(title), 3, 1);
		}

		motherTongueTable.add(getMotherToungeInput(iwc, PARAM_MOTHER_TONGUE_MOTHER_CHILD), 1, 2);
		motherTongueTable.add(getMotherToungeInput(iwc, PARAM_MOTHER_TONGUE_FATHER_CHILD), 2, 2);
		motherTongueTable.add(getMotherToungeInput(iwc, PARAM_MOTHER_TONGUE_PARENTS), 3, 2);

		return motherTongueTable;
	}

	private TextInput getMotherToungeInput(IWContext iwc, String parameterName) {
		TextInput input = new TextInput(parameterName);
		String parameterValue = iwc.getParameter(parameterName);
		if (parameterValue != null) {
			input.setValue(parameterValue);
		}
		return input;
	}

	private DropdownMenu getWorkSituationMenu(IWContext iwc, String parameterName) {
		DropdownMenu workSituationChoice = new DropdownMenu(parameterName);
		workSituationChoice.addMenuElement(1, localize("check.working", "Working"));
		workSituationChoice.addMenuElement(2, localize("check.studying", "Studying"));
		workSituationChoice.addMenuElement(3, localize("check.seeking_work", "Seeking work"));

		String paramWorkSituation = iwc.getParameter(parameterName);
		if (paramWorkSituation != null) {
			workSituationChoice.setSelectedElement(paramWorkSituation);
		}
		return workSituationChoice;
	}

	private CheckBusiness getCheckBusiness(IWContext iwc) throws Exception {
		return (CheckBusiness) com.idega.business.IBOLookup.getServiceInstance(iwc, CheckBusiness.class);
	}

	private MemberFamilyLogic getMemberFamilyLogic(IWContext iwc) throws Exception {
		return (MemberFamilyLogic) com.idega.business.IBOLookup.getServiceInstance(iwc, MemberFamilyLogic.class);
	}
}