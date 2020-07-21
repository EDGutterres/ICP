package eduardo5crypto.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

@Entity
@Table(name = "Crypto")
public class Crypto {

	@Id
	@Type(type = "int")
	private int id;
	
	@Column(name = "Cert_Id")
	private int certId;
	
	@Column(name = "PrivateKey")
	@Type(type = "text")
	private String privateKey;

	@Column(name = "PublicKey")
	@Type(type = "text")
	private String publicKey;
	
	public Crypto() {}
	
	public Crypto(int id, String privateKey, String publicKey) {
		setId(id);
		setPrivateKey(privateKey);
		setPublicKey(publicKey);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public String getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}

	public String getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}
	
	public int getCertId() {
		return certId;
	}
	
	public void setCertId(int certId) {
		this.certId = certId;
	}
	
}
