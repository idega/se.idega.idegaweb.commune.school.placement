/*
 */
package se.idega.idegaweb.commune.childcare.presentation;

import java.rmi.RemoteException;
import java.sql.Date;
import java.util.Collection;
import java.util.Iterator;

import javax.ejb.FinderException;

import se.idega.idegaweb.commune.accounting.childcare.data.ChildCareApplication;
import se.idega.idegaweb.commune.block.importer.business.AlreadyCreatedException;
import se.idega.idegaweb.commune.business.CommuneUserBusiness;
import se.idega.idegaweb.commune.childcare.check.business.CheckBusiness;
import se.idega.idegaweb.commune.childcare.event.ChildCareEventListener;
import se.idega.idegaweb.commune.school.presentation.CentralPlacementEditor;
import se.idega.idegaweb.commune.school.presentation.CentralPlacementProviderEditor;
import se.idega.idegaweb.commune.school.presentation.CentralPlacementSchoolGroupEditor;

import com.idega.block.navigation.presentation.UserHomeLink;
import com.idega.block.school.business.SchoolBusiness;
import com.idega.business.IBOLookup;
import com.idega.core.location.data.Address;
import com.idega.presentation.IWContext;
import com.idega.presentation.Table;
import com.idega.presentation.text.Break;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.BooleanInput;
import com.idega.presentation.ui.DateInput;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.GenericButton;
import com.idega.presentation.ui.RadioButton;
import com.idega.presentation.ui.SubmitButton;
import com.idega.presentation.ui.TextArea;
import com.idega.presentation.ui.TextInput;
import com.idega.presentation.ui.util.SelectorUtility;
import com.idega.user.data.User;
import com.idega.util.IWTimestamp;
import com.idega.util.PersonalIDFormatter;
import com.idega.util.text.Name;

/**
 * @author palli
 * 
 * A class for the presenation layer for creating contracts centrally.
 */
public class ChildCareAdminContracts extends ChildCareBlock {

	private User child = null;

	private ChildCareApplication application = null;

	private final static String PARAM_COMMENT = "prm_comment";

	private final static String PARAM_GETBILL = "prm_getbill";

	private final static String PARAM_OPERATION = "prm_operation";

	private final static String PARAM_GROUP = "prm_group";

	private final static String PARAM_HOURS = "prm_hours";

	private final static String PARAM_EMPLOYMENT = "prm_unemployed";

	private final static String PARAM_PLACEMENT_DATE = "prm_plac_date";

	private final static String PARAM_TERMINATION_DATE = "prm_term_date";

	private final static String PARAM_PRE_SCHOOL = "prm_pre_school";

	private final static String PARAM_LAST_REPLY_DATE = "prm_reply_date";

	private final static String PARAM_EXTRA_CONTRACT = "prm_extra_contract";

	private final static String PARAM_EXTRA_CONTRACT_OTHER = "prm_extra_contract_other";

	private final static String PARAM_EXTRA_CONTRACT_MESSAGE = "prm_extra_contract_message";

	private final static String PARAM_EXTRA_CONTRACT_OTHER_MESSAGE = "prm_extra_contract_other_message";

	private final static String LABEL_COMMENT = "child_care.comment";

	private final static String LABEL_CHILD = "child_care.child";

	private final static String LABEL_USER_NAME = "child_care.name";

	private final static String LABEL_PERSONAL_ID = "child_care.personal_id";

	private final static String LABEL_ADDRESS = "child_care.address";

	private final static String LABEL_GRANTED_CHECK = "child_care.granted_check";

	private final static String LABEL_GETS_BILL = "child_care.gets_bill";

	private final static String LABEL_PROVIDER = "child_care.provider";

	private final static String LABEL_PROVIDER_NAME = "child_care.provider_name";

	private final static String LABEL_PROVIDER_OPERATION = "child_care.provider_operation";

	private final static String LABEL_PROVIDER_GROUP = "child_care.provider_group";

	private final static String LABEL_HOURS = "child_care.hours_pr_week";

	private final static String LABEL_EMPLOYMENT = "child_care.no_job";

	private final static String LABEL_PLACEMENT_DATE = "child_care.placement_date";

	private final static String LABEL_TERMINATION_DATE = "child_care.termination_date";

	private final static String LABEL_LAST_REPLY_DATE = "child_care.last_reply_date";

