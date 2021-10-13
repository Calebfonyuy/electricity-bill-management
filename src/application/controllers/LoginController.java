package application.controllers;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javax.persistence.EntityManager;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import person.User;

public class LoginController implements Controller{

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private TextField username;
    
    @FXML
    private Label notif;

    @FXML
    private PasswordField password;
    
    private MainAppController parent;

	private EntityManager manager;

    @FXML
    void initialize() {
    	
    }
    
    public void setParent(MainAppController parent) {
    	this.parent = parent;
    }
    

    @FXML
    void login(ActionEvent event) {
    	String username = this.username.getText();
    	this.username.clear();
    	String password = this.password.getText();
    	this.password.clear();
    	
    	//Find User from database
    	List<User> users = manager.createQuery("Select u from User u where u.username=?1", User.class).setParameter(1, username).getResultList();
    	if(users.size()<1) {
    		this.notif.setText("Wrong Credentials");
    	}else {
    		User user = users.get(0);
        	if(username.equals(user.getUsername()) && password.equals(user.getPassword())) {
        		this.parent.setUser(user);
        		this.notif.setText("");
        	}else {
        		this.notif.setText("Wrong password");
        	}
    	}
    }

    @FXML
    void switchField(ActionEvent event) {
    	this.password.selectAll();
    }

	@Override
	public void setEntityManager(EntityManager manager) {
		this.manager = manager;
	}
}