/*
 * Created on 5.10.2003
 */
package se.idega.idegaweb.commune.childcare.presentation;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;
import se.idega.idegaweb.commune.care.business.CareConstants;
import se.idega.idegaweb.commune.care.data.AfterSchoolChoice;
import se.idega.idegaweb.commune.care.data.AfterSchoolChoiceHome;
import se.idega.idegaweb.commune.care.data.ChildCareApplication;
import se.idega.idegaweb.commune.childcare.business.AfterSchoolBusiness;
import se.idega.idegaweb.commune.childcare.event.ChildCareEventListener;
import com.idega.block.school.data.SchoolClass;
import com.idega.block.school.data.SchoolClassMember;
import com.idega.block.school.data.SchoolSeason;
import com.idega.business.IBOLookup;
import com.idega.business.IBORuntimeException;
import com.idega.core.contact.data.Phone;
import com.idega.core.location.data.Address;
import com.idega.data.IDOLookup;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.Table2;
import com.idega.presentation.TableCell2;
import com.idega.presentation.TableRow;
import com.idega.presentation.TableRowGroup;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.ListItem;
import com.idega.presentation.text.Lists;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.HiddenInput;
import com.idega.presentation.ui.Label;
import com.idega.presentation.ui.SubmitButton;
import com.idega.user.data.User;
import com.idega.util.IWTimestamp;
import com.idega.util.PersonalIDFormatter;

/**
 * @author laddi
 */
public class AfterSchoolChoiceApprover extends ChildCareBlock {

	public static final String PARAMETER_ACTION = "sch_action";
	
	private int action = 0;
	private String sortStudentsBy = "c.QUEUE_DATE";
	private String sortChoicesBy = "";

	private final String PARAMETER_SORT = "sch_choice_sort";
	private final String PARAMETER_CREATE_CONTRACTS = "sch_create_contracts";
	
	private final int ACTION_MANAGE = 1;
	public static final int ACTION_SAVE = 2;
	private final int ACTION_CREATE_CONTRACTS = 3;

	private boolean iShowCreateContractsButton = false;
	
	private boolean showFClass = false;
	
	public void init(IWContext iwc) throws Exception {
		if (iwc.isLoggedOn()) {
			parseAction(iwc);
			drawForm(iwc);

			/*switch (action) {
			case ACTION_MANAGE:
				drawForm(iwc);
				break;
			case ACTION_CREATE_CONTRACTS:
				handleCreateContracts(iwc);
				break;
			}*/
		}
		else {
			add(super.getSmallHeader(localize("not_logged_on", "Not logged on")));
		}
	}
	
	private void parseAction(IWContext iwc) {
		if (iwc.isParameterSet(PARAMETER_CREATE_CONTRACTS))
			action = ACTION_CREATE_CONTRACTS;		
		else if (iwc.isParameterSet(PARAMETER_ACTION))
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
		form.setID("afterSchoolChoiceApprover");
		form.setStyleClass("adminForm");
		form.setEventListener(ChildCareEventListener.class);
		form.add(new HiddenInput(PARAMETER_ACTION, String.valueOf(action)));

		form.add(getNavigation());
		form.add(getApplications(iwc));
		form.add(getLegend());
		
		add(form);
	}
	
	///malin
	private Collection getApplicationCollection(IWApplicationContext iwac, String sorting) throws RemoteException {
		Collection applications = getAfterSchoolBusiness(iwac).findChoicesByProvider(getSession().getChildCareID(), sorting);
		return applications;
	}
	
	protected Layer getNavigation() throws RemoteException {
		Layer layer = new Layer(Layer.DIV);
		layer.setStyleClass("formSection");
		
		DropdownMenu seasons = getSeasons();
		
		DropdownMenu menu = new DropdownMenu(PARAMETER_SORT);
		menu.addMenuElement("c.QUEUE_DATE", localize("childcare.sort_queuedate", "Queue date"));
		menu.addMenuElement("iu.LAST_NAME", localize("childcare.sort_name", "Name"));
		menu.addMenuElement("iu.PERSONAL_ID", localize("childcare.sort_personal_id", "Personal ID"));
		menu.setSelectedElement(sortChoicesBy);
		menu.setToSubmit();		
		
		Layer formItem = new Layer(Layer.DIV);
		formItem.setStyleClass("formItem");
		Label label = new Label(localize("child_care.season", "Season"), seasons);
		formItem.add(label);
		formItem.add(seasons);
		layer.add(formItem);

		formItem = new Layer(Layer.DIV);
		formItem.setStyleClass("formItem");
		label = new Label(localize("school.sort_by", "Sort by"), menu);
		formItem.add(label);
		formItem.add(menu);
		layer.add(formItem);

		if (iShowCreateContractsButton ) {
			SubmitButton createContracts = new SubmitButton(PARAMETER_CREATE_CONTRACTS, localize("childcare.create_contracts", "Create contracts"));
			createContracts.setSubmitConfirm(localize("childcare.confirm_create_contracts", "Create afterschool contracts for students with school placement."));

			formItem = new Layer(Layer.DIV);
			formItem.setStyleClass("formItem");
			label = new Label(localize("childcare.create_contracts", "Create contracts"), createContracts);
			formItem.add(label);
			formItem.add(createContracts);
			layer.add(formItem);
		}
		
		Layer clearLayer = new Layer(Layer.DIV);
		clearLayer.setStyleClass("Clear");
		layer.add(clearLayer);
		
		return layer;
	}
	