	private final static String LABEL_EXTRA_CONTRACT = "child_care.extra_contract";

	private final static String LABEL_EXTRA_CONTRACT_OTHER = "child_care.extra_contract_other";

	private final static String LABEL_EXTRA_CONTRACT_MESSAGE = "child_care.extra_contract_message";

	private final static String LABEL_EXTRA_CONTRACT_OTHER_MESSAGE = "child_care.extra_contract_other_message";

	private final static int ACTION_VIEW_FORM = 1;

	private final static int ACTION_SAVE = 2;

	private boolean isUpdate = false;

	private boolean finalize = false;
	
	private Date earliestPossiblePlacementDate;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.idega.presentation.PresentationObject#main(com.idega.presentation.IWContext)
	 */
	public void init(IWContext iwc) throws Exception {
		int action = process(iwc);

		if (child != null) {
			if (getBusiness().hasActiveApplication(((Integer) child.getPrimaryKey()).intValue())) {
				add(this.getLocalizedHeader("child_care.child_has_active_placement", "Child has active placement."));
			}
			else {
				if (getBusiness().hasTerminationInFuture(((Integer) child.getPrimaryKey()).intValue())) {
					earliestPossiblePlacementDate = getBusiness().getEarliestPossiblePlacementDate(((Integer) child.getPrimaryKey()).intValue());
					if (earliestPossiblePlacementDate != null) {
						add(getSmallErrorText(localize("child_care.earliest_possible_placement_date", "Earliest possible placement date") + ": " + new IWTimestamp(earliestPossiblePlacementDate).getLocaleDate(iwc.getCurrentLocale(), IWTimestamp.SHORT)));
						add(new Break(2));
					}
				}
				switch (action) {
					case ACTION_VIEW_FORM:
						showForm(iwc);
						break;
					case ACTION_SAVE:
						save(iwc);
						break;
				}
			}
		}
		else {
			add(this.getLocalizedHeader("child_care.no_child_or_application_found", "No child or application found."));
		}
	}

	private void save(IWContext iwc) {
		int ownerID = Integer.parseInt(iwc.getParameter(PARAM_GETBILL));
		int schoolTypeID = -1;
		try {
			schoolTypeID = Integer.parseInt(iwc.getParameter(PARAM_OPERATION));
		}
		catch (NumberFormatException nfe) {
			schoolTypeID = -1;
		}

		int careTime = -1;
		try {
			careTime = Integer.parseInt(iwc.getParameter(PARAM_HOURS));
		}
		catch (NumberFormatException nfe) {
			careTime = -1;
		}

		int employmentTypeID = -1;
		try {
			employmentTypeID = Integer.parseInt(iwc.getParameter(PARAM_EMPLOYMENT));
		}
		catch (NumberFormatException nfe) {
			employmentTypeID = -1;
		}

		IWTimestamp placementDate = new IWTimestamp(iwc.getParameter(PARAM_PLACEMENT_DATE));
		IWTimestamp terminationDate = null;
		if (iwc.isParameterSet(PARAM_TERMINATION_DATE)) {
			terminationDate = new IWTimestamp(iwc.getParameter(PARAM_TERMINATION_DATE));
		}
		IWTimestamp replyDate = null;
		if (iwc.isParameterSet(PARAM_LAST_REPLY_DATE)) replyDate = new IWTimestamp(iwc.getParameter(PARAM_LAST_REPLY_DATE));
		String comment = iwc.getParameter(PARAM_COMMENT);
		String preSchool = iwc.getParameter(PARAM_PRE_SCHOOL);
		String extraContractMessage = iwc.getParameter(PARAM_EXTRA_CONTRACT_MESSAGE);
		String extraContractMessageOther = iwc.getParameter(PARAM_EXTRA_CONTRACT_OTHER_MESSAGE);
		boolean extraContract = BooleanInput.getBooleanReturnValue(iwc.getParameter(PARAM_EXTRA_CONTRACT));
		boolean extraContractOther = BooleanInput.getBooleanReturnValue(iwc.getParameter(PARAM_EXTRA_CONTRACT_OTHER));
		int groupID = -1;
		try {
			groupID = Integer.parseInt(iwc.getParameter(PARAM_GROUP));
		}
		catch (NumberFormatException npe) {
			groupID = -1;
		}
		boolean success;
		String errorMessage = null;
		try {
			User owner = getBusiness().getUserBusiness().getUser(ownerID);
			success = getBusiness().importChildToProvider(getSession().getApplicationID(), getSession().getChildID(), getSession().getChildCareID(), groupID, careTime, employmentTypeID, schoolTypeID, comment, placementDate, terminationDate, iwc.getCurrentLocale(), owner, iwc.getCurrentUser(), true, replyDate, preSchool, extraContract, extraContractMessage, extraContractOther, extraContractMessageOther);
		}
		catch (RemoteException re) {
			success = false;
			errorMessage = localize("child_care.submit_failed", "Submit failed.");
		}
		catch (AlreadyCreatedException ace) {
			success = false;
			errorMessage = localize("child_care.contract_already_exists", "Active contract already exists for this child.");
		}

		if (success) {
			try {
				getSession().setChildID(-1);
				getSession().setChildCareID(-1);
				getSession().setApplicationID(-1);
			}
			catch (RemoteException re) {
				log(re);
			}

			if (getResponsePage() != null) {
				iwc.forwardToIBPage(getParentPage(), getResponsePage());
			}
			else {
				add(getSmallHeader(localize("child_care.submit_successful", "Submit successful.")));
				add(new Break(2));
				add(new UserHomeLink());
			}
		}
		else {
			add(getSmallErrorText(errorMessage));
			add(new Break(2));
			showForm(iwc);
		}
	}

