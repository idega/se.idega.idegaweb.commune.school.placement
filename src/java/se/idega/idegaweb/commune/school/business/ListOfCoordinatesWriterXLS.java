package se.idega.idegaweb.commune.school.business;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import se.idega.idegaweb.commune.business.CommuneUserBusiness;
import se.idega.idegaweb.commune.presentation.CommuneBlock;
import se.idega.idegaweb.commune.school.data.SchoolChoice;
import se.idega.idegaweb.commune.school.data.SchoolChoiceBMPBean;
import com.idega.business.IBOLookup;
import com.idega.core.location.data.Address;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.io.DownloadWriter;
import com.idega.io.MediaWritable;
import com.idega.io.MemoryFileBuffer;
import com.idega.io.MemoryInputStream;
import com.idega.io.MemoryOutputStream;
import com.idega.presentation.IWContext;
import com.idega.user.data.User;
import com.idega.util.PersonalIDFormatter;
import com.idega.util.text.Name;


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
	
	private String searchString = "";
	private int season;
	private int school;
	private int syear;
	private boolean showPriorityColumn = false;
	private boolean showHandicraftColumn = false;
	
	
	public ListOfCoordinatesWriterXLS() {
	}
	
	public void init(HttpServletRequest req, IWContext iwc) {
		try {
			this.locale = iwc.getApplicationSettings().getApplicationLocale();
			this.business = getSchoolCommuneBusiness(iwc);
			this.userBusiness = getCommuneUserBusiness(iwc);
			this.iwrb = iwc.getIWMainApplication().getBundle(CommuneBlock.IW_BUNDLE_IDENTIFIER).getResourceBundle(this.locale);
			 
		//	if (req.getParameter(prmSeasonId) != null && req.getParameter(prmSchoolId) != null) {
				this.season = Integer.parseInt(req.getParameter(prmSeasonId));
				this.school = Integer.parseInt(req.getParameter(prmSchoolId));
				this.syear = Integer.parseInt(req.getParameter(PARAMETER_SCHOOL_YEAR_ID));
				this.searchString = req.getParameter(PARAMETER_SEARCH_STRING);
				this.buffer = writeXLS(this.school, this.season, this.syear);
			//}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String getMimeType() {
		if (this.buffer != null) {
			return this.buffer.getMimeType();
		}
		return "application/x-msexcel";
	}
	
	public void writeTo(OutputStream out) throws IOException {
		if (this.buffer != null) {
			MemoryInputStream mis = new MemoryInputStream(this.buffer);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			while (mis.available() > 0) {
				baos.write(mis.read());
			}
			baos.writeTo(out);
		}
		else {
			System.err.println("buffer is null");
		}
	}
	
	public MemoryFileBuffer writeXLS(int schoolID, int seasonID, int syear) throws Exception {
		MemoryFileBuffer buffer = new MemoryFileBuffer();
		MemoryOutputStream mos = new MemoryOutputStream(buffer);
		String[] validStatuses = new String[] { SchoolChoiceBMPBean.CASE_STATUS_PLACED, SchoolChoiceBMPBean.CASE_STATUS_PRELIMINARY, SchoolChoiceBMPBean.CASE_STATUS_MOVED};
		List students = (List)this.business.getSchoolChoiceBusiness().getApplicantsForSchool(schoolID, seasonID, syear, validStatuses, this.searchString, SchoolChoiceComparator.NAME_SORT, -1, -1);
		String providerCoordinate = this.business.getSchoolBusiness().getSchool(new Integer(schoolID)).getSchoolKeyCode();
		if (!students.isEmpty()) {
			Collections.sort(students,  new ListOfCoordinatesComparator(providerCoordinate, this.business, this.userBusiness));			 
		    HSSFWorkbook wb = new HSSFWorkbook();
		    HSSFSheet sheet = wb.createSheet(this.iwrb.getLocalizedString("school.coordinates","Coordinates"));
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
			cell.setCellValue(this.business.getSchoolBusiness().getSchool(new Integer(schoolID)).getName());
			cell.setCellStyle(style);
			cell = row.createCell((short) 3);
			cell.setCellValue(this.iwrb.getLocalizedString("school.Coordinate","Coordinate")+": "+providerCoordinate);
			cell.setCellStyle(style);
			row = sheet.createRow((short) (cellRow++));
			row = sheet.createRow((short) (cellRow++));
			cell = row.createCell((short) 0);
		    cell.setCellValue(this.iwrb.getLocalizedString("school.name","Name"));
		    cell.setCellStyle(style);
		    cell = row.createCell((short)cellColumn++);
		    cell.setCellValue(this.iwrb.getLocalizedString("school.personal_id","Personal ID"));
		    cell.setCellStyle(style);
		    cell = row.createCell((short)cellColumn++);
		    cell.setCellValue(this.iwrb.getLocalizedString("school.Street","Street"));
		    cell.setCellStyle(style);
			cell = row.createCell((short)cellColumn++);
			cell.setCellValue(this.iwrb.getLocalizedString("school.zip_code","Zip code"));
			cell.setCellStyle(style);
		    cell = row.createCell((short)cellColumn++);
		    cell.setCellValue(this.iwrb.getLocalizedString("school.city","City"));
		    cell.setCellStyle(style);
		    cell = row.createCell((short)cellColumn++);
		    cell.setCellValue(this.iwrb.getLocalizedString("school.coordinate","Coordinate"));
		    cell.setCellStyle(style);
		    
		    SchoolChoice choice;
		    User applicant;
		    Address address;
		    
			Iterator iter = students.iterator();
			while (iter.hasNext()) {
				row = sheet.createRow(cellRow++);
				cellColumn = 0;
				choice = (SchoolChoice) iter.next();
				//created = new IWTimestamp(choice.getCreated());
				applicant = choice.getChild();
				//school = business.getSchoolBusiness().getSchool(new Integer(choice.getCurrentSchoolId()));
				address = this.userBusiness.getUsersMainAddress(applicant);
				
				Name name = new Name(applicant.getFirstName(), applicant.getMiddleName(), applicant.getLastName());
				row.createCell((short)cellColumn++).setCellValue(name.getName(this.locale, true));
			    row.createCell((short)cellColumn++).setCellValue(PersonalIDFormatter.format(applicant.getPersonalID(), this.locale));
			    if (address != null) {
						row.createCell((short)cellColumn).setCellValue(address.getStreetAddress());
					}
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
		return this.showPriorityColumn;
	}

	public void setShowPriorityColumn(boolean showPriorityColumn) {
		this.showPriorityColumn = showPriorityColumn;
	}
	
	public boolean isShowHandicraftColumn() {
		return this.showHandicraftColumn;
	}
	
	public void setShowHandicraftColumn(boolean showHandicraftColumn) {
		this.showHandicraftColumn = showHandicraftColumn;
	}	
}
