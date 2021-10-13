package printing;

import java.io.File;
import java.util.List;

import bill.MonthBill;
import bill.Payment;
import bill.PersonalBill;

public interface PrintDocument {
	public File getFile();
	public boolean printPersonalBill(PersonalBill bill);
	public void printBillPayments(MonthBill bill, List<Payment> payments);
	public void createDocument() throws Exception;
	public void createHeader();
	public void createFooter();
}
