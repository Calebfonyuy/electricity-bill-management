package application.controllers;

import java.io.File;
import java.net.URL;
import java.sql.Date;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import javax.persistence.EntityManager;

import bill.BillStatus;
import bill.Month;
import bill.MonthBill;
import bill.Payment;
import bill.PersonalBill;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import person.Tenant;
import printing.DocumentPrinter;
import printing.PrintDoc;

public class MonthBillController implements Controller {

    @FXML
    private ResourceBundle resources;
    
    @FXML
    private URL location;

    @FXML
    private Button newBill;

    @FXML
    private ListView<MonthBill> billList;

    @FXML
    private Label billUnits;

    @FXML
    private Label billPrice;

    @FXML
    private Label billTotal;

    @FXML
    private Label newBillTotal;
    
    @FXML
    private Label billTitle;
    
    @FXML
    private Label billStatus;
    
    @FXML
    private Label unattributed;
    
    @FXML
    private DatePicker deadline;

    @FXML
    private TableView<PersonalBill> personalBillList;

    @FXML
    private VBox newBillPane;
    
    @FXML
    private VBox billDisplayPane;

    @FXML
    private ComboBox<Month> month;

    @FXML
    private ComboBox<Integer> year;

    @FXML
    private Spinner<Double> units;

    @FXML
    private Spinner<Double> unitPrice;

    @FXML
    private Button cancelBtn;

    @FXML
    private Button saveBtn;
    
    @FXML
    private HBox contentPane;
    
    @FXML
    private Label paidBill;
    
    @FXML
    private Label unpaidBill;
    
    @FXML
    private Label billDeadline;

    @FXML
    private TableColumn<PersonalBill, Tenant> tenantCol;

    @FXML
    private TableColumn<PersonalBill, Integer> costCol;

    @FXML
    private TableColumn<PersonalBill, BillStatus> statusCol;

	private EntityManager manager;

	private Alert alert = new Alert(AlertType.ERROR);

    @FXML
    void cancelBill(ActionEvent event) {
    	//Change field modes for editing
    	this.toggleNewBill(event);
    	this.month.setValue(null);
    	this.year.setValue(null);
    	this.units.getValueFactory().setValue(0.0);
    	this.unitPrice.getValueFactory().setValue(0.0);
    	this.deadline.setValue(null);
    }

    @FXML
    void saveBill(ActionEvent event) {
    	//Validate values in text fields
    	//this.units.commitValue();
    	//this.unitPrice.commitValue();
    	
    	Integer year = this.year.getValue();
    	if(year==null) {
    		this.notify("Please Choose year", AlertType.WARNING);
    		return;
    	}
    	
    	Month month = this.month.getValue();
    	if(month==null) {
    		this.notify("Please Choose month", AlertType.WARNING);
    		return;
    	}
    	
    	Double price = this.unitPrice.getValue();
    	if(price<=0.0) {
    		this.notify("Invalid price!", AlertType.ERROR);
    		return;
    	}
    	
    	Double units = this.units.getValue();
    	if(units<0.0) {
    		this.notify("Invalid number units!", AlertType.ERROR);
    		return;
    	}
    	//Verify that bill does not exist yet
    	MonthBill existing = null;
    	try {
    		existing = this.manager.createQuery("select b from MonthBill b where b.month=?1 and b.year=?2",MonthBill.class)
        			.setParameter(1, month).setParameter(2, year).getSingleResult();
    	}catch(Exception e) {
    		
    	}
    	if(existing!=null) {
    		this.notify("Bill for chosen month and year already exists.", AlertType.ERROR);
    		return;
    	}
    	
    	//Verify that deadline is not on or before the creation date
    	Date lastDay = Date.valueOf(deadline.getValue());
    	Date today = new Date(System.currentTimeMillis());
    	if(lastDay.before(today)) {
    		this.notify("Invalid deadline", AlertType.ERROR);
    		return;
    	}
    	//New Month Bill
    	MonthBill bill = new MonthBill();
    	bill.setId(this.getNextId());
    	bill.setMonth(month);
    	bill.setYear(year);
    	bill.setUnit_cost(price);
    	bill.setUnits(units);
    	bill.setStatus(BillStatus.UNPAID);
    	bill.setDeadline(lastDay);
    	
    	//Confirm bill information
    	this.alert.setHeaderText("Do you really want to save this bill? It cannot be modified later.");
    	Optional<ButtonType> answer = this.notify(bill.displayBill(), AlertType.CONFIRMATION);
    	this.alert.setHeaderText(null);
    	if(answer.get()==ButtonType.NO) return;
    	
    	manager.getTransaction().begin();
    	manager.persist(bill);
    	manager.getTransaction().commit();
    	this.billList.getItems().add(bill);
    	this.cancelBill(event);
    	this.newBill.fire();
    }

