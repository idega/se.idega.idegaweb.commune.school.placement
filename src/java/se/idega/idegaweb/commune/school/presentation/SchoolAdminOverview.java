package se.idega.idegaweb.commune.school.presentation;

import is.idega.block.family.business.FamilyLogic;
import is.idega.block.family.business.NoCustodianFound;
import java.rmi.RemoteException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.ejb.CreateException;
import javax.ejb.FinderException;
import se.idega.idegaweb.commune.business.CommuneUserBusiness;
import se.idega.idegaweb.commune.care.resource.business.ClassMemberException;
import se.idega.idegaweb.commune.care.resource.business.DateException;
import se.idega.idegaweb.commune.care.resource.business.ResourceBusiness;
import se.idega.idegaweb.commune.care.resource.business.ResourceException;
import se.idega.idegaweb.commune.care.resource.data.Resource;
import se.idega.idegaweb.commune.care.resource.data.ResourceClassMember;
import se.idega.idegaweb.commune.care.resource.data.ResourcePermission;
import se.idega.idegaweb.commune.presentation.CommuneBlock;
import se.idega.idegaweb.commune.school.accounting.business.PlacementBusiness;
import se.idega.idegaweb.commune.school.business.CentralPlacementBusiness;
import se.idega.idegaweb.commune.school.business.SchoolCommuneBusiness;
import se.idega.idegaweb.commune.school.business.SchoolCommuneSession;
import se.idega.idegaweb.commune.school.data.SchoolChoice;
import se.idega.util.PIDChecker;
import com.idega.block.school.business.SchoolBusiness;
import com.idega.block.school.business.SchoolBusinessBean;
import com.idega.block.school.business.SchoolYearComparator;
import com.idega.block.school.data.School;
import com.idega.block.school.data.SchoolClass;
import com.idega.block.school.data.SchoolClassMember;
import com.idega.block.school.data.SchoolSeason;
import com.idega.block.school.data.SchoolStudyPath;
import com.idega.block.school.data.SchoolYear;
import com.idega.block.school.presentation.SchoolClassDropdownDouble;
import com.idega.builder.business.BuilderLogic;
import com.idega.business.IBOLookup;
import com.idega.core.contact.data.Email;
import com.idega.core.contact.data.Phone;
import com.idega.core.localisation.data.ICLanguage;
import com.idega.core.localisation.data.ICLanguageHome;
import com.idega.core.location.data.Address;
import com.idega.core.location.data.PostalCode;
import com.idega.data.IDOLookup;
import com.idega.idegaweb.IWBundle;
import com.idega.presentation.IWContext;
import com.idega.presentation.Table;
import com.idega.presentation.text.Break;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.CheckBox;
import com.idega.presentation.ui.CloseButton;
import com.idega.presentation.ui.DateInput;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.GenericButton;
import com.idega.presentation.ui.HiddenInput;
import com.idega.presentation.ui.PrintButton;
import com.idega.presentation.ui.RadioButton;
import com.idega.presentation.ui.SelectDropdownDouble;
import com.idega.presentation.ui.SubmitButton;
import com.idega.presentation.ui.TextArea;
import com.idega.presentation.ui.TextInput;
import com.idega.user.business.NoEmailFoundException;
import com.idega.user.business.NoPhoneFoundException;
import com.idega.user.data.User;
import com.idega.util.Age;
import com.idega.util.IWCalendar;
import com.idega.util.IWTimestamp;
import com.idega.util.PersonalIDFormatter;
import com.idega.util.URLUtil;
import com.idega.util.text.Name;
import com.idega.util.text.TextSoap;

/**
 * @author laddi Last modified: $Date: 2003/10/02 12:09:35 $ by $Author: staffan $
 * @version $Revision: 1.48 $
 */

public class SchoolAdminOverview extends CommuneBlock {

	public static final String PARAMETER_ACTION = "sch_admin_action";
	public static final String PARAMETER_CHOICE_ID = "sch_choice_id";
	public static final String PARAMETER_COMMENT = "sch_comment";
	public static final String PARAMETER_DELETE_RESOURCE_PLACEMENT = "delete_resource_placement";
	public static final String PARAMETER_METHOD = "sch_admin_method";
	public static final String PARAMETER_PAGE_ID = "sch_page_id";
	public static final String PARAMETER_RESOURCE_CHOICE_STATUS = "resource_school_choice_status";
	public static final String PARAMETER_RESOURCE_CLASS_MEMBER = "resource_school_member";
	public static final String PARAMETER_RESOURCE_ENDDATE = "resource_enddate";
	public static final String PARAMETER_RESOURCE_ID = "cacc_resource_id";
	public static final String PARAMETER_RESOURCE_NAME = "sch_resource_name";
	public static final String PARAMETER_RESOURCE_SEASON = "school_choice_season";
	public static final String PARAMETER_RESOURCE_STARTDATE = "resource_startdate";
	public static final String PARAMETER_RESOURCE_STUDENT = "resource_student";
	public static final String PARAMETER_RESOURCE_PERMISSION = "resource_permission";
	public static final String PARAMETER_SCHOOL_CLASS_ID = "sch_class_id";
	public static final String PARAMETER_SCHOOL_CLASS_MEMBER_ID = "sch_class_member_id";
	public static final String PARAMETER_SCHOOL_CLASS_MEMBER_REMOVED_DATE = "sch_class_member_removed";
	public static final String PARAMETER_SCHOOL_MEMBER_ID = "sch_member_id";
	public static final String PARAMETER_SEARCH = "sch_search";
	public static final String PARAMETER_SHOW_NO_CHOICES = "sch_show_no_choices";
	public static final String PARAMETER_SHOW_ONLY_OVERVIEW = "sch_show_only_overview";
	public static final String PARAMETER_USER_ID = "sch_user_id";
	public static final String PARAMETER_SET_AS_DEFAULT = "rem_rej_m";
	public static final String PARAMETER_NATIVE_LANG = "sch_native_lang";
	
	private static final String PARAMETER_REJECT_STUDENT = "sch_rej_st";
	private static final String PARAMETER_PLACEMENT_OFFER = "sch_pl_o";
	private static final String PARAMETER_PLACEMENT_CONFIRMATION = "sch_pl_con";

	public static final String PARAMETER_FROM_CENTRAL_PLACEMENT_EDITOR = "from_central_placement_editor";
	public static final String PARAMETER_RESOURCE_PERM_VALUE_CENTRAL_ADMIN = "resource_perm_central_admin";
	public static final Integer VIEW_ALL_RESOURCES_GRP_ID = new Integer("-7");
	
	public static final int METHOD_OVERVIEW = 1;
	public static final int METHOD_REJECT = 2;
	public static final int METHOD_REPLACE = 3;
	public static final int METHOD_MOVE_GROUP = 5;
	public static final int METHOD_MOVE_YEAR = 6;
	public static final int METHOD_FINALIZE_GROUP = 7;
	public static final int METHOD_EDIT_STUDENT = 8;
	public static final int METHOD_ADD_STUDENT = 9;
	public static final int METHOD_CHANGE_PLACEMENT_DATE = 10;
	public static final int METHOD_LIST_RESOURCES = 11;
	public static final int METHOD_NEW_RESOURCE = 12;
	public static final int METHOD_FINISH_RESOURCE = 13;
	public static final int METHOD_CHANGE_STUDY_PATH = 14;
	public static final int METHOD_NATIVE_LANG_FORM = 15;
	public static final int METHOD_MESSAGE_TEXT = 16;

	public static final int ACTION_REJECT = 1;
	public static final int ACTION_REPLACE = 2;
	public static final int ACTION_MOVE_GROUP = 4;
	public static final int ACTION_MOVE_YEAR = 5;
	public static final int ACTION_FINALIZE_GROUP = 6;
	public static final int ACTION_EDIT_STUDENT = 7;
	public static final int ACTION_ADD_STUDENT = 8;
	public static final int ACTION_CREATE_STUDENT = 9;
	public static final int ACTION_CHANGE_PLACEMENT_DATE = 10;
	public static final int ACTION_SAVE_RESOURCE = 11;
	public static final int ACTION_DELETE_RESOURCE = 12;
	public static final int ACTION_FINISH_RESOURCE = 13;
	public static final int ACTION_CHANGE_STUDY_PATH = 14;
	public static final int ACTION_SAVE_NATIVE_LANGUAGE = 15;
	private static final int ACTION_SAVE_MESSAGE_TEXT = 16;
	
	private static final String PARAMETER_REJECT_MESSAGE = "sch_admin_reject_message";
	private static final String PARAMETER_REPLACE_MESSAGE = "sch_admin_replace_message";
	private static final String PARAMETER_PROTOCOL = "sch_admin_protocol";
	private static final String PARAMETER_SCHOOL_ID = "sch_school_id";
	private static final String PARAMETER_DATE = "sch_date";
	private static final String PARAMETER_FINALIZE_SUBJECT = "sch_admin_finalize_subject";
	private static final String PARAMETER_FINALIZE_BODY = "sch_admin_finalize_body";
	
	private int _method = -1;
	private int _action = -1;

	private int _userID = -1;
	private int _choiceID = -1;
	private int _schoolID = -1;
	private int _schoolClassID = -1;
	private int _schoolYearID = -1;
	private int _schoolClassMemberID = -1;
	
	private boolean _protocol = true;
	//private boolean _move = true;
	private boolean _showOnlyOverview = false;
	private boolean _showNoChoices = false;
	private boolean _viewAllResources = false;
	private boolean _fromCentralPlacementEditor = false;

	private CloseButton close;
	private String searchString;
	private String resourceErrorMsg;

	private int _pageID;
	private Form form;

	/**
	 * @see com.idega.presentation.PresentationObject#main(IWContext)
	 */
	public void main(IWContext iwc) throws Exception {
		setResourceBundle(getResourceBundle(iwc));
		parse(iwc);

		switch (_action) {
		case ACTION_REJECT :
			reject(iwc);
			break;
		case ACTION_REPLACE :
			replace(iwc);
			break;
		case ACTION_MOVE_GROUP :
			moveGroup(iwc);
			break;
		case ACTION_MOVE_YEAR :
			moveYear(iwc);
			break;
		case ACTION_FINALIZE_GROUP :
			finalizeGroup(iwc);
			break;
		case ACTION_EDIT_STUDENT :
			editStudent(iwc);
			break;
		case ACTION_ADD_STUDENT :
			addStudent(iwc);
			break;
		case ACTION_CREATE_STUDENT :
			createStudent(iwc);
			break;
		case ACTION_CHANGE_PLACEMENT_DATE :
			changePlacementDate(iwc);
			break;
		case ACTION_SAVE_RESOURCE :
			resourceErrorMsg = saveResource(iwc);
			if (resourceErrorMsg != null)
				_method = METHOD_NEW_RESOURCE;
			break;
		case ACTION_DELETE_RESOURCE :
			deleteResource(iwc);
			break;
		case ACTION_FINISH_RESOURCE :
			resourceErrorMsg = finishResource(iwc);
			if (resourceErrorMsg != null)
				_method = METHOD_FINISH_RESOURCE;
			break;
		case ACTION_CHANGE_STUDY_PATH :
			changeStudyPath(iwc);
			break;
		case ACTION_SAVE_NATIVE_LANGUAGE :
			saveNativeLanguage(iwc);
			break;
		case ACTION_SAVE_MESSAGE_TEXT :
			saveMessageText(iwc);
			break;

		}

		if (_method != -1)
			drawForm(iwc);
	}

	private void drawForm(IWContext iwc) throws RemoteException {
		form = new Form();
		form.maintainParameter(PARAMETER_USER_ID);
		form.maintainParameter(PARAMETER_CHOICE_ID);
		form.maintainParameter(PARAMETER_PAGE_ID);
		form.maintainParameter(PARAMETER_SCHOOL_CLASS_MEMBER_ID);
		form.maintainParameter(PARAMETER_RESOURCE_PERMISSION);
		form.maintainParameter(PARAMETER_SHOW_NO_CHOICES);
		form.maintainParameter(PARAMETER_SHOW_ONLY_OVERVIEW);
		form.maintainParameter(PARAMETER_FROM_CENTRAL_PLACEMENT_EDITOR);
		form.setStyleAttribute("height:100%");

		Table table = new Table(3, 5);
		table.setRowColor(1, "#000000");
		table.setRowColor(3, "#000000");
		table.setRowColor(5, "#000000");
		table.setColumnColor(1, "#000000");
		table.setColumnColor(3, "#000000");
		table.setColor(2, 2, "#CCCCCC");
		table.setWidth(Table.HUNDRED_PERCENT);
		table.setWidth(2, Table.HUNDRED_PERCENT);
		table.setHeight(Table.HUNDRED_PERCENT);
		table.setHeight(4, Table.HUNDRED_PERCENT);
		table.setCellpadding(0);
		table.setCellspacing(0);
		form.add(table);

		Table headerTable = new Table(1, 1);
		headerTable.setCellpadding(6);
		table.add(headerTable, 2, 2);

		Table contentTable = new Table(1, 1);
		contentTable.setCellpadding(10);
		contentTable.setWidth(Table.HUNDRED_PERCENT);
		contentTable.setHeight(Table.HUNDRED_PERCENT);
		table.add(contentTable, 2, 4);

		close = (CloseButton) getStyledInterface(new CloseButton(localize("close_window", "Close")));

		
		String userName= null;
		String personalId = null;
		String personalIdUserName = "";
		
		User child;
		
		if (_userID != -1) {
			child = getUserBusiness(iwc).getUser(_userID);
			if (child != null){
				personalId = child.getPersonalID();
				userName = getUserBusiness(iwc).getNameLastFirst(child, true);
				personalIdUserName =  "  -  " + userName + "   " + personalId;
			}
		}
				
		
		
		switch (_method) {
		case METHOD_OVERVIEW :
			headerTable.add(getHeader(localize("school.student_overview", "Student overview") + "  -  " + userName), 1, 1);
			contentTable.add(getOverview(iwc), 1, 1);
			break;
		case METHOD_REJECT :
			headerTable.add(getHeader(localize("school.reject_student", "Reject student") + personalIdUserName), 1, 1);
			contentTable.add(getRejectForm(iwc), 1, 1);
			break;
		case METHOD_REPLACE :
			headerTable.add(getHeader(localize("school.student_replacing", "Replace student") + personalIdUserName), 1, 1);
			contentTable.add(getReplaceForm(iwc), 1, 1);
			break;
		case METHOD_MOVE_GROUP :
			headerTable.add(getHeader(localize("school.student_move_group", "Move student to group") + personalIdUserName), 1, 1);
			contentTable.add(getMoveGroupForm(iwc), 1, 1);
			break;
		case METHOD_MOVE_YEAR :
			headerTable.add(getHeader(localize("school.student_move_year", "Change year of school choice") + personalIdUserName), 1, 1);
			contentTable.add(getMoveYearForm(iwc), 1, 1);
			break;
		case METHOD_FINALIZE_GROUP :
			headerTable.add(getHeader(localize("school.finalize_group", "Finalize group")), 1, 1);
			contentTable.add(getFinalizeGroupForm(iwc), 1, 1);
			break;
		case METHOD_EDIT_STUDENT :
			headerTable.add(getHeader(localize("school.edit_student", "Edit student") + personalIdUserName), 1, 1);
			contentTable.add(getEditStudentForm(iwc), 1, 1);
			break;
		case METHOD_ADD_STUDENT :
			headerTable.add(getHeader(localize("school.add_student", "Add student") + personalIdUserName), 1, 1);
			contentTable.add(getAddStudentForm(iwc), 1, 1);
			break;
		case METHOD_CHANGE_PLACEMENT_DATE :
			headerTable.add(getHeader(localize("school.change_placement_date", "Change placement date") + personalIdUserName), 1, 1);
			contentTable.add(getChangePlacementDateForm(iwc), 1, 1);
			break;
		case METHOD_LIST_RESOURCES :
			headerTable.add(getHeader(localize("school.resources.current", "Current Resources") + personalIdUserName), 1, 1);
			contentTable.add(getResourceList(iwc), 1, 1);
			break;
		case METHOD_NEW_RESOURCE :
			headerTable.add(getHeader(localize("school.resources.new", "New Resource")), 1, 1);
			contentTable.add(getResourceForm(iwc), 1, 1);
			break;
		case METHOD_FINISH_RESOURCE :
			headerTable.add(getHeader(localize("school.resources.finish", "Finish Resource")), 1, 1);
			contentTable.add(getFinishResourceForm(iwc), 1, 1);
			break;
		case METHOD_CHANGE_STUDY_PATH :
			headerTable.add(getHeader(localize("school.change_study_path", "Change Study Path") + personalIdUserName), 1, 1);
			contentTable.add(getChangeStudyPathForm(iwc), 1, 1);
			break;
		case METHOD_NATIVE_LANG_FORM :
			headerTable.add(getHeader(localize("school.native_language", "Native language") + personalIdUserName), 1, 1);
			contentTable.add(getNativeLanguageForm(iwc), 1, 1);
			break;
		case METHOD_MESSAGE_TEXT :
			headerTable.add(getHeader(localize("school.message_text", "Message Text")), 1, 1);
			contentTable.add(getMessageTextForm(iwc), 1, 1);
			break;
		}

		add(form);
	}