	public int process(IWContext iwc) {
		try {
			if (getSession().getApplicationID() != -1) {
				application = getBusiness().getApplication(getSession().getApplicationID());
				child = application.getChild();
				isUpdate = true;
				if (application.getApplicationStatus() == getBusiness().getStatusContract()) finalize = true;
			}
			else if (getSession().getChildID() != -1) {
				child = getBusiness().getUserBusiness().getUser(getSession().getChildID());
			}
		}
		catch (RemoteException re) {
			child = null;
			log(re);
		}

		if (iwc.isParameterSet(PARAM_SUBMIT)) {
			return ACTION_SAVE;
		}
		else {
			return ACTION_VIEW_FORM;
		}
	}

	public void showForm(IWContext iwc) {
		Form form = new Form();
		form.setEventListener(ChildCareEventListener.class);
		form.setName(CentralPlacementEditor.FORM_NAME);

		Table table = new Table();
		table.setCellpadding(2);
		table.setCellspacing(0);
		int row = 1;

		table.add(getLocalizedHeader(LABEL_CHILD, "Child"), 1, row++);
		table.add(getLocalizedLabel(LABEL_USER_NAME, "Name"), 1, row);
		Name name = new Name(child.getFirstName(), child.getMiddleName(), child.getLastName());
		table.add(getSmallText(name.getName(iwc.getApplicationSettings().getDefaultLocale(), true)), 3, row++);
		table.add(getLocalizedLabel(LABEL_PERSONAL_ID, "Personal ID"), 1, row);
		table.add(getSmallText(PersonalIDFormatter.format(child.getPersonalID(), iwc.getCurrentLocale())), 3, row++);
		table.add(getLocalizedLabel(LABEL_ADDRESS, "Address"), 1, row);
		try {
			Address address = getUserService(iwc).getUsersMainAddress(child);
			if (address != null) {
				String postalAddress = address.getPostalAddress();
				table.add(getSmallText(address.getStreetAddress()), 3, row);
				if (postalAddress != null) table.add(getSmallText(", " + postalAddress), 3, row);
			}
		}
		catch (RemoteException e) {
			e.printStackTrace();
		}
		row++;

		boolean hasCheck = false;
		try {
			if (child != null) hasCheck = getCheckBusiness(iwc).hasGrantedCheck(child);
		}
		catch (RemoteException e) {
			e.printStackTrace();
		}

		table.add(getLocalizedLabel(LABEL_GRANTED_CHECK, "Granted check"), 1, row);
		if (!hasCheck)
			table.add(getSmallText(localize("child_care.no", "No")), 3, row++);
		else
			table.add(getSmallText(localize("child_care.yes", "Yes")), 3, row++);
		table.setHeight(row++, 12);

		Collection parents;
		try {
			parents = getBusiness().getUserBusiness().getParentsForChild(child);
		}
		catch (RemoteException e2) {
			parents = null;
		}

		if (parents != null) {
			table.add(getLocalizedHeader("child_care.custodians", "Custodians"), 1, row++);

			Iterator iter = parents.iterator();
			while (iter.hasNext()) {
				User parent = (User) iter.next();

				table.add(getLocalizedLabel(LABEL_USER_NAME, "Name"), 1, row);
				name = new Name(parent.getFirstName(), parent.getMiddleName(), parent.getLastName());
				table.add(getSmallText(name.getName(iwc.getApplicationSettings().getDefaultLocale(), true)), 3, row++);
				table.add(getLocalizedLabel(LABEL_PERSONAL_ID, "Personal ID"), 1, row);
				table.add(getSmallText(PersonalIDFormatter.format(parent.getPersonalID(), iwc.getCurrentLocale())), 3, row++);
				table.add(getLocalizedLabel(LABEL_GETS_BILL, "Gets bill"), 1, row);
				RadioButton getBill = this.getRadioButton(PARAM_GETBILL, parent.getPrimaryKey().toString());
				getBill.keepStatusOnAction(true);
				getBill.setMustBeSelected(localize("child_care.must_select_billed_to", "You must select who to send the bill to."));
				if (application != null) {
					if (application.getOwner().equals(parent)) {
						getBill.setSelected(true);
					}
				}
				table.add(getBill, 3, row++);

				row++;
				if (iter.hasNext()) table.setHeight(row++, 6);
			}
		}
		table.setHeight(row++, 12);

		table.add(getLocalizedHeader(LABEL_PROVIDER, "Provider"), 1, row++);
		table.add(getLocalizedLabel(LABEL_PROVIDER_NAME, "Name"), 1, row);
		try {
			Collection schools = getSchoolBusiness(iwc).getSchoolHome().findAllByCategory(getSchoolBusiness(iwc).getCategoryChildcare());
			SelectorUtility sel = new SelectorUtility();
			DropdownMenu prov = (DropdownMenu) sel.getSelectorFromIDOEntities(new DropdownMenu(getSession().getParameterChildCareID()), schools, "getName");
			prov.addMenuElementFirst("-1", "");
			prov.setToSubmit(true);
			prov.setAsNotEmpty(localize("child_care.must_select_provider", "You must select a provider."));
			if (application != null) prov.setSelectedElement(application.getProviderId());
			if (getSession().getChildCareID() != -1) prov.setSelectedElement(getSession().getChildCareID());

			table.add(getStyledInterface(prov), 3, row);

			GenericButton createProvider = getButton(new GenericButton("", localize("child_care.create_provider", "Create provider")));
			createProvider.setWindowToOpen(CentralPlacementProviderEditor.class);
			table.add(Text.getNonBrakingSpace(), 3, row);
			table.add(createProvider, 3, row);
			table.setNoWrap(3, row);
			row++;
		}
		catch (RemoteException e1) {
			e1.printStackTrace();
		}
		catch (FinderException e1) {
			e1.printStackTrace();
		}

		table.add(getLocalizedLabel(LABEL_PROVIDER_OPERATION, "Operation"), 1, row);
		try {
			Collection school_types = getSchoolBusiness(iwc).getSchoolTypeHome().findAllByCategory(getSchoolBusiness(iwc).getCategoryChildcare().getCategory());
			SelectorUtility sel = new SelectorUtility();
			DropdownMenu op = (DropdownMenu) sel.getSelectorFromIDOEntities(new DropdownMenu(PARAM_OPERATION), school_types, "getName");
			op.keepStatusOnAction(true);
			table.add(getStyledInterface(op), 3, row++);
		}
		catch (RemoteException e1) {
			e1.printStackTrace();
		}
		catch (FinderException e1) {
			e1.printStackTrace();
		}

		table.add(getLocalizedLabel(LABEL_PROVIDER_GROUP, "Group"), 1, row);
		try {
			Collection school_classes = getSchoolBusiness(iwc).getSchoolClassHome().findBySchoolAndCategory(getSession().getChildCareID(), getSchoolBusiness(iwc).getCategoryChildcare().getCategory());
			SelectorUtility sel = new SelectorUtility();
			DropdownMenu op = (DropdownMenu) sel.getSelectorFromIDOEntities(new DropdownMenu(PARAM_GROUP), school_classes, "getName");
			if (!op.isEmpty()) {
				if (!isUpdate || finalize) {
					op.setAsNotEmpty(localize("child_care.must_select_group", "You must select a group."), "-1");
				}
				op.addMenuElementFirst("-1", "");
				op.keepStatusOnAction(true);
			}
			table.add(getStyledInterface(op), 3, row);

			GenericButton createGroup = getButton(new GenericButton("", localize("child_care.create_group", "Create group")));
			createGroup.setWindowToOpen(CentralPlacementSchoolGroupEditor.class);
			table.add(Text.getNonBrakingSpace(), 3, row);
			table.add(createGroup, 3, row);
			row++;
		}
		catch (RemoteException e1) {
			e1.printStackTrace();
		}
		catch (FinderException e1) {
			e1.printStackTrace();
		}

		table.add(getSmallHeader(localize("child_care.pre_school", "Specify pre-school:")), 1, row);
		TextInput preSchool = (TextInput) getStyledInterface(new TextInput(PARAM_PRE_SCHOOL));
		preSchool.setLength(40);
		if (application != null && application.getPreSchool() != null) preSchool.setContent(application.getPreSchool());
		table.add(preSchool, 3, row++);
		table.setHeight(row++, 12);

		TextInput hoursWeek = (TextInput) getStyledInterface(new TextInput(PARAM_HOURS));
		hoursWeek.keepStatusOnAction(true);
		hoursWeek.setLength(2);
		if (!isUpdate || finalize) {
			hoursWeek.setAsNotEmpty(localize("child_care.child_care_time_required", "You must fill in the child care time."));
			hoursWeek.setAsIntegers(localize("child_care.only_integers_allowed", "Not a valid child care time."));
		}
		table.add(getLocalizedLabel(LABEL_HOURS, "Hours pr. week"), 1, row);
		table.add(hoursWeek, 3, row++);

		try {
			DropdownMenu employment = this.getEmploymentTypes(PARAM_EMPLOYMENT, -1);
			employment.keepStatusOnAction(true);
			if (!isUpdate || finalize) {
				employment.setAsNotEmpty(localize("child_care.must_select_employment_type", "You must select employment type."), "-1");
			}
			table.add(getLocalizedLabel(LABEL_EMPLOYMENT, "Employment"), 1, row);
			table.add(employment, 3, row++);
		}
		catch (RemoteException e1) {
			e1.printStackTrace();
		}

		table.setHeight(row++, 12);

		BooleanInput hasExtraContract = (BooleanInput) getStyledInterface(new BooleanInput(PARAM_EXTRA_CONTRACT));
		if (application != null) hasExtraContract.setSelected(application.getHasExtraContract());
		TextInput extraContractMessage = (TextInput) getStyledInterface(new TextInput(PARAM_EXTRA_CONTRACT_MESSAGE));
		if (application != null && application.getExtraContractMessage() != null) extraContractMessage.setContent(application.getExtraContractMessage());
		table.add(getSmallHeader(localize(LABEL_EXTRA_CONTRACT, "Extra contract")), 1, row);
		table.add(hasExtraContract, 3, row);
		table.add(new Text(Text.NON_BREAKING_SPACE + Text.NON_BREAKING_SPACE + Text.NON_BREAKING_SPACE), 3, row);
		table.add(getSmallHeader(localize(LABEL_EXTRA_CONTRACT_MESSAGE, "Message")), 3, row);
		table.add(new Text(Text.NON_BREAKING_SPACE + Text.NON_BREAKING_SPACE), 3, row);
		table.add(extraContractMessage, 3, row++);

		BooleanInput hasExtraContractOther = (BooleanInput) getStyledInterface(new BooleanInput(PARAM_EXTRA_CONTRACT_OTHER));
		if (application != null) hasExtraContractOther.setSelected(application.getHasExtraContractOther());
		TextInput extraContractOtherMessage = (TextInput) getStyledInterface(new TextInput(PARAM_EXTRA_CONTRACT_OTHER_MESSAGE));
		if (application != null && application.getExtraContractMessageOther() != null) extraContractOtherMessage.setContent(application.getExtraContractMessageOther());
		table.add(getSmallHeader(localize(LABEL_EXTRA_CONTRACT_OTHER, "Extra contract other")), 1, row);
		table.add(hasExtraContractOther, 3, row);
		table.add(new Text(Text.NON_BREAKING_SPACE + Text.NON_BREAKING_SPACE + Text.NON_BREAKING_SPACE), 3, row);
		table.add(getSmallHeader(localize(LABEL_EXTRA_CONTRACT_OTHER_MESSAGE, "Message")), 3, row);
		table.add(new Text(Text.NON_BREAKING_SPACE + Text.NON_BREAKING_SPACE), 3, row);
		table.add(extraContractOtherMessage, 3, row);

		table.setHeight(row++, 12);

		DateInput placementDate = (DateInput) getStyledInterface(new DateInput(PARAM_PLACEMENT_DATE));
		placementDate.keepStatusOnAction(true);
		placementDate.setToDisplayDayLast(true);
		placementDate.setAsNotEmpty(localize("child_care.must_select_placement_date", "You have to select a placement date"));
		if (application != null) {
			placementDate.setDate(application.getFromDate());
		}
		if (earliestPossiblePlacementDate != null) {
			placementDate.setEarliestPossibleDate(earliestPossiblePlacementDate, localize("child_care.placement_date_overlaps_existing_placement", "Selected placement date overlaps existing contract"));
		}
		table.add(getLocalizedLabel(LABEL_PLACEMENT_DATE, "Placement date"), 1, row);
		table.add(placementDate, 3, row++);

		DateInput terminationDate = (DateInput) getStyledInterface(new DateInput(PARAM_TERMINATION_DATE));
		terminationDate.keepStatusOnAction(true);
		terminationDate.setToDisplayDayLast(true);
		table.add(getLocalizedLabel(LABEL_TERMINATION_DATE, "Termination date"), 1, row);
		table.add(terminationDate, 3, row++);

		if (application != null) {
			DateInput replyDate = (DateInput) getStyledInterface(new DateInput(PARAM_LAST_REPLY_DATE));
			replyDate.keepStatusOnAction(true);
			replyDate.setToDisplayDayLast(true);
			replyDate.setAsNotEmpty(localize("child_care.must_select_last_reply_date", "You have to select a last reply date"));
			table.add(getLocalizedLabel(LABEL_LAST_REPLY_DATE, "Last reply date"), 1, row);
			table.add(replyDate, 3, row++);
		}

		TextArea comment = (TextArea) getStyledInterface(new TextArea(PARAM_COMMENT));
		comment.keepStatusOnAction(true);
		comment.setWidth(Table.HUNDRED_PERCENT);
		comment.setHeight("50");
		if (application != null && application.getMessage() != null) comment.setContent(application.getMessage());
		table.setVerticalAlignment(1, row, Table.VERTICAL_ALIGN_TOP);
		table.add(getLocalizedLabel(LABEL_COMMENT, "Comment"), 1, row);
		table.add(comment, 3, row++);

		table.setHeight(row++, 12);

		SubmitButton submit = (SubmitButton) getButton(new SubmitButton(localize("child_care.save", "Save"), PARAM_SUBMIT, "true"));
		form.add(table);
		table.add(submit, 1, row);
		form.setToDisableOnSubmit(submit, true);

		if (getResponsePage() != null) {
			GenericButton cancel = getButton(new GenericButton("", localize("child_care.cancel", "Cancel")));
			cancel.setPageToOpen(getResponsePage());
			table.add(Text.getNonBrakingSpace(), 1, row);
			table.add(cancel, 1, row);
			table.mergeCells(1, row, table.getColumns(), row);
		}

		add(form);
	}

	private CommuneUserBusiness getUserService(IWContext iwc) throws RemoteException {
		return (CommuneUserBusiness) IBOLookup.getServiceInstance(iwc, CommuneUserBusiness.class);
	}

	private SchoolBusiness getSchoolBusiness(IWContext iwc) throws RemoteException {
		return (SchoolBusiness) IBOLookup.getServiceInstance(iwc, SchoolBusiness.class);
	}

	private CheckBusiness getCheckBusiness(IWContext iwc) throws RemoteException {
		return (CheckBusiness) IBOLookup.getServiceInstance(iwc, CheckBusiness.class);
	}

	/**
	 * Returns a formatted and localized form label.
	 * 
	 * @param textKey
	 *          the text key to localize
	 * @param defaultText
	 *          the default localized text
	 * @author anders
	 */
	protected Text getLocalizedLabel(String textKey, String defaultText) {
		return getSmallHeader(localize(textKey, defaultText) + ":");
	}
}