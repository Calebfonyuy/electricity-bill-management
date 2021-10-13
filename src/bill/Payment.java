package bill;

import bill.PaymentMode;
import bill.PaymentStatus;
import bill.PersonalBill;
import java.io.Serializable;
import java.lang.Integer;
import java.sql.Date;
import javax.persistence.*;
import person.Tenant;

/**
 * Entity implementation class for Entity: Payment
 *
 */
@Entity
public class Payment implements Serializable {
	 @Id
	 @GeneratedValue(strategy=GenerationType.TABLE)
	private int id; 
	private Date date; 
	private Double amount; 
	private PaymentMode payment_mode; 
	private Tenant tenant; 
	private PersonalBill bill; 
	private PaymentStatus status;
	private static final long serialVersionUID = 1L;	
	public Payment() {
		super();
	} 
	   
	public Integer getId() {
 		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}
	   
	public Date getDate() {
 		return this.date;
	}

	public void setDate(Date date) {
		this.date = date;
	}
	   
	public Double getAmount() {
 		return this.amount;
	}

	public void setAmount(double paid) {
		this.amount = paid;
	}
	   
	public PaymentMode getPayment_mode() {
 		return this.payment_mode;
	}

	public void setPayment_mode(PaymentMode payment_mode) {
		this.payment_mode = payment_mode;
	}
	   
	public Tenant getTenant() {
 		return this.tenant;
	}

	public void setTenant(Tenant tenant) {
		this.tenant = tenant;
	}
	   
	public PersonalBill getBill() {
 		return this.bill;
	}

	public void setBill(PersonalBill bill) {
		this.bill = bill;
	}
	   
	public PaymentStatus getStatus() {
 		return this.status;
	}

	public void setStatus(PaymentStatus status) {
		this.status = status;
	}
   
}
