package se.idega.idegaweb.commune.school.presentation;

import java.rmi.RemoteException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.ejb.FinderException;

import se.idega.idegaweb.commune.school.business.SchoolClassWriter;
import se.idega.idegaweb.commune.school.business.SchoolChoiceComparator;
import se.idega.idegaweb.commune.school.business.SchoolClassMemberComparator;
import se.idega.idegaweb.commune.school.data.SchoolChoice;
import se.idega.idegaweb.commune.school.data.SchoolChoiceBMPBean;
import se.idega.idegaweb.commune.school.event.SchoolEventListener;
import se.idega.util.PIDChecker;

import com.idega.block.school.data.School;
import com.idega.block.school.data.SchoolClass;
import com.idega.block.school.data.SchoolClassMember;
import com.idega.block.school.data.SchoolSeason;
import com.idega.block.school.data.SchoolYear;
import com.idega.business.IBOLookup;
import com.idega.core.data.Address;
import com.idega.idegaweb.IWMainApplication;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.Table;
import com.idega.presentation.text.Break;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.CheckBox;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.GenericButton;
import com.idega.presentation.ui.HiddenInput;
import com.idega.presentation.ui.Parameter;
import com.idega.presentation.ui.SubmitButton;
import com.idega.presentation.ui.TextInput;
import com.idega.presentation.ui.Window;
import com.idega.user.business.UserBusiness;
import com.idega.user.data.User;
import com.idega.util.GenericUserComparator;
import com.idega.util.IWCalendar;
import com.idega.util.IWTimestamp;
import com.idega.util.PersonalIDFormatter;

/**
 * @author Laddi
 */
public class SchoolClassEditor extends SchoolCommuneBlock {

	private final String PARAMETER_ACTION = "sch_action";
	private final String PARAMETER_METHOD = "sch_method";
	private final String PARAMETER_APPLICANT_ID = "sch_applicant_id";
	private final String PARAMETER_PREVIOUS_CLASS_ID = "sch_prev_class_id";
	private final String PARAMETER_SORT = "sch_choice_sort";
	private final String PARAMETER_SEARCH = "scH_choise_search";

	private final String PARAMETER_CURRENT_APPLICATION_PAGE = "sch_crrap_pg";

	private final int ACTION_MANAGE = 1;
	private final int ACTION_SAVE = 2;
	private final int ACTION_FINALIZE_GROUP = 3;
	private final int ACTION_DELETE = 4;

	private Map students;

	private int action = 0;
	private int method = 0;
	private int sortStudentsBy = SchoolChoiceComparator.NAME_SORT;
	private int sortChoicesBy = SchoolClassMemberComparator.NAME_SORT;
	private String searchString = "";
	private int _previousSchoolClassID = -1;
	private int _previousSchoolSeasonID = -1;
	private int _previousSchoolYearID = -1;

	private boolean multibleSchools = false;
	private boolean showStudentTable = true;
	private boolean searchEnabled = true;
	private int applicationsPerPage = 10;
	private boolean showStatistics = true;

	public void init(IWContext iwc) throws RemoteException {
		if (iwc.isLoggedOn()) {
			parseAction(iwc);

			switch (method) {
				case ACTION_SAVE :
					saveClass(iwc);
					break;
				case ACTION_DELETE :
					delete(iwc);
					break;
				case ACTION_FINALIZE_GROUP :
					finalize(iwc);
					break;
			}

			switch (action) {
				case ACTION_MANAGE :
					drawForm(iwc);
					break;
				case ACTION_SAVE :
					drawNewGroupForm(iwc);
					break;
			}
		}
		else {
			add(super.getSmallHeader(localize("not_logged_on", "Not logged on")));
		}
	}

	private void parseAction(IWContext iwc) throws RemoteException {
		if (iwc.isParameterSet(PARAMETER_PREVIOUS_CLASS_ID))
			_previousSchoolClassID = Integer.parseInt(iwc.getParameter(PARAMETER_PREVIOUS_CLASS_ID));

		_previousSchoolSeasonID = getBusiness().getPreviousSchoolSeasonID(getSchoolSeasonID());
		_previousSchoolYearID = getBusiness().getPreviousSchoolYear(getSchoolYearID());

		if (iwc.isParameterSet(PARAMETER_ACTION))
			action = Integer.parseInt(iwc.getParameter(PARAMETER_ACTION));
		else
			action = ACTION_MANAGE;

		if (iwc.isParameterSet(PARAMETER_METHOD))
			method = Integer.parseInt(iwc.getParameter(PARAMETER_METHOD));
		else
			method = 0;

		if (iwc.isParameterSet(PARAMETER_SORT))
			sortChoicesBy = Integer.parseInt(iwc.getParameter(PARAMETER_SORT));
		else
			sortChoicesBy = SchoolChoiceComparator.NAME_SORT;
		sortStudentsBy = sortChoicesBy;

		if (iwc.isParameterSet(PARAMETER_SEARCH))
			searchString = iwc.getParameter(PARAMETER_SEARCH);

	}

