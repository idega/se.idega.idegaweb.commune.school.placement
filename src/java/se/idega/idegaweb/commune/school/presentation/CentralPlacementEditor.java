/*
 * Created on 2003-sep-23
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package se.idega.idegaweb.commune.school.presentation;

import is.idega.block.family.business.NoParentFound;

import java.rmi.RemoteException;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Iterator;

import javax.ejb.EJBException;
import javax.ejb.FinderException;

import se.idega.idegaweb.commune.accounting.business.AccountingSession;
import se.idega.idegaweb.commune.accounting.invoice.presentation.RegularPaymentEntriesList;
import se.idega.idegaweb.commune.accounting.resource.business.ResourceBusiness;
import se.idega.idegaweb.commune.accounting.resource.data.Resource;
import se.idega.idegaweb.commune.accounting.resource.data.ResourceClassMember;
import se.idega.idegaweb.commune.accounting.school.data.Provider;
import se.idega.idegaweb.commune.business.CommuneUserBusiness;
import se.idega.idegaweb.commune.childcare.business.ChildCareBusiness;
import se.idega.idegaweb.commune.childcare.data.ChildCareContract;
import se.idega.idegaweb.commune.childcare.presentation.ChildCareChildContracts;
import se.idega.idegaweb.commune.message.business.MessageBusiness;
import se.idega.idegaweb.commune.provider.business.ProviderSession;
import se.idega.idegaweb.commune.provider.presentation.SchoolGroupEditor;
import se.idega.idegaweb.commune.provider.presentation.SchoolGroupEditorAdmin;
import se.idega.idegaweb.commune.school.business.CentralPlacementBusiness;
import se.idega.idegaweb.commune.school.business.CentralPlacementException;
import se.idega.idegaweb.commune.school.business.SchoolChoiceBusiness;
import se.idega.idegaweb.commune.school.business.SchoolCommuneSessionBean;
import se.idega.idegaweb.commune.school.event.SchoolEventListener;

import com.idega.block.school.business.SchoolBusiness;
import com.idega.block.school.data.School;
import com.idega.block.school.data.SchoolCategory;
import com.idega.block.school.data.SchoolCategoryHome;
import com.idega.block.school.data.SchoolClass;
import com.idega.block.school.data.SchoolClassMember;
import com.idega.block.school.data.SchoolClassMemberHome;
import com.idega.block.school.data.SchoolSeason;
import com.idega.block.school.data.SchoolSeasonHome;
import com.idega.block.school.data.SchoolStudyPath;
import com.idega.block.school.data.SchoolStudyPathHome;
import com.idega.block.school.data.SchoolType;
import com.idega.block.school.data.SchoolUser;
import com.idega.block.school.data.SchoolYear;
import com.idega.business.IBOLookup;
import com.idega.business.IBORuntimeException;
import com.idega.core.contact.data.Phone;
import com.idega.core.localisation.data.ICLanguage;
import com.idega.core.localisation.data.ICLanguageHome;
import com.idega.core.location.data.Address;
import com.idega.core.location.data.Commune;
import com.idega.data.IDOLookup;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.IWUserContext;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.Table;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.CheckBox;
import com.idega.presentation.ui.DateInput;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.HiddenInput;
import com.idega.presentation.ui.Parameter;
import com.idega.presentation.ui.SubmitButton;
import com.idega.presentation.ui.TextInput;
import com.idega.user.data.User;
import com.idega.util.IWTimestamp;

/**
 * @author <br><a href="mailto:gobom@wmdata.com">Göran Borgman</a><br>
 * Last modified: $Date: 2004/08/27 19:22:53 $ by $Author: joakim $
 * @version $Revision: 1.80 $
 */
public class CentralPlacementEditor extends SchoolCommuneBlock {
	// *** Localization keys ***
	private static final String KP = "central_placement_editor.";
	private static final String KEY_WINDOW_HEADING = KP + "window_heading";
	private static final String KEY_SEARCH_PUPIL_HEADING = KP + "search_pupil_heading";
	private static final String KEY_PUPIL_HEADING = KP + "pupil_heading";
	private static final String KEY_LATEST_PLACEMENT_HEADING = KP + "latest_placement_heading";
	private static final String KEY_NEW_PLACEMENT_HEADING = KP + "new_placement_heading";
	private static final String KEY_STORED_PLACEMENT_HEADING = KP + "stored_placement_heading";
	private static final String KEY_MSG_OF_NEW_PLACEMENT_HEADING = KP 
																							+ "message of new placement heading";
	private static final String KEY_SEARCH_NO_PUPIL_FOUND = KP + "search_no_pupil_found";
		// Label keys
	private static final String KEY_PERSONAL_ID_LABEL = KP + "personal_id_label";
	private static final String KEY_FIRST_NAME_LABEL = KP + "first_name_label";
	private static final String KEY_LAST_NAME_LABEL = KP + "last_name_label";
	private static final String KEY_ADDRESS_LABEL = KP + "address_label";
	private static final String KEY_PHONE_LABEL = KP + "telephone_label";
	private static final String KEY_SCHOOL_TYPE_LABEL = KP + "school_type_label";
	private static final String KEY_PLACEMENT_LABEL = KP + "placement_label";
	private static final String KEY_RESOURCES_LABEL = KP + "resources_label";
	private static final String KEY_CONTRACT_LABEL = KP + "contract_label";
	private static final String KEY_CONTRACT_YES = KP + "contract_yes";
	private static final String KEY_STARTDATE_LABEL = KP + "startdate_label";
	private static final String KEY_ENDDATE_LABEL = KP + "enddate_label";
	private static final String KEY_PROVIDER_LABEL = KP + "provider_label";
	private static final String KEY_OPERATIONAL_FIELD_LABEL = KP + "main_activity";
	private static final String KEY_ADMIN_LABEL = KP + "admin_label";
	private static final String KEY_SCHOOL_YEAR_LABEL = KP + "school_year_label";
	private static final String KEY_SCHOOL_GROUP_LABEL = KP + "school_group_label";
	private static final String KEY_STUDY_PATH_LABEL = KP + "study_path_label";
	private static final String KEY_NATIVE_LANGUAGE_LABEL = KP + "native_language_label";
	private static final String KEY_LANGUAGE_LABEL = KP + "language_label";
	private static final String KEY_STUDENT = KP + "student";
	private static final String KEY_RESOURCE_LABEL = KP + "resource_label";
	private static final String KEY_COMMUNE_LABEL = KP + "commune_label";
	private static final String KEY_PAYMENT_BY_INVOICE_LABEL = KP + "payment_by_invoice_label";
	public static final String KEY_PLACEMENT_PARAGRAPH_LABEL = KP + "placement_paragraph_label";
	public static final String KEY_LATEST_INVOICE_DATE_LABEL = KP + "latest_placement_date_label";
	public static final String KEY_PAYMENT_BY_AGREEMENT_LABEL = KP + "payment_by_agreement";
	public static final String KEY_INVOICE_INTERVAL_LABEL = KP + "invoice_interval";
	private static final String KEY_PLACEMENT_DATE_LABEL = KP + "placement_date_label";
	private static final String KEY_PARENT_LABEL = KP + "parent_label";
	private static final String KEY_NEW_PROVIDER_LABEL = KP + "new_provider_label";
	
	public static final String KEY_DROPDOWN_CHOSE = KP + "dropdown_chose";
	public static final String KEY_DROPDOWN_YES = KP + "dropdown_yes";
	public static final String KEY_DROPDOWN_NO = KP + "dropdown_no";
	public static final String KEY_SCHOOL_YEAR = KP + "school_year";
	public static final String KEY_SCHOOL_GROUP = KP + "school_group";
	public static final String KEY_CENTRAL_ADMIN = KP + "central_admin";
	public static final String KEY_PROVIDER_ADMIN = KP + "provider_admin";
	public static final String KEY_STORED_MSG_PRFX = KP + "stored_msg_prfx";
	public static final String KEY_STORED_MSG_ERROR = KP + "stored_msg_error";
	public static final String KEY_FINISHED_PLACEMENT = KP + "finished_placement";
	public static final String KEY_FINISHED_PLACEMENT_FOR = KP + "finished_placement_for";
	public static final String KEY_END_DATE = KP + "end_date";
	public static final String KEY_NEW_PLACEMENT = KP + "new_placement";
	public static final String KEY_NEW_PLACEMENT_FOR = KP + "new_placement_for";
	public static final String KEY_START_DATE = KP + "start_date";
	public static final String KEY_STUDY_PATH = KP + "study_path";
	public static final String KEY_LANGUAGE = KP + "language";
	public static final String KEY_NATIVE_LANGUAGE = KP + "native_language";
	
		// Button keys
//	private static final String KEY_BUTTON_SEARCH = KP + "button_search";
	private static final String KEY_BUTTON_REGULAR_PAYMENT = KP + "button_regular_payment";
	private static final String KEY_BUTTON_PLACEMENT_HISTORY = KP + "placement_history";
	private static final String KEY_BUTTON_PUPIL_OVERVIEW = KP + "pupil_overview";
	private static final String KEY_BUTTON_NEW_PROVIDER = KP + "button_new_provider";
	private static final String KEY_BUTTON_CONTRACT_HISTORY = KP + "button_contract_history";
	private static final String KEY_BUTTON_NEW_GROUP = KP + "button_new_group";
	private static final String KEY_BUTTON_PLACE = KP + "button_place";
	private static final String KEY_BUTTON_CANCEL = KP + "button_cancel";
	private static final String KEY_BUTTON_SEND = KP + "button_send";
	private static final String KEY_BUTTON_NEW_PLACEMENT = KP + "button_new_placement";
	private static final String KEY_BUTTON_EDIT_LATEST_PLC = KP + "button_edit_latest_placement";

	// Http request parameters  
	public static final String PARAM_ACTION = "param_action";
	public static final String PARAM_PRESENTATION = "param_presentation";
	public static final String PARAM_SCHOOL_CATEGORY = "param_school_category";
	public static final String PARAM_SCHOOL_CATEGORY_CHANGED = "param_school_category_changed";
	public static final String PARAM_PROVIDER = "param_provider";
	public static final String PARAM_PROVIDER_CHANGED = "param_provider_changed";
	public static final String PARAM_SCHOOL_TYPE = "param_school_type";
	public static final String PARAM_NATIVE_LANGUAGE = "param_native_language";
	public static final String PARAM_LANGUAGE = "param_language";
	public static final String PARAM_SCHOOL_TYPE_CHANGED = "param_school_type_changed";
	public static final String PARAM_SCHOOL_YEAR = "param_school_year";
	public static final String PARAM_SCHOOL_YEAR_CHANGED = "param_school_year_changed";
	public static final String PARAM_SCHOOL_GROUP = "param_school_group";
	public static final String PARAM_PLACEMENT_DATE = "param_placement_date";
	public static final String PARAM_RESOURCES = "param_resources";
	// PARAM_BACK is used in SearchUserModule
	public static final String PARAM_BACK = "param_back";
	public static final String PARAM_PAYMENT_BY_AGREEMENT = "payment_by_agreement";
	public static final String PARAM_PLACEMENT_PARAGRAPH = "placement_paragraph";
	public static final String PARAM_INVOICE_INTERVAL = "invoice_interval";
	public static final String PARAM_STUDY_PATH = "param_study_path";
	public static final String PARAM_MSG_TO_NEW_PROVIDER = "msg_to_new_provider";
	public static final String PARAM_MSG_TO_PARENT = "msg_to_parent";
	public static final String PARAM_STORED_PLACEMENT_ID = "stored_placement_id";
	public static final String PARAM_LATEST_PLACEMENT_ID = "latest_placement_id";
	public static final String PARAM_LATEST_INVOICE_DATE = "param_latest_invoice_date";
	public static final String PARAM_NEW_PLACEMENT = "param_new_placement";
	public static final String PARAM_CANCEL_NEW_PLACEMENT = "param_cancel_new_placement";
	
