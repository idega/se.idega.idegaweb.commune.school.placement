/*
 * $Id: PlacementBusinessBean.java,v 1.1 2004/10/22 12:57:49 thomas Exp $
 * Created on Oct 15, 2004
 *
 * Copyright (C) 2004 Idega Software hf. All Rights Reserved.
 *
 * This software is the proprietary information of Idega hf.
 * Use is subject to license terms.
 */
package se.idega.idegaweb.commune.school.placement.business;

import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import javax.ejb.CreateException;
import javax.ejb.FinderException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import se.cubecon.bun24.viewpoint.business.ViewpointBusiness;
import se.cubecon.bun24.viewpoint.data.SubCategory;
import se.idega.idegaweb.commune.accounting.invoice.business.RegularPaymentBusiness;
import se.idega.idegaweb.commune.accounting.invoice.data.RegularPaymentEntry;
import se.idega.idegaweb.commune.business.CommuneUserBusiness;
import se.idega.idegaweb.commune.care.business.CareBusiness;
import se.idega.idegaweb.commune.care.data.AfterSchoolChoice;
import se.idega.idegaweb.commune.care.data.AfterSchoolChoiceHome;
import se.idega.idegaweb.commune.care.resource.business.ResourceBusiness;
import se.idega.idegaweb.commune.care.resource.data.ResourceClassMember;
import se.idega.idegaweb.commune.childcare.business.ChildCareBusiness;
import se.idega.idegaweb.commune.presentation.CommuneBlock;
import se.idega.idegaweb.commune.school.business.CentralPlacementBusiness;
import se.idega.idegaweb.commune.school.business.CentralPlacementException;
import se.idega.idegaweb.commune.school.business.SchoolChoiceBusiness;
import se.idega.idegaweb.commune.school.business.SchoolChoiceMessagePdfHandler;
import se.idega.idegaweb.commune.school.business.SchoolCommuneSession;
import se.idega.idegaweb.commune.school.data.SchoolChoice;
import se.idega.idegaweb.commune.school.data.SchoolChoiceHome;
import se.idega.idegaweb.commune.school.presentation.CentralPlacementEditor;
import com.idega.block.school.business.SchoolBusiness;
import com.idega.block.school.data.School;
import com.idega.block.school.data.SchoolCategory;
import com.idega.block.school.data.SchoolCategoryHome;
import com.idega.block.school.data.SchoolClassMember;
import com.idega.block.school.data.SchoolSeason;
import com.idega.business.IBOLookup;
import com.idega.business.IBORuntimeException;
import com.idega.business.IBOServiceBean;
import com.idega.core.contact.data.Phone;
import com.idega.data.IDOLookup;
import com.idega.data.IDOLookupException;
import com.idega.presentation.IWContext;
import com.idega.user.business.UserBusiness;
import com.idega.user.data.User;
import com.idega.util.IWTimestamp;


/**
 * @author Göran Borgman
 *
 * Business object with helper methods for CentralPlacementEditor
 * 
 * prior part of CentralPlacementBusiness and SchoolChoiceBusiness moved by Thomas to this class
 */
public class PlacementBusinessBean extends IBOServiceBean  implements PlacementBusiness{
	
	//  Keys for error messages
	private static final String KP = "central_placement_business.";
	private static final String KEY_ERROR_CHILD_ID = KP + "error.child_id";
	private static final String KEY_ERROR_SEASON = KP + "error.no.season.found";
	private static final String KEY_ERROR_CATEGORY_ID = KP + "error.category_id";
	private static final String KEY_ERROR_PROVIDER_ID = KP + "error.provider_id";
	private static final String KEY_ERROR_PLACEMENT_DATE = KP + "error.placement_date";
	private static final String KEY_ERROR_LATEST_REMOVED_DATE = KP + "error.latest_removed_date";
	private static final String KEY_ERROR_SCHOOL_TYPE = KP + "error.school_type";
	private static final String KEY_ERROR_SCHOOL_YEAR = KP + "error.school_year";
	private static final String KEY_ERROR_SCHOOL_GROUP = KP + "error.school_group";
	private static final String KEY_ERROR_STORING_PLACEMENT = KP + "error.saving_placement";
	private static final String KEY_ERROR_STUDY_PATH = KP + "error.study_path";
	private static final String KEY_ERROR_PLC_DATE_OUTSIDE_SEASON = KP + "placement_date outside_season";
	
