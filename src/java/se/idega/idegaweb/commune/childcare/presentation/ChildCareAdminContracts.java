/*
 */
package se.idega.idegaweb.commune.childcare.presentation;

import is.idega.idegaweb.member.presentation.UserSearcher;

import java.rmi.RemoteException;
import java.util.Collection;

import javax.ejb.FinderException;

import se.idega.idegaweb.commune.business.CommuneUserBusiness;
import se.idega.idegaweb.commune.childcare.business.ChildCareBusiness;
import se.idega.idegaweb.commune.childcare.check.business.CheckBusiness;

import com.idega.block.school.business.SchoolBusiness;
import com.idega.block.school.data.School;
import com.idega.business.IBOLookup;
import com.idega.core.location.data.Address;
import com.idega.presentation.IWContext;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.Table;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.DateInput;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.Parameter;
import com.idega.presentation.ui.TextInput;
import com.idega.presentation.ui.util.SelectorUtility;
import com.idega.user.data.User;

/**
 * @author palli
 *
 * A class for the presenation layer for creating contracts centrally.
 */
public class ChildCareAdminContracts extends ChildCareBlock {
	private User _child = null;
	private User _parent1 = null;
	private User _parent2 = null;
	private School _provider = null;
	private int _nameInputLength = 25;
	private int _personalIdInputLength = 15;
	
	private final static String PARAM_CHILD = "child_search";
	private final static String PARAM_PARENT1 = "parent1_search";
	private final static String PARAM_PARENT2 = "parent2_search";
	
	private final static String PARAM_COMMENT = "prm_comment";
	private final static String PARAM_GETBILL1 = "prm_getbill1";
	private final static String PARAM_GETBILL2 = "prm_getbill1";
	private final static String PARAM_PROVIDER = "prm_provider";
	private final static String PARAM_OPERATION = "prm_operation";
	private final static String PARAM_GROUP = "prm_group";
//	private final static String PARAM_PRESCHOOL = "prm_preschool";
	private final static String PARAM_HOURS = "prm_hours";
	private final static String PARAM_PARENTHOME = "prm_parenthome";
	private final static String PARAM_UNEMPLOYED = "prm_unemployed";
	private final static String PARAM_EXTRA_CONTRACT_HOUR = "prm_ex_con_hour";
	private final static String PARAM_EXTRA_CONTRACT_HOUR_ABOUT = "prm_ex_con_hour_about";
	private final static String PARAM_EXTRA_CONTRACT_ALSO = "prm_ex_con_also";
	private final static String PARAM_EXTRA_CONTRACT_ALSO_ABOUT = "prm_ex_con_also_about";
	private final static String PARAM_PLACEMENT_DATE = "prm_plac_date";
	private final static String PARAM_ANSWER_DATE = "prm_answ_date";
	
	
	private final static String LABEL_SCHOOL_CATEGORY = "child_care.category";
	private final static String LABEL_COMMENT = "child_care.comment";
	private final static String LABEL_CHILD = "child_care.child";
	private final static String LABEL_ADDRESS = "child_care.address";
	private final static String LABEL_GRANTED_CHECK = "child_care.granted_check";
	private final static String LABEL_CUSTODIAN1 = "child_care.custodian1";
	private final static String LABEL_CUSTODIAN2 = "child_care.custodian2";
	private final static String LABEL_GETS_BILL = "child_care.gets_bill";
	private final static String LABEL_PROVIDER = "child_care.provider";
	private final static String LABEL_PROVIDER_NAME = "child_care.provider_name";
	private final static String LABEL_PROVIDER_OPERATION = "child_care.provider_operation";
	private final static String LABEL_PROVIDER_GROUP = "child_care.provider_group";
//  private final static String LABEL_PROVIDER_PRESCHOOL = "child_care.provider_preschool";
	private final static String LABEL_HOURS = "child_care.hours_pr_week";
	private final static String LABEL_PARENT_AT_HOME = "child_care.parent_at_home";
	private final static String LABEL_UNEMPLOYED = "child_care.no_job";
	private final static String LABEL_EXTRA_CONTRACT_HOUR = "child_care.ext_cont_hour";
	private final static String LABEL_EXTRA_CONTRACT_ALSO = "child_care.ext_cont_also";
	private final static String LABEL_EXTRA_CONTRACT_ABOUT = "child_care.ext_cont_about";
	private final static String LABEL_PLACEMENT_DATE = "child_care.placement_date";
	private final static String LABEL_ANSWER_DATE = "child_care.answer_date";
	
