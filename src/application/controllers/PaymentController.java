package application.controllers;

import java.net.URL;
import java.sql.Date;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import javax.persistence.EntityManager;

import bill.BillStatus;
import bill.MonthBill;
import bill.Payment;
import bill.PaymentMode;
import bill.PaymentStatus;
import bill.PersonalBill;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import person.Tenant;

public class PaymentController implements BillController{

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private TableView<Payment> historyTable;

    @FXML
    private TableColumn<Payment, Date> dateCol;

    @FXML
    private TableColumn<Payment, Double> amtCol;
    
    @FXML
    private TableColumn<Payment, PaymentMode> modeCol;

    @FXML
    private Button billBtn;

    @FXML
    private DatePicker paymentDate;

    @FXML
    private ComboBox<PaymentMode> paymentMode;

    @FXML
    private Spinner<Double> paymentAmount;

    @FXML
    private Button payBtn;

    @FXML
    private Label tenantName;

    @FXML
    private Label billAmount;

    @FXML
    private Label totalPaid;

    @FXML
    private Label totalUnpaid;

    @FXML
    private Label deadline;

	private PersonalBillController parent;

	private Stage stage;

	private PersonalBill bill;

	private Tenant tenant;

	private EntityManager manager;
	
	private Alert alert = new Alert(AlertType.ERROR);
	private Date today = new Date(System.currentTimeMillis());

	/**
     * Save payment details
     **/
    @FXML
    void savePayment(ActionEvent event) {
    	double unpaid = this.bill.getUnpaidAmount(manager);
    	
    	Date datePaid = Date.valueOf(this.paymentDate.getValue());
    	if(datePaid.after(today)) {
    		this.notify("Payment date cannot be in future!", AlertType.ERROR);
    		return;
    	}
    	
    	PaymentMode mode = this.paymentMode.getValue();
    	if(mode==null) {
    		this.notify("Select Payment mode", AlertType.ERROR);
    		return;
    	}
    	
    	//this.paymentAmount.commitValue();
    	double paid = this.paymentAmount.getValue();
    	if(paid==0d) {
    		this.notify("Cannot pay 0 FCFA", AlertType.ERROR);
    		return;
    	}
    	
    	//Verify that the entered amount is not greater that the expected value
    	if(paid>unpaid) {
    		this.notify("Cannot pay more than required amount", AlertType.ERROR);
    		return;
    	}
    	
    	//Create Payment and save
    	Payment payment = new Payment();
    	payment.setId(this.getNextId());
    	payment.setAmount(paid);
    	payment.setBill(bill);
    	payment.setTenant(tenant);
    	payment.setDate(datePaid);
    	payment.setStatus(PaymentStatus.VALID);
    	payment.setPayment_mode(mode);

    	//Update associated personal and month bills
    	this.bill.updateStatus(this.manager);
    	MonthBill mbil = this.bill.getMonthBill();
    	
    	//Save all information in one transaction 
    	mbil.updateStatus(manager);
    	this.manager.getTransaction().begin();
    	this.manager.persist(payment);
    	this.manager.persist(bill);
    	this.manager.persist(mbil);
    	this.manager.getTransaction().commit();
    	this.setPersonalBill(bill);
    	this.historyTable.getItems().add(payment);
    	this.parent.loadTenantBills();
    	
    	this.paymentDate.setValue(today.toLocalDate());
    	this.paymentAmount.getValueFactory().setValue(0d);
    	this.paymentMode.setValue(null);
    	this.paymentMode.setPromptText("Select mode");
    	this.toggleBillPayment(null);
    	
    	if(paid==unpaid) {
    		this.stage.close();
    	}
    }

    /**
     * Create a payment for bills that are not yet completely paid
     **/
    @FXML
    void toggleBillPayment(ActionEvent event) {
    	if(this.bill.getStatus()==BillStatus.PAID) {
    		this.notify("Bill already completely paid", AlertType.WARNING);
    		return;
    	}
    	boolean state;
    	if(this.paymentDate.isDisabled()) {
    		state = false;
    		this.billBtn.setText("Cancel Payment");
    	}else {
    		state = true;
    		this.billBtn.setText("Pay Bill");
    	}

		this.paymentDate.setDisable(state);
		this.paymentMode.setDisable(state);
		this.paymentAmount.setDisable(state);
		this.payBtn.setDisable(state);
    }

    @FXML
    void initialize() {
    	this.paymentMode.getItems().addAll(PaymentMode.values());
    	this.paymentAmount.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 1000000,0));
    	this.paymentAmount.getEditor().textProperty().addListener((options, oldValue, newValue)->{
    		try {
    			this.paymentAmount.getValueFactory().setValue(Double.valueOf(newValue));
    		}catch(Exception ex) {
    			this.paymentAmount.getValueFactory().setValue(Double.valueOf(oldValue));
    		}
    	});
    	this.paymentDate.setValue(today.toLocalDate());
    	
    	this.amtCol.setCellValueFactory(new PropertyValueFactory<Payment, Double>("amount"));
    	this.dateCol.setCellValueFactory(new PropertyValueFactory<Payment, Date>("date"));
    	this.modeCol.setCellValueFactory(new PropertyValueFactory<Payment, PaymentMode>("payment_mode"));
    }

	@Override
	public void setEntityManager(EntityManager manager) {
		this.manager = manager;
	}

	/**
     * Load payment history for the current personal bill
     **/
	public void loadHistory() {
		if(this.tenant==null||this.bill==null||this.manager==null) return;
		List<Payment> payments = manager.createQuery("Select p from Payment p where p.tenant=?1 and p.bill=?2 order by p.date desc", Payment.class)
				.setParameter(1, this.tenant).setParameter(2, this.bill).getResultList();
		this.historyTable.getItems().clear();
		this.historyTable.getItems().addAll(payments);
	}

	@Override
	public void setTenant(Tenant tenant) {
		this.tenant = tenant;
		this.tenantName.setText(tenant.toString());
	}

	@Override
	public void setPersonalBill(PersonalBill bill) {
		this.bill = bill;
		this.billAmount.setText(bill.getTotalBill()+" FCFA");
		this.totalPaid.setText(bill.getPaidAmount(this.manager).toString()+" FCFA");
		this.totalUnpaid.setText(bill.getUnpaidAmount(this.manager).toString()+" FCFA");
		this.deadline.setText(bill.getDeadline().toString());
	}

	@Override
	public void setStage(Stage stage) {
		this.stage = stage;
	}

	@Override
	public PersonalBill getPersonalBill() {
		return this.bill;
	}

	@Override
	public void setParent(PersonalBillController parent) {
		this.parent = parent;
	}
	
	private Optional<ButtonType> notify(String message, AlertType type){
		this.alert.setContentText(message);
		this.alert.setAlertType(type);
		this.alert.getButtonTypes().clear();
		if(type==AlertType.CONFIRMATION) {
			this.alert.getButtonTypes().addAll(ButtonType.NO, ButtonType.YES);
		}else {
			this.alert.getButtonTypes().add(ButtonType.OK);
		}
		return this.alert.showAndWait();
	}
	
	/**
     * Get the next id from the database
     * This method loads the maximum id and adds one to it then returns the value
     **/
	private int getNextId() {
		try {
			Integer item = this.manager.createQuery("select max(p.id) from Payment p", Integer.class).getSingleResult();
			return item+1;
		}catch(Exception ex) {
			return 1;
		}
	}
}
