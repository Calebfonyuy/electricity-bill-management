package application.controllers;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import javax.persistence.EntityManager;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import person.User;

public class UserController implements Controller{

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private TextField name;

    @FXML
    private TextField surname;

    @FXML
    private TextField username;

    @FXML
    private Label passwordLabel;

    @FXML
    private PasswordField password;

    @FXML
    private PasswordField passwordConfirm;

    @FXML
    private Button saveBtn;

    @FXML
    private Button editToggle;

	private User user;
	private Alert alert = new Alert(AlertType.CONFIRMATION);

	private EntityManager manager;

    @FXML
    void saveChanges(ActionEvent event) {
    	String name = this.name.getText();
    	if(name==""||name.length()<2) {
    		this.notify("Invalid name", AlertType.ERROR);
    		return;
    	}
    	String surname = this.surname.getText();
    	if(surname.length()<2||surname=="") {
    		this.notify("Invalid Surname", AlertType.ERROR);
    		return;
    	}
    	String username = this.username.getText();
    	if(username=="" || username.length()<2) {
    		this.notify("Invalid Username", AlertType.ERROR);
    		return;
    	}
    	String password = this.password.getText();
    	if(password==""||password.length()<5) {
    		this.notify("Password too short. Should have at least 5 characters", AlertType.WARNING);
    		return;
    	}
    	if(!password.equals(this.passwordConfirm.getText())) {
    		this.notify("Passwords do not match. Please check again!", AlertType.ERROR);
    		return;
    	}
    	
    	//Update user information and save
    	this.user.setName(name);
    	this.user.setSurname(surname);
    	this.user.setUsername(username);
    	this.user.setPassword(password);
    	this.manager.getTransaction().begin();
    	this.manager.persist(this.user);
    	this.manager.getTransaction().commit();
    	this.toggleEditing(new ActionEvent());
    }

    @FXML
    void toggleEditing(ActionEvent event) {
    	boolean status;
    	if(this.name.isDisabled()) {
    		status = false;
    	}else {
    		status = true;
    	}
    	
		this.name.setDisable(status);
		this.surname.setDisable(status);
		this.username.setDisable(status);
		this.password.setDisable(status);
		this.passwordConfirm.setDisable(status);
		this.saveBtn.setDisable(status);

    }

    @FXML
    void initialize() {
    	
    }
    
    public void setUser(User user) {
    	this.user = user;
    	setUpFields();
    }
    
    private void setUpFields() {
    	this.name.setText(this.user.getName());
    	this.surname.setText(this.user.getSurname());
    	this.username.setText(this.user.getUsername());
    	this.password.setText(this.user.getPassword());
    	this.passwordConfirm.setText(this.user.getPassword());
    }
    
    private Optional<ButtonType> notify(String message, AlertType type) {
    	alert.setAlertType(type);
    	alert.setContentText(message);
    	return alert.showAndWait();
    }

	@Override
	public void setParent(MainAppController parent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setEntityManager(EntityManager manager) {
		this.manager = manager;
	}
}