	private Table getOverview(IWContext iwc) throws RemoteException {
		Table table = new Table();
		table.setBorder(0);
		table.setCellpadding(5);
		table.setWidth(Table.HUNDRED_PERCENT);
		table.setHeight(Table.HUNDRED_PERCENT);
		int row = 1;
		String sLanguage = null;
		boolean isCommuneAdmin = isCommuneAdministrator(iwc);
		String schoolClassId = iwc.getParameter(PARAMETER_SCHOOL_CLASS_ID);
//		String schoolClassMemberId = iwc.getParameter(PARAMETER_SCHOOL_CLASS_MEMBER_ID);

		if (_userID != -1) {
			User user = getUserBusiness(iwc).getUser(_userID);
			Address address = getUserBusiness(iwc).getUserAddress1(_userID);
			
			
			table.add(getSmallHeader(localize("school.name", "Name")), 1, row);
			Name name = new Name(user.getFirstName(), user.getMiddleName(), user.getLastName());
			table.add(getSmallText(name.getName(iwc.getApplicationSettings().getDefaultLocale(), true)), 2, row++);

			table.add(getSmallHeader(localize("school.personal_id", "Personal ID")), 1, row);
			table.add(getSmallText(PersonalIDFormatter.format(user.getPersonalID(), iwc.getCurrentLocale())), 2, row++);

			table.add(getSmallHeader(localize("school.address", "Address")), 1, row);
			if (address != null)
				table.add(getSmallText(address.getStreetAddress() + ", " + address.getPostalAddress()), 2, row++);
			else
				row++;

			
			try {
				Collection parents = getMemberFamilyLogic(iwc).getCustodiansFor(user);
				table.add(getSmallHeader(localize("school.custodians", "Custodians")), 1, row);
				if (parents != null && !parents.isEmpty()) {
					Iterator iter = parents.iterator();
					while (iter.hasNext()) {
						User parent = (User) iter.next();
						name = new Name(parent.getFirstName(), parent.getMiddleName(), parent.getLastName());
						table.add(getSmallText(name.getName(iwc.getApplicationSettings().getDefaultLocale(), true)), 2, row);
						
						
						try {
							Address addressParent = getCommuneUserBusiness(iwc).getUsersMainAddress(parent);
							
							if (addressParent != null) {
								table.add(new Break(), 2, row);
								table.add(getSmallText(localize("school.address", "Address") + ": "), 2, row);
								table.add(getSmallText(addressParent.getStreetAddress() + ", " + addressParent.getPostalAddress()), 2, row);
							}
							
						}
						catch (Exception naf) {
						}
						
						try {
							Phone phone = getCommuneUserBusiness(iwc).getUsersHomePhone(parent);
							
							if (phone != null && phone.getNumber() != null) {
								table.add(new Break(), 2, row);
								table.add(getSmallText(localize("school.phone", "Phone") + ": "), 2, row);
								table.add(getSmallText(phone.getNumber()), 2, row);
							}
						}
						catch (NoPhoneFoundException npf) {
							log(npf);
						}
						
						try {
							Phone phoneMobile = getCommuneUserBusiness(iwc).getUsersMobilePhone(parent);
							if (phoneMobile != null && phoneMobile.getNumber() != null) {
								table.add(new Break(), 2, row);
								table.add(getSmallText(localize("school.phone_mobile", "Mobile phone") + ": "), 2, row);
								table.add(getSmallText(phoneMobile.getNumber()), 2, row);
							}
						}
						catch (NoPhoneFoundException npf) {
							log(npf);
						}
						
						try {
							Phone phoneWork = getCommuneUserBusiness(iwc).getUsersWorkPhone(parent);
							if (phoneWork != null && phoneWork.getNumber() != null) {
								table.add(new Break(), 2, row);
								table.add(getSmallText(localize("school.phone_work", "Work phone") + ": "), 2, row);
								table.add(getSmallText(phoneWork.getNumber()), 2, row);
							}
							
						}
						catch (NoPhoneFoundException npf) {
							log(npf);
						}
						
						try {
							Email email = getCommuneUserBusiness(iwc).getUsersMainEmail(parent);
							if (email != null && email.getEmailAddress() != null) {
								Link emailLink = this.getSmallLink(email.getEmailAddress());
								emailLink.setURL("mailto:" + email.getEmailAddress());
								table.add(new Break(), 2, row);
								table.add(getSmallText(localize("school.email", "E-mail") + ": "), 2, row);
								table.add(emailLink, 2, row);
							}
						}
						catch (NoEmailFoundException nef) {
						}
						if (iter.hasNext())
							table.add(new Break(2), 2, row);
					}
				}
				row++;
			}
			catch (NoCustodianFound ncf) {
			}
			
			if (schoolClassId != null) {
				try {
					SchoolClassMember scMember = getSchoolCommuneBusiness(iwc).getSchoolBusiness().findSchoolClassMember(_userID, Integer.parseInt(schoolClassId));
					//String scID = getSchoolCommuneBusiness(iwc).getSchoolBusiness().findSchoolClass()
					if (scMember != null) {
						String notes = scMember.getNotes();
//						sLanguage = scMember.getLanguage();
						if (notes != null) {
							table.add(getSmallHeader(localize("school.comment", "Comment")), 1, row);
							table.add(getSmallText(notes), 2, row);
						}
						++row;
					}
				}
				catch (Exception e) {
					++row;
				}
			}
			else {
				++row;
			}
			
			int pendingSchoolId = -1;
			boolean showChangePlacementDate = false;
			boolean isPlaced = false;
			School oldSchool = null;
			//int oldSchoolId = -1;
			SchoolClassMember schClMem = null;
			SchoolClassMember preSchoolClMem = null;
			String language = null;
			SchoolYear schoolYear = null;
			String schoolClass = null;
			//SchoolChoiceBusiness schBuiz;
			int previousSeasonId = -1;
			SchoolSeason preSchoolseason = null;
			boolean started = false;
			
			if (!_showNoChoices) {
				Collection choices = getSchoolCommuneBusiness(iwc).getSchoolChoiceBusiness().findByStudentAndSeason(_userID, getSchoolCommuneSession(iwc).getSchoolSeasonID());
				String message = null;
				String extraChoice = null;
				
				
				
				IWCalendar calendar = null;
				IWTimestamp placementDate = null;
				if (!choices.isEmpty()) {
					table.add(getSmallHeader(localize("school.school_choice", "School choices")), 1, row);

					School school;
					SchoolChoice choice;
					SchoolSeason schSeason = null;
					Iterator iter = choices.iterator();
					while (iter.hasNext()) {
						choice = (SchoolChoice) iter.next();

						if (choice.getChosenSchoolId() != -1) {
							school = getSchoolCommuneBusiness(iwc).getSchoolBusiness().getSchool(new Integer(choice.getChosenSchoolId()));
							String string = String.valueOf(choice.getChoiceOrder()) + ". " + school.getName() + " (" + getSchoolCommuneBusiness(iwc).getLocalizedCaseStatusDescription(choice.getCaseStatus(), iwc.getCurrentLocale()) + ")";
							
														
							if (schClMem == null) {
								try {								
									schSeason = getSchoolCommuneBusiness(iwc).getSchoolChoiceBusiness().getSchoolSeasonHome().findByPrimaryKey(new Integer(getSchoolCommuneSession(iwc).getSchoolSeasonID()));									
									schClMem = getSchoolCommuneBusiness(iwc).getSchoolBusiness().getSchoolClassMemberHome().findLatestByUserAndSchCategoryAndSeason(user, getSchoolCommuneBusiness(iwc).getSchoolBusiness().getCategoryElementarySchool(), schSeason);
									
								}
								catch (FinderException fe){
									log(fe);
								}
							}
							
							
							if (choice.getStatus().equalsIgnoreCase("PREL") || choice.getStatus().equalsIgnoreCase("PLAC") || choice.getStatus().equalsIgnoreCase("FLYT")) {
								if (pendingSchoolId == -1)
									pendingSchoolId = choice.getChosenSchoolId();
								//							table.add("gimmi flippari ", 2, row);
								
								if (choice.getStatus().equalsIgnoreCase("PLAC")){
									isPlaced = true;
									if (schClMem != null){
										schoolClass = schClMem.getSchoolClass().getName();
									}
									
								}
								
							}
							if (choice.getChosenSchoolId() == getSchoolCommuneSession(iwc).getSchoolID()) {
								if (choice.getStatus().equalsIgnoreCase("FLYT")) {
									showChangePlacementDate = true;
								}
								table.add(this.getSmallHeader(string), 2, row);
								extraChoice = choice.getExtraChoiceMessage();
							}
							else {
								table.add(getSmallText(string), 2, row);
							}
						}
						else {
							table.add(getSmallHeader(localize("school.moving_out_of_community", "Moving out of community")), 2, row);
						}

						if (iter.hasNext())
							table.add(new Break(), 2, row);
						if (oldSchool == null){
							oldSchool = choice.getCurrentSchool();
							//oldSchoolId = choice.getCurrentSchoolId();
						}
						if (message == null)
							message = choice.getMessage();
						
						if (language == null) {
							language = choice.getLanguageChoice();
						}
						if (schoolYear == null){
							if (schClMem != null){
								schoolYear = schClMem.getSchoolYear();								
							}
							else{
								schoolYear = choice.getSchoolYear();
							}
						}
						
						IWTimestamp today = IWTimestamp.RightNow();
						if (placementDate == null && choice.getPlacementDate() != null) {
							placementDate = new IWTimestamp(choice.getPlacementDate());
							started = placementDate.getTimestamp().before(today.getDate());
						}
						calendar = new IWCalendar(iwc.getCurrentLocale(), choice.getCreated());
								
						}
					row++;
				}
				

				if (schoolYear != null) {
					table.add(getSmallHeader(localize("school.school_choice_year", "School year")), 1, row);
					table.add(getSmallText(schoolYear.getName()), 2, row++);
				}
				if (schoolClass != null) {
					table.add(getSmallHeader(localize("school.school_class", "School class")), 1, row);
					table.add(getSmallText(schoolClass), 2, row++);
				}
				
				if (calendar != null) {
					table.add(getSmallHeader(localize("school.appl_date", "Application date")), 1, row);
					table.add(getSmallText(calendar.getLocaleDate(IWCalendar.SHORT)), 2, row++);
				}
				
				if (language != null && language.length() > 0 && !language.equals("-1")) {
					table.add(getSmallHeader(localize("school.school_choice_language", "Preferred language")), 1, row);
					table.add(getSmallText(localize(language, language)), 2, row++);
				}
				if (placementDate != null) {
					table.add(getSmallHeader(localize("school.placement_date", "Placement date")), 1, row);
					table.add(getSmallText(placementDate.getLocaleDate(iwc.getCurrentLocale(), IWTimestamp.SHORT)), 2, row++);
					showChangePlacementDate = true;
				}
				if (message != null) {
					table.add(getSmallHeader(localize("school.school_choice_message", "Applicant message")), 1, row);
					table.add(getSmallText(message), 2, row++);
				}
				if (extraChoice != null) {
					table.add(getSmallHeader(localize("school.extra_choice_message", "Extra choice")), 1, row);
					table.add(getSmallText(extraChoice), 2, row++);
				}
			}

			// if not school change
			if (preSchoolClMem == null && !showChangePlacementDate){
				try {
					previousSeasonId = getSchoolCommuneBusiness(iwc).getPreviousSchoolSeasonID(getSchoolCommuneSession(iwc).getSchoolSeasonID());
					preSchoolseason = getSchoolCommuneBusiness(iwc).getSchoolChoiceBusiness().getSchoolSeasonHome().findByPrimaryKey(new Integer(previousSeasonId));
					
					preSchoolClMem = getSchoolCommuneBusiness(iwc).getSchoolBusiness().getSchoolClassMemberHome().findLatestByUserAndSchCategoryAndSeason(user, getSchoolCommuneBusiness(iwc).getSchoolBusiness().getCategoryElementarySchool(), preSchoolseason);
					
					
				} catch (FinderException fe){
					log(fe);
					if (preSchoolClMem == null) {
						try {
							/// if a member in childcare, season is not needed
							preSchoolClMem = getSchoolCommuneBusiness(iwc).getSchoolBusiness().getSchoolClassMemberHome().findLatestByUserAndSchCategory(user, getSchoolCommuneBusiness(iwc).getSchoolBusiness().getCategoryChildcare());

						}
						catch (Exception fex){
							log(fex);
						}
					}
				}
				
				
			}
			
			else if (preSchoolClMem == null && showChangePlacementDate){
				preSchoolClMem = schClMem;
			}
			if (!_showNoChoices) {
				if (oldSchool != null) {
					table.add(getSmallHeader(localize("school.current_shool", "Current school")), 1, row);
					/*SchoolSeason season = null;
					 if (hasMoveChoice)
					 getSchoolCommuneBusiness(iwc).getSchoolBusiness().getSchoolSeason(new Integer(getSchoolCommuneSession(iwc).getSchoolSeasonID()));
					 else
					 getSchoolCommuneBusiness(iwc).getPreviousSchoolSeason(getSchoolCommuneSession(iwc).getSchoolSeasonID());
					 
					 if (season != null) {
					 schoolClassMember = getSchoolCommuneBusiness(iwc).getSchoolBusiness().findByStudentAndSeason(user, season);
					 if (schoolClassMember != null) {
					 
					 School currentSchool = getSchoolCommuneBusiness(iwc).getSchoolBusiness().getSchool(new Integer(schoolClass.getSchoolId()));
					 
					 String schoolString = currentSchool.getName() + " - " + schoolClass.getName();
					 table.add(getSmallText(schoolString), 2, row);
					 }
					 }*/
					
					
					//SchoolClassMember schoolClassMem = getSchoolCommuneBusiness(iwc).getSchoolBusiness().findByStudentAndSchoolAndSeason(_userID, oldSchool.getID(), getSchoolCommuneBusiness(iwc).getPreviousSchoolSeason(getSchoolCommuneSession(iwc).getSchoolSeasonID()));
		
					if (preSchoolClMem != null && !isPlaced && !started) { // && schClMem != null){
				
							SchoolClass schoolClassOld = getSchoolCommuneBusiness(iwc).getSchoolBusiness().findSchoolClass(new Integer(preSchoolClMem.getSchoolClassId()));
							
							String schoolString = oldSchool.getName() + "&nbsp;" + localize("school.group", "Group") + ":&nbsp;" + schoolClassOld.getName();
							table.add(getSmallText(schoolString), 2, row);
						
					}
					else if (schClMem != null){
						SchoolClass schoolClassNew = getSchoolCommuneBusiness(iwc).getSchoolBusiness().findSchoolClass(new Integer(schClMem.getSchoolClassId()));
						School schSchool = schoolClassNew.getSchool();
						String schoolString = schSchool.getName() + "&nbsp;" + localize("school.group", "Group") + ":&nbsp;" + schoolClassNew.getName();
										
						table.add(getSmallText(schoolString), 2, row);
					}
					row++;
					
				}
			}
			
			SchoolClassMember schoolClassMember = null;
			try {
				schoolClassMember = getSchoolCommuneBusiness(iwc).getSchoolBusiness().getSchoolClassMemberHome().findLatestByUserAndSchCategoryAndSeason(user, getSchoolCommuneBusiness(iwc).getSchoolBusiness().getCategoryElementarySchool(), getSchoolCommuneBusiness(iwc).getSchoolChoiceBusiness().getSchoolSeasonHome().findByPrimaryKey(new Integer(getSchoolCommuneSession(iwc).getSchoolSeasonID())));
			}
			catch (RemoteException re) {
				log(re);
			}
			catch (FinderException fe) {
				//Nothing found so we proceed...
			}
			
			//show school year
			if (null != schoolClassMember && schoolYear == null) {
				SchoolYear scYear = getSchoolCommuneBusiness(iwc).getSchoolBusiness().getSchoolYear(schoolClassMember);
				if (null != scYear) {
					table.add(getSmallHeader(localize("school.school_choice_year", "School year")), 1, row);
					table.add(getSmallText(scYear.getName()), 2, row);
					row++;
				}
			}
			
			// show study path, if exists
			if (null != schoolClassMember) {
				final SchoolStudyPath studyPath = getSchoolCommuneBusiness(iwc).getStudyPath(schoolClassMember);
				if (null != studyPath && studyPath.isValid()) {
					table.add(getSmallHeader(localize("school.study_path", "Study Path")), 1, row);
					table.add(getSmallText(studyPath.getCode()), 2, row);
					row++;
				}
			}
			
			// show language, if exists					
			
			if (schoolClassMember != null)
				sLanguage = schoolClassMember.getLanguage();
			if (sLanguage != null && language == null){
				table.add(getSmallHeader(localize("school.school_choice_language", "Preferred language")), 1, row);
				table.add(getSmallText(localize(sLanguage, sLanguage)), 2, row++);
			}
			
			// Native language
			table.add(getSmallHeader(localize("school.native_language", "Native language")), 1, row);
			ICLanguage nativeLang = user.getNativeLanguage();
			if (nativeLang != null)
				table.add(getSmallText(nativeLang.getName()), 2, row);
			row++;

			// *** Resources START ***
			if (_schoolClassMemberID != -1) {
				try {
					SchoolClassMember scMember = getSchoolCommuneBusiness(iwc).getSchoolBusiness().getSchoolClassMemberHome().findByPrimaryKey(new Integer(_schoolClassMemberID));
					table.add(getSmallHeader(localize("school.placement", "Placement")), 1, row);
					table.add(getSmallText(getCentralPlacementBusiness(iwc).getPlacementString(scMember, user, getResourceBundle())), 2, row);
					++row;
				} catch (FinderException e) {
					e.printStackTrace();
				}
				
				
				table.add(new HiddenInput(PARAMETER_SCHOOL_CLASS_MEMBER_ID, String.valueOf(_schoolClassMemberID)), 1, row);
				Integer providerGrpID = getProviderGrpId(iwc);
				Collection rscColl = getResourceBusiness(iwc).getResourcePlacementsByMbrIdOrderByRscName(new Integer(_schoolClassMemberID));
				// Add resource label
				table.add(getSmallHeader(localize("school.resources", "Resources")), 1, row);
				// Loop resources
				int noOfShownRscs = 0;
				for (Iterator iter = rscColl.iterator(); iter.hasNext(); ) {
					ResourceClassMember mbr = (ResourceClassMember) iter.next();
					int rscId = mbr.getResourceFK();
					Resource rsc = getResourceBusiness(iwc).getResourceByPrimaryKey(new Integer(rscId));
					ResourcePermission perm = getResourceBusiness(iwc).getRscPermByRscAndGrpId((Integer) rsc.getPrimaryKey(), providerGrpID);


					boolean providerViewRights = (perm != null && perm.getPermitViewResource());
					
					// Show resource according to permissions					
					if (_viewAllResources || providerViewRights){
						Date startDate = mbr.getStartDate();
						Date endDate = mbr.getEndDate();
						// Build resource name date String
						StringBuffer buf = new StringBuffer();
						buf.append(rsc.getResourceName() + " (");
						if (startDate != null)
							buf.append(startDate.toString());
						buf.append(" - ");
						if (endDate != null)
							buf.append(endDate.toString());
						buf.append(")");
						table.add(getSmallText(buf.toString()), 2, row);
						row++;
						noOfShownRscs++;
					}
				}
				if (noOfShownRscs == 0)
					row++;
			}
			//*** Resources END ***

			table.setColumnVerticalAlignment(1, Table.VERTICAL_ALIGN_TOP);
			table.mergeCells(1, row, table.getColumns(), row);
			table.setHeight(row, Table.HUNDRED_PERCENT);
			table.setRowVerticalAlignment(row, Table.VERTICAL_ALIGN_BOTTOM);

			SubmitButton replace = (SubmitButton) getStyledInterface(new SubmitButton(localize("school.replace", "Replace"), PARAMETER_METHOD, String.valueOf(METHOD_REPLACE)));
			SubmitButton reject = (SubmitButton) getStyledInterface(new SubmitButton(localize("school.reject", "Reject"), PARAMETER_METHOD, String.valueOf(METHOD_REJECT)));
			SubmitButton moveYear = (SubmitButton) getStyledInterface(new SubmitButton(localize("school.change_year", "Change year"), PARAMETER_METHOD, String.valueOf(METHOD_MOVE_YEAR)));
			SubmitButton changeStudyPath = (SubmitButton) getStyledInterface(new SubmitButton(localize("school.change_study_path", "Change Study Path"), PARAMETER_METHOD, String.valueOf(METHOD_CHANGE_STUDY_PATH)));
			SubmitButton changePlacementDate = (SubmitButton) getStyledInterface(new SubmitButton(localize("school.change_placment_date", "Change placement date"), PARAMETER_METHOD, String.valueOf(METHOD_CHANGE_PLACEMENT_DATE)));
			PrintButton print = (PrintButton) getStyledInterface(new PrintButton(localize("school.print", "Print")));
			SubmitButton resources = (SubmitButton) getStyledInterface(new SubmitButton(localize("school.resources", "Resources"), PARAMETER_METHOD, String.valueOf(METHOD_LIST_RESOURCES)));
			SubmitButton nativeLangButton = (SubmitButton) getStyledInterface(new SubmitButton(localize("school.native_language", "Native language"), PARAMETER_METHOD, String.valueOf(METHOD_NATIVE_LANG_FORM)));
			
			
			if (_schoolID != -1 && !_showOnlyOverview) {
				table.add(replace, 1, row);
				table.add(Text.getNonBrakingSpace(), 1, row);

				if (_schoolID == pendingSchoolId) {
					table.add(reject, 1, row);
					table.add(Text.getNonBrakingSpace(), 1, row);
				}

			}

			if (_choiceID != -1 && !_showNoChoices) {
				table.add(moveYear, 1, row);
				table.add(Text.getNonBrakingSpace(), 1, row);
			}

			if (null != schoolClassMember) {
				try {
					final String placementCategory = schoolClassMember.getSchoolType().getSchoolCategory();
					final String highschoolCatgeory = getSchoolBusiness(iwc).getCategoryHighSchool().getCategory();
					if (placementCategory.equals(highschoolCatgeory)) {
						table.add(changeStudyPath, 1, row);
						table.add(Text.getNonBrakingSpace(), 1, row);
						iwc.setSessionAttribute(getClass() + PARAMETER_SCHOOL_CLASS_MEMBER_ID, schoolClassMember);
					}
				}
				catch (final NullPointerException e) {
					logWarning("Category error in school class member " + schoolClassMember.getPrimaryKey());
					log(e);
				}
			}

			if (_showOnlyOverview && _schoolClassMemberID != -1) {
				table.add(resources, 1, row);
				table.add(Text.getNonBrakingSpace(), 1, row);
			}
			
			if (_showOnlyOverview && _schoolClassMemberID != -1) {
				table.add(nativeLangButton, 1, row);
				table.add(Text.getNonBrakingSpace(), 1, row);
			}

			if (showChangePlacementDate) {
				table.add(changePlacementDate, 1, row);
				table.add(Text.getNonBrakingSpace(), 1, row);
			}
			
			if (_schoolClassMemberID > 0 && isCommuneAdmin) {
				GenericButton editPlacementButton = (GenericButton) getStyledInterface(new GenericButton(localize("central_placement_editor.button_edit_placement", "Edit placement")));
				editPlacementButton.addParameter(CentralPlacementEditLatestPlacement.PARAM_LATEST_PLACEMENT_ID, _schoolClassMemberID);
				editPlacementButton.setWindowToOpen(CentralPlacementEditLatestPlacementWindow.class);
				table.add(editPlacementButton, 1, row);
				table.add(Text.getNonBrakingSpace(), 1, row);
			}

			table.add(print, 1, row);
			table.add(Text.getNonBrakingSpace(), 1, row);
			
			// If Overview is used from central_placement_editor no close button should be visible
			if (!_fromCentralPlacementEditor)
				table.add(close, 1, row);
		}

		return table;
	}
	
