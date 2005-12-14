package se.idega.idegaweb.commune.school.business;

import java.rmi.RemoteException;
import java.util.Comparator;
import com.idega.block.school.data.School;
import com.idega.core.location.data.Address;
import com.idega.user.data.User;
import se.idega.idegaweb.commune.business.CommuneUserBusiness;
import se.idega.idegaweb.commune.school.data.SchoolChoice;


public class ListOfCoordinatesComparator implements Comparator {
	
	private SchoolCommuneBusiness business;
	private CommuneUserBusiness userBusiness;
	
	private static final int OBJECT1_IS_BIGGER_THAN_OBJECT2 = 1;
	private static final int OBJECT1_IS_SMALLER_THAN_OBJECT2 = -1;
	private static final int OBJECT1_IS_EQUAL_TO_OBJECT2 = 0;
	
	private String providerCoordinate=null;
	
	ListOfCoordinatesComparator(String providerCoordinate, SchoolCommuneBusiness business,
			CommuneUserBusiness userBusiness) {
		this.business = business;
		this.userBusiness = userBusiness; 	
		this.providerCoordinate = providerCoordinate;
	}
	
	public int compare(Object o1, Object o2) {
		SchoolChoice schoolChoice1 = (SchoolChoice) o1;
		SchoolChoice schoolChoice2 = (SchoolChoice) o2;
		String coordinate1 = getChoiceCoordinate(schoolChoice1);
		String coordinate2 = getChoiceCoordinate(schoolChoice2);
		if(coordinate1==null&&coordinate2==null) {
			return OBJECT1_IS_EQUAL_TO_OBJECT2;
		}
		if(coordinate1==null&&coordinate2!=null) {
			return OBJECT1_IS_BIGGER_THAN_OBJECT2;
		}
		if(coordinate1!=null&&coordinate2==null) {
			return OBJECT1_IS_SMALLER_THAN_OBJECT2;
		}
		int difference1 = Math.abs(Integer.parseInt(coordinate1)-Integer.parseInt(providerCoordinate));
		int difference2 = Math.abs(Integer.parseInt(coordinate2)-Integer.parseInt(providerCoordinate));
		if(difference1==difference2) {
			return OBJECT1_IS_EQUAL_TO_OBJECT2;
		} else if(difference1<difference2) {
			return OBJECT1_IS_SMALLER_THAN_OBJECT2;
		} else if(difference1>difference2) {
			return OBJECT1_IS_BIGGER_THAN_OBJECT2;
		}
		return OBJECT1_IS_EQUAL_TO_OBJECT2;
	}

	private String getChoiceCoordinate(SchoolChoice schoolChoice) {
		User applicant;
		String coordinate=null;
		applicant = schoolChoice.getChild();
		try {
			Address address = userBusiness.getUsersMainAddress(applicant);
			try {
				coordinate =  address.getCoordinate().getCoordinateCode();
			} catch(NullPointerException e) { 
				e.printStackTrace();
			}
		} catch (RemoteException e1) {			
			e1.printStackTrace();
		}
		return coordinate;
	}

	


}
