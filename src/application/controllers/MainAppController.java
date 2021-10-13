package application.controllers;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import application.BillManagement;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import person.User;

public class MainAppController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private StackPane mainView, root;
    @FXML
    private VBox baseView;
    
    private StackPane houseConfig, login, monthBill, personalBill, tenant, userView;
    
    private User user;
    private EntityManager entityManager;

	private PersonalBillController billController;

    @FXML
    void initialize() {
    	String path = System.getProperty("user.home")+"/"+BillManagement.BASE_FOLDER;
    	File base = new File(path);
    	if(!base.isDirectory()) {
    		base.mkdirs();
    	}
    	
    	//Create Local Database File
    	HashMap<String, String> config = new HashMap<String, String>();
    	config.put("javax.persistence.jdbc.url", "jdbc:sqlite:"+path+"/EBM_Dbase.sqlite3");
    	
    	//Create Entity Manager
    	EntityManagerFactory factory = Persistence.createEntityManagerFactory("Electricity Bill Management",config);
    	entityManager = factory.createEntityManager();
    	this.checkUsers();
    	
    	root.getChildren().clear();
    	//Load login screen
        try {
        	FXMLLoader loader = new FXMLLoader(BillManagement.class.getResource("./views/Login.fxml"));
        	login = loader.load();
        	root.getChildren().add(login);
        	LoginController controller = (LoginController)loader.getController();
        	controller.setParent(this);
        	controller.setEntityManager(entityManager);
        }catch(IOException ex){
        	ex.printStackTrace();
        }

    }

    private void checkUsers() {
		// Create Default User in case None exists
		List<User> users=this.entityManager.createQuery("Select u from User u", User.class).getResultList();
		if(users.size()<1) {
			User admin = new User();
			admin.setId(1);
			admin.setName("Admin");
			admin.setSurname("Admin");
			admin.setUsername("admin");
			admin.setPassword("admin");
			entityManager.getTransaction().begin();
			entityManager.persist(admin);
			entityManager.flush();
			entityManager.getTransaction().commit();
		}
	}

	@FXML
    void logOut(ActionEvent event) {
    	this.user = null;
    	this.root.getChildren().clear();
    	this.root.getChildren().add(login);
    }

    @FXML
    void showHouseInformation(ActionEvent event) {
    	if(houseConfig==null) {
    		try {
    			FXMLLoader loader = new FXMLLoader(BillManagement.class.getResource("./views/HouseConfig.fxml"));
    			houseConfig = loader.load();
    		}catch(IOException ex) {
    			ex.printStackTrace();
    			return;
    		}
    	}
    	mainView.getChildren().clear();
    	mainView.getChildren().add(houseConfig);
    }

    /**
     * Display monthly bills for the whole house
     **/
    @FXML
    void showMonthlyBills(ActionEvent event) {
    	if(monthBill==null) {
    		try {
    			FXMLLoader loader = new FXMLLoader(BillManagement.class.getResource("./views/MonthlyBill.fxml"));
				monthBill = loader.load();
				MonthBillController controller = (MonthBillController)loader.getController();
				controller.setEntityManager(this.entityManager);
				controller.setParent(this);
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
    	}
    	mainView.getChildren().clear();
    	mainView.getChildren().add(monthBill);
    }

    /**
     * Load the personal bills view
     **/
    @FXML
    void showPersonalBills(ActionEvent event) {
    	if(personalBill==null) {
    		try {
    			FXMLLoader loader = new FXMLLoader(BillManagement.class.getResource("./views/PersonalBill.fxml"));
    			personalBill = loader.load();
    			PersonalBillController controller = (PersonalBillController)loader.getController();
    			controller.setParent(this);
    			controller.setEntityManager(this.entityManager);
    			this.billController = controller;
    		}catch(IOException ex) {
    			ex.printStackTrace();
    			return;
    		}
    	}
    	
    	this.billController.loadTenants();
    	this.mainView.getChildren().clear();
    	this.mainView.getChildren().add(personalBill);
    }

    @FXML
    void showTenants(ActionEvent event) {
    	if(this.tenant==null) {
    		try {
    			FXMLLoader loader = new FXMLLoader(BillManagement.class.getResource("./views/Tenant.fxml"));
    			this.tenant = loader.load();
    			TenantController controller = (TenantController)loader.getController();
    			controller.setParent(this);
    			controller.setEntityManager(entityManager);
    		}catch(IOException ex) {
    			ex.printStackTrace();
    			return;
    		}
    	}
    	this.mainView.getChildren().clear();
    	this.mainView.getChildren().add(this.tenant);
    }

    @FXML
    void showUserConfig(ActionEvent event) {
    	if(userView==null) {
    		try {
    			FXMLLoader loader = new FXMLLoader(BillManagement.class.getResource("./views/Users.fxml"));
    			userView = loader.load();
    			UserController controller = (UserController)loader.getController();
    			controller.setParent(this);
    			controller.setEntityManager(entityManager);
    			controller.setUser(this.user);
    		}catch(IOException ex) {
    			ex.printStackTrace();
    			return;
    		}
    	}
    	this.mainView.getChildren().clear();
    	this.mainView.getChildren().add(userView);
    }
    
    /**
     * Set the active user and switch to logged in mode
     **/
    public void setUser(User user) {
    	this.user = user;
    	this.root.getChildren().clear();
    	this.root.getChildren().add(baseView);
    	this.showMonthlyBills(null);
    }
}