	private void drawForm(IWContext iwc) throws RemoteException {
		Form form = new Form();
		form.setEventListener(SchoolEventListener.class);
		form.add(new HiddenInput(PARAMETER_ACTION, String.valueOf(action)));

		Table table = new Table(1, 11);
		table.setCellpadding(0);
		table.setCellspacing(0);
		table.setWidth(getWidth());
		table.setHeight(2, "12");
		table.setHeight(4, "6");
		table.setHeight(6, "12");
		table.setHeight(8, "6");
		table.setHeight(10, "12");
		form.add(table);

		Table headerTable = new Table(2, 1);
		headerTable.setWidth(Table.HUNDRED_PERCENT);
		headerTable.setCellpaddingAndCellspacing(0);
		headerTable.setAlignment(2, 1, Table.HORIZONTAL_ALIGN_RIGHT);
		table.add(headerTable, 1, 1);

		headerTable.add(getNavigationTable(true, multibleSchools), 1, 1);
		headerTable.add(getSearchAndSortTable(), 2, 1);
		headerTable.setVerticalAlignment(2, 1, Table.VERTICAL_ALIGN_BOTTOM);

		students = getBusiness().getStudentList(getBusiness().getSchoolBusiness().findStudentsBySchoolAndSeason(getSchoolID(), getSchoolSeasonID()));

		table.add(getApplicationTable(iwc), 1, 5);
		table.add(getChoiceHeader(), 1, 3);

		if (this.showStudentTable) {
			if (_previousSchoolYearID != -1) {
				try {
					Collection previousClasses = getBusiness().getPreviousSchoolClasses(getBusiness().getSchoolBusiness().getSchool(new Integer(getSchoolID())), getBusiness().getSchoolBusiness().getSchoolSeason(new Integer(getSchoolSeasonID())), getBusiness().getSchoolBusiness().getSchoolYear(new Integer(getSchoolYearID())));
					validateSchoolClass(previousClasses);

					table.add(getPreviousHeader(previousClasses), 1, 7);
					table.add(getStudentTable(iwc), 1, 9);
				}
				catch (NullPointerException ne) {
				}
			}

			if (getSchoolClassID() != -1) {
				HiddenInput method = new HiddenInput(PARAMETER_METHOD, "0");
				SubmitButton submit = (SubmitButton) getStyledInterface(new SubmitButton(localize("save", "Save")));
				submit.setValueOnClick(PARAMETER_METHOD, String.valueOf(ACTION_SAVE));
				submit.setValueOnClick(PARAMETER_ACTION, String.valueOf(ACTION_SAVE));
				SubmitButton view = (SubmitButton) getStyledInterface(new SubmitButton(localize("school.view_group", "View group")));
				view.setValueOnClick(PARAMETER_ACTION, String.valueOf(ACTION_SAVE));
				table.add(method, 1, 11);
				table.add(submit, 1, 11);
				table.add(Text.NON_BREAKING_SPACE, 1, 11);
				table.add(view, 1, 11);
			}
		}

		add(form);
	}

	private void drawNewGroupForm(IWContext iwc) throws RemoteException {
		Form form = new Form();
		form.setEventListener(SchoolEventListener.class);
		form.add(new HiddenInput(PARAMETER_ACTION, String.valueOf(action)));

		Table table = new Table();
		table.setCellpadding(0);
		table.setCellspacing(0);
		table.setWidth(getWidth());
		form.add(table);
		int row = 1;

		Table headerTable = new Table(2, 1);
		headerTable.setWidth(Table.HUNDRED_PERCENT);
		headerTable.setCellpaddingAndCellspacing(0);
		headerTable.setAlignment(2, 1, Table.HORIZONTAL_ALIGN_RIGHT);
		table.add(headerTable, 1, row++);
		table.setHeight(row++, "12");

		headerTable.add(getNavigationTable(true, multibleSchools), 1, 1);
		headerTable.add(getSearchAndSortTable(), 2, 1);

		if (getSchoolClassID() != -1) {
			table.setAlignment(1, row, Table.HORIZONTAL_ALIGN_RIGHT);
			table.add(getPDFLink(getBundle().getImage("shared/pdf.gif")), 1, row);
			table.add(Text.NON_BREAKING_SPACE, 1, row);
			table.add(getXLSLink(getBundle().getImage("shared/xls.gif")), 1, row++);
		}

		table.add(getNewStudentTable(iwc), 1, row);

		add(form);
	}

