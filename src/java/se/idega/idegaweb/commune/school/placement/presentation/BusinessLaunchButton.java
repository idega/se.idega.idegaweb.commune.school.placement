/*
 * $Id: BusinessLaunchButton.java,v 1.3 2004/12/07 21:22:20 laddi Exp $
 *
 * Copyright (C) 2002 Idega hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 *
 */
package se.idega.idegaweb.commune.school.placement.presentation;


import java.rmi.RemoteException;

import se.idega.idegaweb.commune.business.NackaFixBusiness;
import se.idega.idegaweb.commune.childcare.presentation.ChildCareBlock;

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
			getBusiness(iwc).fixElementarySchoolPlacements();
		}
		if (iwc.isParameterSet(SUBMIT2)) {
			getBusiness(iwc).fixChildCarePlacements();
		}
		
		displayForm();			
	}

	protected void displayForm() {
		Form form = new Form();
		SubmitButton button = new SubmitButton(SUBMIT,"Fix school placements");
		SubmitButton button2 = new SubmitButton(SUBMIT2,"Fix child care placements");
		form.add(button);
		form.add(button2);
		add(form);	
	}

	public void init(IWContext iwc) throws RemoteException {
		control(iwc);
	}
	
	private NackaFixBusiness getBusiness(IWContext iwc) {
		try {
			return (NackaFixBusiness) IBOLookup.getServiceInstance(iwc, NackaFixBusiness.class);
		}
		catch (IBOLookupException ile) {
			throw new IBORuntimeException(ile);
		}
	}
}
