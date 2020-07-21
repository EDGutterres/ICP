package eduardo5ca.rest;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.List;

import javax.persistence.EntityManager;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONObject;

import eduardo5ca.cryptography.certificates.CertGen;
import eduardo5ca.cryptography.keys.KeyGen;
import eduardo5ca.entities.Certificate;
import eduardo5ca.entities.CertificateAuthority;
import eduardo5ca.rest.config.Emf;

@Path("home")
public class RestAPI {

	EntityManager entityManager = Emf.getInstance().getFactory().createEntityManager();
	boolean caExists;

	@GET
	public Response home() throws Exception {

		caExists = entityManager.createQuery("from CertificateAuthority").getResultList().size() != 0;

		if (!caExists) {

			return Response.status(200).entity("<h2>Ops!</h2>\n"
					+ "<h3>Nao foi detectada uma AC em nosso banco de dados. Por favor, preencha o formulario para criar uma nova AC.</h3>\n"
					+ "<form action=\"/eduardo5ca/api/home/certificateAuthority\" enctype=\"application/x-www-form-urlencoded\" method=\"POST\">\n"
					+ "    <label for=\"c\">Codigo do pais (C):</label><br>\n"
					+ "    <input type=\"text\" id=\"c\" name=\"c\" placeholder=\"Pais\" maxlength=\"2\" style=\"text-transform:uppercase; margin-top: 10px\"><br><br>\n"
					+ "    <label for=\"st\">Nome do estado ou provincia (ST):</label><br>\n"
					+ "    <input type=\"text\" id=\"st\" name=\"st\" placeholder=\"Estado\" style=\"margin-top: 10px;\"><br><br>\n"
					+ "    <label for=\"l\">Localidade (L):</label><br>\n"
					+ "    <input type=\"text\" id=\"l\" name=\"l\" placeholder=\"Local\" style=\"margin-top: 10px;\"><br><br>\n"
					+ "    <label for=\"o\">Nome da organizacao (O):</label><br>\n"
					+ "    <input type=\"text\" name=\"o\" id=\"o\" placeholder=\"Organizacao\" style=\"margin-top: 10px;\"><br><br>\n"
					+ "    <label for=\"ou\">Nome da unidade organizacional (OU):</label><br>\n"
					+ "    <input type=\"text\" name=\"ou\" id=\"ou\" placeholder=\"Unidade Organizacional\" style=\"margin-top: 10px;\"><br><br>\n"
					+ "    <label for=\"cn\">Nome comum (CN):</label><br>\n"
					+ "    <input type=\"text\" name=\"cn\" id=\"cn\" placeholder=\"Nome\" style=\"margin-top: 10px;\"><br><br>\n"
					+ "    <label for=\"keylen\">Escolha o tamanho do par de chaves:</label><br>\n"
					+ "    <input type=\"number\" name=\"keylen\" id=\"keylen\" min=\"600\" max=\"5000\" placeholder=\"Tamanho\" style=\"margin-top: 10px;\"><br><br>\n"
					+ "    <label for=\"duration\">Qual a duracao do certificado (em dias)?</label><br>\n"
					+ "    <input type=\"number\" name=\"duration\" id=\"duration\" min=\"1\" placeholder=\"Duracao\" style=\"margin-top: 10px;\"><br><br>\n"
					+ "    <input type=\"submit\" value=\"Criar AC\">\n" + "</form>").build();
		}

		@SuppressWarnings("unchecked")
		List<CertificateAuthority> certList = entityManager.createQuery("from CertificateAuthority").getResultList();

		CertificateAuthority ca = certList.get(0);

		return Response.status(200).entity("<h1>Bem-vindo a ICP Eduardo!</h1><h2>Dados da AC:</h2>\n" + "<p>"
				+ ca.getData() + "</p><p>Certificado: " + ca.getCertificate()
				+ "</p> <h3><a href=\"/eduardo5user/api/home\">Acessar a pagina do usuario.</a></h3>")
				.build();
	}

