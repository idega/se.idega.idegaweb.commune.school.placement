/*
 * Created on 5.10.2003
 */
package se.idega.idegaweb.commune.childcare.presentation;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;

import se.idega.idegaweb.commune.childcare.business.AfterSchoolBusiness;
import se.idega.idegaweb.commune.childcare.data.ChildCareApplication;
import se.idega.idegaweb.commune.childcare.event.ChildCareEventListener;
import se.idega.idegaweb.commune.school.event.SchoolEventListener;

import com.idega.block.school.data.SchoolClassMember;
import com.idega.business.IBOLookup;
import com.idega.business.IBORuntimeException;
import com.idega.core.contact.data.Phone;
import com.idega.core.location.data.Address;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.presentation.IWContext;
import com.idega.presentation.Table;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.HiddenInput;
import com.idega.user.data.User;
import com.idega.util.IWTimestamp;
import com.idega.util.PersonalIDFormatter;

/**
 * @author laddi
 */
public class AfterSchoolChoiceApprover extends ChildCareBlock {

	/* (non-Javadoc)
	 * @see se.idega.idegaweb.commune.childcare.presentation.ChildCareBlock#init(com.idega.presentation.IWContext)
	 */
	
	public static final String PARAMETER_ACTION = "sch_action";
	
	private int action = 0;
	//private int method = 0;
	private String sortStudentsBy = "c.QUEUE_DATE";
	private String sortChoicesBy = "";
	//private int sortPlaced = SchoolChoiceComparator.PLACED_SORT;
	//private int sortPlacedUnplacedBy = -1;

	
	
	//private String searchString = "";
	//private boolean searchEnabled = true;
	
	
	//private final String PARAMETER_METHOD = "sch_method";
	//private final String PARAMETER_APPLICANT_ID = "sch_applicant_id";
	//private final String PARAMETER_PREVIOUS_CLASS_ID = "sch_prev_class_id";
	private final String PARAMETER_SORT = "sch_choice_sort";
	//private final String PARAMETER_SORT_PLACED = "sch_choice_sort_placed";
	//private final String PARAMETER_SEARCH = "scH_choise_search";	
	
	private final int ACTION_MANAGE = 1;
	public static final int ACTION_SAVE = 2;
	
	public void init(IWContext iwc) throws Exception {
			
		////
		if (iwc.isLoggedOn()) {
			parseAction(iwc);

			switch (action) {
			case ACTION_MANAGE:
				drawForm(iwc);
				break;
			//case ACTION_SAVE:
			//	drawNewGroupForm(iwc);
			//	break;

			}
		}
		else {
			add(super.getSmallHeader(localize("not_logged_on", "Not logged on")));
		}
		
	
	}
	
	private void parseAction(IWContext iwc) {
		//isOngoingSeason = getBusiness().isOngoingSeason(getSchoolSeasonID());

		/*if (iwc.isParameterSet(PARAMETER_METHOD))
			method = Integer.parseInt(iwc.getParameter(PARAMETER_METHOD));
		else
			method = 0;*/

		if (iwc.isParameterSet(PARAMETER_ACTION))
			action = Integer.parseInt(iwc.getParameter(PARAMETER_ACTION));
		else
			action = ACTION_MANAGE;
		
		if (iwc.isParameterSet(PARAMETER_SORT))
			sortChoicesBy = iwc.getParameter(PARAMETER_SORT);
		else
			sortChoicesBy = "c.QUEUE_DATE";
			sortStudentsBy = sortChoicesBy;
			
	}
	
	private void drawForm(IWContext iwc) throws RemoteException {
		Form form = new Form();
		form.setEventListener(SchoolEventListener.class);
		form.add(new HiddenInput(PARAMETER_ACTION, String.valueOf(action)));

		Table table = new Table(3, 17);
				
		table.setCellpadding(0);
		table.setCellspacing(0);
		
		table.mergeCells(1, 2, 3, 2);
		table.mergeCells(1, 3, 3, 3);
		table.mergeCells(1, 4, 3, 4);
		table.mergeCells(1, 5, 3, 5);
		table.mergeCells(1, 6, 3, 6);
		table.mergeCells(1, 7, 3, 7);
		table.mergeCells(1, 8, 3, 8);
		table.mergeCells(1, 9, 3, 9);
		table.mergeCells(1, 10, 3, 10);
		table.mergeCells(1, 11, 3, 11);
		table.mergeCells(1, 12, 3, 12);
		table.mergeCells(1, 13, 3, 13);
		
		table.setWidth(2, 1, 12);
		table.setWidth(3, 1, "100%");
		
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

		if (useStyleNames()) {
			table.setCellpaddingLeft(1, 1, 12);
			table.setCellpaddingLeft(1, 3, 12);
			table.setCellpaddingLeft(1, 5, 12);
			table.setCellpaddingRight(1, 1, 12);
			table.setCellpaddingRight(1, 3, 12);
			table.setCellpaddingRight(1, 5, 12);
			table.setCellpaddingLeft(1, 9, 12);
			table.setCellpaddingRight(1, 9, 12);
			table.setCellpaddingLeft(1, 17, 12);
			table.setCellpaddingRight(1, 17, 12);
		}

		table.add(getNavigationTable(), 1, 1);
		table.add(getSearchAndSortTable(), 3, 1);
		table.add(getApplicationTable(iwc), 1, 5);
		table.add(getLegendTable(), 1, 7);
		
		add(form);
	}
	
	
	
