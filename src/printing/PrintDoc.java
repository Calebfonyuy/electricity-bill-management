package printing;

import java.awt.Desktop;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.Sides;

import application.BillManagement;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;

public class PrintDoc {
	private static Alert info = new Alert(AlertType.INFORMATION);
	private static FileChooser chooser = new FileChooser();
	private static DocFlavor flavor = DocFlavor.INPUT_STREAM.PDF;
	
	static {
		info.setHeaderText("PRINTING FAILED");
		info.setContentText("No Printer found!");
		info.getButtonTypes().clear();
		info.getButtonTypes().add(new ButtonType("Save File"));
		
		chooser.setInitialDirectory(new File(System.getProperty("user.home")));
		chooser.setTitle("Choose folder");
	}
	
	public static void printDocument(File document) {
		ArrayList<File> list = new ArrayList<File>();
		list.add(document);
		printDocuments(list);
	}
	
	public static void printDocuments(List<File> documents) {
		 /*if (Desktop.isDesktopSupported()) {
			 Desktop desktop = Desktop.getDesktop();
            if(desktop.isSupported(Desktop.Action.PRINT)){
                for(File file : documents) {
                	try {
                        desktop.print(file);
                    } catch (IOException ex) {
                    	ex.printStackTrace();
                    }
                }
            }
            else{
                printToFile(documents);
            }
        }
        else{
            printToFile(documents);
        }*/
		printWithPrinter(documents);
	}
	
	public static void printWithPrinter(List<File> documents) {
		if(findPrintServices()) {
			PrinterJob job = PrinterJob.getPrinterJob();
			if(job.printDialog()) {
				for(File document: documents) {
					job.setJobName(document.getName());
					
					try {
						Doc myDoc = new SimpleDoc(new FileInputStream(document),flavor,null);
						
						PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
						aset.add(new Copies(job.getCopies()));
						aset.add(Sides.ONE_SIDED);
						
						DocPrintJob docJob = job.getPrintService().createPrintJob();
						docJob.print(myDoc, aset);
						document.delete();
					}catch(IOException ex) {
						ex.printStackTrace();
					} catch (PrintException e) {
						e.printStackTrace();
					}
				}
			}else {
				printToFile(documents);
			}
		}else {
			printToFile(documents);
		}
	}
	
	private static void printToFile(List<File> documents) {
		info.showAndWait();
		chooser.setInitialDirectory(new File(BillManagement.getSavePath("")));
		for(File document: documents) {
			chooser.setInitialFileName(document.getName());
			File save = chooser.showSaveDialog(null);
			if(save!=null) {
				document.renameTo(save);
			}else {
				document.delete();
			}
		}
	}
	
	private static boolean findPrintServices() {
		DocFlavor myFormat = DocFlavor.SERVICE_FORMATTED.PRINTABLE;
		PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
		PrintService[] services =PrintServiceLookup.lookupPrintServices(myFormat, aset);
		if(services.length>0)
			return true;
		else
			return false;
	}
}
