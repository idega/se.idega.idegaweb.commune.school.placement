package se.idega.idegaweb.commune.school.business;

import is.idega.block.family.business.FamilyLogic;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.sql.Date;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import se.idega.idegaweb.commune.business.CommuneUserBusiness;
import se.idega.idegaweb.commune.care.data.ChildCareApplication;
import se.idega.idegaweb.commune.childcare.business.ChildCareBusiness;
import se.idega.idegaweb.commune.childcare.presentation.ChildCareAdmin;
import se.idega.idegaweb.commune.presentation.CommuneBlock;
import se.idega.idegaweb.commune.school.data.SchoolChoiceBMPBean;


import com.idega.block.school.data.School;

import com.idega.business.IBOLookup;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.io.DownloadWriter;
import com.idega.io.MediaWritable;
import com.idega.io.MemoryFileBuffer;
import com.idega.io.MemoryInputStream;
import com.idega.io.MemoryOutputStream;
import com.idega.presentation.IWContext;
import com.idega.user.data.User;
import com.idega.util.IWCalendar;
import com.idega.util.IWTimestamp;
import com.idega.util.PersonalIDFormatter;
import com.idega.util.text.Name;

/*import com.lowagie.text.BadElementException;
import com.lowagie.text.Cell;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.Table;
*/

import se.idega.util.SchoolClassMemberComparatorForSweden;


public class ListOfCoordinatesWriterXLS extends DownloadWriter implements MediaWritable { 

	private MemoryFileBuffer buffer = null;
	private ChildCareBusiness business;
	
	private Locale locale;
	private IWResourceBundle iwrb;
	
	
	private String schoolName;
	private String groupName;

	public final static String PARAMETER_PROVIDER_ID = "provider_id";
	public final static String PARAMETER_GROUP_ID = "group_id";
	public final static String PARAMETER_SHOW_NOT_YET_ACTIVE = "show_not_yet_active";
	
	public final static String PARAMETER_SORT_BY = "cc_sort_by";
	public final static String PARAMETER_NUMBER_PER_PAGE = "cc_number_per_page";
	public final static String PARAMETER_FROM_DATE = "cc_from_date";
	public final static String PARAMETER_TO_DATE = "cc_to_date";
	public final static String PARAMETER_START = "cc_start";
	
	public final static String PARAMETER_SCHOOL_ID = "school_id";
	public final static String PARAMETER_SCHOOL_SEASON_ID = "school_season_id";
	public final static String PARAMETER_SCHOOL_YEAR_ID = "school_year_id";		
	public final static String PARAMETER_VALID_STATUSES = "valid_statuses";
	public final static String PARAMETER_SEARCH_STRING = "search_string";
	
	public final static String PARAMETER_TYPE = "print_type";

    private int providerId = 0;
	
    private int sortStudentsBy = SchoolChoiceComparator.NAME_SORT;
	private int sortChoicesBy = SchoolClassMemberComparatorForSweden.NAME_SORT;
	private int sortPlaced = SchoolChoiceComparator.PLACED_SORT;
	private int sortPlacedUnplacedBy = -1;
	private int studyPathID = -1;
	
	private String searchString = "";

	private int _previousSchoolClassID = -1;
	private int _previousSchoolSeasonID = -1;
	private int _previousSchoolYearID = -1;
	private int _choiceForDeletion = -1;

	private boolean multibleSchools = false;
	private boolean showStudentTable = true;
	private boolean showMessageTextButton = false;
	private boolean showListOfCoordinatesButton = true;
	private boolean searchEnabled = true;

	private int applicationsPerPage = 10;
    
    
	public ListOfCoordinatesWriterXLS() {
	}
	
