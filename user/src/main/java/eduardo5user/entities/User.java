package eduardo5user.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name = "Users")
public class User {
	
	@Id
	@Column(name = "Id")
	private int id;
	
	@Column(name = "Username")
	private String username;
	
	@Column(name = "Credentials")
	private String creds;
	
	@Column(name = "Data")
	private String data;
	

	public User() {
	}

	public User(int id, String data, String username, String creds) {
		setId(id);
		setData(data);
		setUsername(username);
		setCreds(creds);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}

	public String getData() {
		return data;
	}
	
	public void setData(String data) {
		this.data = data;
	}
	
	public String getCreds() {
		return creds;
	}
	
	public void setCreds(String creds) {
		this.creds = creds;
	}

}