	@FXML
    void toggleNewBill(ActionEvent event) {
		//Show or hide new bill pane
    	if(this.contentPane.getChildren().contains(this.newBillPane)) {
    		this.contentPane.getChildren().remove(this.newBillPane);
    	}else {
    		this.contentPane.getChildren().add(this.newBillPane);
    	}
    }
	
	@FXML
	private void printDistribution(ActionEvent event) {
		if(this.personalBillList.getItems().size()<1) {
			this.notify("No bill Distribution Yet", AlertType.INFORMATION);
			return;
		}
		MonthBill currentBill = this.billList.getSelectionModel().getSelectedItem();
		List<PersonalBill> personals = this.manager.createQuery("select b from PersonalBill b where b.monthBill=?1", PersonalBill.class)
				.setParameter(1, currentBill).getResultList();
		
		DocumentPrinter printer = new DocumentPrinter(currentBill.toString()+" Bill Distribution "+(new Date(System.currentTimeMillis())).toString());
		try {
			printer.createDocument();
		}catch(Exception ex) {
			ex.printStackTrace();
			this.notify("Error Printing Bill Distribution", AlertType.ERROR);
			return;
		}
		
		for(PersonalBill bill: personals) {
			printer.printPersonalBill(bill);
			printer.createFooter();
			printer.printCuttingLine();
		}
		
		File file = printer.getFile();
		PrintDoc.printDocument(file);
		this.notify("Printing complete!", AlertType.INFORMATION);
	}
	
	@FXML
	private void printPayments(ActionEvent event) {
		MonthBill currentBill = this.billList.getSelectionModel().getSelectedItem();
		List<Payment> payments = this.manager.createQuery("select p from Payment p where p.bill in"
				+ "(select b from PersonalBill b where b.monthBill=?1) order by p.date", Payment.class)
				.setParameter(1, currentBill).getResultList();
		
		DocumentPrinter printer = new DocumentPrinter(currentBill.toString()+" Payment Statement "+(new Date(System.currentTimeMillis())).toString());
		try {
			printer.createDocument();
		}catch(Exception ex) {
			ex.printStackTrace();
			this.notify("Error Printing Payment Statement", AlertType.ERROR);
			return;
		}
		
		printer.printBillPayments(currentBill, payments);
		printer.createFooter();
		File file = printer.getFile();
		PrintDoc.printDocument(file);
		this.notify("Printing complete!", AlertType.INFORMATION);
	}

