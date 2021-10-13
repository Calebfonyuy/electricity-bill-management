package printing;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Properties;

import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.DashedLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.HorizontalAlignment;
import com.itextpdf.layout.property.TextAlignment;

import application.BillManagement;
import application.controllers.HouseConfigController;
import bill.MonthBill;
import bill.Payment;
import bill.PersonalBill;
import person.Tenant;

public class DocumentPrinter implements PrintDocument {
	private final File file;
	private PdfDocument pdf;
	private Document document;
	private PdfWriter writer;
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-YYYY");
	
	public DocumentPrinter(String fileName) {
		fileName = BillManagement.getSavePath("Bills/"+fileName+".pdf");
		file = new File(fileName);
	}
	
	@Override
	public void createDocument() throws IOException {
		// Effectively create a pdf document and prepare it for writing
		writer = new PdfWriter(file);
		pdf = new PdfDocument(writer);
		document = new Document(pdf, PageSize.A4);
		document.setFontSize(11f);
	}

	@Override
	public File getFile() {
		// Close document and return
		this.document.close();
		return this.file;
	}

	/**
	 * Write a personal Bill to the document
	 **/
	@Override
	public boolean printPersonalBill(PersonalBill bill) {
		Paragraph para = this.createBillHeader(bill);
		Table billTable = this.getTable(8);
		
		//Add table header row
		billTable.addCell(this.getHeaderCell("Month"));
		billTable.addCell(this.getHeaderCell("Date Read"));
		billTable.addCell(this.getHeaderCell("Previous"));
		billTable.addCell(this.getHeaderCell("Current"));
		billTable.addCell(this.getHeaderCell("Units"));
		billTable.addCell(this.getHeaderCell("Unit Cost"));
		billTable.addCell(this.getHeaderCell("Extra Charges(Toilet, etc)"));
		billTable.addCell(this.getHeaderCell("Total payable"));
		
		//Add bill Information
		billTable.addCell(this.getBodyCell(bill.getMonthBill().toString()));
		billTable.addCell(this.getBodyCell(bill.getDate_read().toString()));
		billTable.addCell(this.getBodyCell(bill.getPrevious_reading().toString()));
		billTable.addCell(this.getBodyCell(bill.getCurrent_reading().toString()));
		billTable.addCell(this.getBodyCell(bill.getTotalUnits().toString()));
		billTable.addCell(this.getBodyCell(bill.getUnitCost()+" FCFA"));
		billTable.addCell(this.getBodyCell(bill.getExtraCharge()+" FCFA"));
		billTable.addCell(this.getBodyCell(bill.getTotalBill()+" FCFA"));
		
		para.add(billTable);
		para.setTextAlignment(TextAlignment.CENTER);
		document.add(para);
		return false;
	}
	
	/**
	 * Add A division line to the document
	 **/
	public void printCuttingLine() {
		 document.add(new LineSeparator(new DashedLine()));
	}
	
	/**
	 * Add a heading to the bill
	 **/
	private Paragraph createBillHeader(PersonalBill bill) {
		Tenant tenant = bill.getTenant();
		Paragraph para = new Paragraph();
		para.add(this.getParagraph("ELECTRICITY BILL "+bill.getMonthBill().toString()+"", TextAlignment.CENTER, 10f).setBold().setUnderline());
		para.add("\n");
		para.add(this.getParagraph("Total Month Bill: "+bill.getMonthBill().getTotalBill()+" FCFA", TextAlignment.LEFT, 9f).setBold());
		para.add("\n");
		Paragraph para1 = new Paragraph();
		para1.add(this.getParagraph(tenant.toString(), TextAlignment.JUSTIFIED, 9f).add(", Room: "+tenant.getRoomNumber()+"\n").setBold());
		para1.add("\n");
		
		Properties info = HouseConfigController.getHouseInformation();
		Paragraph par = this.getParagraph("Payment deadline: "+dateFormat.format(bill.getDeadline()), TextAlignment.LEFT, 9f); 
		if(info!=null) {
			par.add("  Payment done through Mobile Money to the number: "+info.getProperty(HouseConfigController.CONTACT));
		}
		para1.add(par);
		
		para.add(para1);
		return para;
	}