	// Actions
	private static final int ACTION_PLACE_PUPIL = 1;	
	private static final int ACTION_REMOVE_SESSION_CHILD = 2;
	private static final int ACTION_SEND_MESSAGES = 3;
	
	// Presentations
	//private static final int PRESENTATION_SEARCH_FORM = 1;
	public static final String FORM_NAME = "central_placement_editor_form";

	// CSS styles   
	private static final String STYLE_UNDERLINED_SMALL_HEADER =
		"font-style:normal;text-decoration:underline;color:#000000;"
			+ "font-size:10px;font-family:Verdana,Arial,Helvetica;font-weight:bold;";
	private static final  String STYLE_STORED_PLACEMENT_MSG =
	"font-style:normal;color:#0000FF;"
									+ "font-size:10px;font-family:Verdana,Arial,Helvetica;font-weight:normal;";
	// Paths
	private static final String PATH_TRANS_GIF =
		"/idegaweb/bundles/com.idega.core.bundle/resources/transparentcell.gif";

	// Session java bean keys
	public static final String SESSION_KEY_CHILD = KP + "session_key.child";

	// Unique parameter suffix used by SearchUserModule
	private static final String UNIQUE_SUFFIX = "chosen_user";

	// Instance variables
	private IWResourceBundle iwrb;
	private Form form;
	private Table mainTable;
	private int mainTableRow;
	private String mainRowHeight  = null;
	private User child;
	private String uniqueUserSearchParam;
	private Image transGIF = new Image(PATH_TRANS_GIF);
	private String errMsgSearch = null;
	private String errMsgMid = null;
	private SchoolClassMember latestPl = null;
	private SchoolClassMember storedPlacement = null;
	private Link pupilOverviewLinkButton = null;
	private Link editLatestPlacementButton = null;
	private ProviderSession _providerSession = null;
	private SchoolSeason currentSeason = null;

	private int _action = -1;
	private boolean _newPlacement = false;
	private boolean _cancelNewPlacement = false;

	public void init(IWContext iwc) throws Exception {
		iwrb = getResourceBundle(iwc);
		form = new Form();
		form.setName(FORM_NAME);
		form.setEventListener(SchoolEventListener.class);
		
		// Parameter name returning chosen User from SearchUserModule
		uniqueUserSearchParam = SearchUserModule.getUniqueUserParameterName(UNIQUE_SUFFIX);

		form.add(getMainTable());
		parse(iwc);
		
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
		
		
		//Get child (User object) from search result param or session attribute
		getSearchResult(iwc);

		currentSeason = getCentralPlacementBusiness(iwc).getCurrentSeason();
		// Perform actions according the _action input parameter
		switch (_action) {
			case ACTION_PLACE_PUPIL :
				try {
					SchoolSeason chosenSeason = getSchoolSeasonHome().
						findByPrimaryKey(new Integer(getSchoolCommuneSession(iwc).getSchoolSeasonID()));
					latestPl = getCentralPlacementBusiness(iwc).getLatestPlacementFromElemAndHighSchool(child, chosenSeason);
					storedPlacement = storePlacement(iwc, child);
				} 
				catch (Exception e1) {log(e1);}			
				break;
			case ACTION_REMOVE_SESSION_CHILD :
				removeSessionChild(iwc);
				break;
			case ACTION_SEND_MESSAGES :
				sendNewPlacementMessages(iwc);
				break;

		}
		// Show main page tables		
		try {
			if (storedPlacement == null) {
				// Show main form parts
				if (!_newPlacement || _cancelNewPlacement) {
					// First part with search, pupil and latest placement
					setMainTableContent(getSearchTable(iwc));
					setMainTableContent(getPupilTable(iwc, child));
					setMainTableContent(getLatestPlacementTable(iwc, child));
				} else {
					// New placement form
					setMainTableContent(getNewPlacementTable(iwc));
					prepareCentralPlacementProviderSession(iwc);
				}
			} else {
				// Show message form

				setMainTableContent(getStoredPlacementMsgTable(iwc));
			}
		} catch (RemoteException e) {
			e.printStackTrace();
			setMainTableContent(new Text("RemoteException thrown!! Error connecting to EJB's"));
		}

		add(form);
	}

	private Table getMainTable() {
		mainTable = new Table();
		mainTable.setBorder(0);
		mainTable.setWidth(550);
		mainTable.setCellpadding(0);
		mainTable.setCellspacing(0);
		int col = 1;
		mainTableRow = 1;
		
		//  *** WINDOW HEADING ***
		mainTable.add(
			getLocalizedSmallHeader(KEY_WINDOW_HEADING, "Central placement of pupil"), 
																														col, mainTableRow);
		mainTable.setColor(col, mainTableRow, getHeaderColor());
		mainTable.setAlignment(col, mainTableRow, Table.HORIZONTAL_ALIGN_CENTER);
		mainTable.setRowVerticalAlignment(mainTableRow, Table.VERTICAL_ALIGN_MIDDLE);
		mainTable.setRowHeight(mainTableRow++, "20");

		return mainTable;
	}

	private void setMainTableContent(PresentationObject obj) {
		int col = 1;
		mainTable.add(obj, col, mainTableRow++);
	}

	public Table getSearchTable(IWContext iwc) {
		// *** Search Table *** START - the uppermost table
		Table table = new Table();
		table.setBorder(0);
		table.setCellpadding(0);
		table.setCellspacing(0);
		String rowHeight = "25";

		int col = 1;
		int row = 1;

		Image space1 = (Image) transGIF.clone();
		space1.setWidth(6);

		// *** HEADING Search pupil ***
		table.add(space1, col, row);
		Text pupilTxt = new Text(localize(KEY_SEARCH_PUPIL_HEADING, "Search pupil"));
		pupilTxt.setFontStyle(STYLE_UNDERLINED_SMALL_HEADER);
		table.add(pupilTxt, col++, row);
		table.setRowHeight(row, rowHeight);
		//table.setRowVerticalAlignment(row, Table.VERTICAL_ALIGN_BOTTOM);
		col = 1;
		row++;
		
		// User search module - configure and add 
		SearchUserModule searchModule = getSearchUserModule(iwc);
		try {
			// if one user found, set session attribute directly
			searchModule.process(iwc);
			User oneChild = searchModule.getUser();
			if (oneChild != null) {
				iwc.getSession().setAttribute(SESSION_KEY_CHILD, oneChild);
			} else if (iwc.isParameterSet("usrch_search_fname" + UNIQUE_SUFFIX)
							|| iwc.isParameterSet("usrch_search_lname" + UNIQUE_SUFFIX)
							|| iwc.isParameterSet("usrch_search_pid" + UNIQUE_SUFFIX)) {
					errMsgSearch = localize(KEY_SEARCH_NO_PUPIL_FOUND, "No pupil found");
			}
		} catch (Exception e) {}		
		table.add(searchModule, col++, row);
		
		// Get current pupil from session attribute
		child = (User) iwc.getSession().getAttribute(SESSION_KEY_CHILD);		
		
		if (errMsgSearch != null) {
			row++;
			col = 1;
			table.add(Text.getNonBrakingSpace(4), col, row);
			table.add(getSmallErrorText(errMsgSearch), col, row);
		}
		
		return table;
	}
	
