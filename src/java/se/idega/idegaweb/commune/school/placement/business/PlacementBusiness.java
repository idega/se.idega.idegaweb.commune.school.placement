/*
 * $Id: PlacementBusiness.java,v 1.1 2004/10/22 12:57:49 thomas Exp $
 * Created on Oct 18, 2004
 *
 * Copyright (C) 2004 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package se.idega.idegaweb.commune.school.placement.business;

import java.rmi.RemoteException;
import se.idega.idegaweb.commune.school.business.CentralPlacementException;
import com.idega.block.school.data.SchoolClassMember;
import com.idega.business.IBOService;
import com.idega.presentation.IWContext;
import com.idega.user.data.User;


/**
 * 
 *  Last modified: $Date: 2004/10/22 12:57:49 $ by $Author: thomas $
 * 
 * @author <a href="mailto:thomas@idega.com">thomas</a>
 * @version $Revision: 1.1 $
 */
public interface PlacementBusiness extends IBOService {

	/**
	 * @see se.idega.idegaweb.commune.school.placement.business.PlacementBusinessBean#getBundleIdentifier
	 */
	public String getBundleIdentifier() throws java.rmi.RemoteException;

	/**
	 * @see se.idega.idegaweb.commune.school.placement.business.PlacementBusinessBean#storeSchoolClassMember
	 */
	public SchoolClassMember storeSchoolClassMember(IWContext iwc, int childID) throws RemoteException,
			CentralPlacementException;

	/**
	 * @see se.idega.idegaweb.commune.school.placement.business.PlacementBusinessBean#rejectApplication
	 */
	public void rejectApplication(int applicationID, int seasonID, User performer, String messageSubject, String messageBody) throws RemoteException;
}
