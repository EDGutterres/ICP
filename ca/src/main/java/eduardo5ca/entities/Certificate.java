package eduardo5ca.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

@Entity
@Table(name = "Certificates")
public class Certificate {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "Cert_Id")
	private int certID;
	
	@Column(name = "User_Id")
	private int userId;
	
	@Column(name = "Certificate")
	@Type(type = "text")
	private String certificate;
	
	@Column(name = "Revoked")
	private boolean revoked;
	
	public Certificate() {}
	
	public Certificate (int userId, String certificate, boolean revoked) {
		setUserId(userId);
		setCertificate(certificate);
		setRevoked(revoked);
	}

	public int getCertID() {
		return certID;
	}

	public void setCertID(int certID) {
		this.certID = certID;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getCertificate() {
		return certificate;
	}

	public void setCertificate(String certificate) {
		this.certificate = certificate;
	}

	public boolean isRevoked() {
		return revoked;
	}

	public void setRevoked(boolean revoked) {
		this.revoked = revoked;
	}
	
	
	
	
}