	private Table getApplicationTable(IWContext iwc) throws RemoteException {
		Table table = new Table();
		table.setWidth(getWidth());
		table.setCellpadding(getCellpadding());
		table.setCellspacing(getCellspacing());
		boolean showLanguage = false;

		SchoolYear year = getBusiness().getSchoolBusiness().getSchoolYear(new Integer(getSchoolYearID()));
		int schoolYearAge = getBusiness().getGradeForYear(getSchoolYearID()) - 1;
		if (year != null && schoolYearAge >= 12)
			showLanguage = true;

		if (!showStudentTable) {
			table.setColumns(table.getColumns() - 1);
		}

		String[] validStatuses = new String[] { SchoolChoiceBMPBean.CASE_STATUS_PRELIMINARY, SchoolChoiceBMPBean.CASE_STATUS_MOVED };
		//if (choice.getStatus().equalsIgnoreCase("PREL") || choice.getStatus().equalsIgnoreCase("FLYT")) {

		List applicants = new ArrayList(getBusiness().getSchoolChoiceBusiness().getApplicantsForSchool(getSchoolID(), getSchoolSeasonID(), schoolYearAge, validStatuses, searchString));
		int applicantsSize = applicants.size();

		int row = 1;
		int column = 1;
		int headerRow = -1;

		int currPage = 0;
		int maxPage = (int) Math.ceil(applicantsSize / applicationsPerPage);
		if (iwc.isParameterSet(PARAMETER_CURRENT_APPLICATION_PAGE)) {
			currPage = Integer.parseInt(iwc.getParameter(PARAMETER_CURRENT_APPLICATION_PAGE));
		}

		Table navigationTable = new Table(3, 1);
		navigationTable.setCellpadding(0);
		navigationTable.setCellspacing(0);
		navigationTable.setWidth(Table.HUNDRED_PERCENT);
		navigationTable.setWidth(1, "33%");
		navigationTable.setWidth(2, "33%");
		navigationTable.setWidth(3, "33%");
		navigationTable.setAlignment(2, 1, Table.HORIZONTAL_ALIGN_CENTER);
		navigationTable.setAlignment(3, 1, Table.HORIZONTAL_ALIGN_RIGHT);
		table.add(navigationTable, 1, row++);

		Text prev = getSmallText(localize("previous", "Previous"));
		Text next = getSmallText(localize("next", "Next"));
		Text info = getSmallText(localize("page", "Page") + " " + (currPage + 1) + " " + localize("of", "of") + " " + (maxPage + 1));
		if (currPage > 0) {
			Link lPrev = getSmallLink(localize("previous", "Previous"));
			lPrev.addParameter(PARAMETER_CURRENT_APPLICATION_PAGE, Integer.toString(currPage - 1));
			lPrev.addParameter(PARAMETER_SEARCH, iwc.getParameter(PARAMETER_SEARCH));
			lPrev.addParameter(PARAMETER_SORT, iwc.getParameter(PARAMETER_SORT));
			navigationTable.add(lPrev, 1, 1);
		}
		else {
			navigationTable.add(prev, 1, 1);
		}
		navigationTable.add(info, 2, 1);

		if (currPage < maxPage) {
			Link lNext = getSmallLink(localize("next", "Next"));
			lNext.addParameter(PARAMETER_CURRENT_APPLICATION_PAGE, Integer.toString(currPage + 1));
			lNext.addParameter(PARAMETER_SEARCH, iwc.getParameter(PARAMETER_SEARCH));
			lNext.addParameter(PARAMETER_SORT, iwc.getParameter(PARAMETER_SORT));
			navigationTable.add(lNext, 3, 1);
		}
		else {
			navigationTable.add(next, 3, 1);
		}

		headerRow = row;
		table.add(getSmallHeader(localize("school.name", "Name")), column++, row);
		table.add(getSmallHeader(localize("school.personal_id", "Personal ID")), column++, row);
		table.add(getSmallHeader(localize("school.gender", "Gender")), column++, row);
		table.add(getSmallHeader(localize("school.from_school", "From School")), column++, row);
		if (showLanguage)
			table.add(getSmallHeader(localize("school.language", "Language")), column++, row);
		table.add(getSmallHeader(localize("school.date", "Date")), column, row++);

		CheckBox checkBox = new CheckBox();
		Link link;

		if (!applicants.isEmpty()) {
			Map studentMap = getBusiness().getUserMapFromChoices(applicants);
			Collections.sort(applicants, new SchoolChoiceComparator(sortChoicesBy, iwc.getCurrentLocale(), getUserBusiness(iwc), studentMap));
			SchoolChoice choice;
			School school;
			User applicant;
			IWCalendar calendar;

			Iterator iter = applicants.iterator();

			/** Calculating page....starts */
			int start = currPage * applicationsPerPage;
			int end = start + applicationsPerPage;
			for (int i = 0; i < start; i++) {
				if (iter.hasNext()) {
					iter.next();
				}
			}
			/** Calculating page....ends */

			int counter = 0;
			while (iter.hasNext() && counter < applicationsPerPage) {
				++counter;
				column = 1;
				choice = (SchoolChoice) iter.next();
				applicant = (User) studentMap.get(new Integer(choice.getChildId()));
				school = getBusiness().getSchoolBusiness().getSchool(new Integer(choice.getCurrentSchoolId()));
				checkBox = getCheckBox(PARAMETER_APPLICANT_ID, String.valueOf(choice.getChildId()) + "," + choice.getPrimaryKey().toString());
				calendar = new IWCalendar(iwc.getCurrentLocale(), choice.getCreated());
				if (students.containsValue(applicant))
					checkBox.setDisabled(true);

				String name = applicant.getNameLastFirst(true);
				if (iwc.getCurrentLocale().getLanguage().equalsIgnoreCase("is"))
					name = applicant.getName();

				if (choice.getChoiceOrder() > 1 || choice.getStatus().equalsIgnoreCase(SchoolChoiceBMPBean.CASE_STATUS_MOVED))
					table.setRowColor(row, "#FF3333");
				else {
					if (row % 2 == 0)
						table.setRowColor(row, getZebraColor1());
					else
						table.setRowColor(row, getZebraColor2());
				}

				link = (Link) this.getSmallLink(name);
				link.setWindowToOpen(SchoolAdminWindow.class);
				link.setParameter(SchoolAdminOverview.PARAMETER_METHOD, String.valueOf(SchoolAdminOverview.METHOD_OVERVIEW));
				link.setParameter(SchoolAdminOverview.PARAMETER_USER_ID, String.valueOf(choice.getChildId()));
				link.setParameter(SchoolAdminOverview.PARAMETER_CHOICE_ID, choice.getPrimaryKey().toString());

				table.add(link, column++, row);
				table.add(getSmallText(PersonalIDFormatter.format(applicant.getPersonalID(), iwc.getCurrentLocale())), column++, row);
				if (PIDChecker.getInstance().isFemale(applicant.getPersonalID()))
					table.add(getSmallText(localize("school.girl", "Girl")), column++, row);
				else
					table.add(getSmallText(localize("school.boy", "Boy")), column++, row);

				if (school != null) {
					String schoolName = school.getName();
					if (schoolName.length() > 20)
						schoolName = schoolName.substring(0, 20) + "...";
					table.add(getSmallText(schoolName), column, row);
					if (choice.getStatus().equalsIgnoreCase(SchoolChoiceBMPBean.CASE_STATUS_MOVED))
						table.add(getSmallText(" (" + localize("school.moved", "Moved") + ")"), column, row);
				}
				column++;
				if (showLanguage) {
					if (choice.getLanguageChoice() != null)
						table.add(getSmallText(localize(choice.getLanguageChoice(),"")), column, row);
					column++;
				}
				table.add(getSmallText(calendar.getLocaleDate(IWCalendar.SHORT)), column++, row);
				if (showStudentTable && getSchoolClassID() != -1) {
					table.setWidth(column, "12");
					table.add(checkBox, column, row++);
				}
				else {
					++row;
				}
			}
		}

		if (showStatistics) {
			List firstApplicants = new ArrayList(getBusiness().getSchoolChoiceBusiness().getApplicantsForSchool(getSchoolID(), getSchoolSeasonID(), -1, new int[] { 1 }, validStatuses, ""));
			int firstApplSize = firstApplicants.size();
			List secondApplicants = new ArrayList(getBusiness().getSchoolChoiceBusiness().getApplicantsForSchool(getSchoolID(), getSchoolSeasonID(), -1, new int[] { 2 }, validStatuses, ""));
			int secondApplSize = secondApplicants.size();
			List thirdApplicants = new ArrayList(getBusiness().getSchoolChoiceBusiness().getApplicantsForSchool(getSchoolID(), getSchoolSeasonID(), -1, new int[] { 3 }, validStatuses, ""));
			int thirdApplSize = thirdApplicants.size();
			List groupedApplicants = new ArrayList(getBusiness().getSchoolChoiceBusiness().getApplicantsForSchool(getSchoolID(), getSchoolSeasonID(), -1, null, new String[] { SchoolChoiceBMPBean.CASE_STATUS_GROUPED }, ""));
			int groupedApplSize = thirdApplicants.size();

			Table statTable = new Table();
			int sRow = 1;
			statTable.setCellpadding(1);
			statTable.setCellspacing(0);
			statTable.add(getSmallText(localize("filtered_applications", "Filtered applications") + ":"), 1, sRow);
			statTable.add(getSmallText("" + applicantsSize), 2, sRow);
			++sRow;
			statTable.add(getSmallText(localize("applications_on_first_choice", "Applcations on first choice") + ":"), 1, sRow);
			statTable.add(getSmallText("" + firstApplSize), 2, sRow);
			++sRow;
			statTable.add(getSmallText(localize("applications_on_second_choice", "Applcations on second choice") + ":"), 1, sRow);
			statTable.add(getSmallText("" + secondApplSize), 2, sRow);
			++sRow;
			statTable.add(getSmallText(localize("applications_on_third_choice", "Applcations on third choice") + ":"), 1, sRow);
			statTable.add(getSmallText("" + thirdApplSize), 2, sRow);
			++sRow;
			statTable.add(getSmallText(localize("grouped_applications", "Grouped applcations") + ":"), 1, sRow);
			statTable.add(getSmallText("" + groupedApplSize), 2, sRow);

			table.mergeCells(1, row, table.getColumns(), row);
			table.add(statTable, 1, row);
			++row;
		}

		if (showStudentTable && getSchoolClassID() != -1) {
			GenericButton selectAll = (GenericButton) getStyledInterface(new GenericButton());
			selectAll.setValue(localize("school.select_all", "Select all"));
			selectAll.setToCheckOnClick(checkBox, true, false);

			GenericButton deselectAll = (GenericButton) getStyledInterface(new GenericButton());
			deselectAll.setValue(localize("school.deselect_all", "Deselect all"));
			deselectAll.setToCheckOnClick(checkBox, false);

			table.add(selectAll, 1, row);
			table.add(Text.NON_BREAKING_SPACE, 1, row);
			table.add(deselectAll, 1, row);
			table.mergeCells(1, row, table.getColumns(), row);
			table.setAlignment(1, row, Table.HORIZONTAL_ALIGN_RIGHT);
			table.setRowColor(row, "#FFFFFF");
		}
		table.setColumnAlignment(3, Table.HORIZONTAL_ALIGN_CENTER);
		table.setRowColor(headerRow, getHeaderColor());
		table.mergeCells(1, 1, table.getColumns(), 1);

		return table;
	}

