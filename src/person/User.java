package person;

import java.io.Serializable;
import java.lang.String;
import javax.persistence.*;

/**
 * Entity implementation class for Entity: User
 *
 */
@Entity(name="User")
public class User implements Serializable {

	@Id 
	private int id; 
	private String name; 
	private String surname; 
	private String username; 
	private String password;
	private static final long serialVersionUID = 1L;	
	public User() {
		super();
	} 
	   
	public int getId() {
 		return this.id;
	}

	public void setId(int id) {
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
	   
	public String getUsername() {
 		return this.username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
	   
	public String getPassword() {
 		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public String toString() {
		return "User [id=" + id + ", name=" + name + ", surname=" + surname + ", username=" + username +"]";
	}
}
