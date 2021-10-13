package application.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.ResourceBundle;

import application.BillManagement;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class HouseConfigController {
	public static String NAME="name";
	public static String LOCATION="location";
	public static String CONTACT="contact";
	public static String LANDLORD="landlord";
	public static String CARETAKER="caretaker";
	public static String PROPERTY_FILE = "config/house.config.properties";

    @FXML
    private ResourceBundle resources;


    @FXML
    private TextField name;

    @FXML
    private TextField houseLocation;

    @FXML
    private TextField contact;

    @FXML
    private TextField landlord;
    
    @FXML
    private TextField caretaker;

    @FXML
    private Button cancelBtn;

    @FXML
    private Button saveBtn;

    @FXML
    private Button toggleEdit;
    
    private Properties config;
    private String filename = BillManagement.getSavePath(PROPERTY_FILE);

    @FXML
    void cancelEdit(ActionEvent event) {
    	this.fillProperties();
    	this.toggleEditing();
    }

    @FXML
    void saveEdit(ActionEvent event) {
    	Alert alert = new Alert(AlertType.ERROR);
    	String name = this.name.getText();
    	if(name.isEmpty()||name==null) {
    		alert.setContentText("Invalid Name");
    		alert.showAndWait();
    		return;
    	}
    	
    	String location = this.houseLocation.getText();
    	if(location.isEmpty()||location==null) {
    		alert.setContentText("Invalid Location");
    		alert.showAndWait();
    		return;
    	}
    	
    	String contact = this.contact.getText();
    	if(contact==null||contact.isEmpty()) {
    		alert.setContentText("Invalid Contact");
    		alert.showAndWait();
    		return;
    	}
    	
    	String landlord = this.landlord.getText();
    	if(landlord==null||landlord.isEmpty()) {
    		alert.setContentText("Invalid Landlord name");
    		alert.showAndWait();
    		return;
    	}
    	
    	String caretaker = this.caretaker.getText();
    	
    	//Add Information to existing properties object and save to file.
    	config.setProperty(NAME, name);
    	config.setProperty(LOCATION, location);
    	config.setProperty(CONTACT, contact);
    	config.setProperty(LANDLORD, landlord);
    	config.setProperty(CARETAKER, caretaker);
    	try {
    		File file = new File(this.filename);
    		if(!file.exists()) {
    			file.createNewFile();
    		}
			config.store(new FileOutputStream(file), "CEBM configuration file");
			this.toggleEditing();
			alert.setAlertType(AlertType.INFORMATION);
			alert.setContentText("Information saved");
		} catch (IOException e) {
			e.printStackTrace();
			alert.setContentText("Error saving file please try again.");
		}
		alert.showAndWait();
    }

    @FXML
    void initialize() {
    	File file = new File(filename);
		config = new Properties();
    	if(file.exists()) {
    		try {
				config.load(new FileInputStream(file));
				if(config.isEmpty()) throw new IOException("Empty property file");
			} catch (IOException e) {
				e.printStackTrace();
				file.delete();
				this.toggleEditing();
				return;
			}
    		this.fillProperties();
    	}else {
    		this.toggleEditing();
    	}
    	
    	this.toggleEdit.setOnAction((e)-> {this.toggleEditing();});
    }
    
    /**
     * Toggle between editing and display modes
     **/
    private void toggleEditing() {
    	boolean status = !this.name.isDisabled();
    	
    	this.name.setDisable(status);
    	this.houseLocation.setDisable(status);
    	this.contact.setDisable(status);
    	this.landlord.setDisable(status);
    	this.caretaker.setDisable(status);
    	this.saveBtn.setVisible(!status);
    	this.cancelBtn.setVisible(!status);
    	this.toggleEdit.setDisable(!status);
    }
    
    /**
     * Display house information
     **/
    private void fillProperties(){
		this.name.setText(config.getProperty(NAME));
		this.houseLocation.setText(config.getProperty(LOCATION));
		this.contact.setText(config.getProperty(CONTACT));
		this.landlord.setText(config.getProperty(LANDLORD));
		this.caretaker.setText(config.getProperty(CARETAKER));
    }
    
    public static Properties getHouseInformation() {
    	File file = new File(BillManagement.getSavePath(PROPERTY_FILE));
		Properties config = new Properties();
    	if(file.exists()) {
    		try {
				config.load(new FileInputStream(file));
				if(config.isEmpty()) throw new IOException("Empty property file");
				else return config;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
    	}else {
    		return null;
    	}
    }
}