	public Table getPupilTable(IWContext iwc, User child) {
		// *** Search Table *** START - the uppermost table
		Table table = new Table();
		table.setWidth("100%");
		table.setBorder(0);
		table.setCellpadding(2);
		table.setCellspacing(0);
		transGIF.setHeight("1");
		transGIF.setWidth("1");
		String rowHeight = "20";
		
		if (mainRowHeight != null)
			rowHeight = mainRowHeight;
			
		int row = 1;
		int col = 1;
		// add empty space row
		table.add(transGIF, col++, row);
		table.add(transGIF, col++, row);
		table.add(transGIF, col++, row);
		table.add(transGIF, col++, row);
		table.add(transGIF, col++, row);
		// Set COLUMN WIDTH for column 1 to 5
		table.setWidth(1, row, "100");
		//table.setWidth(2, row, "70");
		//table.setWidth(3, row, "70");
		//table.setWidth(4, row, "70");
		//table.setWidth(5, row, "104");
		table.setRowHeight(row, "1");
		
		row++;
		col = 1;

		// *** HEADING Pupil ***
		Text pupilTxt = new Text(localize(KEY_PUPIL_HEADING, "Pupil"));
		pupilTxt.setFontStyle(STYLE_UNDERLINED_SMALL_HEADER);
		table.add(pupilTxt, col++, row);
		table.setRowHeight(row, rowHeight);
		row++;
		col = 1;
		// Personal Id Number
		table.add(getSmallHeader(localize(KEY_PERSONAL_ID_LABEL, "Personal id: ")), col++, row);
		if (child != null)
			table.add(getSmallText(child.getPersonalID()), col++, row);
		table.setRowHeight(row, rowHeight);
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
		table.setRowHeight(row, rowHeight);
		row++;
		col = 1;

		// Address and Phone
		table.add(getSmallHeader(localize(KEY_ADDRESS_LABEL, "Address: ")), col++, row);
		table.setRowHeight(row, rowHeight);
		row++;
		col = 1;
		table.add(getSmallHeader(localize(KEY_PHONE_LABEL, "Phone: ")), col++, row);
		if (child != null) {
			try {
				// child address
				Address address = getUserBusiness(iwc).getUsersMainAddress(child);
				StringBuffer aBuf = new StringBuffer(address.getStreetAddress());
				aBuf.append(", ");
				aBuf.append(address.getPostalCode().getPostalAddress());
				row--;
				table.add(getSmallText(aBuf.toString()), col, row);
				row++;
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
		
		return table;
	}
	
	public Table getLatestPlacementTable(IWContext iwc, User child) throws RemoteException {
		// *** Search Table *** START - the uppermost table
		Table table = new Table();
		table.setWidth("100%");
		table.setBorder(0);
		table.setCellpadding(2);
		table.setCellspacing(0);
		transGIF.setHeight("1");
		transGIF.setWidth("1");
		String rowHeight = "20";

		if (mainRowHeight != null)
			rowHeight = mainRowHeight;
		
		int row = 1;
		int col = 1;
		// add empty space row
		table.add(transGIF, col++, row);
		table.add(transGIF, col++, row);
		table.add(transGIF, col++, row);
		table.add(transGIF, col++, row);
		table.add(transGIF, col++, row); 
		// Set COLUMN WIDTH for column 1 to 5
		table.setWidth(1, row, "100");
		table.setWidth(2, row, "100");
		//table.setWidth(3, row, "70");
		//table.setWidth(4, row, "70");
		//table.setWidth(5, row, "104");
		table.setRowHeight(row, rowHeight);		
		row++;
		col = 1;

		// *** HEADING Latest placement ***
		Text latestPlacementTxt =
			new Text(localize(KEY_LATEST_PLACEMENT_HEADING, "Latest placement"));
		latestPlacementTxt.setFontStyle(STYLE_UNDERLINED_SMALL_HEADER);
		table.add(latestPlacementTxt, col, row);
		table.setRowHeight(row, rowHeight);
		//table.setRowVerticalAlignment(row, Table.VERTICAL_ALIGN_BOTTOM);
		table.mergeCells(col, row, col+1, row);	

		// BUTTON Placement history 
		col = 5;
		table.add(getPlacementHistoryButton(), col, row);
		table.setAlignment(5,row, Table.HORIZONTAL_ALIGN_RIGHT);
		table.setRowHeight(row, rowHeight);
		row++;
		col = 1;
		// School type
		table.add(getSmallHeader(localize(KEY_SCHOOL_TYPE_LABEL, "School type: ")), col, row);
		// BUTTON Pupil overview
		pupilOverviewLinkButton = getPupilOverviewButton(); 
		table.add(pupilOverviewLinkButton, 5, row);
				//PARAM_PRESENTATION, String.valueOf(PRESENTATION_SEARCH_FORM)), 5, row);
		table.setAlignment(5, row, Table.HORIZONTAL_ALIGN_RIGHT);
		table.setRowHeight(row, rowHeight);
			
		row++;
		col = 1;
		// Placement
		table.add(getSmallHeader(localize(KEY_PLACEMENT_LABEL, "Placement: ")), col, row);
		table.setRowHeight(row, rowHeight);
		row++;
		col = 1;
		// Resources
		table.add(getSmallHeader(localize(KEY_RESOURCES_LABEL, "Resources: ")), col, row);
		table.setRowHeight(row, rowHeight);
		row++;
		col = 1;
		// Placement paragraph
		table.add(getSmallHeader(localize(KEY_PLACEMENT_PARAGRAPH_LABEL, "Placement paragraph: ")), col, row);
		table.setRowHeight(row, rowHeight);
		row++;
		col = 1;		
		// Contract
		table.add(getSmallHeader(localize(KEY_CONTRACT_LABEL, "Contract: ")), col, row);
		col = 3;
		table.add(getSmallHeader(localize(KEY_PAYMENT_BY_AGREEMENT_LABEL, "Payment by agreement: ")), col, row);
		// BUTTON Contract history 
		table.add(getContractHistoryButton(), 5, row);
		table.setAlignment(5, row, Table.HORIZONTAL_ALIGN_RIGHT);
		table.setRowHeight(row, rowHeight);
		row++;
		col = 1;
		table.add(getSmallHeader(localize(KEY_STARTDATE_LABEL, "Start date: ")), col, row);
		table.add(getSmallHeader(localize(KEY_ENDDATE_LABEL, "End date: ")), col+2, row);
		col = 5;
		// Edit latest placement BUTTON
		editLatestPlacementButton = getEditLatestPlacementButton();
		table.add(editLatestPlacementButton, col, row);
		table.setAlignment(col, row, Table.HORIZONTAL_ALIGN_RIGHT);
		
		table.setRowHeight(row, rowHeight);
			
		// VALUES - Latest placement
		if (child != null) {
			try {
				latestPl = getCentralPlacementBusiness(iwc).getLatestPlacementFromElemAndHighSchool(child, currentSeason);
				if (latestPl != null) {
					row--;row--;row--;row--;row--;
					col = 2;
					// School type
					try {
						SchoolType type = latestPl.getSchoolType();
						if (type != null)
							table.add(getSmallText(type.getName()), col, row);						
					} catch (Exception e) {}
					// BUTTON Pupil overview
					activatePupilOverviewButton(latestPl);
					
					row++;

					String buf = getPlacementString(latestPl, child, this.iwrb);
					
					table.add(getSmallText(buf), col, row);
					table.mergeCells(col, row, col+2, row);

					// BUTTON Regular payment for Latest Placement	
					try {
						table.add(getRegularPaymentTopButton(iwc, latestPl), 5, row);
						table.setAlignment(5, row, Table.HORIZONTAL_ALIGN_RIGHT);
					} catch (Exception e) {}
					
					row++;
					
					// Resources
					try {
						table.add(getSmallText(getResourcesString(iwc, latestPl)), col, row);
						table.mergeCells(col, row, col+2, row);
					} catch (Exception e) {}
					
					row++;
					
					// Placement
					if (latestPl.getPlacementParagraph() != null) {
						table.add(getSmallText(latestPl.getPlacementParagraph()), col, row);
						table.mergeCells(col, row, col+2, row);
					}
					
					row++;
					
					// Contract
					try {
						ChildCareContract contract =
							getChildCareBusiness(iwc).getValidContractByChild(
							((Integer) child.getPrimaryKey()).intValue());
						if (contract != null) {
							table.add(getSmallText(localize(KEY_CONTRACT_YES, "Yes")), col, row);
						}
					} catch (Exception e) {}

					// Payment by agreement
					col = 4;
					try {
						if (latestPl.getHasCompensationByAgreement()) {
							table.add(getSmallText(localize(KEY_CONTRACT_YES, "Yes")), col, row);
						}
					} catch (Exception e) {}
					
					
					
					row++;
					col = 2;
					
					// Start date
					try {
						table.add(getSmallText(getDateString(latestPl.getRegisterDate())), col, row);
					} catch (Exception e) {}
					
					col = 4;
					
					// End date
					try {
						if (latestPl.getRemovedDate() != null)
							table.add(getSmallText(getDateString(latestPl.getRemovedDate())), col, row);
					} catch (Exception e) {}
					
					// Edit latest placement BUTTON
					activateEditLatestPlacementButton(latestPl);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			// BUTTON Regular payment for Latest Placement					
			if (!(iwc.isParameterSet(PARAM_SCHOOL_CATEGORY)) 
					|| "-1".equals(iwc.getParameter(PARAM_SCHOOL_CATEGORY))) {
				table.add(getRegularPaymentTopButton(iwc, latestPl), 5, row-3);
				table.setAlignment(5, row-3, Table.HORIZONTAL_ALIGN_RIGHT);
			}
		}
		row++;
		col = 1;
		
		// Empty space row
		table.setRowHeight(row, rowHeight);
		row++;
		col = 1;
		
		
		// BUTTON New placement
		table.add(new SubmitButton(iwrb.getLocalizedImageButton(KEY_BUTTON_NEW_PLACEMENT, "New placement"), 
																							PARAM_NEW_PLACEMENT, "true"), col++, row);
		row++;
		col = 1;
		
		// Empty space row
		table.add(transGIF, col, row);
		table.setRowHeight(row, rowHeight);
		row++;
		col = 1;
		


		return table;
	}

	/**
	 * @return
	 */
	public static String getPlacementString(SchoolClassMember placement, User user, IWResourceBundle iwrb) {
		// Placement
		StringBuffer buf = new StringBuffer("");
		try {
			// add school name
			buf.append(placement.getSchoolClass().getSchool().getName());						
		} catch (Exception e) {}
		try {
			// school year
			SchoolYear theYear = placement.getSchoolYear();
			if (theYear != null)
				buf.append(", " + iwrb.getLocalizedString(KEY_SCHOOL_YEAR, "school year") + " "
								   												+ theYear.getName());						
		} catch (Exception e) {}
		try {
			// add school group
			buf.append(", " + iwrb.getLocalizedString(KEY_SCHOOL_GROUP, "group") + " "
								   + placement.getSchoolClass().getSchoolClassName());						
		} catch (Exception e) {}
		try {
			// add study path
			if (placement.getStudyPathId() != -1) {
				SchoolStudyPathHome  home = (SchoolStudyPathHome) IDOLookup.getHome(SchoolStudyPath.class);
				SchoolStudyPath sp = home.findByPrimaryKey(new Integer(placement.getStudyPathId()));
				buf.append(", " + iwrb.getLocalizedString(KEY_STUDY_PATH, "Study path") + " "+ sp.getCode());
			}
		} catch (Exception e) {}
		
		try {
			// add language
			if (placement.getLanguage() != null && !("-1").equals(placement.getLanguage())) {
				buf.append(", " + iwrb.getLocalizedString(KEY_LANGUAGE, "Language") + " "+ iwrb.getLocalizedString(placement.getLanguage(), ""));
			}
		} catch (Exception e) {}

		try {
			// add native language
			if (user.getNativeLanguage() != null) {
				buf.append(", " + iwrb.getLocalizedString(KEY_NATIVE_LANGUAGE, "Native language") + " "+ user.getNativeLanguage());
			}
		} catch (Exception e) {}
		return buf.toString();
	}

	public Table getNewPlacementTable(IWContext iwc) throws RemoteException {
		// *** Search Table *** START - the uppermost table
		Table table = new Table();
		table.setBorder(0);
		table.setWidth("100%");
		table.setCellpadding(2);
		table.setCellspacing(0);
		transGIF.setHeight("1");
		transGIF.setWidth("1");
		String rowHeight = "20";

		if (mainRowHeight != null)
			rowHeight = mainRowHeight;

		int row = 1;
		int col = 1;
		// add empty space row
		table.add(transGIF, col++, row);
		table.add(transGIF, col++, row);
		table.add(transGIF, col++, row);
		table.add(transGIF, col++, row);
		table.add(transGIF, col++, row);
		// Set COLUMN WIDTH for column 1 to 5
		table.setWidth(1, row, "100");
		table.setWidth(2, row, "90");
		//table.setWidth(3, row, "70");
		//table.setWidth(4, row, "70");
		//table.setWidth(5, row, "104");
		table.setRowHeight(row, rowHeight);

		//table.setBorder(1);
		
		// Hidden inputs
		table.add(new HiddenInput(PARAM_SCHOOL_CATEGORY_CHANGED, "-1"), 1, 1);
		table.add(new HiddenInput(PARAM_PROVIDER_CHANGED, "-1"), 1, 1);
		table.add(new HiddenInput(PARAM_SCHOOL_TYPE_CHANGED, "-1"), 1, 1);
		table.add(new HiddenInput(PARAM_SCHOOL_YEAR_CHANGED, "-1"), 1, 1);
		table.add(new HiddenInput(PARAM_NEW_PLACEMENT, "true"), 1, 1);
		
		// ERROR MSG - errMsgMid
		if (errMsgMid != null) {
			row++;
			col = 1;
			table.add(getSmallErrorText(errMsgMid), col, row);
			table.mergeCells(col, row, col+2, row);
		}

		row++;
		col = 1;

		// *** HEADING New placement *** 
		Text newPlacementTxt = new Text(localize(KEY_NEW_PLACEMENT_HEADING, "New placement"));
		newPlacementTxt.setFontStyle(STYLE_UNDERLINED_SMALL_HEADER);
		table.add(newPlacementTxt, col++, row);
		table.setRowHeight(row, rowHeight);
		//table.setRowVerticalAlignment(row, Table.VERTICAL_ALIGN_BOTTOM);
		row++;
		col = 1;
		
		//Student name
		child = (User) iwc.getSession().getAttribute(SESSION_KEY_CHILD);
		table.add(getSmallHeader(localize(KEY_STUDENT, "Student: ")), col++, row);
		if (child != null) {
			table.add(child.getNameLastFirst()+ ",&nbsp;&nbsp;", col, row);
			table.add(child.getPersonalID(), col, row);			
		}
		table.mergeCells(col, row, col+2, row);
		table.setRowHeight(row, rowHeight);
		row++;
		col = 1;
		
		// School Category
		table.add(getSmallHeader(localize(KEY_OPERATIONAL_FIELD_LABEL, "Operational field: ")), col++, row);
		table.add(getSchoolCategoriesDropdown(iwc), col, row);
		table.mergeCells(col, row, col+1, row);
		table.setRowHeight(row, rowHeight);
		row++;
		col = 1;
		
		// School season
		table.add(getSmallHeader(localize("school_season", "School season:")), col++, row);
		table.add(this.getSchoolSeasons(), col, row);
		table.mergeCells(col, row, col+1, row);
		row++;
		col = 1;
		
		// Provider labels
		table.add(getSmallHeader(localize(KEY_PROVIDER_LABEL, "Provider: ")), col++, row);
		table.add(getProvidersDropdown(iwc), col, row);
		table.mergeCells(col, row, col+1, row);
			// New Provider BUTTON
		table.add(getProviderEditorButton(), 5, row);
		table.setAlignment(5, row, Table.HORIZONTAL_ALIGN_RIGHT);
		table.setRowHeight(row, rowHeight);
		row++;
		col = 1;
		table.add(getSmallHeader(localize(KEY_ADDRESS_LABEL, "Address: ")), col++, row);
		table.setRowHeight(row, rowHeight);
		col++; col++;
		row++;
		col = 1;
		table.add(getSmallHeader(localize(KEY_PHONE_LABEL, "Phone: ")), col++, row);
		table.add(getSmallHeader(localize(KEY_COMMUNE_LABEL, "Commune: ")), ++col, row);
		table.setRowHeight(row, rowHeight);
		row++;
		col = 1;
		table.add(getSmallHeader(localize(KEY_ADMIN_LABEL, "Administration: ")), col++, row);
		// Compensation by invoice
		table.add(getSmallHeader(localize(KEY_PAYMENT_BY_INVOICE_LABEL, "Payment by invoice: ")),
																																	++col, row);
		table.setRowHeight(row, rowHeight);

		// Provider values		
		if (storedPlacement == null && iwc.isParameterSet(PARAM_PROVIDER) 
				&& !("-1".equals(iwc.getParameter(PARAM_PROVIDER))) 
				&& !("1".equals(iwc.getParameter(PARAM_SCHOOL_CATEGORY_CHANGED))) ) {
			School school = getCurrentProvider(iwc);
			if (school != null) {
				row--;row--;
				col = 2;
				
				// School Address value
				try {
					String addr = school.getSchoolAddress()+", "+school.getSchoolZipCode()+" "
																					 +school.getSchoolZipArea();
					table.add(getSmallText(addr), col, row);					
				} catch (Exception e) {}
				table.mergeCells(col, row, col+2, row);
				row++;
				col = 2;
				
				// Phone value
				table.add(getSmallText(school.getSchoolPhone()), col, row);
				col++; col++;
				
				// Commune value
				table.add(getSmallText(getCommuneName(school)), col, row);
				table.mergeCells(col, row, col+1, row);
				row++;
				col = 2;
				
				// Administrator value
				String adm = school.getCentralizedAdministration() ? 
					localize(KEY_CENTRAL_ADMIN, "Central") : localize(KEY_PROVIDER_ADMIN, "Provider");
				table.add(getSmallText(adm), col, row);
			}
			col++;col++;
			final Provider provider = new Provider(
																Integer.parseInt (iwc.getParameter(PARAM_PROVIDER)));           	
			if (provider != null) {
				boolean hasCompByInv = provider.getPaymentByInvoice ();
				Text txt = getSmallText(hasCompByInv ? localize(KEY_DROPDOWN_YES, "Yes") 
																			: localize(KEY_DROPDOWN_NO, "No"));
				table.add(txt, col, row);											
			}
		}

		row++;
		col = 1;
		//  input
		table.add(getSmallHeader(localize(KEY_SCHOOL_TYPE_LABEL, "School type:")), col++, row);
		table.add(getSchoolTypesDropdown(iwc), col++, row);
		table.add(
			getSmallHeader(localize(KEY_PLACEMENT_PARAGRAPH_LABEL, "Placement paragraph: ")),
			col++, row);
		table.add(getPlacementParagraphTextInput(iwc), col, row);
		table.mergeCells(col, row, col+1, row);
		table.setAlignment(col, row, Table.HORIZONTAL_ALIGN_LEFT);
		table.setRowHeight(row, rowHeight);
		row++;
		col = 1;
		// School Year input
		table.add(getSmallHeader(localize(KEY_SCHOOL_YEAR_LABEL, "School year: ")), col++, row);
		table.add(getSchoolYearsDropdown(iwc), col++, row);
		// School Group input
		table.add(getSmallHeader(localize(KEY_SCHOOL_GROUP_LABEL, "School group: ")), col++, row);
		table.add(getSchoolGroups(iwc), col++, row);
		// BUTTON New group
		table.add(getGroupEditorButton(iwc), 5, row);
				//PARAM_PRESENTATION, String.valueOf(PRESENTATION_SEARCH_FORM)), 5, row);
		table.setAlignment(5, row, Table.HORIZONTAL_ALIGN_RIGHT);
		table.setRowHeight(row, rowHeight);
		row++;
		col = 1;
		// Study Path input
		table.add(getSmallHeader(localize(KEY_STUDY_PATH_LABEL, "Study path: ")), col++, row);
		table.add(getStudyPathsDropdown(iwc), col++, row);
		row++;
		col = 1;
		
		//Native language input
		table.add(getSmallHeader(localize(KEY_NATIVE_LANGUAGE_LABEL, "Native language: ")), col++, row);
		table.add(getNativeLanguagesDropdown(iwc), col++, row);
		row++;
		col = 1;
		
		//Language input
		table.add(getSmallHeader(localize(KEY_LANGUAGE_LABEL, "Language: ")), col++, row);
		table.add(getLanguageDropdown(iwc), col++, row);
		row++;
		col = 1;
		
		// Resource
		table.add(getSmallHeader(localize(KEY_RESOURCE_LABEL, "Resource: ")), col++, row);
		//  Resource input checkboxes
		if (storedPlacement == null 
				&& !("1".equals(iwc.getParameter(PARAM_SCHOOL_CATEGORY_CHANGED)))
				&& !("1".equals(iwc.getParameter(PARAM_PROVIDER_CHANGED)))
				&& !("1".equals(iwc.getParameter(PARAM_SCHOOL_TYPE_CHANGED))) 
				&& iwc.isParameterSet(PARAM_SCHOOL_TYPE) 
				&& !("-1".equals(iwc.getParameter(PARAM_SCHOOL_TYPE))) 
				&& iwc.isParameterSet(PARAM_SCHOOL_YEAR) 
				&& !("-1".equals(iwc.getParameter(PARAM_SCHOOL_YEAR)))) {
			try {
				Collection rscColl = getResourceBusiness(iwc).getAssignableResourcesByYearAndType(
						iwc.getParameter(PARAM_SCHOOL_YEAR), iwc.getParameter(PARAM_SCHOOL_TYPE));
				CheckBox typeRscBox = new CheckBox(PARAM_RESOURCES);
				String[] rscArr = null;
				if (iwc.isParameterSet(PARAM_RESOURCES)) {
					rscArr = iwc.getParameterValues(PARAM_RESOURCES);
				}
				Integer primaryKey;
				Iterator loop = rscColl.iterator();
				// show resources
				for (int i = 0; loop.hasNext(); i++) {
					col = 2;
					Resource rsc = (Resource) loop.next();
					CheckBox cBox = (CheckBox) typeRscBox.clone();
					primaryKey = (Integer) rsc.getPrimaryKey();
					int intPK = primaryKey.intValue();
					cBox.setValue(intPK);
					boolean isChecked = false;
					if (rscArr != null) {
						for (int j = 0; j < rscArr.length; j++) {
							if (intPK == Integer.parseInt(rscArr[j])) {
								isChecked = true;
								break;
							}							
						}
					}
					cBox.setChecked(isChecked);
					table.add(cBox, col++, row);
					table.add(getSmallText(rsc.getResourceName()), col++, row);
					table.setRowHeight(row, rowHeight);
					row++;
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		table.setRowHeight(row, rowHeight);
		row++;
		col = 1;

		// Latest invoice date
		/*table.add(getSmallHeader(localize(KEY_LATEST_INVOICE_DATE_LABEL, "Latest invoice date")), 
																																col++, row);
		table.add(getLatestInvoiceDateInput(iwc), col, row);
		table.mergeCells(col, row, col+2, row);
		table.setRowHeight(row, rowHeight);

		row++;
		col = 1;
		*/
		
		// START row table
		Table rowTable = new Table(5, 1);
		rowTable.setCellpadding(0);
		rowTable.setCellspacing(0);
		int tmpRow = 1;
		int tmpCol = 1;
		rowTable.setBorder(0);
		
		// 1 Payment by agreement
		rowTable.add(
			getSmallHeader(localize(KEY_PAYMENT_BY_AGREEMENT_LABEL, "Payment by agreement: ")),
			tmpCol, tmpRow);
		rowTable.setWidth(tmpCol, tmpRow, "137");
		tmpCol++; // 2
		rowTable.add(getPaymentByAgreementDropdown(iwc, PARAM_PAYMENT_BY_AGREEMENT)
																														, tmpCol, tmpRow);
		rowTable.setWidth(tmpCol, tmpRow, 70);
		rowTable.setAlignment(tmpCol, tmpRow, Table.HORIZONTAL_ALIGN_CENTER);
		tmpCol++; // 3
		// Invoice interval
		rowTable.add(getSmallHeader(localize(KEY_INVOICE_INTERVAL_LABEL, "Invoice interval: ")),
																													tmpCol, tmpRow);
		tmpCol++; // 4
		rowTable.add(getInvoiceIntervalDropdown(iwc), tmpCol, tmpRow);
		rowTable.setWidth(tmpCol, tmpRow, 90);
		rowTable.setAlignment(tmpCol, tmpRow, Table.HORIZONTAL_ALIGN_CENTER);
		tmpCol++; // 5		
		// BUTTON Regular payment for New Placement					
		//if (iwc.isParameterSet(PARAM_SCHOOL_CATEGORY) 
				//&& !("-1".equals(iwc.getParameter(PARAM_SCHOOL_CATEGORY)))) {
			rowTable.add(getRegularPaymentBottomButton(iwc), tmpCol, tmpRow);																											
			rowTable.setAlignment(tmpCol, tmpRow, Table.HORIZONTAL_ALIGN_RIGHT);
			rowTable.setWidthAndHeightToHundredPercent();
		//}

		// END row table

		table.add(rowTable, col, row);
		table.mergeCells(col, row, col + 4, row);
		table.setRowHeight(row, rowHeight);
		row++;
		col = 1;
		
		// Placement date
		table.add(
			getSmallHeader(localize(KEY_PLACEMENT_DATE_LABEL, "Placement date: ")), col++, row);
		table.add(getPlacementDateInput(iwc), col, row);
		table.mergeCells(col, row, col + 2, row);
		table.setRowHeight(row, rowHeight);
		col = 1;
		row++;

		// empty space row
		Image trans1 = (Image) transGIF.clone();
		table.add(trans1, col, row);
		table.setRowHeight(row, rowHeight);
		
		col = 1;
		row++;		
		// BOTTOM BUTTONS
			// Place
		table.add(new SubmitButton(iwrb.getLocalizedImageButton(KEY_BUTTON_PLACE, "Place"), 
								PARAM_ACTION, String.valueOf(ACTION_PLACE_PUPIL)), col++, row);
			// Cancel		
		table.add(new SubmitButton(iwrb.getLocalizedImageButton(KEY_BUTTON_CANCEL, "Cancel"),
						PARAM_CANCEL_NEW_PLACEMENT, "true"), col++, row);
		col = 1;
		row++;		
		
		// empty space row
		table.add(trans1, col, row);
		table.setRowHeight(row, rowHeight);
		
		return table;
	}
	
	/**
	 * Shows a message of the stored placement after successfull creation.
	 * @param iwc
	 * @return
	 * @throws RemoteException
	 */
	public Table getStoredPlacementMsgTable(IWContext iwc) throws RemoteException {
		// *** Search Table *** START - the uppermost table
		Table table = new Table(3, 12);
		table.setBorder(0);
		table.setCellpadding(0);
		table.setCellspacing(0);
		table.setWidth(Table.HUNDRED_PERCENT);

		int col = 1;
		int row = 1;
		
		table.setWidth(col++, row, 10);
		table.setWidth(col++, row, 30);
		table.setWidth(col, row, Table.HUNDRED_PERCENT);

		Image space1 = (Image) transGIF.clone();
		space1.setWidth(6);
		
		table.add(space1, col, row);
		table.setRowHeight(row, "10");
		row++;
		
		// *** HEADING Stored placement ***
		col = 1;
		table.add(space1, col, row);
		col = 2;
		Text storedTxt = new Text(localize(KEY_STORED_PLACEMENT_HEADING, "Stored placement"));
		storedTxt.setFontStyle(STYLE_UNDERLINED_SMALL_HEADER);
		table.add(storedTxt, col, row);
		table.mergeCells(col, row, col+1, row);
		table.setRowHeight(row, "20");
		table.setRowVerticalAlignment(row, Table.VERTICAL_ALIGN_BOTTOM);
		col = 2;
		row++;
		
		// Empty space row
		table.add(space1, col, row);
		table.setRowHeight(row, "10");
		row++;
		col = 2;

		// Message stored message
		table.add(getStoredPlacementMsg(iwc), col, row);
		table.mergeCells(col, row, col+1, row);
		table.setRowHeight(row, "20");
		table.setRowVerticalAlignment(row, Table.VERTICAL_ALIGN_BOTTOM);
		row++;
		col = 1;

		// Empty space row
		table.add(space1, col, row);
		table.setRowHeight(row, "10");
		row++;
		col = 2;


		// *** HEADING Send message ***
//		table.add(space1, col, row);
		table.add(getSmallHeader(localize(KEY_MSG_OF_NEW_PLACEMENT_HEADING, "Send message of new placement to")), col, row);
		table.mergeCells(col, row, col+1, row);
		table.setRowHeight(row, "20");
		table.setRowVerticalAlignment(row, Table.VERTICAL_ALIGN_BOTTOM);
		col = 1;
		row++;		

		// Empty space row
		table.add(space1, col, row);
		table.setRowHeight(row, "10");						
		col = 2;
		row++;		

		table.add(new CheckBox(PARAM_MSG_TO_NEW_PROVIDER), col++, row);
		table.add(getSmallText(localize(KEY_NEW_PROVIDER_LABEL, "New provider")), col--, row);
		col = 2;
		row++;		

		table.add(new CheckBox(PARAM_MSG_TO_PARENT), col++, row);
		table.add(getSmallText(localize(KEY_PARENT_LABEL, "Parent")), col--, row);
		col = 1;
		row++;				

		// Empty space row
		table.add(space1, col, row);
		table.setRowHeight(row, "10");
		col = 2;
		row++;
		
		table.add(new HiddenInput(PARAM_STORED_PLACEMENT_ID, 
												((Integer) storedPlacement.getPrimaryKey()).toString()), 1, 1);

		// BOTTOM BUTTONS
			// Send
		table.add(new SubmitButton(iwrb.getLocalizedImageButton(KEY_BUTTON_SEND, "Send"), 
								PARAM_ACTION, String.valueOf(ACTION_SEND_MESSAGES)), col, row);
		table.add(space1, col, row);
			// Cancel		
		table.add(new SubmitButton(iwrb.getLocalizedImageButton(KEY_BUTTON_CANCEL, "Cancel")), col, row);
		table.mergeCells(col, row, col+1, row);
		col = 2;
		row++;

		// Empty space row
		table.add(space1, col, row);
		table.setRowHeight(row, "10");
	
		return table;
	}


	/**
	 * Process the search for a pupil. If the uniqueUserSearchParam is set the User is fetched and put in 
	 * session for further use. So here the User session bean is renew if uniqueuserSearchParam is set.
	 * @param iwc Request object context
	 */
	public void getSearchResult(IWContext iwc) {
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
		searcher.setShowMiddleNameInSearch(false);
		searcher.setOwnFormContainer(false);
		searcher.setUniqueIdentifier(UNIQUE_SUFFIX);
		searcher.setSkipResultsForOneFound(true);
		searcher.setHeaderFontStyleName(getStyleName(STYLENAME_SMALL_HEADER));
		searcher.setButtonStyleName(getStyleName(STYLENAME_INTERFACE_BUTTON));
		searcher.setPersonalIDLength(12);
		searcher.setFirstNameLength(15);
		searcher.setLastNameLength(20);
		searcher.setShowSearchParamsAfterSearch(false);

		String prmChild = SearchUserModule.getUniqueUserParameterName("child");
		if (iwc.isParameterSet(prmChild)) {
			searcher.maintainParameter(new Parameter(prmChild, iwc.getParameter(prmChild)));
		}

		return searcher;
	}

	private DropdownMenu getSchoolCategoriesDropdown(IWContext iwc) {
		// Get dropdown for school categories
		DropdownMenu schoolCats = (DropdownMenu) getStyledInterface(
																		new DropdownMenu(PARAM_SCHOOL_CATEGORY));
		schoolCats.addMenuElement("-1", localize(KEY_DROPDOWN_CHOSE, "- Chose -"));
		schoolCats.setValueOnChange(PARAM_SCHOOL_CATEGORY_CHANGED, "1");
		schoolCats.setToSubmit(true);
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
			if (storedPlacement == null && iwc.isParameterSet(PARAM_SCHOOL_CATEGORY))
				schoolCats.setSelectedElement(iwc.getParameter(PARAM_SCHOOL_CATEGORY));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return schoolCats;
	}

	private DropdownMenu getProvidersDropdown(IWContext iwc) {
		// Get dropdown for providers
		DropdownMenu providers = (DropdownMenu) getStyledInterface(
																					new DropdownMenu(PARAM_PROVIDER));
		providers.setValueOnChange(PARAM_PROVIDER_CHANGED, "1");
		providers.setToSubmit(true);
		providers.addMenuElement("-1", localize(KEY_DROPDOWN_CHOSE, "- Chose -"));
		try {
			// Get school category from topmost dropdown
			if (storedPlacement == null && iwc.isParameterSet(PARAM_SCHOOL_CATEGORY) 
					&& !("-1".equals(PARAM_SCHOOL_CATEGORY))) {
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
				if ("1".equals(iwc.getParameter(PARAM_SCHOOL_CATEGORY_CHANGED))) {
					providers.setSelectedElement("-1");
				} else if (iwc.isParameterSet(PARAM_PROVIDER)) {
					providers.setSelectedElement(iwc.getParameter(PARAM_PROVIDER));					
				}
			} else {
				providers.setSelectedElement("-1");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return providers;
	}

	private DropdownMenu getSchoolTypesDropdown(IWContext iwc) {
		DropdownMenu drop = (DropdownMenu) getStyledInterface(
		new DropdownMenu(PARAM_SCHOOL_TYPE));
		drop.setValueOnChange(PARAM_SCHOOL_TYPE_CHANGED, "1");
		drop.setToSubmit(true);
		drop.addMenuElement("-1", localize(KEY_DROPDOWN_CHOSE, "- Chose -"));
		if (storedPlacement == null 
				&& !("1".equals(iwc.getParameter(PARAM_SCHOOL_CATEGORY_CHANGED)))
				&& iwc.isParameterSet(PARAM_SCHOOL_CATEGORY) 
				&& !iwc.getParameter(PARAM_SCHOOL_CATEGORY).equals("-1")
				&& iwc.isParameterSet(PARAM_PROVIDER) 
				&& !iwc.getParameter(PARAM_PROVIDER).equals("-1")) {
			try {
				School school =
					getSchoolBusiness(iwc).getSchool(new Integer(iwc.getParameter(PARAM_PROVIDER)));
				if (school != null) {
					Collection schTypes = school.findRelatedSchoolTypes();
					for (Iterator iter = schTypes.iterator(); iter.hasNext();) {
						SchoolType type = (SchoolType) iter.next();
						SchoolCategory cat = type.getCategory();
						String typeCatID = (String) cat.getPrimaryKey();
						String paramCatID = iwc.getParameter(PARAM_SCHOOL_CATEGORY);
						// Show only if type-schCategory resembles dropdown-schCategory
						if (typeCatID.equals(paramCatID)) {
							int typeID = ((Integer) type.getPrimaryKey()).intValue();
							drop.addMenuElement(typeID, type.getName());
						}
					}
					if ("1".equals(iwc.getParameter(PARAM_PROVIDER_CHANGED))) {
						drop.setSelectedElement("-1");
					} else if (iwc.isParameterSet(PARAM_SCHOOL_TYPE)) {
						drop.setSelectedElement(iwc.getParameter(PARAM_SCHOOL_TYPE));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		} else {
			
		}

		return drop;
	}

	private DropdownMenu getNativeLanguagesDropdown(IWContext iwc) {
		DropdownMenu drop = (DropdownMenu) getStyledInterface(
		new DropdownMenu(PARAM_NATIVE_LANGUAGE));
		
		drop.addMenuElement("-1", localize("school.drp_chose_native_lang", "- Choose languge -"));
		try {
			Collection langs = getICLanguageHome().findAll();
			if (langs != null) {
				for (Iterator iter = langs.iterator(); iter.hasNext();) {
					ICLanguage aLang = (ICLanguage) iter.next();
					int langPK = ((Integer) aLang.getPrimaryKey()).intValue();
					drop.addMenuElement(langPK, aLang.getName());
				}
				if (storedPlacement == null && iwc.isParameterSet(PARAM_NATIVE_LANGUAGE)) {
					drop.setSelectedElement(iwc.getParameter(PARAM_NATIVE_LANGUAGE));
				}
			}
		}
		catch (RemoteException re) {
			re.printStackTrace();
		}
		catch (FinderException fe) {

		}
		return drop;
	}
	
	private DropdownMenu getLanguageDropdown(IWContext iwc) {
		DropdownMenu txtLangChoice = (DropdownMenu) getStyledInterface(new DropdownMenu(PARAM_LANGUAGE));
		txtLangChoice.addMenuElement("-1", localize("school.language", "Language"));
		txtLangChoice.addMenuElement("school.language_german", localize("school.language_german", "German"));
		txtLangChoice.addMenuElement("school.language_french", localize("school.language_french", "French"));
		txtLangChoice.addMenuElement("school.language_spanish", localize("school.language_spanish", "Spanish"));
		txtLangChoice.addMenuElement("school.language_swedish_english", localize("school.language_swedish_english", "Swedish/English"));
		
		if (storedPlacement == null && iwc.isParameterSet(PARAM_LANGUAGE)) {
			txtLangChoice.setSelectedElement(iwc.getParameter(PARAM_LANGUAGE));
		}
		
		return txtLangChoice;
	}
	
	
	private DropdownMenu getStudyPathsDropdown(IWContext iwc) {
		DropdownMenu studyPaths = (DropdownMenu) getStyledInterface(
																				new DropdownMenu(PARAM_STUDY_PATH));
		studyPaths.addMenuElement("-1", localize(KEY_DROPDOWN_CHOSE, "- Chose -"));

		if (storedPlacement == null 
				&& !("1".equals(iwc.getParameter(PARAM_SCHOOL_CATEGORY_CHANGED)))
				&& !("1".equals(iwc.getParameter(PARAM_PROVIDER_CHANGED)))
				&& iwc.isParameterSet(PARAM_PROVIDER) 
				&& !("-1".equals(iwc.getParameter(PARAM_PROVIDER)))				
				&& iwc.isParameterSet(PARAM_SCHOOL_TYPE) 
				&& !("-1".equals(iwc.getParameter(PARAM_SCHOOL_TYPE)))) {
			try {
				Collection coll = getStudyPathHome().findAllStudyPaths();
				for (Iterator iter = coll.iterator(); iter.hasNext();) {
					SchoolStudyPath element = (SchoolStudyPath) iter.next();
					int studyPathID = ((Integer) element.getPrimaryKey()).intValue();
					studyPaths.addMenuElement(studyPathID, element.getCode());
				}
				if ("1".equals(iwc.getParameter(PARAM_SCHOOL_CATEGORY_CHANGED))
						|| "1".equals(iwc.getParameter(PARAM_PROVIDER_CHANGED))) {
					studyPaths.setSelectedElement("-1");					
				} else if (iwc.isParameterSet(PARAM_STUDY_PATH)) {
					studyPaths.setSelectedElement(iwc.getParameter(PARAM_STUDY_PATH));
				}		
			} catch (Exception e) {
				e.printStackTrace();
			}
		}	
		return studyPaths;
	}
	
	

	private DropdownMenu getSchoolYearsDropdown(IWContext iwc) {
		DropdownMenu years = (DropdownMenu) getStyledInterface(
																				new DropdownMenu(PARAM_SCHOOL_YEAR));
		years.setValueOnChange(PARAM_SCHOOL_YEAR_CHANGED, "1");
		years.setToSubmit(true);
		years.addMenuElement("-1", localize(KEY_DROPDOWN_CHOSE, "- Chose -"));
		if (storedPlacement == null 
				&& !("1".equals(iwc.getParameter(PARAM_SCHOOL_CATEGORY_CHANGED))) 
				&& !("1".equals(iwc.getParameter(PARAM_PROVIDER_CHANGED)))
				&& iwc.isParameterSet(PARAM_SCHOOL_TYPE)
				&& !("-1".equals(iwc.getParameter(PARAM_SCHOOL_TYPE)))
				&& iwc.isParameterSet(PARAM_PROVIDER) 
				&& !("-1".equals(iwc.getParameter(PARAM_PROVIDER)))) {

			try {
				String providerIdStr = iwc.getParameter(PARAM_PROVIDER);
				School school = getSchoolBusiness(iwc).getSchool(new Integer(providerIdStr));
				if (school != null) {
					Collection yearColl = school.findRelatedSchoolYearsSortedByName();
					if (yearColl != null) {
						for (Iterator iter = yearColl.iterator(); iter.hasNext();) {
							SchoolYear year = (SchoolYear) iter.next();
							int paramTypeID =
								Integer.parseInt(iwc.getParameter(PARAM_SCHOOL_TYPE));
							if (year.getSchoolTypeId() == paramTypeID) {
								int yearID = ((Integer) year.getPrimaryKey()).intValue();
								years.addMenuElement(yearID, year.getName());
							}
						}
						if ("1".equals(iwc.getParameter(PARAM_SCHOOL_TYPE_CHANGED))) {
							years.setSelectedElement("-1");
						} else if (iwc.isParameterSet(PARAM_SCHOOL_YEAR)) {
							years.setSelectedElement(iwc.getParameter(PARAM_SCHOOL_YEAR));
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			years.setSelectedElement("-1");
		}

		return years;
	}

	private DropdownMenu getSchoolGroups(IWContext iwc) throws RemoteException {
		DropdownMenu groups = (DropdownMenu) getStyledInterface(
																				new DropdownMenu(PARAM_SCHOOL_GROUP));
		groups.addMenuElement("-1", localize(KEY_DROPDOWN_CHOSE, "- Chose -"));
			
		if (storedPlacement == null 
				&& !("1".equals(iwc.getParameter(PARAM_SCHOOL_CATEGORY_CHANGED))) 
				&& !("1".equals(iwc.getParameter(PARAM_PROVIDER_CHANGED)))
				&& !("1".equals(iwc.getParameter(PARAM_SCHOOL_TYPE_CHANGED)))
				&& iwc.isParameterSet(PARAM_PROVIDER) 
				&& iwc.isParameterSet(PARAM_SCHOOL_YEAR)) {
			
			int schoolID = Integer.parseInt(iwc.getParameter(PARAM_PROVIDER));
			int yearID = Integer.parseInt(iwc.getParameter(PARAM_SCHOOL_YEAR));
			
			SchoolSeason chosenSeason = null;
			try {
				//currentSeason = getSchoolChoiceBusiness(iwc).getSchoolSeasonHome().findSeasonByDate(new IWTimestamp().getDate());
				chosenSeason = getSchoolChoiceBusiness(iwc).getSchoolSeasonHome().findByPrimaryKey(new Integer(getSession().getSchoolSeasonID()));
			}
			catch (FinderException e) {
				try {
					chosenSeason = getSchoolChoiceBusiness(iwc).getCurrentSeason();
				} catch (FinderException fe) {}
			}
			
			try {				
				int seasonID = ((Integer) chosenSeason.getPrimaryKey()).intValue();
				Collection groupColl = getSchoolBusiness(iwc)
							.findSchoolClassesBySchoolAndSeasonAndYear(schoolID, seasonID, yearID);
				if (groupColl != null) {
					for (Iterator iter = groupColl.iterator(); iter.hasNext();) {
						SchoolClass group = (SchoolClass) iter.next();
						int groupID = ((Integer) group.getPrimaryKey()).intValue();
						groups.addMenuElement(groupID, group.getName());
					}
					if ("1".equals(iwc.getParameter(PARAM_SCHOOL_YEAR_CHANGED))) {
						groups.setSelectedElement("-1");
					} else if (iwc.isParameterSet(PARAM_SCHOOL_GROUP)) {
						groups.setSelectedElement(iwc.getParameter(PARAM_SCHOOL_GROUP));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			groups.setSelectedElement("-1");
		}

		return groups;
	}
	
	private DropdownMenu getPaymentByAgreementDropdown(IWContext iwc, String param) {
		DropdownMenu yesNo = (DropdownMenu) getStyledInterface(new DropdownMenu(param));
		//yesNo.addMenuElement("-1", localize(KEY_DROPDOWN_CHOSE, "- Chose -"));
		yesNo.addMenuElement(KEY_DROPDOWN_NO, localize(KEY_DROPDOWN_NO, "No"));
		yesNo.addMenuElement(KEY_DROPDOWN_YES, localize(KEY_DROPDOWN_YES, "Yes"));
		yesNo.setSelectedElement(KEY_DROPDOWN_NO);
		if (storedPlacement == null && iwc.isParameterSet(PARAM_PAYMENT_BY_AGREEMENT)) {
			yesNo.setSelectedElement(iwc.getParameter(PARAM_PAYMENT_BY_AGREEMENT));
		}
		return yesNo;
	}

	/*	
	private DateInput getLatestInvoiceDateInput(IWContext iwc) {
		DateInput dInput = (DateInput) getStyledInterface(new DateInput(PARAM_LATEST_INVOICE_DATE, true));
		dInput.setToDisplayDayLast(true);
		
		// Set year range
		IWTimestamp today = IWTimestamp.RightNow();
		int thisYear = today.getYear();
		dInput.setYearRange(thisYear - 1, thisYear + 1);		
		
		if (iwc.isParameterSet(PARAM_LATEST_INVOICE_DATE)) {
			IWTimestamp paramStamp = new IWTimestamp(iwc.getParameter(PARAM_LATEST_INVOICE_DATE));
			java.sql.Date paramDate = paramStamp.getDate();
			dInput.setDate(paramDate);
		}
		
		return dInput;
	}
	*/
	
	private DropdownMenu getInvoiceIntervalDropdown(IWContext iwc) {
		DropdownMenu drop = (DropdownMenu) getStyledInterface(
																		new DropdownMenu(PARAM_INVOICE_INTERVAL));
		drop.addMenuElement("-1", localize(KEY_DROPDOWN_CHOSE, "- Chose -"));
		try {
			Collection intervals = getSchoolBusiness(iwc).findAllSchClMemberInvoiceIntervalTypes();
			if (intervals != null) {
				for (Iterator iter = intervals.iterator(); iter.hasNext();) {
					String intervalKey = (String) iter.next();
					drop.addMenuElement(intervalKey, localize(intervalKey, intervalKey));					
				}
			}
			if (storedPlacement == null && iwc.isParameterSet(PARAM_INVOICE_INTERVAL)) {
				drop.setSelectedElement(iwc.getParameter(PARAM_INVOICE_INTERVAL));		
			}
		} catch (RemoteException re) {
			re.printStackTrace();
		}

		return drop;		
	}
		
	private TextInput getPlacementParagraphTextInput(IWContext iwc) {
		TextInput txt = (TextInput) getStyledInterface(new TextInput(PARAM_PLACEMENT_PARAGRAPH));
		txt.setLength(25);
		if (storedPlacement == null && iwc.isParameterSet(PARAM_PLACEMENT_PARAGRAPH)) {
			txt.setContent(iwc.getParameter(PARAM_PLACEMENT_PARAGRAPH));
		}
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
		int thisYear = today.getYear();
		dInput.setYearRange(thisYear - 1, thisYear + 1);
		if (storedPlacement == null && iwc.isParameterSet(PARAM_PLACEMENT_DATE)) {
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
		if (iwc.isParameterSet(PARAM_PROVIDER) && 
																	!iwc.getParameter(PARAM_PROVIDER).equals("-1")) {
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
	private String getCommuneName(School school) {
		String comName = "";
		Commune commune = school.getCommune();
		if (commune != null)
			comName = commune.getCommuneName();
			
		return comName;
	}
	
	public Text getStoredPlacementMsg(IWContext iwc) throws RemoteException {
		Text txt = null;
		StringBuffer buf = null;
		if (storedPlacement != null) {
			SchoolClassMember pl = storedPlacement;
			buf = new StringBuffer();
			//buf.append(localize(KEY_STORED_MSG_PRFX, "Stored placement: "));
			try {
				buf.append(getUserBusiness(iwc).getUser(pl.getClassMemberId()).getName());
				buf.append(", ");
				SchoolClass schClass = getSchoolBusiness(iwc).getSchoolClassHome().
																	findByPrimaryKey(new Integer(pl.getSchoolClassId()));
				buf.append(schClass.getSchool().getName() + ", ");
				buf.append(localize(KEY_SCHOOL_GROUP, "School group") + ": "  
																										+ schClass.getName() + ", ");
				buf.append(localize(KEY_SCHOOL_YEAR, "School year") 
											 + ": " + pl.getSchoolYear().getSchoolYearName() + ", ");
				IWTimestamp regStamp = new IWTimestamp(pl.getRegisterDate().getTime());
				buf.append(regStamp.getDateString("yyyy-MM-dd"));							
			} catch (Exception e) {
				e.printStackTrace();
			}
			String rscsStr = getResourcesString(iwc, pl);
			if (!rscsStr.equals(""))
				buf.append(", " + rscsStr);

		}				
		txt = new Text(buf.toString());
		txt.setFontStyle(STYLE_STORED_PLACEMENT_MSG);
		return txt;
	}

	/**
	 * Get a String with the Resource names related to param placement
	 * @param iwc
	 * @param placement
	 * @return
	 */	
	private String getResourcesString(IWContext iwc, SchoolClassMember placement) 
																											throws RemoteException {
		Collection coll = getResourceBusiness(iwc).getResourcePlacementsByMemberId(
																							(Integer) placement.getPrimaryKey());
		StringBuffer buf = new StringBuffer("");
		int i = 1;
		for (Iterator iter = coll.iterator(); iter.hasNext();) {
			ResourceClassMember rscPl = (ResourceClassMember) iter.next();
			Date endDate = rscPl.getEndDate();
			if (endDate != null) {
				if (endDate.before(new Date(System.currentTimeMillis()))) {
					continue;
				}
			}
			buf.append(rscPl.getResource().getResourceName());
			if (i < coll.size())
				buf.append(", ");			
			i++;			
		}
				
		return buf.toString();
	}
	
	private String getDateString(Timestamp stamp) {
		IWTimestamp iwts = null;
		String dateStr = "";
		if (stamp != null) {
			iwts = new IWTimestamp(stamp);
			dateStr = iwts.getDateString("yyyy-MM-dd");
		}
		return dateStr;
	}

	private Link getPupilOverviewButton() {
		Link linkButton = new Link(getSmallText(localize(KEY_BUTTON_PUPIL_OVERVIEW, "Pupil overview")));
		linkButton.setAsImageButton(true);
		linkButton.setWindowToOpen(CentralPlacementPupilOverview.class);
		linkButton.addParameter(SchoolAdminOverview.PARAMETER_METHOD, String.valueOf(SchoolAdminOverview.METHOD_OVERVIEW));
		linkButton.addParameter(SchoolAdminOverview.PARAMETER_SHOW_ONLY_OVERVIEW, "true");
		linkButton.addParameter(SchoolAdminOverview.PARAMETER_SHOW_NO_CHOICES, "true");
		linkButton.addParameter(SchoolAdminOverview.PARAMETER_PAGE_ID, getParentPage().getPageID());
		
		return linkButton;
	}
	
	private void activatePupilOverviewButton(SchoolClassMember plc) {
		String schClassId = String.valueOf(plc.getSchoolClassId());
		String plcId =  ((Integer) plc.getPrimaryKey()).toString();
		pupilOverviewLinkButton.addParameter(SchoolAdminOverview.PARAMETER_USER_ID, String.valueOf(plc.getClassMemberId()));
		pupilOverviewLinkButton.addParameter(SchoolAdminOverview.PARAMETER_SCHOOL_CLASS_ID, schClassId);        
		pupilOverviewLinkButton.addParameter(SchoolAdminOverview.PARAMETER_SCHOOL_CLASS_MEMBER_ID, plcId);
		pupilOverviewLinkButton.addParameter(SchoolAdminOverview.PARAMETER_RESOURCE_PERMISSION, 
																  SchoolAdminOverview.PARAMETER_RESOURCE_PERM_VALUE_CENTRAL_ADMIN);
		pupilOverviewLinkButton.addParameter(SchoolAdminOverview.PARAMETER_FROM_CENTRAL_PLACEMENT_EDITOR, "true");
		if (plc.getRemovedDate() != null)
			pupilOverviewLinkButton.addParameter(SchoolAdminOverview.PARAMETER_SCHOOL_CLASS_MEMBER_REMOVED_DATE, plc.getRemovedDate().toString());									
	}

	private Link getPlacementHistoryButton() {
		Link linkButton = new Link(getSmallText(localize(KEY_BUTTON_PLACEMENT_HISTORY, "Placement history")));
		linkButton.setAsImageButton(true);
		linkButton.setWindowToOpen(CentralPlacementHistoryViewer.class);
		if (child != null) {
			Integer PK = (Integer) child.getPrimaryKey();
			linkButton.addParameter(PlacementHistoryViewer.PARAM_PUPIL_ID, PK.intValue());			
		}
		
		return linkButton;
	}

	private Link getContractHistoryButton() {
		Link linkButton = new Link(getSmallText(localize(KEY_BUTTON_CONTRACT_HISTORY, "Contract history")));
		linkButton.setAsImageButton(true);
		linkButton.setWindowToOpen(CentralPlacementChildCareContracts.class);
		if (child != null) {
			Integer PK = (Integer) child.getPrimaryKey();
			linkButton.addParameter(ChildCareChildContracts.PARAMETER_CHILD_ID, PK.intValue());			
		}
		
		return linkButton;
	}


	private Link getRegularPaymentTopButton(IWContext iwc, SchoolClassMember plc) throws RemoteException {
		Link linkButton = new Link(getSmallText(localize(KEY_BUTTON_REGULAR_PAYMENT, "Regular payment")));
		linkButton.setAsImageButton(true);
		linkButton.setWindowToOpen(CentralPlacementRegularPaymentEntriesList.class);

		// Set parameters and session values to use as default in RegularPaymentEntriesList		
			// SchoolCategoryID		
		AccountingSession aSession = (AccountingSession) 
												IBOLookup.getSessionInstance(iwc, AccountingSession.class);
		if (aSession != null) {
			try {
				String opFieldID = (String) plc.getSchoolType().getCategory().getPrimaryKey();
				aSession.setOperationalField(opFieldID);
			} catch (Exception e) {
			}						
		}

			// ProviderID
		int providerID = -1;
		try {
			Integer provID = (Integer) plc.getSchoolClass().getSchool().getPrimaryKey();
			providerID = provID.intValue();
		} catch (Exception e) {			
		}
		if (providerID != -1)
			linkButton.addParameter(RegularPaymentEntriesList.PAR_SELECTED_PROVIDER, providerID);

		return linkButton;
	}


	private Link getRegularPaymentBottomButton(IWContext iwc) throws RemoteException {
		Link linkButton = new Link(getSmallText(localize(KEY_BUTTON_REGULAR_PAYMENT, "Regular payment")));
		linkButton.setAsImageButton(true);
		linkButton.setWindowToOpen(CentralPlacementRegularPaymentEntriesList.class);

		// Set parameters and session values to use as default in RegularPaymentEntriesList
		
			// SchoolCategoryID		
		String categoryID = "-1";
		if (iwc.isParameterSet(PARAM_SCHOOL_CATEGORY))
			categoryID = iwc.getParameter(PARAM_SCHOOL_CATEGORY);
		AccountingSession aSession = (AccountingSession) 
												IBOLookup.getSessionInstance(iwc, AccountingSession.class);
		if (aSession != null && !"-1".equals(categoryID))										
			aSession.setOperationalField(categoryID); //iwc.getParameter(PARAM_SCHOOL_CATEGORY));


		int providerID = -1;
					
		if (!categoryID.equals("-1") && iwc.isParameterSet(PARAM_PROVIDER))
			providerID = Integer.parseInt(iwc.getParameter(PARAM_PROVIDER));
		
			// providerID
		if (providerID != -1)
			linkButton.addParameter(RegularPaymentEntriesList.PAR_SELECTED_PROVIDER, providerID);

		return linkButton;
	}

	private Link getEditLatestPlacementButton() {
		Link linkButton = new Link(getSmallText(localize(KEY_BUTTON_EDIT_LATEST_PLC, "Edit placement")));
		linkButton.setAsImageButton(true);
		linkButton.setWindowToOpen(CentralPlacementEditLatestPlacementWindow.class);		
		return linkButton;
	}
	
	private void activateEditLatestPlacementButton(SchoolClassMember plc) {
		if (plc != null) {
			Integer plcPK = (Integer) plc.getPrimaryKey();
			editLatestPlacementButton.setParameter(
											CentralPlacementEditLatestPlacement.PARAM_LATEST_PLACEMENT_ID, 
											plcPK.toString());
		}
	}
	
	private Link getProviderEditorButton() {
		Link linkButton = new Link(getSmallText(localize(KEY_BUTTON_NEW_PROVIDER, "New provider")));
		linkButton.setAsImageButton(true);
		linkButton.setWindowToOpen(CentralPlacementProviderEditor.class);
		//linkButton.setParameter(SchoolAdminOverview.PARAMETER_METHOD, String.valueOf(SchoolAdminOverview.METHOD_OVERVIEW));
		//linkButton.addParameter(SchoolAdminOverview.PARAMETER_PAGE_ID, getParentPage().getPageID());
		
		return linkButton;
	}

	private Link getGroupEditorButton(IWContext iwc) throws RemoteException {
		Link linkButton = new Link(getSmallText(localize(KEY_BUTTON_NEW_GROUP, "New  group")));
		linkButton.setAsImageButton(true);
		linkButton.setWindowToOpen(CentralPlacementSchoolGroupEditor.class);
		String categoryID = "-1";
		int typeID = -1;
		int providerID = -1;
		int seasonID = -1;
		int yearID = -1;

		//String tmp = iwc.getParameter(PARAM_SCHOOL_CATEGORY);
		
		if (iwc.isParameterSet(PARAM_SCHOOL_CATEGORY))
			categoryID = iwc.getParameter(PARAM_SCHOOL_CATEGORY);

		if (!categoryID.equals("-1") && iwc.isParameterSet(PARAM_SCHOOL_TYPE))
			typeID = Integer.parseInt(iwc.getParameter(PARAM_SCHOOL_TYPE));


		if (!categoryID.equals("-1") && typeID != -1 && iwc.isParameterSet(PARAM_PROVIDER))
			providerID = Integer.parseInt(iwc.getParameter(PARAM_PROVIDER));

		if (!categoryID.equals("-1") && typeID != -1  && providerID != -1 
				&& iwc.isParameterSet(PARAM_SCHOOL_YEAR))
			yearID = Integer.parseInt(iwc.getParameter(PARAM_SCHOOL_YEAR));
		
	// Set parameters and session values to use as default in SchoolGroupEditorAdmin
	
		// SchoolCategoryID		
		AccountingSession aSession = (AccountingSession) 
												IBOLookup.getSessionInstance(iwc, AccountingSession.class);
		if (aSession != null && !"-1".equals(categoryID)) {
			aSession.setOperationalField(categoryID);											
		}

		// Only Bunadmin schools?
		linkButton.addParameter(SchoolGroupEditorAdmin.PARAM_BUNADM, ""+false);

		// SchoolTypeID
		linkButton.addParameter(SchoolGroupEditor.PARAMETER_TYPE_ID, typeID);

		// SchoolID
		linkButton.addParameter(SchoolCommuneSessionBean.PARAMETER_SCHOOL_ID, providerID);

		// SchoolYearID
		ProviderSession pSession = (ProviderSession)
													IBOLookup.getSessionInstance(iwc, ProviderSession.class);
		if (pSession != null)
			pSession.setYearID(yearID);

		// SchoolSeasonID	
		try {
			SchoolSeason currentSeason = getSchoolChoiceBusiness(iwc).getCurrentSeason();
			if (currentSeason != null) {
				seasonID = ((Integer) currentSeason.getPrimaryKey()).intValue();
				if (pSession != null)
					pSession.setSeasonID(seasonID);			
			}						
		} catch (FinderException e) {	}
				
		return linkButton;
	}
	
	/**
	 * Gets and sets a ProviderSession session bean with session data used by SchoolGroupEditor reached by New group button
	 * @param iwc
	 * @throws RemoteException
	 */
	private void prepareCentralPlacementProviderSession(IWContext iwc) throws RemoteException {
		_providerSession = getCentralPlacementProviderSession(iwc);
		if (iwc.isParameterSet(PARAM_PROVIDER) && !("-1".equals(iwc.getParameter(PARAM_PROVIDER)))) {
			String provIdStr = iwc.getParameter(PARAM_PROVIDER);
			int provID = Integer.parseInt(provIdStr);
			_providerSession.setProviderID(provID);
		}
		
		if (iwc.isParameterSet(PARAM_SCHOOL_YEAR)) {
			_providerSession.setYearID(Integer.parseInt(iwc.getParameter(PARAM_SCHOOL_YEAR)));
		}
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
		
		if (iwc.isParameterSet(PARAM_NEW_PLACEMENT)
				&& "true".equals(iwc.getParameter(PARAM_NEW_PLACEMENT))) {
			_newPlacement = true;		
		}
		
		if (iwc.isParameterSet(PARAM_CANCEL_NEW_PLACEMENT)
				&& "true".equals(iwc.getParameter(PARAM_CANCEL_NEW_PLACEMENT))) {
			_cancelNewPlacement = true;
		}
	}

	// *** ACTIONS ***
	private SchoolClassMember storePlacement(IWContext iwc, User child) {
		SchoolClassMember mbr = null;
		int childID =  -1;
		if (child != null)
			childID = ((Integer) child.getPrimaryKey()).intValue();
		try {
			mbr = getCentralPlacementBusiness(iwc).storeSchoolClassMember(iwc, childID);			
			sendEndedPlacementMessageToProvider(iwc);
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
	
	public void sendEndedPlacementMessageToProvider(IWContext iwc) throws RemoteException {
		// Update latest placement with removed_date
		if(latestPl != null) {
			try {
				latestPl = getSchoolClassMemberHome().findByPrimaryKey(latestPl.getPrimaryKey());
			} catch (EJBException e) {
				e.printStackTrace();
			} catch (FinderException e) {
				e.printStackTrace();
			}			
		}
		// Send messages to provider users
		if (latestPl != null) {			
			Collection users = getSchoolBusiness(iwc).getSchoolUsers(latestPl.getSchoolClass().getSchool());
			String subject = localize(KEY_FINISHED_PLACEMENT, "Finished placement");
			String body = null;
			StringBuffer buf = new StringBuffer("");
			
			buf.append(localize(KEY_FINISHED_PLACEMENT_FOR, "Finished placement for")+" "+child.getName());
			try {
				buf.append(", "+latestPl.getSchoolClass().getSchool().getName());						
			} catch (Exception e) {}
			try {
				buf.append(", "+latestPl.getSchoolType().getName());					
			} catch (Exception e) {}
			try {
				buf.append(", "+localize(KEY_SCHOOL_YEAR, "school year")+": "+latestPl.getSchoolYear().getName());						
			} catch (Exception e) {}
			try {
				buf.append(", "+localize(KEY_SCHOOL_GROUP, "group")+": "+latestPl.getSchoolClass().getSchoolClassName());						
			} catch (Exception e) {}
			try {
				buf.append(", "+localize(KEY_END_DATE, "End date")+": ");
				IWTimestamp stamp = new IWTimestamp(latestPl.getRemovedDate());
				buf.append(stamp.getDateString("yyyy-MM-dd"));
			} catch (Exception e) {}
			body = buf.toString();
			for (Iterator iter = users.iterator(); iter.hasNext();) {
				SchoolUser providerUser = (SchoolUser) iter.next();
				User user = providerUser.getUser();
				/*System.out.println("CPE End Message to School user: "+user.getNameLastFirst() + " -  id: "
											+((Integer) user.getPrimaryKey()).toString());*/
				getMessageBusiness(iwc).createUserMessage(user, subject, body, false);
			}
		}
	}

	protected void sendNewPlacementMessages(IWContext iwc) throws RemoteException {
		if (iwc.isParameterSet(PARAM_STORED_PLACEMENT_ID)) {
			String pIdStr = iwc.getParameter(PARAM_STORED_PLACEMENT_ID);
			Integer placementID = new Integer(pIdStr);
			
			try {
				SchoolClassMember placement = getSchoolClassMemberHome().findByPrimaryKey(placementID);

				if (iwc.isParameterSet(PARAM_MSG_TO_PARENT)) {			
					Collection parents;
					try {
						parents = getCommuneUserBusiness(iwc).getMemberFamilyLogic().getParentsFor(child);
						String subject = localize(KEY_NEW_PLACEMENT, "New placement");
						String body = null;
						StringBuffer buf = new StringBuffer("");
				
						buf.append(localize(KEY_NEW_PLACEMENT_FOR, "New placement for")+" "+child.getName());
						try {
							buf.append(", "+placement.getSchoolClass().getSchool().getName());						
						} catch (Exception e) {}
						try {
							buf.append(", "+placement.getSchoolType().getName());					
						} catch (Exception e) {}
						try {
							buf.append(", "+localize(KEY_SCHOOL_YEAR, "school year")+": "+placement.getSchoolYear().getName());						
						} catch (Exception e) {}
						try {
							buf.append(", "+localize(KEY_SCHOOL_GROUP, "group")+": "+placement.getSchoolClass().getSchoolClassName());						
						} catch (Exception e) {}
						try {
							buf.append(", "+localize(KEY_START_DATE, "Start date")+": ");
							IWTimestamp stamp = new IWTimestamp(placement.getRegisterDate());
							buf.append(stamp.getDateString("yyyy-MM-dd"));
						} catch (Exception e) {}
						body = buf.toString();
						for (Iterator iter = parents.iterator(); iter.hasNext();) {
							User user = (User) iter.next();
							/*System.out.println("CPE New Message to Parent: "+user.getNameLastFirst() + " -  id: "
														+((Integer) user.getPrimaryKey()).toString());*/
							getMessageBusiness(iwc).createUserMessage(user, subject, body, false);
						}
		
					} catch (NoParentFound e1) {
						e1.printStackTrace();
					} 
								
				// Send messages to new provider			
				}
				if (iwc.isParameterSet(PARAM_MSG_TO_NEW_PROVIDER)) {
					Collection users = getSchoolBusiness(iwc).getSchoolUsers(placement.getSchoolClass().getSchool());
					String subject = localize(KEY_NEW_PLACEMENT, "New placement");
					String body = null;
					StringBuffer buf = new StringBuffer("");
				
					buf.append(localize(KEY_NEW_PLACEMENT_FOR, "New placement for")+" "+child.getName());
					try {
						buf.append(", "+placement.getSchoolClass().getSchool().getName());						
					} catch (Exception e) {}
					try {
						buf.append(", "+placement.getSchoolType().getName());					
					} catch (Exception e) {}
					try {
						buf.append(", "+localize(KEY_SCHOOL_YEAR, "school year")+": "+placement.getSchoolYear().getName());						
					} catch (Exception e) {}
					try {
						buf.append(", "+localize(KEY_SCHOOL_GROUP, "group")+": "+placement.getSchoolClass().getSchoolClassName());						
					} catch (Exception e) {}
					try {
						buf.append(", "+localize(KEY_START_DATE, "Start date")+": ");
						IWTimestamp stamp = new IWTimestamp(placement.getRegisterDate());
						buf.append(stamp.getDateString("yyyy-MM-dd"));
					} catch (Exception e) {}
					body = buf.toString();
					for (Iterator iter = users.iterator(); iter.hasNext();) {
						SchoolUser providerUser = (SchoolUser) iter.next();
						User user = providerUser.getUser();
						/*System.out.println("CPE New Message to School user: "+user.getNameLastFirst() + " -  id: "
												+((Integer) user.getPrimaryKey()).toString()); */
						getMessageBusiness(iwc).createUserMessage(user, subject, body, false);
					}			
				}				
			} catch (FinderException e2) {
				e2.printStackTrace();
			}
		}		
	}

	
	private ICLanguageHome getICLanguageHome() throws RemoteException {
		return (ICLanguageHome) IDOLookup.getHome(ICLanguage.class);
	}
	
	protected ProviderSession getCentralPlacementProviderSession(IWUserContext iwuc) {
		try {
			return (ProviderSession) IBOLookup.getSessionInstance(iwuc, ProviderSession.class);
		}
		catch (RemoteException re) {
			throw new IBORuntimeException(re.getMessage());
		}
	}

	
	private CentralPlacementBusiness getCentralPlacementBusiness(IWContext iwc) 
																										throws RemoteException {
		return (CentralPlacementBusiness) 
											IBOLookup.getServiceInstance(iwc, CentralPlacementBusiness.class);
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
	private ChildCareBusiness getChildCareBusiness(IWContext iwc) throws RemoteException {
		return (ChildCareBusiness)
											 IBOLookup.getServiceInstance(iwc, ChildCareBusiness.class);
	}

	private ResourceBusiness getResourceBusiness(IWContext iwc) throws RemoteException {
		return (ResourceBusiness) IBOLookup.getServiceInstance(iwc, ResourceBusiness.class);
	}
	
	public SchoolStudyPathHome getStudyPathHome() throws java.rmi.RemoteException {
		return (SchoolStudyPathHome) IDOLookup.getHome(SchoolStudyPath.class);
	}
	
	public SchoolSeasonHome getSchoolSeasonHome() throws RemoteException {
		return (SchoolSeasonHome) IDOLookup.getHome(SchoolSeason.class);
	}
	
	public SchoolClassMemberHome getSchoolClassMemberHome() throws RemoteException {
		return (SchoolClassMemberHome) IDOLookup.getHome(SchoolClassMember.class);
	}

	private MessageBusiness getMessageBusiness(IWContext iwc) throws RemoteException {
		return (MessageBusiness) IBOLookup.getServiceInstance(iwc, MessageBusiness.class);
	}

	public CommuneUserBusiness getCommuneUserBusiness(IWContext iwc) throws RemoteException {
		return (CommuneUserBusiness) IBOLookup.getServiceInstance(iwc, CommuneUserBusiness.class);
	}


}
