/**
 * Created on 28.1.2003
 *
 * This class does something very clever.
 */
package se.idega.idegaweb.commune.school.presentation;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import se.idega.idegaweb.commune.school.accounting.presentation.SchoolAccountingCommuneBlock;
import se.idega.idegaweb.commune.school.business.SchoolChoiceComparator;
import se.idega.idegaweb.commune.school.data.SchoolChoice;
import se.idega.idegaweb.commune.school.data.SchoolChoiceBMPBean;
import se.idega.idegaweb.commune.school.event.SchoolEventListener;
import se.idega.util.PIDChecker;

import com.idega.block.school.data.School;
import com.idega.block.school.data.SchoolYear;
import com.idega.presentation.IWContext;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.Table;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.TextInput;
import com.idega.user.data.User;
import com.idega.util.IWCalendar;
import com.idega.util.PersonalIDFormatter;
import com.idega.util.text.Name;
import com.idega.util.text.TextSoap;

/**
 * @author laddi
 */
public class SchoolChoicesStatistics extends SchoolAccountingCommuneBlock {

	private final String PARAMETER_SORT = "sch_choice_sort";
	private final String PARAMETER_SEARCH = "scH_choise_search";
	private final String PARAMETER_CURRENT_APPLICATION_PAGE = "sch_crrap_pg";

	private int applicationsPerPage = 10;
	private int sortChoicesBy = SchoolChoiceComparator.NAME_SORT;
	private String searchString = "";
	private int currentPage;
	private int start;

	/**
	 * @see se.idega.idegaweb.commune.school.presentation.SchoolCommuneBlock#init(com.idega.presentation.IWContext)
	 */
	public void init(IWContext iwc) throws Exception {
		parseAction(iwc);
		drawForm(iwc);
	}

	private void drawForm(IWContext iwc) throws RemoteException {
		Form form = new Form();
		form.setEventListener(SchoolEventListener.class);
		
		Table table = new Table(1, 5);
		table.setCellpadding(0);
		table.setCellspacing(0);
		table.setWidth(getWidth());
		table.setHeight(2, "12");
		table.setHeight(4, "6");
		form.add(table);

		Table headerTable = new Table(2, 1);
		headerTable.setWidth(Table.HUNDRED_PERCENT);
		headerTable.setCellpaddingAndCellspacing(0);
		headerTable.setAlignment(2, 1, Table.HORIZONTAL_ALIGN_RIGHT);
		table.add(headerTable, 1, 1);

		headerTable.add(getNavigationTable(true, true), 1, 1);
		headerTable.add(getSearchAndSortTable(), 2, 1);
		headerTable.setVerticalAlignment(2, 1, Table.VERTICAL_ALIGN_BOTTOM);

		table.add(getChoiceHeader(), 1, 3);
		table.add(getApplicationTable(iwc), 1, 5);

		add(form);
	}

