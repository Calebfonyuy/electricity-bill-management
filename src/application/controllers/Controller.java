package application.controllers;

import javax.persistence.EntityManager;

public interface Controller {
	abstract void setParent(MainAppController parent);
	
	abstract void setEntityManager(EntityManager manager);
}
