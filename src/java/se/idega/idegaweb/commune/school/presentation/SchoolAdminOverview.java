package se.idega.idegaweb.commune.school.presentation;

import is.idega.idegaweb.member.business.MemberFamilyLogic;
import is.idega.idegaweb.member.business.NoCustodianFound;

import java.rmi.RemoteException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.ejb.CreateException;
import javax.ejb.FinderException;

import se.idega.idegaweb.commune.business.CommuneUserBusiness;
import se.idega.idegaweb.commune.presentation.CommuneBlock;
import se.idega.idegaweb.commune.school.business.SchoolCommuneBusiness;
import se.idega.idegaweb.commune.school.business.SchoolCommuneSession;
import se.idega.idegaweb.commune.school.data.SchoolChoice;
import se.idega.idegaweb.commune.school.data.SchoolChoiceHome;
import se.idega.util.PIDChecker;

import com.idega.block.school.business.SchoolYearComparator;
import com.idega.block.school.data.School;
import com.idega.block.school.data.SchoolClass;
import com.idega.block.school.data.SchoolClassMember;
import com.idega.block.school.data.SchoolSeason;
import com.idega.block.school.data.SchoolType;
import com.idega.block.school.data.SchoolYear;
import com.idega.business.IBOLookup;
import com.idega.core.data.Address;
import com.idega.core.data.Country;
import com.idega.core.data.CountryHome;
import com.idega.core.data.Email;
import com.idega.core.data.Phone;
import com.idega.core.data.PostalCode;
import com.idega.data.IDOLookup;
import com.idega.presentation.IWContext;
import com.idega.presentation.Table;
import com.idega.presentation.text.Break;
import com.idega.presentation.text.HorizontalRule;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.CheckBox;
import com.idega.presentation.ui.CloseButton;
import com.idega.presentation.ui.DateInput;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.HiddenInput;
import com.idega.presentation.ui.RadioButton;
import com.idega.presentation.ui.SubmitButton;
import com.idega.presentation.ui.TextArea;
import com.idega.presentation.ui.TextInput;
import com.idega.presentation.ui.Window;
import com.idega.user.business.NoEmailFoundException;
import com.idega.user.business.NoPhoneFoundException;
import com.idega.user.business.UserBusiness;
import com.idega.user.data.User;
import com.idega.util.Age;
import com.idega.util.IWCalendar;
import com.idega.util.IWTimestamp;
import com.idega.util.PersonalIDFormatter;
import com.idega.util.text.TextSoap;

