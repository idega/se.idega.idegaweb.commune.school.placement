package se.idega.idegaweb.commune.school.presentation;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.ejb.FinderException;

import se.idega.idegaweb.commune.school.business.SchoolChoiceComparator;
import se.idega.idegaweb.commune.school.business.SchoolClassMemberComparator;
import se.idega.idegaweb.commune.school.data.SchoolChoice;
import se.idega.idegaweb.commune.school.data.SchoolChoiceBMPBean;
import se.idega.idegaweb.commune.school.event.SchoolEventListener;
import se.idega.util.PIDChecker;

import com.idega.block.school.data.School;
import com.idega.block.school.data.SchoolClass;
import com.idega.block.school.data.SchoolClassMember;
import com.idega.block.school.data.SchoolYear;
import com.idega.business.IBOLookup;
import com.idega.core.data.Address;
import com.idega.presentation.IWContext;
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
	private final String PARAMETER_SORT ="sch_choice_sort";
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
		}else {
			add(super.getSmallHeader(localize("not_logged_on","Not logged on")));
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
			
		if (sortChoicesBy != SchoolChoiceComparator.LANGUAGE_SORT)
			sortStudentsBy = sortChoicesBy;
		else
			sortStudentsBy = SchoolClassMemberComparator.NAME_SORT;
			
		if (iwc.isParameterSet(PARAMETER_SEARCH))
			searchString = iwc.getParameter(PARAMETER_SEARCH);
			
	}

	private void drawForm(IWContext iwc) throws RemoteException {
		Form form = new Form();
		form.setEventListener(SchoolEventListener.class);
		form.add(new HiddenInput(PARAMETER_ACTION,String.valueOf(action)));

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

		Table headerTable = new Table(2,1);
		headerTable.setWidth(Table.HUNDRED_PERCENT);
		headerTable.setCellpaddingAndCellspacing(0);
		headerTable.setAlignment(2, 1, Table.HORIZONTAL_ALIGN_RIGHT);
		table.add(headerTable,1,1);
		
		headerTable.add(getNavigationTable(true, multibleSchools), 1, 1);
		headerTable.add(getSearchAndSortTable(), 2, 1);
		headerTable.setVerticalAlignment(2, 1, Table.VERTICAL_ALIGN_BOTTOM);

		students = getBusiness().getStudentList(getBusiness().getSchoolClassMemberBusiness().findStudentsBySchoolAndSeason(getSchoolID(), getSchoolSeasonID()));

		table.add(getApplicationTable(iwc), 1, 5);
		table.add(getChoiceHeader(), 1, 3);

		if (this.showStudentTable) {
			if (_previousSchoolYearID != -1) {
				try {
					Collection previousClasses = getBusiness().getPreviousSchoolClasses(getBusiness().getSchoolBusiness().getSchool(new Integer(getSchoolID())), getBusiness().getSchoolSeasonBusiness().getSchoolSeason(new Integer(getSchoolSeasonID())), getBusiness().getSchoolYearBusiness().getSchoolYear(new Integer(getSchoolYearID())));
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
				table.add(Text.NON_BREAKING_SPACE,1,11);
				table.add(view, 1, 11);
			}
		}

		add(form);
	}

	private void drawNewGroupForm(IWContext iwc) throws RemoteException {
		Form form = new Form();
		form.setEventListener(SchoolEventListener.class);
		form.add(new HiddenInput(PARAMETER_ACTION,String.valueOf(action)));

		Table table = new Table(1, 3);
		table.setCellpadding(0);
		table.setCellspacing(0);
		table.setWidth(getWidth());
		table.setHeight(2, "12");
		form.add(table);

		Table headerTable = new Table(2,1);
		headerTable.setWidth(Table.HUNDRED_PERCENT);
		headerTable.setCellpaddingAndCellspacing(0);
		headerTable.setAlignment(2, 1, Table.HORIZONTAL_ALIGN_RIGHT);
		table.add(headerTable,1,1);
		
		headerTable.add(getNavigationTable(true, multibleSchools), 1, 1);
		headerTable.add(getSearchAndSortTable(), 2, 1);

		table.add(getNewStudentTable(iwc), 1, 3);

		add(form);
	}
	
	private Table getApplicationTable(IWContext iwc) throws RemoteException {
		Table table = new Table();
		table.setWidth(getWidth());
		table.setCellpadding(getCellpadding());
		table.setCellspacing(getCellspacing());
		boolean showLanguage = false;

		SchoolYear year = getBusiness().getSchoolYearBusiness().getSchoolYear(new Integer(getSchoolYearID()));
		int schoolYearAge = getBusiness().getGradeForYear(getSession().getSchoolYearID()) - 1;
		if (year != null && schoolYearAge == 13)
			showLanguage = true;
			
			
		if (showLanguage) {
			table.setColumns(7);
			table.setWidth(7,"12");
		}
		else {
			table.setColumns(6);
			table.setWidth(6,"12");
		}

		if (!showStudentTable) {
			table.setColumns(table.getColumns()-1);
		}


		String[] validStatuses = new String[] {SchoolChoiceBMPBean.CASE_STATUS_PRELIMINARY, SchoolChoiceBMPBean.CASE_STATUS_MOVED};
		//if (choice.getStatus().equalsIgnoreCase("PREL") || choice.getStatus().equalsIgnoreCase("FLYT")) {

		List applicants = new Vector(getBusiness().getSchoolChoiceBusiness().getApplicantsForSchool(getSchoolID(), getSchoolSeasonID(), schoolYearAge, validStatuses, searchString));
		int applicantsSize = applicants.size();

		int row = 1;
		int column = 1;
		
		int currPage = 0;
		int maxPage = (int) Math.ceil( applicantsSize / applicationsPerPage );
		if (iwc.isParameterSet(PARAMETER_CURRENT_APPLICATION_PAGE)) {
			currPage = Integer.parseInt(iwc.getParameter(PARAMETER_CURRENT_APPLICATION_PAGE));
		}

		table.mergeCells(1, row, table.getColumns(), row);
		
		Table navigationTable = new Table(3,1);
		navigationTable.setCellpadding(0);
		navigationTable.setCellspacing(0);
		navigationTable.setWidth(Table.HUNDRED_PERCENT);
		navigationTable.setWidth(1, "33%");
		navigationTable.setWidth(2, "33%");
		navigationTable.setWidth(3, "33%");
		navigationTable.setAlignment(2, 1, Table.HORIZONTAL_ALIGN_CENTER);
		navigationTable.setAlignment(3, 1, Table.HORIZONTAL_ALIGN_RIGHT);
		table.add(navigationTable,1,row++);

		Text prev = getSmallText(localize("previous","Previous"));
		Text next = getSmallText(localize("next","Next"));
		Text info = getSmallText(localize("page","Page") +" "+ (currPage +1) +" "+localize("of", "of") +" "+ (maxPage+1));
		if (currPage > 0) {
			Link lPrev = getSmallLink(localize("previous","Previous"));
			lPrev.addParameter(PARAMETER_CURRENT_APPLICATION_PAGE, Integer.toString(currPage-1));
			lPrev.addParameter(PARAMETER_SEARCH, iwc.getParameter(PARAMETER_SEARCH));
			lPrev.addParameter(PARAMETER_SORT, iwc.getParameter(PARAMETER_SORT));
			navigationTable.add(lPrev, 1, 1);
		} else {
			navigationTable.add(prev, 1, 1);
		}
		navigationTable.add(info, 2, 1);
		
		if (currPage < maxPage) {
			Link lNext = getSmallLink(localize("next","Next"));
			lNext.addParameter(PARAMETER_CURRENT_APPLICATION_PAGE, Integer.toString(currPage+1));
			lNext.addParameter(PARAMETER_SEARCH, iwc.getParameter(PARAMETER_SEARCH));
			lNext.addParameter(PARAMETER_SORT, iwc.getParameter(PARAMETER_SORT));
			navigationTable.add(lNext, 3, 1);
		} else {
			navigationTable.add(next, 3, 1);
		}


		table.setRowColor(row, getHeaderColor());
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
				Collections.sort(applicants, new SchoolChoiceComparator(sortChoicesBy,iwc.getCurrentLocale(),getUserBusiness(iwc)));
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
					//if (choice.getStatus().equalsIgnoreCase("PREL") || choice.getStatus().equalsIgnoreCase("FLYT")) {
						applicant = getUserBusiness(iwc).getUser(choice.getChildId());
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
							table.add(getSmallText(localize("school.girl","Girl")),column++,row);
						else
							table.add(getSmallText(localize("school.boy","Boy")),column++,row);
						
						if (school != null) {
							String schoolName = school.getName();
							if (schoolName.length() > 20)
								schoolName = schoolName.substring(0, 20) + "...";
							table.add(getSmallText(schoolName), column, row);
							if (choice.getStatus().equalsIgnoreCase(SchoolChoiceBMPBean.CASE_STATUS_MOVED))
								table.add(getSmallText(" ("+localize("school.moved","Moved")+")"),column,row);
						}
						column++;
						if (showLanguage) {
							if (choice.getLanguageChoice() != null)
								table.add(getSmallText(choice.getLanguageChoice()), column, row);
							column++;
						}
						table.add(getSmallText(calendar.getLocaleDate(IWCalendar.SHORT)), column++, row);
						if (showStudentTable) {
							table.add(checkBox, column, row++);
						}else {
							++row;	
						}
					//}
				}
			}
		//}

		if (showStatistics) {
			System.out.println("[SchoolClassEditor] starting statistics  : "+IWTimestamp.RightNow().toString());
	
//			List totalApplicants = new Vector(getBusiness().getSchoolChoiceBusiness().getApplicantsForSchool(getSchoolID(), getSchoolSeasonID(), -1, null, ""));
//			int totalApplicantsSize = totalApplicants.size();
			List firstApplicants = new Vector(getBusiness().getSchoolChoiceBusiness().getApplicantsForSchool(getSchoolID(), getSchoolSeasonID(), -1, new int[]{1}, validStatuses, ""));
			int firstApplSize = firstApplicants.size();
			List secondApplicants = new Vector(getBusiness().getSchoolChoiceBusiness().getApplicantsForSchool(getSchoolID(), getSchoolSeasonID(), -1, new int[]{2}, validStatuses, ""));
			int secondApplSize = secondApplicants.size();
			List thirdApplicants = new Vector(getBusiness().getSchoolChoiceBusiness().getApplicantsForSchool(getSchoolID(), getSchoolSeasonID(), -1, new int[]{3}, validStatuses, ""));
			int thirdApplSize = thirdApplicants.size();
			List groupedApplicants = new Vector(getBusiness().getSchoolChoiceBusiness().getApplicantsForSchool(getSchoolID(), getSchoolSeasonID(), -1, null, new String[] {SchoolChoiceBMPBean.CASE_STATUS_GROUPED}, ""));
			int groupedApplSize = thirdApplicants.size();

			System.out.println("[SchoolClassEditor] done with statistics : "+IWTimestamp.RightNow().toString());
			
			
			Table statTable = new Table();
			int sRow = 1;
			statTable.setCellpaddingAndCellspacing(0);
//			statTable.add(getSmallText(localize("total_number_applications", "Total applications")+":"), 1, sRow);
//			statTable.add(getSmallText(""+totalApplicantsSize), 2, sRow);
//			if ( totalApplicantsSize != applicantsSize) {
				statTable.add(getSmallText(localize("filtered_applications", "Filtered applications")+":"), 1, sRow);
				statTable.add(getSmallText(""+applicantsSize), 2, sRow);
//			}
			++sRow;
			statTable.add(getSmallText(localize("applications_on_first_choice", "Applcations on first choice")+":"), 1, sRow);
			statTable.add(getSmallText(""+firstApplSize), 2, sRow);
			++sRow;
			statTable.add(getSmallText(localize("applications_on_second_choice", "Applcations on second choice")+":"), 1, sRow);
			statTable.add(getSmallText(""+secondApplSize), 2, sRow);
			++sRow;
			statTable.add(getSmallText(localize("applications_on_third_choice", "Applcations on third choice")+":"), 1, sRow);
			statTable.add(getSmallText(""+thirdApplSize), 2, sRow);
			++sRow;
			statTable.add(getSmallText(localize("grouped_applications", "Grouped applcations")+":"), 1, sRow);
			statTable.add(getSmallText(""+groupedApplSize), 2, sRow);
	
			table.mergeCells(1, row, table.getColumns(), row);
			table.add(statTable, 1, row);
			++row;
		}

		if (showStudentTable) {
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

		return table;
	}

	private Table getStudentTable(IWContext iwc) throws RemoteException {
		Table table = new Table();
		table.setWidth(getWidth());
		table.setCellpadding(getCellpadding());
		table.setCellspacing(getCellspacing());
		table.setColumns(6);
		table.setWidth(6,"12");

		int row = 1;
		table.add(getSmallHeader(localize("school.name", "Name")), 1, row);
		table.add(getSmallHeader(localize("school.personal_id", "Personal ID")), 2, row);
		table.add(getSmallHeader(localize("school.gender", "Gender")), 3, row);
		table.add(getSmallHeader(localize("school.address", "Address")), 4, row);
		table.add(getSmallHeader(localize("school.class", "Class")), 5, row++);

		User student;
		Address address;
		Link link;
		SchoolClassMember studentMember;
		SchoolClass schoolClass = null;
		CheckBox checkBox = new CheckBox();

		List formerStudents = new Vector();
		if (_previousSchoolClassID != -1)
			formerStudents = new Vector(getBusiness().getSchoolClassMemberBusiness().findStudentsInClass(_previousSchoolClassID));
		else
			formerStudents = new Vector(getBusiness().getSchoolClassMemberBusiness().findStudentsBySchoolAndSeasonAndYear(getSchoolID(), _previousSchoolSeasonID, _previousSchoolYearID));

		if (!formerStudents.isEmpty()) {
			Map studentMap = getBusiness().getStudentList(formerStudents);
			Map studentChoices = getBusiness().getStudentChoices(formerStudents, getSession().getSchoolSeasonID());
			Collections.sort(formerStudents, new SchoolClassMemberComparator(sortStudentsBy,iwc.getCurrentLocale(), getUserBusiness(iwc), studentMap));
			Iterator iter = formerStudents.iterator();
			while (iter.hasNext()) {
				studentMember = (SchoolClassMember) iter.next();
				student = (User) studentMap.get(new Integer(studentMember.getClassMemberId()));
				schoolClass = getBusiness().getSchoolClassBusiness().findSchoolClass(new Integer(studentMember.getSchoolClassId()));
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
				
				if (getBusiness().hasChosenOtherSchool((Collection)studentChoices.get(new Integer(studentMember.getClassMemberId())), getSession().getSchoolID())) {
					checkBox.setDisabled(true);
					table.setRowColor(row, "#FF3333");
					link.setParameter(SchoolAdminOverview.PARAMETER_CHOICE_ID, String.valueOf(getBusiness().getChosenSchoolID((Collection)studentChoices.get(new Integer(studentMember.getClassMemberId())))));
				}
				else {
					if (row % 2 == 0)
						table.setRowColor(row, getZebraColor1());
					else
						table.setRowColor(row, getZebraColor2());
				}

				table.add(link, 1, row);
				table.add(getSmallText(PersonalIDFormatter.format(student.getPersonalID(), iwc.getCurrentLocale())), 2, row);
				if (PIDChecker.getInstance().isFemale(student.getPersonalID()))
					table.add(getSmallText(localize("school.girl","Girl")),3,row);
				else
					table.add(getSmallText(localize("school.boy","Boy")),3,row);
				if (address != null && address.getStreetAddress() != null)
					table.add(getSmallText(address.getStreetAddress()), 4, row);
				if (schoolClass != null)
					table.add(getSmallText(schoolClass.getName()), 5, row);
				table.add(checkBox, 6, row++);
			}
		}

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
		table.setColumnAlignment(3, Table.HORIZONTAL_ALIGN_CENTER);
		table.setColumnAlignment(5, Table.HORIZONTAL_ALIGN_CENTER);
		table.setRowColor(1, getHeaderColor());
		table.setRowColor(row, "#FFFFFF");

		return table;
	}

	private Table getNewStudentTable(IWContext iwc) throws RemoteException {
		Table table = new Table();
		table.setWidth(getWidth());
		table.setCellpadding(getCellpadding());
		table.setCellspacing(getCellspacing());
		table.setColumns(7);
		table.setWidth(6,"12");
		table.setWidth(7,"12");

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

		List formerStudents = new Vector(getBusiness().getSchoolClassMemberBusiness().findStudentsInClass(getSchoolClassID()));

		if (!formerStudents.isEmpty()) {
			Map studentMap = getBusiness().getStudentList(formerStudents);
			Collections.sort(formerStudents, new SchoolClassMemberComparator(sortStudentsBy,iwc.getCurrentLocale(), getUserBusiness(iwc), studentMap));
			Iterator iter = formerStudents.iterator();
			while (iter.hasNext()) {
				studentMember = (SchoolClassMember) iter.next();
				student = (User) studentMap.get(new Integer(studentMember.getClassMemberId()));
				schoolClass = getBusiness().getSchoolClassBusiness().findSchoolClass(new Integer(studentMember.getSchoolClassId()));
				address = getUserBusiness(iwc).getUserAddress1(((Integer) student.getPrimaryKey()).intValue());
				delete = (SubmitButton) getStyledInterface(new SubmitButton(getDeleteIcon(localize("school.delete_from_group","Click to remove student from group"))));
				delete.setDescription(localize("school.delete_from_group","Click to remove student from group"));
				delete.setValueOnClick(PARAMETER_APPLICANT_ID, String.valueOf(studentMember.getClassMemberId()));
				delete.setValueOnClick(PARAMETER_METHOD, String.valueOf(ACTION_DELETE));
				move = new Link(getEditIcon(localize("school.move_to_another_group","Move this student to another group")));
				move.setWindowToOpen(SchoolAdminWindow.class);
				move.setParameter(SchoolAdminOverview.PARAMETER_METHOD, String.valueOf(SchoolAdminOverview.METHOD_MOVE_GROUP));
				move.setParameter(SchoolAdminOverview.PARAMETER_USER_ID, String.valueOf(studentMember.getClassMemberId()));
				
				String name = student.getNameLastFirst(true);
				if (iwc.getCurrentLocale().getLanguage().equalsIgnoreCase("is"))
					name = student.getName();

				table.add(getSmallText(name), 1, row);
				table.add(getSmallText(PersonalIDFormatter.format(student.getPersonalID(), iwc.getCurrentLocale())), 2, row);
				
				if (PIDChecker.getInstance().isFemale(student.getPersonalID()))
					table.add(getSmallText(localize("school.girl","Girl")),3,row);
				else
					table.add(getSmallText(localize("school.boy","Boy")),3,row);
				
				if (address != null && address.getStreetAddress() != null)
					table.add(getSmallText(address.getStreetAddress()), 4, row);
				if (schoolClass != null)
					table.add(getSmallText(schoolClass.getName()), 5, row);
				table.add(move, 6, row);
				table.add(delete, 7, row++);
			}
		}

		SubmitButton back = (SubmitButton) getStyledInterface(new SubmitButton(localize("school.back", "Back")));
		back.setValueOnClick(PARAMETER_ACTION, String.valueOf(ACTION_MANAGE));
		
		SubmitButton groupReady = (SubmitButton) getStyledInterface(new SubmitButton(localize("school.class_ready", "Class ready")));
		groupReady.setValueOnClick(PARAMETER_ACTION, String.valueOf(ACTION_MANAGE));
		groupReady.setValueOnClick(PARAMETER_METHOD, String.valueOf(ACTION_FINALIZE_GROUP));
		groupReady.setSubmitConfirm(localize("school.confirm_group_ready","Are you sure you want to set the group as ready and send out e-mails to all parents?"));

		table.add(back, 1, row);
		table.add(Text.NON_BREAKING_SPACE, 1, row);
		table.add(groupReady, 1, row);
		table.mergeCells(1, row, table.getColumns(), row);
		table.setAlignment(1, row, Table.HORIZONTAL_ALIGN_RIGHT);
		table.setColumnAlignment(3, Table.HORIZONTAL_ALIGN_CENTER);
		table.setHorizontalZebraColored(getZebraColor2(), getZebraColor1());
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
	
	protected Table getSearchAndSortTable() {
		Table table = new Table(2,3);
		table.setCellpadding(0);
		table.setCellspacing(0);

		if (searchEnabled) {
			table.add(getSmallHeader(localize("school.search_for","Search for")+":"+Text.NON_BREAKING_SPACE),1,1);
			
			TextInput tiSearch = (TextInput) getStyledInterface(new TextInput(PARAMETER_SEARCH, searchString));
			table.add(tiSearch, 2, 1);
			
			table.setHeight(2, "2");
		}
		
		table.add(getSmallHeader(localize("school.sort_by","Sort by")+":"+Text.NON_BREAKING_SPACE),1,3);
		
		DropdownMenu menu = (DropdownMenu) getStyledInterface(new DropdownMenu(PARAMETER_SORT));
		menu.addMenuElement(SchoolChoiceComparator.NAME_SORT, localize("school.sort_name","Name"));
		menu.addMenuElement(SchoolChoiceComparator.ADDRESS_SORT, localize("school.sort_address","Address"));
		menu.addMenuElement(SchoolChoiceComparator.GENDER_SORT, localize("school.sort_gender","Gender"));
		if (action != ACTION_SAVE)
			menu.addMenuElement(SchoolChoiceComparator.LANGUAGE_SORT, localize("school.sort_language","Language"));
		menu.setSelectedElement(sortChoicesBy);
		menu.setToSubmit();
		table.add(menu,2,3);
		
		table.setColumnAlignment(2, Table.HORIZONTAL_ALIGN_RIGHT);
		
		return table;
	}

	private void saveClass(IWContext iwc) throws RemoteException {
		String[] applications = iwc.getParameterValues(PARAMETER_APPLICANT_ID);
		String[] students = iwc.getParameterValues(getSession().getParameterStudentID());

		IWTimestamp stamp = new IWTimestamp();
		int userID = ((Integer) iwc.getCurrentUser().getPrimaryKey()).intValue();

		if (applications != null && applications.length > 0) {
			for (int a = 0; a < applications.length; a++) {
				StringTokenizer tokens = new StringTokenizer(applications[a],",");
				getBusiness().getSchoolClassMemberBusiness().storeSchoolClassMember(Integer.parseInt(tokens.nextToken()), getSchoolClassID(), stamp.getTimestamp(), userID);
				getBusiness().getSchoolChoiceBusiness().groupPlaceAction(new Integer(tokens.nextToken()), iwc.getCurrentUser());
			}
		}

		if (students != null && students.length > 0) {
			for (int a = 0; a < students.length; a++) {
				getBusiness().getSchoolClassMemberBusiness().storeSchoolClassMember(Integer.parseInt(students[a]), getSchoolClassID(), stamp.getTimestamp(), userID);
			}
		}
	}

	private void delete(IWContext iwc) throws RemoteException {
		String student = iwc.getParameter(PARAMETER_APPLICANT_ID);
		if (student != null && student.length() > 0) {
			getBusiness().getSchoolClassMemberBusiness().removeSchoolClassMember(Integer.parseInt(student));
			SchoolChoice choice = getBusiness().getSchoolChoiceBusiness().findByStudentAndSchoolAndSeason(Integer.parseInt(student), getSchoolID(), getSchoolSeasonID());
			if (choice != null)
				getBusiness().getSchoolChoiceBusiness().setAsPreliminary(choice, iwc.getCurrentUser());
		}
	}
	
	private void finalize(IWContext iwc) throws RemoteException {
		int schoolClassID = getSession().getSchoolClassID();
		getBusiness().finalizeGroup(schoolClassID, localize("school.finalize_subject",""), localize("school.finalize_body",""));
	}

	private void validateSchoolClass(Collection previousClasses) throws RemoteException {
		SchoolClass previousClass = getBusiness().getSchoolClassBusiness().findSchoolClass(new Integer(_previousSchoolClassID));
		if (previousClass != null && !previousClasses.contains(previousClass))
			_previousSchoolClassID = -1;
	}

	private UserBusiness getUserBusiness(IWContext iwc) throws RemoteException {
		return (UserBusiness) IBOLookup.getServiceInstance(iwc, UserBusiness.class);
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