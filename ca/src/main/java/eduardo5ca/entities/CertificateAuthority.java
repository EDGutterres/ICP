package eduardo5ca.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

@Entity
@Table(name="CertificateAuthorities")
public class CertificateAuthority {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Type(type="int")
	private int id;
	
	@Column(name="Certificate")
	@Type(type="text")
	private String certificate;
	
	@Column(name = "PrivateKey")
	@Type(type = "text")
	private String privateKey;

	@Column(name = "PublicKey")
	@Type(type = "text")
	private String publicKey;
	
	@Column(name = "Data")
	@Type(type = "text")
	private String data;
	
	public CertificateAuthority() {}
	
	public CertificateAuthority(String certificate, String data, String privateKey, String publicKey) {
		setCertificate(certificate);
		setData(data);
		setPrivateKey(privateKey);
		setPublicKey(publicKey);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getCertificate() {
		return certificate;
	}
	
	public void setCertificate(String certificate) {
		this.certificate = certificate;
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
	
	public String getData() {
		return data;
	}
	
	public void setData(String data) {
		this.data = data;
	}
	
}