    @FXML
    void initialize() {
    	this.contentPane.getChildren().remove(this.newBillPane);
    	//Load Years
		int yr = Calendar.getInstance().get(Calendar.YEAR);
    	int month = Calendar.getInstance().get(Calendar.MONTH);
    	if(month==Calendar.JANUARY) {
    		//Add previous year so as to take care of december bills
    		this.year.getItems().addAll(yr-1, yr);
    	}else {
    		this.year.getItems().addAll(yr);
    	}
    	
    	//Load Months
    	this.month.getItems().addAll(Month.values());
    	
    	//Setup units and price fields
    	this.units.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 10000,0));
    	this.units.getEditor().textProperty().addListener((options, oldValue, newValue)->{
    		try {
    			this.units.getValueFactory().setValue(Double.valueOf(newValue));
    		}catch(Exception ex) {
    			this.units.getValueFactory().setValue(Double.valueOf(oldValue));
    		}
    	});
    	this.units.valueProperty().addListener((obs, oldVal, newVal)->{
    		newBillTotal.setText(String.valueOf(newVal*this.unitPrice.getValue())+" FCFA");
    	});
    	this.units.setEditable(true);
    	this.unitPrice.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 500000, 0));
    	this.unitPrice.getEditor().textProperty().addListener((options, oldValue, newValue)->{
    		try {
    			this.unitPrice.getValueFactory().setValue(Double.valueOf(newValue));
    		}catch(Exception ex) {
    			this.unitPrice.getValueFactory().setValue(Double.valueOf(oldValue));
    		}
    	});
    	this.unitPrice.valueProperty().addListener((obs, oldVal, newVal)->{
    		newBillTotal.setText(String.valueOf(newVal*this.units.getValue())+" FCFA");
    	});
    	this.unitPrice.setEditable(true);
    	
    	//Bill list Listener
    	this.billList.setOnMouseClicked((e)->{
    		MonthBill bill = this.billList.getSelectionModel().getSelectedItem();
    		if(bill!=null) {
    			this.displayBill(bill);
    			this.billDisplayPane.setDisable(false);
    		}
    	});
    	
    	//Setup Table
    	tenantCol.setCellValueFactory(new PropertyValueFactory<PersonalBill, Tenant>("tenant"));
    	statusCol.setCellValueFactory(new PropertyValueFactory<PersonalBill, BillStatus>("status"));
    	costCol.setCellValueFactory(new PropertyValueFactory<PersonalBill, Integer>("totalBill"));
    }

	@Override
	public void setParent(MainAppController parent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setEntityManager(EntityManager manager) {
		this.manager = manager;
		List<MonthBill> bills = this.manager.createQuery("select b from MonthBill b order by b.year desc, b.month desc", MonthBill.class)
				.getResultList();
		this.billList.getItems().addAll(bills);
	}
	
	private Optional<ButtonType> notify(String message, AlertType type) {
		this.alert.setContentText(message);
		this.alert .setAlertType(type);
		this.alert.getButtonTypes().clear();
		//Verify alert type and adapt buttons
		if(type==AlertType.CONFIRMATION) {
			this.alert.getButtonTypes().addAll(ButtonType.NO, ButtonType.YES);
		}else {
			this.alert.getButtonTypes().add(ButtonType.OK);
		}
		return this.alert.showAndWait();
	}
	
	private int getNextId() {
		//Get last saved id and increment
		Integer item = this.manager.createQuery("select max(b.id) from MonthBill b", Integer.class).getSingleResult();
		if(item==null) return 1;
		else {
			return item+1;
		}
	}
	
	private void displayBill(MonthBill bill) {
		if(bill.updateStatus()) {
			this.manager.getTransaction().begin();
			this.manager.persist(bill);
			this.manager.getTransaction().commit();
		}
		
		//Display Bill Information
		this.personalBillList.getItems().clear();
		this.billTitle.setText(bill.toString());
		this.billUnits.setText(bill.getUnits()+" units");
		this.billPrice.setText(bill.getUnit_cost()+" FCFA");
		this.billTotal.setText(String.valueOf(bill.getUnit_cost()*bill.getUnits())+" FCFA");
		this.billStatus.setText(bill.getStatus().toString());
		this.paidBill.setText(bill.getPaidAmount(this.manager).toString()+" FCFA");
		this.unpaidBill.setText(bill.getUnpaidAmount(this.manager).toString()+" FCFA");
		this.unattributed.setText(bill.getUnsharedBill(manager).toString()+" FCFA");
		this.billDeadline.setText(bill.getDeadline().toString());
		this.billDisplayPane.setVisible(true);
		
		//Display bill distribution to tenants
		List<PersonalBill> personals = this.manager.createQuery("select b from PersonalBill b where b.monthBill=?1", PersonalBill.class)
				.setParameter(1, bill).getResultList();
		this.personalBillList.getItems().addAll(personals);
	}
}
