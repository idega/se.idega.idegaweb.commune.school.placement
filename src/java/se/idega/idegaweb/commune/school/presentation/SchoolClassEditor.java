package se.idega.idegaweb.commune.school.presentation;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.ejb.FinderException;

import se.idega.idegaweb.commune.presentation.CommuneBlock;
import se.idega.idegaweb.commune.school.business.SchoolCommuneBusiness;
import se.idega.idegaweb.commune.school.business.SchoolCommuneSession;
import se.idega.idegaweb.commune.school.event.SchoolEventListener;

import com.idega.block.school.data.SchoolClass;
import com.idega.block.school.data.SchoolClassMember;
import com.idega.block.school.data.SchoolSeason;
import com.idega.block.school.data.SchoolYear;
import com.idega.business.IBOLookup;
import com.idega.core.data.Address;
import com.idega.data.IDORelationshipException;
import com.idega.presentation.IWContext;
import com.idega.presentation.Table;
import com.idega.presentation.ui.CheckBox;
import com.idega.presentation.ui.DropdownMenu;
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
public class SchoolClassEditor extends CommuneBlock {

	protected static final String PARAMETER_SCHOOL_ID = "sch_s_id";
	protected static final String PARAMETER_SCHOOL_YEAR_ID = "sch_s_y_id";
	protected static final String PARAMETER_SCHOOL_SEASON_ID = "sch_s_s_id";
	protected static final String PARAMETER_SCHOOL_CLASS_ID = "sch_s_c_id";
	protected static final String PARAMETER_STUDENT_ID = "sch_st_id";

	private SchoolCommuneBusiness business;
	private int _schoolID = -1;
	private int _schoolSeasonID = -1;
	private int _schoolYearID = -1;
	private int _schoolClassID = -1;
	
	public void main(IWContext iwc) throws Exception{
		setResourceBundle(getResourceBundle(iwc));
		business = getSchoolCommuneBusiness(iwc);
		init(iwc);

		drawForm(iwc);
	}
	
	private void init(IWContext iwc) throws RemoteException {
		_schoolID = getSchoolCommuneSession(iwc).getSchoolID();	
		_schoolSeasonID = getSchoolCommuneSession(iwc).getSchoolSeasonID();
		_schoolYearID = getSchoolCommuneSession(iwc).getSchoolYearID();
		_schoolClassID = getSchoolCommuneSession(iwc).getSchoolClassID();
	}
	