	private Table getStudentTable(IWContext iwc) throws RemoteException {
		Table table = new Table();
		table.setWidth(getWidth());
		table.setCellpadding(getCellpadding());
		table.setCellspacing(getCellspacing());
		SchoolYear schoolYear = getBusiness().getSchoolBusiness().getSchoolYear(new Integer(_previousSchoolYearID));
		int schoolAge = -1;
		if (schoolYear != null)
			schoolAge = schoolYear.getSchoolYearAge();

		int row = 1;
		table.add(getSmallHeader(localize("school.name", "Name")), 1, row);
		table.add(getSmallHeader(localize("school.personal_id", "Personal ID")), 2, row);
		table.add(getSmallHeader(localize("school.gender", "Gender")), 3, row);
		table.add(getSmallHeader(localize("school.address", "Address")), 4, row);
		if (schoolAge >= 12)
			table.add(getSmallHeader(localize("school.language", "Language")), 5, row);
		row++;

		User student;
		Address address;
		Link link;
		SchoolClassMember studentMember;
		CheckBox checkBox = new CheckBox();
		int numberOfStudents = 0;

		List formerStudents = new ArrayList();
		if (_previousSchoolClassID != -1)
			formerStudents = new ArrayList(getBusiness().getSchoolBusiness().findStudentsInClass(_previousSchoolClassID));
		else
			formerStudents = new ArrayList(getBusiness().getSchoolBusiness().findStudentsBySchoolAndSeasonAndYear(getSchoolID(), _previousSchoolSeasonID, _previousSchoolYearID));

		if (!formerStudents.isEmpty()) {
			numberOfStudents = formerStudents.size();
			Map studentMap = getBusiness().getStudentList(formerStudents);
			Map studentChoices = getBusiness().getStudentChoices(formerStudents, getSchoolSeasonID());
			Collections.sort(formerStudents, new SchoolClassMemberComparator(sortStudentsBy, iwc.getCurrentLocale(), getUserBusiness(iwc), studentMap));
			Iterator iter = formerStudents.iterator();
			int column = 1;
			while (iter.hasNext()) {
				column = 1;
				studentMember = (SchoolClassMember) iter.next();
				student = (User) studentMap.get(new Integer(studentMember.getClassMemberId()));
				address = getUserBusiness(iwc).getUserAddress1(((Integer) student.getPrimaryKey()).intValue());
				checkBox = getCheckBox(getSession().getParameterStudentID(), String.valueOf(((Integer) student.getPrimaryKey()).intValue()));
				if (students.containsValue(student))
					checkBox.setDisabled(true);

				String name = student.getNameLastFirst(true);
				if (iwc.getCurrentLocale().getLanguage().equalsIgnoreCase("is"))
					name = student.getName();

				link = (Link) this.getSmallLink(name);
				link.setWindowToOpen(SchoolAdminWindow.class);
				link.setParameter(SchoolAdminOverview.PARAMETER_METHOD, String.valueOf(SchoolAdminOverview.METHOD_OVERVIEW));
				link.setParameter(SchoolAdminOverview.PARAMETER_USER_ID, String.valueOf(studentMember.getClassMemberId()));

				if (studentMember.getNeedsSpecialAttention()) {
					checkBox.setDisabled(true);
					boolean[] hasChoices = getBusiness().hasSchoolChoices(studentMember.getClassMemberId(), getSchoolSeasonID());
					if (hasChoices[0] && hasChoices[1])
						table.setRowColor(row, "#FF3333");
					else if (hasChoices[0] && !hasChoices[1])
						table.setRowColor(row, "#33FF33");
					else
						table.setRowColor(row, "#3333FF");
					link.setParameter(SchoolAdminOverview.PARAMETER_CHOICE_ID, String.valueOf(getBusiness().getChosenSchoolID((Collection) studentChoices.get(new Integer(studentMember.getClassMemberId())))));
				}
				else {
					if (row % 2 == 0)
						table.setRowColor(row, getZebraColor1());
					else
						table.setRowColor(row, getZebraColor2());
				}

				table.add(link, column++, row);
				table.add(getSmallText(PersonalIDFormatter.format(student.getPersonalID(), iwc.getCurrentLocale())), column++, row);
				if (PIDChecker.getInstance().isFemale(student.getPersonalID()))
					table.add(getSmallText(localize("school.girl", "Girl")), column++, row);
				else
					table.add(getSmallText(localize("school.boy", "Boy")), column++, row);
				if (address != null && address.getStreetAddress() != null)
					table.add(getSmallText(address.getStreetAddress()), column, row);
				column++;
				if (schoolAge >= 12) {
					if (studentMember.getLanguage() != null)
						table.add(getSmallText(localize(studentMember.getLanguage(),"")), 5, row);
					column++;
				}
				if (getSchoolClassID() != -1) {
					table.setWidth(column, "12");
					table.add(checkBox, column, row);
				}
				row++;
			}
		}

		if (numberOfStudents > 0) {
			table.mergeCells(1, row, table.getColumns(), row);
			table.add(getSmallHeader(localize("school.number_of_students", "Number of students") + ": " + String.valueOf(numberOfStudents)), 1, row++);
		}

		if (getSchoolClassID() != -1) {
			GenericButton selectAll = (GenericButton) getStyledInterface(new GenericButton());
			selectAll.setValue(localize("school.select_all", "Select all"));
			selectAll.setToCheckOnClick(checkBox, true, false);
	
			GenericButton deselectAll = (GenericButton) getStyledInterface(new GenericButton());
			deselectAll.setValue(localize("school.deselect_all", "Deselect all"));
			deselectAll.setToCheckOnClick(checkBox, false);
	
			table.add(selectAll, 1, row);
			table.add(Text.NON_BREAKING_SPACE, 1, row);
			table.add(deselectAll, 1, row);
			table.mergeCells(1, row, 6, row);
			table.setAlignment(1, row, Table.HORIZONTAL_ALIGN_RIGHT);
			table.setRowColor(row, "#FFFFFF");
		}
		table.setColumnAlignment(3, Table.HORIZONTAL_ALIGN_CENTER);
		table.setRowColor(1, getHeaderColor());

		return table;
	}

