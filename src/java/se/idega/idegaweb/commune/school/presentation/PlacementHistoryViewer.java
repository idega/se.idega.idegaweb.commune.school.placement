/*
 * Created on 2003-nov-19
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package se.idega.idegaweb.commune.school.presentation;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;

import javax.ejb.FinderException;

import se.idega.idegaweb.commune.accounting.resource.business.ResourceBusiness;
import se.idega.idegaweb.commune.business.CommuneUserBusiness;
import se.idega.idegaweb.commune.presentation.CommuneBlock;
import se.idega.idegaweb.commune.school.business.CentralPlacementBusiness;

import com.idega.block.school.business.SchoolBusiness;
import com.idega.block.school.data.SchoolClassMember;
import com.idega.block.school.data.SchoolStudyPath;
import com.idega.block.school.data.SchoolStudyPathHome;
import com.idega.business.IBOLookup;
import com.idega.core.contact.data.Phone;
import com.idega.core.location.data.Address;
import com.idega.data.IDOLookup;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.Table;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.HiddenInput;
import com.idega.presentation.ui.SubmitButton;
import com.idega.user.data.User;
import com.idega.util.text.Name;

/**
 * @author WMGOBOM
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class PlacementHistoryViewer extends CommuneBlock {
	// *** Localization keys ***
	private static final String KP = "placement_history_viewer.";
	private static final String KEY_WINDOW_HEADING = KP + "window_heading";
	private static final String KEY_SEARCH_PUPIL_HEADING = KP + "search_pupil_heading";
	private static final String KEY_PUPIL_HEADING = KP + "pupil_heading";
	private static final String KEY_PLACEMENTS_HEADING = KP + "placements_heading";
	private static final String KEY_CONFIRM_REMOVE_PLC_MSG = KP + "confirm_remove_plc_msg";
	private static final String KEY_TOOLTIP_REMOVE_PLC = KP + "tooltip_remove_placement";
	private static final String KEY_TOOLTIP_PUPIL_OVERVIEW = KP + "tooltip_pupil_overview";

		// Label keys
	private static final String KEY_PERSONAL_ID_LABEL = KP + "personal_id_label";
	private static final String KEY_FIRST_NAME_LABEL = KP + "first_name_label";
	private static final String KEY_LAST_NAME_LABEL = KP + "last_name_label";
	private static final String KEY_ADDRESS_LABEL = KP + "address_label";
	private static final String KEY_PHONE_LABEL = KP + "telephone_label";

		// Column headers
	private static final String KEY_NUMBER = KP + "number";
	private static final String KEY_SCHOOL_TYPE = KP + "school_type";
	private static final String KEY_SCHOOL_YEAR = KP + "school_year";
	private static final String KEY_SCHOOL_GROUP = KP + "school_group";
	private static final String KEY_PROVIDER = KP + "provider";
	private static final String KEY_END_DATE = KP + "end_date";
	private static final String KEY_START_DATE = KP + "start_date";
	private static final String KEY_RESOURCES = KP + "resources";
	private static final String KEY_STUDY_PATH = KP + "study_path";
	private static final String KEY_REGISTRATOR = KP + "registrator";
	private static final String KEY_REGISTRATION_CREATED_DATE = KP + "registration_created_date";
	private static final String KEY_PLACEMENT_PARAGRAPH_SHORT = KP + "placement_paragraph_sh";
	private static final String KEY_NOTES = KP + "notes";
	
	
		// Button keys
//	private static final String KEY_BUTTON_SEARCH = KP + "button_search";
	//private static final String KEY_BUTTON_REGULAR_PAYMENT = KP + "button_regular_payment";


	// Http request parameters
	public static final String PARAM_PUPIL_ID = "param_pupil_id";
	 
	public static final String PARAM_ACTION = "param_action";
	public static final String PARAM_PRESENTATION = "param_presentation";
	public static final String PARAM_SCHOOL_CATEGORY = "param_school_category";
	public static final String PARAM_SCHOOL_CATEGORY_CHANGED = "param_school_category_changed";
	public static final String PARAM_PROVIDER = "param_provider";
	public static final String PARAM_PROVIDER_CHANGED = "param_provider_changed";
	public static final String PARAM_SCHOOL_TYPE = "param_school_type";
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
	public static final String PARAM_REMOVE_PLACEMENT = "remove_placement";
	
	// CSS styles   
	private static final String STYLE_UNDERLINED_SMALL_HEADER =
		"font-style:normal;text-decoration:underline;color:#000000;"
			+ "font-size:10px;font-family:Verdana,Arial,Helvetica;font-weight:bold;";

/*	private static final  String STYLE_STORED_PLACEMENT_MSG =
	"font-style:normal;color:#0000FF;"
									+ "font-size:10px;font-family:Verdana,Arial,Helvetica;font-weight:normal;";
*/
	// Paths
	private static final String PATH_TRANS_GIF =
		"/idegaweb/bundles/com.idega.core.bundle/resources/transparentcell.gif";

	// Session java bean keys
	public static final String SESSION_KEY_CHILD = KP + "session_key.child";

	// Unique parameter suffix used by SearchUserModule
	private static final String UNIQUE_SUFFIX = "chosen_user";
	
	public static final String SEARCH_FORM_NAME = "search_form";


	// Instance variables
	//private IWResourceBundle iwrb;
	private Form form;
	private Table mainTable;
	private int mainTableRow;
	private User pupil;
	private String uniqueUserSearchParam;
	private Image transGIF = new Image(PATH_TRANS_GIF);



	public void main(IWContext iwc) throws Exception {
		//iwrb = getResourceBundle(iwc);
		form = new Form();
		form.setName(SEARCH_FORM_NAME);
		
	
		// Parameter name returning chosen User from SearchUserModule
		uniqueUserSearchParam = SearchUserModule.getUniqueUserParameterName(UNIQUE_SUFFIX);
		form.maintainAllParameters();
		
		if (iwc.isParameterSet(PARAM_REMOVE_PLACEMENT) 
				&& !("-1".equals(iwc.getParameter(PARAM_REMOVE_PLACEMENT)))) {
			// A remove placement button is pressed
			String plcIdStr = null;
			try {
				plcIdStr = iwc.getParameter(PARAM_REMOVE_PLACEMENT);
				Integer plcPK = new Integer(plcIdStr);
				getCentralPlacementBusiness(iwc).removeSchoolClassMember(plcPK);
			}catch (Exception e) {
				logWarning("Error erasing SchooClassMember with PK: " + plcIdStr);
				log(e);
			}
		}

		pupil = getPupilFromParam(iwc);
		
		form.add(getMainTable());
		setMainTableContent(getSearchTable(iwc));
		setMainTableContent(getPupilTable(iwc, pupil));
		setMainTableContent(getPlacementTable(iwc));
		
		// Add empty bottom that fills the bottom window space
		setMainTableContent(transGIF);
		mainTable.setHeight(1, mainTableRow -1, Table.HUNDRED_PERCENT);
		
		add(form);
	}

	private Table getMainTable() {
		mainTable = new Table();
		mainTable.setBorder(0);
		mainTable.setWidth(Table.HUNDRED_PERCENT);
		mainTable.setHeight(Table.HUNDRED_PERCENT);
		mainTable.setCellpadding(0);
		mainTable.setCellspacing(0);
		int col = 1;
		mainTableRow = 1;
		
		//  *** WINDOW HEADING ***
		mainTable.add(
			getLocalizedSmallHeader(KEY_WINDOW_HEADING, "Placement history viewer"), 
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

		int col = 1;
		int row = 1;

		Image space1 = (Image) transGIF.clone();
		space1.setWidth(6);

		// *** HEADING Search pupil ***
		table.add(space1, col, row);
		Text pupilTxt = new Text(localize(KEY_SEARCH_PUPIL_HEADING, "Search pupil"));
		pupilTxt.setFontStyle(STYLE_UNDERLINED_SMALL_HEADER);
		table.add(pupilTxt, col++, row);
		table.setRowHeight(row, "40");
		table.setRowVerticalAlignment(row, Table.VERTICAL_ALIGN_BOTTOM);
		col = 1;
		row++;
		// User search module - configure and add
		SearchUserModule searchMod = getSearchUserModule();
		table.add(searchMod, col++, row);

		// Get pupil if only one found
		try {
			searchMod.process(iwc);
			User oneChild = searchMod.getUser();
			if (oneChild != null) {
				pupil = oneChild;
			}
		} catch (Exception e) {}
		
		return table;
	}
	
	public Table getPupilTable(IWContext iwc, User pupil) {
		// *** Search Table *** START - the uppermost table
		Table table = new Table();
		table.setWidth("100%");
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
		table.setWidth(1, row, "100");
		//table.setWidth(2, row, "70");
		//table.setWidth(3, row, "70");
		//table.setWidth(4, row, "70");
		//table.setWidth(5, row, "104");

		row++;
		col = 1;

		// *** HEADING Pupil ***
		Text pupilTxt = new Text(localize(KEY_PUPIL_HEADING, "Pupil"));
		pupilTxt.setFontStyle(STYLE_UNDERLINED_SMALL_HEADER);
		table.add(pupilTxt, col++, row);
		table.setRowHeight(row, "20");
		table.setRowVerticalAlignment(row, Table.VERTICAL_ALIGN_BOTTOM);
		row++;
		col = 1;

		// empty space row
		table.add(transGIF, col, row);
		table.setRowHeight(row, "5");
		col = 1;
		row++;		
		
		// Personal Id Number
		table.add(getSmallHeader(localize(KEY_PERSONAL_ID_LABEL, "Personal id: ")), col++, row);
		if (pupil != null)
			table.add(getSmallText(pupil.getPersonalID()), col++, row);
		row++;
		col = 1;
		// Last Name
		table.add(getSmallHeader(localize(KEY_LAST_NAME_LABEL, "Last name: ")), col++, row);

		Table nameTable = new Table();
		col = 1;
		nameTable.setCellpadding(0);
		nameTable.setCellspacing(0);
		if (pupil != null)
			nameTable.add(getSmallText(pupil.getLastName()), col++, 1);
		// First Name       
		nameTable.add(getSmallHeader(localize(KEY_FIRST_NAME_LABEL, "First name: ")), col++, 1);
		if (pupil != null)
			nameTable.add(getSmallText(pupil.getFirstName()), col++, 1);
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
		if (pupil != null) {
			try {
				// pupil address
				Address address = getUserBusiness(iwc).getUsersMainAddress(pupil);
				StringBuffer aBuf = new StringBuffer(address.getStreetAddress());
				aBuf.append(", ");
				aBuf.append(address.getPostalCode().getPostalAddress());
				row--;
				table.add(getSmallText(aBuf.toString()), col, row);
				row++;
				// Get pupil phones
				Collection phones = pupil.getPhones();
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
	
	private Table getPlacementTable(IWContext iwc) throws RemoteException {
		Table table = new Table();
		//table.setColor("#DDDDDD");
		table.setBorder(0);
		table.setWidth(Table.HUNDRED_PERCENT);
		table.setCellpadding(1);
		table.setCellspacing(2);
		int col = 1;
		int row = 1;
		
		// *** HEADING Placements ***
		Text pupilTxt = new Text(localize(KEY_PLACEMENTS_HEADING, "Placements"));
		pupilTxt.setFontStyle(STYLE_UNDERLINED_SMALL_HEADER);
		table.add(pupilTxt, col++, row);
		table.setRowHeight(row, "40");
		table.setRowVerticalAlignment(row, Table.VERTICAL_ALIGN_BOTTOM);
		table.mergeCells(col, row, table.getColumns(), row);
		col = 1;
		row++;

		// empty space row
		table.add(transGIF, col, row);
		table.setRowHeight(row, "10");
		col = 1;
		row++;		
		
		//  *** Column headings ***
		table.add(getLocalizedSmallHeader(KEY_NUMBER, "No."), col, row);
		table.setAlignment(col++, row, Table.HORIZONTAL_ALIGN_CENTER);		
		table.add(getLocalizedSmallHeader(KEY_SCHOOL_TYPE, "School type"), col, row);
		table.setAlignment(col++, row, Table.HORIZONTAL_ALIGN_CENTER);
		table.add(getLocalizedSmallHeader(KEY_PROVIDER, "Provider"), col, row);
		table.setAlignment(col++, row, Table.HORIZONTAL_ALIGN_CENTER);
		table.add(getLocalizedSmallHeader(KEY_SCHOOL_YEAR, "School year"), col, row);		
		table.setAlignment(col++, row, Table.HORIZONTAL_ALIGN_CENTER);
		table.add(getLocalizedSmallHeader(KEY_STUDY_PATH, "Study path"), col, row);
		table.setAlignment(col++, row, Table.HORIZONTAL_ALIGN_CENTER);
		table.add(getLocalizedSmallHeader(KEY_SCHOOL_GROUP, "School group"), col, row);		
		table.setAlignment(col++, row, Table.HORIZONTAL_ALIGN_CENTER);
		table.add(getLocalizedSmallHeader(KEY_START_DATE, "Start date"), col, row);		
		table.setAlignment(col++, row, Table.HORIZONTAL_ALIGN_CENTER);
		table.add(getLocalizedSmallHeader(KEY_END_DATE, "End date"), col, row);		
		table.setAlignment(col++, row, Table.HORIZONTAL_ALIGN_CENTER);
		table.add(getLocalizedSmallHeader(KEY_REGISTRATOR, "Registrator"), col, row);		
		table.setAlignment(col++, row, Table.HORIZONTAL_ALIGN_CENTER);
		table.add(getLocalizedSmallHeader(KEY_REGISTRATION_CREATED_DATE, "Created date"), col, row);		
		table.setAlignment(col++, row, Table.HORIZONTAL_ALIGN_CENTER);
		table.add(getLocalizedSmallHeader(KEY_PLACEMENT_PARAGRAPH_SHORT, "Par"), col, row);		
		table.setAlignment(col++, row, Table.HORIZONTAL_ALIGN_CENTER);
		table.add(getLocalizedSmallHeader(KEY_NOTES, "Notes"), col, row);		
		table.setAlignment(col++, row, Table.HORIZONTAL_ALIGN_CENTER);
		table.add(Text.getNonBrakingSpace(), col, row);
		table.setAlignment(col++, row, Table.HORIZONTAL_ALIGN_CENTER);
		table.add(Text.getNonBrakingSpace(), col, row);
		table.setAlignment(col++, row, Table.HORIZONTAL_ALIGN_CENTER);
		
		table.setRowColor(row, getHeaderColor());
				

		table.setRowHeight(row, "18");
		row++;
		
		// Loop placements
		Collection placements = null;
		try {
			if (pupil != null) {
				placements = getSchoolBusiness(iwc).getSchoolClassMemberHome()
																		   .findAllOrderedByRegisterDate(pupil);
				
			}
		} catch (FinderException e) {}		
		
		if (placements != null && placements.size() > 0) {
			int zebra = 0;
			int rowNum = 0;
			table.add(new HiddenInput(PARAM_REMOVE_PLACEMENT, "-1"), 1, 1);
			for (Iterator iter = placements.iterator(); iter.hasNext();) {
				rowNum++;
				SchoolClassMember plc = (SchoolClassMember) iter.next();
				col = 1;
				
				// Row number
				table.add(getSmallText(String.valueOf(rowNum)), col++, row);
				// School type
				try {
					table.add(getSmallText(plc.getSchoolType().getName()), col++, row);						
				} catch (Exception e) {col++;}
				// Provider
				try {
					table.add(getSmallText(plc.getSchoolClass().getSchool().getName()), col++, row);						
				} catch (Exception e) {col++;}
				// School year
				try {
					table.add(getSmallText(plc.getSchoolYear().getName()), col++, row);						
				} catch (Exception e) {col++;}
				// Study path
				try {
					if (plc.getStudyPathId() != -1) {
						SchoolStudyPathHome  home = (SchoolStudyPathHome) IDOLookup.getHome(SchoolStudyPath.class);
						SchoolStudyPath sp = home.findByPrimaryKey(new Integer(plc.getStudyPathId()));
						table.add(getSmallText(sp.getCode()), col, row);
					}
				} catch (Exception e) {}
				col++;
				// School type
				try {
					table.add(getSmallText(plc.getSchoolClass().getSchoolClassName()), col++, row);						
				} catch (Exception e) {col++;}
				// Start date
				try {
					String dateStr = getCentralPlacementBusiness(iwc).getDateString(
																							plc.getRegisterDate(), "yyyy-MM-dd");
					table.add(getSmallText(dateStr), col++, row);
				} catch (Exception e) {col++;}		
				// End date
				try {
					String dateStr = getCentralPlacementBusiness(iwc).
																getDateString(plc.getRemovedDate(), "yyyy-MM-dd");
					table.add(getSmallText(dateStr), col++, row);
				} catch (Exception e) {col++;}
				// Registrator
				try {
					int registratorID = plc.getRegistratorId();
					User registrator = getUserBusiness(iwc).getUser(registratorID);
					if (registrator != null) {
						Name name = new Name(registrator.getFirstName(), registrator.getMiddleName(), registrator.getLastName());
						table.add(getSmallText(name.getName(iwc.getApplicationSettings().getDefaultLocale(), false)), col++, row);
					}
				} catch (Exception e) {col++;}
				// Created date
				try {
					String dateStr = getCentralPlacementBusiness(iwc).
														getDateString(plc.getRegistrationCreatedDate(), "yyyy-MM-dd");
					table.add(getSmallText(dateStr), col++, row);
				} catch (Exception e) {col++;}
				// Placement paragraph
				try {
					if (plc.getPlacementParagraph() != null)
						table.add(getSmallText(plc.getPlacementParagraph()), col++, row);
					else
						col++;
				} catch (Exception e) {col++;}
				// Notes
				try {
					if (plc.getNotes() != null)
						table.add(getSmallText(plc.getNotes()), col++, row);
					else
						col++;
				} catch (Exception e) {col++;}
				// Pupil overview button
				try {
					// Get Pupil overview button					
					String plcId =  ((Integer) plc.getPrimaryKey()).toString();
					String schClassId = String.valueOf(plc.getSchoolClassId());

					Link editButt = new Link(this.getEditIcon(localize(KEY_TOOLTIP_PUPIL_OVERVIEW, "Pupil overview")));
					editButt.setWindowToOpen(PlacementHistoryEditPlacement.class);
					editButt.setParameter(SchoolAdminOverview.PARAMETER_METHOD, String.valueOf(SchoolAdminOverview.METHOD_OVERVIEW));
					editButt.addParameter(SchoolAdminOverview.PARAMETER_METHOD, String.valueOf(SchoolAdminOverview.METHOD_OVERVIEW));
					editButt.addParameter(SchoolAdminOverview.PARAMETER_SHOW_ONLY_OVERVIEW, "true");
					editButt.addParameter(SchoolAdminOverview.PARAMETER_SHOW_NO_CHOICES, "true");
					editButt.addParameter(SchoolAdminOverview.PARAMETER_PAGE_ID, getParentPage().getPageID());
					editButt.addParameter(SchoolAdminOverview.PARAMETER_USER_ID, String.valueOf(plc.getClassMemberId()));
					editButt.addParameter(SchoolAdminOverview.PARAMETER_SCHOOL_CLASS_ID, schClassId);        
					editButt.addParameter(SchoolAdminOverview.PARAMETER_SCHOOL_CLASS_MEMBER_ID, plcId);
					editButt.addParameter(SchoolAdminOverview.PARAMETER_RESOURCE_PERMISSION, 
							SchoolAdminOverview.PARAMETER_RESOURCE_PERM_VALUE_CENTRAL_ADMIN);
					editButt.addParameter(SchoolAdminOverview.PARAMETER_FROM_CENTRAL_PLACEMENT_EDITOR, "true");
					if (plc.getRemovedDate() != null)
						editButt.addParameter(SchoolAdminOverview.PARAMETER_SCHOOL_CLASS_MEMBER_REMOVED_DATE, plc.getRemovedDate().toString());									
				
					table.add(editButt, col, row);
					table.setAlignment(col++, row, Table.HORIZONTAL_ALIGN_CENTER);
				} catch (Exception e) {col++;}				
				// Remove button
				try {
					// Get remove button      
					Image delImg = getDeleteIcon(localize(KEY_TOOLTIP_REMOVE_PLC, "Delete placement"));
					int plcID = ((Integer) plc.getPrimaryKey()).intValue();

					SubmitButton delButt = new SubmitButton(delImg);
					delButt.setValueOnClick(PARAM_REMOVE_PLACEMENT, String.valueOf(plcID));							
					delButt.setSubmitConfirm(localize(KEY_CONFIRM_REMOVE_PLC_MSG, 
									"Do you really want to erase this school placement and its resource placements?"));
					delButt.setToolTip(localize(KEY_TOOLTIP_REMOVE_PLC, "Delete school placement"));
					table.add(delButt, col, row);
					table.setAlignment(col, row, Table.HORIZONTAL_ALIGN_CENTER);
				} catch (Exception e) {}
				
				String zebraColor = zebra % 2 == 0 ? getZebraColor2() : getZebraColor1();		
				table.setRowColor(row, zebraColor);
				col = 2;
				row++;				
				
				// Resources
				String rscStr = getResourceBusiness(iwc).getResourcesStringXtraInfo(plc);
				if (!("".equals(rscStr))) {
					table.add(getSmallText("<i>" + localize(KEY_RESOURCES, "Resources")+":</i> "), col, row);
					table.add(getSmallText("<i>" + rscStr + "</i>"), col, row);
					table.setRowColor(row, zebraColor);
					table.mergeCells(col, row, table.getColumns(), row);
					row++;
				}
				
				zebra++;				
			}			
		}
		
		col = 1;

		// empty space row
		table.add(transGIF, col, row);
		table.setRowHeight(row, "20");


		return table;
	}

	
	private User getPupilFromParam(IWContext iwc) throws RemoteException {
		User child = null;
		// Parameter name returning chosen User from SearchUserModule
		uniqueUserSearchParam = SearchUserModule.getUniqueUserParameterName(UNIQUE_SUFFIX);

		if (iwc.isParameterSet(PARAM_PUPIL_ID)) {
			String idStr = iwc.getParameter(PARAM_PUPIL_ID);
			int childID = Integer.parseInt(idStr);
			child = getUserBusiness(iwc).getUser(childID);
		} else if (iwc.isParameterSet(uniqueUserSearchParam)) {
			int childID = Integer.parseInt(iwc.getParameter(uniqueUserSearchParam));
			child = getUserBusiness(iwc).getUser(childID);
		}
		
		return child;
	}

	private SearchUserModule getSearchUserModule(/*IWContext iwc*/) {
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
		
		/*if (iwc.isParameterSet(uniqueUserSearchParam)) {
			searcher.maintainParameter(new Parameter(uniqueUserSearchParam, 
														iwc.getParameter(uniqueUserSearchParam)));
		}*/

		return searcher;
	}

	public CommuneUserBusiness getUserBusiness(IWContext iwc) throws RemoteException {
		return (CommuneUserBusiness) 
										IBOLookup.getServiceInstance(iwc, CommuneUserBusiness.class);
	}

	public SchoolBusiness getSchoolBusiness(IWContext iwc) throws RemoteException {
		return (SchoolBusiness) IBOLookup.getServiceInstance(iwc, SchoolBusiness.class);
	}

	public CentralPlacementBusiness getCentralPlacementBusiness(IWContext iwc) 
																										throws RemoteException {
		return (CentralPlacementBusiness) 
											IBOLookup.getServiceInstance(iwc, CentralPlacementBusiness.class);
	}

	private ResourceBusiness getResourceBusiness(IWContext iwc) throws RemoteException {
		return (ResourceBusiness) IBOLookup.getServiceInstance(iwc, ResourceBusiness.class);
	}



}
