/*
 * Created on 11.4.2003
 */
package se.idega.idegaweb.commune.childcare.presentation;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.ejb.FinderException;

import se.idega.idegaweb.commune.childcare.business.AfterSchoolBusiness;
import se.idega.idegaweb.commune.childcare.data.AfterSchoolChoice;
import se.idega.idegaweb.commune.presentation.CitizenChildren;
import se.idega.idegaweb.commune.school.business.SchoolChoiceBusiness;
import se.idega.idegaweb.commune.school.data.SchoolChoice;

import com.idega.block.navigation.presentation.UserHomeLink;
import com.idega.block.school.data.SchoolArea;
import com.idega.block.school.data.SchoolSeason;
import com.idega.business.IBOLookup;
import com.idega.core.builder.data.ICPage;
import com.idega.core.location.data.Address;
import com.idega.data.IDOCreateException;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.presentation.IWContext;
import com.idega.presentation.Table;
import com.idega.presentation.text.Break;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.CheckBox;
import com.idega.presentation.ui.DateInput;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.SubmitButton;
import com.idega.presentation.ui.TextArea;
import com.idega.user.data.User;
import com.idega.util.IWTimestamp;
import com.idega.util.PersonalIDFormatter;
import com.idega.util.text.Name;

/**
 * Application form for after school centre
 * @author aron
 *
 */
public class AfterSchoolChoiceApplication extends ChildCareBlock {

	private User child;
	//	private GrantedCheck check;

	private final static int ACTION_VIEW_FORM = 1;
	private final static int ACTION_SUBMIT = 2;

	private int _action = -1;

	private final static String PARAMETER_ACTION = "ccas_action";
	private final static String PARAM_FORM_SUBMIT = "ccas_submit";
	private final static String PARAM_DATE = "ccas_date";
	private final static String PARAM_AREA = "ccas_area";
	private final static String PARAM_PROVIDER = "ccas_provider";
	private final static String PARAM_MESSAGE = "ccas_message";
	private final static String PARAM_SEND_TO_CHECK = "ccas_send_to_check";
	
	private final static String PROVIDERS = "ccas_providers";
	private final static String NAME = "ccas_name";
	private final static String PID = "ccas_pid";
	private final static String ADDRESS = "ccas_address";
	private final static String CARE_FROM = "ccas_care_from";
	private final static String APPLICATION_INSERTED = "ccas_application_ok";
	private final static String APPLICATION_FAILURE = "ccas_application_failed";
	private final static String SEND_TO_CHECK = "ccas_send_to_check";
	private final static String CHECK_MESSAGE = "ccas_check_message";
	
	private final static String LOCALIZE_PREFIX = "after_school.";

	private final static String EMAIL_PROVIDER_SUBJECT = "application_received_subject";
	private final static String EMAIL_PROVIDER_MESSAGE = "application_received_body";

	private String prmChildId = CitizenChildren.getChildIDParameterName();

	private Collection areas;
	private Map providerMap;

	private boolean isAdmin = false;
	private boolean _useOngoingSeason = false;
	private boolean _showCheckOption = false;
	
	private ICPage _checkPage;

	/**
	 * @see se.idega.idegaweb.commune.childcare.presentation.ChildCareBlock#init(com.idega.presentation.IWContext)
	 */
	public void init(IWContext iwc) throws Exception {
		initChild(iwc);
		if (child != null) {
			parseAction(iwc);

			switch (_action) {
				case ACTION_VIEW_FORM :
					viewForm(iwc);
					break;
				case ACTION_SUBMIT :
					submitForm(iwc);
					break;
			}
		}
		else {
			add(getHeader(localize("no_student_id_provided", "No student provided")));
		}
	}

