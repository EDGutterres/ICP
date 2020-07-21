package eduardo5crypto.rest;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.List;

import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.bouncycastle.cms.CMSSignedData;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;

import javax.persistence.EntityManager;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import eduardo5crypto.cryptography.certificates.CSRGen;
import eduardo5crypto.cryptography.keys.KeyGen;
import eduardo5crypto.cryptography.signatures.SignatureGenerator;
import eduardo5crypto.entities.Crypto;
import eduardo5crypto.rest.config.Emf;
import eduardo5crypto.rest.config.FileUploadForm;

@Path("home")
public class RestAPI {

	EntityManager entityManager = Emf.getInstance().getFactory().createEntityManager();

	@GET
	@Path("CSR")
	public String getCSR(@QueryParam("id") int id, @QueryParam("c") String c, @QueryParam("st") String st,
			@QueryParam("l") String l, @QueryParam("o") String o, @QueryParam("ou") String ou,
			@QueryParam("cn") String cn, @QueryParam("keylen") int keylen) throws Exception {

		KeyPair keypair = new KeyGen("rsa").generateKeyPair(keylen);

		CSRGen csrGen = new CSRGen(keypair);
		String csr = csrGen.getCSR(c, st, l, o, ou, cn);

		@SuppressWarnings("unchecked")
		List<Crypto> cryptoList = entityManager.createQuery("from Crypto").getResultList();
		Crypto newCrypto = new Crypto();
		boolean foundUser = false;

		for (Crypto crypto : cryptoList) {
			if (crypto.getId() == id) {
				newCrypto = crypto;
				foundUser = true;
			}
		}

		if (foundUser) {
			entityManager.getTransaction().begin();
			newCrypto.setPrivateKey(Base64.getEncoder().encodeToString(keypair.getPrivate().getEncoded()));
			newCrypto.setPublicKey(Base64.getEncoder().encodeToString(keypair.getPublic().getEncoded()));

		} else {

			Crypto crypto = new Crypto(id, Base64.getEncoder().encodeToString(keypair.getPrivate().getEncoded()),
					Base64.getEncoder().encodeToString(keypair.getPublic().getEncoded()));

			entityManager.getTransaction().begin();
			entityManager.persist(crypto);

		}
		entityManager.getTransaction().commit();
		entityManager.close();
		return csr;
	}

	@GET
	@Path("publicKey")
	public String getPublicKey(@QueryParam("id") int id) {

		@SuppressWarnings("unchecked")
		List<Crypto> cryptoList = entityManager.createQuery("from Crypto").getResultList();

		for (Crypto c : cryptoList) {
			if (c.getId() == id) {
				return c.getPublicKey();
			}
		}

		return null;
	}

	@GET
	@Path("privateKey")
	public String getPrivateKey(@QueryParam("id") int id) {

		@SuppressWarnings("unchecked")
		List<Crypto> cryptoList = entityManager.createQuery("from Crypto").getResultList();

		for (Crypto c : cryptoList) {
			if (c.getId() == id) {
				return c.getPrivateKey();
			}
		}

		return null;
	}

	@POST
	@Path("{id}/file")
	@Consumes("multipart/form-data")
	@Produces("text/plain")
	public Response sign(@PathParam("id") int id, @QueryParam("certificate") String certificate,
			@MultipartForm FileUploadForm form) throws Exception {

		@SuppressWarnings("unchecked")
		List<Crypto> cryptoList = entityManager.createQuery("from Crypto").getResultList();
		boolean foundUser = false;

		for (Crypto newCrypto : cryptoList) {
			if (newCrypto.getId() == id) {
				foundUser = true;
				break;
			}
		}

		if (foundUser) {

			PrivateKey privKey = KeyGen.getPrivateKeyFromString(getPrivateKey(id));

			byte[] uploadedFile = form.getData();

			X509Certificate cert = CSRGen.getCertificateFromString(certificate);

			SignatureGenerator sigGen = new SignatureGenerator();
			sigGen.informSigner(cert, privKey);
			CMSSignedData signature = sigGen.sign(uploadedFile);

			String filename = "myFileSignature.der";
			File fileToBeDownloaded = new File(filename);

			if (!fileToBeDownloaded.exists()) {
				fileToBeDownloaded.createNewFile();
			}

			sigGen.writeSignature(fileToBeDownloaded, signature);

			ResponseBuilder response = Response.ok((Object) fileToBeDownloaded);
			response.header("Content-Disposition", "attachment; filename=\"" + filename + "\"");
			return response.build();
		}

		return Response.status(200).entity("<h3>Erro: Nao foi encontrado um usuario com esse ID.</h3>\n"
				+ "<a href=\"/eduardo5user/api/home\"><button>Voltar</button></a>").build();
	}

	@POST
	@Path("{id}/publicKey")
	@Consumes("multipart/form-data")
	@Produces("text/plain")
	public Response downloadPublic(@PathParam("id") int id) throws Exception {

		@SuppressWarnings("unchecked")
		List<Crypto> cryptoList = entityManager.createQuery("from Crypto").getResultList();
		String key = "";
		boolean foundKey = false;

		for (Crypto c : cryptoList) {
			if (c.getId() == id) {
				key = c.getPublicKey();
				foundKey = true;
			}
		}

		if (foundKey) {

			String filename = "myPublicKey.key";
			File fileToBeDownloaded = new File(filename);

			if (!fileToBeDownloaded.exists()) {
				fileToBeDownloaded.createNewFile();
			}

			BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
			writer.write(key);
			writer.flush();
			writer.close();

			ResponseBuilder response = Response.ok((Object) fileToBeDownloaded);
			response.header("Content-Disposition", "attachment; filename=\"" + filename + "\"");
			return response.build();

		}

		return Response.status(303).entity("<h2>Usuario ou arquivo nao encontrados.</h2>").build();

	}

	@POST
	@Path("{id}/privateKey")
	@Consumes("multipart/form-data")
	@Produces("text/plain")
	public Response downloadPrivate(@PathParam("id") int id) throws Exception {

		@SuppressWarnings("unchecked")
		List<Crypto> cryptoList = entityManager.createQuery("from Crypto").getResultList();
		String key = "";
		boolean foundKey = false;

		for (Crypto c : cryptoList) {
			if (c.getId() == id) {
				key = c.getPrivateKey();
				foundKey = true;
			}
		}

		if (foundKey) {

			String filename = "myPrivateKey.key";
			File fileToBeDownloaded = new File(filename);

			if (!fileToBeDownloaded.exists()) {
				fileToBeDownloaded.createNewFile();
			}

			BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
			writer.write(key);
			writer.flush();
			writer.close();

			ResponseBuilder response = Response.ok((Object) fileToBeDownloaded);
			response.header("Content-Disposition", "attachment; filename=\"" + filename + "\"");
			return response.build();

		}

		return Response.status(303).entity("<h2>Usuario ou arquivo nao encontrados.</h2>").build();

	}

	@GET
	@Path("{id}/certId/{certId}")
	public String setCertId(@PathParam("id") int id, @PathParam("certId") int certId) {
		@SuppressWarnings("unchecked")
		List<Crypto> cryptoList = entityManager.createQuery("from Crypto").getResultList();

		for (Crypto newCrypto : cryptoList) {
			if (newCrypto.getId() == id) {
				entityManager.getTransaction().begin();
				newCrypto.setCertId(certId);
				entityManager.getTransaction().commit();
				entityManager.close();
				return "ok";
			}
		}

		return "not ok";
	}

}