	private void parseAction(IWContext iwc) {
		if (iwc.isParameterSet(PARAMETER_SORT))
			sortChoicesBy = Integer.parseInt(iwc.getParameter(PARAMETER_SORT));
		else
			sortChoicesBy = SchoolChoiceComparator.NAME_SORT;

		if (iwc.isParameterSet(PARAMETER_SEARCH))
			searchString = iwc.getParameter(PARAMETER_SEARCH);

		if (searchString != null && searchString.length() > 0) {
			try {
				String temp = searchString;
				if (temp.indexOf("-") != -1)
					temp = TextSoap.findAndCut(temp, "-");
				Long.parseLong(temp);
				if (temp.length() == 10 ) {
					int firstTwo = Integer.parseInt(temp.substring(0, 2));
					if (firstTwo < 85) {
						temp = "20"+temp;
					}	else {
						temp = "19"+temp;
					}
				}
				searchString = temp;
			}
			catch (NumberFormatException nfe) {}
		}
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
		if (getSchoolID() == -1)
			showLanguage = true;

		String[] validStatuses =  new String[] { SchoolChoiceBMPBean.CASE_STATUS_PLACED, SchoolChoiceBMPBean.CASE_STATUS_PRELIMINARY, SchoolChoiceBMPBean.CASE_STATUS_MOVED };
		int applicantsSize = 0;
		try {
			applicantsSize = getBusiness().getSchoolChoiceBusiness().getNumberOfApplicantsForSchool(getSchoolID(), getSchoolSeasonID(), schoolYearAge, null, validStatuses, searchString);
		}
		catch (Exception e) {
			applicantsSize = 0;
		}

		int row = 1;
		int column = 1;
		int headerRow = -1;

		table.add(getNavigationTable(iwc, applicantsSize), 1, row++);

		headerRow = row;
		table.add(getSmallHeader(localize("school.name", "Name")), column++, row);
		table.add(getSmallHeader(localize("school.personal_id", "Personal ID")), column++, row);
		table.add(getSmallHeader(localize("school.gender", "Gender")), column++, row);
		table.add(getSmallHeader(localize("school.from_school", "From School")), column++, row);
		if (showLanguage)
			table.add(getSmallHeader(localize("school.language", "Language")), column++, row);
		table.add(getSmallHeader(localize("school.date", "Date")), column, row++);

		Collection applicants = getBusiness().getSchoolChoiceBusiness().getApplicantsForSchool(getSchoolID(), getSchoolSeasonID(), schoolYearAge, validStatuses, searchString, sortChoicesBy, applicationsPerPage, start);
		if (!applicants.isEmpty()) {
			Map studentMap = getBusiness().getUserMapFromChoices(applicants);

			Link link;
			SchoolChoice choice;
			School school;
			User applicant;
			IWCalendar calendar;

			Iterator iter = applicants.iterator();
			while (iter.hasNext()) {
				column = 1;
				choice = (SchoolChoice) iter.next();
				applicant = (User) studentMap.get(new Integer(choice.getChildId()));
				school = getBusiness().getSchoolBusiness().getSchool(new Integer(choice.getCurrentSchoolId()));
				calendar = new IWCalendar(iwc.getCurrentLocale(), choice.getCreated());

				Name childName = new Name(applicant.getFirstName(), applicant.getMiddleName(), applicant.getLastName());
				String name = childName.getName(iwc.getApplicationSettings().getDefaultLocale(), true);

				if (choice.getChoiceOrder() > 1 || choice.getStatus().equalsIgnoreCase(SchoolChoiceBMPBean.CASE_STATUS_MOVED))
					table.setRowColor(row, "#FFEAEA");
				else {
					if (row % 2 == 0)
						table.setRowColor(row, getZebraColor1());
					else
						table.setRowColor(row, getZebraColor2());
				}

				link = getSmallLink(name);
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
				table.add(getSmallText(calendar.getLocaleDate(IWCalendar.SHORT)), column++, row++);
			}
		}

		table.mergeCells(1, row, table.getColumns(), row);
		table.add(getStatisticsTable(validStatuses), 1, row++);

		table.setColumnAlignment(3, Table.HORIZONTAL_ALIGN_CENTER);
		table.setRowColor(headerRow, getHeaderColor());
		table.mergeCells(1, 1, table.getColumns(), 1);

		return table;
	}
	