	private Table getRejectForm(IWContext iwc) throws RemoteException {
		Table table = new Table();
		table.setCellpadding(5);
		table.setWidth(Table.HUNDRED_PERCENT);
		table.setHeight(Table.HUNDRED_PERCENT);
		int row = 1;

		User user = iwc.getCurrentUser();
		Email mail = getUserBusiness(iwc).getUserMail(user);

		String email = "";
		if (mail != null)
			email = mail.getEmailAddress();

		String workphone = "";
		//Collection phones = null;
		try {
			Phone phone = getUserBusiness(iwc).getUsersWorkPhone(user);
			workphone = phone.getNumber();
		}
		catch (NoPhoneFoundException npfe) {
			workphone = "";
		}

		SchoolChoice choice = null;
		try {
			choice = getSchoolCommuneBusiness(iwc).getSchoolChoiceBusiness().getSchoolChoice(_choiceID);
		}
		catch (FinderException e) {
			choice = null;
		}

		//Object[] arguments = {user.getName(), email, workphone, choice.getChild().getNameLastFirst(true), choice.getChosenSchool().getName()};
		Object[] arguments = {user.getName(), email, workphone, choice.getChild().getName(), choice.getChosenSchool().getName()};

		String message = MessageFormat.format(localize("school.reject_student_message", "We are sorry that we cannot offer {3} a place in {4} at present, if you have any questions, please contact {0} via either phone ({1}) or e-mail ({2})."), arguments);
		try {
			School school = getSchoolBusiness(iwc).getSchoolHome().findByPrimaryKey(new Integer(this._schoolID));
			if (school != null) {
				String defaultRejectionText = getSchoolBusiness(iwc).getProperty(school, SchoolBusinessBean.PROPERTY_NAME_REJECT_STUDENT_MESSAGE);
				if (defaultRejectionText != null) {
					message = MessageFormat.format(convertMessageTextFromDB(defaultRejectionText), arguments);
				}
			}
		}
		catch (FinderException e1) {
			e1.printStackTrace();
		}

		
		TextArea textArea = (TextArea) getStyledInterface(new TextArea(PARAMETER_REJECT_MESSAGE, message));
		textArea.setWidth(Table.HUNDRED_PERCENT);
		textArea.setRows(4);

		table.add(getSmallHeader(localize("school.reject_student_message_info", "The following message will be sent to the students' parents.")), 1, row++);
		table.add(textArea, 1, row++);
		//table.add(getSmallHeader(localize("school.set_as_default", "Set as default") + " "), 1, row);
		//table.add(new CheckBox(PARAMETER_SET_AS_DEFAULT), 1, row++);

		SubmitButton reject = (SubmitButton) getStyledInterface(new SubmitButton(localize("school.reject", "Reject"), PARAMETER_ACTION, String.valueOf(ACTION_REJECT)));
		reject.setSubmitConfirm(localize("school.reject_confirmation", "Are you sure you want to reject this student?  Action can not be undone."));
		table.add(reject, 1, row);
		table.add(Text.getNonBrakingSpace(), 1, row);
		table.add(close, 1, row);
		table.setHeight(row, Table.HUNDRED_PERCENT);
		table.setRowVerticalAlignment(row, Table.VERTICAL_ALIGN_BOTTOM);

		return table;
	}

