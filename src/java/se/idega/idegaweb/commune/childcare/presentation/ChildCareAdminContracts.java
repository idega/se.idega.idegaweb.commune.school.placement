/*
 */
package se.idega.idegaweb.commune.childcare.presentation;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;

import javax.ejb.FinderException;

import se.idega.idegaweb.commune.block.importer.business.AlreadyCreatedException;
import se.idega.idegaweb.commune.business.CommuneUserBusiness;
import se.idega.idegaweb.commune.childcare.check.business.CheckBusiness;
import se.idega.idegaweb.commune.childcare.data.ChildCareApplication;
import se.idega.idegaweb.commune.childcare.event.ChildCareEventListener;
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

/**
 * @author palli
 *
 * A class for the presenation layer for creating contracts centrally.
 */
public class ChildCareAdminContracts extends ChildCareBlock {
	private User _child = null;
	private ChildCareApplication _application = null;
	
	private final static String PARAM_COMMENT = "prm_comment";
	private final static String PARAM_GETBILL = "prm_getbill";
	private final static String PARAM_OPERATION = "prm_operation";
	private final static String PARAM_GROUP = "prm_group";
	private final static String PARAM_HOURS = "prm_hours";
	private final static String PARAM_EMPLOYMENT = "prm_unemployed";
	private final static String PARAM_PLACEMENT_DATE = "prm_plac_date";
		
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
	
	private final static int ACTION_VIEW_FORM = 1;
	private final static int ACTION_SAVE = 2;
	
	/* (non-Javadoc)
	 * @see com.idega.presentation.PresentationObject#main(com.idega.presentation.IWContext)
	 */
	public void init(IWContext iwc) throws Exception {
		int action = process(iwc);
		
		if (_child != null) {
			switch (action) {
				case ACTION_VIEW_FORM :
					showForm(iwc);
					break;
				case ACTION_SAVE :
					save(iwc);
					break;
				}
		}
		else {
			add(this.getLocalizedHeader("child_care.no_child_or_application_found","No child or application found."));
		}
	}
	
	private void save(IWContext iwc) {
		int ownerID = Integer.parseInt(iwc.getParameter(PARAM_GETBILL));
		int schoolTypeID = Integer.parseInt(iwc.getParameter(PARAM_OPERATION));
		int careTime = Integer.parseInt(iwc.getParameter(PARAM_HOURS));
		int employmentTypeID = Integer.parseInt(iwc.getParameter(PARAM_EMPLOYMENT));
		IWTimestamp placementDate = new IWTimestamp(iwc.getParameter(PARAM_PLACEMENT_DATE));
		String comment = iwc.getParameter(PARAM_COMMENT);
		int groupID = -1;
		try {
			groupID = Integer.parseInt(iwc.getParameter(PARAM_GROUP));
		}
		catch (NumberFormatException npe) {
			groupID = -1;
		}
		boolean success;
		
		try {
			User owner = getBusiness().getUserBusiness().getUser(ownerID);
			success = getBusiness().importChildToProvider(getSession().getApplicationID(), getSession().getChildID(), getSession().getChildCareID(), groupID, careTime, employmentTypeID, schoolTypeID, comment, placementDate, null, iwc.getCurrentLocale(), owner, iwc.getCurrentUser(), true);
		}
		catch (RemoteException re) {
			success = false;
		}
		catch (AlreadyCreatedException ace) {
			success = false;
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
			add(getSmallErrorText(localize("child_care.submit_failed", "Submit failed.")));
			add(new Break(2));
			showForm(iwc);
		}
	}
	