	private Table2 getApplications(IWContext iwc) throws RemoteException {
		Table2 table = new Table2();
		table.setStyleClass("adminTable");
		table.setStyleClass("ruler");
		table.setWidth("100%");
		table.setCellpadding(0);
		table.setCellspacing(0);
		
		TableRowGroup group = table.createHeaderRowGroup();
		TableRow row = group.createRow();
		TableCell2 cell = row.createHeaderCell();
		cell.setStyleClass("firstColumn");
		cell.setStyleClass("name");
		cell.add(new Text(getResourceBundle().getLocalizedString("child_care.name", "Name")));
		
		cell = row.createHeaderCell();
		cell.setStyleClass("personalID");
		cell.add(new Text(getResourceBundle().getLocalizedString("child_care.personal_id", "Personal ID")));
		
		cell = row.createHeaderCell();
		cell.setStyleClass("address");
		cell.add(new Text(getResourceBundle().getLocalizedString("child_care.address", "Address")));
		
		cell = row.createHeaderCell();
		cell.setStyleClass("phone");
		cell.setStyleClass("lastColumn");
		cell.add(new Text(getResourceBundle().getLocalizedString("child_care.phone", "Phone")));
		
		group = table.createBodyRowGroup();
		int iRow = 1;
		
		//boolean showMessage = false;
		Collection applications = getApplicationCollection(iwc, sortStudentsBy);
		if (applications != null && !applications.isEmpty()) {
			ChildCareApplication application;
			User child;
			Address address;
			Phone phone;
			Link link;
			//boolean hasMessage = false;
			String name = null;
			//boolean showPriority = false;
			IWTimestamp today = new IWTimestamp();
			int selectedSeason = Integer.valueOf(getSeasons().getSelectedElementValue()).intValue();

			Iterator iter = applications.iterator();
			while (iter.hasNext()) {
				row = group.createRow();
				
				application = (ChildCareApplication) iter.next();
				child = application.getChild();
				address = getBusiness().getUserBusiness().getUsersMainAddress(child);
				phone = getBusiness().getUserBusiness().getChildHomePhone(child);
				//hasMessage = application.getMessage() != null;
				boolean active = false;
				AfterSchoolChoice afc = null;
				AfterSchoolChoiceHome home = (AfterSchoolChoiceHome) IDOLookup.getHome(AfterSchoolChoice.class);
				int itemSeason = 0;
				try {
					afc = home.findByPrimaryKey(application.getPrimaryKey());
					if (afc != null)
						itemSeason = afc.getSchoolSeasonId();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				if (selectedSeason == itemSeason) {  // filter for seasons added by Igors 10.01.2006
					boolean isFClass = afc.getFClass();
					if (showFClass) {
						if (!isFClass) {
							continue;
						}
					}
					else {
						if (isFClass) {
							continue;
						}
					}
					SchoolClassMember member;
					IWTimestamp terminated = null;
					IWTimestamp startdate = null;
					Collection placings = getBusiness().getSchoolBusiness().findClassMemberInChildCare(
							((Integer) child.getPrimaryKey()).intValue(), getSession().getChildCareID());
					Iterator iterPlac = placings.iterator();
					while (iterPlac.hasNext()) {
						member = (SchoolClassMember) iterPlac.next();
						SchoolClass schoolGroup = member.getSchoolClass();
						SchoolSeason season = schoolGroup.getSchoolSeason();
						if (season != null
								&& ((Integer) season.getPrimaryKey()).intValue() == getSession().getSeasonID()) {
							if (member.getRemovedDate() != null) {
								terminated = new IWTimestamp(member.getRemovedDate());
								if (terminated.isEarlierThan(today))
									active = false;
								else
									active = true;
							}
							else if (member.getRegisterDate() != null) {
								startdate = new IWTimestamp(member.getRegisterDate());
								if (startdate.isLaterThan(today)) {
									active = true;
								}
								else if (startdate.isEarlierThan(today) && member.getRemovedDate() == null) {
									active = true;
								}
								else
									active = false;
							}
							else {
								active = true;
							}
						}
					}
					if (!active) {
						if (iRow % 2 == 0) {
							row.setStyleClass("evenRow");
						}
						else {
							row.setStyleClass("oddRow");
						}
						if (application.getApplicationStatus() == getBusiness().getStatusAccepted()) {
							row.setStyleClass("accepted");
						}
						else if (application.getApplicationStatus() == getBusiness().getStatusParentsAccept()) {
							row.setStyleClass("parentAccepted");
						}
						else if (application.getApplicationStatus() == getBusiness().getStatusContract()) {
							row.setStyleClass("contract");
						}

						name = getBusiness().getUserBusiness().getNameLastFirst(child, true);
						link = new Link(name);
						link.setEventListener(ChildCareEventListener.class);
						link.setParameter(getSession().getParameterUserID(), String.valueOf(application.getChildId()));
						link.setParameter(getSession().getParameterApplicationID(),
								application.getPrimaryKey().toString());
						link.setParameter(getSession().getParameterCaseCode(), CareConstants.AFTER_SCHOOL_CASE_CODE_KEY);
						if (getResponsePage() != null)
							link.setPage(getResponsePage());

						cell = row.createCell();
						cell.setStyleClass("firstColumn");
						cell.setStyleClass("name");
						
						/*boolean hasQueuePriority = application.getHasQueuePriority();
						if (hasQueuePriority) {
							showPriority = true;
							Text priority = new Text("&Delta;");
							priority.setStyleClass("required");
							cell.add(priority);
						}
						if (hasMessage) {
							showMessage = true;
							Text message = new Text("*");
							message.setStyleClass("required");
							cell.add(message);
						}*/
						cell.add(link);
						
						cell = row.createCell();
						cell.setStyleClass("personalID");
						cell.add(new Text(PersonalIDFormatter.format(child.getPersonalID(), iwc.getCurrentLocale())));
						
						cell = row.createCell();
						cell.setStyleClass("address");
						if (address != null) {
							cell.add(new Text(address.getStreetAddress()));
						}
						else {
							cell.add(new Text("-"));
						}
						
						cell = row.createCell();
						cell.setStyleClass("phone");
						cell.setStyleClass("lastColumn");
						if (phone != null) {
							cell.add(new Text(phone.getNumber()));
						}
						else {
							cell.add(new Text("-"));
						}
						
						iRow++;
					} // active
				} // season filter
			} // while
		}
		return table;
	}
	
	private Lists getLegend() {
		Lists list = new Lists();
		list.setStyleClass("legend");
		
		ListItem item = new ListItem();
		item.setStyleClass("accepted");
		item.add(new Text(localize("child_care.application_status_accepted", "Accepted")));
		list.add(item);
		
		item = new ListItem();
		item.setStyleClass("parentAccepted");
		item.add(new Text(localize("child_care.application_status_parents_accepted", "Parents accepted")));
		list.add(item);

		item = new ListItem();
		item.setStyleClass("contract");
		item.add(new Text(localize("child_care.application_status_contract", "Contract")));
		list.add(item);
		
		return list;
	}

	/*private void handleCreateContracts(IWContext iwc) throws RemoteException {
		String message = localize("childcare.contracts_created_for", "Contracts created for the following students:");
		String errorMessage = null;
		Form form = new Form();
		Table table = new Table();
		table.setWidth(Table.HUNDRED_PERCENT);
		table.setCellpadding(getCellpadding());
		table.setCellspacing(getCellspacing());
		
		Collection students = createContracts(iwc);
		if (students == null) {
			errorMessage = localize("childcare.create_contracts_error", "Could not create contracts.");
		} else {
			if (students.size() == 0) {
				message = localize("childcare.no_contracts_created", "No students with pending after-school application and school placement.");
			}
			int row = 1;
			Iterator iter = students.iterator();
			while (iter.hasNext()) {
				User student = (User) iter.next();
				String name = student.getLastName() + ", " + student.getFirstName();
				table.add(getSmallText(name), 1, row++);
			}
 			form.add(table);
			form.add(new Break(2));
		}
		
		SubmitButton backButton = (SubmitButton) getStyledInterface(new SubmitButton("", localize("childcare.back", "Back")));
		form.add(backButton);
		
		if (errorMessage != null) {
			add(getErrorText(errorMessage));
		} else {
			add(getSmallHeader(message));
		}
		add(new Break(2));
		add(form);
	}
	
	private Collection createContracts(IWContext iwc) throws RemoteException {
		return getAfterSchoolBusiness(iwc).createContractsForChildrenWithSchoolPlacement(getSession().getChildCareID(), iwc.getCurrentUser(), iwc.getCurrentLocale());
	}*/
	
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
			// empty			
		}
		//this.searchEnabled = searchEnabled;
	}

	
	public void setShowCreateContractsButton(boolean showCreateContractsButton) {
		iShowCreateContractsButton = showCreateContractsButton;
	}

	public boolean getShowFClass() {
		return showFClass;
	}

	public void setShowFClass(boolean showFClass) {
		this.showFClass = showFClass;
	}
}