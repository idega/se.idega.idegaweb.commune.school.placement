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

import se.idega.idegaweb.commune.school.business.SchoolClassMemberComparator;
import se.idega.idegaweb.commune.school.data.SchoolChoice;
import se.idega.idegaweb.commune.school.event.SchoolEventListener;

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
import com.idega.presentation.ui.SubmitButton;
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

	private final int ACTION_MANAGE = 1;
	private final int ACTION_SAVE = 2;
	private final int ACTION_FINALIZE_GROUP = 3;
	private final int ACTION_DELETE = 4;

	private Map students;

	private int action = 0;
	private int method = 0;
	private int _previousSchoolClassID = -1;
	private int _previousSchoolSeasonID = -1;
	private int _previousSchoolYearID = -1;

	public void init(IWContext iwc) throws RemoteException {
		parseAction(iwc);

		switch (method) {
			case ACTION_SAVE :
				saveClass(iwc);
				break;
			case ACTION_DELETE :
				delete(iwc);
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

		table.add(getNavigationTable(true), 1, 1);

		students = getBusiness().getStudentList(getBusiness().getSchoolClassMemberBusiness().findStudentsBySchoolAndSeason(getSchoolID(), getSchoolSeasonID()));

		table.add(getApplicationTable(iwc), 1, 5);
		table.add(getChoiceHeader(), 1, 3);

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
			table.add(method, 1, 11);
			table.add(submit, 1, 11);
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

		table.add(getNavigationTable(true), 1, 1);
		table.add(getNewStudentTable(iwc), 1, 3);

		add(form);
	}

	private Table getApplicationTable(IWContext iwc) throws RemoteException {
		Table table = new Table();
		table.setColumns(5);
		table.setWidth(getWidth());
		table.setCellpadding(1);
		table.setCellspacing(0);

		int row = 1;
		table.add(getHeader(localize("school.name", "Name")), 1, row);
		table.add(getHeader(localize("school.personal_id", "Personal ID")), 2, row);
		table.add(getHeader(localize("school.from_school", "From School")), 3, row);
		table.add(getHeader(localize("school.date", "Date")), 4, row++);

		CheckBox checkBox = new CheckBox();
		Link link;
		SchoolYear year = getBusiness().getSchoolYearBusiness().getSchoolYear(new Integer(getSchoolYearID()));
		if (year != null) {
			Collection applicants = getBusiness().getSchoolChoiceBusiness().getApplicantsForSchoolAndSeasonAndGrade(getSchoolID(), getSchoolSeasonID(), year.getSchoolYearAge() - 1);
			if (!applicants.isEmpty()) {
				SchoolChoice choice;
				School school;
				User applicant;
				IWCalendar calendar;

				Iterator iter = applicants.iterator();
				while (iter.hasNext()) {
					choice = (SchoolChoice) iter.next();
					if (choice.getStatus().equalsIgnoreCase("PREL")) {
						applicant = getUserBusiness(iwc).getUser(choice.getChildId());
						school = getBusiness().getSchoolBusiness().getSchool(new Integer(choice.getCurrentSchoolId()));
						checkBox = new CheckBox(PARAMETER_APPLICANT_ID, String.valueOf(choice.getChildId()) + "," + choice.getPrimaryKey().toString());
						calendar = new IWCalendar(iwc.getCurrentLocale(), choice.getCreated());
						checkBox.setWidth("12");
						checkBox.setHeight("12");
						if (students.containsValue(applicant))
							checkBox.setDisabled(true);

						String name = applicant.getNameLastFirst(true);
						if (iwc.getCurrentLocale().getLanguage().equalsIgnoreCase("is"))
							name = applicant.getName();

						if (choice.getChoiceOrder() > 1)
							table.setRowColor(row, "#FF3333");
						else {
							if (row % 2 == 0)
								table.setRowColor(row, "#FFFFFF");
							else
								table.setRowColor(row, "#EFEFEF");
						}

						link = (Link) this.getSmallLink(name);
						link.setWindowToOpen(SchoolAdminWindow.class);
						link.setParameter(SchoolAdminOverview.PARAMETER_METHOD, String.valueOf(SchoolAdminOverview.METHOD_OVERVIEW));
						link.setParameter(SchoolAdminOverview.PARAMETER_USER_ID, String.valueOf(choice.getChildId()));
						link.setParameter(SchoolAdminOverview.PARAMETER_CHOICE_ID, choice.getPrimaryKey().toString());
						
						table.add(link, 1, row);
						table.add(getSmallText(PersonalIDFormatter.format(applicant.getPersonalID(), iwc.getCurrentLocale())), 2, row);
						if (school != null) {
							String schoolName = school.getName();
							if (schoolName.length() > 20)
								schoolName = schoolName.substring(0, 20) + "...";
							table.add(getSmallText(schoolName), 3, row);
						}
						table.add(getSmallText(calendar.getLocaleDate(IWCalendar.SHORT)), 4, row);
						table.add(checkBox, 5, row++);
					}
				}
			}
		}

		GenericButton selectAll = (GenericButton) getStyledInterface(new GenericButton());
		selectAll.setValue(localize("school.select_all", "Select all"));
		selectAll.setToCheckOnClick(checkBox, true, false);

		GenericButton deselectAll = (GenericButton) getStyledInterface(new GenericButton());
		deselectAll.setValue(localize("school.deselect_all", "Deselect all"));
		deselectAll.setToCheckOnClick(checkBox, false);

		table.add(selectAll, 1, row);
		table.add(deselectAll, 1, row);
		table.mergeCells(1, row, table.getColumns(), row);
		table.setAlignment(1, row, Table.HORIZONTAL_ALIGN_RIGHT);
		table.setColumnAlignment(4, Table.HORIZONTAL_ALIGN_CENTER);
		table.setColumnAlignment(5, Table.HORIZONTAL_ALIGN_RIGHT);
		table.setRowColor(1, "#CCCCCC");
		table.setRowColor(row, "#FFFFFF");

		return table;
	}

	private Table getStudentTable(IWContext iwc) throws RemoteException {
		Table table = new Table();
		table.setWidth(getWidth());
		table.setCellpadding(1);
		table.setCellspacing(0);
		table.setColumns(5);

		int row = 1;
		table.add(getHeader(localize("school.name", "Name")), 1, row);
		table.add(getHeader(localize("school.personal_id", "Personal ID")), 2, row);
		table.add(getHeader(localize("school.address", "Address")), 3, row);
		table.add(getHeader(localize("school.class", "Class")), 4, row++);

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
			Collections.sort(formerStudents, new SchoolClassMemberComparator(iwc.getCurrentLocale(), getUserBusiness(iwc), studentMap));
			Iterator iter = formerStudents.iterator();
			while (iter.hasNext()) {
				studentMember = (SchoolClassMember) iter.next();
				student = (User) studentMap.get(new Integer(studentMember.getClassMemberId()));
				schoolClass = getBusiness().getSchoolClassBusiness().findSchoolClass(new Integer(studentMember.getSchoolClassId()));
				address = getUserBusiness(iwc).getUserAddress1(((Integer) student.getPrimaryKey()).intValue());
				checkBox = new CheckBox(getSession().getParameterStudentID(), String.valueOf(((Integer) student.getPrimaryKey()).intValue()));
				checkBox.setWidth("12");
				checkBox.setHeight("12");
				if (students.containsValue(student))
					checkBox.setDisabled(true);

				String name = student.getNameLastFirst(true);
				if (iwc.getCurrentLocale().getLanguage().equalsIgnoreCase("is"))
					name = student.getName();

				link = (Link) this.getSmallLink(name);
				link.setWindowToOpen(SchoolAdminWindow.class);
				link.setParameter(SchoolAdminOverview.PARAMETER_METHOD, String.valueOf(SchoolAdminOverview.METHOD_OVERVIEW));
				link.setParameter(SchoolAdminOverview.PARAMETER_USER_ID, String.valueOf(studentMember.getClassMemberId()));
				
				table.add(link, 1, row);
				table.add(getSmallText(PersonalIDFormatter.format(student.getPersonalID(), iwc.getCurrentLocale())), 2, row);
				if (address != null && address.getStreetAddress() != null)
					table.add(getSmallText(address.getStreetAddress()), 3, row);
				if (schoolClass != null)
					table.add(getSmallText(schoolClass.getName()), 4, row);
				table.add(checkBox, 5, row++);
			}
		}

		GenericButton selectAll = (GenericButton) getStyledInterface(new GenericButton());
		selectAll.setValue(localize("school.select_all", "Select all"));
		selectAll.setToCheckOnClick(checkBox, true, false);

		GenericButton deselectAll = (GenericButton) getStyledInterface(new GenericButton());
		deselectAll.setValue(localize("school.deselect_all", "Deselect all"));
		deselectAll.setToCheckOnClick(checkBox, false);

		table.add(selectAll, 1, row);
		table.add(deselectAll, 1, row);
		table.mergeCells(1, row, 5, row);
		table.setAlignment(1, row, Table.HORIZONTAL_ALIGN_RIGHT);
		table.setColumnAlignment(4, Table.HORIZONTAL_ALIGN_CENTER);
		table.setColumnAlignment(5, Table.HORIZONTAL_ALIGN_RIGHT);
		table.setHorizontalZebraColored("#EFEFEF", "#FFFFFF");
		table.setRowColor(1, "#CCCCCC");
		table.setRowColor(row, "#FFFFFF");

		return table;
	}

	private Table getNewStudentTable(IWContext iwc) throws RemoteException {
		Table table = new Table();
		table.setWidth(getWidth());
		table.setCellpadding(1);
		table.setCellspacing(0);
		table.setColumns(5);

		int row = 1;
		table.add(getHeader(localize("school.name", "Name")), 1, row);
		table.add(getHeader(localize("school.personal_id", "Personal ID")), 2, row);
		table.add(getHeader(localize("school.address", "Address")), 3, row);
		table.add(getHeader(localize("school.class", "Class")), 4, row);
		table.add(new HiddenInput(PARAMETER_APPLICANT_ID, "-1"), 5, row);
		table.add(new HiddenInput(PARAMETER_METHOD, "0"), 5, row++);

		User student;
		Address address;
		SchoolClassMember studentMember;
		SchoolClass schoolClass = null;
		SubmitButton delete;

		List formerStudents = new Vector(getBusiness().getSchoolClassMemberBusiness().findStudentsInClass(getSchoolClassID()));

		if (!formerStudents.isEmpty()) {
			Map studentMap = getBusiness().getStudentList(formerStudents);
			Collections.sort(formerStudents, new SchoolClassMemberComparator(iwc.getCurrentLocale(), getUserBusiness(iwc), studentMap));
			Iterator iter = formerStudents.iterator();
			while (iter.hasNext()) {
				studentMember = (SchoolClassMember) iter.next();
				student = (User) studentMap.get(new Integer(studentMember.getClassMemberId()));
				schoolClass = getBusiness().getSchoolClassBusiness().findSchoolClass(new Integer(studentMember.getSchoolClassId()));
				address = getUserBusiness(iwc).getUserAddress1(((Integer) student.getPrimaryKey()).intValue());
				delete = (SubmitButton) getStyledInterface(new SubmitButton(localize("school.delete", "Delete")));
				delete.setValueOnClick(PARAMETER_APPLICANT_ID, String.valueOf(studentMember.getClassMemberId()));
				delete.setValueOnClick(PARAMETER_METHOD, String.valueOf(ACTION_DELETE));

				String name = student.getNameLastFirst(true);
				if (iwc.getCurrentLocale().getLanguage().equalsIgnoreCase("is"))
					name = student.getName();

				table.add(getSmallText(name), 1, row);
				table.add(getSmallText(PersonalIDFormatter.format(student.getPersonalID(), iwc.getCurrentLocale())), 2, row);
				if (address != null && address.getStreetAddress() != null)
					table.add(getSmallText(address.getStreetAddress()), 3, row);
				if (schoolClass != null)
					table.add(getSmallText(schoolClass.getName()), 4, row);
				table.add(delete, 5, row++);
			}
		}

		SubmitButton back = (SubmitButton) getStyledInterface(new SubmitButton(localize("school.back", "Back")));
		back.setValueOnClick(PARAMETER_ACTION, String.valueOf(ACTION_MANAGE));
		SubmitButton groupReady = (SubmitButton) getStyledInterface(new SubmitButton(localize("school.class_ready", "Class ready")));
		groupReady.setValueOnClick(PARAMETER_ACTION, String.valueOf(ACTION_FINALIZE_GROUP));

		table.add(back, 1, row);
		table.add(groupReady, 1, row);
		table.mergeCells(1, row, table.getColumns(), row);
		table.setAlignment(1, row, Table.HORIZONTAL_ALIGN_RIGHT);
		table.setColumnAlignment(4, Table.HORIZONTAL_ALIGN_CENTER);
		table.setColumnAlignment(5, Table.HORIZONTAL_ALIGN_RIGHT);
		table.setHorizontalZebraColored("#EFEFEF", "#FFFFFF");
		table.setRowColor(1, "#CCCCCC");
		table.setRowColor(row, "#FFFFFF");

		return table;
	}

	protected Table getPreviousHeader(Collection classes) throws RemoteException {
		Table table = new Table(2, 1);
		table.setCellpadding(0);
		table.setCellspacing(0);
		table.setWidth(Table.HUNDRED_PERCENT);
		table.setAlignment(2, 1, Table.HORIZONTAL_ALIGN_RIGHT);

		table.add(getHeader(localize("school.previous_year_class", "Previous year class")), 1, 1);
		table.add(getSmallHeader(localize("school.class", "Class") + Text.NON_BREAKING_SPACE), 2, 1);
		table.add(getPreviousSchoolClasses(classes), 2, 1);

		return table;
	}

	protected Table getChoiceHeader() throws RemoteException {
		Table table = new Table(2, 1);
		table.setCellpadding(0);
		table.setCellspacing(0);
		table.setWidth(Table.HUNDRED_PERCENT);
		table.setAlignment(2, 1, Table.HORIZONTAL_ALIGN_RIGHT);

		table.add(getHeader(localize("school.school_choices_for_year", "School choices for selected year")), 1, 1);
		table.add(getHeader(String.valueOf(getBusiness().getSchoolChoiceBusiness().getNumberOfApplications(getSchoolID(), getSchoolSeasonID(), getBusiness().getGradeForYear(getSchoolYearID()) - 1))), 2, 1);
		table.add(getHeader(" / "), 2, 1);
		table.add(getHeader(String.valueOf(getBusiness().getSchoolChoiceBusiness().getNumberOfApplications(getSchoolID(), getSchoolSeasonID()))), 2, 1);

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

	private void validateSchoolClass(Collection previousClasses) throws RemoteException {
		SchoolClass previousClass = getBusiness().getSchoolClassBusiness().findSchoolClass(new Integer(_previousSchoolClassID));
		if (previousClass != null && !previousClasses.contains(previousClass))
			_previousSchoolClassID = -1;
	}

	private UserBusiness getUserBusiness(IWContext iwc) throws RemoteException {
		return (UserBusiness) IBOLookup.getServiceInstance(iwc, UserBusiness.class);
	}
}