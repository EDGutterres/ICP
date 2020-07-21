package eduardo6ra.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name = "UserData")
public class UserData {
	
	@Id
	@Column(name = "Id")
	private int id;
	
	@Column(name = "C")
	private String c;
	
	@Column(name = "ST")
	private String st;
	
	@Column(name = "L")
	private String l;
	
	@Column(name = "O")
	private String o;
	
	@Column(name = "OU")
	private String ou;
	
	@Column(name = "CN")
	private String cn;
	
	@Column(name = "KeyLen")
	private int keylen;
	
	@Column(name= "Duration")
	private int duration;
	
	@Column(name = "Approved")
	private boolean approved;
	
	@Column(name = "Refused")
	private boolean refused;
	

	public UserData() {
	}

	public UserData(int id, String c, String st, String l, String o, String ou, String cn, int keylen, int duration, boolean approved, boolean refused) {
		setId(id);
		setC(c);
		setSt(st);
		setL(l);
		setO(o);
		setOu(ou);
		setCn(cn);
		setKeylen(keylen);
		setDuration(duration);
		setApproved(approved);
		setRefused(refused);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getC() {
		return c;
	}

	public void setC(String c) {
		this.c = c;
	}

	public String getSt() {
		return st;
	}

	public void setSt(String st) {
		this.st = st;
	}

	public String getL() {
		return l;
	}

	public void setL(String l) {
		this.l = l;
	}

	public String getO() {
		return o;
	}

	public void setO(String o) {
		this.o = o;
	}

	public String getOu() {
		return ou;
	}

	public void setOu(String ou) {
		this.ou = ou;
	}

	public String getCn() {
		return cn;
	}

	public void setCn(String cn) {
		this.cn = cn;
	}

	public int getKeylen() {
		return keylen;
	}

	public void setKeylen(int keylen) {
		this.keylen = keylen;
	}
	
	public boolean isApproved() {
		return approved;
	}
	
	public void setApproved(boolean approved) {
		this.approved = approved;
	}

	public int getDuration() {
		return duration;
	}
	
	public void setDuration(int duration) {
		this.duration = duration;
	}
	
	public boolean isRefused() {
		return refused;
	}
	
	public void setRefused(boolean refused) {
		this.refused = refused;
	}
}
