/*
 * Created on 2003-sep-23
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package se.idega.idegaweb.commune.school.presentation;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;

import se.idega.idegaweb.commune.accounting.resource.business.ResourceBusiness;
import se.idega.idegaweb.commune.accounting.resource.data.Resource;
import se.idega.idegaweb.commune.business.CommuneUserBusiness;
import se.idega.idegaweb.commune.childcare.business.ChildCareBusinessBean;
import se.idega.idegaweb.commune.childcare.data.ChildCareContract;
import se.idega.idegaweb.commune.presentation.CommuneBlock;
import se.idega.idegaweb.commune.school.business.CentralPlacementBusinessBean;
import se.idega.idegaweb.commune.school.business.CentralPlacementException;
import se.idega.idegaweb.commune.school.business.SchoolChoiceBusiness;

import com.idega.block.school.business.SchoolBusiness;
import com.idega.block.school.data.School;
import com.idega.block.school.data.SchoolCategory;
import com.idega.block.school.data.SchoolCategoryHome;
import com.idega.block.school.data.SchoolClass;
import com.idega.block.school.data.SchoolClassMember;
import com.idega.block.school.data.SchoolSeason;
import com.idega.block.school.data.SchoolStudyPath;
import com.idega.block.school.data.SchoolStudyPathHome;
import com.idega.block.school.data.SchoolType;
import com.idega.block.school.data.SchoolYear;
import com.idega.business.IBOLookup;
import com.idega.core.contact.data.Phone;
import com.idega.core.location.data.Address;
import com.idega.core.location.data.Commune;
import com.idega.data.IDOLookup;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.Table;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.CheckBox;
import com.idega.presentation.ui.DateInput;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.Parameter;
import com.idega.presentation.ui.SubmitButton;
import com.idega.presentation.ui.TextInput;
import com.idega.user.data.User;
import com.idega.util.IWTimestamp;

/**
 * @author Göran Borgman
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class CentralPlacementEditor extends CommuneBlock {
	// *** Localization keys ***
	private static final String KP = "central_placing_editor.";
	private static final String KEY_WINDOW_HEADING = KP + "window_heading";
	private static final String KEY_SEARCH_PUPIL_HEADING = KP + "search_pupil_heading";
	private static final String KEY_PUPIL_HEADING = KP + "pupil_heading";
	private static final String KEY_CURRENT_PLACEMENT_HEADING = KP + "current_placemant_heading";
	private static final String KEY_NEW_PLACEMENT_HEADING = KP + "new_placement_heading";
		// Label keys
	private static final String KEY_PERSONAL_ID_LABEL = KP + "personal_id_label";
	private static final String KEY_FIRST_NAME_LABEL = KP + "first_name_label";
	private static final String KEY_LAST_NAME_LABEL = KP + "last_name_label";
	private static final String KEY_ADDRESS_LABEL = KP + "address_label";
	private static final String KEY_PHONE_LABEL = KP + "telephone_label";
	private static final String KEY_ACTIVITY_LABEL = KP + "activity_label";
	private static final String KEY_PLACEMENT_LABEL = KP + "placement_label";
	private static final String KEY_RESOURCES_LABEL = KP + "resources_label";
	private static final String KEY_CONTRACT_LABEL = KP + "contract_label";
	private static final String KEY_CONTRACT_YES = KP + "contract_yes";
	private static final String KEY_PROVIDER_LABEL = KP + "provider_label";
	private static final String KEY_MAIN_ACTIVITY_LABEL = KP + "main_activity";
	private static final String KEY_ADMIN_LABEL = KP + "admin_label";
	private static final String KEY_SCHOOL_YEAR_LABEL = KP + "school_year_label";
	private static final String KEY_SCHOOL_GROUP_LABEL = KP + "school_group_label";
	private static final String KEY_STUDY_PATH_LABEL = KP + "study_path_label";
	private static final String KEY_RESOURCE_LABEL = KP + "resource_label";
	private static final String KEY_COMMUNE_LABEL = KP + "commune_label";
	private static final String KEY_PAYMENT_BY_INVOICE_LABEL = KP + "payment_by_invoice_label";
	private static final String KEY_PLACEMENT_PARAGRAPH_LABEL = "placement_paragraph_label";
	private static final String KEY_PAYMENT_BY_AGREEMENT_LABEL = 
																								KP + "Payment by agreement: ";
	private static final String KEY_INVOICE_INTERVAL_LABEL = KP + "Invoice interval: ";
	private static final String KEY_PLACEMENT_DATE_LABEL = KP + "placement_date_label";

	private static final String KEY_DROPDOWN_CHOSE = KP + "dropdown_chose";
	private static final String KEY_DROPDOWN_YES = KP + "dropdown_yes";
	private static final String KEY_DROPDOWN_NO = KP + "dropdown_no";
	private static final String KEY_SCHOOL_YEAR = KP + "school_year";
	private static final String KEY_SCHOOL_GROUP = KP + "school_group";
	private static final String KEY_CENTRAL_ADMIN = KP + "central_admin";
	private static final String KEY_PROVIDER_ADMIN = KP + "provider_admin";
	
		// Button keys
	private static final String KEY_BUTTON_SEARCH = KP + "button_search";
	private static final String KEY_BUTTON_REGULAR_PAYMENT = KP + "button_regular_payment";
	private static final String KEY_BUTTON_PLACEMENT_HISTORY = KP + "placement_history";
	private static final String KEY_BUTTON_PUPIL_OVERVIEW = KP + "pupil_overview";
	//private static final String KEY_BUTTON_NEW_PROVIDER = KP + "button_new_provider";
	private static final String KEY_BUTTON_CONTRACT_HISTORY = KP + "button_contract_history";
	private static final String KEY_BUTTON_NEW_GROUP = KP + "button_new_group";
	private static final String KEY_BUTTON_PLACE = KP + "button_place";
	private static final String KEY_BUTTON_CANCEL = KP + "button_cancel";	

	// Http request parameters  
	public static final String PARAM_ACTION = "param_action";
	public static final String PARAM_PRESENTATION = "param_presentation";
	public static final String PARAM_SCHOOL_CATEGORY = "param_school_category";
	public static final String PARAM_PROVIDER = "param_provider";
	public static final String PARAM_ACTIVITY = "param_activity";
	public static final String PARAM_SCHOOL_YEAR = "param_school_year";
	public static final String PARAM_SCHOOL_GROUP = "param_school_group";
	//private static final String PARAM_STUDY_PATH = "param_study_path";
	public static final String PARAM_PLACEMENT_DATE = "param_placement_date";
	public static final String PARAM_RESOURCES = "param_resources";
	//private static final String PARAM_HIDDEN_SUBMIT_SRC = "param_hidden_submit_src";
	// PARAM_BACK is used in SearchUserModule
	public static final String PARAM_BACK = "param_back";
	public static final String PARAM_PAYMENT_BY_AGREEMENT = "payment_by_agreement";
	public static final String PARAM_PAYMENT_BY_INVOICE = "payment_by_invoice";
	public static final String PARAM_PLACEMENT_PARAGRAPH = "placement_paragraph";
	public static final String PARAM_INVOICE_INTERVAL = "invoice_interval";
	public static final String PARAM_STUDY_PATH = "study_path";

	// Actions
	private static final int ACTION_PLACE_PUPIL = 1;	
	private static final int ACTION_REMOVE_SESSION_CHILD = 2;
	
	// Presentations
	private static final int PRESENTATION_SEARCH_FORM = 1;

	// CSS styles   
	private static final String STYLE_UNDERLINED_SMALL_HEADER =
		"font-style:normal;text-decoration:underline;color:#000000;"
			+ "font-size:10px;font-family:Verdana,Arial,Helvetica;font-weight:bold;";
	// Paths
	private static final String PATH_TRANS_GIF =
		"/idegaweb/bundles/com.idega.core.bundle/resources/transparentcell.gif";

	// Session java bean keys
	public static final String SESSION_KEY_CHILD = KP + "session_key.child";

	// Unique parameter suffix used by SearchUserModule
	private static final String UNIQUE_SUFFIX = "chosen_user";

	// Instance variables
	private IWResourceBundle iwrb;
	//private IWBundle iwb;
	private Form form;
	private Table mainTable;
	private User child;
	private String uniqueUserSearchParam;
	private Address address;
	//private boolean hasChild = false;
	//private ApplicationForm appForm;
	private Image transGIF = new Image(PATH_TRANS_GIF);
	private String errMsgMid = null;
	//private String errMsgBottom = null;
	// Form status variables
	//private String categoryStatus = "-1";
	//private String providerStatus = "-1";
	//private String activityStatus = "-1";
	//private String yearStatus = "-1";
	//private String groupStatus = "-1";

	private int _action = -1;
	private int _presentation = -1;

	public void main(IWContext iwc) throws Exception {
		iwrb = getResourceBundle(iwc);
		form = new Form();		
		// Parameter name returning chosen User from SearchUserModule
		uniqueUserSearchParam = SearchUserModule.getUniqueUserParameterName(UNIQUE_SUFFIX);

		form.add(getMainTable());
		parse(iwc);
		
		// remove warning
		PARAM_BACK.toString();
		
		// Borgman test
		//String testParam = iwc.getParameter(PARAM_BACK);
		//String searchCommitted = iwc.getParameter(SearchUserModule.SEARCH_COMMITTED + UNIQUE_SUFFIX);

		//String first = iwc.getParameter("usrch_search_fname" + UNIQUE_SUFFIX);
		//String middle = iwc.getParameter("usrch_search_mname" + UNIQUE_SUFFIX);
		//String last = iwc.getParameter("usrch_search_lname" + UNIQUE_SUFFIX);
		//String pid = iwc.getParameter("usrch_search_pid" + UNIQUE_SUFFIX);
		//String uID = iwc.getParameter("usrch_user_id_" + UNIQUE_SUFFIX);
		//String uID2 = iwc.getParameter(uniqueUserSearchParam);

		//String act = iwc.getParameter(PARAM_ACTION);
		//String pres = iwc.getParameter(PARAM_PRESENTATION);

		//String uniqueStr = SearchUserModule.getUniqueUserParameterName(UNIQUE_SUFFIX);
		
		
		// Process search to get child (User object)
		processSearch(iwc);

		// Perform actions according the _action input parameter
		switch (_action) {
			case ACTION_PLACE_PUPIL :
				storePlacement(iwc);
				break;
			case ACTION_REMOVE_SESSION_CHILD :
				removeSessionChild(iwc);
				break;

		}

		if (errMsgMid == null && (child == null || _presentation == PRESENTATION_SEARCH_FORM
				|| iwc.isParameterSet(SearchUserModule.SEARCH_COMMITTED + UNIQUE_SUFFIX))) {
			// show search form
			setMainTableContent(getSearchTable(iwc));
		} else {
			// show place pupil form
			setMainTableContent(getPlacementTable(iwc));
		}
		add(form);
	}

	private Table getMainTable() {
		mainTable = new Table();
		mainTable.setBorder(0);
		mainTable.setCellpadding(0);
		mainTable.setCellspacing(0);
		int col = 1;
		int row = 1;
		
		//  *** WINDOW HEADING ***
		mainTable.add(
			getLocalizedSmallHeader(KEY_WINDOW_HEADING, "Central placing of pupil"), col, row);
		mainTable.setColor(col, row, getHeaderColor());
		mainTable.setAlignment(col, row, Table.HORIZONTAL_ALIGN_CENTER);
		mainTable.setRowVerticalAlignment(row, Table.VERTICAL_ALIGN_MIDDLE);
		mainTable.setRowHeight(row, "20");

		return mainTable;
	}

	private void setMainTableContent(PresentationObject obj) {
		int col = 1;
		int row = 2;
		mainTable.add(obj, col, row);
	}

	public Table getSearchTable(IWContext iwc) {
		// *** Search Table *** START - the uppermost table
		Table table = new Table();
		table.setBorder(0);
		table.setCellpadding(0);
		table.setCellspacing(0);

		int col = 1;
		int row = 1;

		// *** HEADING Search pupil ***
		Image space1 = (Image) transGIF.clone();
		space1.setWidth(6);
		//space1.setHeight(20);
		table.add(space1, col, row);
		Text pupilTxt = new Text(localize(KEY_SEARCH_PUPIL_HEADING, "Search pupil"));
		pupilTxt.setFontStyle(STYLE_UNDERLINED_SMALL_HEADER);
		table.add(pupilTxt, col++, row);
		table.setRowHeight(row, "20");
		table.setRowVerticalAlignment(row, Table.VERTICAL_ALIGN_BOTTOM);
		col = 1;
		row++;
		// User search module - configure and add 
		table.add(getSearchUserModule(iwc), col++, row);
		
		return table;
	}

	public Table getPlacementTable(IWContext iwc) {
		// *** Search Table *** START - the uppermost table
		Table table = new Table();
		table.setBorder(0);
		table.setCellpadding(2);
		table.setCellspacing(0);
		transGIF.setHeight("1");
		transGIF.setWidth("1");

		int row = 1;
		int col = 1;
		// add empty space row
		table.add(transGIF, col++, row);
		table.add(transGIF, col++, row);
		table.add(transGIF, col++, row);
		table.add(transGIF, col++, row);
		table.add(transGIF, col++, row);
		// Set COLUMN WIDTH for column 1 to 5
		table.setWidth(1, row, "1");
		table.setWidth(2, row, "70");
		table.setWidth(3, row, "70");
		table.setWidth(4, row, "70");
		table.setWidth(5, row, "104");

		row++;
		col = 1;

		// *** HEADING Pupil ***
		Text pupilTxt = new Text(localize(KEY_PUPIL_HEADING, "Pupil"));
		pupilTxt.setFontStyle(STYLE_UNDERLINED_SMALL_HEADER);
		table.add(pupilTxt, col++, row);
		table.setRowHeight(row, "20");
		table.setRowVerticalAlignment(row, Table.VERTICAL_ALIGN_BOTTOM);

		// BUTTON Search new child 
		table.add(new SubmitButton(iwrb.getLocalizedImageButton(KEY_BUTTON_SEARCH, "Search"),
							PARAM_PRESENTATION, String.valueOf(PRESENTATION_SEARCH_FORM)), 5, row);
		table.setAlignment(5,row, Table.HORIZONTAL_ALIGN_RIGHT);
		row++;
		col = 1;
		// Personal Id Number
		table.add(getSmallHeader(localize(KEY_PERSONAL_ID_LABEL, "Personal id: ")), col++, row);
		if (child != null)
			table.add(getSmallText(child.getPersonalID()), col++, row);
		row++;
		col = 1;
		// Last Name
		table.add(getSmallHeader(localize(KEY_LAST_NAME_LABEL, "Last name: ")), col++, row);

		Table nameTable = new Table();
		col = 1;
		nameTable.setCellpadding(0);
		nameTable.setCellspacing(0);
		if (child != null)
			nameTable.add(getSmallText(child.getLastName()), col++, 1);
		// First Name       
		nameTable.add(getSmallHeader(localize(KEY_FIRST_NAME_LABEL, "First name: ")), col++, 1);
		if (child != null)
			nameTable.add(getSmallText(child.getFirstName()), col++, 1);
		nameTable.setWidth(1, 1, "100");
		nameTable.setWidth(2, 1, "100");
		nameTable.setWidth(3, 1, "100");
		table.add(nameTable, 2, row);
		table.mergeCells(2, row, 5, row);
		row++;
		col = 1;

		// Address and Phone
		table.add(getSmallHeader(localize(KEY_ADDRESS_LABEL, "Address: ")), col++, row);
		row++;
		col = 1;
		table.add(getSmallHeader(localize(KEY_PHONE_LABEL, "Phone: ")), col++, row);
		if (child != null) {
			try {
				// child address
				if (address != null) {
					address = getUserBusiness(iwc).getUsersMainAddress(child);
					StringBuffer aBuf = new StringBuffer(address.getStreetAddress());
					aBuf.append(", ");
					aBuf.append(address.getPostalCode().getPostalAddress());
					row--;
					table.add(getSmallText(aBuf.toString()), col, row);
					row++;
				}
				// Get child phones
				Collection phones = child.getPhones();
				int i = 0;
				int phonesSize = phones.size();
				StringBuffer pBuf = new StringBuffer();
				for (Iterator iter = phones.iterator(); iter.hasNext(); i++) {
					Phone phone = (Phone) iter.next();
					pBuf.append(phone.getNumber());
					if (i < phonesSize - 1)
						pBuf.append(", ");
				}
				pBuf.append("&nbsp;");
				table.add(getSmallText(pBuf.toString()), col, row);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		row++;
		col = 1;
		
		// *** HEADING Current placment ***
		Text currentPlacementTxt =
			new Text(localize(KEY_CURRENT_PLACEMENT_HEADING, "Current placement"));
		currentPlacementTxt.setFontStyle(STYLE_UNDERLINED_SMALL_HEADER);
		table.add(currentPlacementTxt, col, row);
		table.setRowHeight(row, "20");
		table.setRowVerticalAlignment(row, Table.VERTICAL_ALIGN_BOTTOM);
		table.mergeCells(col, row, col+1, row);	
		// BUTTON Placement history 
		col = 5;
		table.add(new SubmitButton(iwrb.getLocalizedImageButton(
												KEY_BUTTON_PLACEMENT_HISTORY, "Placement history")), col, row);
				//PARAM_PRESENTATION, String.valueOf(PRESENTATION_SEARCH_FORM)), 5, row);
		table.setAlignment(5,row, Table.HORIZONTAL_ALIGN_RIGHT);
		row++;
		col = 1;
		// Activity
		table.add(getSmallHeader(localize(KEY_ACTIVITY_LABEL, "Activity: ")), col, row);
		// BUTTON Pupil overview 
		table.add(new SubmitButton(iwrb.getLocalizedImageButton(
												KEY_BUTTON_PUPIL_OVERVIEW, "Pupil overview")), 5, row);
				//PARAM_PRESENTATION, String.valueOf(PRESENTATION_SEARCH_FORM)), 5, row);
		table.setAlignment(5, row, Table.HORIZONTAL_ALIGN_RIGHT);
		row++;
		col = 1;
		// Placement
		table.add(getSmallHeader(localize(KEY_PLACEMENT_LABEL, "Placement: ")), col, row);
		// BUTTON Regular payment 
		table.add(new SubmitButton(iwrb.getLocalizedImageButton(
												KEY_BUTTON_REGULAR_PAYMENT, "Regular payment")), 5, row);
				//PARAM_PRESENTATION, String.valueOf(PRESENTATION_SEARCH_FORM)), 5, row);
		table.setAlignment(5, row, Table.HORIZONTAL_ALIGN_RIGHT);
		row++;
		col = 1;
		// Resources
		table.add(getSmallHeader(localize(KEY_RESOURCES_LABEL, "Resources: ")), col, row);
		row++;
		col = 1;		
		// Contract
		table.add(getSmallHeader(localize(KEY_CONTRACT_LABEL, "Contract: ")), col, row);
		// BUTTON Contract history 
		table.add(new SubmitButton(iwrb.getLocalizedImageButton(
												KEY_BUTTON_CONTRACT_HISTORY, "Contract history")), 5, row);
				//PARAM_PRESENTATION, String.valueOf(PRESENTATION_SEARCH_FORM)), 5, row);
		table.setAlignment(5, row, Table.HORIZONTAL_ALIGN_RIGHT);

		// Values - Current placement
		if (child != null) {
			try {
				SchoolClassMember placement = getCentralPlacementBusiness(iwc).
																				getCurrentSchoolClassMembership(child, iwc);
				if (placement != null) {
					row--; row--;row--;
					col = 2;
					// Activity

					//table.add(placement.getSchoolClass().getSchoolType().getName(), col, row);
					row++;

					// Placement
					StringBuffer buf =
						new StringBuffer(placement.getSchoolClass().getSchool().getName());
					buf.append(", " + localize(KEY_SCHOOL_YEAR, "school year") + " "
							+ placement.getSchoolClass().getSchoolYear().getName() + ", "
							+ localize(KEY_SCHOOL_GROUP, "group") + " "
							+ placement.getSchoolClass().getSchoolClassName());
					table.add(getSmallText(buf.toString()), col, row);
					table.mergeCells(col, row, col+2, row);
					row++;

					// Resources
					row++;

				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			// Contract	  
			try {
				ChildCareContract contract =
					getChildCareBusiness(iwc).getValidContractByChild(
						((Integer) child.getPrimaryKey()).intValue());
				if (contract != null) {
					table.add(getSmallText(localize(KEY_CONTRACT_YES, "Yes")), col, row);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		row++;
		col = 1;

		// ERROR MSG - errMsgMid
		if (errMsgMid != null) {
			table.add(getSmallErrorText(errMsgMid), col, row);
			table.mergeCells(col, row, col+2, row);
			row++;
		}

		// *** HEADING New placement *** 
		Text newPlacementTxt = new Text(localize(KEY_NEW_PLACEMENT_HEADING, "New placement"));
		newPlacementTxt.setFontStyle(STYLE_UNDERLINED_SMALL_HEADER);
		table.add(newPlacementTxt, col++, row);
		table.setRowHeight(row, "20");
		table.setRowVerticalAlignment(row, Table.VERTICAL_ALIGN_BOTTOM);
		row++;
		col = 1;
		// School Category
		table.add(getSmallHeader(localize(KEY_MAIN_ACTIVITY_LABEL, "Main activity: ")), col++, row);
		table.add(getSchoolCategoriesDropdown(iwc), col++, row);
		row++;
		col = 1;
		// Provider labels
		table.add(getSmallHeader(localize(KEY_PROVIDER_LABEL, "Provider: ")), col++, row);
		table.add(getProvidersDropdown(iwc), col++, row);
		row++;
		col = 1;
		table.add(getSmallHeader(localize(KEY_ADDRESS_LABEL, "Address: ")), col++, row);
		col++; col++;
		table.add(getSmallHeader(localize(KEY_COMMUNE_LABEL, "Commune: ")), col, row);
		table.mergeCells(col, row, col+1, row);
		row++;
		col = 1;
		table.add(getSmallHeader(localize(KEY_PHONE_LABEL, "Phone: ")), col++, row);
		row++;
		col = 1;
		table.add(getSmallHeader(localize(KEY_ADMIN_LABEL, "Administration: ")), col++, row);
		// Provider values
		School school = getCurrentProvider(iwc);
		if (school != null && iwc.isParameterSet(PARAM_PROVIDER)) {
			row--;row--;
			col = 2;
			// School Address value
			table.add(school.getSchoolAddress()+", "+school.getSchoolZipCode()+" "
																						+school.getSchoolZipArea(), col, row);
			table.mergeCells(col, row, col+1, row);
			col++; col++;
			// Commune value
			table.add(getCommuneName(school), col, row);
			row++;
			col = 2;
			// Phone value
			table.add(school.getSchoolPhone(), col, row);
			row++;
			// Administrator value
			table.add(school.getCentralizedAdministration() ? localize(KEY_CENTRAL_ADMIN, "Central") :
																	localize(KEY_PROVIDER_ADMIN, "Provider"), col, row);
		}
		table.add(getSmallHeader(localize(KEY_PAYMENT_BY_INVOICE_LABEL, "Payment by invoice: ")),
																																++col, row);
		table.add(getYesNoDropdown(PARAM_PAYMENT_BY_INVOICE), ++col, row);
		
		row++;
		col = 2;
		table.add(transGIF, col, row); // EMPTY SPACE ROW
		table.setRowHeight(row, "10");
		row++;
		col = 1;
		// Activity input
		table.add(getSmallHeader(localize(KEY_ACTIVITY_LABEL, "Activity:")), col++, row);
		table.add(getActivitiesDropdown(iwc), col++, row);
		table.add(
			getSmallHeader(localize(KEY_PLACEMENT_PARAGRAPH_LABEL, "Placement paragraph: ")),
			col++, row);
		table.add(getPlacementParagraphTextInput(), col, row);
		table.mergeCells(col, row, col+1, row);
		row++;
		col = 1;
		// School Year input
		table.add(getSmallHeader(localize(KEY_SCHOOL_YEAR_LABEL, "School year: ")), col++, row);
		table.add(getSchoolYearsDropdown(iwc), col++, row);
		// School Group input
		table.add(getSmallHeader(localize(KEY_SCHOOL_GROUP_LABEL, "School group: ")), col++, row);
		table.add(getSchoolGroups(iwc), col++, row);
		// BUTTON New group
		table.add(new SubmitButton(iwrb.getLocalizedImageButton(
																KEY_BUTTON_NEW_GROUP, "New group")), 5, row);
				//PARAM_PRESENTATION, String.valueOf(PRESENTATION_SEARCH_FORM)), 5, row);
		table.setAlignment(5, row, Table.HORIZONTAL_ALIGN_RIGHT);
		row++;
		col = 1;
		// Study Path input
		table.add(getSmallHeader(localize(KEY_STUDY_PATH_LABEL, "Study path: ")), col++, row);
		table.add(getStudyPathsDropdown(iwc), col++, row);
		row++;
		col = 1;
		table.add(transGIF, col, row); // EMPTY SPACE ROW
		table.setRowHeight(row, "10");
		row++;
		col = 1;
		// Resource
		table.add(getSmallHeader(localize(KEY_RESOURCE_LABEL, "Resource: ")), col++, row);
		//  Resource input checkboxes
		if (iwc.isParameterSet(PARAM_ACTIVITY) && iwc.isParameterSet(PARAM_SCHOOL_YEAR)) {
			try {
				Collection rscColl = getResourceBusiness(iwc).getAssignableResourcesByYearAndType(
						iwc.getParameter(PARAM_SCHOOL_YEAR), iwc.getParameter(PARAM_ACTIVITY));
				CheckBox typeRscBox = new CheckBox(PARAM_RESOURCES);
				Integer primaryKey;
				Iterator loop = rscColl.iterator();
				while (loop.hasNext()) {
					col = 2;
					Resource rsc = (Resource) loop.next();
					CheckBox cBox = (CheckBox) typeRscBox.clone();
					primaryKey = (Integer) rsc.getPrimaryKey();
					cBox.setValue(primaryKey.intValue());

					// Set related school types to checked
					/* if (theRsc != null) {
					   Map typeMap = busyBean.getRelatedSchoolTypes(theRsc);
					   Set typeKeys = typeMap.keySet();
					   if (typeKeys.contains(primaryKey)) {
					     cBox.setChecked(true);
					   }
					 } */
					table.add(cBox, col++, row);
					table.add(getSmallText(rsc.getResourceName()), col++, row);
					row++;
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}

		row++;
		col = 1;		
		// START row table
		Table rowTable = new Table();
		rowTable.setCellpadding(0);
		rowTable.setCellspacing(0);
		int tmpRow = 1;
		int tmpCol = 1;
		rowTable.setBorder(0);
		
		// Payment by agreement
		rowTable.add(
			getSmallHeader(localize(KEY_PAYMENT_BY_AGREEMENT_LABEL, "Payment by agreement: ")),
			tmpCol++, tmpRow);
		rowTable.add(getYesNoDropdown(PARAM_PAYMENT_BY_AGREEMENT), tmpCol, tmpRow);
		rowTable.setWidth(tmpCol++, tmpRow, 100);
		// Invoice interval
		rowTable.add(getSmallHeader(localize(KEY_INVOICE_INTERVAL_LABEL, "Invoice interval: ")),
																													tmpCol++, tmpRow);
		rowTable.add(getInvoiceIntervalDropdown(iwc), tmpCol++, tmpRow);
		// BUTTON Regular payment 
		rowTable.add(new SubmitButton(
				iwrb.getLocalizedImageButton(KEY_BUTTON_REGULAR_PAYMENT, "Regular payment")),
																														tmpCol, tmpRow);
																											
		//		PARAM_PRESENTATION, String.valueOf(PRESENTATION_SEARCH_FORM)), tmpCol, tmpRow);
		rowTable.setAlignment(tmpCol, tmpRow, Table.HORIZONTAL_ALIGN_RIGHT);
		rowTable.setWidthAndHeightToHundredPercent();
		// END row table

		table.add(rowTable, col, row);
		table.mergeCells(col, row, col + 4, row);
		row++;
		col = 1;
		
		// Placement date
		table.add(
			getSmallHeader(localize(KEY_PLACEMENT_DATE_LABEL, "Placement date: ")), col++, row);
		table.add(getPlacementDateInput(iwc), col, row);
		table.mergeCells(col, row, col + 2, row);
		col = 1;
		row++;

		// empty space row
		Image trans1 = (Image) transGIF.clone();
		table.add(trans1, col, row);

		col = 1;
		row++;		
		// BOTTOM BUTTONS
			// Place
		table.add(new SubmitButton(iwrb.getLocalizedImageButton(KEY_BUTTON_PLACE, "Place"), 
								PARAM_ACTION, String.valueOf(ACTION_PLACE_PUPIL)), col++, row);
			// Cancel		
		table.add(new SubmitButton(iwrb.getLocalizedImageButton(KEY_BUTTON_CANCEL, "Cancel"),
						PARAM_PRESENTATION, String.valueOf(PRESENTATION_SEARCH_FORM)), col++, row);


		return table;
	}

	/**
	 * Process the search for a pupil. If the uniqueUserSearchParam is set the User is fetched and put in 
	 * session for further use. So here the User session bean is renew if uniqueuserSearchParam is set.
	 * @param iwc Request object context
	 */
	public void processSearch(IWContext iwc) {
		if (iwc.isParameterSet(uniqueUserSearchParam)) {
			Integer userID = Integer.valueOf(iwc.getParameter(uniqueUserSearchParam));
			try {
				child = getUserBusiness(iwc).getUser(userID);
				// Put User object in session
				iwc.getSession().setAttribute(SESSION_KEY_CHILD, child);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		} else {
			child = (User) iwc.getSession().getAttribute(SESSION_KEY_CHILD);
		}
	}

	private SearchUserModule getSearchUserModule(IWContext iwc) {
		SearchUserModule searcher = new SearchUserModule();
		searcher.setShowMiddleNameInSearch(true);
		searcher.setOwnFormContainer(false);
		searcher.setUniqueIdentifier(UNIQUE_SUFFIX);
		searcher.setSkipResultsForOneFound(false);
		searcher.setHeaderFontStyleName(getStyleName(STYLENAME_SMALL_HEADER));
		searcher.setButtonStyleName(getStyleName(STYLENAME_INTERFACE_BUTTON));
		searcher.setPersonalIDLength(12);
		searcher.setFirstNameLength(15);
		searcher.setMiddleNameLength(15);
		searcher.setLastNameLength(20);

		String prmChild = SearchUserModule.getUniqueUserParameterName("child");
		if (iwc.isParameterSet(prmChild)) {
			searcher.maintainParameter(new Parameter(prmChild, iwc.getParameter(prmChild)));
		}

		return searcher;
	}

	private DropdownMenu getSchoolCategoriesDropdown(IWContext iwc) {
		// Get dropdown for school categories
		DropdownMenu schoolCats = new DropdownMenu(PARAM_SCHOOL_CATEGORY);
		schoolCats.setToSubmit(true);
		schoolCats.addMenuElement("-1", localize(KEY_DROPDOWN_CHOSE, "- Chose -"));
		try {
			SchoolCategoryHome schCatHome = getSchoolBusiness(iwc).getSchoolCategoryHome();
			SchoolCategory elementary = schCatHome.findElementarySchoolCategory();
			SchoolCategory highSchool = schCatHome.findHighSchoolCategory();
			schoolCats.addMenuElement(
				(String) elementary.getPrimaryKey(),
				localize(elementary.getLocalizedKey(), "Elementary school"));
			schoolCats.addMenuElement(
				(String) highSchool.getPrimaryKey(),
				localize(highSchool.getLocalizedKey(), "High School"));
			if (iwc.isParameterSet(PARAM_SCHOOL_CATEGORY))
				schoolCats.setSelectedElement(iwc.getParameter(PARAM_SCHOOL_CATEGORY));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return schoolCats;
	}

	private DropdownMenu getProvidersDropdown(IWContext iwc) {
		// Get dropdown for providers
		DropdownMenu providers = new DropdownMenu(PARAM_PROVIDER);
		providers.setToSubmit(true);
		providers.addMenuElement("-1", localize(KEY_DROPDOWN_CHOSE, "- Chose -"));
		try {
			// Get school category from topmost dropdown
			if (iwc.isParameterSet(PARAM_SCHOOL_CATEGORY)) {
				// Get schooltypes in category
				Collection schTypes =
					getSchoolBusiness(iwc).findAllSchoolTypesInCategory(
						iwc.getParameter(PARAM_SCHOOL_CATEGORY));
				// Get schools in school types of chosen school category
				Collection schools = getSchoolBusiness(iwc).findAllSchoolsByType(schTypes);
				// Fill upp dropdown with schools
				for (Iterator iter = schools.iterator(); iter.hasNext();) {
					School tmpSchool = (School) iter.next();
					int schoolID = ((Integer) tmpSchool.getPrimaryKey()).intValue();
					providers.addMenuElement(schoolID, tmpSchool.getSchoolName());
				}
				if (iwc.isParameterSet(PARAM_PROVIDER))
					providers.setSelectedElement(iwc.getParameter(PARAM_PROVIDER));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return providers;
	}

	private DropdownMenu getActivitiesDropdown(IWContext iwc) {
		DropdownMenu activities = new DropdownMenu(PARAM_ACTIVITY);
		activities.setToSubmit(true);
		activities.addMenuElement("-1", localize(KEY_DROPDOWN_CHOSE, "- Chose -"));
		if (iwc.isParameterSet(PARAM_PROVIDER)) {
			try {
				School school =
					getSchoolBusiness(iwc).getSchool(new Integer(iwc.getParameter(PARAM_PROVIDER)));
				if (school != null) {
					Collection schTypes = school.findRelatedSchoolTypes();
					for (Iterator iter = schTypes.iterator(); iter.hasNext();) {
						SchoolType type = (SchoolType) iter.next();
						int typeID = ((Integer) type.getPrimaryKey()).intValue();
						activities.addMenuElement(typeID, type.getName());
					}
					if (iwc.isParameterSet(PARAM_ACTIVITY)) {
						activities.setSelectedElement(iwc.getParameter(PARAM_ACTIVITY));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		return activities;
	}

	private DropdownMenu getSchoolYearsDropdown(IWContext iwc) {
		DropdownMenu years = new DropdownMenu(PARAM_SCHOOL_YEAR);
		years.setToSubmit(true);
		years.addMenuElement("-1", localize(KEY_DROPDOWN_CHOSE, "- Chose -"));
		if (iwc.isParameterSet(PARAM_ACTIVITY)) {
			try {
				if (iwc.isParameterSet(PARAM_PROVIDER)) {
					String providerIdStr = iwc.getParameter(PARAM_PROVIDER);
					School school = getSchoolBusiness(iwc).getSchool(new Integer(providerIdStr));
					if (school != null) {
						Collection yearColl = school.findRelatedSchoolYears();
						if (yearColl != null) {
							for (Iterator iter = yearColl.iterator(); iter.hasNext();) {
								SchoolYear year = (SchoolYear) iter.next();
								int paramTypeID =
									Integer.parseInt(iwc.getParameter(PARAM_ACTIVITY));
								if (year.getSchoolTypeId() == paramTypeID) {
									int yearID = ((Integer) year.getPrimaryKey()).intValue();
									years.addMenuElement(yearID, year.getName());
								}
							}
							if (iwc.isParameterSet(PARAM_SCHOOL_YEAR)) {
								years.setSelectedElement(iwc.getParameter(PARAM_SCHOOL_YEAR));
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return years;
	}

	private DropdownMenu getSchoolGroups(IWContext iwc) {
		DropdownMenu groups = new DropdownMenu(PARAM_SCHOOL_GROUP);
		groups.setToSubmit(true);
		groups.addMenuElement("-1", localize(KEY_DROPDOWN_CHOSE, "- Chose -"));
		if (iwc.isParameterSet(PARAM_PROVIDER) && iwc.isParameterSet(PARAM_SCHOOL_YEAR)) {
			int schoolID = Integer.parseInt(iwc.getParameter(PARAM_PROVIDER));
			int yearID = Integer.parseInt(iwc.getParameter(PARAM_SCHOOL_YEAR));
			try {
				SchoolSeason currentSeason = getSchoolChoiceBusiness(iwc).getCurrentSeason();
				int seasonID = ((Integer) currentSeason.getPrimaryKey()).intValue();
				Collection groupColl =
					getSchoolBusiness(iwc).findSchoolClassesBySchoolAndSeasonAndYear(
																										schoolID, seasonID, yearID);
				if (groupColl != null) {
					for (Iterator iter = groupColl.iterator(); iter.hasNext();) {
						SchoolClass group = (SchoolClass) iter.next();
						int groupID = ((Integer) group.getPrimaryKey()).intValue();
						groups.addMenuElement(groupID, group.getName());
					}
					if (iwc.isParameterSet(PARAM_SCHOOL_GROUP))
						groups.setSelectedElement(iwc.getParameter(PARAM_SCHOOL_GROUP));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return groups;
	}
	
	private DropdownMenu getYesNoDropdown(String param) {
		DropdownMenu yesNo = new DropdownMenu(param);
		yesNo.addMenuElement("-1", localize(KEY_DROPDOWN_CHOSE, "- Chose -"));
		yesNo.addMenuElement(KEY_DROPDOWN_NO, localize(KEY_DROPDOWN_NO, "No"));
		yesNo.addMenuElement(KEY_DROPDOWN_YES, localize(KEY_DROPDOWN_YES, "Yes"));		
		return yesNo;
	}
	
	private DropdownMenu getInvoiceIntervalDropdown(IWContext iwc) {
		DropdownMenu drop = new DropdownMenu(PARAM_INVOICE_INTERVAL);
		drop.addMenuElement("-1", localize(KEY_DROPDOWN_CHOSE, "- Chose -"));
		try {
			Collection intervals = getSchoolBusiness(iwc).findAllSchClMemberInvoiceIntervalTypes();
			if (intervals != null) {
				for (Iterator iter = intervals.iterator(); iter.hasNext();) {
					String intervalKey = (String) iter.next();
					drop.addMenuElement(intervalKey, localize(intervalKey, intervalKey));					
				}
			}			
		} catch (RemoteException re) {
			re.printStackTrace();
		}

		return drop;		
	}
	
	private DropdownMenu getStudyPathsDropdown(IWContext iwc) {
		DropdownMenu studyPath = new DropdownMenu(PARAM_STUDY_PATH);
		studyPath.addMenuElement("-1", localize(KEY_DROPDOWN_CHOSE, "- Chose -"));

		String schoolIdStr = iwc.getParameter(PARAM_PROVIDER);
		String schTypeIdStr = iwc.getParameter(PARAM_ACTIVITY);		
		if (schoolIdStr != null && schTypeIdStr != null) {
			Integer schTypePK = new Integer(schTypeIdStr);
			try {
				School school = getSchoolBusiness(iwc).getSchool(new Integer(schoolIdStr));
				Collection coll = getStudyPathHome().findStudyPaths(school, schTypePK);
				for (Iterator iter = coll.iterator(); iter.hasNext();) {
					SchoolStudyPath element = (SchoolStudyPath) iter.next();
					int studyPathID = ((Integer) element.getPrimaryKey()).intValue();
					studyPath.addMenuElement(studyPathID, element.getCode());
				}		
			} catch (Exception e) {
				e.printStackTrace();
			}
		}	
		return studyPath;
	}
	
	private TextInput getPlacementParagraphTextInput() {
		TextInput txt = new TextInput(PARAM_PLACEMENT_PARAGRAPH);
		txt.setLength(25);
		return txt;
	}
	

	private DateInput getPlacementDateInput(IWContext iwc) {
		//DateInput dInput = new DateInput(PARAM_PLACEMENT_DATE);
		DateInput dInput = (DateInput) getStyledInterface(
																		new DateInput(PARAM_PLACEMENT_DATE, true));
		IWTimestamp today = IWTimestamp.RightNow();
		today.setAsDate();
		java.sql.Date todayDate = today.getDate();
		dInput.setToDisplayDayLast(true);
		if (iwc.isParameterSet(PARAM_PLACEMENT_DATE)) {
			IWTimestamp placeStamp = new IWTimestamp(iwc.getParameter(PARAM_PLACEMENT_DATE));
			java.sql.Date placeDate = placeStamp.getDate();
			dInput.setDate(placeDate);
		} else {
			dInput.setDate(todayDate);
		}

		return dInput;
	}
	
	/**
	 * Returns a current School(Provider) object if PARAM_PROVIDER is set
	 * @param iwc
	 * @return School 
	 */
	private School getCurrentProvider(IWContext iwc) {
		School school = null;
		if (iwc.isParameterSet(PARAM_PROVIDER)) {
			try {
				school = getSchoolBusiness(iwc).
													getSchool(new Integer(iwc.getParameter(PARAM_PROVIDER)));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return school;
	}

	
	/**
	 * Gets the name of a Commune. Returns empty String if Commune is null
	 * 
	 * @param iwc
	 * @param communePK
	 * @return
	 */
	public String getCommuneName(School school) {
		String comName = "";
		Commune commune = school.getCommune();
		if (commune != null)
			comName = commune.getCommuneName();
			
		return comName;
	}

	/**
	 * Parse input request parameters 
	 * @param iwc Request object context
	 */
	private void parse(IWContext iwc) {
		if (iwc.isParameterSet(PARAM_ACTION)) {
			String actionStr = iwc.getParameter(PARAM_ACTION);
			_action = Integer.parseInt(actionStr);

		}
		if (iwc.isParameterSet(PARAM_PRESENTATION)) {
			String presStr = iwc.getParameter(PARAM_PRESENTATION);
			_presentation = Integer.parseInt(presStr);
		}
	}

	// *** ACTIONS ***
	private SchoolClassMember storePlacement(IWContext iwc) {
		SchoolClassMember mbr = null;
		int childID =  -1;
		if (child != null)
			childID = ((Integer) child.getPrimaryKey()).intValue();
		try {
			mbr = getCentralPlacementBusiness(iwc).storeSchoolClassMember(iwc, childID);			
		} catch (CentralPlacementException cpe) {
			errMsgMid = localize(cpe.getKey(), cpe.getDefTrans());
		} catch (RemoteException re) {
			re.printStackTrace();
		}
														
		return mbr;																									
	}

	protected void removeSessionChild(IWContext iwc) {
		iwc.getSession().removeAttribute(SESSION_KEY_CHILD);
		child = null;
	}
	
	private CentralPlacementBusinessBean getCentralPlacementBusiness(IWContext iwc) 
																										throws RemoteException {
		return (CentralPlacementBusinessBean) 
					IBOLookup.getServiceInstance(iwc, CentralPlacementBusinessBean.class);
	}


	private CommuneUserBusiness getUserBusiness(IWContext iwc) throws RemoteException {
		return (CommuneUserBusiness) 
										IBOLookup.getServiceInstance(iwc, CommuneUserBusiness.class);
	}

	private SchoolBusiness getSchoolBusiness(IWContext iwc) throws RemoteException {
		return (SchoolBusiness) IBOLookup.getServiceInstance(iwc, SchoolBusiness.class);
	}

	private SchoolChoiceBusiness getSchoolChoiceBusiness(IWContext iwc) throws RemoteException {
		return (SchoolChoiceBusiness) IBOLookup.getServiceInstance(iwc, SchoolChoiceBusiness.class);
	}

/*	private SchoolCommuneBusiness getSchoolCommuneBusiness(IWContext iwc) 
																										throws RemoteException {
		return (SchoolCommuneBusiness) IBOLookup.getServiceInstance(iwc, 
																								SchoolCommuneBusiness.class);
	}
*/
	private ChildCareBusinessBean getChildCareBusiness(IWContext iwc) throws RemoteException {
		return (ChildCareBusinessBean)
											 IBOLookup.getServiceInstance(iwc, ChildCareBusinessBean.class);
	}

	private ResourceBusiness getResourceBusiness(IWContext iwc) throws RemoteException {
		return (ResourceBusiness) IBOLookup.getServiceInstance(iwc, ResourceBusiness.class);
	}
	
	public SchoolStudyPathHome getStudyPathHome() throws java.rmi.RemoteException {
		return (SchoolStudyPathHome) IDOLookup.getHome(SchoolStudyPath.class);
	}


}