	private Table getReplaceForm(IWContext iwc) throws RemoteException {
		Table table = new Table();
		table.setCellpadding(5);
		table.setWidth(Table.HUNDRED_PERCENT);
		table.setHeight(Table.HUNDRED_PERCENT);
		table.add(new HiddenInput(PARAMETER_METHOD, String.valueOf(METHOD_REPLACE)), 1, 1);
		int row = 1;

		User user = getUserBusiness(iwc).getUser(_userID);

		table.add(getSmallHeader(localize("school.replace_student_info", "You have selected to replace student: ") + user.getName() + "."), 1, row++);

		CheckBox box = new CheckBox(PARAMETER_PROTOCOL);
		box.setWidth("12");
		box.setHeight("12");
		box.keepStatusOnAction(true);
		table.add(box, 1, row);
		table.add(getSmallText(localize("school.protocol_followed", "All protocols have been followed")), 1, row++);

		IWTimestamp stamp = new IWTimestamp();
		DateInput input = (DateInput) getStyledInterface(new DateInput(PARAMETER_DATE, true));
		input.setToDisplayDayLast(true);
		input.keepStatusOnAction(true);
		input.setStyle("commune_" + STYLENAME_INTERFACE);
		input.setYearRange(stamp.getYear(), stamp.getYear() - 5);
		if (_protocol)
			table.add(getSmallHeader(localize("school.replace_date", "Replace date") + ":" + Text.NON_BREAKING_SPACE + Text.NON_BREAKING_SPACE + Text.NON_BREAKING_SPACE), 1, row);
		else
			table.add(getSmallErrorText(localize("school.replace_date", "Replace date") + ":" + Text.NON_BREAKING_SPACE + Text.NON_BREAKING_SPACE + Text.NON_BREAKING_SPACE), 1, row);
		table.add(input, 1, row++);

		table.add(getNavigationTable(iwc, localize("school.replace_to", "Replace to") + ":", false, false), 1, row++);

		//if (_protocol)
		table.add(getSmallHeader(localize("school.replace_reason", "Replace reason") + ":"), 1, row);
		//else
		//table.add(getSmallErrorText(localize("school.replace_reason","Replace reason")+":"),1,row);
		table.add(new Break(), 1, row);
		TextArea textArea = (TextArea) getStyledInterface(new TextArea(PARAMETER_REPLACE_MESSAGE));
		textArea.setWidth(Table.HUNDRED_PERCENT);
		//textArea.setAsNotEmpty(localize("school.must_provide_reason_for_replacement","You must specify a reason for replacement."));
		textArea.keepStatusOnAction(true);
		textArea.setRows(4);
		table.add(textArea, 1, row++);

		SubmitButton replace = (SubmitButton) getStyledInterface(new SubmitButton(localize("school.replace", "Replace"), PARAMETER_ACTION, String.valueOf(ACTION_REPLACE)));
		replace.setValueOnClick(PARAMETER_METHOD, "-1");
		//replace.setToEnableWhenChecked(PARAMETER_PROTOCOL);
		table.add(replace, 1, row);
		table.add(Text.getNonBrakingSpace(), 1, row);
		table.add(close, 1, row);
		table.setHeight(row, Table.HUNDRED_PERCENT);
		table.setRowVerticalAlignment(row, Table.VERTICAL_ALIGN_BOTTOM);

		return table;
	}

	private Table getChangePlacementDateForm(IWContext iwc) throws RemoteException {
		Table table = new Table();
		table.setCellpadding(5);
		table.setWidth(Table.HUNDRED_PERCENT);
		table.setHeight(Table.HUNDRED_PERCENT);
		int row = 1;

		table.add(getSmallHeader(localize("school.change_placement_date_info", "Select the placement date and click 'Change placement date'.")), 1, row++);

		table.add(getSmallHeader(localize("school.placment_date", "Placement date") + ":"), 1, row);
		table.add(Text.getNonBrakingSpace(), 1, row);

		IWTimestamp stamp = new IWTimestamp();
		SchoolChoice choice = null;
		try {
			choice = getSchoolCommuneBusiness(iwc).getSchoolChoiceBusiness().getSchoolChoice(_choiceID);
		}
		catch (FinderException e) {
			choice = null;
		}
		DateInput input = (DateInput) getStyledInterface(new DateInput(PARAMETER_DATE));
		//vänta på svar från Nacka
		SchoolSeason schSeason =  getSchoolBusiness(iwc).getSchoolSeason(new Integer (getSchoolCommuneSession(iwc).getSchoolSeasonID()));
		input.setLatestPossibleDate(schSeason.getSchoolSeasonEnd(), localize("school.dates_not_in_season", "You can not choose a date outside of the season."));
		
		if (choice != null && choice.getPlacementDate() != null)
			input.setDate(choice.getPlacementDate());
		else
			input.setDate(stamp.getDate());
		input.setEarliestPossibleDate(stamp.getDate(), localize("school.dates_back_in_time_not_allowed", "You can not choose a date back in time."));
		
		
		table.add(input, 1, row++);

		SubmitButton changePlacementDate = (SubmitButton) getStyledInterface(new SubmitButton(localize("school.change_placement_date", "Change placement date"), PARAMETER_ACTION, String.valueOf(ACTION_CHANGE_PLACEMENT_DATE)));
		table.add(changePlacementDate, 1, row);
		table.add(Text.getNonBrakingSpace(), 1, row);
		
		SubmitButton backButton = (SubmitButton) getStyledInterface(new SubmitButton(localize("school.button.back", "Back"), PARAMETER_METHOD, String.valueOf(METHOD_OVERVIEW)));
		backButton.setValueOnClick(PARAMETER_ACTION, "-1");
		table.add(backButton, 1, row);
		
		table.add(close, 1, row);
		table.setHeight(row, Table.HUNDRED_PERCENT);
		table.setRowVerticalAlignment(row, Table.VERTICAL_ALIGN_BOTTOM);

		return table;
	}

	private Table getMoveGroupForm(IWContext iwc) throws RemoteException {
		Table table = new Table();
		table.setCellpadding(5);
		table.setWidth(Table.HUNDRED_PERCENT);
		table.setHeight(Table.HUNDRED_PERCENT);
		table.add(new HiddenInput(PARAMETER_METHOD, String.valueOf(METHOD_MOVE_GROUP)), 1, 1);
		//table.add(new HiddenInput(PARAMETER_ACTION, String.valueOf(ACTION_MOVE_GROUP)));
		table.add(new HiddenInput(PARAMETER_PAGE_ID, String.valueOf(_pageID)), 1, 1);
		if (_showNoChoices)
			table.add(new HiddenInput(PARAMETER_SHOW_NO_CHOICES, "true"), 1, 1);
		int row = 1;
		
		boolean isSubGroup = false;
		SchoolClass group = getSchoolBusiness(iwc).findSchoolClass(new Integer(_schoolClassID));
		if (group != null) {
			isSubGroup = group.getIsSubGroup();
		}

		table.add(getSmallHeader(localize("school.move_group_info", "Select the new group for the student and click 'Move'.")), 1, row++);

		table.add(getNavigationTable(iwc, localize("school.move_to", "Move to") + ":", isSubGroup, true), 1, row++);

		SubmitButton move = (SubmitButton) getStyledInterface(new SubmitButton(localize("school.move", "Move"), PARAMETER_ACTION, String.valueOf(ACTION_MOVE_GROUP)));
		move.setValueOnClick(PARAMETER_METHOD, "-1");
		table.add(move, 1, row);
		table.add(Text.getNonBrakingSpace(), 1, row);
		table.add(close, 1, row);
		table.setHeight(row, Table.HUNDRED_PERCENT);
		table.setRowVerticalAlignment(row, Table.VERTICAL_ALIGN_BOTTOM);

		return table;
	}

	private Table getMoveYearForm(IWContext iwc) throws RemoteException {
		Table table = new Table();
		table.setCellpadding(5);
		table.setWidth(Table.HUNDRED_PERCENT);
		table.setHeight(Table.HUNDRED_PERCENT);
		table.add(new HiddenInput(PARAMETER_METHOD, String.valueOf(METHOD_MOVE_YEAR)), 1, 1);
		table.add(new HiddenInput(PARAMETER_ACTION, String.valueOf(ACTION_MOVE_YEAR)), 1, 1);
		int row = 1;

		table.add(getSmallHeader(localize("school.move_year_info", "Select the new year for the student and click 'Move'.")), 1, row++);

		table.add(getSmallHeader(localize("school.new_year", "New year") + ": "), 1, row);

		DropdownMenu menu = getSchoolYears(iwc);
		table.add(menu, 1, row++);

		SubmitButton move = (SubmitButton) getStyledInterface(new SubmitButton(localize("school.move", "Move")));
		move.setValueOnClick(PARAMETER_METHOD, "-1");
		table.add(move, 1, row);
		table.add(Text.getNonBrakingSpace(), 1, row);
		table.add(close, 1, row);
		table.setHeight(row, Table.HUNDRED_PERCENT);
		table.setRowVerticalAlignment(row, Table.VERTICAL_ALIGN_BOTTOM);

		return table;
	}

	private Table getFinalizeGroupForm(IWContext iwc) throws RemoteException {
		Table table = new Table();
		table.setCellpadding(5);
		table.setWidth(Table.HUNDRED_PERCENT);
		table.setHeight(Table.HUNDRED_PERCENT);
		table.add(new HiddenInput(PARAMETER_METHOD, String.valueOf(METHOD_FINALIZE_GROUP)), 1, 1);
		table.add(new HiddenInput(PARAMETER_ACTION, String.valueOf(ACTION_FINALIZE_GROUP)), 1, 1);
		table.add(new HiddenInput(PARAMETER_PAGE_ID, String.valueOf(_pageID)), 1, 1);
		int row = 1;

		String subject = null;
		String body = null;
		String message = null;

		String defBody = null;
		int schoolClassID = getSchoolCommuneSession(iwc).getSchoolClassID();
		SchoolClass schoolClass = getSchoolCommuneBusiness(iwc).getSchoolBusiness().findSchoolClass(new Integer(schoolClassID));
		if (schoolClass != null) {
			School school = getSchoolCommuneBusiness(iwc).getSchoolBusiness().getSchool(new Integer(schoolClass.getSchoolId()));
			if (schoolClass.getReady()) {
				subject = localize("school.finalize_subject", "");
				body = localize("school.finalize_body", "");
				message = localize("school.proceed_with_ready_marking", "Proceed with marking class as ready and send out messages?");
				defBody = getSchoolBusiness(iwc).getProperty(school, SchoolBusinessBean.PROPERTY_NAME_GROUP_CONFIRM_MESSAGE);
			}
			else {
				subject = localize("school.students_put_in_class_subject", "");
				body = localize("school.students_put_in_class_body", "");
				message = localize("school.proceed_with_locked_marking", "Proceed with marking class as locked and send out messages?");
				defBody = getSchoolBusiness(iwc).getProperty(school, SchoolBusinessBean.PROPERTY_NAME_GROUP_OFFER_MESSAGE);
			}

			if (defBody != null) {
				Object[] arguments = {school.getName(), schoolClass.getName(), new IWTimestamp().getLocaleDate(iwc.getCurrentLocale(), IWTimestamp.SHORT)};
				defBody = MessageFormat.format(convertMessageTextFromDB(defBody), arguments);
			}
			else if (body != null) {
				Object[] arguments = {school.getName(), schoolClass.getName(), new IWTimestamp().getLocaleDate(iwc.getCurrentLocale(), IWTimestamp.SHORT)};
				body = MessageFormat.format(body, arguments);
			}

		}

		table.add(getSmallHeader(localize("school.finalize_header", "Message headline") + ": "), 1, row);
		TextInput header = (TextInput) getStyledInterface(new TextInput(PARAMETER_FINALIZE_SUBJECT));
		header.setLength(40);
		header.setAsNotEmpty(localize("school.not_empty_finalize_subject", "Message subject can not be empty."));
		if (subject != null)
			header.setContent(subject);
		table.add(header, 1, row++);

		table.add(getSmallHeader(localize("school.finalize_text", "Message body") + ":"), 1, row);
		table.add(new Break(), 1, row);
		TextArea text = (TextArea) getStyledInterface(new TextArea(PARAMETER_FINALIZE_BODY));
		text.setWidth(Table.HUNDRED_PERCENT);
		text.setRows(10);
		text.setAsNotEmpty(localize("school.not_empty_finalize_body", "Message body can not be empty."));
		table.add(text, 1, row++);

		if (defBody != null) {
			text.setContent(defBody);
		}
		else if (body != null) {
			text.setContent(body);
		}




		SubmitButton send = (SubmitButton) getStyledInterface(new SubmitButton(localize("school.send", "Send")));
		send.setValueOnClick(PARAMETER_METHOD, "-1");
		send.setSubmitConfirm(message);
		form.setToDisableOnSubmit(send, true);

		table.add(send, 1, row);
		table.add(Text.getNonBrakingSpace(), 1, row);
		table.add(close, 1, row);
		table.setHeight(row, Table.HUNDRED_PERCENT);
		table.setRowVerticalAlignment(row, Table.VERTICAL_ALIGN_BOTTOM);

		return table;
	}