	@POST
	@Path("certificateAuthority")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response createCA(@FormParam("c") String c, @FormParam("st") String st, @FormParam("l") String l,
			@FormParam("o") String o, @FormParam("ou") String ou, @FormParam("cn") String cn,
			@FormParam("keylen") int keylen, @FormParam("duration") int duration) throws Exception {

		KeyPair keypair = new KeyGen("rsa").generateKeyPair(keylen);
		PrivateKey privkey = keypair.getPrivate();
		PublicKey pubkey = keypair.getPublic();

		X509Certificate certificate = new CertGen(c, st, l, o, ou, cn).generate(privkey, pubkey, duration,
				"SHA256withRSA", true, null, 1);

		String certData = certificate.getSubjectDN().getName();
		String certString = Base64.getEncoder().encodeToString(certificate.getEncoded());
		String pubKeyString = Base64.getEncoder().encodeToString(pubkey.getEncoded());
		String privKeyString = Base64.getEncoder().encodeToString(privkey.getEncoded());

		CertificateAuthority ca = new CertificateAuthority(certString, certData, privKeyString, pubKeyString);

		entityManager.getTransaction().begin();
		entityManager.persist(ca);
		entityManager.getTransaction().commit();
		entityManager.close();
		return Response.status(200).entity(
				"<h3>AC criada com sucesso!</h3>\n" + "<a href=\"/eduardo5ca/api/home\"><button>Voltar</button></a>")
				.build();

	}

	@GET
	@Path("caInfo")
	public String caInfo() throws Exception {
		caExists = entityManager.createQuery("from CertificateAuthority").getResultList().size() != 0;
		if (caExists) {
			@SuppressWarnings("unchecked")
			List<CertificateAuthority> certList = entityManager.createQuery("from CertificateAuthority")
					.getResultList();
			CertificateAuthority ca = certList.get(0);

			return new JSONObject().put("privkey", ca.getPrivateKey()).put("cert", ca.getCertificate()).toString();
		}
		return null;
	}

	@GET
	@Path("certificate")
	public String signCertificate(@QueryParam("csr") String csr, @QueryParam("pubkey") String pubkey,
			@QueryParam("id") int id, @QueryParam("duration") int duration) throws Exception {

		@SuppressWarnings("unchecked")
		List<CertificateAuthority> caList = entityManager.createQuery("from CertificateAuthority").getResultList();
		CertificateAuthority ca = caList.get(0);

		String caPrivate = ca.getPrivateKey();
		String caB64 = ca.getCertificate();
		String certificate = CertGen.sign(csr, caPrivate, caB64, pubkey, id, duration);
		
		Certificate cert = new Certificate(id, certificate, false);
		
		entityManager.getTransaction().begin();
		entityManager.persist(cert);
		entityManager.getTransaction().commit();
		entityManager.close();
		
		return CertGen.getCertificateFromString(certificate).getSubjectDN().getName();
	}

	@GET
	@Path("caExists")
	public String caExists() {
		caExists = entityManager.createQuery("from CertificateAuthority").getResultList().size() != 0;
		return caExists + "";
	}
	
	@GET
	@Path("revoked")
	public String revoked(@QueryParam("id") int id) {
		@SuppressWarnings("unchecked")
		List<Certificate> certList = entityManager.createQuery("from Certificate").getResultList();
		boolean revoked = false;
		
		for (Certificate cert: certList) {
			if (cert.getUserId()==id && !cert.isRevoked()) {
				return "false";
			}
			if (cert.getUserId()==id && cert.isRevoked()) {
				revoked = true;
			}
				
		}
		
		if (revoked) {
			return "true";
		}
		
		return "not created";
		
	}
	
	@GET
	@Path("{id}/revoke")
	public String revoke(@PathParam("id") int id) {
		
		@SuppressWarnings("unchecked")
		List<Certificate> certList = entityManager.createQuery("from Certificate").getResultList();
		
		for (Certificate cert: certList) {
			if (cert.getUserId()==id && !cert.isRevoked()) {
				entityManager.getTransaction().begin();
				cert.setRevoked(true);
				entityManager.getTransaction().commit();
				entityManager.close();	
				return "revoked";
			}
				
		}
		
		return "not found";
	}
	
	@GET
	@Path("{id}/certificate")
	public String getCertificate(@PathParam("id") int id) {
		
		@SuppressWarnings("unchecked")
		List<Certificate> certList = entityManager.createQuery("from Certificate").getResultList();
		String certificate = "";
		
		for (Certificate cert: certList) {
			if (cert.getUserId()==id && !cert.isRevoked()) {
				return cert.getCertificate();
			}
			if (cert.getUserId()==id) {
				certificate = cert.getCertificate();
			}
				
		}
		
		return certificate;
	}
	
	@GET
	@Path("{id}/certificate/certId")
	public String getCertId(@PathParam("id") int id) {
		
		@SuppressWarnings("unchecked")
		List<Certificate> certList = entityManager.createQuery("from Certificate").getResultList();
		
		for (Certificate cert: certList) {
			if (cert.getUserId()==id && !cert.isRevoked()) {
				return cert.getCertID()+"";
			}	
		}
		
		return "0";
	}

}