	private final static String LABEL_NEW_PROVIDER = "child_care.new_provider";
	
	/* (non-Javadoc)
	 * @see com.idega.presentation.PresentationObject#main(com.idega.presentation.IWContext)
	 */
	public void init(IWContext iwc) throws Exception {
		process(iwc);
		showForm(iwc);
	}
	
	public void process(IWContext iwc) {
		String prm = UserSearcher.getUniqueUserParameterName(PARAM_CHILD);
		if (iwc.isParameterSet(prm)) {
			Integer firstUserID = Integer.valueOf(iwc.getParameter(prm));
			try {
				_child = getUserService(iwc).getUser(firstUserID);
			}
			catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		prm = UserSearcher.getUniqueUserParameterName(PARAM_PARENT1);
		if (iwc.isParameterSet(prm)) {
			Integer secondUserID = Integer.valueOf(iwc.getParameter(prm));
			try {
				_parent1 = getUserService(iwc).getUser(secondUserID);
			}
			catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		prm = UserSearcher.getUniqueUserParameterName(PARAM_PARENT2);
		if (iwc.isParameterSet(prm)) {
			Integer secondUserID = Integer.valueOf(iwc.getParameter(prm));
			try {
				_parent2 = getUserService(iwc).getUser(secondUserID);
			}
			catch (RemoteException e) {
				e.printStackTrace();
			}
		}

		if (iwc.isParameterSet(PARAM_SUBMIT)) {
			//TODO: Laddi - Tja, safna saman stupid parametrum og senda í createContract fall...
		}
	}
	
	public void showForm(IWContext iwc) {
		Form form = new Form();
		Table t = new Table(4,30);
		t.mergeCells(1,4,4,4);
		t.mergeCells(1,8,4,8);
		t.mergeCells(1,11,4,11);
		
		t.add(getLocalizedLabel(LABEL_SCHOOL_CATEGORY,"School category"),1,1);
		t.add(getLocalizedLabel(LABEL_COMMENT,"Comment"),1,2);
		t.add(getLocalizedLabel(LABEL_CHILD,"Child"),1,3);
		
		t.add(getLocalizedLabel(LABEL_ADDRESS,"Address"),1,5);
		t.add(getLocalizedLabel(LABEL_GRANTED_CHECK,"Granted check"),1,6);
		t.add(getLocalizedLabel(LABEL_CUSTODIAN1,"Custodion 1"),1,7);
		
		t.add(getLocalizedLabel(LABEL_GETS_BILL,"Gets bill"),1,9);
		t.add(getLocalizedLabel(LABEL_CUSTODIAN2,"Custodion 2"),1,10);

		t.add(getLocalizedLabel(LABEL_GETS_BILL,"Gets bill"),1,12);

		t.add(getLocalizedLabel(LABEL_PROVIDER,"Provider"),1,14);
		t.add(getLocalizedLabel(LABEL_PROVIDER_NAME,"Name"),1,15);
		t.add(getLocalizedLabel(LABEL_PROVIDER_OPERATION,"Operation"),1,16);
		t.add(getLocalizedLabel(LABEL_PROVIDER_GROUP,"Group"),1,17);
//		t.add(getLocalizedLabel(LABEL_PROVIDER_PRESCHOOL,"Preschool"),1,18);
		
		t.add(getLocalizedLabel(LABEL_HOURS,"Hours pr. week"),1,20);
		t.add(getLocalizedLabel(LABEL_PARENT_AT_HOME,"Parent at home"),1,21);
		t.add(getLocalizedLabel(LABEL_UNEMPLOYED,"Unemployed"),1,22);
		
		t.add(getLocalizedLabel(LABEL_EXTRA_CONTRACT_HOUR,"Extra contract hour"),1,24);
		t.add(getLocalizedLabel(LABEL_EXTRA_CONTRACT_ABOUT,"About"),3,24);
		t.add(getLocalizedLabel(LABEL_EXTRA_CONTRACT_ALSO,"Extra contract also"),1,25);
		t.add(getLocalizedLabel(LABEL_EXTRA_CONTRACT_ABOUT,"About"),3,25);
		
		t.add(getLocalizedLabel(LABEL_PLACEMENT_DATE,"Placement date"),1,27);
		t.add(getLocalizedLabel(LABEL_ANSWER_DATE,"Answer date"),1,28);

		t.add(getLocalizedString("child_care.cild_care","Child care",iwc),2,1);
		t.add(getStyledInterface(new TextInput(PARAM_COMMENT)),2,2);
		t.add(getSearchItem(iwc,PARAM_CHILD),1,4);
		if (_child != null) {
			try {
				Address addr = getUserService(iwc).getUsersMainAddress(_child);
				if (addr != null)
					t.add(addr.getName(),2,5);  //TODO: Laddi - put in the right format for the address
			}
			catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		
		boolean hasCheck = false;
		try {
			if (_child != null)
				hasCheck = getCheckBusiness(iwc).hasGrantedCheck(_child);
		}
		catch (RemoteException e) {
			e.printStackTrace();
		}

		if (!hasCheck)
			t.add(localize("child_care.no","No"),2,6);
		else
			t.add(localize("child_care.yes","Yes"),2,6);
		
		t.add(getSearchItem(iwc,PARAM_PARENT1),1,8);
		t.add(getCheckBox(PARAM_GETBILL1,"true"),2,9);
		t.add(getSearchItem(iwc,PARAM_PARENT2),1,11);
		t.add(getCheckBox(PARAM_GETBILL2,"true"),2,12);
		
		try {
			Collection schools = getSchoolBusiness(iwc).getSchoolHome().findAllByCategory(getSchoolBusiness(iwc).getCategoryChildcare());
			SelectorUtility sel = new SelectorUtility();
			DropdownMenu prov = (DropdownMenu) sel.getSelectorFromIDOEntities(new DropdownMenu(PARAM_PROVIDER), schools, "getName");
			t.add(getStyledInterface(prov),2,15);
		}
		catch (RemoteException e1) {
			e1.printStackTrace();
		}
		catch (FinderException e1) {
			e1.printStackTrace();
		}
		
		try {
			Collection school_types = getSchoolBusiness(iwc).getSchoolTypeHome().findAllByCategory(getSchoolBusiness(iwc).getCategoryChildcare().getCategory());
			SelectorUtility sel = new SelectorUtility();
			//TODO: Laddi - Nota localized hérna?
			DropdownMenu op = (DropdownMenu) sel.getSelectorFromIDOEntities(new DropdownMenu(PARAM_OPERATION), school_types, "getName");
			t.add(getStyledInterface(op),2,16);
		}
		catch (RemoteException e1) {
			e1.printStackTrace();
		}
		catch (FinderException e1) {
			e1.printStackTrace();
		}
		
		//TODO: Laddi - Put in link to create new provider
		
		try {
			//TODO: Laddi - Þarf að laga þannig að school sé notaður
			Collection school_classes = getSchoolBusiness(iwc).getSchoolClassHome().findBySchoolAndCategory(-1,getSchoolBusiness(iwc).getCategoryChildcare().getCategory());
			SelectorUtility sel = new SelectorUtility();
			//TODO: Laddi - Nota localized hérna?
			DropdownMenu op = (DropdownMenu) sel.getSelectorFromIDOEntities(new DropdownMenu(PARAM_GROUP), school_classes, "getName");
			t.add(getStyledInterface(op),2,16);
		}
		catch (RemoteException e1) {
			e1.printStackTrace();
		}
		catch (FinderException e1) {
			e1.printStackTrace();
		}
		
		t.add(getStyledInterface(new TextInput(PARAM_HOURS)),2,20);
		t.add(getCheckBox(PARAM_PARENTHOME,"true"),2,21);
		t.add(getCheckBox(PARAM_UNEMPLOYED,"true"),2,22);
		
		t.add(getCheckBox(PARAM_EXTRA_CONTRACT_HOUR,"true"),2,24);
		t.add(getStyledInterface(new TextInput(PARAM_EXTRA_CONTRACT_HOUR_ABOUT)),4,24);
		t.add(getCheckBox(PARAM_EXTRA_CONTRACT_ALSO,"true"),2,25);
		t.add(getStyledInterface(new TextInput(PARAM_EXTRA_CONTRACT_ALSO_ABOUT)),4,25);
						
		t.add(getStyledInterface(new DateInput(PARAM_PLACEMENT_DATE)),2,27);
		t.add(getStyledInterface(new DateInput(PARAM_ANSWER_DATE)),2,28);
						
		t.add(getSubmitButton(PARAM_SUBMIT),1,30);				
												
		form.add(t);
		add(form);
	}
	

	public PresentationObject getSearchItem(IWContext iwc, String name) {
		Table table = new Table();
		UserSearcher searcher = new UserSearcher();
		searcher.setShowMiddleNameInSearch(false);
		searcher.setOwnFormContainer(false);
		searcher.setUniqueIdentifier(name);
		searcher.setSkipResultsForOneFound(false);
		searcher.setHeaderFontStyleName(getStyleName(STYLENAME_SMALL_HEADER));
		searcher.setButtonStyleName(getStyleName(STYLENAME_INTERFACE_BUTTON));
		searcher.setPersonalIDLength(_personalIdInputLength);
		searcher.setFirstNameLength(_nameInputLength);
		searcher.setLastNameLength(_nameInputLength);
		if (iwc.isParameterSet(name)) {
			searcher.maintainParameter(new Parameter(name, iwc.getParameter(name)));
		}
		table.add(searcher, 1, 1);
		
		return table;
	}

	private CommuneUserBusiness getUserService(IWContext iwc) throws RemoteException {
		return (CommuneUserBusiness) IBOLookup.getServiceInstance(iwc, CommuneUserBusiness.class);
	}

	private ChildCareBusiness getChildCareBusiness(IWContext iwc) throws RemoteException {
		return (ChildCareBusiness) IBOLookup.getServiceInstance(iwc, ChildCareBusiness.class);
	}

	private SchoolBusiness getSchoolBusiness(IWContext iwc) throws RemoteException {
		return (SchoolBusiness) IBOLookup.getServiceInstance(iwc, SchoolBusiness.class);
	}

	private CheckBusiness getCheckBusiness(IWContext iwc) throws RemoteException {
		return (CheckBusiness) IBOLookup.getServiceInstance(iwc, CheckBusiness.class);
	}
 
	/*	private PresentationObject getBruttoIncomeEditorButton(IWContext iwc) {
			GenericButton button = new SubmitButton(localize("household.edit_brutto_income", "Edit brutto income"));
			button = getButton(button);
			if (hasUser && userBruttoIncomePageID != null) {
				button.setPageToOpen(userBruttoIncomePageID.intValue());
			}
			else if (hasUser && userBruttoIncomeWindowClass != null) {
				button.setOnClick(
					getButtonOnClickForWindow(iwc, userBruttoIncomeWindowClass, userBruttoIncomeUserParameterName));
			}
			else {
				button.setDisabled(true);
			}
			return button;
		}
		private String getButtonOnClickForWindow(IWContext iwc, Class windowClass, String userParameterName) {
			String prm = "";
			if (userParameterName != null)
				prm = "&" + userParameterName + "=" + "'+this.form.usr_drp.value+' ";
			String URL = Window.getWindowURL(windowClass, iwc) + prm;
			return "javascript:" + Window.getCallingScriptString(windowClass, URL, true, iwc) + ";return false;";
		}*/

 	/**
	 * @return
	 */
	public int getNameInputLength() {
		return _nameInputLength;
	}
	/**
	 * @param nameInputLength
	 */
	public void setNameInputLength(int nameInputLength) {
		_nameInputLength = nameInputLength;
	}
	/**
	 * @return
	 */
	public int getPersonalIdInputLength() {
		return _personalIdInputLength;
	}
	/**
	 * @param personalIdInputLength
	 */
	public void setPersonalIdInputLength(int personalIdInputLength) {
		_personalIdInputLength = personalIdInputLength;
	}
	
	/**
	 * Returns a formatted and localized form label.
	 * @param textKey the text key to localize
	 * @param defaultText the default localized text
	 * @author anders
	 */
	protected Text getLocalizedLabel(String textKey, String defaultText) {
		return getSmallHeader(localize(textKey, defaultText) + ":");
	}
}