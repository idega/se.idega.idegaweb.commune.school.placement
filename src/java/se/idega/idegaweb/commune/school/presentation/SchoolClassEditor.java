package se.idega.idegaweb.commune.school.presentation;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.ejb.FinderException;

import se.idega.idegaweb.commune.school.data.SchoolChoice;
import se.idega.idegaweb.commune.school.event.SchoolEventListener;

import com.idega.block.school.data.School;
import com.idega.block.school.data.SchoolClass;
import com.idega.block.school.data.SchoolYear;
import com.idega.business.IBOLookup;
import com.idega.core.data.Address;
import com.idega.presentation.IWContext;
import com.idega.presentation.Table;
import com.idega.presentation.text.Break;
import com.idega.presentation.ui.CheckBox;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.GenericButton;
import com.idega.presentation.ui.SubmitButton;
import com.idega.user.business.UserBusiness;
import com.idega.user.data.User;
import com.idega.util.GenericUserComparator;
import com.idega.util.PersonalIDFormatter;

/**
 * @author Laddi
 */
public class SchoolClassEditor extends SchoolCommuneBlock {
	
	private final String PARAMETER_APPLICANT_ID = "sch_applicant_id";

	public void init(IWContext iwc) throws RemoteException {
		drawForm(iwc);
	}
	
	private void drawForm(IWContext iwc) throws RemoteException {
		Form form = new Form();
		form.setEventListener(SchoolEventListener.class);
		
		form.add(getSchoolSeasons());
		form.add(getSchoolYears());
		form.add(getSchoolClasses());
		
		if ( getSchoolClassID() != -1 ) {
			form.add(getApplicationTable(iwc));
			form.add(new Break(2));
			form.add(getStudentTable(iwc));
		}
		
		add(form);
	}
	
	private Table getApplicationTable(IWContext iwc) throws RemoteException {
		Table table = new Table();
		table.setWidth(getWidth());
		table.setCellpadding(1);
		table.setCellspacing(0);

		int row = 1;
		table.add(getHeader(localize("school.name", "Name")),1,row);
		table.add(getHeader(localize("school.personal_id", "Personal ID")),2,row);
		table.add(getHeader(localize("school.address", "Address")),3,row);
		table.add(getHeader(localize("school.from_school", "From School")),4,row++);
		
		CheckBox checkBox = new CheckBox();
		SchoolYear year = getBusiness().getSchoolYear(getSchoolYearID());
		Collection applicants = getBusiness().getSchoolChoiceBusiness().getApplicantsForSchoolAndSeasonAndGrade(getSchoolID(), getSchoolSeasonID(), year.getSchoolYearAge()-1);
		if ( !applicants.isEmpty() ) {
			SchoolChoice choice;
			School school;
			User applicant;
			Address address;
			
			Iterator iter = applicants.iterator();
			while (iter.hasNext()) {
				choice = (SchoolChoice) iter.next();
				applicant = getUserBusiness(iwc).getUser(choice.getChildId());
				address = getUserBusiness(iwc).getUserAddress1(choice.getChildId());
				school = getBusiness().getSchoolBusiness().getSchool(new Integer(choice.getCurrentSchoolId()));
				checkBox = new CheckBox(PARAMETER_APPLICANT_ID,String.valueOf(choice.getChildId()));
				checkBox.setWidth("12");
				checkBox.setHeight("12");
				
				String name = applicant.getNameLastFirst(true);
				if ( iwc.getCurrentLocale().getLanguage().equalsIgnoreCase("is") )
					name = applicant.getName();
				
				table.add(getSmallText(name),1,row);
				table.add(getSmallText(PersonalIDFormatter.format(applicant.getPersonalID(),iwc.getCurrentLocale())),2,row);
				if ( address != null && address.getStreetAddress() != null )
					table.add(getSmallText(address.getStreetAddress()),3,row);
				if ( school != null )
					table.add(getSmallText(school.getName()),4,row);
				table.add(checkBox,5,row++);
			}	
		}
		
		GenericButton selectAll = (GenericButton) getStyledInterface(new GenericButton());
		selectAll.setValue(localize("school.select_all","Select all"));
		selectAll.setToCheckOnClick(checkBox, true, false);
		
		GenericButton deselectAll = (GenericButton) getStyledInterface(new GenericButton());
		deselectAll.setValue(localize("school.deselect_all","Deselect all"));
		deselectAll.setToCheckOnClick(checkBox, false);
		
		table.add(selectAll,1,row);
		table.add(deselectAll,1,row);
		table.mergeCells(1, row, 5, row);
		table.setAlignment(1, row, Table.HORIZONTAL_ALIGN_RIGHT);
		table.setColumnAlignment(4, Table.HORIZONTAL_ALIGN_CENTER);
		table.setHorizontalZebraColored("#EFEFEF", "#FFFFFF");
		table.setRowColor(1, "#CCCCCC");
		table.setRowColor(row, "#FFFFFF");
		
		return table;
	}
	
