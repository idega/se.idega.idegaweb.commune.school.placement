/*
 * $Id$
 * Created on Jan 12, 2006
 *
 * Copyright (C) 2006 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package se.idega.idegaweb.commune.school.presentation;

import is.idega.idegaweb.egov.application.presentation.ApplicationForm;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;
import se.idega.idegaweb.commune.care.business.CareBusiness;
import se.idega.idegaweb.commune.school.business.CommuneSchoolBusiness;
import se.idega.idegaweb.commune.school.business.CommuneSchoolSession;
import se.idega.idegaweb.commune.school.data.SchoolChoice;
import com.idega.block.school.data.School;
import com.idega.block.school.data.SchoolSeason;
import com.idega.block.school.data.SchoolYear;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.business.IBORuntimeException;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.IWUserContext;
import com.idega.presentation.IWContext;
import com.idega.presentation.Table;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.CheckBox;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.TextArea;
import com.idega.presentation.ui.TextInput;
import com.idega.user.data.User;


public class SchoolApplicationViewer extends ApplicationForm {

	private IWResourceBundle iwrb;
	
	public void present(IWContext iwc) {
		iwrb = getResourceBundle(iwc);
	}
	
	public String getCaseCode() {
		return null;
	}
	
	private void viewApplication(IWContext iwc, SchoolSeason season, boolean hasPlacing) throws RemoteException {
		Form form = new Form();

		Table table = new Table();
		table.setCellpadding(0);
		table.setCellspacing(0);
		table.setWidth(Table.HUNDRED_PERCENT);
		form.add(table);
		int row = 1;
		
		table.add(getPersonInfo(iwc, getCommuneSchoolSession(iwc).getUser()), 1, row++);
		table.setHeight(row++, 6);
		
		table.add(getHeader(iwrb.getLocalizedString("application.view_application", "View application")), 1, row++);
		table.setHeight(row++, 6);
		
		Table viewTable = new Table();
		viewTable.setColumns(2);
		table.add(viewTable, 1, row++);
		int iRow = 1;
		
		SchoolYear year = null;
		String message = null;
		Collection choices = getCommuneSchoolBusiness(iwc).getAllChoices(getCommuneSchoolSession(iwc).getUser(), season);
		
		int count = 1;
		Iterator iter = choices.iterator();
		while (iter.hasNext()) {
			SchoolChoice choice = (SchoolChoice) iter.next();
			School school = choice.getChosenSchool();
			
			if (count == 1) {
				viewTable.add(new Text(iwrb.getLocalizedString("application.first_school", "First school")), 1, iRow);
			}
			else if (count == 2) {
				viewTable.add(new Text(iwrb.getLocalizedString("application.second_school", "Second school")), 1, iRow);
			}
			else if (count == 3) {
				viewTable.add(new Text(iwrb.getLocalizedString("application.third_school", "Third school")), 1, iRow);
			}
			viewTable.add(new Text(school.getSchoolName()), 2, iRow++);

			if (year == null) {
				year = choice.getSchoolYear();
			}
			if (message == null) {
				message = choice.getMessage();
			}
		}
		
		viewTable.setHeight(iRow++, 6);
		
		if (year != null) {
			viewTable.add(new Text(iwrb.getLocalizedString("application.year", "Year")), 1, iRow);
			viewTable.add(new Text(year.getSchoolYearName()), 2, iRow++);
			viewTable.setHeight(iRow++, 6);
			viewTable.mergeCells(1, iRow, 2, iRow);
			viewTable.setBottomCellBorder(1, iRow++, 1, "#D7D7D7", "solid");
			viewTable.setHeight(iRow++, 6);
		}
		
		if (message != null) {
			viewTable.add(new Text(iwrb.getLocalizedString("application.message", "Message")), 1, iRow++);
			viewTable.mergeCells(1, iRow, 2, iRow);
			viewTable.add(new Text(message), 1, iRow++);
			viewTable.setHeight(iRow++, 6);
			viewTable.mergeCells(1, iRow, 2, iRow);
			viewTable.setBottomCellBorder(1, iRow++, 1, "#D7D7D7", "solid");
			viewTable.setHeight(iRow++, 6);
		}
		
		iRow = addChildInformation(iwc, viewTable, getCommuneSchoolSession(iwc).getUser(), iRow);
		
		boolean canDisplaySchoolImages = getCommuneSchoolBusiness(iwc).canDisplaySchoolImages(getCommuneSchoolSession(iwc).getUser());
		viewTable.mergeCells(1, iRow, table.getColumns(), iRow);
		viewTable.add(getBooleanTable(new Text(iwrb.getLocalizedString("child.can_diplay_images_info", "Can display images")), canDisplaySchoolImages), 1, iRow);
		viewTable.setWidth(1, "50%");
		viewTable.setWidth(2, "50%");

		add(form);
	}
	
	protected int addChildInformation(IWContext iwc, Table table, User child, int iRow) throws RemoteException {
		Boolean hasGrowthDeviation = getCareBusiness(iwc).hasGrowthDeviation(child);
		String growthDeviation = getCareBusiness(iwc).getGrowthDeviationDetails(child);
		Boolean hasAllergies = getCareBusiness(iwc).hasAllergies(child);
		String allergies = getCareBusiness(iwc).getAllergiesDetails(child);
		String lastCareProvider = getCareBusiness(iwc).getLastCareProvider(child);
		Boolean canContactLastProvider = getCareBusiness(iwc).canContactLastCareProvider(child);
		String otherInformation = getCareBusiness(iwc).getOtherInformation(child);
		
		if (hasGrowthDeviation != null) {
			table.mergeCells(1, iRow, table.getColumns(), iRow);
			table.add(getBooleanTable(new Text(iwrb.getLocalizedString("child.has_growth_deviation_info", "Has growth deviation")), hasGrowthDeviation.booleanValue()), 1, iRow++);

			if (growthDeviation != null) {
				table.setHeight(iRow++, 6);
				table.mergeCells(1, iRow, table.getColumns(), iRow);
				table.add(getTextAreaTable(new Text(iwrb.getLocalizedString("child.growth_deviation_details_info", "Growth deviation details")), growthDeviation), 1, iRow++);
			}
			
			table.setHeight(iRow++, 6);
			table.mergeCells(1, iRow, table.getColumns(), iRow);
			table.setBottomCellBorder(1, iRow++, 1, "#D7D7D7", "solid");
			table.setHeight(iRow++, 6);
		}
		
		if (hasAllergies != null) {
			table.mergeCells(1, iRow, table.getColumns(), iRow);
			table.add(getBooleanTable(new Text(iwrb.getLocalizedString("child.has_allergies_info", "Has allergies")), hasAllergies.booleanValue()), 1, iRow++);
	
			if (allergies != null) {
				table.setHeight(iRow++, 6);
				table.mergeCells(1, iRow, table.getColumns(), iRow);
				table.add(getTextAreaTable(new Text(iwrb.getLocalizedString("child.allergies_details_info", "Allergies details")), allergies), 1, iRow++);
			}
			
			table.setHeight(iRow++, 6);
			table.mergeCells(1, iRow, table.getColumns(), iRow);
			table.setBottomCellBorder(1, iRow++, 1, "#D7D7D7", "solid");
			table.setHeight(iRow++, 6);
		}
		
		if (lastCareProvider != null) {
			table.mergeCells(1, iRow, table.getColumns(), iRow);
			table.add(getTextInputTable(new Text(iwrb.getLocalizedString("child.last_care_provider_info", "Last care provider")), lastCareProvider), 1, iRow++);

			table.setHeight(iRow++, 6);
			table.mergeCells(1, iRow, table.getColumns(), iRow);
			table.setBottomCellBorder(1, iRow++, 1, "#D7D7D7", "solid");
			table.setHeight(iRow++, 6);
		}
		
		table.mergeCells(1, iRow, table.getColumns(), iRow);
		table.add(getBooleanTable(new Text(iwrb.getLocalizedString("child.can_contact_last_care_provider_info", "Can contact last care provider")), canContactLastProvider != null ? canContactLastProvider.booleanValue() : false), 1, iRow++);
		
		table.setHeight(iRow++, 6);
		table.mergeCells(1, iRow, table.getColumns(), iRow);
		table.setBottomCellBorder(1, iRow++, 1, "#D7D7D7", "solid");
		table.setHeight(iRow++, 6);

		if (otherInformation != null) {
			table.mergeCells(1, iRow, table.getColumns(), iRow);
			table.add(getTextAreaTable(new Text(iwrb.getLocalizedString("child.other_information_info", "Other information")), otherInformation), 1, iRow++);

			table.setHeight(iRow++, 6);
			table.mergeCells(1, iRow, table.getColumns(), iRow);
			table.setBottomCellBorder(1, iRow++, 1, "#D7D7D7", "solid");
			table.setHeight(iRow++, 6);
		}

		return iRow;
	}

	protected Table getBooleanTable(Text text, boolean checked) {
		Table table = new Table(3, 1);
		table.setCellpadding(0);
		table.setCellspacing(0);
		table.setWidth(2, 6);
		table.setVerticalAlignment(1, 1, Table.VERTICAL_ALIGN_TOP);
		
		CheckBox box = new CheckBox("void", "");
		box.setChecked(checked);
		box.setDisabled(true);
		table.add(box, 1, 1);
		table.add(text, 3, 1);
		
		return table;
	}

	protected Table getTextInputTable(Text text, String value) {
		Table table = new Table(1, 2);
		table.setCellpadding(0);
		table.setCellspacing(0);
		
		table.add(text, 1, 1);

		TextInput input = new TextInput("void");
		input.setContent(value);
		input.setDisabled(true);
		table.add(input, 1, 2);
		
		return table;
	}

	protected Table getTextAreaTable(Text text, String value) {
		Table table = new Table(1, 2);
		table.setCellpadding(0);
		table.setCellspacing(0);
		
		table.add(text, 1, 1);

		TextArea input = new TextArea("void");
		input.setContent(value);
		input.setDisabled(true);
		input.setWidth("100%");
		input.setRows(4);
		table.add(input, 1, 2);
		
		return table;
	}
	
	protected CommuneSchoolSession getCommuneSchoolSession(IWUserContext iwuc) {
		try {
			return (CommuneSchoolSession) IBOLookup.getSessionInstance(iwuc, CommuneSchoolSession.class);
		}
		catch (IBOLookupException ile) {
			throw new IBORuntimeException(ile);
		}
	}

	protected CommuneSchoolBusiness getCommuneSchoolBusiness(IWApplicationContext iwac) {
		try {
			return (CommuneSchoolBusiness) IBOLookup.getServiceInstance(iwac, CommuneSchoolBusiness.class);
		}
		catch (IBOLookupException ile) {
			throw new IBORuntimeException(ile);
		}
	}

	protected CareBusiness getCareBusiness(IWApplicationContext iwac) {
		try {
			return (CareBusiness) IBOLookup.getServiceInstance(iwac, CareBusiness.class);
		}
		catch (IBOLookupException ile) {
			throw new IBORuntimeException(ile);
		}
	}
}