	private Table getEditStudentForm(IWContext iwc) throws RemoteException {
		Table table = new Table();
		table.setCellpadding(5);
		table.setWidth(Table.HUNDRED_PERCENT);
		table.setHeight(Table.HUNDRED_PERCENT);
		table.add(new HiddenInput(PARAMETER_METHOD, String.valueOf(METHOD_EDIT_STUDENT)), 1, 1);
		table.add(new HiddenInput(PARAMETER_ACTION, String.valueOf(ACTION_EDIT_STUDENT)), 1, 1);
		table.add(new HiddenInput(PARAMETER_USER_ID, String.valueOf(_userID)), 1, 1);
		table.add(new HiddenInput(PARAMETER_PAGE_ID, String.valueOf(_pageID)), 1, 1);
		int row = 1;

		User user = getUserBusiness(iwc).getUser(_userID);
		Address address = getUserBusiness(iwc).getUsersMainAddress(user);
		PostalCode code = null;
		if (address != null) {
			try {
				code = address.getPostalCode();
			}
			catch (Exception e) {
				code = null;
			}
		}

		table.add(getSmallHeader(localize("school.first_name", "First name") + ":"), 1, row);
		TextInput firstName = (TextInput) getStyledInterface(new TextInput("first_name"));
		if (user.getFirstName() != null && !user.getFirstName().equalsIgnoreCase(user.getPersonalID()))
			firstName.setContent(user.getFirstName());
		table.add(firstName, 2, row++);

		table.add(getSmallHeader(localize("school.middle_name", "Middle name") + ":"), 1, row);
		TextInput middleName = (TextInput) getStyledInterface(new TextInput("middle_name"));
		if (user.getMiddleName() != null)
			middleName.setContent(user.getMiddleName());
		table.add(middleName, 2, row++);

		table.add(getSmallHeader(localize("school.last_name", "Last name") + ":"), 1, row);
		TextInput lastName = (TextInput) getStyledInterface(new TextInput("last_name"));
		if (user.getLastName() != null)
			lastName.setContent(user.getLastName());
		table.add(lastName, 2, row++);

		table.add(getSmallHeader(localize("school.address", "Address") + ":"), 1, row);
		TextInput streetAddress = (TextInput) getStyledInterface(new TextInput("address"));
		if (address != null && address.getStreetAddress() != null)
			streetAddress.setContent(address.getStreetAddress());
		table.add(streetAddress, 2, row++);

		table.add(getSmallHeader(localize("school.postal_code", "Postal code") + ":"), 1, row);
		TextInput postalCode = (TextInput) getStyledInterface(new TextInput("postal_code"));
		if (code != null && code.getPostalCode() != null)
			postalCode.setContent(code.getPostalCode());
		table.add(postalCode, 2, row++);

		table.add(getSmallHeader(localize("school.city", "City") + ":"), 1, row);
		TextInput postalName = (TextInput) getStyledInterface(new TextInput("postal_name"));
		if (address != null && address.getCity() != null)
			postalName.setContent(address.getCity());
		table.add(postalName, 2, row++);

		SubmitButton update = (SubmitButton) getStyledInterface(new SubmitButton(localize("school.update", "Update")));
		update.setValueOnClick(PARAMETER_METHOD, "-1");
		table.mergeCells(1, row, 2, row);
		table.add(update, 1, row);
		table.add(Text.getNonBrakingSpace(), 1, row);
		table.add(close, 1, row);
		table.setHeight(row, Table.HUNDRED_PERCENT);
		table.setRowVerticalAlignment(row, Table.VERTICAL_ALIGN_BOTTOM);
		table.setWidth(1, "100");
		table.setWidth(1, row, Table.HUNDRED_PERCENT);
		
		return table;
	}

	private Table getAddStudentForm(IWContext iwc) {
		Table table = new Table();
		table.setCellpadding(5);
		table.setWidth(Table.HUNDRED_PERCENT);
		table.setHeight(Table.HUNDRED_PERCENT);
		table.add(new HiddenInput(PARAMETER_METHOD, String.valueOf(METHOD_ADD_STUDENT)), 1, 1);
		table.add(new HiddenInput(PARAMETER_PAGE_ID, String.valueOf(_pageID)), 1, 1);
		int row = 1;

		table.add(getSmallHeader(localize("commune.enter_search_string", "Enter search string") + ":"), 1, row);

		TextInput searchInput = (TextInput) getStyledInterface(new TextInput(PARAMETER_SEARCH));
		searchInput.setWidth("200");
		searchInput.setMininumLength(6, localize("commune.search_string_too_short", "Search string must be at least six characters."));
		searchInput.keepStatusOnAction(true);
		table.add(searchInput, 2, row++);

		SubmitButton searchButton = (SubmitButton) this.getButton(new SubmitButton(localize("search", "Search")));
		table.add(searchButton, 1, row++);

		if (searchString != null) {
			try {
				Collection users = getUserBusiness(iwc).getUserHome().findUsersBySearchCondition(searchString, true);
				if (!users.isEmpty()) {
					Table userTable = new Table();
					userTable.setCellpadding(0);
					userTable.setCellspacing(0);
					userTable.setWidth(Table.HUNDRED_PERCENT);
					table.add(userTable, 1, row);
					table.mergeCells(1, row, 2, row);

					User user;
					Age age;
					Date d;
					RadioButton radio;
					int userRow = 1;
					boolean addSubmit = false;

					userTable.add(getSmallHeader(localize("commune.found_users", "Found users") + ":"), 1, userRow++);
					userTable.setHeight(userRow++, 6);

					Iterator iter = users.iterator();
					while (iter.hasNext()) {
						user = (User) iter.next();
						d = user.getDateOfBirth();
						if (d == null) {
							if (user.getPersonalID() != null) {
								d = PIDChecker.getInstance().getDateFromPersonalID(user.getPersonalID());
							}
							if (d == null)
								d = new Date();
						}
						age = new Age(d);

						if (age.getYears() <= 18) {
							addSubmit = true;
							radio = getRadioButton(PARAMETER_USER_ID, user.getPrimaryKey().toString());

							userTable.add(radio, 1, userRow);
							userTable.add(Text.getNonBrakingSpace(), 1, userRow);
							Name name = new Name(user.getFirstName(), user.getMiddleName(), user.getLastName());
							userTable.add(getSmallText(name.getName(iwc.getApplicationSettings().getDefaultLocale(), true)), 1, userRow);
							userTable.add(getSmallText(" ("), 1, userRow);
							userTable.add(getSmallText(PersonalIDFormatter.format(user.getPersonalID(), iwc.getCurrentLocale()) + ")"), 1, userRow++);
						}
					}

					userTable.setHeight(userRow++, 12);

					userTable.add(getSmallHeader(localize("commune.enter_comment", "Enter comment") + ":"), 1, userRow++);

					userTable.setHeight(userRow++, 6);
					TextArea freeText = (TextArea) getStyledInterface(new TextArea(PARAMETER_COMMENT));
					freeText.setWidth(Table.HUNDRED_PERCENT);
					freeText.setHeight("60");
					freeText.setWrap(true);
					userTable.add(freeText, 1, userRow++);

					userTable.setHeight(userRow++, 6);

					if (addSubmit) {
						SubmitButton addButton = (SubmitButton) getButton(new SubmitButton(localize("school.add_student", "Add student"), PARAMETER_ACTION, String.valueOf(ACTION_ADD_STUDENT)));
						addButton.setValueOnClick(PARAMETER_METHOD, "-1");
						addButton.setToEnableWhenSelected(PARAMETER_USER_ID);
						userTable.add(addButton, 1, userRow);
					}
					else {
						userTable.add(getSmallHeader(localize("school.no_student_found", "No student found")), 1, userRow++);
					}
				}
				else {
					table.add(getSmallHeader(localize("school.no_student_found", "No student found")), 1, row++);
					if (PIDChecker.getInstance().isValid(searchString)) {
						SubmitButton create = (SubmitButton) getButton(new SubmitButton(localize("school.create_student", "Create student"), PARAMETER_ACTION, String.valueOf(ACTION_CREATE_STUDENT)));
						table.add(create, 1, row);
					}
				}
			}
			catch (Exception e) {
			}
		}
		table.setHeight(++row, Table.HUNDRED_PERCENT);

		return table;
	}

	private Table getResourceList(IWContext iwc) throws RemoteException {
		Table table = new Table();
		table.setBorder(0);
		table.setCellpadding(1);
		table.setCellspacing(2);
		table.setWidth(Table.HUNDRED_PERCENT);
		table.setHeight(Table.HUNDRED_PERCENT);

		// Create Header row
		table.setWidth(1, "150");
		table.setWidth(2, "90");
		table.setWidth(3, "90");
		table.setWidth(4, "120");
		table.setWidth(5, "50");
		table.add(getSmallHeader(localize("school.resource", "Resource")), 1, 1);
		table.add(getSmallHeader(localize("school.startdate", "Startdate")), 2, 1);
		table.add(getSmallHeader(localize("school.enddate", "Enddate")), 3, 1);
		table.add(getSmallHeader(localize("school.finish", "Finish")), 4, 1);
		table.add(getSmallHeader(localize("school.delete", "Delete")), 5, 1);
		table.addText(Text.NON_BREAKING_SPACE, 6, 1);
		table.setRowColor(1, getHeaderColor());
		table.setColor(6, 1, "#FFFFFF");
		table.add(new HiddenInput(PARAMETER_ACTION, "-1"), 6, 1);
		table.add(new HiddenInput(PARAMETER_SHOW_ONLY_OVERVIEW, "false"), 6, 1);
		table.add(new HiddenInput(PARAMETER_DELETE_RESOURCE_PLACEMENT, "-1"), 6, 1);
		table.add(new HiddenInput(PARAMETER_RESOURCE_CLASS_MEMBER, "-1"), 6, 1);
		table.add(new HiddenInput(PARAMETER_RESOURCE_NAME, ""), 6, 1);
		table.add(new HiddenInput(PARAMETER_RESOURCE_STARTDATE, ""), 6, 1);
		table.add(new HiddenInput(PARAMETER_RESOURCE_ENDDATE, ""), 6, 1);
		table.setRowHeight(1, "7");

		// list resources
		int row = 2;
		Integer providerGrpID = getProviderGrpId(iwc);
		Collection rscColl = getResourceBusiness(iwc).getResourcePlacementsByMbrIdOrderByRscName(new Integer(_schoolClassMemberID));
		SubmitButton finish;
		SubmitButton delete;

		for (Iterator iter = rscColl.iterator(); iter.hasNext(); ) {
			ResourceClassMember mbr = (ResourceClassMember) iter.next();
			int rscId = mbr.getResourceFK();
			Resource rsc = getResourceBusiness(iwc).getResourceByPrimaryKey(new Integer(rscId));
			ResourcePermission perm = getResourceBusiness(iwc).getRscPermByRscAndGrpId((Integer) rsc.getPrimaryKey(), providerGrpID);

			// Show resource row if a provider permission with view rights exists
			boolean hasProviderRights = (perm != null && perm.getPermitViewResource());
			
			// Show all resources according to permissions
			if (_viewAllResources || hasProviderRights) {				
				// Row buttons
				delete = new SubmitButton(getDeleteIcon(localize("school.delete_resource_placement", "Click to remove resource placement from student")), PARAMETER_METHOD, String.valueOf(METHOD_LIST_RESOURCES));
				delete.setDescription(localize("school.delete_resource_placement", "Click to remove resource placement from student"));
				delete.setValueOnClick(PARAMETER_ACTION, String.valueOf(ACTION_DELETE_RESOURCE));
				delete.setValueOnClick(PARAMETER_DELETE_RESOURCE_PLACEMENT, mbr.getPrimaryKey().toString());
				delete.setSubmitConfirm(localize("school.confirm_resource_placement_delete", "Are you sure you want to delete this resource from the student?"));

				finish = new SubmitButton(getEditIcon(localize("school.finish_resource_placement", "Click to finish resource placement setting the end date")), PARAMETER_METHOD, String.valueOf(METHOD_FINISH_RESOURCE));
				finish.setDescription(localize("school.finish_resource_placement", "Click to finish resource placement setting the end date"));
				finish.setValueOnClick(PARAMETER_RESOURCE_CLASS_MEMBER, mbr.getPrimaryKey().toString());
				finish.setValueOnClick(PARAMETER_RESOURCE_NAME, rsc.getResourceName());
				finish.setValueOnClick(PARAMETER_RESOURCE_STARTDATE, mbr.getStartDate().toString());
				if (mbr.getEndDate() != null)
					finish.setValueOnClick(PARAMETER_RESOURCE_ENDDATE, mbr.getEndDate().toString());
				// Build table row			
				table.add(getSmallText(rsc.getResourceName()), 1, row);
				Date startDate = mbr.getStartDate();
				if (startDate != null)
					table.add(getSmallText(startDate.toString()), 2, row);
				Date endDate = mbr.getEndDate();
				if (endDate != null)
					table.add(getSmallText(endDate.toString()), 3, row);
				table.add(finish, 4, row);
				table.add(delete, 5, row);
				if (row % 2 == 1)
					table.setRowColor(row, getZebraColor1());
				else
					table.setRowColor(row, getZebraColor2());
				table.setColor(6, row, "#FFFFFF");
				row++;
			}
		}
		// Add space row
		table.add(Text.NON_BREAKING_SPACE, 1, row);
		row++;	
		
		SubmitButton newButton = (SubmitButton) getStyledInterface(new SubmitButton(localize("sch.button.new", "New"), PARAMETER_METHOD, String.valueOf(METHOD_NEW_RESOURCE)));
		SubmitButton backButton = (SubmitButton) getStyledInterface(new SubmitButton(localize("school.button.back", "Back"), PARAMETER_METHOD, String.valueOf(METHOD_OVERVIEW)));
		//backButton.setValueOnClick(PARAMETER_SHOW_ONLY_OVERVIEW, "true");

		table.add(newButton, 1, row);
		table.add(Text.getNonBrakingSpace(), 1, row);
		table.add(backButton, 1, row);
		table.add(Text.getNonBrakingSpace(), 1, row);
		
		if (!_fromCentralPlacementEditor) {
			table.add(close, 1, row);
		}
		
		table.mergeCells(1, row, table.getColumns(), row);
		row++;
		// Last transparent row to fill up the page
		table.addText(Text.NON_BREAKING_SPACE, 1, row);
		table.mergeCells(1, row, table.getColumns(), row);
		table.setRowHeight(row, "100%");

		return table;
	}

	private Table getResourceForm(IWContext iwc) throws RemoteException {
		Table table = new Table();
		table.setBorder(0);
		table.setCellpadding(1);
		table.setCellspacing(2);

		// *** Input labels ***
		int row = 1;
		if (resourceErrorMsg != null) {
			table.mergeCells(1, 1, 2, 1);
			table.add(getSmallErrorText(resourceErrorMsg), 1, row++);
		}
		table.add(getSmallHeader(localize("school.resource", "Resource")), 1, row++);
		table.add(getSmallHeader(localize("school.startdate", "Startdate")), 1, row++);
		table.add(getSmallHeader(localize("school.enddate", "Enddate")), 1, row);

		// *** Input fields ***
		if (resourceErrorMsg != null) {
			row = 2;
		}
		else {
			row = 1;
		}
		DropdownMenu rscDD = getAssignableResources(iwc);
		if (iwc.isParameterSet(PARAMETER_RESOURCE_ID))
			rscDD.setSelectedElement(iwc.getParameter(PARAMETER_RESOURCE_ID));
		table.add(rscDD, 2, row++);
		long currentTime = new Date().getTime();

		DateInput startDate = new DateInput(PARAMETER_RESOURCE_STARTDATE);
		startDate.setToDisplayDayLast(true);
		if (iwc.isParameterSet(PARAMETER_RESOURCE_STARTDATE)) {
			startDate.setContent(iwc.getParameter(PARAMETER_RESOURCE_STARTDATE));
		}
		else {
			startDate.setDate(new java.sql.Date(currentTime));
		}
		table.add(startDate, 2, row++);

		DateInput endDate = new DateInput(PARAMETER_RESOURCE_ENDDATE);
		endDate.setToDisplayDayLast(true);
		if (iwc.isParameterSet(PARAMETER_RESOURCE_ENDDATE)) {
			endDate.setContent(iwc.getParameter(PARAMETER_RESOURCE_ENDDATE));
		}
		table.add(endDate, 2, row++);

		// *** Button row ***
		SubmitButton addButton = (SubmitButton) getStyledInterface(new SubmitButton(localize("school.button.add", "Add"), PARAMETER_METHOD, String.valueOf(METHOD_OVERVIEW)));
		addButton.setValueOnClick(PARAMETER_ACTION, String.valueOf(ACTION_SAVE_RESOURCE));
		//addButton.setValueOnClick(PARAMETER_SHOW_ONLY_OVERVIEW, "true");
		table.add(addButton, 1, row);
		table.add(Text.getNonBrakingSpace(), 1, row);

		SubmitButton backButton = (SubmitButton) getStyledInterface(new SubmitButton(localize("school.button.back", "Back"), PARAMETER_METHOD, String.valueOf(METHOD_LIST_RESOURCES)));
		table.add(backButton, 1, row);
		table.add(Text.getNonBrakingSpace(), 1, row);
		
		if (!_fromCentralPlacementEditor) {
			table.add(close, 1, row);
		}
		table.mergeCells(1, row, 3, row++);

		//table.add(new HiddenInput(PARAMETER_SHOW_ONLY_OVERVIEW, ""), 1, row);
		table.add(new HiddenInput(PARAMETER_ACTION, "-1"), 1, row);

		// *** Bottom&Right table space ***
		table.setWidth(Table.HUNDRED_PERCENT);
		table.setHeight(Table.HUNDRED_PERCENT);
		// Last transparent column to fill up the page
		table.addText(Text.NON_BREAKING_SPACE, 3, 1);
		// Last transparent row to fill up the page
		table.addText(Text.NON_BREAKING_SPACE, 1, row);
		table.mergeCells(1, row, 3, row);
		table.setRowHeight(row, "100%");

		return table;
	}