	private void drawForm(IWContext iwc) throws Exception {
		Form form = new Form();
		form.setEventListener(SchoolEventListener.class);
		
		form.add(getSchoolSeasons());
		form.add(getSchoolYears());
		DropdownMenu schoolClasses = getSchoolClasses();
		form.add(schoolClasses);
		
		if ( _schoolClassID != -1 ) {
			Table table = new Table();
			table.setWidth(getWidth());
			table.setCellpadding(1);
			table.setCellspacing(0);
			form.add(table);
		
			int row = 1;
			table.add(getHeader(localize("school.name", "Name")),1,row);
			table.add(getHeader(localize("school.personal_id", "Personal ID")),2,row);
			table.add(getHeader(localize("school.address", "Address")),3,row);
			table.add(getHeader(localize("school.class", "Class")),4,row++);
			
			User student;
			Address address;
			CheckBox checkBox = new CheckBox();
			
			SchoolClass schoolClass = business.getSchoolClassBusiness().findSchoolClass(new Integer(_schoolClassID));
			List students = business.getStudentList(business.getSchoolClassMemberBusiness().findStudentsInClass(_schoolClassID));
			if ( !students.isEmpty() ) {
				Collections.sort(students, new GenericUserComparator(iwc.getCurrentLocale()));
				Iterator iter = students.iterator();
				while (iter.hasNext()) {
					student = (User) iter.next();
					address = getUserBusiness(iwc).getUserAddress1(((Integer)student.getPrimaryKey()).intValue());
					checkBox = new CheckBox(PARAMETER_STUDENT_ID,String.valueOf(((Integer)student.getPrimaryKey()).intValue()));
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
			selectAll.setToCheckOnClick(checkBox, true);
			
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
		}
		
		add(form);
	}
	
	private DropdownMenu getSchoolSeasons() throws RemoteException {
		DropdownMenu menu = new DropdownMenu(PARAMETER_SCHOOL_SEASON_ID);
		menu.setToSubmit();
		
		Collection seasons = business.getSchoolSeasonBusiness().findAllSchoolSeasons();
		if ( !seasons.isEmpty() ) {
			Iterator iter = seasons.iterator();
			while (iter.hasNext()) {
				SchoolSeason element = (SchoolSeason) iter.next();
				menu.addMenuElement(element.getPrimaryKey().toString(), element.getSchoolSeasonName());
			}
		}
		else {
			menu.addMenuElement(-1, "");	
		}
		
		if ( _schoolSeasonID != -1 )
			menu.setSelectedElement(_schoolSeasonID);
		
		return (DropdownMenu) getStyledInterface(menu);	
	}
		
	private DropdownMenu getSchoolYears() throws RemoteException {
		DropdownMenu menu = new DropdownMenu(PARAMETER_SCHOOL_YEAR_ID);
		menu.setToSubmit();
		
		if ( _schoolID != -1 ) {
			try {
				Collection years = business.getSchoolBusiness().findAllSchoolYearsInSchool(_schoolID);
				if ( !years.isEmpty() ) {
					Iterator iter = years.iterator();
					while (iter.hasNext()) {
						SchoolYear element = (SchoolYear) iter.next();
						menu.addMenuElement(element.getPrimaryKey().toString(), element.getSchoolYearName());
						if ( _schoolYearID == -1 )
							_schoolYearID = ((Integer)element.getPrimaryKey()).intValue();
					}
				}
				else {
					_schoolYearID = -1;
					menu.addMenuElement(-1, "");	
				}
			}
			catch (IDORelationshipException ire) {}
		}
		else {
			menu.addMenuElement(-1, "");	
		}
		
		if ( _schoolYearID != -1 )
			menu.setSelectedElement(_schoolYearID);
		
		return (DropdownMenu) getStyledInterface(menu);
	}
	
	private DropdownMenu getSchoolClasses() throws FinderException, RemoteException {
		DropdownMenu menu = new DropdownMenu(PARAMETER_SCHOOL_CLASS_ID);
		menu.setToSubmit();
		
		if ( _schoolID != -1 && _schoolSeasonID != -1 && _schoolYearID != -1 ) {
			Collection classes = business.getSchoolClassBusiness().findSchoolClassesBySchoolAndSeasonAndYear(_schoolID, _schoolSeasonID, _schoolYearID);
			if ( !classes.isEmpty() ) {
				Iterator iter = classes.iterator();
				while (iter.hasNext()) {
					SchoolClass element = (SchoolClass) iter.next();
					menu.addMenuElement(element.getPrimaryKey().toString(), element.getName());
					if ( _schoolClassID == -1 )
						_schoolClassID = ((Integer)element.getPrimaryKey()).intValue();
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
		
		if ( _schoolClassID != -1 )
			menu.setSelectedElement(_schoolClassID);
		
		return (DropdownMenu) getStyledInterface(menu);	
	}
		
	private SchoolCommuneBusiness getSchoolCommuneBusiness(IWContext iwc) throws RemoteException {
		return (SchoolCommuneBusiness) IBOLookup.getServiceInstance(iwc, SchoolCommuneBusiness.class);	
	}
	
	private UserBusiness getUserBusiness(IWContext iwc) throws RemoteException {
		return (UserBusiness) IBOLookup.getServiceInstance(iwc, UserBusiness.class);	
	}
	
	private SchoolCommuneSession getSchoolCommuneSession(IWContext iwc) throws RemoteException {
		return (SchoolCommuneSession) IBOLookup.getSessionInstance(iwc, SchoolCommuneSession.class);	
	}
	
	public String getBundleIdentifier(){
		return IW_BUNDLE_IDENTIFIER;
	}
}