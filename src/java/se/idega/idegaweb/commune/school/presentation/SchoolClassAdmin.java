/**
 * Created on 1.2.2003 This class does something very clever.
 */
package se.idega.idegaweb.commune.school.presentation;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import se.idega.idegaweb.commune.school.business.SchoolChoiceComparator;
import se.idega.idegaweb.commune.school.business.SchoolClassMemberComparator;
import se.idega.idegaweb.commune.school.data.SchoolChoice;
import se.idega.idegaweb.commune.school.event.SchoolEventListener;
import se.idega.util.PIDChecker;

import com.idega.block.school.data.SchoolClass;
import com.idega.block.school.data.SchoolClassMember;
import com.idega.block.school.data.SchoolYear;
import com.idega.business.IBOLookup;
import com.idega.core.location.data.Address;
import com.idega.presentation.IWContext;
import com.idega.presentation.Table;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.HiddenInput;
import com.idega.user.business.UserBusiness;
import com.idega.user.data.User;
import com.idega.util.IWTimestamp;
import com.idega.util.PersonalIDFormatter;

/**
 * @author laddi To change this generated comment edit the template variable
 *         "typecomment": Window>Preferences>Java>Templates. To enable and
 *         disable the creation of type comments go to
 *         Window>Preferences>Java>Code Generation.
 */
public class SchoolClassAdmin extends SchoolCommuneBlock {

	private final String PARAMETER_ACTION = "sch_action";
	private final String PARAMETER_METHOD = "sch_method";
	private final String PARAMETER_STUDENT_ID = "sch_student_id";
	private final String PARAMETER_SORT = "sch_student_sort";

	private final int ACTION_MANAGE = 1;
	private final int ACTION_SAVE = 2;
	private final int ACTION_DELETE = 4;

	private int action = 0;
	private int method = 0;
	private int sortStudentsBy = SchoolChoiceComparator.NAME_SORT;

	private boolean multipleSchools = false;
	private boolean showBunRadioButtons = false;
	private SchoolClass _group;

	/**
	 * @see se.idega.idegaweb.commune.school.presentation.SchoolCommuneBlock#init(com.idega.presentation.IWContext)
	 */
	public void init(IWContext iwc) throws RemoteException {
		if (iwc.isLoggedOn()) {
			parseAction(iwc);

			switch (method) {
				case ACTION_SAVE :
					//saveClass(iwc);
					break;
				case ACTION_DELETE :
					delete(iwc);
					break;
			}

			switch (action) {
				case ACTION_MANAGE :
					drawForm(iwc);
					break;
			}
		}
		else {
			add(super.getSmallHeader(localize("not_logged_on", "Not logged on")));
		}
	}

	private void parseAction(IWContext iwc) {
		if (iwc.isParameterSet(PARAMETER_ACTION))
			action = Integer.parseInt(iwc.getParameter(PARAMETER_ACTION));
		else
			action = ACTION_MANAGE;

		if (iwc.isParameterSet(PARAMETER_METHOD))
			method = Integer.parseInt(iwc.getParameter(PARAMETER_METHOD));
		else
			method = 0;

		if (iwc.isParameterSet(PARAMETER_SORT))
			sortStudentsBy = Integer.parseInt(iwc.getParameter(PARAMETER_SORT));
		else
			sortStudentsBy = SchoolChoiceComparator.NAME_SORT;

	}

	private void drawForm(IWContext iwc) throws RemoteException {
		Form form = new Form();
		form.setEventListener(SchoolEventListener.class);
		form.add(new HiddenInput(PARAMETER_ACTION, String.valueOf(action)));

		Table table = new Table(1, 3);
		table.setCellpadding(0);
		table.setCellspacing(0);
		table.setWidth(getWidth());
		table.setHeight(2, "12");

		form.add(table);

		Table headerTable = new Table(1, 3);
		headerTable.setWidth(Table.HUNDRED_PERCENT);
		headerTable.setCellpaddingAndCellspacing(0);
		headerTable.setHeight(1, 2, "20");
		//headerTable.setAlignment(2, 1, Table.HORIZONTAL_ALIGN_RIGHT);
		table.add(headerTable, 1, 1);

		headerTable.add(getNavigationTable(true, multipleSchools, showBunRadioButtons), 1, 1);
		headerTable.add(getSortTable(), 1, 3);
		headerTable.setVerticalAlignment(1, 3, Table.VERTICAL_ALIGN_BOTTOM);

		if (getSchoolClassID() != -1) {
			_group = getBusiness().getSchoolBusiness().findSchoolClass(new Integer(getSchoolClassID()));
			table.add(getStudentTable(iwc), 1, 3);
			table.add(getLegendTable(), 1, 3);
		}
		add(form);
	}