	private Table getNewStudentTable(IWContext iwc) throws RemoteException {
		boolean isReady = false;
		SchoolClass newSchoolClass = getBusiness().getSchoolBusiness().findSchoolClass(new Integer(getSchoolClassID()));
		if (newSchoolClass != null)
			isReady = newSchoolClass.getReady();

		Table table = new Table();
		table.setWidth(getWidth());
		table.setCellpadding(getCellpadding());
		table.setCellspacing(getCellspacing());
		table.setColumns(7);
		table.setWidth(6, "12");
		table.setWidth(7, "12");
		int row = 1;

		table.add(getSmallHeader(localize("school.name", "Name")), 1, row);
		table.add(getSmallHeader(localize("school.personal_id", "Personal ID")), 2, row);
		table.add(getSmallHeader(localize("school.gender", "Gender")), 3, row);
		table.add(getSmallHeader(localize("school.address", "Address")), 4, row);
		table.add(getSmallHeader(localize("school.class", "Class")), 5, row);
		table.add(new HiddenInput(PARAMETER_APPLICANT_ID, "-1"), 6, row);
		table.add(new HiddenInput(PARAMETER_METHOD, "0"), 7, row++);

		User student;
		Address address;
		SchoolClassMember studentMember;
		SchoolClass schoolClass = null;
		SubmitButton delete;
		Link move;
		Link link;
		int numberOfStudents = 0;

		List formerStudents = new ArrayList(getBusiness().getSchoolBusiness().findStudentsInClass(getSchoolClassID()));

		if (!formerStudents.isEmpty()) {
			numberOfStudents = formerStudents.size();
			Map studentMap = getBusiness().getStudentList(formerStudents);
			Collections.sort(formerStudents, new SchoolClassMemberComparator(sortStudentsBy, iwc.getCurrentLocale(), getUserBusiness(iwc), studentMap));
			Iterator iter = formerStudents.iterator();
			while (iter.hasNext()) {
				studentMember = (SchoolClassMember) iter.next();
				student = (User) studentMap.get(new Integer(studentMember.getClassMemberId()));
				schoolClass = getBusiness().getSchoolBusiness().findSchoolClass(new Integer(studentMember.getSchoolClassId()));
				address = getUserBusiness(iwc).getUserAddress1(((Integer) student.getPrimaryKey()).intValue());
				delete = (SubmitButton) getStyledInterface(new SubmitButton(getDeleteIcon(localize("school.delete_from_group", "Click to remove student from group")),"delete_student_"+String.valueOf(new Integer(studentMember.getClassMemberId()))));
				delete.setDescription(localize("school.delete_from_group", "Click to remove student from group"));
				delete.setValueOnClick(PARAMETER_APPLICANT_ID, String.valueOf(studentMember.getClassMemberId()));
				delete.setValueOnClick(PARAMETER_METHOD, String.valueOf(ACTION_DELETE));
				delete.setSubmitConfirm(localize("school.confirm_student_delete","Are you sure you want to remove the student from this class?"));
				move = new Link(getEditIcon(localize("school.move_to_another_group", "Move this student to another group")));
				move.setWindowToOpen(SchoolAdminWindow.class);
				move.setParameter(SchoolAdminOverview.PARAMETER_METHOD, String.valueOf(SchoolAdminOverview.METHOD_MOVE_GROUP));
				move.setParameter(SchoolAdminOverview.PARAMETER_USER_ID, String.valueOf(studentMember.getClassMemberId()));

				String name = student.getNameLastFirst(true);
				if (iwc.getCurrentLocale().getLanguage().equalsIgnoreCase("is"))
					name = student.getName();

				if (row % 2 == 0)
					table.setRowColor(row, getZebraColor1());
				else
					table.setRowColor(row, getZebraColor2());

				link = (Link) this.getSmallLink(name);
				link.setWindowToOpen(SchoolAdminWindow.class);
				link.setParameter(SchoolAdminOverview.PARAMETER_METHOD, String.valueOf(SchoolAdminOverview.METHOD_OVERVIEW));
				link.setParameter(SchoolAdminOverview.PARAMETER_USER_ID, String.valueOf(studentMember.getClassMemberId()));
				link.setParameter(SchoolAdminOverview.PARAMETER_SHOW_ONLY_OVERVIEW, "true");
				table.add(link, 1, row);
				table.add(getSmallText(PersonalIDFormatter.format(student.getPersonalID(), iwc.getCurrentLocale())), 2, row);

				if (PIDChecker.getInstance().isFemale(student.getPersonalID()))
					table.add(getSmallText(localize("school.girl", "Girl")), 3, row);
				else
					table.add(getSmallText(localize("school.boy", "Boy")), 3, row);

				if (address != null && address.getStreetAddress() != null)
					table.add(getSmallText(address.getStreetAddress()), 4, row);
				if (schoolClass != null)
					table.add(getSmallText(schoolClass.getName()), 5, row);
				table.add(move, 6, row);
				table.add(delete, 7, row++);
			}
		}

		if (numberOfStudents > 0) {
			table.mergeCells(1, row, table.getColumns(), row);
			table.add(getSmallHeader(localize("school.number_of_students", "Number of students") + ": " + String.valueOf(numberOfStudents)), 1, row++);
		}

		SubmitButton back = (SubmitButton) getStyledInterface(new SubmitButton(localize("school.back", "Back")));
		back.setValueOnClick(PARAMETER_ACTION, String.valueOf(ACTION_MANAGE));

		String buttonLabel = "";
		if (isReady)
			buttonLabel = localize("school.class_locked", "Class locked");
		else
			buttonLabel = localize("school.class_ready", "Class ready");

		SubmitButton groupReady = (SubmitButton) getStyledInterface(new SubmitButton(buttonLabel));
		groupReady.setValueOnClick(PARAMETER_ACTION, String.valueOf(ACTION_MANAGE));
		groupReady.setValueOnClick(PARAMETER_METHOD, String.valueOf(ACTION_FINALIZE_GROUP));
		if (isReady) {
			groupReady.setSubmitConfirm(localize("school.confirm_group_locked", "Are you sure you want to set the group as locked and send out e-mails to all parents?"));
			if (!getBusiness().canMarkSchoolClass(newSchoolClass, "mark_locked_date"))
				groupReady.setDisabled(true);
		}
		else {
			groupReady.setSubmitConfirm(localize("school.confirm_group_ready", "Are you sure you want to set the group as ready and send out e-mails to all parents?"));
			if (!getBusiness().canMarkSchoolClass(newSchoolClass, "mark_ready_date"))
				groupReady.setDisabled(true);
		}

		table.add(back, 1, row);
		table.add(Text.NON_BREAKING_SPACE, 1, row);
		table.add(groupReady, 1, row);
		table.mergeCells(1, row, table.getColumns(), row);
		table.setAlignment(1, row, Table.HORIZONTAL_ALIGN_RIGHT);
		table.setColumnAlignment(3, Table.HORIZONTAL_ALIGN_CENTER);
		table.setRowColor(1, getHeaderColor());
		table.setRowColor(row, "#FFFFFF");

		return table;
	}

