package se.idega.idegaweb.commune.school.presentation;

import java.rmi.RemoteException;

import com.idega.presentation.IWContext;
import com.idega.presentation.Table;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.Window;

/**
 * @author laddi
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class SchoolAdminWindow extends Window {

	public SchoolAdminWindow() {
		this.setWidth(450);
		this.setHeight(300);
		this.setScrollbar(true);
		this.setResizable(false);	
	}

	/**
	 * @see com.idega.presentation.PresentationObject#main(IWContext)
	 */
	public void main(IWContext iwc) throws Exception {
		add(new SchoolAdminOverview());
	}
	
}