	public void init(HttpServletRequest req, IWContext iwc) {
		try {
			System.out.println("----------------------  init ---------------------- ----------------------");
			locale = iwc.getApplicationSettings().getApplicationLocale();
			business = getChildCareBusiness(iwc);
		
			iwrb = iwc.getIWMainApplication().getBundle(CommuneBlock.IW_BUNDLE_IDENTIFIER).getResourceBundle(locale);
			
			if (req.getParameter(PARAMETER_PROVIDER_ID) != null) {
				//int groupID = Integer.parseInt(req.getParameter(PARAMETER_GROUP_ID));
				providerId = Integer.parseInt(req.getParameter(PARAMETER_PROVIDER_ID));
				int sortBy = Integer.parseInt(req.getParameter(PARAMETER_SORT_BY));
				int numberPerPage = Integer.parseInt(req.getParameter(PARAMETER_NUMBER_PER_PAGE));
				int start = Integer.parseInt(req.getParameter(PARAMETER_START));
				String fromDate = req.getParameter(PARAMETER_FROM_DATE);
				String toDate = req.getParameter(PARAMETER_TO_DATE);
															
				IWTimestamp stampFrom = null;
				IWTimestamp stampTo = null;
				
				
				if (fromDate != null)
					stampFrom = new IWTimestamp(fromDate);
				if (toDate != null) 
					stampTo = new IWTimestamp(toDate);
					
				Collection applications = null;
				if (stampFrom != null && stampTo != null)
					applications = getApplicationCollection(iwc, providerId, sortBy, numberPerPage, start, stampFrom.getDate(), stampTo.getDate());
				else {
					applications = getApplicationCollection(iwc, providerId, sortBy, numberPerPage, start, null, null);
				}
				schoolName = business.getSchoolBusiness().getSchool(new Integer(providerId)).getSchoolName();
				buffer = writeXLS(applications, iwc);
				setAsDownload(iwc,"childcare_siblinglist.xls",buffer.length());
				
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String getMimeType() {
		if (buffer != null)
			return buffer.getMimeType();
		return super.getMimeType();
	}
	
	public void writeTo(OutputStream out) throws IOException {
		if (buffer != null) {
			MemoryInputStream mis = new MemoryInputStream(buffer);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			while (mis.available() > 0) {
				baos.write(mis.read());
			}
			baos.writeTo(out);
		}
		else
			System.err.println("buffer is null");
	}
	
	public MemoryFileBuffer writeXLS(Collection applications, IWContext iwc)
            throws Exception {
		Collection applicants = null;
		String[] validStatuses = new String[] { SchoolChoiceBMPBean.CASE_STATUS_PLACED, SchoolChoiceBMPBean.CASE_STATUS_PRELIMINARY, SchoolChoiceBMPBean.CASE_STATUS_MOVED};
		SchoolChoiceBusinessBean schoolChoiceBean = new SchoolChoiceBusinessBean();
		int start = -1;
		Integer school_id = new Integer(PARAMETER_SCHOOL_ID);
		Integer school_season_id = new Integer(PARAMETER_SCHOOL_SEASON_ID);
		Integer school_year_id = new Integer(PARAMETER_SCHOOL_YEAR_ID);
		if((PARAMETER_SCHOOL_ID != null) ) {
			try {
				applicants = schoolChoiceBean.getApplicantsForSchool(school_id.intValue(), school_season_id.intValue(), school_year_id.intValue(), validStatuses, searchString, sortChoicesBy, applicationsPerPage, start, sortPlacedUnplacedBy);
			}catch(RemoteException e) {
				//log(e);
			}
		} 
		System.out.println("================================================= +++");

        MemoryFileBuffer buffer = new MemoryFileBuffer();
        MemoryOutputStream mos = new MemoryOutputStream(buffer);
        if (!applications.isEmpty()) {
            HSSFWorkbook wb = new HSSFWorkbook();
            HSSFSheet sheet = wb.createSheet(schoolName);
            sheet.setColumnWidth((short) 0, (short) (30 * 256));
            sheet.setColumnWidth((short) 1, (short) (14 * 256));
            sheet.setColumnWidth((short) 2, (short) (30 * 256));
            sheet.setColumnWidth((short) 3, (short) (16 * 256));
            sheet.setColumnWidth((short) 4, (short) (20 * 256));
            sheet.setColumnWidth((short) 5, (short) (14 * 256));
            sheet.setColumnWidth((short) 6, (short) (14 * 256));
            sheet.setColumnWidth((short) 7, (short) (30 * 256));

            HSSFFont font = wb.createFont();
            font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
            font.setFontHeightInPoints((short) 12);
            HSSFCellStyle style = wb.createCellStyle();
            style.setFont(font);

            int cellRow = 0;
            HSSFRow row = sheet.createRow((short) cellRow++);
            HSSFCell cell = row.createCell((short) 0);
            cell.setCellValue(schoolName);
            cell.setCellStyle(style);
            cell = row.createCell((short) 1);

            if (groupName != null) {
                row = sheet.createRow((short) cellRow++);
                cell = row.createCell((short) 0);
                cell.setCellValue(groupName);
                cell.setCellStyle(style);
            }

            row = sheet.createRow((short) cellRow++);

            row = sheet.createRow((short) cellRow++);

            cell = row.createCell((short) 0);
            cell.setCellValue(iwrb
                    .getLocalizedString("child_care.name", "Name"));
            cell.setCellStyle(style);
            cell = row.createCell((short) 1);
            cell.setCellValue(iwrb.getLocalizedString("child_care.personal_id",
                    "Personal ID"));
            cell.setCellStyle(style);
            cell = row.createCell((short) 2);
            cell.setCellValue(iwrb.getLocalizedString("child_care.sibling_name",
                    "Sibling name"));
            cell.setCellStyle(style);
            cell = row.createCell((short) 3);
            cell.setCellValue(iwrb.getLocalizedString(
                    "child_care.sibling_pid", "Sibling p.id"));
            cell.setCellStyle(style);
            cell = row.createCell((short) 4);
            cell.setCellValue(iwrb.getLocalizedString("child_care.sibling_provider",
                    "Provider"));
            cell.setCellStyle(style);
            cell = row.createCell((short) 5);
            cell.setCellValue(iwrb.getLocalizedString("child_care.start_date",
                    "Start date"));
            cell.setCellStyle(style);
            cell = row.createCell((short) 6);
            cell.setCellValue(iwrb.getLocalizedString("child_care.end_date",
                    "End date"));
            cell.setCellStyle(style);
            
            ChildCareApplication application;
            User child;          
            IWCalendar placementDate;
            
            Iterator iter = applications.iterator();
            while (iter.hasNext()) {
                row = sheet.createRow((short) cellRow++);
                application = (ChildCareApplication) iter.next();
                child = application.getChild();

                placementDate = new IWCalendar(iwc.getCurrentLocale(),
                        application.getFromDate());
                School provider = getChildCareBusiness(iwc)
                        .getCurrentProviderByPlacement(application.getChildId());              

                Name name = new Name(child.getFirstName(), child
                        .getMiddleName(), child.getLastName());
                row.createCell((short) 0).setCellValue(
                        name.getName(locale, true));
                row.createCell((short) 1).setCellValue(
                        PersonalIDFormatter.format(child.getPersonalID(),
                                locale));                
                User parent = application.getOwner();
                
            }
            wb.write(mos);
        }

        buffer.setMimeType("application/x-msexcel");
        return buffer;
    }
	
	public FamilyLogic getMemberFamilyLogic(IWContext iwc) throws RemoteException {		
		FamilyLogic service = (FamilyLogic) IBOLookup.getServiceInstance(iwc, FamilyLogic.class);	
		return service;
	}
	
	/*private Table getTable(String[] headers, int[] sizes) throws BadElementException, DocumentException {
		Table datatable = new Table(headers.length);
		datatable.setPadding(0.0f);
		datatable.setSpacing(0.0f);
		datatable.setBorder(Rectangle.NO_BORDER);
		datatable.setWidth(100);
		if (sizes != null)
			datatable.setWidths(sizes);
		for (int i = 0; i < headers.length; i++) {
			Cell cell = new Cell(new Phrase(headers[i], new Font(Font.HELVETICA, 12, Font.BOLD)));
			cell.setBorder(Rectangle.BOTTOM);
			datatable.addCell(cell);
		}
		datatable.setDefaultCellBorderWidth(0);
		datatable.setDefaultCellBorder(Rectangle.NO_BORDER);
		datatable.setDefaultRowspan(1);
		return datatable;
	}*/

	private Collection getApplicationCollection(IWContext iwc, int childcareId, int sortBy, int numberPerPage, int start, Date fromDate, Date toDate) throws RemoteException {
		Collection applications;
        
        int ordering = getOrdering(childcareId); 
        
		if (sortBy != -1 && fromDate != null && toDate != null) // && sortBy != SORT_ALL)
			applications = getChildCareBusiness(iwc).getUnhandledApplicationsByProvider(childcareId, numberPerPage, start, sortBy, fromDate, toDate, ordering);
		else
		    //applications = getChildCareBusiness(iwc).getUnhandledApplicationsByProvider(childcareId);
            //Dainis 2005-09-30: lets use the same method as in ChildCareAdmin instead
            applications = getChildCareBusiness(iwc).getUnhandledApplicationsByProvider(childcareId, Integer.MAX_VALUE, 0, ordering);
		
		return applications;
	}
	
	protected ChildCareBusiness getChildCareBusiness(IWApplicationContext iwc) throws RemoteException {
		return (ChildCareBusiness) IBOLookup.getServiceInstance(iwc, ChildCareBusiness.class);	
	}

	protected CommuneUserBusiness getCommuneUserBusiness(IWApplicationContext iwc) throws RemoteException {
		return (CommuneUserBusiness) IBOLookup.getServiceInstance(iwc, CommuneUserBusiness.class);	
	}
    
	protected Collection getApplicants() {
		Collection applicants = null;
		// get ic_address_coordinate for child from table ic_user_address. should be easy as that afaiu.
		String[] validStatuses = new String[] { SchoolChoiceBMPBean.CASE_STATUS_PLACED, SchoolChoiceBMPBean.CASE_STATUS_PRELIMINARY, SchoolChoiceBMPBean.CASE_STATUS_MOVED};
		//getBusiness().		
		SchoolChoiceBusinessBean schoolChoiceBean = new SchoolChoiceBusinessBean();
		int start = -1;
		Integer school_id = new Integer(PARAMETER_SCHOOL_ID);
		Integer school_season_id = new Integer(PARAMETER_SCHOOL_SEASON_ID);
		Integer school_year_id = new Integer(PARAMETER_SCHOOL_YEAR_ID);
		if((PARAMETER_SCHOOL_ID != null) ) {
			try {
				applicants = schoolChoiceBean.getApplicantsForSchool(school_id.intValue(), school_season_id.intValue(), school_year_id.intValue(), validStatuses, searchString, sortChoicesBy, applicationsPerPage, start, sortPlacedUnplacedBy);
			}catch(RemoteException e) {
				//log(e);
			}
		} 
		System.out.println("================================================= +++");
		return applicants;
	}
	
    private int getOrdering(int providerId) throws RemoteException {
        School provider = business.getSchoolBusiness().getSchool(new Integer(providerId));
        int ordering = provider.getSortByBirthdate() ? ChildCareAdmin.ORDER_BY_DATE_OF_BIRTH : ChildCareAdmin.ORDER_BY_QUEUE_DATE;
        return ordering;
    }    
}