	private Table getFinishResourceForm(IWContext iwc) {
		Table table = new Table();
		table.setBorder(0);
		table.setCellpadding(1);
		table.setCellspacing(2);

		// *** Input labels ***
		int row = 1;
		if (resourceErrorMsg != null) {
			table.mergeCells(1, 1, 2, 1);
			table.add(getSmallErrorText(resourceErrorMsg), 1, row++);
		}
		table.add(getSmallHeader(localize("school.resource", "Resource")), 1, row++);
		if (iwc.isParameterSet(PARAMETER_RESOURCE_STARTDATE))
			table.add(getSmallHeader(localize("school.startdate", "Startdate")), 1, row++);
		table.add(getSmallHeader(localize("school.enddate", "Enddate")), 1, row);

		// *** Error message ***
		if (resourceErrorMsg != null) {
			row = 2;
		}
		else {
			row = 1;
		}
		table.add(getSmallText(iwc.getParameter(PARAMETER_RESOURCE_NAME)), 2, row++);
		DateInput startDate = (DateInput) getStyledInterface(new DateInput(PARAMETER_RESOURCE_STARTDATE));
		startDate.setToDisplayDayLast(true);
		if (iwc.isParameterSet(PARAMETER_RESOURCE_STARTDATE)) {
			startDate.setContent(iwc.getParameter(PARAMETER_RESOURCE_STARTDATE));
			startDate.setDisabled(true);
			table.add(startDate, 2, row++);
		}

		// *** Enddate ***
		DateInput endDate = (DateInput) getStyledInterface(new DateInput(PARAMETER_RESOURCE_ENDDATE));
		endDate.setToDisplayDayLast(true);
		if (iwc.isParameterSet(PARAMETER_RESOURCE_ENDDATE)) {
			endDate.setContent(iwc.getParameter(PARAMETER_RESOURCE_ENDDATE));
		}
		else {
			endDate.setContent(iwc.getParameter(PARAMETER_RESOURCE_STARTDATE));
		}
		table.add(endDate, 2, row++);

		// *** Button row ***
		SubmitButton finishButton = (SubmitButton) getStyledInterface(new SubmitButton(localize("school.finish", "Finish"), PARAMETER_METHOD, String.valueOf(METHOD_OVERVIEW)));
		table.add(new HiddenInput(PARAMETER_ACTION, String.valueOf(ACTION_FINISH_RESOURCE)), 1, row);
		table.add(new HiddenInput(PARAMETER_RESOURCE_CLASS_MEMBER, iwc.getParameter(PARAMETER_RESOURCE_CLASS_MEMBER)), 1, row);
		table.add(new HiddenInput(PARAMETER_RESOURCE_NAME, iwc.getParameter(PARAMETER_RESOURCE_NAME)), 1, row);
		table.add(new HiddenInput(PARAMETER_RESOURCE_STARTDATE, iwc.getParameter(PARAMETER_RESOURCE_STARTDATE)), 1, row);
		table.add(new HiddenInput(PARAMETER_SHOW_ONLY_OVERVIEW, "true"), 1, row);
		table.add(finishButton, 1, row);
		table.add(Text.getNonBrakingSpace(), 1, row);

		SubmitButton backButton = (SubmitButton) getStyledInterface(new SubmitButton(localize("school.button.back", "Back"), PARAMETER_METHOD, String.valueOf(METHOD_LIST_RESOURCES)));
		backButton.setValueOnClick(PARAMETER_ACTION, "-1");
		table.add(backButton, 1, row);
		table.add(Text.getNonBrakingSpace(), 1, row);

		table.add(close, 1, row);
		table.mergeCells(1, row, 3, row++);

		// *** Bottom&Right table space ***
		table.setWidth(Table.HUNDRED_PERCENT);
		table.setHeight(Table.HUNDRED_PERCENT);
		// Last transparent column to fill up the page
		table.addText(Text.NON_BREAKING_SPACE, 3, 1);
		// Last transparent row to fill up the page
		table.addText(Text.NON_BREAKING_SPACE, 1, row);
		table.mergeCells(1, row, 3, row);
		table.setRowHeight(row, "100%");

		return table;
	}

	private void saveMessageText(IWContext iwc) throws RemoteException {
		String rejectStudent = iwc.getParameter(PARAMETER_REJECT_STUDENT);
		String placementOffer = iwc.getParameter(PARAMETER_PLACEMENT_OFFER);
		String placementConfirmation = iwc.getParameter(PARAMETER_PLACEMENT_CONFIRMATION);
		
		//add("<br>placementOffer = "+placementOffer);
		//add("<br>placementConfirmation = "+placementConfirmation);
		
		try {
			School school = getSchoolBusiness(iwc).getSchoolHome().findByPrimaryKey(new Integer(_schoolID));
			if (rejectStudent != null && !"".equals(rejectStudent)) {
				getSchoolBusiness(iwc).setProperty(school, SchoolBusinessBean.PROPERTY_NAME_REJECT_STUDENT_MESSAGE, convertMessageTextForDB(rejectStudent));
			}
			if (placementOffer != null && !"".equals(placementOffer)) {
				getSchoolBusiness(iwc).setProperty(school, SchoolBusinessBean.PROPERTY_NAME_GROUP_OFFER_MESSAGE, convertMessageTextForDB(placementOffer));
			}
			if (placementConfirmation != null && !"".equals(placementConfirmation)) {
				getSchoolBusiness(iwc).setProperty(school, SchoolBusinessBean.PROPERTY_NAME_GROUP_CONFIRM_MESSAGE, convertMessageTextForDB(placementConfirmation));
			}
			
//			add("<br>rejectStudent = "+mReject);
//			add("<br>placementOffer = "+mPlaceOff);
//			add("<br>placementConfirmation = "+mPlaceCon);
		} catch (FinderException e) {
			e.printStackTrace();
		}
	}
	
	private String convertMessageTextForDB(String string) {
		for (int i = 0; i < 5; i++) {
			string = TextSoap.findAndReplace(string, "{"+i+"}", "["+i+"]");
		}
		return string;
	}
	
	private String convertMessageTextFromDB(String string) {
		for (int i = 0; i < 5; i++) {
			string = TextSoap.findAndReplace(string, "["+i+"]", "{"+i+"}");
		}
		return string;
	}

	
	private Table getMessageTextForm(IWContext iwc) throws RemoteException {
		Table table = new Table(3, 9);
		table.setBorder(0);
		table.setCellpadding(1);
		table.setCellspacing(2);
		table.setWidth("100%");
		table.setWidth(3, 100);
		table.mergeCells(1, 1, 3, 1);
		int row = 1;
		
		TextArea rejectStudent = (TextArea) getStyledInterface(new TextArea(PARAMETER_REJECT_STUDENT));
		rejectStudent.setWidth(Table.HUNDRED_PERCENT);
		rejectStudent.setRows(4);
		
		TextArea placementOffer = (TextArea) getStyledInterface(new TextArea(PARAMETER_PLACEMENT_OFFER));
		placementOffer.setWidth(Table.HUNDRED_PERCENT);
		placementOffer.setRows(4);
		
		TextArea placementConfirmation = (TextArea) getStyledInterface(new TextArea(PARAMETER_PLACEMENT_CONFIRMATION));
		placementConfirmation.setWidth(Table.HUNDRED_PERCENT);
		placementConfirmation.setRows(4);
		
		try {
			School school = getSchoolBusiness(iwc).getSchoolHome().findByPrimaryKey(new Integer(_schoolID));
			String mReject = getSchoolBusiness(iwc).getProperty(school, SchoolBusinessBean.PROPERTY_NAME_REJECT_STUDENT_MESSAGE);
			String mPlaceOff = getSchoolBusiness(iwc).getProperty(school, SchoolBusinessBean.PROPERTY_NAME_GROUP_OFFER_MESSAGE);
			String mPlaceCon = getSchoolBusiness(iwc).getProperty(school, SchoolBusinessBean.PROPERTY_NAME_GROUP_CONFIRM_MESSAGE);
			if (mReject != null) {
				rejectStudent.setContent(convertMessageTextFromDB(mReject));
			} 
			else {
				String message = localize("school.reject_student_message", "We are sorry that we cannot offer {3} a place in {4} at present, if you have any questions, please contact {0} via either phone ({1}) or e-mail ({2}).");
				rejectStudent.setContent(message);
			}
			if (mPlaceOff != null) {
				placementOffer.setContent(convertMessageTextFromDB(mPlaceOff));
			}
			else {
				String body = localize("school.students_put_in_class_body", "");
				placementOffer.setContent(body);
			}
			if (mPlaceCon != null) {
				placementConfirmation.setContent(convertMessageTextFromDB(mPlaceCon));
			}
			else {
				String body = localize("school.finalize_body", "");
				placementConfirmation.setContent(body);
			}
		} catch (FinderException e) {
			e.printStackTrace();
		}
		
		table.add(getSmallText(localize("school.message_info", "Here you can change your messages that are sent to the parents")), row, 1);
		table.add(new Break(),row, 1);
		table.add(getSmallErrorText(localize("school.message_warning", "The info in the text which is displayed with {1} etc should not be removed.")), row, 1);
		row++;
		table.add(getSmallHeader(localize("school.reject_student", "Reject student")), 1, row++);
		table.setVerticalAlignment(1, row, Table.VERTICAL_ALIGN_TOP);
		table.add(rejectStudent, 1, row++);
		table.add(getSmallHeader(localize("school.placement_offer", "Placement offer")), 1, row++);
		table.setVerticalAlignment(1, row, Table.VERTICAL_ALIGN_TOP);
		table.add(placementOffer, 1, row++);
		table.add(getSmallHeader(localize("school.placement_confirmation", "Placement confirmation")), 1, row++);
		table.setVerticalAlignment(1, row, Table.VERTICAL_ALIGN_TOP);
		table.add(placementConfirmation, 1, row);

		row = 3;
		table.setVerticalAlignment(3, row, Table.VERTICAL_ALIGN_TOP);
		table.add(getSmallText("{0} "+localize("school.current_user_name", "User name")), 3, row);
		table.add(Text.BREAK, 3, row);
		table.add(getSmallText("{1} "+localize("school.email", "Email")), 3, row);
		table.add(Text.BREAK, 3, row);
		table.add(getSmallText("{2} "+localize("school.workphone", "Work Phone")), 3, row);
		table.add(Text.BREAK, 3, row);
		table.add(getSmallText("{3} "+localize("school.child_name", "Child name")), 3, row);
		table.add(Text.BREAK, 3, row);
		table.add(getSmallText("{4} "+localize("school.school_name", "School name")), 3, row);
		table.add(Text.BREAK, 3, row++);
		
		//Object[] rejectArgs = {user.getName(), email, workphone, choice.getChild().getNameLastFirst(true), choice.getChosenSchool().getName()};
		//Object[] placeArgs = {school.getName(), schoolClass.getName(), new IWTimestamp().getLocaleDate(iwc.getCurrentLocale(), IWTimestamp.SHORT)};
		row++;
		table.setVerticalAlignment(3, row, Table.VERTICAL_ALIGN_TOP);
		table.add(getSmallText("{0} "+localize("school.school_name", "School name")), 3, row);
		table.add(Text.BREAK, 3, row);
		table.add(getSmallText("{1} "+localize("school.school_class", "School class")), 3, row);
		table.add(Text.BREAK, 3, row);
		table.add(getSmallText("{2} "+localize("school.date", "Date")), 3, row);
		table.add(Text.BREAK, 3, row++);
		
		row++;
		table.setVerticalAlignment(3, row, Table.VERTICAL_ALIGN_TOP);
		table.add(getSmallText("{0} "+localize("school.school_name", "School name")), 3, row);
		table.add(Text.BREAK, 3, row);
		table.add(getSmallText("{1} "+localize("school.school_class", "School class")), 3, row);
		table.add(Text.BREAK, 3, row);
		table.add(getSmallText("{2} "+localize("school.date", "Date")), 3, row);
		table.add(Text.BREAK, 3, row++);
		
		row++;

		SubmitButton setButton = (SubmitButton) getStyledInterface(new SubmitButton(localize("school.button.set", "Set"), PARAMETER_ACTION, String.valueOf(ACTION_SAVE_MESSAGE_TEXT)));
		table.add(new HiddenInput(PARAMETER_METHOD, String.valueOf(METHOD_MESSAGE_TEXT)), 1, row);
		table.add(setButton, 1, row);

		table.add(Text.NON_BREAKING_SPACE, 1, row);
		
		CloseButton closeButton = (CloseButton) getStyledInterface(new CloseButton(localize("school.button.close", "Close")));
		table.add(closeButton, 1, row);

		return table;
	}