	private Table getStudentTable(IWContext iwc) throws RemoteException {
		Table table = new Table();
		table.setWidth(getWidth());
		table.setCellpadding(1);
		table.setCellspacing(0);
	
		int row = 1;
		table.add(getHeader(localize("school.name", "Name")),1,row);
		table.add(getHeader(localize("school.personal_id", "Personal ID")),2,row);
		table.add(getHeader(localize("school.address", "Address")),3,row);
		table.add(getHeader(localize("school.class", "Class")),4,row++);
		
		User student;
		Address address;
		CheckBox checkBox = new CheckBox();
		
		SchoolClass schoolClass = getBusiness().getSchoolClassBusiness().findSchoolClass(new Integer(getSchoolClassID()));
		List students = getBusiness().getStudentList(getBusiness().getSchoolClassMemberBusiness().findStudentsInClass(getSchoolClassID()));
		if ( !students.isEmpty() ) {
			Collections.sort(students, new GenericUserComparator(iwc.getCurrentLocale()));
			Iterator iter = students.iterator();
			while (iter.hasNext()) {
				student = (User) iter.next();
				address = getUserBusiness(iwc).getUserAddress1(((Integer)student.getPrimaryKey()).intValue());
				checkBox = new CheckBox(getSession().getParameterStudentID(),String.valueOf(((Integer)student.getPrimaryKey()).intValue()));
				checkBox.setWidth("12");
				checkBox.setHeight("12");
				
				String name = student.getNameLastFirst(true);
				if ( iwc.getCurrentLocale().getLanguage().equalsIgnoreCase("is") )
					name = student.getName();
				
				table.add(getSmallText(name),1,row);
				table.add(getSmallText(PersonalIDFormatter.format(student.getPersonalID(),iwc.getCurrentLocale())),2,row);
				if ( address != null && address.getStreetAddress() != null )
					table.add(getSmallText(address.getStreetAddress()),3,row);
				table.add(getSmallText(schoolClass.getName()),4,row);
				table.add(checkBox,5,row++);
			}	
		}
		
		GenericButton selectAll = (GenericButton) getStyledInterface(new GenericButton());
		selectAll.setValue(localize("school.select_all","Select all"));
		selectAll.setToCheckOnClick(checkBox, true, false);
		
		GenericButton deselectAll = (GenericButton) getStyledInterface(new GenericButton());
		deselectAll.setValue(localize("school.deselect_all","Deselect all"));
		deselectAll.setToCheckOnClick(checkBox, false);
		
		SubmitButton submit = (SubmitButton) getStyledInterface(new SubmitButton(localize("save","Save")));
		
		table.add(selectAll,1,row);
		table.add(deselectAll,1,row);
		table.add(submit,1,row);
		table.mergeCells(1, row, 5, row);
		table.setAlignment(1, row, Table.HORIZONTAL_ALIGN_RIGHT);
		table.setColumnAlignment(4, Table.HORIZONTAL_ALIGN_CENTER);
		table.setHorizontalZebraColored("#EFEFEF", "#FFFFFF");
		table.setRowColor(1, "#CCCCCC");
		table.setRowColor(row, "#FFFFFF");
		
		return table;
	}
	
	private UserBusiness getUserBusiness(IWContext iwc) throws RemoteException {
		return (UserBusiness) IBOLookup.getServiceInstance(iwc, UserBusiness.class);	
	}
}