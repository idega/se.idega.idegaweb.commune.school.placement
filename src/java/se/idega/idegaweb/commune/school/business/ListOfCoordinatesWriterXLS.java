package se.idega.idegaweb.commune.school.business;

import is.idega.block.family.business.FamilyLogic;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.ejb.FinderException;
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
import se.idega.idegaweb.commune.school.data.SchoolChoice;
import se.idega.idegaweb.commune.school.data.SchoolChoiceBMPBean;
import se.idega.idegaweb.commune.school.presentation.SchoolAdminOverview;
import se.idega.idegaweb.commune.school.presentation.SchoolAdminWindow;


import com.idega.block.school.data.School;
import com.idega.block.school.data.SchoolClassMember;
import com.idega.block.school.data.SchoolSeason;
import com.idega.block.school.data.SchoolStudyPath;
import com.idega.block.school.data.SchoolStudyPathHome;
import com.idega.block.school.data.SchoolYear;

import com.idega.business.IBOLookup;
import com.idega.core.location.data.Address;
import com.idega.data.IDOLookup;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.io.DownloadWriter;
import com.idega.io.MediaWritable;
import com.idega.io.MemoryFileBuffer;
import com.idega.io.MemoryInputStream;
import com.idega.io.MemoryOutputStream;
import com.idega.presentation.IWContext;
import com.idega.presentation.Image;
import com.idega.presentation.Table;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.CheckBox;
import com.idega.presentation.ui.GenericButton;
import com.idega.presentation.ui.HiddenInput;
import com.idega.presentation.ui.SubmitButton;
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
	private SchoolCommuneBusiness business;
	private CommuneUserBusiness userBusiness;
	private Locale locale;
	private IWResourceBundle iwrb;
	
	public final static String prmSeasonId = "season_id";
	public final static String prmSchoolId = "school_id";
	//public final static String prmGrade = "grade";
	public final static String PARAMETER_SHOW_PRIORITY_COLUMN = "show_priority_column";
	public final static String PARAMETER_SHOW_HANDICRAFT_COLUMN = "show_handicraft_column";
	
	public final static String PARAMETER_SCHOOL_YEAR_ID = "school_year_id";		
	public final static String PARAMETER_VALID_STATUSES = "valid_statuses";
	public final static String PARAMETER_SEARCH_STRING = "search_string";
	public final static String PARAMETER_PROVIDER_NAME = "provider_name";
	
	private int applicationsPerPage = 10;
	private int sortChoicesBy = SchoolClassMemberComparatorForSweden.NAME_SORT;
	private String searchString = "";
	private int sortPlacedUnplacedBy = -1;
	
	private int season;
	private int school;
	private int syear;
	private boolean showPriorityColumn = false;
	private boolean showHandicraftColumn = false;
	
	
	public ListOfCoordinatesWriterXLS() {
	}
	
	public void init(HttpServletRequest req, IWContext iwc) {
		try {
			locale = iwc.getApplicationSettings().getApplicationLocale();
			business = getSchoolCommuneBusiness(iwc);
			userBusiness = getCommuneUserBusiness(iwc);
			iwrb = iwc.getIWMainApplication().getBundle(CommuneBlock.IW_BUNDLE_IDENTIFIER).getResourceBundle(locale);
			 
		//	if (req.getParameter(prmSeasonId) != null && req.getParameter(prmSchoolId) != null) {
				season = Integer.parseInt(req.getParameter(prmSeasonId));
				school = Integer.parseInt(req.getParameter(prmSchoolId));
				syear = Integer.parseInt(req.getParameter(PARAMETER_SCHOOL_YEAR_ID));
				searchString = req.getParameter(PARAMETER_SEARCH_STRING);
				buffer = writeXLS(school, season, syear);
			//}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String getMimeType() {
		if (buffer != null)
			return buffer.getMimeType();
		return "application/x-msexcel";
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
	
	public MemoryFileBuffer writeXLS(int schoolID, int seasonID, int syear) throws Exception {
		MemoryFileBuffer buffer = new MemoryFileBuffer();
		MemoryOutputStream mos = new MemoryOutputStream(buffer);
		String[] validStatuses = new String[] { SchoolChoiceBMPBean.CASE_STATUS_PLACED, SchoolChoiceBMPBean.CASE_STATUS_PRELIMINARY, SchoolChoiceBMPBean.CASE_STATUS_MOVED};
		List students = (List)business.getSchoolChoiceBusiness().getApplicantsForSchool(schoolID, seasonID, syear, validStatuses, searchString, SchoolChoiceComparator.NAME_SORT, -1, -1);
		String providerCoordinate = business.getSchoolBusiness().getSchool(schoolID).getSchoolKeyCode();
		if (!students.isEmpty()) {
			Collections.sort(students,  new ListOfCoordinatesComparator(providerCoordinate, business, userBusiness));			 
		    HSSFWorkbook wb = new HSSFWorkbook();
		    HSSFSheet sheet = wb.createSheet(iwrb.getLocalizedString("school.coordinates","Coordinates"));
		    sheet.setColumnWidth((short)0, (short) (30 * 256));
		    sheet.setColumnWidth((short)1, (short) (14 * 256));
		    sheet.setColumnWidth((short)2, (short) (30 * 256));
		    sheet.setColumnWidth((short)3, (short) (14 * 256));
			sheet.setColumnWidth((short)4, (short) (30 * 256));
			sheet.setColumnWidth((short)5, (short) (14 * 256));
			sheet.setColumnWidth((short)6, (short) (14 * 256));
			HSSFFont font = wb.createFont();
		    font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		    font.setFontHeightInPoints((short)12);
		    HSSFCellStyle style = wb.createCellStyle();
		    style.setFont(font);
		    	
			int cellRow = 0;
			int cellColumn = 0;
			HSSFRow row = sheet.createRow(cellRow++);
			HSSFCell cell = row.createCell((short)cellColumn++);
			cell = row.createCell((short) 0);			
			cell.setCellValue(business.getSchoolBusiness().getSchool(schoolID).getName());
			cell.setCellStyle(style);
			cell = row.createCell((short) 3);
			cell.setCellValue(iwrb.getLocalizedString("school.Coordinate","Coordinate")+": "+providerCoordinate);
			cell.setCellStyle(style);
			row = sheet.createRow((short) (cellRow++));
			row = sheet.createRow((short) (cellRow++));
			cell = row.createCell((short) 0);
		    cell.setCellValue(iwrb.getLocalizedString("school.name","Name"));
		    cell.setCellStyle(style);
		    cell = row.createCell((short)cellColumn++);
		    cell.setCellValue(iwrb.getLocalizedString("school.personal_id","Personal ID"));
		    cell.setCellStyle(style);
		    cell = row.createCell((short)cellColumn++);
		    cell.setCellValue(iwrb.getLocalizedString("school.Street","Street"));
		    cell.setCellStyle(style);
			cell = row.createCell((short)cellColumn++);
			cell.setCellValue(iwrb.getLocalizedString("school.zip_code","Zip code"));
			cell.setCellStyle(style);
		    cell = row.createCell((short)cellColumn++);
		    cell.setCellValue(iwrb.getLocalizedString("school.city","City"));
		    cell.setCellStyle(style);
		    cell = row.createCell((short)cellColumn++);
		    cell.setCellValue(iwrb.getLocalizedString("school.coordinate","Coordinate"));
		    cell.setCellStyle(style);
		    
		    SchoolChoice choice;
		    School school;
		    User applicant;
		    Address address;
		    IWTimestamp created;
		    
			Iterator iter = students.iterator();
			while (iter.hasNext()) {
				row = sheet.createRow(cellRow++);
				cellColumn = 0;
				choice = (SchoolChoice) iter.next();
				created = new IWTimestamp(choice.getCreated());
				applicant = choice.getChild();
				school = business.getSchoolBusiness().getSchool(new Integer(choice.getCurrentSchoolId()));
				address = userBusiness.getUsersMainAddress(applicant);
				
				Name name = new Name(applicant.getFirstName(), applicant.getMiddleName(), applicant.getLastName());
				row.createCell((short)cellColumn++).setCellValue(name.getName(locale, true));
			    row.createCell((short)cellColumn++).setCellValue(PersonalIDFormatter.format(applicant.getPersonalID(), locale));
			    if (address != null)
				    row.createCell((short)cellColumn).setCellValue(address.getStreetAddress());
			    cellColumn++;
				row.createCell((short)cellColumn++).setCellValue(address.getPostalAddress().split(" ")[0]);
				row.createCell((short)cellColumn++).setCellValue(address.getCity());
				try {
					row.createCell((short)cellColumn++).setCellValue(address.getCoordinate().getCoordinateCode());
				}catch(NullPointerException e) { 
					e.printStackTrace();
				}
			    cellColumn++;
			}
			wb.write(mos);
		}
		buffer.setMimeType("application/x-msexcel");
		return buffer;
	}
	
	protected SchoolCommuneBusiness getSchoolCommuneBusiness(IWApplicationContext iwc) throws RemoteException {
		return (SchoolCommuneBusiness) IBOLookup.getServiceInstance(iwc, SchoolCommuneBusiness.class);	
	}

	protected CommuneUserBusiness getCommuneUserBusiness(IWApplicationContext iwc) throws RemoteException {
		return (CommuneUserBusiness) IBOLookup.getServiceInstance(iwc, CommuneUserBusiness.class);	
	}

	public boolean getShowPriorityColumn() {
		return showPriorityColumn;
	}

	public void setShowPriorityColumn(boolean showPriorityColumn) {
		this.showPriorityColumn = showPriorityColumn;
	}
	
	public boolean isShowHandicraftColumn() {
		return showHandicraftColumn;
	}
	
	public void setShowHandicraftColumn(boolean showHandicraftColumn) {
		this.showHandicraftColumn = showHandicraftColumn;
	}	
}
