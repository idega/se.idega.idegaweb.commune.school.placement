/*
 * $Id: SchoolBlock.java,v 1.2 2005/08/10 15:06:35 thomas Exp $
 * Created on Aug 3, 2005
 *
 * Copyright (C) 2005 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package se.idega.idegaweb.commune.school.presentation;

import java.rmi.RemoteException;
import se.idega.idegaweb.commune.business.CommuneUserBusiness;
import se.idega.idegaweb.commune.care.business.CareBusiness;
import se.idega.idegaweb.commune.childcare.business.AfterSchoolBusiness;
import se.idega.idegaweb.commune.presentation.CommuneBlock;
import se.idega.idegaweb.commune.school.business.CommuneSchoolBusiness;
import se.idega.idegaweb.commune.school.business.CommuneSchoolSession;
import se.idega.idegaweb.commune.school.business.SchoolConstants;
import com.idega.block.school.business.SchoolBusiness;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.business.IBORuntimeException;
import com.idega.core.contact.data.Email;
import com.idega.core.contact.data.Phone;
import com.idega.core.location.data.Address;
import com.idega.core.location.data.PostalCode;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWUserContext;
import com.idega.idegaweb.help.presentation.Help;
import com.idega.presentation.IWContext;
import com.idega.presentation.Table;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.TextArea;
import com.idega.presentation.ui.TextInput;
import com.idega.user.business.NoEmailFoundException;
import com.idega.user.business.NoPhoneFoundException;
import com.idega.user.data.User;
import com.idega.util.PersonalIDFormatter;


/**
 * Last modified: $Date: 2005/08/10 15:06:35 $ by $Author: thomas $
 * 
 * @author <a href="mailto:laddi@idega.com">laddi</a>
 * @version $Revision: 1.2 $
 */
public abstract class SchoolBlock extends CommuneBlock {

	protected static final String STYLENAME_HEADER_CELL = "HeaderCell";
	protected static final String STYLENAME_HEADING_CELL = "HeadingCell";
	protected static final String STYLENAME_TEXT_CELL = "TextCell";
	protected static final String STYLENAME_INPUT_CELL = "InputCell";
	protected static final String STYLENAME_INFORMATION_CELL = "InformationCell";

	private SchoolBusiness sBusiness;
	private CommuneUserBusiness uBusiness;
	private CommuneSchoolBusiness business;
	private CareBusiness careBusiness;
	private CommuneSchoolSession session;
	private AfterSchoolBusiness aBusiness;

	public void main(IWContext iwc) throws Exception {
		initialize(iwc);
		present(iwc);
	}

	public abstract void present(IWContext iwc) throws Exception;

	public SchoolBusiness getSchoolBusiness() {
		return sBusiness;
	}

	public CommuneUserBusiness getUserBusiness() {
		return uBusiness;
	}

	public CommuneSchoolBusiness getBusiness() {
		return business;
	}
	
	public CareBusiness getCareBusiness() {
		return careBusiness;
	}
	
	public AfterSchoolBusiness getAfterSchoolBusiness() {
		return aBusiness;
	}

	public CommuneSchoolSession getSession() {
		return session;
	}

	protected Table getPersonInfoTable(IWContext iwc, User user) throws RemoteException {
		Table table = new Table();
		table.setCellpadding(getCellpadding());
		table.setCellspacing(0);
		table.setColumns(5);
		table.setWidth(3, 12);
		table.setWidth(Table.HUNDRED_PERCENT);
		int row = 1;
		
		Address address = getUserBusiness().getUsersMainAddress(user);
		PostalCode postal = null;
		if (address != null) {
			postal = address.getPostalCode();
		}
		Phone phone = null;
		try {
			phone = getUserBusiness().getUsersHomePhone(user);
		}
		catch (NoPhoneFoundException npfe) {
			phone = null;
		}
		Phone mobile = null;
		try {
			mobile = getUserBusiness().getUsersMobilePhone(user);
		}
		catch (NoPhoneFoundException npfe) {
			mobile = null;
		}
		Email email = null;
		try {
			email = getUserBusiness().getUsersMainEmail(user);
		}
		catch (NoEmailFoundException nefe) {
			email = null;
		}
		
		table.add(getSmallHeader(localize("name", "Name")), 1, row);
		table.add(getText(user.getName()), 2, row);
		
		table.add(getSmallHeader(localize("personal_id", "Personal ID")), 4, row);
		table.add(getText(PersonalIDFormatter.format(user.getPersonalID(), iwc.getCurrentLocale())), 5, row++);
		
		table.add(getSmallHeader(localize("address", "Address")), 1, row);
		table.add(getSmallHeader(localize("zip_code", "Postal code")), 4, row);
		if (address != null) {
			table.add(getText(address.getStreetAddress()), 2, row);
		}
		if (postal != null) {
			table.add(getText(postal.getPostalAddress()), 5, row);
		}
		row++;
		
		table.add(getSmallHeader(localize("home_phone", "Home phone")), 1, row);
		table.add(getSmallHeader(localize("mobile_phone", "Mobile phone")), 4, row);
		if (phone != null && phone.getNumber() != null) {
			table.add(getText(phone.getNumber()), 2, row);
		}
		if (mobile != null && mobile.getNumber() != null) {
			table.add(getText(mobile.getNumber()), 5, row);
		}
		row++;
		
		table.add(getSmallHeader(localize("email", "E-mail")), 1, row);
		if (email != null && email.getEmailAddress() != null) {
			table.add(getText(email.getEmailAddress()), 2, row);
		}
		row++;
		
		table.setHeight(row, 6);
		table.mergeCells(1, row, table.getColumns(), row);
		table.setBottomCellBorder(1, row++, 1, "#D7D7D7", "solid");
		table.setHeight(row++, 6);
		
		return table;
	}
	
