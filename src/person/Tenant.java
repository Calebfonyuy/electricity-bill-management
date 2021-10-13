package person;

import java.io.Serializable;
import java.lang.Integer;
import java.lang.String;
import javax.persistence.*;

/**
 * Entity implementation class for Entity: Tenant
 *
 */
@Entity(name="Tenant")
@SequenceGenerator(name="seq", initialValue=1, allocationSize=100)
public class Tenant implements Serializable {

	 @Id
	 @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="seq")
	private int id; 
	private String name; 
	private String surname; 
	private Integer roomNumber; 
	private String telephone; 
	private String email;
	private Integer status;
	private static final long serialVersionUID = 1L;	
	public Tenant() {
		super();
	} 
	   
	public Integer getId() {
 		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}
	   
	public String getName() {
 		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}
	   
	public String getSurname() {
 		return this.surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}
	   
	public Integer getRoomNumber() {
 		return this.roomNumber;
	}

	public void setRoomNumber(Integer roomNumber) {
		this.roomNumber = roomNumber;
	}
	   
	public String getTelephone() {
 		return this.telephone;
	}

	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}
	   
	public String getEmail() {
 		return this.email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}
	
   @Override
   public String toString() {
	   return this.name+" "+ this.surname;
   }
}