	private PresentationObject getStatisticsTable(String[] validStatuses) {
		try {
			int firstApplSize = getBusiness().getSchoolChoiceBusiness().getSchoolChoiceHome().getCount(getSchoolID(), getSchoolSeasonID(), -1, new int[] { 1 }, validStatuses, "");
			int secondApplSize = getBusiness().getSchoolChoiceBusiness().getSchoolChoiceHome().getCount(getSchoolID(), getSchoolSeasonID(), -1, new int[] { 2 }, validStatuses, "");
			int thirdApplSize = getBusiness().getSchoolChoiceBusiness().getSchoolChoiceHome().getCount(getSchoolID(), getSchoolSeasonID(), -1, new int[] { 3 }, validStatuses, "");

			int unHandledMoves = getBusiness().getSchoolChoiceBusiness().getSchoolChoiceHome().getNumberOfUnHandledMoves(getSchoolSeasonID());
			int handledMoves = getBusiness().getSchoolChoiceBusiness().getSchoolChoiceHome().getNumberOfHandledMoves(getSchoolSeasonID());
			

			String[] allStatuses = new String[] { SchoolChoiceBMPBean.CASE_STATUS_PRELIMINARY, SchoolChoiceBMPBean.CASE_STATUS_MOVED, SchoolChoiceBMPBean.CASE_STATUS_PLACED };
			String[] handledStatuses = new String[] { SchoolChoiceBMPBean.CASE_STATUS_PLACED };
//			String[] unhandledStatuses = new String[] { SchoolChoiceBMPBean.CASE_STATUS_PRELIMINARY, SchoolChoiceBMPBean.CASE_STATUS_MOVED };

			
			int allApplSize = getBusiness().getSchoolChoiceBusiness().getSchoolChoiceHome().getCount(getSchoolID(), getSchoolSeasonID(), -1, new int[] {}, allStatuses, "");
			int handledApplSize = getBusiness().getSchoolChoiceBusiness().getSchoolChoiceHome().getCount(getSchoolID(), getSchoolSeasonID(), -1, new int[] {}, handledStatuses, "");
//			int unhandledApplSize = getBusiness().getSchoolChoiceBusiness().getSchoolChoiceHome().getCount(getSchoolID(), getSchoolSeasonID(), -1, new int[] {}, unhandledStatuses, "");
			
			Table statTable = new Table();
			int sRow = 1;
			statTable.setCellpadding(1);
			statTable.setCellspacing(0);

			statTable.add(getSmallText(localize("applications_all", "All applications") + ":"), 1, sRow);
			statTable.add(getSmallText("" + allApplSize), 2, sRow++);

			statTable.add(getSmallText(localize("applications_handled", "Handled applications") + ":"), 1, sRow);
			statTable.add(getSmallText("" + handledApplSize), 2, sRow++);

			statTable.add(getSmallText(localize("applications_on_first_choice", "Applcations on first choice") + ":"), 1, sRow);
			statTable.add(getSmallText("" + firstApplSize), 2, sRow++);

			statTable.add(getSmallText(localize("applications_on_second_choice", "Applcations on second choice") + ":"), 1, sRow);
			statTable.add(getSmallText("" + secondApplSize), 2, sRow++);

			statTable.add(getSmallText(localize("applications_on_third_choice", "Applcations on third choice") + ":"), 1, sRow);
			statTable.add(getSmallText("" + thirdApplSize), 2, sRow++);

			statTable.add(getSmallText("&nbsp;"), 1, sRow);
			statTable.add(getSmallText("&nbsp;"), 2, sRow++);

			statTable.add(getSmallText(localize("moves", "Total moves") + ":"), 1, sRow);
			statTable.add(getSmallText("" + (unHandledMoves + handledMoves)), 2, sRow++);

			statTable.add(getSmallText(localize("handled_moves", "Handled moves") + ":"), 1, sRow);
			statTable.add(getSmallText("" + (handledMoves)), 2, sRow++);


			return statTable;
		}
		catch (Exception e) {
			return getSmallText(localize("error_in_statistics","Error in statistics"));
		}
	}
	
