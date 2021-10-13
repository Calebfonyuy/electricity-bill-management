package bill;

import bill.BillStatus;
import bill.Month;
import java.io.Serializable;
import java.lang.Double;
import java.lang.Integer;
import java.sql.Date;
import java.util.List;

import javax.persistence.*;

/**
 * Entity implementation class for Entity: GeneralBill
 *
 */
@Entity(name="MonthBill")
public class MonthBill implements Serializable {

	@Id
	private Integer id; 
	private Month month; 
	private Integer year; 
	private Double units; 
	private Double unit_cost; 
	private BillStatus status;
	private Date deadline;
	private static final long serialVersionUID = 1L;	
	public MonthBill() {
		super();
	} 
	   
	public Integer getId() {
 		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}
	   
	public Month getMonth() {
 		return this.month;
	}

	public void setMonth(Month month) {
		this.month = month;
	}
	   
	public Integer getYear() {
 		return this.year;
	}

	public void setYear(Integer year) {
		this.year = year;
	}
	   
	public Double getUnits() {
 		return this.units;
	}

	public void setUnits(Double units) {
		this.units = units;
	}
	   
	public Double getUnit_cost() {
 		return this.unit_cost;
	}

	public void setUnit_cost(Double unit_cost) {
		this.unit_cost = unit_cost;
	}
	   
	public BillStatus getStatus() {
 		return this.status;
	}

	public void setStatus(BillStatus status) {
		this.status = status;
	}
	
	 @Override
	public String toString() {
		return month+", "+year;
	}
	 
	public String displayBill() {
		return "month=" + month + ", year=" + year + ", units=" + units + ", unit cost=" + unit_cost+", deadline="+this.deadline.toString();
	}

	public Date getDeadline() {
		return deadline;
	}

	public void setDeadline(Date deadline) {
		this.deadline = deadline;
	}
	
	public boolean updateStatus() {
		if(this.status==BillStatus.PAID) return false;
		Date today = new Date(System.currentTimeMillis());
		if(this.deadline.before(today)||this.deadline.equals(today)) {
			this.status = BillStatus.DUE;
			return true;
		}
		return false;
	}
	
	public boolean updateStatus(EntityManager manager) {
		if(this.status==BillStatus.PAID) return false;
		Double total = manager.createQuery("select sum(p.amount) from Payment p where p.bill in "
				+ "(select b from PersonalBill b where b.monthBill=?1)", Double.class).setParameter(1, this).getSingleResult();
		if((total-this.getTotalBill())<=1E-5) {
			this.setStatus(BillStatus.PAID);
		}else if(total>0) {
			this.setStatus(BillStatus.PARTIAL);
		}else {
			return false;
		}
		return true;
	}

	public Double getTotalBill() {
		return this.unit_cost*this.units;
	}
	
	public Double getSharedBill(EntityManager manager) {
		List<PersonalBill> bills = manager.createQuery("select b from PersonalBill b where b.monthBill=?1", PersonalBill.class)
				.setParameter(1, this).getResultList();
		Double total = 0.0;
		for(PersonalBill bill: bills) {
			total += bill.getTotalBill();
		}
		return total;
	}

	public Double getUnsharedBill(EntityManager manager) {
		return this.getTotalBill()-this.getSharedBill(manager);
	}
	
	public Double getPaidAmount(EntityManager manager) {
		try {
			Double total = manager.createQuery("select sum(p.amount) from Payment p where p.bill in"
					+ "(select b from PersonalBill b where b.monthBill=?1)", Double.class).setParameter(1, this).getSingleResult();
			if(total==null) return 0d;
			else return total;
		}catch(Exception ex) {
			ex.printStackTrace();
		}
		return 0d;
	}

	public Double getUnpaidAmount(EntityManager manager) {
		try {
			Double total = manager.createQuery("select sum(p.amount) from Payment p where p.bill in"
					+ "(select b from PersonalBill b where b.monthBill=?1)", Double.class).setParameter(1, this).getSingleResult();
			if(total==null) return this.getTotalBill();
			else return this.getTotalBill()-total;
		}catch(Exception ex) {
			ex.printStackTrace();
		}
		return this.getTotalBill();
	}
}
