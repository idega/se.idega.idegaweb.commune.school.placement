package se.idega.idegaweb.commune.school.presentation;

import java.rmi.RemoteException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ejb.FinderException;

import se.idega.idegaweb.commune.school.accounting.presentation.SchoolAccountingCommuneBlock;
import se.idega.idegaweb.commune.school.business.SchoolChoiceBusiness;
import se.idega.idegaweb.commune.school.business.SchoolChoiceComparator;
import se.idega.idegaweb.commune.school.business.SchoolChoiceWriter;
import se.idega.idegaweb.commune.school.business.SchoolClassMemberComparator;
import se.idega.idegaweb.commune.school.business.SchoolClassWriter;
import se.idega.idegaweb.commune.school.data.SchoolChoice;
import se.idega.idegaweb.commune.school.data.SchoolChoiceBMPBean;
import se.idega.idegaweb.commune.school.data.SchoolChoiceHome;
import se.idega.idegaweb.commune.school.event.SchoolEventListener;
import se.idega.util.PIDChecker;

import com.idega.block.process.data.Case;
import com.idega.block.school.business.SchoolBusiness;
import com.idega.block.school.data.School;
import com.idega.block.school.data.SchoolClass;
import com.idega.block.school.data.SchoolClassMember;
import com.idega.block.school.data.SchoolSeason;
import com.idega.block.school.data.SchoolYear;
import com.idega.business.IBOLookup;
import com.idega.core.location.data.Address;
import com.idega.core.location.data.Commune;
import com.idega.core.location.data.CommuneHome;
import com.idega.data.IDOLookup;
import com.idega.idegaweb.IWMainApplication;
import com.idega.io.MediaWritable;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.Layer;
import com.idega.presentation.Table;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.CheckBox;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.GenericButton;
import com.idega.presentation.ui.HiddenInput;
import com.idega.presentation.ui.SubmitButton;
import com.idega.presentation.ui.TextInput;
import com.idega.user.data.User;
import com.idega.util.IWTimestamp;
import com.idega.util.PersonalIDFormatter;
import com.idega.util.text.TextSoap;

/**
 * @author Laddi
 */
public class SchoolClassEditor extends SchoolAccountingCommuneBlock {

	public static final String PARAMETER_ACTION = "sch_action";

	private final String PARAMETER_METHOD = "sch_method";
	private final String PARAMETER_APPLICANT_ID = "sch_applicant_id";
	private final String PARAMETER_PREVIOUS_CLASS_ID = "sch_prev_class_id";
	private final String PARAMETER_SORT = "sch_choice_sort";
	private final String PARAMETER_SORT_PLACED = "sch_choice_sort_placed";
	private final String PARAMETER_SEARCH = "scH_choise_search";
	private final String PARAMETER_CURRENT_APPLICATION_PAGE = "sch_crrap_pg";
	private final String PARAMETER_DELETE_CHOICE_ID = "delete_sch_choice";

	private final int ACTION_MANAGE = 1;
	public static final int ACTION_SAVE = 2;
	private final int ACTION_FINALIZE_GROUP = 3;
	private final int ACTION_DELETE = 4;
	private final int ACTION_DELETE_SCHOOL_CHOICE = 5;

	private int action = 0;
	private int method = 0;
	private int sortStudentsBy = SchoolChoiceComparator.NAME_SORT;
	private int sortChoicesBy = SchoolClassMemberComparator.NAME_SORT;
	private int sortPlaced = SchoolChoiceComparator.PLACED_SORT;
	private int sortPlacedUnplacedBy = -1;

	private String searchString = "";

	private int _previousSchoolClassID = -1;
	private int _previousSchoolSeasonID = -1;
	private int _previousSchoolYearID = -1;
	private int _choiceForDeletion = -1;

	private boolean multibleSchools = false;
	private boolean showStudentTable = true;
	private boolean showMessageTextButton = false;
	private boolean searchEnabled = true;

	private int applicationsPerPage = 10;

	private boolean showStatistics = true;
	private boolean showBunRadioButtons = false;
	//private boolean isOngoingSeason = false;
	private boolean _useForTesting = false;

	private SchoolClass _group;

	private int _languageAge = 12;

	public void init(IWContext iwc) throws RemoteException {
		if (iwc.isLoggedOn()) {
			parseAction(iwc);

			switch (method) {
				case ACTION_SAVE:
					saveClass(iwc);
					break;
				case ACTION_DELETE:
					delete(iwc);
					break;
				case ACTION_FINALIZE_GROUP:
					finalizeGroup();
					break;
				case ACTION_DELETE_SCHOOL_CHOICE:
					deleteSchoolChoice(iwc, _choiceForDeletion);
					break;
			}

			switch (action) {
				case ACTION_MANAGE:
					drawForm(iwc);
					break;
				case ACTION_SAVE:
					drawNewGroupForm(iwc);
					break;

			}
		}
		else {
			add(super.getSmallHeader(localize("not_logged_on", "Not logged on")));
		}
	}