/**
 * @author laddi
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class SchoolAdminOverview extends CommuneBlock {

	public static final String PARAMETER_ACTION = "sch_admin_action";
	public static final String PARAMETER_METHOD = "sch_admin_method";
	public static final String PARAMETER_USER_ID = "sch_user_id";
	public static final String PARAMETER_CHOICE_ID = "sch_choice_id";
	public static final String PARAMETER_SHOW_ONLY_OVERVIEW = "sch_show_only_overview";
	public static final String PARAMETER_SHOW_NO_CHOICES = "sch_show_no_choices";
	public static final String PARAMETER_SEARCH = "sch_search";

	public static final int METHOD_OVERVIEW = 1;
	public static final int METHOD_REJECT = 2;
	public static final int METHOD_REPLACE = 3;
	public static final int METHOD_MOVE = 4;
	public static final int METHOD_MOVE_GROUP = 5;
	public static final int METHOD_MOVE_YEAR = 6;
	public static final int METHOD_FINALIZE_GROUP = 7;
	public static final int METHOD_EDIT_STUDENT = 8;
	public static final int METHOD_ADD_STUDENT = 9;

	public static final int ACTION_REJECT = 1;
	public static final int ACTION_REPLACE = 2;
	public static final int ACTION_MOVE = 3;
	public static final int ACTION_MOVE_GROUP = 4;
	public static final int ACTION_MOVE_YEAR = 5;
	public static final int ACTION_FINALIZE_GROUP = 6;
	public static final int ACTION_EDIT_STUDENT = 7;
	public static final int ACTION_ADD_STUDENT = 8;
	public static final int ACTION_CREATE_STUDENT = 9;

	private static final String PARAMETER_REJECT_MESSAGE = "sch_admin_reject_message";
	private static final String PARAMETER_REPLACE_MESSAGE = "sch_admin_replace_message";
	private static final String PARAMETER_MOVE_MESSAGE = "sch_admin_replace_message";
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

	private boolean _protocol = true;
	private boolean _move = true;
	private boolean _showOnlyOverview = false;
	private boolean _showNoChoices = false;

	private CloseButton close;
	private String searchString;

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
			case ACTION_MOVE :
				move(iwc);
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
		}

		if (_method != -1)
			drawForm(iwc);
	}

	private void drawForm(IWContext iwc) throws RemoteException {
		Form form = new Form();
		form.maintainParameter(PARAMETER_USER_ID);
		form.maintainParameter(PARAMETER_CHOICE_ID);
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

		switch (_method) {
			case METHOD_OVERVIEW :
				headerTable.add(getHeader(localize("school.student_overview", "Student overview")));
				contentTable.add(getOverview(iwc));
				break;
			case METHOD_REJECT :
				headerTable.add(getHeader(localize("school.reject_student", "Reject student")));
				contentTable.add(getRejectForm(iwc));
				break;
			case METHOD_REPLACE :
				headerTable.add(getHeader(localize("school.student_replacing", "Replace student")));
				contentTable.add(getReplaceForm(iwc));
				break;
			case METHOD_MOVE :
				headerTable.add(getHeader(localize("school.student_move", "Move student")));
				contentTable.add(getMoveForm(iwc));
				break;
			case METHOD_MOVE_GROUP :
				headerTable.add(getHeader(localize("school.student_move_group", "Move student to group")));
				contentTable.add(getMoveGroupForm(iwc));
				break;
			case METHOD_MOVE_YEAR :
				headerTable.add(getHeader(localize("school.student_move_year", "Change year of school choice")));
				contentTable.add(getMoveYearForm(iwc));
				break;
			case METHOD_FINALIZE_GROUP :
				headerTable.add(getHeader(localize("school.finalize_group", "Finalize group")));
				contentTable.add(getFinalizeGroupForm(iwc));
				break;
			case METHOD_EDIT_STUDENT :
				headerTable.add(getHeader(localize("school.edit_student", "Edit student")));
				contentTable.add(getEditStudentForm(iwc));
				break;
			case METHOD_ADD_STUDENT :
				headerTable.add(getHeader(localize("school.add_student", "Add student")));
				contentTable.add(getAddStudentForm(iwc));
				break;
		}
		
		add(form);
	}

	private Table getOverview(IWContext iwc) throws RemoteException {
		Table table = new Table();
		table.setCellpadding(5);
		table.setWidth(Table.HUNDRED_PERCENT);
		table.setHeight(Table.HUNDRED_PERCENT);
		int row = 1;

		if (_userID != -1) {
			User user = getUserBusiness(iwc).getUser(_userID);
			Address address = getUserBusiness(iwc).getUserAddress1(_userID);

			table.add(getSmallHeader(localize("school.name", "Name")), 1, row);
			table.add(getSmallText(user.getNameLastFirst(true)), 2, row++);

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
						table.add(getSmallText(parent.getNameLastFirst(true)), 2, row);
						try {
							Phone phone = getCommuneUserBusiness(iwc).getUsersHomePhone(parent);
							if (phone != null && phone.getNumber() != null) {
								table.add(new Break(), 2, row);
								table.add(getSmallText(localize("school.phone","Phone")+": "), 2, row);
								table.add(getSmallText(phone.getNumber()), 2, row);
							}
						}
						catch (NoPhoneFoundException npf) {
						}
						try {
							Email email = getCommuneUserBusiness(iwc).getUsersMainEmail(parent);
							if (email != null && email.getEmailAddress() != null) {
								Link emailLink = this.getSmallLink(email.getEmailAddress());
								emailLink.setURL("mailto:"+email.getEmailAddress());
								table.add(new Break(), 2, row);
								table.add(getSmallText(localize("school.email","E-mail")+": "), 2, row);
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

			int pendingSchoolId = -1;

			if (!_showNoChoices) {
				Collection choices = getSchoolCommuneBusiness(iwc).getSchoolChoiceBusiness().findByStudentAndSeason(_userID, getSchoolCommuneSession(iwc).getSchoolSeasonID());
				String message = null;
				String language = null;
				IWCalendar calendar = null;
				if (!choices.isEmpty()) {
					table.add(getSmallHeader(localize("school.school_choice", "School choices")), 1, row);

					School school;
					SchoolChoice choice;
					Iterator iter = choices.iterator();
					while (iter.hasNext()) {
						choice = (SchoolChoice) iter.next();

						if (choice.getChosenSchoolId() != -1) {
							school = getSchoolCommuneBusiness(iwc).getSchoolBusiness().getSchool(new Integer(choice.getChosenSchoolId()));
							String string = String.valueOf(choice.getChoiceOrder()) + ". " + school.getName() + " (" + getSchoolCommuneBusiness(iwc).getLocalizedCaseStatusDescription(choice.getCaseStatus(), iwc.getCurrentLocale()) + ")";
							if (choice.getStatus().equalsIgnoreCase("PREL") || choice.getStatus().equalsIgnoreCase("PLAC") || choice.getStatus().equalsIgnoreCase("FLYT")) {
								if (pendingSchoolId == -1)
									pendingSchoolId = choice.getChosenSchoolId();
								//							table.add("gimmi flippari ", 2, row);	
							}
							if (choice.getChosenSchoolId() == getSchoolCommuneSession(iwc).getSchoolID()) {
								table.add(this.getSmallHeader(string), 2, row);
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
						if (message == null)
							message = choice.getMessage();
						if (language == null && choice.getLanguageChoice() != null)
							language = choice.getLanguageChoice();
						calendar = new IWCalendar(iwc.getCurrentLocale(), choice.getCreated());
					}
					row++;
				}

				if (calendar != null) {
					table.add(getSmallHeader(localize("school.date","Date")), 1, row);
					table.add(getSmallText(calendar.getLocaleDate(IWCalendar.SHORT)), 2, row++);
				}
				if (language != null && language.length() > 0) {
					table.add(getSmallHeader(localize("school.school_choice_language", "Preferred language")), 1, row);
					table.add(getSmallText(localize(language,language)), 2, row++);
				}
				if (message != null) {
					table.add(getSmallHeader(localize("school.school_choice_message", "Applicant message")), 1, row);
					table.add(getSmallText(message), 2, row++);
				}
			}

			if (!_showNoChoices) {
				table.add(getSmallHeader(localize("school.current_shool", "Current school")), 1, row);
				SchoolSeason season = getSchoolCommuneBusiness(iwc).getPreviousSchoolSeason(getSchoolCommuneSession(iwc).getSchoolSeasonID());
				if (season != null) {
					SchoolClassMember schoolClassMember = getSchoolCommuneBusiness(iwc).getSchoolBusiness().findByStudentAndSeason(user, season);
					if (schoolClassMember != null) {
						SchoolClass schoolClass = getSchoolCommuneBusiness(iwc).getSchoolBusiness().findSchoolClass(new Integer(schoolClassMember.getSchoolClassId()));
						School currentSchool = getSchoolCommuneBusiness(iwc).getSchoolBusiness().getSchool(new Integer(schoolClass.getSchoolId()));
						SchoolYear schoolYear = getSchoolCommuneBusiness(iwc).getSchoolBusiness().getSchoolYear(new Integer(schoolClass.getSchoolYearId()));
	
						String schoolString = currentSchool.getName() + " - " + schoolClass.getName();
						table.add(getSmallText(schoolString), 2, row);
					}
				}
				row++;
			}

			table.setColumnVerticalAlignment(1, Table.VERTICAL_ALIGN_TOP);
			table.mergeCells(1, row, table.getColumns(), row);
			table.setHeight(row, Table.HUNDRED_PERCENT);
			table.setRowVerticalAlignment(row, Table.VERTICAL_ALIGN_BOTTOM);

			SubmitButton replace = (SubmitButton) getStyledInterface(new SubmitButton(localize("school.replace", "Replace"), PARAMETER_METHOD, String.valueOf(METHOD_REPLACE)));
			SubmitButton reject = (SubmitButton) getStyledInterface(new SubmitButton(localize("school.reject", "Reject"), PARAMETER_METHOD, String.valueOf(METHOD_REJECT)));
			SubmitButton move = (SubmitButton) getStyledInterface(new SubmitButton(localize("school.move", "Move"), PARAMETER_METHOD, String.valueOf(METHOD_MOVE)));
			SubmitButton moveYear = (SubmitButton) getStyledInterface(new SubmitButton(localize("school.change_year", "Change year"), PARAMETER_METHOD, String.valueOf(METHOD_MOVE_YEAR)));
			SubmitButton editStudent = (SubmitButton) getStyledInterface(new SubmitButton(localize("school.edit_student", "Edit student"), PARAMETER_METHOD, String.valueOf(METHOD_EDIT_STUDENT)));

			if (_schoolID != -1 && !_showOnlyOverview) {
				table.add(replace, 1, row);
				table.add(Text.NON_BREAKING_SPACE, 1, row);

				if (_schoolID == pendingSchoolId) {
					table.add(reject, 1, row);
					table.add(Text.NON_BREAKING_SPACE, 1, row);
				}

				/** Gimmi 13.11.2002 _schoolID is NOT -1 so no need to check again....    */
				/** was like this --->  if (_choiceID == -1 && _choiceID != _schoolID) {  */
				if (_choiceID == -1) {
					table.add(move, 1, row);
					table.add(Text.NON_BREAKING_SPACE, 1, row);
				}
			}

			if (_choiceID != -1 && !_showNoChoices) {
				table.add(moveYear, 1, row);
				table.add(Text.NON_BREAKING_SPACE, 1, row);
			}
			
			if (_showNoChoices) {
				table.add(editStudent, 1, row);
				table.add(Text.NON_BREAKING_SPACE, 1, row);
			}

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
		try {
			Phone phone = getUserBusiness(iwc).getUsersWorkPhone(user);
			workphone = phone.getNumber();
		}
		catch (NoPhoneFoundException npfe) {
			workphone = "";
		}

		Object[] arguments = { user.getName(), mail, workphone };

		String message = MessageFormat.format(localize("school.reject_student_message", "We are sorry that we cannot offer you a place in our school at present, if you have any questions, please contact {0} via either phone ({1}) or e-mail ({2})."), arguments);
		TextArea textArea = (TextArea) getStyledInterface(new TextArea(PARAMETER_REJECT_MESSAGE, message));
		textArea.setWidth(Table.HUNDRED_PERCENT);
		textArea.setHeight(4);

		table.add(getSmallHeader(localize("school.reject_student_message_info", "The following message will be sent to the students' parents.")), 1, row++);
		table.add(textArea, 1, row++);

		SubmitButton reject = (SubmitButton) getStyledInterface(new SubmitButton(localize("school.reject", "Reject"), PARAMETER_ACTION, String.valueOf(ACTION_REJECT)));
		table.add(reject, 1, row);
		table.add(Text.NON_BREAKING_SPACE, 1, row);
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
		table.add(new HiddenInput(PARAMETER_METHOD, String.valueOf(METHOD_REPLACE)));
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

		table.add(getNavigationTable(iwc, localize("school.replace_to", "Replace to") + ":"), 1, row++);

		//if (_protocol)
		table.add(getSmallHeader(localize("school.replace_reason", "Replace reason") + ":"), 1, row);
		//else
		//table.add(getSmallErrorText(localize("school.replace_reason","Replace reason")+":"),1,row);
		table.add(new Break(), 1, row);
		TextArea textArea = (TextArea) getStyledInterface(new TextArea(PARAMETER_REPLACE_MESSAGE));
		textArea.setWidth(Table.HUNDRED_PERCENT);
		//textArea.setAsNotEmpty(localize("school.must_provide_reason_for_replacement","You must specify a reason for replacement."));
		textArea.keepStatusOnAction(true);
		textArea.setHeight(4);
		table.add(textArea, 1, row++);

		SubmitButton replace = (SubmitButton) getStyledInterface(new SubmitButton(localize("school.replace", "Replace"), PARAMETER_ACTION, String.valueOf(ACTION_REPLACE)));
		replace.setValueOnClick(PARAMETER_METHOD, "-1");
		//replace.setToEnableWhenChecked(PARAMETER_PROTOCOL);
		table.add(replace, 1, row);
		table.add(Text.NON_BREAKING_SPACE, 1, row);
		table.add(close, 1, row);
		table.setHeight(row, Table.HUNDRED_PERCENT);
		table.setRowVerticalAlignment(row, Table.VERTICAL_ALIGN_BOTTOM);

		return table;
	}

	private Table getMoveForm(IWContext iwc) throws RemoteException {
		Table table = new Table();
		table.setCellpadding(5);
		table.setWidth(Table.HUNDRED_PERCENT);
		table.setHeight(Table.HUNDRED_PERCENT);
		table.add(new HiddenInput(PARAMETER_METHOD, String.valueOf(METHOD_MOVE)));
		int row = 1;

		User user = getUserBusiness(iwc).getUser(_userID);

		table.add(getSmallHeader(localize("school.move_student_info", "You have selected to move student: ") + user.getName() + "."), 1, row++);

		table.add(getSmallHeader(localize("school.new_school", "New school") + ": "), 1, row);
		DropdownMenu schools = getSchools(iwc);
		schools.addMenuElementFirst("-1", localize("school.move_outside_of_nacka", "Outside of Nacka"));
		table.add(schools, 1, row++);

		table.add(getSmallHeader(localize("school.move_reason_text", "The following message will be sent to the new school as the reason for move.")), 1, row++);

		table.add(getSmallText(localize("school.move_reason", "Move reason") + ":"), 1, row);
		table.add(new Break(), 1, row);
		TextArea textArea = (TextArea) getStyledInterface(new TextArea(PARAMETER_MOVE_MESSAGE));
		textArea.setWidth(Table.HUNDRED_PERCENT);
		textArea.setHeight(4);
		textArea.setAsNotEmpty(localize("school.must_provide_reason_for_move", "You must specify a reason for move."));
		table.add(textArea, 1, row++);

		SubmitButton move = (SubmitButton) getStyledInterface(new SubmitButton(localize("school.move", "Move"), PARAMETER_ACTION, String.valueOf(ACTION_MOVE)));
		move.setValueOnClick(PARAMETER_METHOD, "-1");
		table.add(move, 1, row);
		table.add(Text.NON_BREAKING_SPACE, 1, row);
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
		table.add(new HiddenInput(PARAMETER_METHOD, String.valueOf(METHOD_MOVE_GROUP)));
		//table.add(new HiddenInput(PARAMETER_ACTION, String.valueOf(ACTION_MOVE_GROUP)));
		int row = 1;

		User user = getUserBusiness(iwc).getUser(_userID);

		table.add(getSmallHeader(localize("school.move_group_info", "Select the new group for the student and click 'Move'.")), 1, row++);

		table.add(getNavigationTable(iwc, localize("school.move_to", "Move to") + ":"), 1, row++);

		SubmitButton move = (SubmitButton) getStyledInterface(new SubmitButton(localize("school.move", "Move"), PARAMETER_ACTION, String.valueOf(ACTION_MOVE_GROUP)));
		move.setValueOnClick(PARAMETER_METHOD, "-1");
		table.add(move, 1, row);
		table.add(Text.NON_BREAKING_SPACE, 1, row);
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
		table.add(new HiddenInput(PARAMETER_METHOD, String.valueOf(METHOD_MOVE_YEAR)));
		table.add(new HiddenInput(PARAMETER_ACTION, String.valueOf(ACTION_MOVE_YEAR)));
		int row = 1;

		User user = getUserBusiness(iwc).getUser(_userID);

		table.add(getSmallHeader(localize("school.move_year_info", "Select the new year for the student and click 'Move'.")), 1, row++);

		table.add(getSmallHeader(localize("school.new_year", "New year") + ": "), 1, row);

		DropdownMenu menu = getSchoolYears(iwc);
		menu.setToSubmit(false);
		table.add(menu, 1, row++);

		SubmitButton move = (SubmitButton) getStyledInterface(new SubmitButton(localize("school.move", "Move")));
		move.setValueOnClick(PARAMETER_METHOD, "-1");
		table.add(move, 1, row);
		table.add(Text.NON_BREAKING_SPACE, 1, row);
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
		table.add(new HiddenInput(PARAMETER_METHOD, String.valueOf(METHOD_FINALIZE_GROUP)));
		table.add(new HiddenInput(PARAMETER_ACTION, String.valueOf(ACTION_FINALIZE_GROUP)));
		int row = 1;

		String subject = null;
		String body = null;
		int schoolClassID = getSchoolCommuneSession(iwc).getSchoolClassID();
		SchoolClass schoolClass = getSchoolCommuneBusiness(iwc).getSchoolBusiness().findSchoolClass(new Integer(schoolClassID));
		if (schoolClass != null) {
			if (schoolClass.getReady()) {
				subject = localize("school.finalize_subject", "");
				body = localize("school.finalize_body", "");
			}
			else {
				subject = localize("school.students_put_in_class_subject", "");
				body = localize("school.students_put_in_class_body", "");
			}

			if (body != null) {
				School school = getSchoolCommuneBusiness(iwc).getSchoolBusiness().getSchool(new Integer(schoolClass.getSchoolId()));
				Object[] arguments = { school.getName(), schoolClass.getName() };
				body = MessageFormat.format(body, arguments);
			}
		}

		table.add(getSmallHeader(localize("school.finalize_header", "Message headline") + ": "), 1, row);
		TextInput header = (TextInput) getStyledInterface(new TextInput(PARAMETER_FINALIZE_SUBJECT));
		header.setLength(40);
		header.setAsNotEmpty(localize("school.not_empty_finalize_subject","Message subject can not be empty."));
		if (subject != null)
			header.setContent(subject);
		table.add(header, 1, row++);
		
		table.add(getSmallHeader(localize("school.finalize_text", "Message body") + ":"), 1, row);
		table.add(new Break(), 1, row);
		TextArea text = (TextArea) getStyledInterface(new TextArea(PARAMETER_FINALIZE_BODY));
		text.setWidth(Table.HUNDRED_PERCENT);
		text.setRows(10);
		text.setAsNotEmpty(localize("school.not_empty_finalize_body","Message body can not be empty."));
		if (body != null)
			text.setContent(body);
		table.add(text, 1, row++);
		
		SubmitButton move = (SubmitButton) getStyledInterface(new SubmitButton(localize("school.send", "Send")));
		move.setValueOnClick(PARAMETER_METHOD, "-1");
		table.add(move, 1, row);
		table.add(Text.NON_BREAKING_SPACE, 1, row);
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
		table.add(new HiddenInput(PARAMETER_METHOD, String.valueOf(METHOD_EDIT_STUDENT)));
		table.add(new HiddenInput(PARAMETER_ACTION, String.valueOf(ACTION_EDIT_STUDENT)));
		table.add(new HiddenInput(PARAMETER_USER_ID, String.valueOf(_userID)));
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
		table.add(Text.NON_BREAKING_SPACE, 1, row);
		table.add(close, 1, row);
		table.setHeight(row, Table.HUNDRED_PERCENT);
		table.setRowVerticalAlignment(row, Table.VERTICAL_ALIGN_BOTTOM);
		table.setWidth(1, "100");

		return table;
	}
	
	private Table getAddStudentForm(IWContext iwc) {
		Table table = new Table();
		table.setCellpadding(5);
		table.setWidth(Table.HUNDRED_PERCENT);
		table.setHeight(Table.HUNDRED_PERCENT);
		table.add(new HiddenInput(PARAMETER_METHOD, String.valueOf(METHOD_ADD_STUDENT)));
		int row = 1;

		table.add(getSmallHeader(localize("commune.enter_search_string","Enter search string")+":"), 1, row);
		
		TextInput searchInput = (TextInput) getStyledInterface(new TextInput(PARAMETER_SEARCH));
		searchInput.setLength(40);
		searchInput.setMininumLength(6, localize("commune.search_string_too_short","Search string must be at least six characters."));
		searchInput.keepStatusOnAction(true);
		table.add(searchInput, 2, row++);
		
		SubmitButton searchButton = (SubmitButton) this.getButton(new SubmitButton(localize("search","Search")));
		table.add(searchButton, 1, row++);
		
		if (searchString != null) {
			try {
				Collection users = getUserBusiness(iwc).getUserHome().findUsersBySearchCondition(searchString);
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
				
					userTable.add(getSmallHeader(localize("commune.found_users","Found users")+":"), 1, userRow++);
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
							userTable.add(Text.NON_BREAKING_SPACE, 1, userRow);
							userTable.add(getSmallText(user.getNameLastFirst(true)), 1, userRow);
							userTable.add(getSmallText(" ("), 1, userRow);
							userTable.add(getSmallText(PersonalIDFormatter.format(user.getPersonalID(), iwc.getCurrentLocale())+")"), 1, userRow++);
						}
					}
				
					userTable.setHeight(userRow++, 6);
					if (addSubmit) {
						SubmitButton addButton = (SubmitButton) getButton(new SubmitButton(localize("school.add_student","Add student"), PARAMETER_ACTION, String.valueOf(ACTION_ADD_STUDENT)));
						addButton.setValueOnClick(PARAMETER_METHOD, "-1");
						addButton.setToEnableWhenSelected(PARAMETER_USER_ID);
						userTable.add(addButton, 1, userRow);
					}
					else {
						userTable.add(getSmallHeader(localize("school.no_student_found","No student found")), 1, userRow++);
					}
				}
				else {
					table.add(getSmallHeader(localize("school.no_student_found","No student found")), 1, row++);
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

	protected Table getNavigationTable(IWContext iwc, String heading) throws RemoteException {
		Table table = new Table(7, 1);
		table.setCellpadding(0);
		table.setCellspacing(0);
		table.setWidth(2, "8");
		table.setWidth(5, "8");

		table.add(getSmallHeader(heading), 1, 1);
		table.add(getSmallHeader(localize("school.year", "Year") + ":" + Text.NON_BREAKING_SPACE), 3, 1);
		table.add(getSchoolYears(iwc), 4, 1);
		table.add(getSmallHeader(localize("school.class", "Class") + ":" + Text.NON_BREAKING_SPACE), 6, 1);
		table.add(getSchoolClasses(iwc), 7, 1);

		return table;
	}

	private void reject(IWContext iwc) throws RemoteException {
		String messageHeader = localize("school.reject_message_header", "School choice rejected.");
		String messageBody = iwc.getParameter(PARAMETER_REJECT_MESSAGE);
		getSchoolCommuneBusiness(iwc).getSchoolChoiceBusiness().rejectApplication(_choiceID, getSchoolCommuneSession(iwc).getSchoolSeasonID(), iwc.getCurrentUser(), messageHeader, messageBody);

		getParentPage().setParentToReload();
		getParentPage().close();
	}

	private void replace(IWContext iwc) throws RemoteException {
		if (iwc.isParameterSet(PARAMETER_DATE)) {
			String message = iwc.isParameterSet(PARAMETER_REPLACE_MESSAGE) ? iwc.getParameter(PARAMETER_REPLACE_MESSAGE) : "";
			String date = iwc.getParameter(PARAMETER_DATE);

			IWTimestamp stamp = new IWTimestamp(date);
			SchoolClassMember member = getSchoolCommuneBusiness(iwc).getSchoolBusiness().storeSchoolClassMember(_userID, _schoolClassID, stamp.getTimestamp(), ((Integer) iwc.getCurrentUser().getPrimaryKey()).intValue(), message);
			getSchoolCommuneBusiness(iwc).setStudentAsSpeciallyPlaced(member);
			getSchoolCommuneBusiness(iwc).setNeedsSpecialAttention(member, getSchoolCommuneBusiness(iwc).getPreviousSchoolSeason(getSchoolCommuneSession(iwc).getSchoolSeasonID()), true);
			if (_choiceID != -1)
				getSchoolCommuneBusiness(iwc).getSchoolChoiceBusiness().groupPlaceAction(new Integer(_choiceID), iwc.getCurrentUser());

			getParentPage().setParentToReload();
			getParentPage().close();
		}
		else {
			_protocol = false;
			_method = this.METHOD_REPLACE;
		}
	}

	private void move(IWContext iwc) throws RemoteException {
		if (iwc.isParameterSet(PARAMETER_MOVE_MESSAGE)) {
			String message = iwc.getParameter(PARAMETER_MOVE_MESSAGE);
			String schoolName = localize("school.outside_commune","a school outside of the commune");

			int schoolID = Integer.parseInt(iwc.getParameter(PARAMETER_SCHOOL_ID));
			int schoolTypeID = -1;
			if (schoolID != -1) {
				try {
					School school = getSchoolCommuneBusiness(iwc).getSchoolBusiness().getSchool(new Integer(schoolID));
					schoolName = school.getSchoolName();
					Collection Stypes = school.findRelatedSchoolTypes();
					if (!Stypes.isEmpty()) {
						SchoolType schoolType = (SchoolType) Stypes.iterator().next();
						schoolTypeID = ((Integer) schoolType.getPrimaryKey()).intValue();
					}
				}
				catch (Exception e) {
					schoolTypeID = -1;
				}
			}

			SchoolYear year = getSchoolCommuneBusiness(iwc).getSchoolBusiness().getSchoolYear(new Integer(getSchoolCommuneSession(iwc).getSchoolYearID()));
			int grade = getSchoolCommuneBusiness(iwc).getGradeForYear(getSchoolCommuneSession(iwc).getSchoolYearID()) - 1;
			User student = getUserBusiness(iwc).getUser(_userID);
			Address studentAddress = getUserBusiness(iwc).getUserAddress1(_userID);
			getSchoolCommuneBusiness(iwc).setNeedsSpecialAttention(_userID, getSchoolCommuneBusiness(iwc).getPreviousSchoolSeasonID(getSchoolCommuneSession(iwc).getSchoolSeasonID()), true);

			//if (schoolID != -1) {
				try {
					User headmaster = getSchoolCommuneBusiness(iwc).getSchoolBusiness().getHeadmaster(schoolID);
					//if (headmaster != null) {
					String address = "";
					if (studentAddress != null)
						address = studentAddress.getStreetAddress();
					Object[] arguments = { student.getNameLastFirst(true), PersonalIDFormatter.format(student.getPersonalID(), iwc.getCurrentLocale()), address, schoolName };
					String messageSubject = localize("school.student_moved", "Student moved to your school");
					String messageBody = localize("school.student_moved_body", "The following student has been moved to your school and will need to be handled accordingly: ");
					getSchoolCommuneBusiness(iwc).getSchoolChoiceBusiness().getMessageBusiness().sendMessageToCommuneAdministrators(MessageFormat.format(messageSubject, arguments), MessageFormat.format(messageBody, arguments));
					if (headmaster != null)
						getSchoolCommuneBusiness(iwc).getSchoolChoiceBusiness().getMessageBusiness().createUserMessage(headmaster,MessageFormat.format(messageSubject, arguments), MessageFormat.format(messageBody, arguments));
				}
				catch (Exception e) {
				}
			//}

			IWTimestamp stamp = new IWTimestamp();
			try {
				getSchoolCommuneBusiness(iwc).getSchoolChoiceBusiness().createSchoolChoice(((Integer) iwc.getCurrentUser().getPrimaryKey()).intValue(), _userID, schoolTypeID, getSchoolCommuneSession(iwc).getSchoolID(), schoolID, grade, 1, 2, 1, 1, "", message, stamp.getTimestampRightNow(), true, false, false, true, false, getSchoolCommuneBusiness(iwc).getCaseStatus("FLYT"), null);
			}
			catch (Exception e) {
				e.printStackTrace();
			}

			getParentPage().setParentToReload();
			getParentPage().close();
		}
		else {
			_move = false;
			_method = this.METHOD_MOVE;
		}
	}

	private void moveGroup(IWContext iwc) throws RemoteException {
		getSchoolCommuneBusiness(iwc).moveToGroup(_userID, _schoolClassID, getSchoolCommuneSession(iwc).getSchoolClassID());
		getParentPage().setParentToReload();
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

		getParentPage().setParentToReload();
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

		getParentPage().setParentToReload();
		getParentPage().close();
	}
	
	private void addStudent(IWContext iwc) throws RemoteException {
		if (_userID != -1) {
			getSchoolCommuneBusiness(iwc).getSchoolBusiness().storeSchoolClassMember(_userID, getSchoolCommuneSession(iwc).getSchoolClassID(), new IWTimestamp().getTimestamp(), ((Integer)iwc.getCurrentUser().getPrimaryKey()).intValue());
			getParentPage().setParentToReload();
			getParentPage().close();
		}
		else
			_method = METHOD_ADD_STUDENT;
	}
	
	private void createStudent(IWContext iwc) throws RemoteException {
		try {
			User user = getUserBusiness(iwc).createSpecialCitizenByPersonalIDIfDoesNotExist(searchString,null,null,searchString);
			_userID = ((Integer) user.getPrimaryKey()).intValue();

			getSchoolCommuneBusiness(iwc).getSchoolBusiness().storeSchoolClassMember(_userID, getSchoolCommuneSession(iwc).getSchoolClassID(), new IWTimestamp().getTimestamp(), ((Integer)iwc.getCurrentUser().getPrimaryKey()).intValue());
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

		if (_schoolClassID != -1 && _schoolYearID != -1)
			validateSchoolClass(iwc);

		if (iwc.isParameterSet(PARAMETER_SEARCH))
			searchString = iwc.getParameter(PARAMETER_SEARCH);
		if (searchString != null && searchString.length() > 0) {
			try {
				String temp = searchString;
				temp = TextSoap.findAndCut(temp, "-");
				Long.parseLong(temp);
				if (temp.length() == 10 ) {
					int firstTwo = Integer.parseInt(temp.substring(0, 2));
					if (firstTwo < 04) {
						temp = "20"+temp;
					}	else {
						temp = "19"+temp;
					}
				}
				searchString = temp;
			}
			catch (NumberFormatException nfe) {}
		}
			
		_schoolID = getSchoolCommuneSession(iwc).getSchoolID();

		/** @todo LAGA... er ekki alveg rett */
	}

	protected DropdownMenu getSchoolYears(IWContext iwc) throws RemoteException {
		DropdownMenu menu = new DropdownMenu(getSchoolCommuneSession(iwc).getParameterSchoolYearID());
		menu.setToSubmit();

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

	protected DropdownMenu getSchoolClasses(IWContext iwc) throws RemoteException {
		DropdownMenu menu = new DropdownMenu(getSchoolCommuneSession(iwc).getParameterSchoolClassID());
		menu.setToSubmit();

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
	}

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

	private void validateSchoolClass(IWContext iwc) throws RemoteException {
		SchoolClass schoolClass = getSchoolCommuneBusiness(iwc).getSchoolBusiness().findSchoolClass(new Integer(_schoolClassID));
		if (schoolClass.getSchoolYearId() != _schoolYearID) {
			Collection schoolClasses = getSchoolCommuneBusiness(iwc).getSchoolBusiness().findSchoolClassesBySchoolAndSeasonAndYear(getSchoolCommuneSession(iwc).getSchoolID(), getSchoolCommuneSession(iwc).getSchoolSeasonID(), _schoolYearID);
			if (!schoolClasses.isEmpty()) {
				Iterator iter = schoolClasses.iterator();
				while (iter.hasNext()) {
					_schoolClassID = ((Integer) ((SchoolClass) iter.next()).getPrimaryKey()).intValue();
					continue;
				}
			}
		}
	}

	private SchoolCommuneBusiness getSchoolCommuneBusiness(IWContext iwc) throws RemoteException {
		return (SchoolCommuneBusiness) IBOLookup.getServiceInstance(iwc, SchoolCommuneBusiness.class);
	}

	private CommuneUserBusiness getCommuneUserBusiness(IWContext iwc) throws RemoteException {
		return (CommuneUserBusiness) IBOLookup.getServiceInstance(iwc, CommuneUserBusiness.class);
	}

	private SchoolCommuneSession getSchoolCommuneSession(IWContext iwc) throws RemoteException {
		return (SchoolCommuneSession) IBOLookup.getSessionInstance(iwc, SchoolCommuneSession.class);
	}

	private CommuneUserBusiness getUserBusiness(IWContext iwc) throws RemoteException {
		return (CommuneUserBusiness) IBOLookup.getServiceInstance(iwc, CommuneUserBusiness.class);
	}

	private MemberFamilyLogic getMemberFamilyLogic(IWContext iwc) throws RemoteException {
		return (MemberFamilyLogic) com.idega.business.IBOLookup.getServiceInstance(iwc, MemberFamilyLogic.class);
	}
}