	public String getBundleIdentifier() {
		return CommuneBlock.IW_BUNDLE_IDENTIFIER;
	}
	
	/**
	 * Stores a new placement(SchoolClassMember) with resources and ends the current placement
	 */
	public SchoolClassMember storeSchoolClassMember(IWContext iwc, int childID) 
																throws RemoteException, CentralPlacementException {
		int studentID = -1;
		User student = null;
		int schoolClassID = -1;
		int schoolYearID = -1;
		int schoolTypeID = -1;
		int registrator = -1;
		int nativeLanguageID = -1;
		
		String placementDateStr = "-1";
		String sLanguage = null;
		Timestamp registerStamp = null;
		java.sql.Date registerDate = null;
		Timestamp dayBeforeRegStamp = null;
		Date dayBeforeRegDate = null;
		java.sql.Date dayBeforeSqlDate = null;
		//String seeDayBeforeDate = null;
		String notes = null;
		SchoolClassMember newPlacement = null;
		//SchoolClassMember currentPlacement = null;
		SchoolClassMember latestPlacement = null;
		SchoolSeason chosenSeason = null;
		int newPlacementID = -1;
		
	// *** START - Check in params ***
		// pupil
		if (childID == -1) {
			throw new CentralPlacementException(KEY_ERROR_CHILD_ID, "No valid pupil found");
		}
		studentID = childID;
		student = getUserBusiness().getUser(studentID);
		if (student == null) 
			throw new CentralPlacementException(KEY_ERROR_CHILD_ID, "No valid pupil found");
		
		try {
			chosenSeason = getCareBusiness().getSchoolSeasonHome().
				findByPrimaryKey(new Integer(getSchoolCommuneSession(iwc).getSchoolSeasonID()));
		} catch (Exception e1) {
			//empty
		}
		if (chosenSeason == null)
			throw new CentralPlacementException(KEY_ERROR_SEASON, "Error finding chosen season");
		
		latestPlacement = getCentralPlacementBusiness().getLatestPlacementFromElemAndHighSchool(student, chosenSeason);
		
		
		// operational field
		if (iwc.isParameterSet(CentralPlacementEditor.PARAM_SCHOOL_CATEGORY)) {
			String categoryID = iwc.getParameter(CentralPlacementEditor.PARAM_SCHOOL_CATEGORY);
			if (categoryID.equals("-1")) {
				throw new CentralPlacementException(KEY_ERROR_CATEGORY_ID, 
						"You must chose an operational field for the placement");
			} 
			SchoolCategoryHome schCatHome = getSchoolBusiness().getSchoolCategoryHome();
		
			SchoolCategory highSchool;
			try {
				highSchool = schCatHome.findHighSchoolCategory();
				String highSchoolPK = (String) highSchool.getPrimaryKey();
			
				// Study path
				if (categoryID.equals(highSchoolPK)) {
					if (iwc.isParameterSet(CentralPlacementEditor.PARAM_STUDY_PATH)) {
						String studyPathID = iwc.getParameter(CentralPlacementEditor.PARAM_STUDY_PATH);
						if (studyPathID.equals("-1")) {
							throw new CentralPlacementException(KEY_ERROR_STUDY_PATH, 
									"You must chose a study path, for a high school placement");
						}						
					} else {
						throw new CentralPlacementException(KEY_ERROR_STUDY_PATH, 
								"You must chose a study path, for a high school placement");
						
					}
				}
			
			} catch (FinderException e1) {
				log(e1);
			}			
		}
		// provider
		if (iwc.isParameterSet(CentralPlacementEditor.PARAM_PROVIDER)) {
			String providerID = iwc.getParameter(CentralPlacementEditor.PARAM_PROVIDER);
			if (providerID.equals("-1")) {
				throw new CentralPlacementException(KEY_ERROR_PROVIDER_ID, 
																				"You must chose a school for the placement");
			} 
		}
		// school type
		if (iwc.isParameterSet(CentralPlacementEditor.PARAM_SCHOOL_TYPE)) {
			String typeID = iwc.getParameter(CentralPlacementEditor.PARAM_SCHOOL_TYPE);
			if (typeID.equals("-1")) {
				throw new CentralPlacementException(KEY_ERROR_SCHOOL_TYPE, 
																				"You must chose a school type");
			} 
			schoolTypeID = Integer.parseInt(typeID);
		}
		// school year
		if (iwc.isParameterSet(CentralPlacementEditor.PARAM_SCHOOL_YEAR)) {
			String yearID = iwc.getParameter(CentralPlacementEditor.PARAM_SCHOOL_YEAR);
			if (yearID.equals("-1")) {
				throw new CentralPlacementException(KEY_ERROR_SCHOOL_YEAR, 
																				"You must chose a school year");
			} 
			schoolYearID = Integer.parseInt(yearID);
		}
		
		// language
		if (iwc.isParameterSet(CentralPlacementEditor.PARAM_LANGUAGE)) {
			sLanguage = iwc.getParameter(CentralPlacementEditor.PARAM_LANGUAGE);
		}
		
		// school group
		if (iwc.isParameterSet(CentralPlacementEditor.PARAM_SCHOOL_GROUP)) {
			String groupID = iwc.getParameter(CentralPlacementEditor.PARAM_SCHOOL_GROUP);
			if (groupID.equals("-1")) {
				throw new CentralPlacementException(KEY_ERROR_SCHOOL_GROUP, 
																				"You must chose a school group");
			} 
			schoolClassID = Integer.parseInt(groupID);
		}
		
		// registerDate
		if (iwc.isParameterSet(CentralPlacementEditor.PARAM_PLACEMENT_DATE)) {
			//IWTimestamp today = IWTimestamp.RightNow();
			//today.setAsDate();    
			IWTimestamp placeStamp;
			String placeDateStr = iwc.getParameter(CentralPlacementEditor.PARAM_PLACEMENT_DATE);
			if (!placeDateStr.equals("")) {
				placeStamp= new IWTimestamp(placeDateStr);
				placeStamp.setAsDate();
				
				// Check if within chosen season
				java.sql.Date seasonStartDate = chosenSeason.getSchoolSeasonStart();
				java.sql.Date seasonEndDate = chosenSeason.getSchoolSeasonEnd();
				IWTimestamp seasonStartStamp = new IWTimestamp(seasonStartDate.toString());
				IWTimestamp seasonEndStamp = new IWTimestamp(seasonEndDate.toString());
				seasonStartStamp.setAsDate();
				seasonEndStamp.setAsDate();
				
				if (placeStamp.isEarlierThan(seasonStartStamp) 
						|| seasonEndStamp.isEarlierThan(placeStamp)) {
					throw new CentralPlacementException(KEY_ERROR_PLC_DATE_OUTSIDE_SEASON,
							"Placement date outside chosen seasons date boundries");
				}
			  
				// Get dayBeforeRegDate for further use
				IWTimestamp dayBeforeStamp = new IWTimestamp(placeStamp.getDate());
				dayBeforeStamp.addDays(-1);
				dayBeforeRegStamp = dayBeforeStamp.getTimestamp();
				dayBeforeRegDate = dayBeforeStamp.getDate();
				dayBeforeSqlDate = new java.sql.Date(dayBeforeRegDate.getTime());
				
		/* Below *** Removed check if earlier than today ***/
				/*if (placeStamp.isEarlierThan(today)) {
					throw new CentralPlacementException(KEY_ERROR_PLACEMENT_DATE, 
														"Placement date must be set and cannot be earlier than today");
				} else {
				*/
				
				// Check latest placement, if new removed date is before registerdate, throw exception.
				if (latestPlacement != null) {
					Timestamp latestRegDateStamp = latestPlacement.getRegisterDate();
					IWTimestamp latestRegDate = new IWTimestamp(latestRegDateStamp);
					latestRegDate.setAsDate();
					dayBeforeStamp.setAsDate();
					if (dayBeforeStamp.isEarlierThan(latestRegDate)) {
						throw new CentralPlacementException(KEY_ERROR_LATEST_REMOVED_DATE,
								"End date of latest placement, cannot be earlier than its start date");
					}
				}
				
				registerStamp = placeStamp.getTimestamp();
				registerDate = new java.sql.Date(placeStamp.getDate().getTime());
				
				//}
			} else {
				throw new CentralPlacementException(KEY_ERROR_PLACEMENT_DATE, 
														"Placement date must be set");
			}
			placementDateStr = placeDateStr;
		}  else {
			throw new CentralPlacementException(KEY_ERROR_PLACEMENT_DATE, 
												"Placement date must be set");
		}
	
		
		// registrator
		int currentUser = iwc.getCurrentUserId();
		registrator = currentUser;
						
	// *** END - Check in params ***
																											 	
	// *** START - Store new placement and end current placement ***
		UserTransaction trans = getSessionContext().getUserTransaction();
		try {
			// Start transaction
			trans.begin();
			// Create new placement
			newPlacement = getSchoolBusiness().storeNewSchoolClassMember(studentID, schoolClassID, schoolYearID, schoolTypeID, registerStamp, registrator, notes, sLanguage);
			if (newPlacement != null) {
			// *** START - Store the rest of the parameters ***
				newPlacementID = ((Integer) newPlacement.getPrimaryKey()).intValue(); // test
				
				// Compensation by agreement
				if (iwc.isParameterSet(CentralPlacementEditor.PARAM_PAYMENT_BY_AGREEMENT) &&
								!iwc.getParameter(CentralPlacementEditor.PARAM_PAYMENT_BY_AGREEMENT).
																															equals("-1")) {
					String value = 
									iwc.getParameter(CentralPlacementEditor.PARAM_PAYMENT_BY_AGREEMENT);
					if (value.equals(CentralPlacementEditor.KEY_DROPDOWN_YES)) {
						newPlacement.setHasCompensationByAgreement(true);
					} else if (value.equals(CentralPlacementEditor.KEY_DROPDOWN_NO)) {
						newPlacement.setHasCompensationByAgreement(false);
					}
				}
				// Placement paragraph
				if (iwc.isParameterSet(CentralPlacementEditor.PARAM_PLACEMENT_PARAGRAPH)) {
					newPlacement.setPlacementParagraph(
									iwc.getParameter(CentralPlacementEditor.PARAM_PLACEMENT_PARAGRAPH));					
				}
				// Invoice interval
				if (iwc.isParameterSet(CentralPlacementEditor.PARAM_INVOICE_INTERVAL)) {
					newPlacement.setInvoiceInterval(
										iwc.getParameter(CentralPlacementEditor.PARAM_INVOICE_INTERVAL));
				}
				// Study path
				if (iwc.isParameterSet(CentralPlacementEditor.PARAM_STUDY_PATH)) {
					String studyPathIDStr = iwc.getParameter(CentralPlacementEditor.PARAM_STUDY_PATH);
					if (!studyPathIDStr.equals("-1")) {
						int pK = Integer.parseInt(iwc.getParameter(CentralPlacementEditor.PARAM_STUDY_PATH));
						newPlacement.setStudyPathId(pK);
					}
				}
				
				//Native language
				if (iwc.isParameterSet(CentralPlacementEditor.PARAM_NATIVE_LANGUAGE)) {
					String studyNativeLangStr = iwc.getParameter(CentralPlacementEditor.PARAM_NATIVE_LANGUAGE);
					if (!studyNativeLangStr.equals("-1")) {
						nativeLanguageID = Integer.parseInt(studyNativeLangStr);
						student.setNativeLanguage(nativeLanguageID);
						student.store();
					}
				}
				
				// Latest invoice date
				if (iwc.isParameterSet(CentralPlacementEditor.PARAM_LATEST_INVOICE_DATE)) {
					IWTimestamp stamp = new IWTimestamp(iwc.getParameter(
																CentralPlacementEditor.PARAM_LATEST_INVOICE_DATE));
					newPlacement.setLatestInvoiceDate(stamp.getTimestamp());
				}
				
				// Resources				
				if (iwc.isParameterSet(CentralPlacementEditor.PARAM_RESOURCES)) {
					String [] arr = iwc.getParameterValues(CentralPlacementEditor.PARAM_RESOURCES);

					for (int i = 0; i < arr.length; i++) {
						int rscPK = Integer.parseInt(arr[i]);
						getResourceBusiness()
							.createResourcePlacement(rscPK, newPlacementID, placementDateStr, registrator);						
					}					
				}
				// Store newPlacement
				newPlacement.store();
				
			//	*** END - Store the rest of the parameters ***	
			}
			
			// End old placement
			if (latestPlacement != null) {
				// Set removed date 
				latestPlacement.setRemovedDate(dayBeforeRegStamp);
				latestPlacement.store();
				
				// finish old resource placements
				Collection rscPlaces = getResourceBusiness().getResourcePlacementsByMemberId(
																				(Integer) latestPlacement.getPrimaryKey());
				for (Iterator iter = rscPlaces.iterator(); iter.hasNext();) {
					ResourceClassMember rscPlace = (ResourceClassMember) iter.next();
					rscPlace.setEndDate(dayBeforeRegDate);
					//String seeDate = seeDayBeforeDate;
					rscPlace.store();
				}
				
				// Finish ongoing regular payments
				School provider = latestPlacement.getSchoolClass().getSchool();
				Collection regPayEntries = getRegularPaymentBusiness()
															.findOngoingRegularPaymentsForUserAndSchoolByDate(
																									student, provider, registerDate);
				for (Iterator iter = regPayEntries.iterator(); iter.hasNext();) {
					RegularPaymentEntry regPay = (RegularPaymentEntry) iter.next();
					regPay.setTo(dayBeforeSqlDate);					
				}
			}
			trans.commit();
			

		} catch (Exception e) {
			try {
				trans.rollback();
				e.printStackTrace();
				throw new CentralPlacementException(KEY_ERROR_STORING_PLACEMENT,
															"Error storing new placement. Transaction is rolled back.");					
			} catch (IllegalStateException e1) {
				e1.printStackTrace();
			} catch (SecurityException e1) {
				e1.printStackTrace();
			} catch (SystemException e1) {
				e1.printStackTrace();
			}

		}									
	// *** END - Store new placement and end current placement ***
											
		// int studentID, int schoolClassID, Timestamp registerDate, int registrator, String notes
		return newPlacement; 
		
	}

