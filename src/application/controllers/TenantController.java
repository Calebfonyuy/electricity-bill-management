package application.controllers;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import javax.persistence.EntityManager;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import person.Tenant;

public class TenantController implements Controller{

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private ListView<Tenant> tenantList;

    @FXML
    private Button newBtn;

    @FXML
    private Button editBtn;

    @FXML
    private Button deleteBtn;

    @FXML
    private TextField name;

    @FXML
    private TextField surname;

    @FXML
    private TextField roomNumber;

    @FXML
    private TextField telephone;

    @FXML
    private TextField email;


    @FXML
    private Button cancelBtn;

    @FXML
    private Button saveBtn;
    
    private Tenant tenant;
    private Alert alert = new Alert(AlertType.ERROR);

	private EntityManager manager;

    @FXML
    void cancelEdit(ActionEvent event) {
    	this.toggleEdit(event);
    }

    @FXML
    void deleteTenant(ActionEvent event) {
    	if(this.tenant==null) return;
    	Optional<ButtonType> answer = this.notify("Do you really want to delete this tenant?", AlertType.CONFIRMATION);
    	if(answer.get()==ButtonType.YES) {
    		//Delete Tenant
    		this.tenant.setStatus(0);
    		this.manager.getTransaction().begin();
    		this.manager.persist(this.tenant);
    		this.manager.getTransaction().commit();
    		this.tenantList.getItems().remove(this.tenant);
    		this.tenant = null;
    	}
    }

    @FXML
    //Collect Tenant Information and Save
    void saveEdit(ActionEvent event) {
    	String name = this.name.getText();
    	if(name==""||name.length()<2) {
    		this.notify("Invalid Name", AlertType.ERROR);
    		return;
    	}
    	
    	String surname = this.surname.getText();
    	if(surname==""||surname.length()<2) {
    		this.notify("Invalid Surname", AlertType.ERROR);
    		return;
    	}
    	
    	String room = this.roomNumber.getText();
    	try {
    		if(room==""||room.length()<1) throw new Exception();
    		Integer.valueOf(room);
    	}catch(Exception ex) {
    		this.notify("Room Number should be a Number!", AlertType.ERROR);
    		return;
    	}
    	
    	List<Tenant> saved = this.manager.createQuery("select t from Tenant t where t.status=1 and t.roomNumber=?1", Tenant.class)
    			.setParameter(1, Integer.valueOf(room)).getResultList();
    	if(saved.size()>1) {
    		this.notify("Tenant already exists for room "+room+"!", AlertType.WARNING);
    		return;
    	}
    	
    	String phone = this.telephone.getText();
    	if(phone==""||phone.length()<9) {
    		this.notify("Invalid Phone Number", AlertType.ERROR);
    		return;
    	}
    	
    	String email = this.email.getText();
    	if(!email.isEmpty() && email.indexOf('@')<0) {
    		this.notify("Invalid email", AlertType.WARNING);
    		return;
    	}
    	boolean isNew = false;
    	if(this.tenant==null) {
    		this.tenant = new Tenant();
    		this.tenant.setId(this.getNextId());
    		isNew = true;
    	}
    	
    	this.tenant.setName(name);
    	this.tenant.setSurname(surname);
    	this.tenant.setRoomNumber(Integer.valueOf(room));
    	this.tenant.setTelephone(phone);
    	this.tenant.setEmail(email);
    	this.tenant.setStatus(1);
    	//Save Tenant
    	this.manager.getTransaction().begin();
    	this.manager.persist(this.tenant);
    	this.manager.getTransaction().commit();
    	
    	if(isNew)this.tenantList.getItems().add(this.tenant);
    	this.toggleEdit(event);
    }

    @FXML
    //Switch between editing mode and viewing mode
    void toggleEdit(ActionEvent event) {
    	boolean status;
    	if(this.name.isDisabled()) {
    		status = false;
    	}else {
    		status = true;
    	}
    	this.name.setDisable(status);
    	this.surname.setDisable(status);
    	this.roomNumber.setDisable(status);
    	this.telephone.setDisable(status);
    	this.email.setDisable(status);
    	this.saveBtn.setVisible(!status);
    	this.cancelBtn.setVisible(!status);
    }

    @FXML
    void toggleNew(ActionEvent event) {
    	this.tenant = null;
    	this.name.clear();
    	this.surname.clear();
    	this.roomNumber.clear();
    	this.telephone.clear();
    	this.email.clear();
    	if(this.name.isDisabled()) {
    		this.toggleEdit(event);
    	}
    }

    @FXML
    void initialize() {
    	this.tenantList.setOnMouseClicked((e)->{
    		Tenant tmp = this.tenantList.getSelectionModel().getSelectedItem();
    		if(tmp==null) return;
    		else {
    			this.tenant = tmp;
    			this.name.setText(this.tenant.getName());
    			this.surname.setText(this.tenant.getSurname());
    			this.roomNumber.setText(this.tenant.getRoomNumber().toString());
    			this.telephone.setText(this.tenant.getTelephone());
    			this.email.setText(this.tenant.getEmail());
    			if(!this.name.isDisabled()) {
    				this.toggleEdit(new ActionEvent());
    			}
    		}
    	});
    }
    
    //Display all types of notifications using and alert dialog box
    private Optional<ButtonType> notify(String message, AlertType alertType) {
    	alert.setContentText(message);
    	alert.setAlertType(alertType);
		this.alert.getButtonTypes().clear();
    	if(alertType==AlertType.CONFIRMATION) {
    		this.alert.getButtonTypes().addAll(ButtonType.NO, ButtonType.YES);
    	}else {
    		this.alert.getButtonTypes().add(ButtonType.OK);
    	}
    	return alert.showAndWait();
    }

	@Override
	public void setParent(MainAppController parent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setEntityManager(EntityManager manager) {
		this.manager = manager;
    	//Load tenant list and add to ListView
		List<Tenant> tenants = this.manager.createQuery("Select t from Tenant t where t.status=1", Tenant.class).getResultList();
		this.tenantList.getItems().addAll(tenants);
	}
	
	private int getNextId() {
		//Load max tenant id and increment if necessary
		Integer item = this.manager.createQuery("select max(u.id) from Tenant u", Integer.class).getSingleResult();
		if(item==null) return 1;
		else {
			return item+1;
		}
	}
}