	protected void setColorToCell(Table table, int column, int row, String color) {
		table.setColor(column, row, color);
		table.setCellBorder(column, row, 1, "#000000", "solid");
	}
	
	protected DropdownMenu getDropdown(String parameterName, Object selectedElement) {
		DropdownMenu drop = (DropdownMenu) getStyledInterface(new DropdownMenu(parameterName));
		drop.setWidth("100%");
		if (selectedElement != null) {
			drop.setSelectedElement(selectedElement.toString());
		}
		
		return drop;
	}
	
	protected TextInput getTextInput(String parameterName, Object content) {
		return getTextInput(parameterName, content, false);
	}
	
	protected TextInput getTextInput(String parameterName, Object content, boolean disabled) {
		TextInput input = (TextInput) getStyledInterface(new TextInput(parameterName));
		input.setWidth("100%");
		if (content != null) {
			input.setContent(content.toString());
		}
		input.setDisabled(disabled);
		
		return input;
	}
	
	protected TextArea getTextArea(String parameterName, Object content) {
		TextArea area = (TextArea) getStyledInterface(new TextArea(parameterName));
		area.setWidth("100%");
		if (content != null) {
			area.setContent(content.toString());
		}
		
		return area;
	}
	
	protected Help getHelpButton(String key) {
		Help help = new Help();
		help.setHelpTextBundle(SchoolConstants.HELP_BUNDLE_IDENTFIER);
		help.setHelpTextKey(key);
		help.setImage(getBundle().getImage("help.gif"));
		return help;
	}

	public String getBundleIdentifier() {
		return IW_BUNDLE_IDENTIFIER;
	}

	private void initialize(IWContext iwc) {
		setResourceBundle(getResourceBundle(iwc));
		setBundle(getBundle(iwc));
		business = getCommuneSchoolBusiness(iwc);
		session = getCommuneSchoolSession(iwc);
		sBusiness = getSchoolBusiness(iwc);
		uBusiness = getUserBusiness(iwc);
		careBusiness = getCareBusiness(iwc);
		aBusiness = getAfterSchoolBusiness(iwc);
	}

	private CommuneSchoolBusiness getCommuneSchoolBusiness(IWApplicationContext iwac) {
		try {
			return (CommuneSchoolBusiness) IBOLookup.getServiceInstance(iwac, CommuneSchoolBusiness.class);
		}
		catch (IBOLookupException ile) {
			throw new IBORuntimeException(ile);
		}
	}

	private AfterSchoolBusiness getAfterSchoolBusiness(IWApplicationContext iwac) {
		try {
			return (AfterSchoolBusiness) IBOLookup.getServiceInstance(iwac, AfterSchoolBusiness.class);
		}
		catch (IBOLookupException ile) {
			throw new IBORuntimeException(ile);
		}
	}

	private CommuneSchoolSession getCommuneSchoolSession(IWUserContext iwuc) {
		try {
			return (CommuneSchoolSession) IBOLookup.getSessionInstance(iwuc, CommuneSchoolSession.class);
		}
		catch (IBOLookupException ile) {
			throw new IBORuntimeException(ile);
		}
	}

	private SchoolBusiness getSchoolBusiness(IWApplicationContext iwac) {
		try {
			return (SchoolBusiness) IBOLookup.getServiceInstance(iwac, SchoolBusiness.class);
		}
		catch (IBOLookupException ile) {
			throw new IBORuntimeException(ile);
		}
	}

	protected CommuneUserBusiness getUserBusiness(IWApplicationContext iwac) {
		try {
			return super.getUserBusiness(iwac);
		}
		catch (IBOLookupException ile) {
			throw new IBORuntimeException(ile);
		}
	}

	private CareBusiness getCareBusiness(IWApplicationContext iwac) {
		try {
			return (CareBusiness) IBOLookup.getServiceInstance(iwac, CareBusiness.class);
		}
		catch (IBOLookupException ile) {
			throw new IBORuntimeException(ile);
		}
	}
}