	protected Table getPreviousHeader(Collection classes) throws RemoteException {
		Table table = new Table(2, 1);
		table.setCellpadding(0);
		table.setCellspacing(0);
		table.setWidth(Table.HUNDRED_PERCENT);
		table.setAlignment(2, 1, Table.HORIZONTAL_ALIGN_RIGHT);

		table.add(getSmallHeader(localize("school.previous_year_class", "Previous year class")), 1, 1);
		table.add(getSmallHeader(localize("school.class", "Class") + ":" + Text.NON_BREAKING_SPACE), 2, 1);
		table.add(getPreviousSchoolClasses(classes), 2, 1);

		return table;
	}

	protected Table getChoiceHeader() throws RemoteException {
		Table table = new Table(2, 1);
		table.setCellpadding(0);
		table.setCellspacing(0);
		table.setWidth(Table.HUNDRED_PERCENT);
		table.setAlignment(2, 1, Table.HORIZONTAL_ALIGN_RIGHT);

		table.add(getSmallHeader(localize("school.school_choices_for_year", "School choices for selected year")), 1, 1);
		table.add(getSmallHeader(String.valueOf(getBusiness().getSchoolChoiceBusiness().getNumberOfApplications(getSchoolID(), getSchoolSeasonID(), getBusiness().getGradeForYear(getSchoolYearID()) - 1))), 2, 1);
		table.add(getSmallHeader(" / "), 2, 1);
		table.add(getSmallHeader(String.valueOf(getBusiness().getSchoolChoiceBusiness().getNumberOfApplications(getSchoolID(), getSchoolSeasonID()))), 2, 1);

		return table;
	}