	private Table getNativeLanguageForm(IWContext iwc) {
		Table table = new Table(2, 8);
		table.setBorder(0);
		table.setCellpadding(1);
		table.setCellspacing(2);

		int row = 1;
		
		// *** Native language ***
		table.add(getSmallHeader(localize("school.native_language", "Native language")), 1, row);
		table.add(Text.getNonBrakingSpace(6), 1, row);
		table.add(getNativeLanguagesDropdown(iwc), 1, row);
		row++;
		row++;
		
		// *** Button row ***
		SubmitButton setButton = (SubmitButton) getStyledInterface(new SubmitButton(localize("school.button.set", "Set"), PARAMETER_METHOD, String.valueOf(METHOD_OVERVIEW)));
		table.add(new HiddenInput(PARAMETER_ACTION, String.valueOf(ACTION_SAVE_NATIVE_LANGUAGE)), 1, row);
		table.add(setButton, 1, row);
		
		table.add(Text.getNonBrakingSpace(), 1, row);

		SubmitButton backButton = (SubmitButton) getStyledInterface(new SubmitButton(localize("school.button.back", "Back"), PARAMETER_METHOD, String.valueOf(METHOD_OVERVIEW)));
		backButton.setValueOnClick(PARAMETER_ACTION, "-1");
		table.add(backButton, 1, row);
		
		table.add(Text.getNonBrakingSpace(), 1, row);
		
		if (!_fromCentralPlacementEditor) {
			table.add(close, 1, row);
		}
		
		row++;
		// *** Bottom&Right table space ***
		table.setWidth(Table.HUNDRED_PERCENT);
		table.setHeight(Table.HUNDRED_PERCENT);
		// Last transparent column to fill up the page
		table.addText(Text.NON_BREAKING_SPACE, 2, 1);
		// Last transparent row to fill up the page
		table.addText(Text.NON_BREAKING_SPACE, 1, row);
		table.mergeCells(1, row, 2, row);
		table.setRowHeight(row, "100%");

		return table;
	}
	
	private DropdownMenu getNativeLanguagesDropdown(IWContext iwc) {
		DropdownMenu drop = (DropdownMenu) getStyledInterface(new DropdownMenu(PARAMETER_NATIVE_LANG));
		drop.addMenuElement("-1", localize("school.drp_chose_native_lang", "- Chose languge -"));
		try {
			ICLanguageHome lHome = (ICLanguageHome) IDOLookup.getHome(ICLanguage.class);
			Collection langs = lHome.findAll();
			if (langs != null) {
				for (Iterator iter = langs.iterator(); iter.hasNext();) {
					ICLanguage aLang = (ICLanguage) iter.next();
					int langPK = ((Integer) aLang.getPrimaryKey()).intValue();
					drop.addMenuElement(langPK, aLang.getName());					
				}
			}
			
			// Set selected lang if pupil has chosen one
			User pupil = getUserBusiness(iwc).getUser(_userID);
			ICLanguage lang = pupil.getNativeLanguage();
			if (lang != null) {
				Integer langPK = (Integer) lang.getPrimaryKey();
				int langID = langPK.intValue();
				drop.setSelectedElement(langID);
			}
		} catch (RemoteException re) {
			re.printStackTrace();
		} catch (FinderException fe) {
			
		}	
		return drop;		
	}
	
	
	protected Table getNavigationTable(IWContext iwc, String heading, boolean showSubGroups, boolean setDefaultValues) throws RemoteException {
		Table table = new Table(4, 1);
		table.setCellpadding(0);
		table.setCellspacing(0);
		table.setWidth(2, "8");
		//table.setWidth(5, "8");

		table.add(getSmallHeader(heading), 1, 1);
		table.add(getSmallHeader(localize("school.year_class", "Year/Class") + ":" + Text.NON_BREAKING_SPACE), 3, 1);
		table.add(getDropdown(iwc, showSubGroups, setDefaultValues), 4, 1);
		/*table.add(getSchoolYears(iwc), 4, 1);
		 table.add(getSmallHeader(localize("school.class", "Class") + ":" + Text.NON_BREAKING_SPACE), 6, 1);
		 table.add(getSchoolClasses(iwc, setToSubmit), 7, 1);*/

		return table;
	}

	private SelectDropdownDouble getDropdown(IWContext iwc, boolean showSubGroups, boolean setDefaultValues) throws RemoteException {
		SchoolClassDropdownDouble dropdown = (SchoolClassDropdownDouble) getStyledInterface(new SchoolClassDropdownDouble(getSchoolCommuneSession(iwc).getParameterSchoolYearID(), getSchoolCommuneSession(iwc).getParameterSchoolClassID()));
		if (setDefaultValues) {
			dropdown.setSelectedValues(String.valueOf(getSchoolCommuneSession(iwc).getSchoolYearID()), String.valueOf(getSchoolCommuneSession(iwc).getSchoolClassID()));
		}
		else {
			dropdown.addEmptyElement(localize("school.year", "Year"), localize("school.group", "Group"));
		}
		dropdown.getSecondaryDropdown().setAsNotEmpty(localize("school.must_select_group", "You must select a group."));
		
		try {
			if (getSchoolCommuneSession(iwc).getSchoolID() != -1) {
				Collection years = getSchoolCommuneBusiness(iwc).getSchoolBusiness().findAllSchoolYearsInSchool(getSchoolCommuneSession(iwc).getSchoolID());
				if (!years.isEmpty()) {
					Map yearGroupMap = getSchoolCommuneBusiness(iwc).getYearClassMap(years, _schoolID, getSchoolCommuneSession(iwc).getSchoolSeasonID(), localize("school.group", "Group"), showSubGroups);
					if (yearGroupMap != null) {
						Iterator iter = yearGroupMap.keySet().iterator();
						while (iter.hasNext()) {
							SchoolYear year = (SchoolYear) iter.next();
							dropdown.addMenuElement(year.getPrimaryKey().toString(), year.getSchoolYearName(), (Map) yearGroupMap.get(year));
						}
					}
				}
			}
		}
		catch (RemoteException e) {
			e.printStackTrace();
		}

		return dropdown;
	}
	
	private void reject(IWContext iwc) throws RemoteException {
		String messageHeader = localize("school.reject_message_header", "School choice rejected.");
		String messageBody = iwc.getParameter(PARAMETER_REJECT_MESSAGE);
		getPlacementBusiness(iwc).rejectApplication(_choiceID, getSchoolCommuneSession(iwc).getSchoolSeasonID(), iwc.getCurrentUser(), messageHeader, messageBody);

		if (iwc.isParameterSet(PARAMETER_SET_AS_DEFAULT)) {
			try {
				School school = getSchoolBusiness(iwc).getSchoolHome().findByPrimaryKey(new Integer(_schoolID));
				getSchoolBusiness(iwc).setProperty(school, SchoolBusinessBean.PROPERTY_NAME_REJECT_STUDENT_MESSAGE, messageBody);
			}
			catch (FinderException e) {
				e.printStackTrace();
			}
		}
		getParentPage().setParentToReload();
		getParentPage().close();
	}

	private void changePlacementDate(IWContext iwc) throws RemoteException {
		if (iwc.isParameterSet(PARAMETER_DATE)) {
			IWTimestamp newDate = new IWTimestamp(iwc.getParameter(PARAMETER_DATE));
			SchoolChoice choice;
			try {
				choice = getSchoolCommuneBusiness(iwc).getSchoolChoiceBusiness().getSchoolChoice(_choiceID);
				choice.setPlacementDate(newDate.getDate());
				choice.store();
			}
			catch (FinderException e) {
				e.printStackTrace();
			}
		}

		getParentPage().setParentToReload();
		getParentPage().close();
	}

	private void replace(IWContext iwc) throws RemoteException {
		if (iwc.isParameterSet(PARAMETER_DATE)) {
			String message = iwc.isParameterSet(PARAMETER_REPLACE_MESSAGE) ? iwc.getParameter(PARAMETER_REPLACE_MESSAGE) : "";
			String date = iwc.getParameter(PARAMETER_DATE);

			IWTimestamp stamp = new IWTimestamp(date);
			int schoolTypeID = getSchoolBusiness(iwc).getSchoolTypeIdFromSchoolClass(_schoolClassID);
			SchoolClassMember member = getSchoolCommuneBusiness(iwc).getSchoolBusiness().storeSchoolClassMember(_userID, _schoolClassID, _schoolYearID, schoolTypeID, stamp.getTimestamp(), ((Integer) iwc.getCurrentUser().getPrimaryKey()).intValue(), message);
			getSchoolCommuneBusiness(iwc).setStudentAsSpeciallyPlaced(member);
			getSchoolCommuneBusiness(iwc).setNeedsSpecialAttention(member, getSchoolCommuneBusiness(iwc).getPreviousSchoolSeason(getSchoolCommuneSession(iwc).getSchoolSeasonID()), true);
			if (_choiceID != -1)
				getSchoolCommuneBusiness(iwc).getSchoolChoiceBusiness().groupPlaceAction(new Integer(_choiceID), iwc.getCurrentUser());

			getParentPage().setParentToReload();
			getParentPage().close();
		}
		else {
			_protocol = false;
			_method = METHOD_REPLACE;
		}
	}

	private void moveGroup(IWContext iwc) throws RemoteException {
		getSchoolCommuneBusiness(iwc).moveToGroup(_userID, _schoolClassID, getSchoolCommuneSession(iwc).getSchoolClassID(), _schoolYearID);
		URLUtil URL = new URLUtil(BuilderLogic.getInstance().getIBPageURL(iwc, _pageID));
		if (!_showNoChoices)
			URL.addParameter(SchoolClassEditor.PARAMETER_ACTION, SchoolClassEditor.ACTION_SAVE);
		getParentPage().setParentToRedirect(URL.toString());
		getParentPage().close();
	}

	private void moveYear(IWContext iwc) throws RemoteException {
		getSchoolCommuneBusiness(iwc).getSchoolChoiceBusiness().changeSchoolYearForChoice(_userID, getSchoolCommuneSession(iwc).getSchoolSeasonID(), _schoolYearID);
		getParentPage().setParentToReload();
		getParentPage().close();
	}

	private void finalizeGroup(IWContext iwc) throws RemoteException {
		String subject = iwc.getParameter(PARAMETER_FINALIZE_SUBJECT);
		String body = iwc.getParameter(PARAMETER_FINALIZE_BODY);

		int schoolClassID = getSchoolCommuneSession(iwc).getSchoolClassID();
		SchoolClass schoolClass = getSchoolCommuneBusiness(iwc).getSchoolBusiness().findSchoolClass(new Integer(schoolClassID));
		if (schoolClass != null) {
			if (schoolClass.getReady()) {
				getSchoolCommuneBusiness(iwc).markSchoolClassLocked(schoolClass);
				getSchoolCommuneBusiness(iwc).finalizeGroup(schoolClass, subject, body, true);
			}
			else {
				getSchoolCommuneBusiness(iwc).markSchoolClassReady(schoolClass);
				getSchoolCommuneBusiness(iwc).finalizeGroup(schoolClass, subject, body, false);
			}
		}

		URLUtil URL = new URLUtil(BuilderLogic.getInstance().getIBPageURL(iwc, _pageID));
		URL.addParameter(SchoolClassEditor.PARAMETER_ACTION, SchoolClassEditor.ACTION_SAVE);
		getParentPage().setParentToRedirect(URL.toString());
		getParentPage().close();
	}

	private void editStudent(IWContext iwc) throws RemoteException {
		User user = getUserBusiness(iwc).getUser(new Integer(_userID));

		String first = iwc.getParameter("first_name");
		String middle = iwc.getParameter("middle_name");
		String last = iwc.getParameter("last_name");
		String address = iwc.getParameter("address");
		String postalCode = iwc.getParameter("postal_code");
		String postalName = iwc.getParameter("postal_name");

		getUserBusiness(iwc).updateCitizen(_userID, first, middle, last, user.getPersonalID());
		getUserBusiness(iwc).updateCitizenAddress(_userID, address, postalCode, postalName);

		getParentPage().setParentToRedirect(BuilderLogic.getInstance().getIBPageURL(iwc, _pageID));
		getParentPage().close();
	}

	private void addStudent(IWContext iwc) throws RemoteException {
		if (_userID != -1) {
			int schoolTypeID = getSchoolBusiness(iwc).getSchoolTypeIdFromSchoolClass(getSchoolCommuneSession(iwc).getSchoolClassID());
			getSchoolCommuneBusiness(iwc).getSchoolBusiness().storeSchoolClassMember(_userID, getSchoolCommuneSession(iwc).getSchoolClassID(), getSchoolCommuneSession(iwc).getSchoolYearID(), schoolTypeID, new IWTimestamp().getTimestamp(), ((Integer) iwc.getCurrentUser().getPrimaryKey()).intValue(), iwc.getParameter(PARAMETER_COMMENT));
			getParentPage().setParentToRedirect(BuilderLogic.getInstance().getIBPageURL(iwc, _pageID));
			getParentPage().close();
		}
		else
			_method = METHOD_ADD_STUDENT;
	}

	private void createStudent(IWContext iwc) throws RemoteException {
		try {
			User user = getUserBusiness(iwc).createSpecialCitizenByPersonalIDIfDoesNotExist(searchString, null, null, searchString);
			_userID = ((Integer) user.getPrimaryKey()).intValue();
			int schoolTypeID = getSchoolBusiness(iwc).getSchoolTypeIdFromSchoolClass(getSchoolCommuneSession(iwc).getSchoolClassID());
			getSchoolCommuneBusiness(iwc).getSchoolBusiness().storeSchoolClassMember(_userID, getSchoolCommuneSession(iwc).getSchoolClassID(), getSchoolCommuneSession(iwc).getSchoolYearID(), schoolTypeID, new IWTimestamp().getTimestamp(), ((Integer) iwc.getCurrentUser().getPrimaryKey()).intValue());
			_method = METHOD_EDIT_STUDENT;
		}
		catch (CreateException ce) {
			ce.printStackTrace(System.err);
		}
	}

