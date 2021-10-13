package application.controllers;

import javax.persistence.EntityManager;

import bill.PersonalBill;
import javafx.stage.Stage;
import person.Tenant;

public interface BillController {
	public void setEntityManager(EntityManager manager);
	public void setTenant(Tenant tenant);
	public void setPersonalBill(PersonalBill bill);
	public void setStage(Stage stage);
	public void setParent(PersonalBillController parent);
	
	public PersonalBill getPersonalBill();
}