	protected DropdownMenu getPreviousSchoolClasses(Collection classes) throws RemoteException {
		DropdownMenu menu = new DropdownMenu(PARAMETER_PREVIOUS_CLASS_ID);
		menu.setToSubmit();

		if (!classes.isEmpty()) {
			Iterator iter = classes.iterator();
			menu.addMenuElementFirst("-1", localize("school.all", "All"));
			while (iter.hasNext()) {
				SchoolClass element = (SchoolClass) iter.next();
				menu.addMenuElement(element.getPrimaryKey().toString(), element.getName());
			}
		}
		else {
			menu.addMenuElement(-1, "");
		}

		if (_previousSchoolClassID != -1)
			menu.setSelectedElement(_previousSchoolClassID);

		return (DropdownMenu) getStyledInterface(menu);
	}

	protected Table getSearchAndSortTable() throws RemoteException {
		Table table = new Table(2, 3);
		table.setCellpadding(0);
		table.setCellspacing(0);
		SchoolYear schoolYear = getBusiness().getSchoolBusiness().getSchoolYear(new Integer(getSchoolYearID()));
		int yearAge = -1;
		if (schoolYear != null)
			yearAge = schoolYear.getSchoolYearAge();

		if (searchEnabled) {
			table.add(getSmallHeader(localize("school.search_for", "Search for") + ":" + Text.NON_BREAKING_SPACE), 1, 1);

			TextInput tiSearch = (TextInput) getStyledInterface(new TextInput(PARAMETER_SEARCH, searchString));
			table.add(tiSearch, 2, 1);

			table.setHeight(2, "2");
		}

		table.add(getSmallHeader(localize("school.sort_by", "Sort by") + ":" + Text.NON_BREAKING_SPACE), 1, 3);

		DropdownMenu menu = (DropdownMenu) getStyledInterface(new DropdownMenu(PARAMETER_SORT));
		menu.addMenuElement(SchoolChoiceComparator.NAME_SORT, localize("school.sort_name", "Name"));
		menu.addMenuElement(SchoolChoiceComparator.PERSONAL_ID_SORT, localize("school.sort_personal_id", "Personal ID"));
		menu.addMenuElement(SchoolChoiceComparator.ADDRESS_SORT, localize("school.sort_address", "Address"));
		menu.addMenuElement(SchoolChoiceComparator.GENDER_SORT, localize("school.sort_gender", "Gender"));
		if (action != ACTION_SAVE && yearAge >= 12)
			menu.addMenuElement(SchoolChoiceComparator.LANGUAGE_SORT, localize("school.sort_language", "Language"));
		menu.setSelectedElement(sortChoicesBy);
		menu.setToSubmit();
		table.add(menu, 2, 3);

		table.setColumnAlignment(2, Table.HORIZONTAL_ALIGN_RIGHT);

		return table;
	}

