package application.controllers;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import javax.persistence.EntityManager;

import application.BillManagement;
import bill.BillStatus;
import bill.MonthBill;
import bill.PersonalBill;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import person.Tenant;
import printing.DocumentPrinter;
import printing.PrintDoc;

public class PersonalBillController implements Controller{

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private ListView<Tenant> tenantList;

    @FXML
    private Label tenantName;

    @FXML
    private Label tenantRoom;

    @FXML
    private Label tenantNumber;

    @FXML
    private Label tenantMail;

    @FXML
    private Button newBillBtn;

    @FXML
    private Button editBtn;

    @FXML
    private Button payBtn;

    @FXML
    private Button deleteBtn;

    @FXML
    private Button printBtn;

    @FXML
    private TableView<PersonalBill> billTable;

    @FXML
    private TableColumn<PersonalBill, MonthBill> monthCol;

    @FXML
    private TableColumn<PersonalBill, Double> prevCol;

    @FXML
    private TableColumn<PersonalBill, Double> currCol;

    @FXML
    private TableColumn<PersonalBill, Double> unitCol;

    @FXML
    private TableColumn<PersonalBill, Double> costCol;

    @FXML
    private TableColumn<PersonalBill, Double> extraCol;

    @FXML
    private TableColumn<PersonalBill, Double> totalCol;

    @FXML
    private TableColumn<PersonalBill, BillStatus> statusCol;
    
    @FXML
    private TableColumn<PersonalBill, Date> deadlineCol;
    
    @FXML
    private HBox buttonRow;

	private EntityManager manager;
	private Stage stage;
	private Scene newBillScene, paymentScene, editBillScene;
	private PaymentController paymentController;
	private NewPersonalBillController newBillController, billEditController;

	private Tenant activeTenant;
	private PersonalBill activeBill;

	private Alert alert = new Alert(AlertType.ERROR);

	/**
	 * Display Scene for creation of new Bills
	 **/
    @FXML
    void createBill(ActionEvent event) {
    	//A tenant must be selected for a new bill to be created
    	if(this.activeTenant==null) return;
    	//Create scene if scene does not exist yet
    	if(newBillScene==null) {
    		createNewBillScene();
    	}
    	
    	//Pass required data and load displays
    	newBillController.setTenant(activeTenant);
    	newBillController.setPersonalBill(null);
    	newBillController.loadBillList();
    	
    	//Display New bill screen
    	this.stage.setScene(newBillScene);
		this.stage.setWidth(360);
    	this.stage.setTitle("New Bill");
    	this.stage.show();
    }
    
    /**
     * Create Scene for creating new bills
     **/
    private void createNewBillScene() {
    	try {
    		FXMLLoader loader = new FXMLLoader(BillManagement.class.getResource("./views/NewPersonalBill.fxml"));
    		VBox pane = loader.load();
    		newBillScene = new Scene(pane);
    		newBillScene.getStylesheets().add("/application/resources/styles.css");
    		//Initialize controller 
    		newBillController = (NewPersonalBillController)loader.getController();
    		newBillController.setEntityManager(manager);
    		newBillController.setStage(stage);
    		newBillController.setPersonalBill(null);
    		newBillController.setParent(this);
    	}catch(IOException ex) {
    		ex.printStackTrace();
    		this.newBillScene = null;
    	}
	}
    
    /**
     * Load And display information for currently selected Tenant
     **/
	@FXML
    public void displayTenantBills(MouseEvent event) {
    	Tenant chosen = this.tenantList.getSelectionModel().getSelectedItem();
    	this.buttonRow.setDisable(true);
    	if(chosen==null) {
    		return;
    	}
    	
    	this.tenantName.setText(chosen.toString());
    	this.tenantNumber.setText(chosen.getTelephone());
    	this.tenantRoom.setText(chosen.getRoomNumber().toString());
    	this.tenantMail.setText(chosen.getEmail());

    	this.activeTenant = chosen;
    	this.loadTenantBills();
    }
	
	/**
	 * Load and display tenant bills on table
	 **/
	public void loadTenantBills() {
		this.billTable.getItems().clear();
    	List<PersonalBill> billList = this.manager.createQuery("Select b from PersonalBill b where b.tenant=?1 order by b.id desc", PersonalBill.class)
    			.setParameter(1, this.activeTenant).getResultList();

    	for(PersonalBill pbill: billList) {
    		if(pbill.getStatus()!=BillStatus.PAID) {
    			pbill.updateStatus(manager);
    			this.manager.getTransaction().begin();
    			this.manager.persist(pbill);
    			this.manager.getTransaction().commit();
    		}
    	}
    	this.billTable.getItems().addAll(billList);
	}

	/**
	 * Load and display bill editing screen
	 **/
    @FXML
    void editBill(ActionEvent event) {
    	//Create scene if not created yet.
    	if(editBillScene==null) {
    		createBillEditScene();
    	}
    	
    	//Pass necessary information for bill display and editing
    	billEditController.setTenant(activeTenant);
    	billEditController.setPersonalBill(this.activeBill);
    	billEditController.loadBillList();
    	
    	this.stage.setScene(editBillScene);
		this.stage.setWidth(360);
    	this.stage.setTitle("Edit Bill");
    	this.stage.show();
    }

    /**
     * Create Bill Editing scene
     **/
    private void createBillEditScene() {
    	try {
    		FXMLLoader loader = new FXMLLoader(BillManagement.class.getResource("./views/NewPersonalBill.fxml"));
    		VBox pane = loader.load();
    		editBillScene = new Scene(pane);
    		editBillScene.getStylesheets().add("/application/resources/styles.css");
    		//Initialize controller.
    		billEditController = (NewPersonalBillController)loader.getController();
    		billEditController.setEntityManager(manager);
    		billEditController.setStage(stage);
    		billEditController.setParent(this);
    	}catch(IOException ex) {
    		ex.printStackTrace();
    		this.editBillScene = null;
    	}
	}