	// methods needed by SchoolAdminOverview
	
	public void rejectApplication(int applicationID, int seasonID, User performer, String messageSubject, String messageBody) throws RemoteException {
		rejectApplication(applicationID,seasonID,performer,messageSubject,messageBody,SchoolChoiceMessagePdfHandler.CODE_APPLICATION_REJECT);
	}
	
	
	private void rejectApplication(int applicationID, int seasonID, User performer, String messageSubject, String messageBody,String code) throws RemoteException {
		try {
			SchoolChoiceBusiness schoolChoiceBusiness = getSchoolChoiceBusiness();
			SchoolChoice choice = this.getSchoolChoiceHome().findByPrimaryKey(new Integer(applicationID));
			User child = choice.getChild();
			String status = choice.getCaseStatus().toString();
			schoolChoiceBusiness.changeCaseStatus(choice, schoolChoiceBusiness.getCaseStatusDenied().getStatus(), performer);
			choice.store();

			if (!status.equalsIgnoreCase(schoolChoiceBusiness.getCaseStatusMoved().getStatus())) {
				Collection coll = schoolChoiceBusiness.findByStudentAndSeason(choice.getChildId(), seasonID);
				Iterator iter = coll.iterator();
				while (iter.hasNext()) {
					SchoolChoice element = (SchoolChoice) iter.next();
					if (element.getChoiceOrder() == (choice.getChoiceOrder() + 1) && !element.getCaseStatus().equals("AVSL")) {
						schoolChoiceBusiness.changeCaseStatus(element, schoolChoiceBusiness.getCaseStatusPreliminary().getStatus(), performer);
						schoolChoiceBusiness.sendMessageToParents(element, schoolChoiceBusiness.getPreliminaryMessageSubject(), schoolChoiceBusiness.getPreliminaryMessageBody(element),code, schoolChoiceBusiness.getPreliminaryMessageSubject(),schoolChoiceBusiness.getPreliminaryMessageBody(element),code,false);
						continue;						
					}
				}
			}

			schoolChoiceBusiness.sendMessageToParents(choice, messageSubject, messageBody,code,messageSubject, messageBody,code,false);
			rejectAfterSchoolApplication(choice.getChildId(), choice.getChosenSchoolId(), seasonID, performer);

			if (choice.getChoiceOrder() == 3) {
				ViewpointBusiness vpb = (ViewpointBusiness) getServiceInstance(ViewpointBusiness.class);
				SubCategory subCategory = vpb.findSubCategory("Skolval");
				if (subCategory != null) {
					try {
						Phone phone = getCommuneUserBusiness().getChildHomePhone(child);

						StringBuffer body = new StringBuffer();
						//body.append(child.getNameLastFirst(true)).append(" - ").append(child.getPersonalID());
						body.append(child.getName()).append(" - ").append(child.getPersonalID());
						if (phone != null) {
							body.append("\ntel: ").append(phone.getNumber());
						}
						vpb.createViewpoint(performer, messageSubject, body.toString(), subCategory.getName(), getCommuneUserBusiness().getRootAdministratorGroupID(), -1);
					}
					catch (CreateException ce) {
						ce.printStackTrace();
					}
				}
			}
		}
		catch (FinderException fe) {
			// empty
		}
	}
	