	private Table getNavigationTable(IWContext iwc, int applicantsSize) {
		currentPage = 0;
		int maxPage = (int) Math.ceil(applicantsSize / applicationsPerPage);
		if (iwc.isParameterSet(PARAMETER_CURRENT_APPLICATION_PAGE)) {
			currentPage = Integer.parseInt(iwc.getParameter(PARAMETER_CURRENT_APPLICATION_PAGE));
		}
		start = currentPage * applicationsPerPage;

		Table navigationTable = new Table(3, 1);
		navigationTable.setCellpadding(0);
		navigationTable.setCellspacing(0);
		navigationTable.setWidth(Table.HUNDRED_PERCENT);
		navigationTable.setWidth(1, "33%");
		navigationTable.setWidth(2, "33%");
		navigationTable.setWidth(3, "33%");
		navigationTable.setAlignment(2, 1, Table.HORIZONTAL_ALIGN_CENTER);
		navigationTable.setAlignment(3, 1, Table.HORIZONTAL_ALIGN_RIGHT);

		Text prev = getSmallText(localize("previous", "Previous"));
		Text next = getSmallText(localize("next", "Next"));
		Text info = getSmallText(localize("page", "Page") + " " + (currentPage + 1) + " " + localize("of", "of") + " " + (maxPage + 1));
		if (currentPage > 0) {
			Link lPrev = getSmallLink(localize("previous", "Previous"));
			lPrev.addParameter(PARAMETER_CURRENT_APPLICATION_PAGE, Integer.toString(currentPage - 1));
			lPrev.addParameter(PARAMETER_SEARCH, iwc.getParameter(PARAMETER_SEARCH));
			lPrev.addParameter(PARAMETER_SORT, iwc.getParameter(PARAMETER_SORT));
			navigationTable.add(lPrev, 1, 1);
		}
		else {
			navigationTable.add(prev, 1, 1);
		}
		navigationTable.add(info, 2, 1);

		if (currentPage < maxPage) {
			Link lNext = getSmallLink(localize("next", "Next"));
			lNext.addParameter(PARAMETER_CURRENT_APPLICATION_PAGE, Integer.toString(currentPage + 1));
			lNext.addParameter(PARAMETER_SEARCH, iwc.getParameter(PARAMETER_SEARCH));
			lNext.addParameter(PARAMETER_SORT, iwc.getParameter(PARAMETER_SORT));
			navigationTable.add(lNext, 3, 1);
		}
		else {
			navigationTable.add(next, 3, 1);
		}
		
		return navigationTable;
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

	protected Table getSearchAndSortTable() throws RemoteException {
		Table table = new Table(2, 3);
		table.setCellpadding(0);
		table.setCellspacing(0);
		SchoolYear schoolYear = getBusiness().getSchoolBusiness().getSchoolYear(new Integer(getSchoolYearID()));
		int yearAge = -1;
		if (schoolYear != null)
			yearAge = schoolYear.getSchoolYearAge();

		table.add(getSmallHeader(localize("school.search_for", "Search for") + ":" + Text.NON_BREAKING_SPACE), 1, 1);
		TextInput tiSearch = (TextInput) getStyledInterface(new TextInput(PARAMETER_SEARCH, searchString));
		table.add(tiSearch, 2, 1);
		table.setHeight(2, "2");

		table.add(getSmallHeader(localize("school.sort_by", "Sort by") + ":" + Text.NON_BREAKING_SPACE), 1, 3);

		DropdownMenu menu = (DropdownMenu) getStyledInterface(new DropdownMenu(PARAMETER_SORT));
		menu.addMenuElement(SchoolChoiceComparator.NAME_SORT, localize("school.sort_name", "Name"));
		menu.addMenuElement(SchoolChoiceComparator.PERSONAL_ID_SORT, localize("school.sort_personal_id", "Personal ID"));
		menu.addMenuElement(SchoolChoiceComparator.GENDER_SORT, localize("school.sort_gender", "Gender"));
		if (yearAge >= 12)
			menu.addMenuElement(SchoolChoiceComparator.LANGUAGE_SORT, localize("school.sort_language", "Language"));
		menu.setSelectedElement(sortChoicesBy);
		menu.setToSubmit();
		table.add(menu, 2, 3);

		table.setColumnAlignment(2, Table.HORIZONTAL_ALIGN_RIGHT);

		return table;
	}
}