	private void parse(IWContext iwc) throws RemoteException {
		if (iwc.isParameterSet(PARAMETER_METHOD))
			_method = Integer.parseInt(iwc.getParameter(PARAMETER_METHOD));

		if (iwc.isParameterSet(PARAMETER_ACTION))
			_action = Integer.parseInt(iwc.getParameter(PARAMETER_ACTION));

		if (iwc.isParameterSet(PARAMETER_USER_ID))
			_userID = Integer.parseInt(iwc.getParameter(PARAMETER_USER_ID));

		if (iwc.isParameterSet(PARAMETER_CHOICE_ID))
			_choiceID = Integer.parseInt(iwc.getParameter(PARAMETER_CHOICE_ID));

		if (iwc.isParameterSet(getSchoolCommuneSession(iwc).getParameterSchoolYearID()))
			_schoolYearID = Integer.parseInt(iwc.getParameter(getSchoolCommuneSession(iwc).getParameterSchoolYearID()));

		if (iwc.isParameterSet(getSchoolCommuneSession(iwc).getParameterSchoolClassID()))
			_schoolClassID = Integer.parseInt(iwc.getParameter(getSchoolCommuneSession(iwc).getParameterSchoolClassID()));

		if (iwc.isParameterSet(PARAMETER_SHOW_ONLY_OVERVIEW))
			_showOnlyOverview = true;

		if (iwc.isParameterSet(PARAMETER_SHOW_NO_CHOICES))
			_showNoChoices = true;

		if (iwc.isParameterSet(PARAMETER_PAGE_ID))
			_pageID = Integer.parseInt(iwc.getParameter(PARAMETER_PAGE_ID));

		//if (_schoolClassID != -1 && _schoolYearID != -1)
		//validateSchoolClass(iwc);

		if (iwc.isParameterSet(PARAMETER_SEARCH))
			searchString = iwc.getParameter(PARAMETER_SEARCH);
		if (searchString != null && searchString.length() > 0) {
			try {
				String temp = searchString;
				temp = TextSoap.findAndCut(temp, "-");
				Long.parseLong(temp);
				if (temp.length() == 10) {
					int firstTwo = Integer.parseInt(temp.substring(0, 2));
					if (firstTwo < 04) {
						temp = "20" + temp;
					}
					else {
						temp = "19" + temp;
					}
				}
				searchString = temp;
			}
			catch (NumberFormatException nfe) {
			}
		}

		_schoolID = getSchoolCommuneSession(iwc).getSchoolID();

		if (iwc.isParameterSet(PARAMETER_SCHOOL_CLASS_MEMBER_ID)) {
			_schoolClassMemberID = Integer.parseInt(iwc.getParameter(PARAMETER_SCHOOL_CLASS_MEMBER_ID));
		}
		
		if (iwc.isParameterSet(PARAMETER_RESOURCE_PERMISSION) 
				&& iwc.getParameter(PARAMETER_RESOURCE_PERMISSION).equals(PARAMETER_RESOURCE_PERM_VALUE_CENTRAL_ADMIN)) {
			_viewAllResources = true;
		}
		
		if (iwc.isParameterSet(PARAMETER_FROM_CENTRAL_PLACEMENT_EDITOR)) {
			if ("true".equals(iwc.getParameter(PARAMETER_FROM_CENTRAL_PLACEMENT_EDITOR))) {
				_fromCentralPlacementEditor = true;
			}
		}
	}

	protected DropdownMenu getSchoolYears(IWContext iwc) throws RemoteException {
		DropdownMenu menu = new DropdownMenu(getSchoolCommuneSession(iwc).getParameterSchoolYearID());

		if (getSchoolCommuneSession(iwc).getSchoolID() != -1) {
			List years = new Vector(getSchoolCommuneBusiness(iwc).getSchoolBusiness().findAllSchoolYearsInSchool(getSchoolCommuneSession(iwc).getSchoolID()));
			if (!years.isEmpty()) {
				Collections.sort(years, new SchoolYearComparator());
				Iterator iter = years.iterator();
				while (iter.hasNext()) {
					SchoolYear element = (SchoolYear) iter.next();
					menu.addMenuElement(element.getPrimaryKey().toString(), element.getSchoolYearName());
					if (_schoolYearID == -1)
						_schoolYearID = ((Integer) element.getPrimaryKey()).intValue();
				}
			}
			else {
				_schoolYearID = -1;
				menu.addMenuElement(-1, "");
			}
		}
		else {
			menu.addMenuElement(-1, "");
		}

		if (_schoolYearID != -1)
			menu.setSelectedElement(_schoolYearID);

		return (DropdownMenu) getStyledInterface(menu);
	}

	/*protected DropdownMenu getSchoolClasses(IWContext iwc, boolean setToSubmit) throws RemoteException {
	 DropdownMenu menu = new DropdownMenu(getSchoolCommuneSession(iwc).getParameterSchoolClassID());
	 if (setToSubmit) {
	 menu.setToSubmit();
	 }

	 if (_schoolYearID == -1)
	 _schoolYearID = getSchoolCommuneSession(iwc).getSchoolYearID();

	 if (getSchoolCommuneSession(iwc).getSchoolID() != -1 && getSchoolCommuneSession(iwc).getSchoolSeasonID() != -1 && _schoolYearID != -1) {
	 Collection classes = getSchoolCommuneBusiness(iwc).getSchoolBusiness().findSchoolClassesBySchoolAndSeasonAndYear(getSchoolCommuneSession(iwc).getSchoolID(), getSchoolCommuneSession(iwc).getSchoolSeasonID(), _schoolYearID);
	 if (!classes.isEmpty()) {
	 Iterator iter = classes.iterator();
	 while (iter.hasNext()) {
	 SchoolClass element = (SchoolClass) iter.next();
	 menu.addMenuElement(element.getPrimaryKey().toString(), element.getName());
	 if (_schoolClassID == -1)
	 _schoolClassID = ((Integer) element.getPrimaryKey()).intValue();
	 }
	 }
	 else {
	 _schoolClassID = -1;
	 menu.addMenuElement(-1, "");
	 }
	 }
	 else {
	 menu.addMenuElement(-1, "");
	 }

	 if (_schoolClassID != -1)
	 menu.setSelectedElement(_schoolClassID);

	 return (DropdownMenu) getStyledInterface(menu);
	 }*/

	protected DropdownMenu getSchools(IWContext iwc) throws RemoteException {
		DropdownMenu menu = new DropdownMenu(PARAMETER_SCHOOL_ID);
		menu.setToSubmit();
		menu.keepStatusOnAction(true);

		Collection classes = getSchoolCommuneBusiness(iwc).getSchoolBusiness().findAllSchools();
		if (!classes.isEmpty()) {
			Iterator iter = classes.iterator();
			while (iter.hasNext()) {
				School element = (School) iter.next();
				menu.addMenuElement(element.getPrimaryKey().toString(), element.getName());
			}
		}

		return (DropdownMenu) getStyledInterface(menu);
	}

	private DropdownMenu getAssignableResources(IWContext iwc) throws RemoteException {
		DropdownMenu DD = new DropdownMenu(PARAMETER_RESOURCE_ID);
		DD.addMenuElement("-1", "- " + localize("cacc_chose_resource", "chose resource") + " -");
		Collection rscColl = null;

		Integer providerGrpId = getProviderGrpId(iwc);
		int classMemberId = 0;
		classMemberId = _schoolClassMemberID;
		if (providerGrpId != null && classMemberId != 0) {
			if (_viewAllResources)
				// central administration is using this block, and has permission to see all resources
				rscColl = getResourceBusiness(iwc).getAssignableResourcesForPlacement(VIEW_ALL_RESOURCES_GRP_ID, new Integer(classMemberId));
			else
				// Only resources with a permission for the provider group id(from getProviderGrpId()) should be visible
				rscColl = getResourceBusiness(iwc).getAssignableResourcesForPlacement(providerGrpId, new Integer(classMemberId));
			
			for (Iterator iter = rscColl.iterator(); iter.hasNext(); ) {
				Resource currRsc = (Resource) iter.next();
				DD.addMenuElement(currRsc.getPrimaryKey().toString(), currRsc.getResourceName());
			}
		}
		DD.setSelectedElement("-1");

		return DD;
	}

	private String saveResource(IWContext iwc) throws RemoteException {
		int rscID = -1;
		String errMsg = null;

		String rscIdStr = iwc.getParameter(PARAMETER_RESOURCE_ID);
		if (rscIdStr != null)
			rscID = Integer.parseInt(rscIdStr);

		// Save the resource placement
		String startDateStr = iwc.getParameter(PARAMETER_RESOURCE_STARTDATE);
		String endDateStr = iwc.getParameter(PARAMETER_RESOURCE_ENDDATE);
		
		int registratorID = iwc.getCurrentUserId();
		try {
			getResourceBusiness(iwc).createResourcePlacement(rscID, _schoolClassMemberID, startDateStr, endDateStr, registratorID, _viewAllResources);
		}
		catch (ResourceException re) {
			errMsg = localize(re.getKey(), re.getDefTrans());
		}
		catch (DateException de) {
			errMsg = localize(de.getKey(), de.getDefTrans());
		}
		catch (ClassMemberException cme) {
			errMsg = localize(cme.getKey(), cme.getDefTrans());
		}
		return errMsg;
	}

	private String finishResource(IWContext iwc) throws RemoteException, FinderException {
		String errMsg = null;
		Integer classMemberID = new Integer(iwc.getParameter(PARAMETER_RESOURCE_CLASS_MEMBER));
		String startDateStr = iwc.getParameter(PARAMETER_RESOURCE_STARTDATE);
		String endDateStr = iwc.getParameter(PARAMETER_RESOURCE_ENDDATE);
		try {
			getResourceBusiness(iwc).finishResourceClassMember(new Integer(_schoolClassMemberID), 
					classMemberID, startDateStr, endDateStr, _viewAllResources);
		}
		catch (DateException de) {
			errMsg = localize(de.getKey(), de.getDefTrans());
		}
		catch (ClassMemberException cme) {
			errMsg = localize(cme.getKey(), cme.getDefTrans());
		}
		return errMsg;
	}

	private void deleteResource(IWContext iwc) {
		Integer rscPlaceID;
		if (iwc.isParameterSet(PARAMETER_DELETE_RESOURCE_PLACEMENT)) {
			try {
				rscPlaceID = new Integer(iwc.getParameter(PARAMETER_DELETE_RESOURCE_PLACEMENT));
				getResourceBusiness(iwc).deleteResourceClassMember(rscPlaceID);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private void saveNativeLanguage(IWContext iwc) throws RemoteException {
		if (_userID != -1) {
			User pupil = getUserBusiness(iwc).getUser(_userID);
			if (iwc.isParameterSet(PARAMETER_NATIVE_LANG)) {
				String nLangIdStr = iwc.getParameter(PARAMETER_NATIVE_LANG);
				int langID = Integer.parseInt(nLangIdStr);
				pupil.setNativeLanguage(langID);
				pupil.store();
			}
		}
	}

	private Integer getProviderGrpId(IWContext iwc) {
		/** ******************** Bundle properties ******************* */
		String BUNDLE_NAME_COMMUNE = "se.idega.idegaweb.commune";
		String PROP_COMMUNE_PROVIDER_GRP_ID = "provider_administrators_group_id";

		// Get group id from the commune bundle for the group Provider
		IWBundle communeBundle = iwc.getIWMainApplication().getBundle(BUNDLE_NAME_COMMUNE);
		String anordnareIdStr = communeBundle.getProperty(PROP_COMMUNE_PROVIDER_GRP_ID);

		return new Integer(anordnareIdStr);
	}

	/*private void validateSchoolClass(IWContext iwc) throws RemoteException {
	 SchoolClass schoolClass = getSchoolCommuneBusiness(iwc).getSchoolBusiness().findSchoolClass(new Integer(_schoolClassID));
	 SchoolYear schoolYear = getSchoolCommuneBusiness(iwc).getSchoolBusiness().getSchoolYear(new Integer(_schoolYearID));
	 if (schoolYear != null || !schoolClass.hasRelationToSchoolYear(schoolYear)) {
	 Collection schoolClasses = getSchoolCommuneBusiness(iwc).getSchoolBusiness().findSchoolClassesBySchoolAndSeasonAndYear(getSchoolCommuneSession(iwc).getSchoolID(), getSchoolCommuneSession(iwc).getSchoolSeasonID(), _schoolYearID);
	 if (!schoolClasses.isEmpty()) {
	 Iterator iter = schoolClasses.iterator();
	 while (iter.hasNext()) {
	 _schoolClassID = ((Integer) ((SchoolClass) iter.next()).getPrimaryKey()).intValue();
	 continue;
	 }
	 }
	 }
	 }*/
	
	
	private PlacementBusiness getPlacementBusiness(IWContext iwc) throws RemoteException {
		return (PlacementBusiness) IBOLookup.getServiceInstance(iwc, PlacementBusiness.class);
	}

	
	private CentralPlacementBusiness getCentralPlacementBusiness(IWContext iwc) throws RemoteException {
		return (CentralPlacementBusiness) IBOLookup.getServiceInstance(iwc, CentralPlacementBusiness.class);
	}
	
	private SchoolCommuneBusiness getSchoolCommuneBusiness(IWContext iwc) throws RemoteException {
		return (SchoolCommuneBusiness) IBOLookup.getServiceInstance(iwc, SchoolCommuneBusiness.class);
	}

	private SchoolBusiness getSchoolBusiness(IWContext iwc) throws RemoteException {
		return (SchoolBusiness) IBOLookup.getServiceInstance(iwc, SchoolBusiness.class);
	}

	private CommuneUserBusiness getCommuneUserBusiness(IWContext iwc) throws RemoteException {
		return getUserBusiness(iwc);
	}

	private SchoolCommuneSession getSchoolCommuneSession(IWContext iwc) throws RemoteException {
		return (SchoolCommuneSession) IBOLookup.getSessionInstance(iwc, SchoolCommuneSession.class);
	}


	private FamilyLogic getMemberFamilyLogic(IWContext iwc) throws RemoteException {
		return getUserBusiness(iwc).getMemberFamilyLogic();
	}

	private ResourceBusiness getResourceBusiness(IWContext iwc) throws RemoteException {
		return (ResourceBusiness) IBOLookup.getServiceInstance(iwc, ResourceBusiness.class);
	}

	private void changeStudyPath(final IWContext context) {
		final SchoolClassMember student = (SchoolClassMember) context.getSessionAttribute(getClass() + PARAMETER_SCHOOL_CLASS_MEMBER_ID);
		final int studyPathId = Integer.parseInt(context.getParameter("school_study_path"));
		student.setStudyPathId(studyPathId);
		student.store();
	}

	private Table getChangeStudyPathForm(final IWContext context) throws RemoteException {
		Table table = new Table();
		table.setBorder(0);
		table.setCellpadding(1);
		table.setCellspacing(2);
		final DropdownMenu studyPathDropdown = new DropdownMenu("school_study_path");
		studyPathDropdown.setToSubmit(false);
		final SchoolStudyPath[] studyPaths = getSchoolCommuneBusiness(context).getAllStudyPaths();
		for (int i = 0; i < studyPaths.length; i++) {
			studyPathDropdown.addMenuElement(studyPaths[i].getPrimaryKey().toString(), studyPaths[i].getCode());
		}
		table.add(studyPathDropdown, 1, 1);
		final SubmitButton submit = (SubmitButton) getStyledInterface(new SubmitButton(localize("school.change", "Change"), PARAMETER_METHOD, String.valueOf(METHOD_OVERVIEW)));
		table.add(new HiddenInput(PARAMETER_ACTION, String.valueOf(ACTION_CHANGE_STUDY_PATH)), 1, 2);
		table.add(submit, 1, 2);
		table.add(Text.getNonBrakingSpace(), 1, 2);
		table.add(close, 1, 2);
		table.mergeCells(1, 2, 2, 2);
		table.setWidth(Table.HUNDRED_PERCENT);
		table.setHeight(Table.HUNDRED_PERCENT);
		table.addText(Text.NON_BREAKING_SPACE, 3, 1);
		table.addText(Text.NON_BREAKING_SPACE, 1, 2);
		table.mergeCells(1, 3, 3, 3);
		table.setRowHeight(3, "100%");

		return table;
	}

}