package bill;

import bill.BillStatus;

import java.io.Serializable;
import java.lang.Integer;
import java.sql.Date;
import javax.persistence.*;
import person.Tenant;

/**
 * Entity implementation class for Entity: PersonalBill
 *
 */
@Entity(name="PersonalBill")
public class PersonalBill implements Serializable {

	 @Id
	private Integer id; 
	private Double previous_reading; 
	private Double current_reading; 
	private Date date_read; 
	private Double extraCharge; 
	private Date deadline; 
	private BillStatus status; 
	private Tenant tenant;
	private MonthBill monthBill;
	@Transient
	private Double totalBill;
	@Transient
	private Double totalUnits;
	@Transient
	private Double unitCost;
	private static final long serialVersionUID = 1L;	
	public PersonalBill() {
		super();
	} 
	   
	public Integer getId() {
 		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}
	   
	public Double getPrevious_reading() {
 		return this.previous_reading;
	}

	public void setPrevious_reading(Double previous_reading) {
		this.previous_reading = previous_reading;
	}
	   
	public Double getCurrent_reading() {
 		return this.current_reading;
	}

	public void setCurrent_reading(Double current_reading) {
		this.current_reading = current_reading;
	}
	   
	public Date getDate_read() {
 		return this.date_read;
	}

	public void setDate_read(Date date_read) {
		this.date_read = date_read;
	}
	   
	public Double getExtraCharge() {
 		return this.extraCharge;
	}

	public void setExtraCharge(Double extraCharge) {
		this.extraCharge = extraCharge;
	}
	   
	public Date getDeadline() {
 		return this.deadline;
	}

	public void setDeadline(Date deadline) {
		this.deadline = deadline;
	}
	   
	public BillStatus getStatus() {
 		return this.status;
	}

	public void setStatus(BillStatus status) {
		this.status = status;
	}
	   
	public Tenant getTenant() {
 		return this.tenant;
	}

	public void setTenant(Tenant tenant) {
		this.tenant = tenant;
	}

	public MonthBill getMonthBill() {
		return monthBill;
	}

	public void setMonthBill(MonthBill monthBill) {
		this.monthBill = monthBill;
		this.unitCost = this.monthBill.getUnit_cost();
	}

	public Double getTotalBill() {
		this.calculateTotalBill();
		return totalBill;
	}

	public Double getTotalUnits() {
		this.calculateTotalBill();
		return totalUnits;
	}
	
   private void calculateTotalBill() {
	   if(this.previous_reading!=null&&this.current_reading!=null&&this.monthBill==null||this.extraCharge!=null) {
		   this.totalUnits = this.current_reading-this.previous_reading;
		   this.totalBill = (this.totalUnits*this.monthBill.getUnit_cost())+this.extraCharge;
	   }else {
		   this.totalBill=0.0;
	   }
   }

	public Double getUnitCost() {
		this.unitCost = this.monthBill.getUnit_cost();
		return unitCost;
	}
	
	public void updateStatus(EntityManager manager) {
		if(this.deadline.before(new Date(System.currentTimeMillis()))) {
			this.status = BillStatus.DUE;
		}
		try {
			Double total = manager.createQuery("select sum(p.amount) from Payment p where p.bill=?1 and p.tenant=?2", Double.class)
					.setParameter(1, this).setParameter(2, this.tenant).getSingleResult();
			if((total-this.getTotalBill())==0.0) {
				this.status = BillStatus.PAID;
			}else {
				if(total>0) {
					this.status = BillStatus.PARTIAL;
					this.monthBill.setStatus(BillStatus.PARTIAL);
				}
			}
		}catch(Exception ex) {
			
		}
	}
	
	public Double getPaidAmount(EntityManager manager) {
		try {
			Double total = manager.createQuery("select sum(p.amount) from Payment p where p.bill=?1 and p.tenant=?2", Double.class)
					.setParameter(1, this).setParameter(2, this.tenant).getSingleResult();
			if(total==null) return 0.0;
			else return total;
		}catch(Exception ex) {
			ex.printStackTrace();
			return 0.0;
		}
	}

	public Double getUnpaidAmount(EntityManager manager) {
		try {
			Double total = manager.createQuery("select sum(p.amount) from Payment p where p.bill=?1 and p.tenant=?2", Double.class)
					.setParameter(1, this).setParameter(2, this.tenant).getSingleResult();
			if(total==null) return this.getTotalBill();
			else return this.getTotalBill()-total;
		}catch(Exception ex) {
			ex.printStackTrace();
			return this.getTotalBill();
		}
	}

	@Override
	public String toString() {
		return "PersonalBill [id=" + id + ", previous_reading=" + previous_reading + ", current_reading="
				+ current_reading + ", date_read=" + date_read + ", extraCharge=" + extraCharge + ", deadline="
				+ deadline + ", status=" + status + ", tenant=" + tenant + ", monthBill=" + monthBill + ", totalBill="
				+ totalBill + ", totalUnits=" + totalUnits + ", unitCost=" + unitCost + "]";
	}
}