	private void saveClass(IWContext iwc) throws RemoteException {
		String[] applications = iwc.getParameterValues(PARAMETER_APPLICANT_ID);
		String[] students = iwc.getParameterValues(getSession().getParameterStudentID());

		IWTimestamp stamp = new IWTimestamp();
		int userID = ((Integer) iwc.getCurrentUser().getPrimaryKey()).intValue();
		SchoolClassMember member;
		SchoolSeason previousSeason = getBusiness().getPreviousSchoolSeason(getSchoolSeasonID());

		if (applications != null && applications.length > 0) {
			for (int a = 0; a < applications.length; a++) {
				StringTokenizer tokens = new StringTokenizer(applications[a], ",");
				member = getBusiness().getSchoolBusiness().storeSchoolClassMember(Integer.parseInt(tokens.nextToken()), getSchoolClassID(), stamp.getTimestamp(), userID);
				getBusiness().getSchoolChoiceBusiness().groupPlaceAction(new Integer(tokens.nextToken()), iwc.getCurrentUser());
				if (member != null)
					getBusiness().importStudentInformationToNewClass(member, previousSeason);
			}
		}

		if (students != null && students.length > 0) {
			for (int a = 0; a < students.length; a++) {
				member = getBusiness().getSchoolBusiness().storeSchoolClassMember(Integer.parseInt(students[a]), getSchoolClassID(), stamp.getTimestamp(), userID);
				if (member != null)
					getBusiness().importStudentInformationToNewClass(member, previousSeason);
			}
		}
	}

	private void delete(IWContext iwc) throws RemoteException {
		String student = iwc.getParameter(PARAMETER_APPLICANT_ID);
		if (student != null && student.length() > 0) {
			getBusiness().getSchoolBusiness().removeSchoolClassMemberFromClass(Integer.parseInt(student),getSchoolClassID());
			SchoolChoice choice = getBusiness().getSchoolChoiceBusiness().findByStudentAndSchoolAndSeason(Integer.parseInt(student), getSchoolID(), getSchoolSeasonID());
			getBusiness().setNeedsSpecialAttention(Integer.parseInt(student), getBusiness().getPreviousSchoolSeasonID(getSchoolSeasonID()),false);
			if (choice != null)
				getBusiness().getSchoolChoiceBusiness().setAsPreliminary(choice, iwc.getCurrentUser());
		}
	}

	private void finalize(IWContext iwc) throws RemoteException {
		int schoolClassID = getSchoolClassID();
		SchoolClass schoolClass = getBusiness().getSchoolBusiness().findSchoolClass(new Integer(schoolClassID));
		if (schoolClass != null) {
			if (schoolClass.getReady()) {
				getBusiness().markSchoolClassLocked(schoolClass);
				getBusiness().finalizeGroup(schoolClass, localize("school.finalize_subject", ""), localize("school.finalize_body", ""));
			}
			else {
				getBusiness().markSchoolClassReady(schoolClass);
				getBusiness().finalizeGroup(schoolClass, localize("school.students_put_in_class_subject", ""), localize("school.students_put_in_class_body", ""));
			}
		}
	}

	private void validateSchoolClass(Collection previousClasses) throws RemoteException {
		SchoolClass previousClass = getBusiness().getSchoolBusiness().findSchoolClass(new Integer(_previousSchoolClassID));
		if (previousClass != null && !previousClasses.contains(previousClass))
			_previousSchoolClassID = -1;
	}

	private UserBusiness getUserBusiness(IWContext iwc) throws RemoteException {
		return (UserBusiness) IBOLookup.getServiceInstance(iwc, UserBusiness.class);
	}

	public Link getPDFLink(Image image) throws RemoteException {
		Link link = new Link(image);
		link.setWindow(getFileWindow());
		link.addParameter(SchoolClassWriter.prmClassId, getSchoolClassID());
		link.addParameter(SchoolClassWriter.prmPrintType, SchoolClassWriter.PDF);
		link.addParameter(SchoolClassWriter.PRM_WRITABLE_CLASS, IWMainApplication.getEncryptedClassName(SchoolClassWriter.class));
		return link;
	}

	public Link getXLSLink(Image image) throws RemoteException {
		Link link = new Link(image);
		link.setWindow(getFileWindow());
		link.addParameter(SchoolClassWriter.prmClassId, getSchoolClassID());
		link.addParameter(SchoolClassWriter.prmPrintType, SchoolClassWriter.XLS);
		link.addParameter(SchoolClassWriter.PRM_WRITABLE_CLASS, IWMainApplication.getEncryptedClassName(SchoolClassWriter.class));
		return link;
	}

	public Window getFileWindow() {
		Window w = new Window(localize("school.class", "School class"), getIWApplicationContext().getApplication().getMediaServletURI());
		w.setResizable(true);
		w.setMenubar(true);
		w.setHeight(400);
		w.setWidth(500);
		return w;
	}

	/** setters */
	public void setMultipleSchools(boolean multiple) {
		this.multibleSchools = multiple;
	}
	public void setShowStudentTable(boolean show) {
		this.showStudentTable = show;
	}
	public void setSearchEnabled(boolean searchEnabled) {
		this.searchEnabled = searchEnabled;
	}
	public void setShowStatistics(boolean show) {
		this.showStatistics = show;
	}
}