	/*private Collection getApplicationCollection(IWApplicationContext iwac) throws RemoteException {
		Collection applications = getAfterSchoolBusiness(iwac).findChoicesByProvider(getSession().getChildCareID());
		return applications;
	}*/

	
	///malin
	private Collection getApplicationCollection(IWApplicationContext iwac, String sorting) throws RemoteException {
		Collection applications = getAfterSchoolBusiness(iwac).findChoicesByProvider(getSession().getChildCareID(), sorting);
		return applications;
	}
	
	protected Form getSearchAndSortTable() {
		Form form = new Form();
		form.setEventListener(SchoolEventListener.class);
		form.add(new HiddenInput(PARAMETER_ACTION, String.valueOf(action)));
		
		Table table = new Table(4, 1);
		table.setCellpadding(0);
		table.setCellspacing(0);
		table.setBorder(0);
		
		
		table.add(getSmallHeader(localize("school.sort_by", "Sort by") + ":"), 1, 1);

		DropdownMenu menu = (DropdownMenu) getStyledInterface(new DropdownMenu(PARAMETER_SORT));
		menu.addMenuElement("c.QUEUE_DATE", localize("childcare.sort_queuedate", "Queue date"));
		menu.addMenuElement("iu.LAST_NAME", localize("childcare.sort_name", "Name"));
		menu.addMenuElement("iu.PERSONAL_ID", localize("childcare.sort_personal_id", "Personal ID"));
		
		menu.setSelectedElement(sortChoicesBy);
		menu.setToSubmit();		
		table.setWidth(2, 4);
		table.add(menu, 3, 1);
		
		
		form.add(table);
		
		return form;
	}
	
	///
	private Form getNavigationTable() throws RemoteException {
		Form form = new Form();
		form.setEventListener(ChildCareEventListener.class);
		
		Table table = new Table(3, 1);
		table.setWidth(2, 4);
		table.setCellpadding(0);
		table.setCellspacing(0);
		form.add(table);
		table.add(getSmallHeader(localize("child_care.season", "Season") + ":"), 1, 1);
		table.add(getSeasons(), 3, 1);
		
		return form;
	}
	
