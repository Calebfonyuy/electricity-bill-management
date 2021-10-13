package application.controllers;

import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import javax.persistence.EntityManager;

import bill.BillStatus;
import bill.MonthBill;
import bill.PersonalBill;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import person.Tenant;

public class NewPersonalBillController implements BillController{

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private TextField previous;

    @FXML
    private TextField current;

    @FXML
    private TextField units;

    @FXML
    private DatePicker readDate;

    @FXML
    private DatePicker deadline;

    @FXML
    private TextField extra;

    @FXML
    private ComboBox<MonthBill> monthBill;
    
    @FXML
    private Label totalBill;
    
    @FXML
    private Label monthBillTotal;
    
    @FXML
    private Label monthBillDeadline;

	private EntityManager manager;

	private Stage stage;

	private PersonalBill bill;

	private Tenant tenant;


	private Double unitCost;

	private Alert alert = new Alert(AlertType.ERROR);

	private PersonalBillController parent;

    @FXML
    void cancelBill(ActionEvent event) {
    	this.stage.close();
    }

    @FXML
    void saveBill(ActionEvent event) {
    	MonthBill mbill = this.monthBill.getValue();
    	
    	if(mbill==null) {
    		this.notify("Choose a month bill", AlertType.ERROR);
    		return;
    	}
    	
    	double prev = Double.valueOf(this.previous.getText());
    	double curr=0.0;
    	try {
    		curr = Double.valueOf(this.current.getText());
    	}catch(Exception ex) {
    		this.notify("Invalid current reading", AlertType.ERROR);
    		return;
    	}
    	
    	if(curr<prev) {
    		this.notify("Current reading must be greater or equal to previous.", AlertType.ERROR);
    		return;
    	}
    	
    	Date readDate = Date.valueOf(this.readDate.getValue());
    	Date today = new Date(System.currentTimeMillis());
    	if(readDate.after(today)) {
    		this.notify("Reading date cannot be after today.", AlertType.ERROR);
    		return;
    	}
    	
    	Date deadDate = Date.valueOf(this.deadline.getValue());
    	if(deadDate.before(today)) {
    		this.notify("Deadline must be earliest today.", AlertType.ERROR);
    		return;
    	}
    	
    	if(deadDate.after(mbill.getDeadline())) {
    		this.notify("Deadline cannot be later than deadline for monthly bill.", AlertType.ERROR);
    		return;
    	}
    	
    	double ext = 0.0;
    	try {
    		ext = Double.valueOf(this.extra.getText());
    	}catch(Exception ex) {
    		this.notify("Invalid extra charge", AlertType.ERROR);
    		return;
    	}

    	double unshared = mbill.getTotalBill()-mbill.getSharedBill(manager);
    	
    	//Create new bill in case controller is in new bill mode
    	if(this.bill==null) {
    		this.bill = new PersonalBill();
    		this.bill.setId(this.getNextId());
    		try {
    			//Verify that a bill does not already exist for the particular tenant and particular month bill
        		PersonalBill existing = this.manager.createQuery("select b from PersonalBill b where b.monthBill=?1 and b.tenant=?2", PersonalBill.class)
            			.setParameter(1, mbill).setParameter(2, this.tenant).getSingleResult();
        		if(existing!=null) {
        			this.notify(this.tenant.getName()+" already has a portion of this bill. \nPlease select it and edit rather.", AlertType.ERROR);
        			return;
        		}
        	}catch(Exception ex) {
        		
        	}
    	}else {
    		unshared = unshared+this.bill.getTotalBill();
    	}
    	this.bill.setMonthBill(mbill);
    	this.bill.setPrevious_reading(prev);
    	this.bill.setCurrent_reading(curr);
    	this.bill.setDate_read(readDate);
    	this.bill.setDeadline(deadDate);
    	this.bill.setExtraCharge(ext);
    	this.bill.setTenant(this.tenant);
    	this.bill.setStatus(BillStatus.UNPAID);
    	
    	

    	//Verify that the amount being allocated to the user is not above the remaining amount for the month bill
    	if(bill.getTotalBill()>unshared) {
    		this.notify(bill.getTotalBill()+" is greater than the unshared bill("+unshared+")", AlertType.ERROR);
    		return;
    	}
    	
    	this.manager.getTransaction().begin();
    	this.manager.persist(this.bill);
    	this.manager.getTransaction().commit();
    	this.stage.close();
    	this.parent.loadTenantBills();
    }