	private void parseAction(IWContext iwc) throws RemoteException {
		//isOngoingSeason = getBusiness().isOngoingSeason(getSchoolSeasonID());

		if (iwc.isParameterSet(PARAMETER_PREVIOUS_CLASS_ID)) _previousSchoolClassID = Integer.parseInt(iwc.getParameter(PARAMETER_PREVIOUS_CLASS_ID));

		_previousSchoolSeasonID = getBusiness().getPreviousSchoolSeasonID(getSchoolSeasonID());
		if (getSchoolYearID() != -1) _previousSchoolYearID = getBusiness().getPreviousSchoolYear(getSchoolYearID());

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

		if (iwc.isParameterSet(PARAMETER_SORT_PLACED))
			sortPlaced = Integer.parseInt(iwc.getParameter(PARAMETER_SORT_PLACED));
		else
			sortPlaced = -1;
		sortPlacedUnplacedBy = sortPlaced;

		if (iwc.isParameterSet(PARAMETER_SEARCH)) searchString = iwc.getParameter(PARAMETER_SEARCH);

		_group = getBusiness().getSchoolBusiness().findSchoolClass(new Integer(getSchoolClassID()));

		/** Fixing String */
		if (searchString != null && searchString.length() > 0) {
			try {
				String temp = searchString;
				temp = TextSoap.findAndCut(temp, "-");
				Long.parseLong(temp);
				if (temp.length() == 10) {
					int firstTwo = Integer.parseInt(temp.substring(0, 2));
					if (firstTwo < 85) {
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

		if (iwc.isParameterSet(PARAMETER_DELETE_CHOICE_ID)) _choiceForDeletion = Integer.parseInt(iwc.getParameter(PARAMETER_DELETE_CHOICE_ID));
	}

	private void drawForm(IWContext iwc) throws RemoteException {
		Form form = new Form();
		form.setEventListener(SchoolEventListener.class);
		form.add(new HiddenInput(PARAMETER_ACTION, String.valueOf(action)));

		Table table = new Table(1, 17);
		table.setCellpadding(0);
		table.setCellspacing(0);
		table.setWidth(getWidth());
		table.setHeight(2, "6");
		table.setHeight(4, "12");
		table.setHeight(6, "3");
		table.setHeight(8, "3");
		table.setHeight(10, "18");
		table.setHeight(12, "3");
		table.setHeight(14, "3");
		table.setHeight(16, "12");
		form.add(table);

		table.add(getNavigationTable(true, multibleSchools, showBunRadioButtons), 1, 1);
		table.add(getSearchAndSortTable(), 1, 3);
		table.add(getSmallHeader(localize("school.school_choices_for_year", "School choices for selected year")), 1, 5);
		if (useStyleNames()) {
			table.setCellpaddingLeft(1, 1, 12);
			table.setCellpaddingLeft(1, 3, 12);
			table.setCellpaddingLeft(1, 5, 12);
			table.setCellpaddingRight(1, 1, 12);
			table.setCellpaddingRight(1, 3, 12);
			table.setCellpaddingRight(1, 5, 12);
		}

		table.add(getApplicationTable(iwc), 1, 7);

		table.add(getLegendTable(true, getSchoolID() < 1), 1, 9);
		if (useStyleNames()) {
			table.setCellpaddingLeft(1, 9, 12);
			table.setCellpaddingRight(1, 9, 12);
		}

		if (this.showStudentTable) {
			//if (_previousSchoolYearID != -1 && !isOngoingSeason) {
			if (_previousSchoolYearID != -1) {
				try {
					Collection previousClasses = getBusiness().getPreviousSchoolClasses(getBusiness().getSchoolBusiness().getSchool(new Integer(getSchoolID())), getBusiness().getSchoolBusiness().getSchoolSeason(new Integer(getSchoolSeasonID())), getBusiness().getSchoolBusiness().getSchoolYear(new Integer(getSchoolYearID())));
					validateSchoolClass(previousClasses);

					table.add(getPreviousHeader(previousClasses), 1, 11);
					table.add(getStudentTable(iwc), 1, 13);
					table.add(getLegendTable(), 1, 15);
					if (useStyleNames()) {
						table.setCellpaddingLeft(1, 11, 12);
						table.setCellpaddingLeft(1, 15, 12);
						table.setCellpaddingRight(1, 11, 12);
						table.setCellpaddingRight(1, 15, 12);
					}
				}
				catch (NullPointerException ne) {
				}
			}

			if (getSchoolSeasonID() != -1) {
				GenericButton report = getButton(new GenericButton("report", localize("school.show_student_info", "Student list")));
				if (getResponsePage() != null) {
					report.setPageToOpen(getResponsePage());
					table.add(report, 1, 17);
					table.add(Text.getNonBrakingSpace(), 1, 17);
				}
			}

			if (getSchoolClassID() != -1) {
				HiddenInput method = new HiddenInput(PARAMETER_METHOD, "0");

				SubmitButton submit = (SubmitButton) getStyledInterface(new SubmitButton(localize("save", "Save")));
				submit.setValueOnClick(PARAMETER_METHOD, String.valueOf(ACTION_SAVE));
				submit.setValueOnClick(PARAMETER_ACTION, String.valueOf(ACTION_SAVE));
				form.setToDisableOnSubmit(submit, true);
				SubmitButton view = (SubmitButton) getStyledInterface(new SubmitButton(localize("school.view_group", "View group")));
				view.setValueOnClick(PARAMETER_ACTION, String.valueOf(ACTION_SAVE));
				table.add(method, 1, 17);
				table.add(submit, 1, 17);
				table.add(Text.getNonBrakingSpace(), 1, 17);
				table.add(view, 1, 17);
			}
		}

		if (showMessageTextButton) {
			GenericButton msgText = getButton(new GenericButton("messageText", localize("school.show_message_text", "Message Text")));
			msgText.addParameterToWindow(SchoolAdminOverview.PARAMETER_METHOD, SchoolAdminOverview.METHOD_MESSAGE_TEXT);
			msgText.setWindowToOpen(SchoolAdminWindow.class);
			table.add(Text.getNonBrakingSpace(), 1, 17);
			table.add(msgText, 1, 17);
		}
		
		if (useStyleNames()) {
			table.setCellpaddingLeft(1, 17, 12);
			table.setCellpaddingRight(1, 17, 12);
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

		headerTable.add(getNavigationTable(true, multibleSchools, showBunRadioButtons), 1, 1);
		headerTable.add(getSearchAndSortTable(), 2, 1);
		if (useStyleNames()) {
			headerTable.setCellpaddingLeft(1, 1, 12);
			headerTable.setCellpaddingRight(2, 1, 12);
		}

		if (getSchoolClassID() != -1) {
			table.setAlignment(1, row, Table.HORIZONTAL_ALIGN_RIGHT);
			if (useStyleNames()) {
				table.setCellpaddingRight(1, row, 12);
			}
			Link pdfLink = getPDFLink(SchoolClassWriter.class, getBundle().getImage("shared/pdf.gif"));
			pdfLink.addParameter(SchoolClassWriter.prmClassId, getSchoolClassID());
			pdfLink.addParameter(SchoolClassWriter.prmYearId, getSchoolYearID());
			table.add(pdfLink, 1, row);
			Link excelLink = getXLSLink(SchoolClassWriter.class, getBundle().getImage("shared/xls.gif"));
			excelLink.addParameter(SchoolClassWriter.prmClassId, getSchoolClassID());
			excelLink.addParameter(SchoolClassWriter.prmYearId, getSchoolYearID());
			table.add(Text.getNonBrakingSpace(), 1, row);
			table.add(excelLink, 1, row++);
		}

		table.add(getNewStudentTable(iwc), 1, row++);
		table.add(getLegendTable(), 1, row);
		if (useStyleNames()) {
			table.setCellpaddingLeft(1, row, 12);
			table.setCellpaddingRight(1, row, 12);
		}

		add(form);
	}

	private Table getApplicationTable(IWContext iwc) throws RemoteException {
		Table table = new Table();
		table.setWidth(getWidth());
		table.setCellpadding(getCellpadding());
		table.setCellspacing(getCellspacing());
		boolean showLanguage = false;

		SchoolYear year = getBusiness().getSchoolBusiness().getSchoolYear(new Integer(getSchoolYearID()));
		boolean currentSeason = getBusiness().getCurrentSchoolSeasonID() == getSchoolSeasonID();
		if (year != null && year.getSchoolYearAge() >= _languageAge) showLanguage = true;

		if (showLanguage) {
			table.setColumns(9); 
		}
		else {
			table.setColumns(8);
		}
		
		if (useStyleNames()) {
			table.setColumns(table.getColumns() - 1);
		}

		if (!showStudentTable) {
			table.setColumns(table.getColumns() - 1);
		}

		String[] validStatuses = new String[] { SchoolChoiceBMPBean.CASE_STATUS_PLACED, SchoolChoiceBMPBean.CASE_STATUS_PRELIMINARY, SchoolChoiceBMPBean.CASE_STATUS_MOVED};

		int applicantsSize = 0;
		int start = -1;
		int currPage = 0;
		int maxPage = 0;

		if (this.multibleSchools) {
			try {
				applicantsSize = getBusiness().getSchoolChoiceBusiness().getNumberOfApplicantsForSchool(getSchoolID(), getSchoolSeasonID(), getSchoolYearID(), null, validStatuses, searchString, sortPlacedUnplacedBy);

			}
			catch (Exception e) {
				applicantsSize = 0;
			}

			currPage = 0;
			maxPage = (int) Math.ceil(applicantsSize / applicationsPerPage);
			if (iwc.isParameterSet(PARAMETER_CURRENT_APPLICATION_PAGE)) {
				currPage = Integer.parseInt(iwc.getParameter(PARAMETER_CURRENT_APPLICATION_PAGE));
			}
			start = currPage * applicationsPerPage;
		}
		else {
			applicationsPerPage = -1;
		}

		Collection applicants = getBusiness().getSchoolChoiceBusiness().getApplicantsForSchool(getSchoolID(), getSchoolSeasonID(), getSchoolYearID(), validStatuses, searchString, sortChoicesBy, applicationsPerPage, start, sortPlacedUnplacedBy);

		int row = 2;
		if (multibleSchools) row = 1;
		int column = 1;
		int headerRow = -1;

		if (this.multibleSchools) {
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
				lPrev.addParameter(PARAMETER_SORT_PLACED, iwc.getParameter(PARAMETER_SORT_PLACED));
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
				lNext.addParameter(PARAMETER_SORT_PLACED, iwc.getParameter(PARAMETER_SORT_PLACED));
				navigationTable.add(lNext, 3, 1);
			}
			else {
				navigationTable.add(next, 3, 1);
			}
		}

		headerRow = row;
		if (useStyleNames()) {
			table.setCellpaddingLeft(1, row, 12);
			table.setCellpaddingRight(table.getColumns(), row, 12);
		}
		table.add(getSmallHeader(localize("school.name", "Name")), column++, row);
		table.add(getSmallHeader(localize("school.personal_id", "Personal ID")), column++, row);
		table.add(getSmallHeader(localize("school.address", "Address")), column++, row);
		if (!useStyleNames()) {
			table.add(getSmallHeader(localize("school.gender", "Gender")), column++, row);
		}
		table.add(getSmallHeader(localize("school.from_school", "From School")), column++, row);
		if (showLanguage) table.add(getSmallHeader(localize("school.language", "Language")), column++, row);
		table.add(getSmallHeader(localize("school.created", "Created")), column++, row);
		table.add(Text.getNonBrakingSpace(), column, row); // Empty header for
		// erase buttons
		row++;

		CheckBox checkBox = new CheckBox();
		Link link;

		// Added for SchoolChoice deletion
		if (getSchoolClassID() == -1) table.add(new HiddenInput(PARAMETER_METHOD, "0"), column, row);
		table.add(new HiddenInput(PARAMETER_DELETE_CHOICE_ID, "-1"), column, row);

		Date from = null;
		Date to = null;
		try {
			from = getBusiness().getSchoolChoiceBusiness().getSchoolChoiceStartDate().getDate();
			to = getBusiness().getSchoolChoiceBusiness().getSchoolChoiceEndDate().getDate();
		}
		catch (FinderException e) {
			log(e);
		}

		if (!applicants.isEmpty()) {
			SchoolChoice choice;
			School school;
			User applicant;
			Address address;
			IWTimestamp created;
			boolean hasComment = false;
			boolean showComment = false;
			boolean hasPlacement = false;
			boolean showPlacement = false;

			Iterator iter = applicants.iterator();

			while (iter.hasNext()) {
				column = 1;
				choice = (SchoolChoice) iter.next();
				created = new IWTimestamp(choice.getCreated());
				applicant = getUserBusiness(iwc).getUser(choice.getChildId());
				school = getBusiness().getSchoolBusiness().getSchool(new Integer(choice.getCurrentSchoolId()));
				checkBox = getCheckBox(PARAMETER_APPLICANT_ID, choice.getPrimaryKey().toString());
				address = getUserBusiness(iwc).getUsersMainAddress(applicant);
				hasComment = choice.getMessage() != null;

				if (getBusiness().isAlreadyInSchool(choice.getChildId(), getSession().getSchoolID(), getSession().getSchoolSeasonID())) {
					hasPlacement = true;
					if (_group != null && _group.getIsSubGroup()) {
						if (getBusiness().getSchoolBusiness().hasGroupPlacement(choice.getChildId(), getSchoolClassID(), _group.getIsSubGroup())) {
							checkBox.setDisabled(true);
						}
					}
					else {
						checkBox.setDisabled(true);
					}
				}
				else {
					hasPlacement = false;
					if (_group != null && _group.getIsSubGroup()) {
						checkBox.setDisabled(true);
					}
				}

				if (useStyleNames()) {
					if (row % 2 == 0)
						table.setRowStyleClass(row, getDarkRowClass());
					else
						table.setRowStyleClass(row, getLightRowClass());
					table.setCellpaddingLeft(1, row, 12);
					table.setCellpaddingRight(table.getColumns(), row, 12);
				}

				//String name = applicant.getNameLastFirst(true);
				String name = getBusiness().getUserBusiness().getNameLastFirst(applicant, true);
				if (iwc.getCurrentLocale().getLanguage().equalsIgnoreCase("is")) name = applicant.getName();

				if (choice.getStatus().equalsIgnoreCase(SchoolChoiceBMPBean.CASE_STATUS_MOVED)) {
					table.setRowColor(row, HAS_MOVE_CHOICE_COLOR_THIS_SCHOOL);
				}
				else if (choice.getChoiceOrder() > 1) {
					table.setRowColor(row, HAS_REJECTED_FIRST_CHOICE_COLOR);
				}
				else if ((choice.getSchoolChoiceDate().before(from) || choice.getSchoolChoiceDate().after(to)) && currentSeason && (getSchoolID() < 1)) {
					table.setRowColor(row, HAS_MOVED_TO_COMMUNE_COLOR);
				}
				else {
					if (!useStyleNames()) {
						if (row % 2 == 0)
							table.setRowColor(row, getZebraColor1());
						else
							table.setRowColor(row, getZebraColor2());
					}
				}

				link = getSmallLink(name);
				link.setWindowToOpen(SchoolAdminWindow.class);
				link.setParameter(SchoolAdminOverview.PARAMETER_METHOD, String.valueOf(SchoolAdminOverview.METHOD_OVERVIEW));
				link.setParameter(SchoolAdminOverview.PARAMETER_USER_ID, String.valueOf(choice.getChildId()));
				link.setParameter(SchoolAdminOverview.PARAMETER_CHOICE_ID, choice.getPrimaryKey().toString());
				link.setParameter(SchoolAdminOverview.PARAMETER_RESOURCE_SEASON, String.valueOf(choice.getSchoolSeasonId()));
				link.setParameter(SchoolAdminOverview.PARAMETER_RESOURCE_STUDENT, String.valueOf(choice.getChildId()));
				link.setParameter(SchoolAdminOverview.PARAMETER_RESOURCE_CHOICE_STATUS, choice.getStatus());
				link.setParameter(SchoolAdminOverview.PARAMETER_RESOURCE_CLASS_MEMBER, "-1");

				if (hasComment || hasPlacement) {
					if (hasComment) {
						showComment = true;
						table.add(getSmallErrorText("*"), column, row);
					}
					if (hasPlacement) {
						showPlacement = true;
						table.add(getSmallErrorText("+"), column, row);
					}
					table.add(getSmallText(Text.NON_BREAKING_SPACE), column, row);
				}

				table.add(link, column++, row);
				table.add(getSmallText(PersonalIDFormatter.format(applicant.getPersonalID(), iwc.getCurrentLocale())), column, row);
				table.setNoWrap(column, row);
				column++;
				if (address != null && address.getStreetAddress() != null) {
					table.add(getSmallText(address.getStreetAddress()), column, row);
				}
				column++;
				if (!useStyleNames()) {
					if (PIDChecker.getInstance().isFemale(applicant.getPersonalID()))
						table.add(getSmallText(localize("school.girl", "Girl")), column++, row);
					else
						table.add(getSmallText(localize("school.boy", "Boy")), column++, row);
				}
				
				if (school != null) {
					String schoolName = school.getName();
					if (schoolName.length() > 20) schoolName = schoolName.substring(0, 20) + "...";
					table.add(getSmallText(schoolName), column, row);
					if (choice.getStatus().equalsIgnoreCase(SchoolChoiceBMPBean.CASE_STATUS_MOVED)) table.add(getSmallText(" (" + localize("school.moved", "Moved") + ")"), column, row);
				}
				column++;
				if (showLanguage) { 
					if (choice.getLanguageChoice() != null) table.add(getSmallText(localize(choice.getLanguageChoice(), "")), column, row);
					column++;
				}
				table.add(getSmallText(created.getLocaleDate(iwc.getCurrentLocale(), IWTimestamp.SHORT)), column, row);
				table.setNoWrap(column, row);
				column++;
				if (showStudentTable && getSchoolClassID() != -1) {
					table.setWidth(column, "12");
					table.add(checkBox, column, row);
					column++;
				}
				if (livesOutsideDefaultCommune(iwc, applicant)) {
					// Get delete button
					Image delImg = getDeleteIcon(localize("delete", "Delete"));
					int choiceID = ((Integer) choice.getPrimaryKey()).intValue();

					SubmitButton delButt = new SubmitButton(delImg);
					delButt.setValueOnClick(PARAMETER_METHOD, String.valueOf(ACTION_DELETE_SCHOOL_CHOICE));
					delButt.setValueOnClick(PARAMETER_DELETE_CHOICE_ID, String.valueOf(choiceID));
					delButt.setSubmitConfirm(localize("school.confirm_delete_school_choice_msg", "Do you really want to erase this school choice?"));
					delButt.setToolTip(localize("school.delete_school_choice", "Delete school choice"));
					table.add(delButt, column, row);
				}
				row++;
			}

			if (showComment || showPlacement) {
				table.setHeight(row++, 2);
				if (showComment) {
					table.mergeCells(1, row, table.getColumns(), row);
					if (useStyleNames()) {
						table.setCellpaddingLeft(1, row, 12);
					}
					table.add(getSmallErrorText("* "), 1, row);
					table.add(getSmallText(localize("school_choice.has_comment", "Application has comment attached")), 1, row++);
				}
				if (showPlacement) {
					table.mergeCells(1, row, table.getColumns(), row);
					if (useStyleNames()) {
						table.setCellpaddingLeft(1, row, 12);
					}
					table.add(getSmallErrorText("+ "), 1, row);
					table.add(getSmallText(localize("school_choice.has_main_group_placement", "Student is placed in main group")), 1, row++);
				}
			}
		}

		if (showStatistics) {
			try {
				int firstApplSize = getSchoolChoiceHome().getCount(getSchoolID(), getSchoolSeasonID(), -1, new int[] { 1}, validStatuses, "");
				int secondApplSize = getSchoolChoiceHome().getCount(getSchoolID(), getSchoolSeasonID(), -1, new int[] { 2}, validStatuses, "");
				int thirdApplSize = getSchoolChoiceHome().getCount(getSchoolID(), getSchoolSeasonID(), -1, new int[] { 3}, validStatuses, "");

				String[] allStatuses = new String[] { SchoolChoiceBMPBean.CASE_STATUS_PRELIMINARY, SchoolChoiceBMPBean.CASE_STATUS_MOVED, SchoolChoiceBMPBean.CASE_STATUS_PLACED};
				String[] handledStatuses = new String[] { SchoolChoiceBMPBean.CASE_STATUS_PLACED};

				int unHandledMoves = getBusiness().getSchoolChoiceBusiness().getSchoolChoiceHome().getNumberOfUnHandledMoves(getSchoolSeasonID());
				int handledMoves = getBusiness().getSchoolChoiceBusiness().getSchoolChoiceHome().getNumberOfHandledMoves(getSchoolSeasonID());

				int allApplSize = getSchoolChoiceHome().getCount(getSchoolID(), getSchoolSeasonID(), -1, new int[] {}, allStatuses, "");
				int handledApplSize = getSchoolChoiceHome().getCount(getSchoolID(), getSchoolSeasonID(), -1, new int[] {}, handledStatuses, "");

				int allApplSizeNewCitizens = getSchoolChoiceHome().getCountOutsideInterval(getSchoolID(), getSchoolSeasonID(), -1, new int[] {}, allStatuses, "", from, to);
				int handledApplSizeNewCitizens = getSchoolChoiceHome().getCountOutsideInterval(getSchoolID(), getSchoolSeasonID(), -1, new int[] {}, handledStatuses, "", from, to);
				int firstApplSizeNewCitizens = getSchoolChoiceHome().getCountOutsideInterval(getSchoolID(), getSchoolSeasonID(), -1, new int[] { 1}, validStatuses, "", from, to);
				int secondApplSizeNewCitizens = getSchoolChoiceHome().getCountOutsideInterval(getSchoolID(), getSchoolSeasonID(), -1, new int[] { 2}, validStatuses, "", from, to);
				int thirdApplSizeNewCitizens = getSchoolChoiceHome().getCountOutsideInterval(getSchoolID(), getSchoolSeasonID(), -1, new int[] { 3}, validStatuses, "", from, to);

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

				statTable.add(getSmallText("&nbsp;"), 1, sRow);
				statTable.add(getSmallText("&nbsp;"), 2, sRow++);

				if (currentSeason) {
					statTable.add(getSmallText(localize("applications_all_new_citizens", "All applications new citizens") + ":"), 1, sRow);
					statTable.add(getSmallText("" + allApplSizeNewCitizens), 2, sRow++);

					statTable.add(getSmallText(localize("applications_handled_new_citizens", "Handled applications new citizens") + ":"), 1, sRow);
					statTable.add(getSmallText("" + handledApplSizeNewCitizens), 2, sRow++);

					statTable.add(getSmallText(localize("applications_on_first_choice_new_citizens", "Applications on first choice new citizens") + ":"), 1, sRow);
					statTable.add(getSmallText("" + firstApplSizeNewCitizens), 2, sRow++);

					statTable.add(getSmallText(localize("applications_on_second_choice_new_citizens", "Applications on second choice new citizens") + ":"), 1, sRow);
					statTable.add(getSmallText("" + secondApplSizeNewCitizens), 2, sRow++);

					statTable.add(getSmallText(localize("applications_on_third_choice_new_citizens", "Applications on third choice new citizens") + ":"), 1, sRow);
					statTable.add(getSmallText("" + thirdApplSizeNewCitizens), 2, sRow++);

					statTable.add(getSmallText("&nbsp;"), 1, sRow);
					statTable.add(getSmallText("&nbsp;"), 2, sRow++);
				}

				table.mergeCells(1, row, table.getColumns(), row);
				table.add(statTable, 1, row);
				++row;
			}
			catch (Exception e) {
				table.add(getSmallText(localize("error_in_statistics", "Error in statistics")), 1, row);
				++row;
				e.printStackTrace(System.err);
			}
		}

		if (showStudentTable && getSchoolClassID() != -1) {

			GenericButton selectAll = (GenericButton) getStyledInterface(new GenericButton());
			selectAll.setValue(localize("school.select_all", "Select all"));
			selectAll.setToCheckOnClick(checkBox, true, false);

			GenericButton deselectAll = (GenericButton) getStyledInterface(new GenericButton());
			deselectAll.setValue(localize("school.deselect_all", "Deselect all"));
			deselectAll.setToCheckOnClick(checkBox, false);

			table.add(selectAll, 1, row);
			table.add(Text.getNonBrakingSpace(), 1, row);
			table.add(deselectAll, 1, row);
			table.mergeCells(1, row, table.getColumns(), row);
			table.setAlignment(1, row, Table.HORIZONTAL_ALIGN_RIGHT);
			table.setRowColor(row, "#FFFFFF");
			if (useStyleNames()) {
				table.setCellpaddingLeft(1, row, 12);
				table.setCellpaddingRight(1, row, 12);
			}
		}
		table.setColumnAlignment(4, Table.HORIZONTAL_ALIGN_CENTER);
		if (useStyleNames()) {
			table.setRowStyleClass(headerRow, getHeaderRowClass());
		}
		else {
			table.setRowColor(headerRow, getHeaderColor());
		}
		if (headerRow != 1) table.mergeCells(1, 1, table.getColumns(), 1);

		if (!this.multibleSchools) {
			table.mergeCells(1, 1, table.getColumns(), 1);
			table.setAlignment(1, 1, Table.HORIZONTAL_ALIGN_RIGHT);
			Image xls = getBundle().getImage("shared/xls.gif");
			xls.setToolTip(localize("school.list_new_students", "List new students"));
			Link excelLink = getChoicesXLSLink(SchoolChoiceWriter.class, xls);
			excelLink.addParameter(SchoolChoiceWriter.prmGrade, getSchoolYearID());
			table.add(excelLink, 1, 1);
		}

		return table;
	}

	private Table getStudentTable(IWContext iwc) throws RemoteException {
		Table table = new Table();
		table.setWidth(getWidth());
		table.setCellpadding(getCellpadding());
		table.setCellspacing(getCellspacing());
		SchoolYear schoolYear = getBusiness().getSchoolBusiness().getSchoolYear(new Integer(_previousSchoolYearID));
		int schoolAge = -1;
		if (schoolYear != null) schoolAge = schoolYear.getSchoolYearAge();

		int row = 1;
		int column = 1;
		table.add(getSmallHeader(localize("school.name", "Name")), column++, row);
		table.add(getSmallHeader(localize("school.personal_id", "Personal ID")), column++, row);
		if (!useStyleNames()) {
			table.add(getSmallHeader(localize("school.gender", "Gender")), column++, row);
		}
		table.add(getSmallHeader(localize("school.address", "Address")), column++, row);
		if (schoolAge >= 12) table.add(getSmallHeader(localize("school.language", "Language")), column++, row);
		if (useStyleNames()) {
			table.setCellpaddingLeft(1, row, 12);
			table.setCellpaddingRight(table.getColumns(), row, 12);
		}
		row++;

		User student;
		Address address;
		Link link;
		SchoolClassMember studentMember;
		CheckBox checkBox = new CheckBox();
		int numberOfStudents = 0;
		boolean hasPlacement = false;
		boolean showPlacement = false;

		List formerStudents = new ArrayList();
		if (_previousSchoolClassID != -1)
			formerStudents = new ArrayList(getBusiness().getSchoolBusiness().findStudentsInClassAndYear(_previousSchoolClassID, _previousSchoolYearID));
		else
			formerStudents = new ArrayList(getBusiness().getSchoolBusiness().findStudentsBySchoolAndSeasonAndYear(getSchoolID(), _previousSchoolSeasonID, _previousSchoolYearID));

		if (!formerStudents.isEmpty()) {
			numberOfStudents = formerStudents.size();
			Map studentMap = getBusiness().getStudentList(formerStudents);

			Map studentChoices = getBusiness().getStudentChoices(formerStudents, getSchoolSeasonID());
			Collections.sort(formerStudents, new SchoolClassMemberComparator(sortStudentsBy, iwc.getCurrentLocale(), getUserBusiness(iwc), studentMap));
			Iterator iter = formerStudents.iterator();
			while (iter.hasNext()) {
				column = 1;
				studentMember = (SchoolClassMember) iter.next();
				student = (User) studentMap.get(new Integer(studentMember.getClassMemberId()));
				address = getUserBusiness(iwc).getUserAddress1(((Integer) student.getPrimaryKey()).intValue());
				checkBox = getCheckBox(getSession().getParameterStudentID(), String.valueOf(((Integer) student.getPrimaryKey()).intValue()));
				if (getBusiness().isAlreadyInSchool(studentMember.getClassMemberId(), getSession().getSchoolID(), getSession().getSchoolSeasonID())) {
					hasPlacement = true;
					if (_group != null && _group.getIsSubGroup()) {
						if (getBusiness().getSchoolBusiness().hasGroupPlacement(studentMember.getClassMemberId(), getSchoolClassID())) {
							checkBox.setDisabled(true);
						}
					}
					else {
						checkBox.setDisabled(true);
					}
				}
				else {
					hasPlacement = false;
					if (_group != null && _group.getIsSubGroup()) {
						checkBox.setDisabled(true);
					}
				}

				//String name = student.getNameLastFirst(true);
				String name = getBusiness().getUserBusiness().getNameLastFirst(student, true);
				if (iwc.getCurrentLocale().getLanguage().equalsIgnoreCase("is")) name = student.getName();

				link = getSmallLink(name);
				link.setWindowToOpen(SchoolAdminWindow.class);
				link.setParameter(SchoolAdminOverview.PARAMETER_METHOD, String.valueOf(SchoolAdminOverview.METHOD_OVERVIEW));
				link.setParameter(SchoolAdminOverview.PARAMETER_USER_ID, String.valueOf(studentMember.getClassMemberId()));

				if (studentMember.getNeedsSpecialAttention()) {
					checkBox.setDisabled(true);
					link.setParameter(SchoolAdminOverview.PARAMETER_CHOICE_ID, String.valueOf(getBusiness().getChosenSchoolID((Collection) studentChoices.get(new Integer(studentMember.getClassMemberId())))));
				}

				boolean hasChoice = getBusiness().hasChoicesForSeason(studentMember.getClassMemberId(), getSchoolSeasonID());
				boolean hasMoveChoice = getBusiness().hasMoveChoiceToOtherSchool(studentMember.getClassMemberId(), getSchoolID(), getSchoolSeasonID());

				if (useStyleNames()) {
					if (row % 2 == 0)
						table.setRowStyleClass(row, getDarkRowClass());
					else
						table.setRowStyleClass(row, getLightRowClass());
					table.setCellpaddingLeft(1, row, 12);
					table.setCellpaddingRight(table.getColumns(), row, 12);
				}

				if (hasMoveChoice) {
					table.setRowColor(row, HAS_MOVE_CHOICE_COLOR);
				}
				else if (studentMember.getSpeciallyPlaced()) {
					table.setRowColor(row, IS_SPECIALLY_PLACED_COLOR);
				}
				else if (hasChoice) {
					table.setRowColor(row, HAS_SCHOOL_CHOICE_COLOR);
				}
				else {
					if (!useStyleNames()) {
						if (row % 2 == 0)
							table.setRowColor(row, getZebraColor1());
						else
							table.setRowColor(row, getZebraColor2());
					}
				}

				if (hasPlacement) {
					showPlacement = true;
					table.add(getSmallErrorText("+"), column, row);
					table.add(getSmallText(Text.NON_BREAKING_SPACE), column, row);
				}

				table.add(link, column++, row);
				table.add(getSmallText(PersonalIDFormatter.format(student.getPersonalID(), iwc.getCurrentLocale())), column++, row);
				
				if (!useStyleNames()) {
					if (PIDChecker.getInstance().isFemale(student.getPersonalID()))
						table.add(getSmallText(localize("school.girl", "Girl")), column++, row);
					else
						table.add(getSmallText(localize("school.boy", "Boy")), column++, row);
				}
				
				if (address != null && address.getStreetAddress() != null) table.add(getSmallText(address.getStreetAddress()), column, row);
				column++;
				if (schoolAge >= 12) {
					if (studentMember.getLanguage() != null) table.add(getSmallText(localize(studentMember.getLanguage(), "")), 5, row);
					column++;
				}
				if (getSchoolClassID() != -1) {
					table.setWidth(column, "12");
					table.add(checkBox, column, row);
				}
				row++;
			}

			if (showPlacement) {
				table.setHeight(row++, 2);
				table.mergeCells(1, row, table.getColumns(), row);
				if (useStyleNames()) {
					table.setCellpaddingLeft(1, row, 12);
				}
				table.add(getSmallErrorText("+ "), 1, row);
				table.add(getSmallText(localize("school_choice.has_main_group_placement", "Student is placed in main group")), 1, row++);
			}
			//}
		}
		if (numberOfStudents > 0) {
			table.mergeCells(1, row, table.getColumns(), row);
			if (useStyleNames()) {
				table.setCellpaddingLeft(1, row, 12);
			}
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
			table.add(Text.getNonBrakingSpace(), 1, row);
			table.add(deselectAll, 1, row);
			table.mergeCells(1, row, 6, row);
			table.setAlignment(1, row, Table.HORIZONTAL_ALIGN_RIGHT);
			table.setRowColor(row, "#FFFFFF");
			if (useStyleNames()) {
				table.setCellpaddingLeft(1, row, 12);
				table.setCellpaddingRight(1, row, 12);
			}
		}
		table.setColumnAlignment(3, Table.HORIZONTAL_ALIGN_CENTER);
		if (useStyleNames()) {
			table.setRowStyleClass(1, getHeaderRowClass());
		}
		else {
			table.setRowColor(1, getHeaderColor());
		}

		return table;
	}

	private Table getNewStudentTable(IWContext iwc) throws RemoteException {
		boolean isReady = false;
		boolean isLocked = false;
		boolean isSubGroup = false;
		SchoolClass newSchoolClass = getBusiness().getSchoolBusiness().findSchoolClass(new Integer(getSchoolClassID()));
		if (newSchoolClass != null) {
			isReady = newSchoolClass.getReady();
			isLocked = newSchoolClass.getLocked();
			isSubGroup = newSchoolClass.getIsSubGroup();
		}

		Table table = new Table();
		table.setWidth(getWidth());
		table.setCellpadding(getCellpadding());
		table.setCellspacing(getCellspacing());
		table.setColumns(8);
		int column = 5;
		if (useStyleNames()) {
			table.setColumns(7);
			column = 4;
		}
		table.setWidth(column++, "12");
		table.setWidth(column++, "12");
		table.setWidth(column++, "12");
		table.setWidth(column++, "12");
		int row = 1;
		column = 1;

		table.add(getSmallHeader(localize("school.name", "Name")), column++, row);
		table.add(getSmallHeader(localize("school.personal_id", "Personal ID")), column++, row);
		if (!useStyleNames()) {
			table.add(getSmallHeader(localize("school.gender", "Gender")), column++, row);
		}
		table.add(getSmallHeader(localize("school.address", "Address")), column++, row);
		table.add(new HiddenInput(PARAMETER_APPLICANT_ID, "-1"), column++, row);
		table.add(new HiddenInput(PARAMETER_METHOD, "0"), column++, row);
		if (useStyleNames()) {
			table.setCellpaddingLeft(1, row, 12);
			table.setCellpaddingRight(table.getColumns(), row, 12);
		}

		Layer layerE = new Layer(Layer.DIV);
		Layer layerD = new Layer(Layer.DIV);
		layerE.add(getSmallHeader(localize("school.placing_offer_alphabet_letter", "E")));
		layerD.add(getSmallHeader(localize("school.placing_confirmed_alphabet_letter", "D")));
		layerE.setToolTip(localize("school.placing_offer_tool_tip", "Tool tip E"));
		layerD.setToolTip(localize("school.placing_confirmed_tool_tip", "Tool tip D"));

		table.add(layerE, column++, row);
		table.add(layerD, column++, row++);

		User student;
		Address address;
		SchoolClassMember studentMember;
		SubmitButton delete;
		Link move;
		Link link;
		int numberOfStudents = 0;
		boolean hasChoice = false;
		boolean hasMoveChoice = false;
		boolean isSpeciallyPlaced = false;
		boolean hasComment = false;
		boolean showComment = false;

		List formerStudents = null;
		if (!isSubGroup) {
			formerStudents = new ArrayList(getBusiness().getSchoolBusiness().findStudentsInClassAndYear(getSchoolClassID(), getSchoolYearID()));
		}
		else {
			formerStudents = new ArrayList(getBusiness().getSchoolBusiness().findSubGroupPlacements(newSchoolClass));
		}

		if (!formerStudents.isEmpty()) {
			numberOfStudents = formerStudents.size();
			Map studentMap = getBusiness().getStudentList(formerStudents);
			Collections.sort(formerStudents, new SchoolClassMemberComparator(sortStudentsBy, iwc.getCurrentLocale(), getUserBusiness(iwc), studentMap));
			Iterator iter = formerStudents.iterator();
			while (iter.hasNext()) {
				column = 1;
				studentMember = (SchoolClassMember) iter.next();
				student = (User) studentMap.get(new Integer(studentMember.getClassMemberId()));
				address = getUserBusiness(iwc).getUserAddress1(((Integer) student.getPrimaryKey()).intValue());
				hasChoice = getBusiness().hasChoiceToThisSchool(studentMember.getClassMemberId(), getSchoolID(), getSchoolSeasonID());
				hasMoveChoice = getBusiness().hasMoveChoiceToOtherSchool(studentMember.getClassMemberId(), getSchoolID(), getSchoolSeasonID());
				isSpeciallyPlaced = studentMember.getSpeciallyPlaced();

				hasComment = studentMember.getNotes() != null;

				delete = new SubmitButton(getDeleteIcon(localize("school.delete_from_group", "Click to remove student from group")), "delete_student_" + String.valueOf(new Integer(studentMember.getClassMemberId())));
				delete.setDescription(localize("school.delete_from_group", "Click to remove student from group"));
				delete.setValueOnClick(PARAMETER_APPLICANT_ID, String.valueOf(studentMember.getClassMemberId()));
				delete.setValueOnClick(PARAMETER_METHOD, String.valueOf(ACTION_DELETE));
				delete.setSubmitConfirm(localize("school.confirm_student_delete", "Are you sure you want to remove the student from this class?"));
				move = new Link(getEditIcon(localize("school.move_to_another_group", "Move this student to another group")));
				move.setWindowToOpen(SchoolAdminWindow.class);
				move.setParameter(SchoolAdminOverview.PARAMETER_METHOD, String.valueOf(SchoolAdminOverview.METHOD_MOVE_GROUP));
				move.setParameter(getSchoolCommuneSession(iwc).getParameterSchoolClassID(), String.valueOf(getSchoolClassID()));
				move.setParameter(SchoolAdminOverview.PARAMETER_USER_ID, String.valueOf(studentMember.getClassMemberId()));
				move.setParameter(SchoolAdminOverview.PARAMETER_PAGE_ID, String.valueOf(getParentPage().getPageID()));

				//String name = student.getNameLastFirst(true);
				String name = getBusiness().getUserBusiness().getNameLastFirst(student, true);
				if (iwc.getCurrentLocale().getLanguage().equalsIgnoreCase("is")) name = student.getName();

				if (useStyleNames()) {
					if (row % 2 == 0)
						table.setRowStyleClass(row, getDarkRowClass());
					else
						table.setRowStyleClass(row, getLightRowClass());
					table.setCellpaddingLeft(1, row, 12);
					table.setCellpaddingRight(table.getColumns(), row, 12);
				}

				if (hasMoveChoice) {
					table.setRowColor(row, HAS_MOVE_CHOICE_COLOR);
				}
				else if (isSpeciallyPlaced) {
					table.setRowColor(row, IS_SPECIALLY_PLACED_COLOR);
				}
				else if (hasChoice) {
					table.setRowColor(row, HAS_SCHOOL_CHOICE_COLOR);
				}
				else {
					if (!useStyleNames()) {
						if (row % 2 == 0)
							table.setRowColor(row, getZebraColor1());
						else
							table.setRowColor(row, getZebraColor2());
					}
				}

				if (hasComment) {
					showComment = true;
					table.add(getSmallErrorText("*"), column, row);
					table.add(getSmallText(Text.NON_BREAKING_SPACE), column, row);
				}

				link = getSmallLink(name);
				link.setWindowToOpen(SchoolAdminWindow.class);
				link.setParameter(SchoolAdminOverview.PARAMETER_METHOD, String.valueOf(SchoolAdminOverview.METHOD_OVERVIEW));
				link.setParameter(SchoolAdminOverview.PARAMETER_USER_ID, String.valueOf(studentMember.getClassMemberId()));
				link.setParameter(SchoolAdminOverview.PARAMETER_SHOW_ONLY_OVERVIEW, "true");
				table.add(link, column++, row);
				table.add(getSmallText(PersonalIDFormatter.format(student.getPersonalID(), iwc.getCurrentLocale())), column++, row);

				if (!useStyleNames()) {
					if (PIDChecker.getInstance().isFemale(student.getPersonalID()))
						table.add(getSmallText(localize("school.girl", "Girl")), column++, row);
					else
						table.add(getSmallText(localize("school.boy", "Boy")), column++, row);
				}
				
				if (address != null && address.getStreetAddress() != null) table.add(getSmallText(address.getStreetAddress()), column, row);
				column++;
				table.add(move, column++, row);
				table.add(delete, column++, row);

				if (hasChoice) {
					SchoolChoice choice = getBusiness().getSchoolChoiceBusiness().findByStudentAndSchoolAndSeason(studentMember.getClassMemberId(), session.getSchoolID(), session.getSchoolSeasonID());
					if (choice != null) {
						table.setAlignment(7, row, Table.HORIZONTAL_ALIGN_CENTER);
						table.setAlignment(8, row, Table.HORIZONTAL_ALIGN_CENTER);
						if (choice.getHasReceivedPlacementMessage())
							table.add(getSmallText(localize("school_choice.YES", "YES")), column++, row);
						else
							table.add(getSmallText(localize("school_choice.NO", "NO")), column++, row);

						if (choice.getHasReceivedConfirmationMessage())
							table.add(getSmallText(localize("school_choice.YES", "YES")), column++, row);
						else
							table.add(getSmallText(localize("school_choice.NO", "NO")), column++, row);
					}
				}
				row++;
			}

			if (showComment) {
				table.setHeight(row++, 2);
				table.mergeCells(1, row, table.getColumns(), row);
				if (useStyleNames()) {
					table.setCellpaddingLeft(1, row, 12);
				}
				table.add(getSmallErrorText("* "), 1, row);
				table.add(getSmallText(localize("school.has_notes", "Placment has comment attached")), 1, row++);
			}
		}

		if (numberOfStudents > 0) {
			table.mergeCells(1, row, table.getColumns(), row);
			if (useStyleNames()) {
				table.setCellpaddingLeft(1, row, 12);
			}
			table.add(getSmallHeader(localize("school.number_of_students", "Number of students") + ": " + String.valueOf(numberOfStudents)), 1, row++);
		}

		if (isReady && newSchoolClass.getReadyDate() != null) {
			table.setHeight(row++, 3);

			IWTimestamp readyDate = new IWTimestamp(newSchoolClass.getReadyDate());
			table.mergeCells(1, row, table.getColumns(), row);
			if (useStyleNames()) {
				table.setCellpaddingLeft(1, row, 12);
			}
			table.add(getSmallHeader(localize("school.mark_ready_when", "School group was marked as ready") + ": " + readyDate.getLocaleDateAndTime(iwc.getCurrentLocale(), IWTimestamp.SHORT, IWTimestamp.SHORT)), 1, row++);
			if (isLocked && newSchoolClass.getLockedDate() != null) {
				IWTimestamp lockedDate = new IWTimestamp(newSchoolClass.getLockedDate());
				table.mergeCells(1, row, table.getColumns(), row);
				table.add(getSmallHeader(localize("school.mark_locked_when", "School group was marked as locked") + ": " + lockedDate.getLocaleDateAndTime(iwc.getCurrentLocale(), IWTimestamp.SHORT, IWTimestamp.SHORT)), 1, row++);
			}
		}

		SubmitButton back = (SubmitButton) getStyledInterface(new SubmitButton(localize("school.back", "Back")));
		back.setValueOnClick(PARAMETER_ACTION, String.valueOf(ACTION_MANAGE));

		String buttonLabel = "";
		if (isReady)
			buttonLabel = localize("school.class_locked", "Class locked");
		else
			buttonLabel = localize("school.class_ready", "Class ready");

		table.add(back, 1, row);
		if (useStyleNames()) {
			table.setCellpaddingLeft(1, row, 12);
		}

		if (!isSubGroup) {
			table.add(Text.getNonBrakingSpace(), 1, row);

			GenericButton groupReady = (GenericButton) getStyledInterface(new GenericButton("finalize", buttonLabel));
			groupReady.setWindowToOpen(SchoolAdminWindow.class);
			groupReady.addParameterToWindow(SchoolAdminOverview.PARAMETER_METHOD, String.valueOf(SchoolAdminOverview.METHOD_FINALIZE_GROUP));
			groupReady.addParameterToWindow(SchoolAdminOverview.PARAMETER_PAGE_ID, String.valueOf(getParentPage().getPageID()));

			if (isReady) {
				if (!getBusiness().canMarkSchoolClass(newSchoolClass, "mark_locked_date") && !_useForTesting) {
					groupReady.setDisabled(true);
				}
			}
			else {
				if (!getBusiness().canMarkSchoolClass(newSchoolClass, "mark_ready_date") && !_useForTesting) {
					groupReady.setDisabled(true);
				}
			}

			table.add(groupReady, 1, row);
		}
		table.mergeCells(1, row, table.getColumns(), row);
		table.setAlignment(1, row, Table.HORIZONTAL_ALIGN_RIGHT);
		table.setColumnAlignment(3, Table.HORIZONTAL_ALIGN_CENTER);
		table.setColumnAlignment(7, Table.HORIZONTAL_ALIGN_CENTER);
		table.setColumnAlignment(8, Table.HORIZONTAL_ALIGN_CENTER);
		if (useStyleNames()) {
			table.setRowStyleClass(row, getHeaderRowClass());
		}
		else {
			table.setRowColor(1, getHeaderColor());
		}
		table.setRowColor(row, "#FFFFFF");

		return table;
	}

	protected Table getPreviousHeader(Collection classes) {
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

	protected DropdownMenu getPreviousSchoolClasses(Collection classes) {
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

		if (_previousSchoolClassID != -1) menu.setSelectedElement(_previousSchoolClassID);

		return (DropdownMenu) getStyledInterface(menu);
	}

	protected Table getSearchAndSortTable() throws RemoteException {
		Table table = new Table(4, 1);
		table.setCellpadding(0);
		table.setCellspacing(0);
		table.setWidth("100%");

		SchoolYear schoolYear = null;
		if (getSchoolYearID() != -1) schoolYear = getBusiness().getSchoolBusiness().getSchoolYear(new Integer(getSchoolYearID()));
		int yearAge = -1;
		if (schoolYear != null) yearAge = schoolYear.getSchoolYearAge();

		if (searchEnabled) {
			table.setAlignment(4, 1, Table.HORIZONTAL_ALIGN_RIGHT);
			table.add(getSmallHeader(localize("school.search_for", "Search for") + ":" + Text.NON_BREAKING_SPACE), 4, 1);

			TextInput tiSearch = (TextInput) getStyledInterface(new TextInput(PARAMETER_SEARCH, searchString));
			tiSearch.setLength(16);
			table.add(tiSearch, 4, 1);

			SubmitButton submit = (SubmitButton) getStyledInterface(new SubmitButton(localize("school.search", "Search")));
			table.add(Text.getNonBrakingSpace(), 4, 1);
			table.add(submit, 4, 1);

		}

		table.add(getSmallHeader(localize("school.sort_by", "Sort by") + ":" + Text.NON_BREAKING_SPACE), 1, 1);

		DropdownMenu menu = (DropdownMenu) getStyledInterface(new DropdownMenu(PARAMETER_SORT));
		menu.addMenuElement(SchoolChoiceComparator.NAME_SORT, localize("school.sort_name", "Name"));
		menu.addMenuElement(SchoolChoiceComparator.PERSONAL_ID_SORT, localize("school.sort_personal_id", "Personal ID"));
		menu.addMenuElement(SchoolChoiceComparator.ADDRESS_SORT, localize("school.sort_address", "Address"));
		menu.addMenuElement(SchoolChoiceComparator.GENDER_SORT, localize("school.sort_gender", "Gender"));

		if (action != ACTION_SAVE && yearAge >= 12) menu.addMenuElement(SchoolChoiceComparator.LANGUAGE_SORT, localize("school.sort_language", "Language"));
		menu.addMenuElement(SchoolChoiceComparator.CREATED_SORT, localize("school.sort_created", "Created"));
		menu.setSelectedElement(sortChoicesBy);
		menu.setToSubmit();
		table.add(menu, 2, 1);
		table.add("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;", 2, 1);
		if (action != ACTION_SAVE) {
			DropdownMenu menuPlaced = (DropdownMenu) getStyledInterface(new DropdownMenu(PARAMETER_SORT_PLACED));
			menuPlaced.addMenuElement(-1, localize("school.sort_all", "All"));
			menuPlaced.addMenuElement(SchoolChoiceComparator.PLACED_SORT, localize("school.sort_placed", "Placed"));
			menuPlaced.addMenuElement(SchoolChoiceComparator.UNPLACED_SORT, localize("school.sort_unplaced", "Unplaced"));
			menuPlaced.setSelectedElement(sortPlacedUnplacedBy);
			menuPlaced.setToSubmit();
			table.add(menuPlaced, 2, 1);
			if (showStudentTable && getSchoolClassID() != -1) {
				menuPlaced.setDisabled(true);
				menuPlaced.setSelectedElement(-1);
			}
		}

		table.setColumnAlignment(1, Table.HORIZONTAL_ALIGN_LEFT);
		table.setColumnAlignment(4, Table.HORIZONTAL_ALIGN_RIGHT);
		table.setNoWrap(1, 1);
		table.setNoWrap(4, 1);
		table.setWidth(1, 1, "100");

		return table;
	}

	private void saveClass(IWContext iwc) throws RemoteException {
		String[] applications = iwc.getParameterValues(PARAMETER_APPLICANT_ID);
		String[] students = iwc.getParameterValues(getSession().getParameterStudentID());

		IWTimestamp stamp = new IWTimestamp();
		int userID = ((Integer) iwc.getCurrentUser().getPrimaryKey()).intValue();
		SchoolClassMember member;
		SchoolChoice choice;
		SchoolSeason season = getBusiness().getSchoolChoiceBusiness().getSchoolBusiness().getSchoolSeason(new Integer(getSchoolSeasonID()));
		if (season != null) {
			stamp = new IWTimestamp(season.getSchoolSeasonStart());
		}
		SchoolSeason previousSeason = getBusiness().getPreviousSchoolSeason(getSchoolSeasonID());
		getBusiness().resetSchoolClassStatus(getSchoolClassID());

		if (applications != null && applications.length > 0) {
			for (int a = 0; a < applications.length; a++) {
				int schoolTypeID = getSchoolBusiness(iwc).getSchoolTypeIdFromSchoolClass(getSchoolClassID());
				choice = getBusiness().getSchoolChoiceBusiness().groupPlaceAction(new Integer(applications[a]), iwc.getCurrentUser());
				IWTimestamp placementDate = null;
				if (choice != null) {
					if (choice.getPlacementDate() != null) {
						placementDate = new IWTimestamp(choice.getPlacementDate());
					}
					else {
						placementDate = new IWTimestamp(stamp);
					}
					member = getBusiness().getSchoolBusiness().storeSchoolClassMember(choice.getChildId(), getSchoolClassID(), getSchoolYearID(), schoolTypeID, placementDate.getTimestamp(), null, userID, choice.getMessage(), choice.getLanguageChoice());
					if (member != null) {
						getBusiness().importStudentInformationToNewClass(member, previousSeason);
					}
				}
			}
		}

		if (students != null && students.length > 0) {
			for (int a = 0; a < students.length; a++) {
				int schoolTypeID = getSchoolBusiness(iwc).getSchoolTypeIdFromSchoolClass(getSchoolClassID());
				member = getBusiness().getSchoolBusiness().storeSchoolClassMember(Integer.parseInt(students[a]), getSchoolClassID(), getSchoolYearID(), schoolTypeID, stamp.getTimestamp(), userID);

				if (member != null) getBusiness().importStudentInformationToNewClass(member, previousSeason);
			}
		}
	}

	private Link getChoicesXLSLink(Class classToUse, Image image) throws RemoteException {
		Link link = new Link(image);
		link.setWindow(getFileWindow());
		link.addParameter(MediaWritable.PRM_WRITABLE_CLASS, IWMainApplication.getEncryptedClassName(classToUse));
		link.addParameter(SchoolChoiceWriter.prmSchoolId, getSession().getSchoolID());
		link.addParameter(SchoolChoiceWriter.prmSeasonId, getSession().getSchoolSeasonID());
		return link;
	}

	private void delete(IWContext iwc) throws RemoteException {
		String student = iwc.getParameter(PARAMETER_APPLICANT_ID);
		if (student != null && student.length() > 0) {
			getBusiness().getSchoolBusiness().removeSchoolClassMemberFromClass(Integer.parseInt(student), getSchoolClassID());
			SchoolClass group = getBusiness().getSchoolBusiness().findSchoolClass(new Integer(getSchoolClassID()));
			if (!group.getIsSubGroup()) {
				SchoolChoice choice = getBusiness().getSchoolChoiceBusiness().findByStudentAndSchoolAndSeason(Integer.parseInt(student), getSchoolID(), getSchoolSeasonID());
				getBusiness().setNeedsSpecialAttention(Integer.parseInt(student), getBusiness().getPreviousSchoolSeasonID(getSchoolSeasonID()), false);
				if (choice != null) {
					getBusiness().getSchoolChoiceBusiness().setAsPreliminary(choice, iwc.getCurrentUser());
				}
			}
		}
	}

	private void finalizeGroup() throws RemoteException {
		int schoolClassID = getSchoolClassID();
		SchoolClass schoolClass = getBusiness().getSchoolBusiness().findSchoolClass(new Integer(schoolClassID));
		if (schoolClass != null) {
			if (schoolClass.getReady()) {
				getBusiness().markSchoolClassLocked(schoolClass);
				getBusiness().finalizeGroup(schoolClass, localize("school.finalize_subject", ""), localize("school.finalize_body", ""), true);
			}
			else {
				getBusiness().markSchoolClassReady(schoolClass);
				getBusiness().finalizeGroup(schoolClass, localize("school.students_put_in_class_subject", ""), localize("school.students_put_in_class_body", ""), false);
			}
		}
	}

	private void validateSchoolClass(Collection previousClasses) throws RemoteException {
		SchoolClass previousClass = getBusiness().getSchoolBusiness().findSchoolClass(new Integer(_previousSchoolClassID));
		if (previousClass != null && !previousClasses.contains(previousClass)) _previousSchoolClassID = -1;
	}

	private boolean livesOutsideDefaultCommune(IWContext iwc, User applicant) {
		boolean showEraseButton = false;
		try {

			int homeComID = -1;

			// Get default Commune primary key
			CommuneHome cHome = (CommuneHome) IDOLookup.getHome(Commune.class);
			Commune defCom = cHome.findDefaultCommune();
			Integer defComPK = (Integer) defCom.getPrimaryKey();

			// Get applicants home Commune primary key
			Address applicantAddr;
			applicantAddr = getUserBusiness(iwc).getUsersMainAddress(applicant);

			if (applicantAddr != null) {
				homeComID = applicantAddr.getCommuneID(); // return -1 if null
			}

			// If user doesn't live in default commune - return true
			if (homeComID != -1 && defComPK.intValue() != homeComID) {
				showEraseButton = true;
				//logWarning("__SHOW ERASE BUTTON__");
			}

		}
		catch (Exception e) {
		}

		return showEraseButton;
	}

	/*
	 * Delete a school choice by setting it's corresponding case status to
	 * deleted(UPPS)
	 */
	private void deleteSchoolChoice(IWContext iwc, int choiceID) throws RemoteException {
		try {
			Case theCase = getSchoolChoiceBusiness(iwc).getCase(choiceID);
			if (theCase != null) {
				theCase.setStatus(SchoolChoiceBMPBean.CASE_STATUS_CANCELLED);
				theCase.store();
			}
		}
		catch (FinderException e) {
			log(e);
		}
	}


	private SchoolChoiceHome getSchoolChoiceHome() throws RemoteException {
		return (SchoolChoiceHome) com.idega.data.IDOLookup.getHome(SchoolChoice.class);
	}

	private SchoolBusiness getSchoolBusiness(IWContext iwc) throws RemoteException {
		return (SchoolBusiness) IBOLookup.getServiceInstance(iwc, SchoolBusiness.class);
	}

	private SchoolChoiceBusiness getSchoolChoiceBusiness(IWContext iwc) throws RemoteException {
		return (SchoolChoiceBusiness) IBOLookup.getServiceInstance(iwc, SchoolChoiceBusiness.class);
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

	/**
	 * Turns on/off view of radiobuttons for showing BUN administrated shools or
	 * not
	 * 
	 * @param show
	 */
	public void setShowBunRadioButtons(boolean show) {
		this.showBunRadioButtons = show;
	}

	/**
	 * @param useForTesting
	 *          The useForTesting to set.
	 */
	public void setUseForTesting(boolean useForTesting) {
		this._useForTesting = useForTesting;
	}

	/**
	 * @param languageAge
	 *          The languageAge to set.
	 */
	public void setLanguageAge(int languageAge) {
		this._languageAge = languageAge;
	}
	
	public void setShowMessageTextButton(boolean show) {
		showMessageTextButton = show;
	}
}