	public int process(IWContext iwc) {
		try {
			if (getSession().getApplicationID() != -1) {
				_application = getBusiness().getApplication(getSession().getApplicationID());
				_child = _application.getChild();
			}
			else if (getSession().getChildID() != -1) {
				_child = getBusiness().getUserBusiness().getUser(getSession().getChildID());
			}
		}
		catch (RemoteException re) {
			_child = null;
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

		Table table = new Table();
		table.setCellpadding(2);
		table.setCellspacing(0);
		int row = 1;
		
		table.add(getLocalizedHeader(LABEL_CHILD,"Child"),1,row++);
		table.add(getLocalizedLabel(LABEL_USER_NAME,"Name"),1,row);
		table.add(getSmallText(_child.getNameLastFirst(true)),3,row++);
		table.add(getLocalizedLabel(LABEL_PERSONAL_ID,"Personal ID"),1,row);
		table.add(getSmallText(PersonalIDFormatter.format(_child.getPersonalID(), iwc.getCurrentLocale())),3,row++);
		table.add(getLocalizedLabel(LABEL_ADDRESS,"Address"),1,row);
		try {
			Address address = getUserService(iwc).getUsersMainAddress(_child);
			if (address != null) {
				String postalAddress = address.getPostalAddress();
				table.add(getSmallText(address.getStreetAddress()),3,row);
				if (postalAddress != null)
					table.add(getSmallText(", " + postalAddress), 3, row);
			}
		}
		catch (RemoteException e) {
			e.printStackTrace();
		}
		row++;
		
		boolean hasCheck = false;
		try {
			if (_child != null)
				hasCheck = getCheckBusiness(iwc).hasGrantedCheck(_child);
		}
		catch (RemoteException e) {
			e.printStackTrace();
		}

		table.add(getLocalizedLabel(LABEL_GRANTED_CHECK,"Granted check"),1,row);
		if (!hasCheck)
			table.add(getSmallText(localize("child_care.no","No")),3,row++);
		else
			table.add(getSmallText(localize("child_care.yes","Yes")),3,row++);
		table.setHeight(row++, 12);
		
		Collection parents;
		try {
			parents = getBusiness().getUserBusiness().getParentsForChild(_child);
		}
		catch (RemoteException e2) {
			parents = null;
		}
		
		if (parents != null) {
			table.add(getLocalizedHeader("child_care.custodians","Custodians"), 1, row++);

			Iterator iter = parents.iterator();
			while (iter.hasNext()) {
				User parent = (User) iter.next();

				table.add(getLocalizedLabel(LABEL_USER_NAME,"Name"),1,row);
				table.add(getSmallText(parent.getNameLastFirst(true)), 3, row++);
				table.add(getLocalizedLabel(LABEL_PERSONAL_ID,"Personal ID"),1,row);
				table.add(getSmallText(PersonalIDFormatter.format(parent.getPersonalID(), iwc.getCurrentLocale())), 3, row++);
				table.add(getLocalizedLabel(LABEL_GETS_BILL,"Gets bill"),1,row);
				RadioButton getBill = this.getRadioButton(PARAM_GETBILL, parent.getPrimaryKey().toString());
				getBill.keepStatusOnAction(true);
				getBill.setMustBeSelected(localize("child_care.must_select_billed_to", "You must select who to send the bill to."));
				table.add(getBill,3,row++);
				
				row++;
				if (iter.hasNext())
					table.setHeight(row++, 6);
			}
		}
		table.setHeight(row++, 12);
		
		table.add(getLocalizedHeader(LABEL_PROVIDER,"Provider"),1,row++);
		table.add(getLocalizedLabel(LABEL_PROVIDER_NAME,"Name"),1,row);
		try {
			Collection schools = getSchoolBusiness(iwc).getSchoolHome().findAllByCategory(getSchoolBusiness(iwc).getCategoryChildcare());
			SelectorUtility sel = new SelectorUtility();
			DropdownMenu prov = (DropdownMenu) sel.getSelectorFromIDOEntities(new DropdownMenu(getSession().getParameterChildCareID()), schools, "getName");
			prov.addMenuElementFirst("-1", "");
			prov.setToSubmit(true);
			prov.setAsNotEmpty(localize("child_care.must_select_provider","You must select a provider."));
			if (_application != null)
				prov.setSelectedElement(_application.getProviderId());
			if (getSession().getChildCareID() != -1)
				prov.setSelectedElement(getSession().getChildCareID());
			
			table.add(getStyledInterface(prov),3,row);

			GenericButton createProvider = getButton(new GenericButton("", localize("child_care.create_provider", "Create provider")));
			createProvider.setWindowToOpen(CentralPlacementProviderEditor.class);
			table.add(Text.getNonBrakingSpace(), 3, row);
			table.add(createProvider, 3, row);
			row++;
		}
		catch (RemoteException e1) {
			e1.printStackTrace();
		}
		catch (FinderException e1) {
			e1.printStackTrace();
		}
		
		table.add(getLocalizedLabel(LABEL_PROVIDER_OPERATION,"Operation"),1,row);
		try {
			Collection school_types = getSchoolBusiness(iwc).getSchoolTypeHome().findAllByCategory(getSchoolBusiness(iwc).getCategoryChildcare().getCategory());
			SelectorUtility sel = new SelectorUtility();
			DropdownMenu op = (DropdownMenu) sel.getSelectorFromIDOEntities(new DropdownMenu(PARAM_OPERATION), school_types, "getName");
			op.keepStatusOnAction(true);
			table.add(getStyledInterface(op),3,row++);
		}
		catch (RemoteException e1) {
			e1.printStackTrace();
		}
		catch (FinderException e1) {
			e1.printStackTrace();
		}
		
		table.add(getLocalizedLabel(LABEL_PROVIDER_GROUP,"Group"),1,row);
		try {
			Collection school_classes = getSchoolBusiness(iwc).getSchoolClassHome().findBySchoolAndCategory(getSession().getChildCareID(), getSchoolBusiness(iwc).getCategoryChildcare().getCategory());
			SelectorUtility sel = new SelectorUtility();
			DropdownMenu op = (DropdownMenu) sel.getSelectorFromIDOEntities(new DropdownMenu(PARAM_GROUP), school_classes, "getName");
			if (!op.isEmpty()) {
				op.setAsNotEmpty(localize("child_care.must_select_group","You must select a group."), "-1");
				op.addMenuElementFirst("-1", "");
				op.keepStatusOnAction(true);
			}
			table.add(getStyledInterface(op),3,row);
			
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
		table.setHeight(row++, 12);
		
		TextInput hoursWeek = (TextInput) getStyledInterface(new TextInput(PARAM_HOURS));
		hoursWeek.keepStatusOnAction(true);
		hoursWeek.setLength(2);
		hoursWeek.setAsNotEmpty(localize("child_care.child_care_time_required","You must fill in the child care time."));
		hoursWeek.setAsIntegers(localize("child_care.only_integers_allowed","Not a valid child care time."));
		table.add(getLocalizedLabel(LABEL_HOURS,"Hours pr. week"),1,row);
		table.add(hoursWeek,3,row++);
		
		try {
			DropdownMenu employment = this.getEmploymentTypes(PARAM_EMPLOYMENT, -1);
			employment.keepStatusOnAction(true);
			employment.setAsNotEmpty(localize("child_care.must_select_employment_type","You must select employment type."), "-1");
			table.add(getLocalizedLabel(LABEL_EMPLOYMENT,"Employment"),1,row);
			table.add(employment,3,row++);
		}
		catch (RemoteException e1) {
			e1.printStackTrace();
		}
		
		table.setHeight(row++, 12);
		
		DateInput placementDate = (DateInput) getStyledInterface(new DateInput(PARAM_PLACEMENT_DATE));
		placementDate.keepStatusOnAction(true);
		placementDate.setToDisplayDayLast(true);
		placementDate.setAsNotEmpty(localize("child_care.must_select_placement_date", "You have to select a placement date"));
		if (_application != null)
			placementDate.setDate(_application.getFromDate());
		table.add(getLocalizedLabel(LABEL_PLACEMENT_DATE,"Placement date"),1,row);
		table.add(placementDate,3,row++);
		
		TextArea comment = (TextArea) getStyledInterface(new TextArea(PARAM_COMMENT));
		comment.keepStatusOnAction(true);
		comment.setWidth(Table.HUNDRED_PERCENT);
		comment.setHeight("50");
		if (_application != null && _application.getMessage() != null)
			comment.setContent(_application.getMessage());
		table.setVerticalAlignment(1, row, Table.VERTICAL_ALIGN_TOP);
		table.add(getLocalizedLabel(LABEL_COMMENT,"Comment"),1,row);
		table.add(comment,3,row++);
		
		table.setHeight(row++, 12);
		
		SubmitButton submit = (SubmitButton) getButton(new SubmitButton(localize("child_care.save", "Save"), PARAM_SUBMIT, "true"));
		table.add(submit,1,row);
		
		if (getResponsePage() != null) {
			GenericButton cancel = getButton(new GenericButton("", localize("child_care.cancel", "Cancel")));
			cancel.setPageToOpen(getResponsePage());
			table.add(Text.getNonBrakingSpace(), 1, row);
			table.add(cancel, 1, row);
			table.mergeCells(1, row, table.getColumns(), row);
		}
												
		form.add(table);
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
	 * @param textKey the text key to localize
	 * @param defaultText the default localized text
	 * @author anders
	 */
	protected Text getLocalizedLabel(String textKey, String defaultText) {
		return getSmallHeader(localize(textKey, defaultText) + ":");
	}
}