	private Table getStudentTable(IWContext iwc) throws RemoteException {
		Table table = new Table();
		table.setWidth(getWidth());
		table.setCellpadding(getCellpadding());
		table.setCellspacing(getCellspacing());
		table.setColumns(6);
		table.setWidth(6, "12");
		//table.setWidth(7, "12");
		int row = 1;

		/*if (_group.getIsSubGroup()) {
			Table addTable = new Table(3, 1);
			addTable.setCellpadding(0);
			addTable.setCellspacing(0);
			addTable.setWidth(2, "4");
			table.add(addTable, 1, row++);

			Link addLink = new Link(getEditIcon(localize("school.add_student", "Add student")));
			addLink.setWindowToOpen(SchoolAdminWindow.class);
			addLink.addParameter(SchoolAdminOverview.PARAMETER_METHOD, SchoolAdminOverview.METHOD_ADD_STUDENT);
			addLink.addParameter(SchoolAdminOverview.PARAMETER_PAGE_ID, getParentPage().getPageID());
			addTable.add(addLink, 1, 1);

			Link addLinkText = getSmallLink(localize("school.add_student", "Add student"));
			addLinkText.setWindowToOpen(SchoolAdminWindow.class);
			addLinkText.addParameter(SchoolAdminOverview.PARAMETER_METHOD, SchoolAdminOverview.METHOD_ADD_STUDENT);
			addLinkText.addParameter(SchoolAdminOverview.PARAMETER_PAGE_ID, getParentPage().getPageID());
			addTable.add(addLinkText, 3, 1);
		}*/

		table.add(getSmallHeader(localize("school.name", "Name")), 1, row);
		table.add(getSmallHeader(localize("school.personal_id", "Personal ID")), 2, row);
		table.add(getSmallHeader(localize("school.gender", "Gender")), 3, row);
		table.add(getSmallHeader(localize("school.address", "Address")), 4, row);
		table.add(getSmallHeader(localize("school.class", "Class")), 5, row);
		table.add(new HiddenInput(PARAMETER_STUDENT_ID, "-1"), 6, row);
		table.add(new HiddenInput(PARAMETER_METHOD, "0"), 6, row);
		table.setRowColor(row++, getHeaderColor());

		User student;
		Address address;
		SchoolClassMember studentMember;
		SchoolClass schoolClass = null;
		//SubmitButton delete;
		Link move;
		Link link;
		int numberOfStudents = 0;
		boolean hasChoice = false;
		boolean hasMoveChoice = false;
		boolean hasSpecialPlacement = false;
		boolean hasComment = false;
		boolean notStarted = false;
		boolean hasTerminationDate = false;
		boolean showComment = false;
		boolean showNotStarted = false;
		boolean showHasTermination = false;

		IWTimestamp stamp = new IWTimestamp();
		IWTimestamp startDate;

		List students = null;
		if (!_group.getIsSubGroup()) {
			students = new ArrayList(getBusiness().getSchoolBusiness().findStudentsInClassAndYear(getSchoolClassID(), getSchoolYearID()));
		}
		else {
			students = new ArrayList(getBusiness().getSchoolBusiness().findSubGroupPlacements(_group));
		}

		if (!students.isEmpty()) {
			numberOfStudents = students.size();
			Map studentMap = getBusiness().getStudentList(students);
			Collections.sort(students, new SchoolClassMemberComparator(sortStudentsBy, iwc.getCurrentLocale(), getUserBusiness(iwc), studentMap));
			Iterator iter = students.iterator();
			while (iter.hasNext()) {
				studentMember = (SchoolClassMember) iter.next();
				student = (User) studentMap.get(new Integer(studentMember.getClassMemberId()));
				schoolClass = getBusiness().getSchoolBusiness().findSchoolClass(new Integer(studentMember.getSchoolClassId()));
				address = getUserBusiness(iwc).getUserAddress1(((Integer) student.getPrimaryKey()).intValue());
				hasChoice = getBusiness().hasChoiceToThisSchool(studentMember.getClassMemberId(), getSchoolID(), getSchoolSeasonID());
				hasMoveChoice = getBusiness().hasMoveChoiceToOtherSchool(studentMember.getClassMemberId(), getSchoolID(), getSchoolSeasonID());
				hasSpecialPlacement = studentMember.getSpeciallyPlaced();
				hasComment = studentMember.getNotes() != null;
				notStarted = false;
				hasTerminationDate = false;

				if (studentMember.getRegisterDate() != null) {
					startDate = new IWTimestamp(studentMember.getRegisterDate());
					if (startDate.isLaterThan(stamp)) {
						notStarted = true;
					}
				}
				if (studentMember.getRemovedDate() != null) {
					hasTerminationDate = true;
				}

				/*
				 * delete = new
				 * SubmitButton(getDeleteIcon(localize("school.delete_from_group",
				 * "Click to remove student from
				 * group")),"delete_student_"+String.valueOf(new
				 * Integer(studentMember.getClassMemberId())));
				 * delete.setDescription(localize("school.delete_from_group", "Click to
				 * remove student from group"));
				 * delete.setValueOnClick(PARAMETER_STUDENT_ID,
				 * String.valueOf(studentMember.getClassMemberId()));
				 * delete.setValueOnClick(PARAMETER_METHOD,
				 * String.valueOf(ACTION_DELETE));
				 * delete.setSubmitConfirm(localize("school.confirm_student_delete","Are
				 * you sure you want to remove the student from this class?"));
				 */
				move = new Link(getEditIcon(localize("school.move_to_another_group", "Move this student to another group")));
				move.setWindowToOpen(SchoolAdminWindow.class);
				move.setParameter(SchoolAdminOverview.PARAMETER_METHOD, String.valueOf(SchoolAdminOverview.METHOD_MOVE_GROUP));
				move.setParameter(getSchoolCommuneSession(iwc).getParameterSchoolClassID(), String.valueOf(studentMember.getSchoolClassId()));
				move.setParameter(SchoolAdminOverview.PARAMETER_USER_ID, String.valueOf(studentMember.getClassMemberId()));
				move.setParameter(SchoolAdminOverview.PARAMETER_SHOW_NO_CHOICES, "true");
				move.addParameter(SchoolAdminOverview.PARAMETER_PAGE_ID, getParentPage().getPageID());

				//String name = student.getNameLastFirst(true);
				String name = getBusiness().getUserBusiness().getNameLastFirst(student, true);
				if (iwc.getCurrentLocale().getLanguage().equalsIgnoreCase("is"))
					name = student.getName();

				if (hasMoveChoice) {
					table.setRowColor(row, HAS_MOVE_CHOICE_COLOR);
				}
				else if (hasSpecialPlacement) {
					table.setRowColor(row, IS_SPECIALLY_PLACED_COLOR);
				}
				else if (hasChoice) {
					table.setRowColor(row, HAS_SCHOOL_CHOICE_COLOR);
				}
				else {
					if (row % 2 == 0)
						table.setRowColor(row, getZebraColor1());
					else
						table.setRowColor(row, getZebraColor2());
				}

				if (hasComment) {
					showComment = true;
					table.add(getSmallErrorText("*"), 1, row);
				}
				if (notStarted) {
					showNotStarted = true;
					table.add(getSmallErrorText("+"), 1, row);
				}
				if (hasTerminationDate) {
					showHasTermination = true;
					table.add(getSmallErrorText("&Delta;"), 1, row);
				}
				if (hasComment || notStarted || hasTerminationDate) {
					table.add(getSmallText(Text.NON_BREAKING_SPACE), 1, row);
				}

				link = getSmallLink(name);
				link.setWindowToOpen(SchoolAdminWindow.class);
				link.setParameter(SchoolAdminOverview.PARAMETER_METHOD, String.valueOf(SchoolAdminOverview.METHOD_OVERVIEW));
				link.setParameter(SchoolAdminOverview.PARAMETER_USER_ID, String.valueOf(studentMember.getClassMemberId()));
				link.setParameter(SchoolAdminOverview.PARAMETER_SHOW_ONLY_OVERVIEW, "true");
				link.setParameter(SchoolAdminOverview.PARAMETER_SHOW_NO_CHOICES, "true");
				link.addParameter(SchoolAdminOverview.PARAMETER_PAGE_ID, getParentPage().getPageID());
				link.addParameter(SchoolAdminOverview.PARAMETER_SCHOOL_CLASS_ID, getSchoolClassID());
				link.addParameter(SchoolAdminOverview.PARAMETER_SCHOOL_CLASS_MEMBER_ID, ((Integer) studentMember.getPrimaryKey()).toString());
				if (studentMember.getRemovedDate() != null)
					link.addParameter(SchoolAdminOverview.PARAMETER_SCHOOL_CLASS_MEMBER_REMOVED_DATE, studentMember.getRemovedDate().toString());

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
				//table.add(delete, 7, row);
				row++;
			}

			if (showComment || showNotStarted || showHasTermination) {
				table.setHeight(row++, 2);
				if (showComment) {
					table.mergeCells(1, row, table.getColumns(), row);
					table.add(getSmallErrorText("* "), 1, row);
					table.add(getSmallText(localize("school.has_notes", "Placment has comment attached")), 1, row++);
				}
				if (showNotStarted) {
					table.mergeCells(1, row, table.getColumns(), row);
					table.add(getSmallErrorText("+ "), 1, row);
					table.add(getSmallText(localize("school.placement_has_not_started", "Placment has not started yet")), 1, row++);
				}
				if (showHasTermination) {
					table.mergeCells(1, row, table.getColumns(), row);
					table.add(getSmallErrorText("&Delta; "), 1, row);
					table.add(getSmallText(localize("school.placement_has_termination_date", "Placment has termination date")), 1, row++);
				}
			}
		}

		if (numberOfStudents > 0) {
			table.mergeCells(1, row, table.getColumns(), row);
			table.add(getSmallHeader(localize("school.number_of_students", "Number of students") + ": " + String.valueOf(numberOfStudents)), 1, row++);
		}

		table.setColumnAlignment(3, Table.HORIZONTAL_ALIGN_CENTER);
		table.setColumnAlignment(5, Table.HORIZONTAL_ALIGN_CENTER);
		table.setRowColor(row, "#FFFFFF");

		return table;
	}