	private Table getApplicationTable(IWContext iwc) throws RemoteException {
		Table table = new Table();
		table.setWidth(Table.HUNDRED_PERCENT);
		table.setCellpadding(getCellpadding());
		table.setCellspacing(getCellspacing());
		table.setColumns(4);
		if (useStyleNames()) {
			table.setRowStyleClass(1, getHeaderRowClass());
		}
		else {
			table.setRowColor(1, getHeaderColor());
		}
		int row = 1;
		int column = 1;
			
		if (useStyleNames()) {
			table.setCellpaddingLeft(1, row, 12);
			table.setCellpaddingRight(table.getColumns(), row, 12);
		}
		table.add(getLocalizedSmallHeader("child_care.name","Name"), column++, row);
		table.add(getLocalizedSmallHeader("child_care.personal_id","Personal ID"), column++, row);
		table.add(getLocalizedSmallHeader("child_care.address","Address"), column++, row);
		table.add(getLocalizedSmallHeader("child_care.phone","Phone"), column++, row++);

		boolean showMessage = false;

		Collection applications = getApplicationCollection(iwc, sortStudentsBy);
		if (applications != null && !applications.isEmpty()) {
			ChildCareApplication application;
			User child;
			Address address;
			Phone phone;
			Link link;
			boolean hasMessage = false;
			String name = null;
			
			IWTimestamp today = new IWTimestamp();		
			
			Iterator iter = applications.iterator();
			while (iter.hasNext()) {
				column = 1;
				application = (ChildCareApplication) iter.next();
				child = application.getChild();
				address = getBusiness().getUserBusiness().getUsersMainAddress(child);
				phone = getBusiness().getUserBusiness().getChildHomePhone(child);
				hasMessage = application.getMessage() != null;		
				boolean active = false;
				
				SchoolClassMember member;
				IWTimestamp terminated = null;
				IWTimestamp startdate = null;
				
				Collection placings = getBusiness().getSchoolBusiness().findClassMemberInChildCare(((Integer) child.getPrimaryKey()).intValue(), getSession().getChildCareID());
				Iterator iterPlac = placings.iterator();
				
				while (iterPlac.hasNext()) {
					column = 1;
					member = (SchoolClassMember) iterPlac.next();
					
					if (member.getRemovedDate() != null){
						terminated = new IWTimestamp(member.getRemovedDate());
						if (terminated.isEarlierThan(today))
							active = false;
						else 
							active = true;
					}else if (member.getRegisterDate() != null){
						startdate = new IWTimestamp(member.getRegisterDate());
						if (startdate.isLaterThan(today)){
							active = true;
						}
						else if (startdate.isEarlierThan(today) && member.getRemovedDate() == null){
							active = true;
						}
						else
							active = false;
					}
					else {
						active =true;
					}
				}
				
				
				if (!active){
				
				if (useStyleNames()) {
					if (row % 2 == 0) {
						table.setRowStyleClass(row, getDarkRowClass());
					}
					else {
						table.setRowStyleClass(row, getLightRowClass());
					}
					table.setCellpaddingLeft(1, row, 12);
					table.setCellpaddingRight(table.getColumns(), row, 12);
				}

				if (application.getApplicationStatus() == getBusiness().getStatusAccepted()) {
					table.setRowColor(row, ACCEPTED_COLOR);
				}
				else if (application.getApplicationStatus() == getBusiness().getStatusParentsAccept()) {
					table.setRowColor(row, PARENTS_ACCEPTED_COLOR);
				}
				else if (application.getApplicationStatus() == getBusiness().getStatusContract()) {
					table.setRowColor(row, CONTRACT_COLOR);
				}
				else {
					if (!useStyleNames()) {
						if (row % 2 == 0)
							table.setRowColor(row, getZebraColor1());
						else
							table.setRowColor(row, getZebraColor2());
					}
				}
					
				//link = getSmallLink(child.getNameLastFirst(true));
				name = getBusiness().getUserBusiness().getNameLastFirst(child, true);
				link = getSmallLink(name);
				link.setEventListener(ChildCareEventListener.class);
				link.setParameter(getSession().getParameterUserID(), String.valueOf(application.getChildId()));
				link.setParameter(getSession().getParameterApplicationID(), application.getPrimaryKey().toString());
				link.setParameter(getSession().getParameterCaseCode(), getBusiness().getAfterSchoolCareCaseCode());
				if (getResponsePage() != null)
					link.setPage(getResponsePage());
	
				if (hasMessage) {
					showMessage = true;
					table.add(getSmallErrorText("*"), column, row);
					table.add(getSmallText(Text.NON_BREAKING_SPACE), column, row);
				}
				
				table.add(link, column++, row);
				table.add(getSmallText(PersonalIDFormatter.format(child.getPersonalID(), iwc.getCurrentLocale())), column++, row);
				
				if (address != null)
					table.add(getSmallText(address.getStreetAddress()), column++, row);
				else
					table.add(getSmallText("-"), column++, row);
				if (phone != null)
					table.add(getSmallText(phone.getNumber()), column++, row++);
				else
					table.add(getSmallText("-"), column++, row++);
			}
			} //active
			if (showMessage) {
				table.setHeight(row++, 2);
				table.mergeCells(1, row, table.getColumns(), row);
				if (useStyleNames()) {
					table.setCellpaddingLeft(1, row, 12);
				}
				table.add(getSmallErrorText("* "), 1, row);
				table.add(getSmallText(localize("child_care.has_message_in_application","The application has a message attached")), 1, row++);
			}
			
			table.setColumnAlignment(2, Table.HORIZONTAL_ALIGN_CENTER);
			table.setColumnAlignment(4, Table.HORIZONTAL_ALIGN_CENTER);
		}
			
		return table;
	}
	
	protected AfterSchoolBusiness getAfterSchoolBusiness(IWApplicationContext iwac) {
		try {
			return (AfterSchoolBusiness) IBOLookup.getServiceInstance(iwac, AfterSchoolBusiness.class);
		}
		catch (RemoteException e) {
			throw new IBORuntimeException(e.getMessage());
		}
	}
	
	public void setSearchEnabled(boolean searchEnabled) {
		if (searchEnabled) {
			
		}
		//this.searchEnabled = searchEnabled;
	}
}