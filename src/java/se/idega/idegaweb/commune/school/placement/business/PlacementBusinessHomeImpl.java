/*
 * $Id: PlacementBusinessHomeImpl.java,v 1.1 2004/10/22 12:57:49 thomas Exp $
 * Created on Oct 18, 2004
 *
 * Copyright (C) 2004 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package se.idega.idegaweb.commune.school.placement.business;

import com.idega.business.IBOHomeImpl;


/**
 * 
 *  Last modified: $Date: 2004/10/22 12:57:49 $ by $Author: thomas $
 * 
 * @author <a href="mailto:thomas@idega.com">thomas</a>
 * @version $Revision: 1.1 $
 */
public class PlacementBusinessHomeImpl extends IBOHomeImpl implements PlacementBusinessHome {

	protected Class getBeanInterfaceClass() {
		return PlacementBusiness.class;
	}

	public PlacementBusiness create() throws javax.ejb.CreateException {
		return (PlacementBusiness) super.createIBO();
	}
}