	protected Table getSortTable() throws RemoteException {
		Table table = new Table(2, 3);
		table.setCellpadding(0);
		table.setCellspacing(0);
		SchoolYear schoolYear = getBusiness().getSchoolBusiness().getSchoolYear(new Integer(getSchoolYearID()));
		int yearAge = -1;
		if (schoolYear != null)
			yearAge = schoolYear.getSchoolYearAge();

		table.add(getSmallHeader(localize("school.sort_by", "Sort by") + ":" + Text.NON_BREAKING_SPACE), 1, 3);

		DropdownMenu menu = (DropdownMenu) getStyledInterface(new DropdownMenu(PARAMETER_SORT));
		menu.addMenuElement(SchoolChoiceComparator.NAME_SORT, localize("school.sort_name", "Name"));
		menu.addMenuElement(SchoolChoiceComparator.PERSONAL_ID_SORT, localize("school.sort_personal_id", "Personal ID"));
		menu.addMenuElement(SchoolChoiceComparator.ADDRESS_SORT, localize("school.sort_address", "Address"));
		menu.addMenuElement(SchoolChoiceComparator.GENDER_SORT, localize("school.sort_gender", "Gender"));
		if (action != ACTION_SAVE && yearAge >= 12)
			menu.addMenuElement(SchoolChoiceComparator.LANGUAGE_SORT, localize("school.sort_language", "Language"));
		menu.setSelectedElement(sortStudentsBy);
		menu.setToSubmit();
		table.add(menu, 2, 3);

		table.setColumnAlignment(2, Table.HORIZONTAL_ALIGN_RIGHT);

		return table;
	}

