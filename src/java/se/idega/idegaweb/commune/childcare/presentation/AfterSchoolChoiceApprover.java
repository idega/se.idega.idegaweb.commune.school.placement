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

import com.idega.business.IBOLookup;
import com.idega.business.IBORuntimeException;
import com.idega.core.contact.data.Phone;
import com.idega.core.location.data.Address;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.presentation.IWContext;
import com.idega.presentation.Table;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.Form;
import com.idega.user.data.User;
import com.idega.util.PersonalIDFormatter;

/**
 * @author laddi
 */
public class AfterSchoolChoiceApprover extends ChildCareBlock {

	/* (non-Javadoc)
	 * @see se.idega.idegaweb.commune.childcare.presentation.ChildCareBlock#init(com.idega.presentation.IWContext)
	 */
	public void init(IWContext iwc) throws Exception {
		Table table = new Table(1,5);
		table.setWidth(getWidth());
		table.setHeight(2, 12);
		table.setHeight(4, 6);
		table.setCellpadding(0);
		table.setCellspacing(0);
		add(table);
		
		table.add(getNavigationTable(), 1, 1);
		table.add(getApplicationTable(iwc), 1, 3);
		table.add(getLegendTable(), 1, 5);
	}
	
	private Collection getApplicationCollection(IWApplicationContext iwac) throws RemoteException {
		Collection applications = getAfterSchoolBusiness(iwac).findChoicesByProvider(getSession().getChildCareID());
		return applications;
	}

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
		Table applicationTable = new Table();
		applicationTable.setWidth(Table.HUNDRED_PERCENT);
		applicationTable.setCellpadding(getCellpadding());
		applicationTable.setCellspacing(getCellspacing());
		applicationTable.setColumns(4);
		applicationTable.setRowColor(1, getHeaderColor());
		int row = 1;
		int column = 1;
			
		applicationTable.add(getLocalizedSmallHeader("child_care.name","Name"), column++, row);
		applicationTable.add(getLocalizedSmallHeader("child_care.personal_id","Personal ID"), column++, row);
		applicationTable.add(getLocalizedSmallHeader("child_care.address","Address"), column++, row);
		applicationTable.add(getLocalizedSmallHeader("child_care.phone","Phone"), column++, row++);

		boolean showMessage = false;

		Collection applications = getApplicationCollection(iwc);
		if (applications != null && !applications.isEmpty()) {
			ChildCareApplication application;
			User child;
			Address address;
			Phone phone;
			Link link;
			boolean hasMessage = false;
				
			Iterator iter = applications.iterator();
			while (iter.hasNext()) {
				column = 1;
				application = (ChildCareApplication) iter.next();
				child = application.getChild();
				address = getBusiness().getUserBusiness().getUsersMainAddress(child);
				phone = getBusiness().getUserBusiness().getChildHomePhone(child);
				hasMessage = application.getMessage() != null;		
						
				if (application.getApplicationStatus() == getBusiness().getStatusAccepted()) {
					applicationTable.setRowColor(row, ACCEPTED_COLOR);
				}
				else if (application.getApplicationStatus() == getBusiness().getStatusParentsAccept()) {
					applicationTable.setRowColor(row, PARENTS_ACCEPTED_COLOR);
				}
				else if (application.getApplicationStatus() == getBusiness().getStatusContract()) {
					applicationTable.setRowColor(row, CONTRACT_COLOR);
				}
				else {
					if (row % 2 == 0)
						applicationTable.setRowColor(row, getZebraColor1());
					else
						applicationTable.setRowColor(row, getZebraColor2());
				}
					
				link = getSmallLink(child.getNameLastFirst(true));
				link.setEventListener(ChildCareEventListener.class);
				link.setParameter(getSession().getParameterUserID(), String.valueOf(application.getChildId()));
				link.setParameter(getSession().getParameterApplicationID(), application.getPrimaryKey().toString());
				link.setParameter(getSession().getParameterCaseCode(), getBusiness().getAfterSchoolCareCaseCode());
				if (getResponsePage() != null)
					link.setPage(getResponsePage());
	
				if (hasMessage) {
					showMessage = true;
					applicationTable.add(getSmallErrorText("*"), column, row);
					applicationTable.add(getSmallText(Text.NON_BREAKING_SPACE), column, row);
				}

				applicationTable.add(link, column++, row);
				applicationTable.add(getSmallText(PersonalIDFormatter.format(child.getPersonalID(), iwc.getCurrentLocale())), column++, row);
				if (address != null)
					applicationTable.add(getSmallText(address.getStreetAddress()), column++, row);
				else
					applicationTable.add(getSmallText("-"), column++, row);
				if (phone != null)
					applicationTable.add(getSmallText(phone.getNumber()), column++, row++);
				else
					applicationTable.add(getSmallText("-"), column++, row++);
			}

			if (showMessage) {
				applicationTable.setHeight(row++, 2);
				applicationTable.mergeCells(1, row, applicationTable.getColumns(), row);
				applicationTable.add(getSmallErrorText("* "), 1, row);
				applicationTable.add(getSmallText(localize("child_care.has_message_in_application","The application has a message attached")), 1, row++);
			}
			
			applicationTable.setColumnAlignment(2, Table.HORIZONTAL_ALIGN_CENTER);
			applicationTable.setColumnAlignment(4, Table.HORIZONTAL_ALIGN_CENTER);
		}
			
		return applicationTable;
	}
	
	protected AfterSchoolBusiness getAfterSchoolBusiness(IWApplicationContext iwac) {
		try {
			return (AfterSchoolBusiness) IBOLookup.getServiceInstance(iwac, AfterSchoolBusiness.class);
		}
		catch (RemoteException e) {
			throw new IBORuntimeException(e.getMessage());
		}
	}
}