	public void initChild(IWContext iwc) {
		String ID = iwc.getParameter(prmChildId);
		if (ID != null && !ID.equals("-1")) {
			try {
				child = getBusiness().getUserBusiness().getUser(Integer.parseInt(ID));
			}
			catch (NumberFormatException e) {
				e.printStackTrace();
			}
			catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		else {
			try {
				child = getBusiness().getUserBusiness().getUser(getSession().getChildID());
			}
			catch (RemoteException e) {
				e.printStackTrace();
			}
			catch (Exception e){
				log(e);
			}
			
		}
	}

	protected boolean isAdmin(IWContext iwc) {
		if (iwc.hasEditPermission(this))
			return true;

		try {
			return getBusiness().getUserBusiness().isRootCommuneAdministrator(iwc.getCurrentUser());
		}
		catch (RemoteException re) {
			return false;
		}
	}

	private void parseAction(IWContext iwc) {
		isAdmin = isAdmin(iwc);

		if (iwc.isParameterSet(PARAMETER_ACTION))
			_action = Integer.parseInt(iwc.getParameter(PARAMETER_ACTION));
		else
			_action = ACTION_VIEW_FORM;

	}

	private void viewForm(IWContext iwc) throws RemoteException {
		if (child != null) {
			Form form = new Form();
			form.maintainParameter(prmChildId);

			Table table = new Table();
			//table.setWidth(getWidth());
			table.setCellpadding(getCellpadding());
			table.setCellspacing(getCellspacing());
			form.add(table);

			int row = 1;
			table.add(getChildInfoTable(iwc), 1, row++);
			table.setHeight(row++, 12);
			table.add(getInputTable(iwc), 1, row++);
			table.setHeight(row++, 12);

			SubmitButton submit = (SubmitButton) getButton(new SubmitButton(localize(PARAM_FORM_SUBMIT, "Submit application"), PARAMETER_ACTION, String.valueOf(ACTION_SUBMIT)));
			if (isAdmin) {
				try {
					User parent = getBusiness().getUserBusiness().getCustodianForChild(child);
					if (parent == null)
						submit.setDisabled(true);
				}
				catch (RemoteException re) {
					submit.setDisabled(true);
				}
			}
			table.setBorder(0);
			table.add(submit, 1, row);
			table.setAlignment(1, row, Table.HORIZONTAL_ALIGN_RIGHT);
			submit.setOnSubmitFunction("checkApplication", getSubmitCheckScript());
			form.setToDisableOnSubmit(submit, true);
			

			if (submit.getDisabled()) {
				row++;
				table.setHeight(row++, 6);
				table.add(getSmallErrorText(localize("no_parent_found", "No parent found")), 1, row);
			}

			add(form);
		}
		else {

			add(getErrorText(localize("no_child_selected", "No child selected.")));
			add(new Break(2));
			add(new UserHomeLink());
		}
	}

	private void submitForm(IWContext iwc) {
		List choices = null;
		boolean done = false;
		boolean sendToCheckPage = false;
		
		try {
			int numberOfChoices = 3;
			Integer[] providers = new Integer[numberOfChoices];
			String[] dates = new String[numberOfChoices];
			sendToCheckPage = iwc.isParameterSet(PARAM_SEND_TO_CHECK);

			for (int i = 0; i < numberOfChoices; i++) {
				providers[i] = iwc.isParameterSet(PARAM_PROVIDER + "_" + (i + 1)) ? Integer.valueOf(iwc.getParameter(PARAM_PROVIDER + "_" + (i + 1))) : null;
				dates[i] = iwc.isParameterSet(PARAM_DATE + "_" + (i + 1)) ? iwc.getParameter(PARAM_DATE + "_" + (i + 1)) : null;
			}
			String message = iwc.getParameter(PARAM_MESSAGE);

			User parent = null;
			if (isAdmin) {
				parent = getBusiness().getUserBusiness().getCustodianForChild(child);
			}
			else {
				parent = iwc.getCurrentUser();
			}

			SchoolSeason season = null;
			if (_useOngoingSeason) {
				try {
					season = getBusiness().getSchoolChoiceBusiness().getSchoolSeasonHome().findSeasonByDate(new IWTimestamp().getDate());
				}
				catch (FinderException e) {
					season = null;
				}
				catch (RemoteException e) {
					season = null;
				}
			}
			
			String subject = localize(EMAIL_PROVIDER_SUBJECT, "After school application received");
			String body = localize(EMAIL_PROVIDER_MESSAGE, "We have received your after school application for {0} to {1}.");
			choices = getAfterSchoolBusiness(iwc).createAfterSchoolChoices(parent, (Integer) child.getPrimaryKey(), providers, message, dates, season, subject, body);
			done = choices != null && !choices.isEmpty();
		}
		catch (RemoteException e) {
			e.printStackTrace();
			done = false;
		}
		catch (IDOCreateException e) {
			e.printStackTrace();
			done = false;
		}

		if (done) {
			if (sendToCheckPage) {
				if (getCheckPage() != null) {
					iwc.forwardToIBPage(getParentPage(), getCheckPage());
				}
				else {
					add(new Text(localize(APPLICATION_INSERTED, "Application submitted")));
				}
			}
			else {
				if (getResponsePage() != null) {
					iwc.forwardToIBPage(getParentPage(), getResponsePage());
				}
				else {
					add(new Text(localize(APPLICATION_INSERTED, "Application submitted")));
				}
			}
		}
		else {
			add(new Text(localize(APPLICATION_FAILURE, "Failed to submit application")));
		}
	}

	private Table getInputTable(IWContext iwc) throws RemoteException {
		Table inputTable = new Table();
		inputTable.setCellspacing(0);
		inputTable.setCellpadding(2);
		inputTable.setColumns(3);

		int row = 1;

		if (_showCheckOption) {
			inputTable.mergeCells(1, row, inputTable.getColumns(), row);
			inputTable.setWidth(1, row, Table.HUNDRED_PERCENT);
			inputTable.add(getSmallHeader(localize(CHECK_MESSAGE, "Please note that if you select a private provider, you have to fill out a check application as well.")), 1, row++);
			inputTable.setHeight(row++, 6);
			
			CheckBox sendToCheck = getCheckBox(PARAM_SEND_TO_CHECK, "true");
			inputTable.mergeCells(1, row, inputTable.getColumns(), row);
			inputTable.setWidth(1, row, Table.HUNDRED_PERCENT);
			inputTable.add(sendToCheck, 1, row);
			inputTable.add(Text.getNonBrakingSpace(), 1, row);
			inputTable.add(getSmallHeader(localize(SEND_TO_CHECK, "Yes, send me to the check application.")), 1, row++);
			
			inputTable.setHeight(row++, 12);
		}
		
		inputTable.mergeCells(1, row, inputTable.getColumns(), row);
		inputTable.add(getHeader(localize(PROVIDERS, "Providers")), 1, row++);

		String message = null;
		String strProvider = localize(PARAM_PROVIDER, "Provider");
		Text labelProvider = getSmallHeader(strProvider);
		Text labelFrom = getSmallHeader(localize(CARE_FROM, "From") + ":");
		IWTimestamp stamp = new IWTimestamp();

		AfterSchoolChoice afterSchoolChoice = null;
		SchoolChoice schoolChoice = null;
		int areaID = -1;
		int schoolID = -1;
		Integer childID = new Integer(getSession().getChildID());
		try {
			Integer seasonID = (Integer) getSchoolChoiceBusiness(iwc).getCurrentSeason().getPrimaryKey();
			for (int i = 1; i <= 3; i++) {
				try {
					afterSchoolChoice = getAfterSchoolBusiness(iwc).findChoicesByChildAndChoiceNumberAndSeason(childID, i, seasonID);
					areaID = afterSchoolChoice.getProvider().getSchoolAreaId();
					schoolID = afterSchoolChoice.getProviderId();
					message = afterSchoolChoice.getMessage();
				}
				catch (FinderException fe) {
					try {
						schoolChoice = getSchoolChoiceBusiness(iwc).getSchoolChoiceHome().findByChildAndChoiceNumberAndSeason(childID, new Integer(i), seasonID);
						if (getSchoolChoiceBusiness(iwc).getSchoolBusiness().hasAfterSchoolActivities(schoolChoice.getChosenSchoolId())) {
							areaID = schoolChoice.getChosenSchool().getSchoolAreaId();
							schoolID = schoolChoice.getChosenSchoolId();
						}
						else {
							areaID = -1;
							schoolID = -1;
						}
					}
					catch (FinderException e) {
						areaID = -1;
						schoolID = -1;
						message = null;
					}
				}

				ProviderDropdownDouble dropdown = (ProviderDropdownDouble) getStyledInterface(getDropdown(iwc.getCurrentLocale(), PARAM_AREA + "_" + i, PARAM_PROVIDER + "_" + i));
				if (areaID > 0 && schoolID > 0) {
					dropdown.setSelectedValues(String.valueOf(areaID), String.valueOf(schoolID));
				}
				labelProvider = getSmallHeader(strProvider + Text.NON_BREAKING_SPACE + i + ":");
				inputTable.add(labelProvider, 1, row);
				inputTable.setWidth(1, row, 100);
				inputTable.add(dropdown, 3, row++);

				DateInput date = (DateInput) getStyledInterface(new DateInput(PARAM_DATE + "_" + i));
				if (afterSchoolChoice != null)
					date.setDate(afterSchoolChoice.getFromDate());
				else
					date.setToCurrentDate();
				if (isAdmin)
					date.setYearRange(stamp.getYear() - 5, stamp.getYear() + 5);
				inputTable.add(labelFrom, 1, row);
				inputTable.setWidth(1, row, 100);
				inputTable.add(date, 3, row++);

				inputTable.setHeight(row++, 12);
			}

			TextArea messageArea = (TextArea) getStyledInterface(new TextArea(PARAM_MESSAGE));
			messageArea.setRows(4);
			messageArea.setWidth(Table.HUNDRED_PERCENT);
			if (message != null)
				messageArea.setContent(message);

			inputTable.setVerticalAlignment(1, row, Table.VERTICAL_ALIGN_TOP);
			inputTable.add(getSmallHeader(localize("message", "Message")), 1, row);
			inputTable.setWidth(1, row, 100);
			inputTable.add(messageArea, 3, row++);
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		inputTable.setWidth(2, 8);

		return inputTable;
	}

	private Table getChildInfoTable(IWContext iwc) {
		Table table = new Table(3, 3);
		table.setColumns(3);
		table.setCellpadding(2);
		table.setCellspacing(0);
		table.setWidth(1, 100);
		table.setWidth(2, 8);

		table.add(getSmallHeader(localize(NAME, "Name") + ":"), 1, 1);
		table.add(getSmallHeader(localize(PID, "Personal ID") + ":"), 1, 2);
		table.add(getSmallHeader(localize(ADDRESS, "Address") + ":"), 1, 3);

		Name name = new Name(child.getFirstName(), child.getMiddleName(), child.getLastName());
		table.add(getSmallText(name.getName(iwc.getApplicationSettings().getDefaultLocale(), true)), 3, 1);
		String personalID = PersonalIDFormatter.format(child.getPersonalID(), iwc.getIWMainApplication().getSettings().getApplicationLocale());
		table.add(getSmallText(personalID), 3, 2);

		try {
			Address address = getBusiness().getUserBusiness().getUsersMainAddress(child);
			if (address != null)
				table.add(getSmallText(address.getStreetAddress() + ", " + address.getPostalAddress()), 3, 3);
		}
		catch (RemoteException e) {
		}

		return table;
	}

	public String getSubmitCheckScript() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("\nfunction checkApplication(){\n\t");
		buffer.append("\n\t var dropOne = ").append("findObj('").append(PARAM_PROVIDER + "_1").append("');");
		buffer.append("\n\t var dropTwo = ").append("findObj('").append(PARAM_PROVIDER + "_2").append("');");
		buffer.append("\n\t var dropThree = ").append("findObj('").append(PARAM_PROVIDER + "_3").append("');");

		buffer.append("\n\t var one = 0;");
		buffer.append("\n\t var two = 0;");
		buffer.append("\n\t var three = 0;");
		buffer.append("\n\t var length = 0;");

		buffer.append("\n\n\t if (dropOne.selectedIndex > 0) {\n\t\t one = dropOne.options[dropOne.selectedIndex].value;\n\t\t length++;\n\t }");
		buffer.append("\n\t if (dropTwo.selectedIndex > 0) {\n\t\t two = dropTwo.options[dropTwo.selectedIndex].value;\n\t\t length++;\n\t }");
		buffer.append("\n\t if (dropThree.selectedIndex > 0) {\n\t\t three = dropThree.options[dropThree.selectedIndex].value;\n\t\t length++;\n\t }");

		buffer.append("\n\t if(length > 0){");
		buffer.append("\n\t\t if(one > 0 && (one == two || one == three)){");
		String message = localize("must_not_be_the_same", "Please do not choose the same provider more than once.");
		buffer.append("\n\t\t\t alert('").append(message).append("');");
		buffer.append("\n\t\t\t return false;");
		buffer.append("\n\t\t }");
		buffer.append("\n\t\t if(two > 0 && (two == one || two == three)){");
		message = localize("must_not_be_the_same", "Please do not choose the same provider more than once.");
		buffer.append("\n\t\t\t alert('").append(message).append("');");
		buffer.append("\n\t\t\t return false;");
		buffer.append("\n\t\t }");
		buffer.append("\n\t\t if(three > 0 && (three == one || three == two )){");
		message = localize("must_not_be_the_same", "Please do not choose the same provider more than once.");
		buffer.append("\n\t\t\t alert('").append(message).append("');");
		buffer.append("\n\t\t\t return false;");
		buffer.append("\n\t\t }");

		buffer.append("\n\t }");
		buffer.append("\n\t else {");
		message = localize("must_fill_out_one", "Please fill out the first choice.");
		buffer.append("\n\t\t alert('").append(message).append("');");
		buffer.append("\n\t\t return false;");
		buffer.append("\n\t }");
		message = localize("less_than_three_chosen", "You have chosen less than three choices.  An offer can not be guaranteed within three months.");
		buffer.append("\n\t if(length < 3)\n\t\t return confirm('").append(message).append("');");
		buffer.append("\n\t document.body.style.cursor = 'wait'");
		buffer.append("\n\t return true;");
		buffer.append("\n}\n");
		return buffer.toString();
	}

	private ProviderDropdownDouble getDropdown(Locale locale, String primaryName, String secondaryName) {
		ProviderDropdownDouble dropdown = new ProviderDropdownDouble(primaryName, secondaryName);
		String emptyString = localize("select_provider", "Select provider...");
		dropdown.addEmptyElement(localize("select_area", "Select area..."), emptyString);

		try {
			if (areas == null)
				areas = getBusiness().getSchoolBusiness().findAllSchoolAreas();
			if (providerMap == null)
				providerMap = getBusiness().getProviderAreaMap(areas, locale, emptyString, true);

			if (areas != null && providerMap != null) {
				Iterator iter = areas.iterator();
				while (iter.hasNext()) {
					SchoolArea area = (SchoolArea) iter.next();
					dropdown.addMenuElement(area.getPrimaryKey().toString(), area.getSchoolAreaName(), (Map) providerMap.get(area));
				}
			}
		}
		catch (RemoteException e) {
			e.printStackTrace();
		}

		return dropdown;
	}

	private SchoolChoiceBusiness getSchoolChoiceBusiness(IWApplicationContext iwac) throws RemoteException {
		return (SchoolChoiceBusiness) IBOLookup.getServiceInstance(iwac, SchoolChoiceBusiness.class);
	}

	private AfterSchoolBusiness getAfterSchoolBusiness(IWApplicationContext iwac) throws RemoteException {
		return (AfterSchoolBusiness) IBOLookup.getServiceInstance(iwac, AfterSchoolBusiness.class);
	}

	/* (non-Javadoc)
	 * @see se.idega.idegaweb.commune.presentation.CommuneBlock#localize(java.lang.String, java.lang.String)
	 */
	public String localize(String textKey, String defaultText) {
		return super.localize(LOCALIZE_PREFIX + textKey, defaultText);
	}
	
	public void setUseOngoingSeason(boolean useOngoingSeason) {
		_useOngoingSeason = useOngoingSeason;
	}
	
	/**
	 * @param showCheckOption The showCheckOption to set.
	 */
	public void setShowCheckOption(boolean showCheckOption) {
		this._showCheckOption = showCheckOption;
	}
	
	/**
	 * @param checkPage The checkPage to set.
	 */
	public void setCheckPage(ICPage checkPage) {
		this._checkPage = checkPage;
	}
	
	/**
	 * @return Returns the checkPage.
	 */
	private ICPage getCheckPage() {
		return this._checkPage;
	}
}