    /**
     * Display bill payment management view
     **/
	@FXML
    void showPayments(ActionEvent event) {
		if(this.paymentScene==null) {
			createPaymentScene();
		}
		
		//Pass on required information
		paymentController.setTenant(activeTenant);
		paymentController.setPersonalBill(activeBill);
		paymentController.loadHistory();
		
		this.stage.setScene(paymentScene);
		this.stage.setWidth(600);
		this.stage.setTitle("Pay Bill");
		this.stage.show();
    }

	/**
	 * Create bill payment scene
	 **/
    private void createPaymentScene() {
		try {
			FXMLLoader loader = new FXMLLoader(BillManagement.class.getResource("./views/Payments.fxml"));
			StackPane pane = loader.load();
			paymentScene = new Scene(pane);
			paymentScene.getStylesheets().add("/application/resources/styles.css");
			//Initialize controller
			paymentController = (PaymentController)loader.getController();
			paymentController.setStage(stage);
			paymentController.setTenant(activeTenant);
			paymentController.setParent(this);
			paymentController.setEntityManager(manager);
		}catch(IOException ex) {
			ex.printStackTrace();
			paymentScene = null;
		}
	}
    
    @FXML
    void printBills(ActionEvent event) {
    	DocumentPrinter printer;
    	
    	try {
    		printer = new DocumentPrinter(this.activeTenant.getName()+"-"
    					+this.activeBill.getMonthBill().toString()+"-"+(new Date(System.currentTimeMillis()).toString()));
    		printer.createDocument();
    	}catch(IOException ex) {
    		this.notify("Error Printing bill", AlertType.INFORMATION);
    		return;
    	}
    	
    	printer.printPersonalBill(this.activeBill);
    	printer.createFooter();
    	File file = printer.getFile();
    	PrintDoc.printDocument(file);
    	this.notify("Report Printing completed!", AlertType.INFORMATION);
    }
    
    @FXML
    private void deleteBill() {
    	PersonalBill bill = this.billTable.getSelectionModel().getSelectedItem();
    	if(bill.getStatus()==BillStatus.UNPAID) {
    		//Delete the selected bill
    		Optional<ButtonType> choice = this.notify("Do you really want to delete this bill?", AlertType.CONFIRMATION);
    		if(choice.get()!=ButtonType.YES) {
    			return;
    		}
    		this.manager.getTransaction().begin();
    		this.manager.remove(bill);
    		this.manager.getTransaction().commit();
    		this.billTable.getItems().remove(bill);
    		this.notify("Bill Deleted", AlertType.INFORMATION);
    	}else {
    		this.notify("Impossible to delete a bill with status "+bill.getStatus(), AlertType.ERROR);
    	}
    }

    /**
     * Display all notifications related to the associated window according to notification type
     **/
	private Optional<ButtonType> notify(String message, AlertType type) {
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

	@FXML
    void initialize() {
		//Setup stage for displaying child views
		this.stage = new Stage();
		this.stage.setHeight(400);
		this.stage.initModality(Modality.APPLICATION_MODAL);
		this.stage.setOnCloseRequest((e)->{
			this.displayTenantBills(null);
		});
		
    	setupTable();
    }
    
    private void setupTable(){
    	// Setup table columns and data references
    	monthCol.setCellValueFactory(new PropertyValueFactory<PersonalBill, MonthBill>("monthBill"));
    	prevCol.setCellValueFactory(new PropertyValueFactory<PersonalBill, Double>("previous_reading"));
    	currCol.setCellValueFactory(new PropertyValueFactory<PersonalBill, Double>("current_reading"));
    	unitCol.setCellValueFactory(new PropertyValueFactory<PersonalBill, Double>("totalUnits"));
    	costCol.setCellValueFactory(new PropertyValueFactory<PersonalBill, Double>("unitCost"));
    	extraCol.setCellValueFactory(new PropertyValueFactory<PersonalBill, Double>("extraCharge"));
    	totalCol.setCellValueFactory(new PropertyValueFactory<PersonalBill, Double>("totalBill"));
    	statusCol.setCellValueFactory(new PropertyValueFactory<PersonalBill, BillStatus>("status"));
    	deadlineCol.setCellValueFactory(new PropertyValueFactory<PersonalBill, Date>("deadline"));
    	
    	//Create on click event handler
    	this.billTable.setOnMouseClicked((e)->{
    		PersonalBill tmp = this.billTable.getSelectionModel().getSelectedItem();
    		if(tmp==null) {
    			this.buttonRow.setDisable(true);
    		}else {
    			this.activeBill = tmp;
    			this.buttonRow.setDisable(false);
    			if(this.activeBill.getStatus()!=BillStatus.UNPAID) {
    				this.deleteBtn.setDisable(true);
    				this.editBtn.setDisable(true);
    			}else {
    				this.deleteBtn.setDisable(false);
    				this.editBtn.setDisable(false);
    			}
    		}
    	});
    }

	@Override
	public void setParent(MainAppController parent) {
		// Method useless for this controller
	}

	@Override
	public void setEntityManager(EntityManager manager) {
		this.manager = manager;
		this.loadTenants();
	}
	
	/**
	 * Load and display all active tenants.
	 **/
	public void loadTenants() {
		List<Tenant> tenants = this.manager.createQuery("select t from Tenant t where t.status=1",Tenant.class).getResultList();
		this.tenantList.getItems().clear();
		this.tenantList.getItems().addAll(tenants);
	}
	
}