	@FXML
    void initialize() {
    	this.unitCost = 0.0;
    	this.monthBill.setOnAction((e)->{
    		MonthBill active = this.monthBill.getValue();
    		if(active!=null) {
    			//Fill Bill Information into the various fields
    			this.units.setText(active.getUnit_cost().toString());
    			this.monthBillTotal.setText(String.valueOf(active.getUnsharedBill(this.manager)));
    			this.monthBillDeadline.setText(active.getDeadline().toString());
    			this.unitCost = active.getUnit_cost();
    		}else {
    			this.unitCost=0.0;
    			this.monthBillTotal.setText("");
    			this.monthBillDeadline.setText("");
    			this.units.clear();
    		}
    		this.calculateBill();
    	});
    	
    	this.current.setOnKeyTyped((e)->{
    		this.calculateBill();
    	});
    	
    	this.extra.setOnKeyTyped((e)->{
    		this.calculateBill();
    	});
    }

	@Override
	public void setEntityManager(EntityManager manager) {
		this.manager  = manager;
		this.loadBillList();
	}
	
	public void loadBillList() {
		this.monthBill.getItems().clear();
		List<MonthBill> bills = this.manager.createQuery("Select b from MonthBill b where b.status!=?1", MonthBill.class)
				.setParameter(1, BillStatus.PAID).getResultList();
		this.monthBill.getItems().addAll(bills);
	}

	@Override
	public void setTenant(Tenant tenant) {
		this.tenant = tenant;
		if(this.manager!=null) {
			try {
				//Select the last current reading for a given tenant's bill.
				Double prev = this.manager.createQuery("Select b.current_reading from PersonalBill b "
						+ "where b.id=(select max(p.id) from PersonalBill p where p.tenant=?1)", Double.class)
						.setParameter(1, this.tenant).getSingleResult();
				this.previous.setText(prev.toString());
			}catch(Exception ex) {
				ex.printStackTrace();
				this.previous.setText("0");
			}
		}
	}

	@Override
	public void setPersonalBill(PersonalBill bill) {
		this.bill = bill;
		if(bill!=null) {
			// Set controller and view into editing mode
			for(MonthBill mbil: monthBill.getItems()) {
				if(mbil.toString().equals(bill.getMonthBill().toString())) {
					this.monthBill.setValue(mbil);
					//this.monthBill.setPromptText(mbil.toString());
					this.monthBill.getSelectionModel().select(mbil);
					break;
				}
			}
			this.previous.setText(bill.getPrevious_reading()+"");
			this.current.setText(bill.getCurrent_reading()+"");
			this.deadline.setValue(bill.getDeadline().toLocalDate());
			this.extra.setText(bill.getExtraCharge()+"");
			this.readDate.setValue(bill.getDate_read().toLocalDate());
			this.units.setText(bill.getUnitCost()+"");
			this.totalBill.setText(bill.getTotalBill()+" FCFA");
		}else {
			// Set controller and view into creation mode
			this.loadBillList();
			this.current.setText("0");
			this.deadline.setValue(LocalDate.now());
			this.extra.setText("0");
			this.readDate.setValue(LocalDate.now());
			this.units.clear();
			this.totalBill.setText("");
		}
	}

	@Override
	public void setStage(Stage stage) {
		this.stage = stage;
	}

	@Override
	public PersonalBill getPersonalBill() {
		return this.bill;
	}
	
	private Double calculateBill() {
		Double bill = 0.0;
		try {
			double consumed = Double.valueOf(this.current.getText())-Double.valueOf(this.previous.getText());
			bill = (this.unitCost*consumed)+Double.valueOf(this.extra.getText());
		}catch(Exception ex) {
			bill = 0.0;
		}
		this.totalBill.setText(bill.toString()+" FCFA");
		return bill;
	}

    private Optional<ButtonType> notify(String string, AlertType type) {
    	this.alert.setContentText(string);
    	this.alert.setAlertType(type);
    	return this.alert.showAndWait();
	}
    
    /**
     * Get the next id from the database
     * This method loads the maximum id and adds one to it then returns the value
     **/
    private int getNextId() {
		Integer item = this.manager.createQuery("select max(u.id) from PersonalBill u", Integer.class).getSingleResult();
		if(item==null) return 1;
		else {
			return item+1;
		}
	}

	@Override
	public void setParent(PersonalBillController parent) {
		this.parent = parent;
	}
}