	private AfterSchoolChoice findChoicesByChildAndProviderAndSeason(int childID, int providerID, int seasonID) throws FinderException, RemoteException {
		String[] caseStatus = { getSchoolChoiceBusiness().getCaseStatusPreliminary().getStatus() };
		return getAfterSchoolChoiceHome().findByChildAndProviderAndSeason(childID, providerID, seasonID, caseStatus);
	}
	
	private AfterSchoolChoiceHome getAfterSchoolChoiceHome() {
		try {
			return (AfterSchoolChoiceHome) IDOLookup.getHome(AfterSchoolChoice.class);
		}
		catch (IDOLookupException e) {
			throw new IBORuntimeException(e.getMessage());
		}
	}
	
	private boolean rejectAfterSchoolApplication(int childID, int providerID, int seasonID, User user) throws RemoteException {
		try {
			AfterSchoolChoice choice = findChoicesByChildAndProviderAndSeason(childID, providerID, seasonID);
			if (choice != null) {
				String subject = this.getLocalizedString("after_school.application_rejected_subject", "After school application rejected");
				String message = this.getLocalizedString("after_school.application_rejected_body", "Your after school application for {0}, {2}, to provider {1} has been rejected. Your next selected will be made active.");
				getChildCareBusiness().rejectApplication(choice, subject, message, user);
			}
			return true;
		}
		catch (FinderException e) {
			return false;
		}
	}
	