	/**
	 * Print payment List for a given month bill
	 **/
	@Override
	public void printBillPayments(MonthBill bill, List<Payment> payments) {
		
		Table payTable = this.getTable(5);

		//Create table header row
		payTable.addCell(this.getHeaderCell("SN"));
		payTable.addCell(this.getHeaderCell("Tenant"));
		payTable.addCell(this.getHeaderCell("Date Paid"));
		payTable.addCell(this.getHeaderCell("Payment Mode"));
		payTable.addCell(this.getHeaderCell("Amount Paid"));
		
		//Print Inner cells with row numbering.
		int count = 1;
		Double total=0d;
		for(Payment pay: payments) {
			payTable.addCell(this.getBodyCell(count+""));
			payTable.addCell(this.getBodyCell(pay.getTenant().toString()));
			payTable.addCell(this.getBodyCell(dateFormat.format(pay.getDate())));
			payTable.addCell(this.getBodyCell(pay.getPayment_mode().toString()));
			payTable.addCell(this.getBodyCell(pay.getAmount().toString()));
			count++;
			total +=pay.getAmount();
		}
		
		payTable.setFontSize(8.5f);
		this.printMonthBill(bill);
		String statement="Amount Paid: "+total+" FCFA     Unpaid Amount: "+(bill.getTotalBill()-total)+" FCFA";
		//Add various portions to document
		document.add(this.getParagraph(statement, TextAlignment.CENTER, 9f));
		document.add(payTable);
	}
	
	/**
	 * Print Information about a month bill to document
	 * @param MonthBill bill
	 * @return void
	 **/
	private void printMonthBill(MonthBill bill) {
		document.add(this.getParagraph("Bill Payment Statement for "+bill.toString(), TextAlignment.CENTER, 10f).setBold().setUnderline());
		String text = "Total Units: "+bill.getUnits()+"   Unit Cost: "+bill.getUnit_cost(); 
		text += " FCFA Total Bill: "+bill.getTotalBill()+" FCFA   Payment Deadline: "+dateFormat.format(bill.getDeadline());
		document.add(this.getParagraph(text, TextAlignment.CENTER, 9f));
	}

	/**
	 * Add Special header to document
	 **/
	@Override
	public void createHeader() {
		Paragraph head = new Paragraph();
		head.setFontSize(12.5f);
		head.setTextAlignment(TextAlignment.CENTER);
		head.add(this.getParagraph("CITE ELECTRICITY BILL MANAGEMENT", TextAlignment.CENTER, 10f));
		head.add("\n");
		Properties info = HouseConfigController.getHouseInformation();
		if(info==null) {
			head.add(this.getParagraph(this.dateFormat.format((new Date(System.currentTimeMillis()))), TextAlignment.CENTER, 9.5f));
		}else {
			head.add(this.getParagraph(info.getProperty(HouseConfigController.NAME)+",  "+info.getProperty(HouseConfigController.LOCATION),
						TextAlignment.CENTER, 9.5f));
			head.add("\n");
			head.add(this.getParagraph("Landlord: "+info.getProperty(HouseConfigController.LANDLORD)+", Caretaker: "+info.getProperty(HouseConfigController.CARETAKER)+
					", Contact: "+info.getProperty(HouseConfigController.CONTACT),TextAlignment.CENTER, 9.5f));
		}
		
		head.add("\n");
		
		document.add(head);
	}

	/**
	 * Add footer advertisement 
	 **/
	@Override
	public void createFooter() {
		Paragraph foot = new Paragraph();
		foot.setTextAlignment(TextAlignment.RIGHT);
		foot.setFontSize(7f);
		foot.add("CITE ELECTRICITY BILL MANAGER \u00a9 2020 GGS godsgracesoftwares@gmail.com\n\n\n");
		
		document.add(foot);
	}
	
	private Paragraph getParagraph(String text, TextAlignment alignment, float fontSize) {
		Paragraph  para = new Paragraph();
		para.add(text);
		para.setFontSize(fontSize);
		para.setTextAlignment(alignment);
		
		return para;
	}

	private Table getTable(int cols) {
		Table trait = new Table(cols);
		trait.setHorizontalAlignment(HorizontalAlignment.CENTER);
		trait.setTextAlignment(TextAlignment.CENTER);
		trait.setWidth(PageSize.A4.getWidth()*0.9f);

		return trait;
	}
	
	private Cell getHeaderCell(String content) {
		Cell head = new Cell().add(new Paragraph(content));
		head.setBackgroundColor(new DeviceRgb(Color.CYAN));
		return head;
	}
	
	private Cell getBodyCell(String content) {
		Cell body = new Cell().add(new Paragraph(content));
		return body;
	}
}
