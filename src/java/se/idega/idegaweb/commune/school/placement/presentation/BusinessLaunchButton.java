/*
 * $Id: BusinessLaunchButton.java,v 1.1 2004/10/22 11:21:53 thomas Exp $
 *
 * Copyright (C) 2002 Idega hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 *
 */
package se.idega.idegaweb.commune.school.placement.presentation;


import java.rmi.RemoteException;

import se.idega.idegaweb.commune.childcare.presentation.ChildCareBlock;
import se.idega.idegaweb.commune.school.business.SchoolChoiceBusiness;

import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.business.IBORuntimeException;
import com.idega.presentation.IWContext;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.SubmitButton;

/**
 * This class does something very clever.....
 * 
 * @author <a href="palli@idega.is">Pall Helgason</a>
 * @version 1.0
 */
public class BusinessLaunchButton extends ChildCareBlock {
	protected static final String SUBMIT = "detonate";
	protected static final String SUBMIT2 = "checks";
	
	protected void control(IWContext iwc) throws RemoteException {
		if (iwc.isParameterSet(SUBMIT)) {
			getBusiness(iwc).importLanguageToPlacement();
		}
		
		displayForm();			
	}

	protected void displayForm() {
		Form form = new Form();
		SubmitButton button = new SubmitButton(SUBMIT,"Add language to placements");
		form.add(button);
		add(form);	
	}

	public void init(IWContext iwc) throws RemoteException {
		control(iwc);
	}
	
	private SchoolChoiceBusiness getBusiness(IWContext iwc) {
		try {
			return (SchoolChoiceBusiness) IBOLookup.getServiceInstance(iwc, SchoolChoiceBusiness.class);
		}
		catch (IBOLookupException ile) {
			throw new IBORuntimeException(ile);
		}
	}
}