	private CommuneUserBusiness getCommuneUserBusiness() throws RemoteException {
		return (CommuneUserBusiness) this.getIDOHome(CommuneUserBusiness.class);
	}

	private SchoolChoiceHome getSchoolChoiceHome() throws java.rmi.RemoteException {
		return (SchoolChoiceHome) this.getIDOHome(SchoolChoice.class);
	}
	
	private CareBusiness getCareBusiness() throws RemoteException {
		return (CareBusiness) getServiceInstance(CareBusiness.class);
	}

	private ChildCareBusiness getChildCareBusiness() throws RemoteException {
		return (ChildCareBusiness) getServiceInstance(ChildCareBusiness.class);
	}

	
	private CentralPlacementBusiness getCentralPlacementBusiness() throws RemoteException {
		return (CentralPlacementBusiness) getServiceInstance(CentralPlacementBusiness.class);
	}
	
	private ResourceBusiness getResourceBusiness() throws RemoteException {
		return (ResourceBusiness) getServiceInstance(ResourceBusiness.class);
	}
	
	private RegularPaymentBusiness getRegularPaymentBusiness() throws RemoteException {
		return (RegularPaymentBusiness) getServiceInstance(RegularPaymentBusiness.class);
	}
	
	private SchoolBusiness getSchoolBusiness() throws RemoteException {
		return (SchoolBusiness) getServiceInstance(SchoolBusiness.class);
	}
	
	private SchoolChoiceBusiness getSchoolChoiceBusiness() throws RemoteException {
		return (SchoolChoiceBusiness) getServiceInstance(SchoolChoiceBusiness.class);
	}
	
	private UserBusiness getUserBusiness() throws RemoteException {
		return (UserBusiness) getServiceInstance(UserBusiness.class);
	}
	
	private SchoolCommuneSession getSchoolCommuneSession(IWContext iwc) throws RemoteException {
		return (SchoolCommuneSession) IBOLookup.getSessionInstance(iwc, SchoolCommuneSession.class);	
	}
	
	private Locale getDefaultLocale() {
		return getIWApplicationContext().getIWMainApplication().getSettings().getDefaultLocale();
	}

	private String getLocalizedString(String key, String defaultValue) {
		return getLocalizedString(key, defaultValue, this.getDefaultLocale());
	}
	
	private String getLocalizedString(String key, String defaultValue, Locale locale) {
		return getBundle().getResourceBundle(locale).getLocalizedString(key, defaultValue);
	}
}
