package se.idega.idegaweb.commune.school.presentation;

import is.idega.idegaweb.member.business.MemberFamilyLogic;
import is.idega.idegaweb.member.business.NoCustodianFound;

import java.rmi.RemoteException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import se.idega.idegaweb.commune.presentation.CommuneBlock;
import se.idega.idegaweb.commune.school.business.SchoolCommuneBusiness;
import se.idega.idegaweb.commune.school.business.SchoolCommuneSession;
import se.idega.idegaweb.commune.school.data.SchoolChoice;

import com.idega.block.school.business.SchoolYearComparator;
import com.idega.block.school.data.School;
import com.idega.block.school.data.SchoolClass;
import com.idega.block.school.data.SchoolClassMember;
import com.idega.block.school.data.SchoolSeason;
import com.idega.block.school.data.SchoolYear;
import com.idega.business.IBOLookup;
import com.idega.core.data.Address;
import com.idega.core.data.Email;
import com.idega.core.data.Phone;
import com.idega.presentation.IWContext;
import com.idega.presentation.Table;
import com.idega.presentation.text.Break;
import com.idega.presentation.text.HorizontalRule;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.CheckBox;
import com.idega.presentation.ui.CloseButton;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.HiddenInput;
import com.idega.presentation.ui.SubmitButton;
import com.idega.presentation.ui.TextArea;
import com.idega.presentation.ui.Window;
import com.idega.user.business.NoPhoneFoundException;
import com.idega.user.business.UserBusiness;
import com.idega.user.data.User;
import com.idega.util.IWTimestamp;
import com.idega.util.PersonalIDFormatter;

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
	
	public static final int METHOD_OVERVIEW = 1;
	public static final int METHOD_REJECT = 2;
	public static final int METHOD_REPLACE = 3;
	public static final int METHOD_MOVE = 4;
	
	public static final int ACTION_REJECT = 1;
	public static final int ACTION_REPLACE = 2;
	public static final int ACTION_MOVE = 3;
	
	private static final String PARAMETER_REJECT_MESSAGE = "sch_admin_reject_message";
	private static final String PARAMETER_REPLACE_MESSAGE = "sch_admin_replace_message";
	private static final String PARAMETER_MOVE_MESSAGE = "sch_admin_replace_message";
	private static final String PARAMETER_PROTOCOL = "sch_admin_protocol";
	private static final String PARAMETER_SCHOOL_ID ="sch_school_id";

	private int _method = -1;
	private int _action = -1;
	
	private int _userID = -1;
	private int _choiceID = -1;
	private int _schoolClassID = -1;
	private int _schoolYearID = -1;
	
	private boolean _protocol = true;
	private boolean _move = true;


	
	/**
	 * @see com.idega.presentation.PresentationObject#main(IWContext)
	 */
	public void main(IWContext iwc) throws Exception {
		setResourceBundle(getResourceBundle(iwc));
		parse(iwc);
		
		switch (_action) {
			case ACTION_REJECT:
				reject(iwc);
				break;
			case ACTION_REPLACE:
				replace(iwc);
				break;
			case ACTION_MOVE:
				move(iwc);
				break;
		}
		
		if (_method != -1)
			drawForm(iwc);
	}
	
	private void drawForm(IWContext iwc) throws RemoteException {
		Form form = new Form();
		form.maintainParameter(PARAMETER_USER_ID);
		form.maintainParameter(PARAMETER_CHOICE_ID);
		
		Table table = new Table(3,5);
		table.setRowColor(1, "#000000");
		table.setRowColor(3, "#000000");
		table.setRowColor(5, "#000000");
		table.setColumnColor(1, "#000000");
		table.setColumnColor(3, "#000000");
		table.setColor(2, 2, "#CCCCCC");
		table.setWidth(Table.HUNDRED_PERCENT);
		table.setWidth(2,Table.HUNDRED_PERCENT);
		table.setHeight(Table.HUNDRED_PERCENT);
		table.setHeight(4,Table.HUNDRED_PERCENT);
		table.setCellpadding(0);
		table.setCellspacing(0);
		form.add(table);
		
		Table headerTable = new Table(1,1);
		headerTable.setCellpadding(6);
		table.add(headerTable,2,2);
		
		Table contentTable = new Table(1,1);
		contentTable.setCellpadding(10);
		table.add(contentTable,2,4);
		
		switch (_method) {
			case METHOD_OVERVIEW:
				headerTable.add(getHeader(localize("school.student_overview","Student overview")));
				contentTable.add(getOverview(iwc));
				break;
			case METHOD_REJECT:
				headerTable.add(getHeader(localize("school.reject_student","Reject student")));
				contentTable.add(getRejectForm(iwc));
				break;
			case METHOD_REPLACE:
				headerTable.add(getHeader(localize("school.student_replacing","Replace student")));
				contentTable.add(getReplaceForm(iwc));
				break;
			case METHOD_MOVE:
				headerTable.add(getHeader(localize("school.student_move","Move student")));
				contentTable.add(getMoveForm(iwc));
				break;
		}
		
		add(form);
	}
	
	private Table getOverview(IWContext iwc) throws RemoteException {
		Table table = new Table();
		table.setCellpadding(5);
		table.setWidth(Table.HUNDRED_PERCENT);
		int row = 1;
		
		if (_userID != -1) {
			User user = getUserBusiness(iwc).getUser(_userID);
			Address address = getUserBusiness(iwc).getUserAddress1(_userID);
			
			table.add(getSmallHeader(localize("school.name","Name")),1,row);
			table.add(getSmallText(user.getNameLastFirst(true)),2,row++);

			table.add(getSmallHeader(localize("school.personal_id","Personal ID")),1,row);
			table.add(getSmallText(PersonalIDFormatter.format(user.getPersonalID(), iwc.getCurrentLocale())),2,row++);

			table.add(getSmallHeader(localize("school.address","Address")),1,row);
			if (address != null)
				table.add(getSmallText(address.getStreetAddress() + ", " + address.getPostalAddress()),2,row++);
			else
				row++;
				
			try {
				Collection parents = getMemberFamilyLogic(iwc).getCustodiansFor(user);
				table.add(getSmallHeader(localize("school.custodians","Custodians")),1,row);
				if (parents != null && !parents.isEmpty()) {
					Iterator iter = parents.iterator();
					while (iter.hasNext()) {
						User parent = (User) iter.next();
						table.add(getSmallText(parent.getNameLastFirst(true)),2,row);
						if (iter.hasNext())
							table.add(new Break(),2,row);
					}	
				}
				row++;
			}
			catch (NoCustodianFound ncf) {}
			
			if (_choiceID != -1) {
				Collection choices = getSchoolCommuneBusiness(iwc).getSchoolChoiceBusiness().findByStudentAndSeason(_userID, getSchoolCommuneSession(iwc).getSchoolSeasonID());
				String message = null;
				if (!choices.isEmpty()) {
					table.add(getSmallHeader(localize("school.school_choice","School choices")),1,row);
					
					School school;
					SchoolChoice choice;
					Iterator iter = choices.iterator();
					while (iter.hasNext()) {
						choice = (SchoolChoice) iter.next();
						school = getSchoolCommuneBusiness(iwc).getSchoolBusiness().getSchool(new Integer(choice.getChosenSchoolId()));
						String string = String.valueOf(choice.getChoiceOrder()) + ". " + school.getName() + " (" + getSchoolCommuneBusiness(iwc).getLocalizedCaseStatusDescription(choice.getCaseStatus(),iwc.getCurrentLocale()) + ")";
						if (choice.getChosenSchoolId() == getSchoolCommuneSession(iwc).getSchoolID())
							table.add(this.getSmallHeader(string),2,row);
						else
							table.add(getSmallText(string),2,row);
						
						if (iter.hasNext())
							table.add(new Break(),2,row);
						if (message == null)
							message = choice.getMessage();
					}
					row++;
				}
				
				if (message != null) {
					table.add(getSmallHeader(localize("school.school_choice_message","Applicant message")),1,row);
					table.add(getSmallText(message),2,row++);
				}
			}
			
			table.add(getSmallHeader(localize("school.current_shool","Current school")),1,row);
			SchoolSeason season = getSchoolCommuneBusiness(iwc).getPreviousSchoolSeason(getSchoolCommuneSession(iwc).getSchoolSeasonID());
      if (season != null) {
		    SchoolClassMember schoolClassMember = getSchoolCommuneBusiness(iwc).getSchoolClassMemberBusiness().findByStudentAndSeason(user,season);
		    if (schoolClassMember != null) {
		      SchoolClass schoolClass = getSchoolCommuneBusiness(iwc).getSchoolClassBusiness().findSchoolClass(new Integer(schoolClassMember.getSchoolClassId()));
		      School currentSchool = getSchoolCommuneBusiness(iwc).getSchoolBusiness().getSchool(new Integer(schoolClass.getSchoolId()));
		      SchoolYear schoolYear = getSchoolCommuneBusiness(iwc).getSchoolYearBusiness().getSchoolYear(new Integer(schoolClass.getSchoolYearId()));
		      
		      String schoolString = currentSchool.getName() + " - " + schoolClass.getName();
		      table.add(getSmallText(schoolString),2,row);
		    }
      }
      row++;
      
      table.setColumnVerticalAlignment(1, Table.VERTICAL_ALIGN_TOP);
      table.mergeCells(1, row, table.getColumns(), row);
      
      CloseButton close = (CloseButton) getStyledInterface(new CloseButton(localize("back","Back")));
      SubmitButton replace = (SubmitButton) getStyledInterface(new SubmitButton(localize("school.replace","Replace"),PARAMETER_METHOD,String.valueOf(METHOD_REPLACE)));
      SubmitButton reject = (SubmitButton) getStyledInterface(new SubmitButton(localize("school.reject","Reject"),PARAMETER_METHOD,String.valueOf(METHOD_REJECT)));
      SubmitButton move = (SubmitButton) getStyledInterface(new SubmitButton(localize("school.move","Move"),PARAMETER_METHOD,String.valueOf(METHOD_MOVE)));
      
      table.add(close,1,row);
      table.add(Text.NON_BREAKING_SPACE,1,row);
      table.add(replace,1,row);
      table.add(Text.NON_BREAKING_SPACE,1,row);
      if (_choiceID != -1) {
      	table.add(reject,1,row);
	      table.add(Text.NON_BREAKING_SPACE,1,row);
      }
      table.add(move,1,row);
		}
		
		return table;
	}
	
	private Table getRejectForm(IWContext iwc) throws RemoteException {
		Table table = new Table();
		table.setCellpadding(5);
		table.setWidth(Table.HUNDRED_PERCENT);
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
		
		String message = MessageFormat.format(localize("school.reject_student_message","We are sorry that we cannot offer you a place in our school at present, if you have any questions, please contact {0} via either phone ({1}) or e-mail ({2})."),arguments);
		TextArea textArea = (TextArea) getStyledInterface(new TextArea(PARAMETER_REJECT_MESSAGE,message));
		textArea.setWidth(Table.HUNDRED_PERCENT);
		textArea.setHeight(4);
	
		table.add(getSmallHeader(localize("school.reject_student_message_info","The following message will be sent to the students' parents.")),1,row++);
		table.add(textArea,1,row++);
		
		SubmitButton reject = (SubmitButton) getStyledInterface(new SubmitButton(localize("school.reject","Reject"),PARAMETER_ACTION,String.valueOf(ACTION_REJECT)));
		table.add(reject,1,row);
		
		return table;
	}
	
	private Table getReplaceForm(IWContext iwc) throws RemoteException {
		Table table = new Table();
		table.setCellpadding(5);
		table.setWidth(Table.HUNDRED_PERCENT);
		table.add(new HiddenInput(PARAMETER_METHOD,String.valueOf(METHOD_REPLACE)));
		int row = 1;
		
		User user = getUserBusiness(iwc).getUser(_userID);
		
		table.add(getSmallHeader(localize("school.replace_student_info","You have selected to replace student: ")+user.getName()+"."),1,row++);
		
		CheckBox box = new CheckBox(PARAMETER_PROTOCOL);
		box.setWidth("12");
		box.setHeight("12");
		box.keepStatusOnAction(true);
		table.add(box,1,row);
		if (_protocol)
			table.add(getSmallText(localize("school.protocol_followed","All protocols have been followed")),1,row++);
		else
			table.add(getSmallErrorText(localize("school.protocol_followed","All protocols have been followed")),1,row++);
		
		table.add(getNavigationTable(iwc),1,row++);
		
		HorizontalRule rule = new HorizontalRule(Table.HUNDRED_PERCENT,1);
		table.add(rule,1,row++);
		
		table.add(getSmallHeader(localize("school.replace_reason_text","The following message will be sent to BUN as the reason for replacement.")),1,row++);
		
		table.add(getSmallText(localize("school.replace_reason","Replace reason")+":"),1,row);
		table.add(new Break(),1,row);
		TextArea textArea = (TextArea) getStyledInterface(new TextArea(PARAMETER_REPLACE_MESSAGE));
		textArea.setWidth(Table.HUNDRED_PERCENT);
		textArea.keepStatusOnAction(true);
		textArea.setHeight(4);
		table.add(textArea,1,row++);
		
		SubmitButton replace = (SubmitButton) getStyledInterface(new SubmitButton(localize("school.replace","Replace"),PARAMETER_ACTION,String.valueOf(ACTION_REPLACE)));
		replace.setValueOnClick(PARAMETER_METHOD, "-1");
		table.add(replace,1,row);
		
		return table;
	}
	
	private Table getMoveForm(IWContext iwc) throws RemoteException {
		Table table = new Table();
		table.setCellpadding(5);
		table.setWidth(Table.HUNDRED_PERCENT);
		table.add(new HiddenInput(PARAMETER_METHOD,String.valueOf(METHOD_MOVE)));
		int row = 1;
		
		User user = getUserBusiness(iwc).getUser(_userID);
		
		table.add(getSmallHeader(localize("school.move_student_info","You have selected to move student: ")+user.getName()+"."),1,row++);
		
		table.add(getSmallHeader(localize("school.new_school","New school")+": "),1,row);
		table.add(getSchools(iwc),1,row++);
		
		table.add(getSmallHeader(localize("school.move_reason_text","The following message will be sent to the new school as the reason for move.")),1,row++);
		
		if (_move)
			table.add(getSmallText(localize("school.move_reason","Move reason")+":"),1,row);
		else
			table.add(getSmallErrorText(localize("school.move_reason","Move reason")+":"),1,row);
		table.add(new Break(),1,row);
		TextArea textArea = (TextArea) getStyledInterface(new TextArea(PARAMETER_MOVE_MESSAGE));
		textArea.setWidth(Table.HUNDRED_PERCENT);
		textArea.setHeight(4);
		table.add(textArea,1,row++);
		
		SubmitButton move = (SubmitButton) getStyledInterface(new SubmitButton(localize("school.move","Move"),PARAMETER_ACTION,String.valueOf(ACTION_MOVE)));
		move.setValueOnClick(PARAMETER_METHOD, "-1");
		table.add(move,1,row);
		
		return table;
	}
	
	protected Table getNavigationTable(IWContext iwc) throws RemoteException {
		Table table = new Table(7,1);
		table.setCellpadding(0);
		table.setCellspacing(0);
		table.setWidth(2,"8");
		table.setWidth(5, "8");

		table.add(getSmallHeader(localize("school.replace_to","Replace to")+":"),1,1);
		table.add(getSmallHeader(localize("school.year","Year")+":"+Text.NON_BREAKING_SPACE),3,1);
		table.add(getSchoolYears(iwc),4,1);
		table.add(getSmallHeader(localize("school.class","Class")+":"+Text.NON_BREAKING_SPACE),6,1);
		table.add(getSchoolClasses(iwc),7,1);
		
		return table;
	}
	
	private void reject(IWContext iwc) throws RemoteException {
		String messageHeader = localize("school.reject_message_header","School choice rejected.");
		String messageBody = iwc.getParameter(PARAMETER_REJECT_MESSAGE);
		getSchoolCommuneBusiness(iwc).getSchoolChoiceBusiness().rejectApplication(_choiceID, getSchoolCommuneSession(iwc).getSchoolSeasonID(), iwc.getCurrentUser(), messageHeader, messageBody);
		
		getParentPage().setParentToReload();
		getParentPage().close();	
	}

	private void replace(IWContext iwc) throws RemoteException {
		if (iwc.isParameterSet(PARAMETER_PROTOCOL) && iwc.isParameterSet(PARAMETER_REPLACE_MESSAGE)) {
			String message = iwc.getParameter(PARAMETER_REPLACE_MESSAGE);
			
			IWTimestamp stamp = new IWTimestamp();
			getSchoolCommuneBusiness(iwc).getSchoolClassMemberBusiness().storeSchoolClassMember(_userID, _schoolClassID, stamp.getTimestamp(), ((Integer)iwc.getCurrentUser().getPrimaryKey()).intValue());
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
			int schoolID = Integer.parseInt(iwc.getParameter(PARAMETER_SCHOOL_ID));
			int grade = getSchoolCommuneBusiness(iwc).getSchoolYear(getSchoolCommuneSession(iwc).getSchoolYearID()).getSchoolYearAge() + 1;
			
			IWTimestamp stamp = new IWTimestamp();
			try {
				getSchoolCommuneBusiness(iwc).getSchoolChoiceBusiness().createSchoolChoice(((Integer)iwc.getCurrentUser().getPrimaryKey()).intValue(), _userID, getSchoolCommuneSession(iwc).getSchoolID(), schoolID, grade, 1, 2, 1, 1, "", message, stamp.getTimestampRightNow(), true, false, false, true, false, getSchoolCommuneBusiness(iwc).getCaseStatus("FLYT"), null);
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
		
		if ( _schoolClassID != -1 && _schoolYearID != -1 )
			validateSchoolClass(iwc);
	}

	protected DropdownMenu getSchoolYears(IWContext iwc) throws RemoteException {
		DropdownMenu menu = new DropdownMenu(getSchoolCommuneSession(iwc).getParameterSchoolYearID());
		menu.setToSubmit();
		
		if ( getSchoolCommuneSession(iwc).getSchoolID() != -1 ) {
			List years = new Vector(getSchoolCommuneBusiness(iwc).getSchoolBusiness().findAllSchoolYearsInSchool(getSchoolCommuneSession(iwc).getSchoolID()));
			if ( !years.isEmpty() ) {
	      Collections.sort(years,new SchoolYearComparator());
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
		else {
			menu.addMenuElement(-1, "");	
		}
		
		if ( _schoolYearID != -1 )
			menu.setSelectedElement(_schoolYearID);
		
		return (DropdownMenu) getStyledInterface(menu);
	}
	
	protected DropdownMenu getSchoolClasses(IWContext iwc) throws RemoteException {
		DropdownMenu menu = new DropdownMenu(getSchoolCommuneSession(iwc).getParameterSchoolClassID());
		menu.setToSubmit();
		
		if ( getSchoolCommuneSession(iwc).getSchoolID() != -1 && getSchoolCommuneSession(iwc).getSchoolSeasonID() != -1 && _schoolYearID != -1 ) {
			Collection classes = getSchoolCommuneBusiness(iwc).getSchoolClassBusiness().findSchoolClassesBySchoolAndSeasonAndYear(getSchoolCommuneSession(iwc).getSchoolID(), getSchoolCommuneSession(iwc).getSchoolSeasonID(), _schoolYearID);
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
		
	protected DropdownMenu getSchools(IWContext iwc) throws RemoteException {
		DropdownMenu menu = new DropdownMenu(PARAMETER_SCHOOL_ID);
		menu.setToSubmit();
		menu.keepStatusOnAction(true);
		
		Collection classes = getSchoolCommuneBusiness(iwc).getSchoolBusiness().findAllSchools();
		if ( !classes.isEmpty() ) {
			Iterator iter = classes.iterator();
			while (iter.hasNext()) {
				School element = (School) iter.next();
				menu.addMenuElement(element.getPrimaryKey().toString(), element.getName());
			}
		}
		
		return (DropdownMenu) getStyledInterface(menu);	
	}
		
	private void validateSchoolClass(IWContext iwc) throws RemoteException {
		SchoolClass schoolClass = getSchoolCommuneBusiness(iwc).getSchoolClassBusiness().findSchoolClass(new Integer(_schoolClassID));
		if ( schoolClass.getSchoolYearId() != _schoolYearID ) {
			Collection schoolClasses = getSchoolCommuneBusiness(iwc).getSchoolClassBusiness().findSchoolClassesBySchoolAndSeasonAndYear(getSchoolCommuneSession(iwc).getSchoolID(), getSchoolCommuneSession(iwc).getSchoolSeasonID(), _schoolYearID);
			if ( !schoolClasses.isEmpty() ) {
				Iterator iter = schoolClasses.iterator();
				while (iter.hasNext()) {
					_schoolClassID = ((Integer)((SchoolClass) iter.next()).getPrimaryKey()).intValue();
					continue;
				}
			}
		}
	}
	
	private SchoolCommuneBusiness getSchoolCommuneBusiness(IWContext iwc) throws RemoteException {
		return (SchoolCommuneBusiness) IBOLookup.getServiceInstance(iwc, SchoolCommuneBusiness.class);	
	}
	
	private SchoolCommuneSession getSchoolCommuneSession(IWContext iwc) throws RemoteException {
		return (SchoolCommuneSession) IBOLookup.getSessionInstance(iwc, SchoolCommuneSession.class);	
	}
	
	private UserBusiness getUserBusiness(IWContext iwc) throws RemoteException {
		return (UserBusiness) IBOLookup.getServiceInstance(iwc, UserBusiness.class);
	}

	private MemberFamilyLogic getMemberFamilyLogic(IWContext iwc) throws RemoteException {
		return (MemberFamilyLogic) com.idega.business.IBOLookup.getServiceInstance(iwc, MemberFamilyLogic.class);
	}
}