	private void delete(IWContext iwc) throws RemoteException {
		String student = iwc.getParameter(PARAMETER_STUDENT_ID);
		if (student != null && student.length() > 0) {
			getBusiness().getSchoolBusiness().removeSchoolClassMemberFromClass(Integer.parseInt(student), getSchoolClassID());
			SchoolChoice choice = getBusiness().getSchoolChoiceBusiness().findByStudentAndSchoolAndSeason(Integer.parseInt(student), getSchoolID(), getSchoolSeasonID());
			getBusiness().setNeedsSpecialAttention(Integer.parseInt(student), getBusiness().getPreviousSchoolSeasonID(getSchoolSeasonID()), false);
			if (choice != null) {
				if (choice.getCaseStatus().equals("PLAC"))
					getBusiness().getSchoolChoiceBusiness().setAsPreliminary(choice, iwc.getCurrentUser());
			}
		}
	}

	private UserBusiness getUserBusiness(IWContext iwc) throws RemoteException {
		return (UserBusiness) IBOLookup.getServiceInstance(iwc, UserBusiness.class);
	}

	/* Setters */
	/**
	 * Turns on/of view of drop down showing providers
	 */
	public void setMultipleSchools(boolean multiple) {
		multipleSchools = multiple;
	}

	/**
	 * Turns on/off view of radiobuttons for showing BUN administrated shools or
	 * not
	 * 
	 * @param show
	 */
	public void setShowBunRadioButtons(boolean show) {
		showBunRadioButtons = show;
	}
}