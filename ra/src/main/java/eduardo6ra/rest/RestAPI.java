package eduardo6ra.rest;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import eduardo6ra.entities.RegistrationAuthority;
import eduardo6ra.entities.UserData;
import eduardo6ra.rest.config.Emf;
import eduardo6ra.rest.config.ParameterStringBuilder;

@Path("home")
public class RestAPI {

	EntityManager entityManager = Emf.getInstance().getFactory().createEntityManager();
	boolean raExists;

	@GET
	public Response home() throws Exception {
		if (caInfo("http://localhost:8080/eduardo5ca/api/home/caExists").equals("false")) {
			return Response.status(200).entity("Erro: CA nao existe.").build();
		}

		raExists = entityManager.createQuery("from RegistrationAuthority").getResultList().size() != 0;

		if (!raExists) {
			return Response.status(200).entity("<h2>Ops!</h2>\n"
					+ "<h3>Nao foi detectada uma AR em nosso banco de dados. Por favor, preencha o formulario para criar uma nova AR.</h3>\n"
					+ "<form action=\"/eduardo6ra/api/home/registrationAuthority\" enctype=\"application/x-www-form-urlencoded\" method=\"POST\">\n"
					+ "    <label for=\"c\">ID:</label><br>\n"
					+ "    <input type=\"number\" id=\"id\" name=\"id\" placeholder=\"Numero de identificacao\" maxlength=\"6\" style=\"margin-top: 10px\"><br><br>\n"
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
					+ "    <input type=\"submit\" value=\"Criar AR\">\n" + "</form>").build();
		}
		@SuppressWarnings("unchecked")
		List<RegistrationAuthority> raList = entityManager.createQuery("from RegistrationAuthority").getResultList();

		RegistrationAuthority ra = raList.get(0);

		return Response.status(200).entity("<h1>Bem-vindo a AR Eduardo!</h1><h2>Dados da AR:</h2>\n" + "<p>"
				+ ra.getData() + "</p><p>Certificado: " + ra.getCertificate()
				+ "</p> <h3><a href=\"/eduardo6ra/api/home/requisitions\">Ir para a pagina de requisicoes de certificados.</a></h3>")
				.build();
	}

	@POST
	@Path("registrationAuthority")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response createRA(@FormParam("id") int id, @FormParam("c") String c, @FormParam("st") String st,
			@FormParam("l") String l, @FormParam("o") String o, @FormParam("ou") String ou, @FormParam("cn") String cn,
			@FormParam("keylen") int keylen, @FormParam("duration") int duration) throws Exception {

		String csr = csrInfo(id, c, st, l, o, ou, cn, keylen);
		String pubkey = getPublicKey(id);
		String privKey = getPrivateKey(id);
		String data = signCertificate(csr, pubkey, id, duration);
		String certificate = getCertificate(id);

		RegistrationAuthority ra = new RegistrationAuthority(id, certificate, data, privKey, pubkey);

		entityManager.getTransaction().begin();
		entityManager.persist(ra);
		entityManager.getTransaction().commit();
		entityManager.close();

		return Response.status(200).entity(
				"<h3>AR criada com sucesso!</h3>\n" + "<a href=\"/eduardo6ra/api/home\"><button>Voltar</button></a>")
				.build();

	}

	@GET
	@Path("requisitions")
	public Response viewReqs() {
		@SuppressWarnings("unchecked")
		List<UserData> dataList = entityManager.createQuery("from UserData").getResultList();
		String message = "<h2>Lista de requisicao de certificados</h2>";

		for (UserData ud : dataList) {
			if (!ud.isApproved() && !ud.isRefused()) {
				message += "<h3>" + ud.getCn() + "</h3>";
				message += "<p>Dados: C = " + ud.getC() + ", ST = " + ud.getSt() + ", L = " + ud.getL() + ", O = "
						+ ud.getO() + ", OU = " + ud.getOu() + "." + "</p>";
				message += "<form action=\"/eduardo6ra/api/home/" + ud.getId() + "/approve\" method=\"POST\">\n"
						+ "    <button type=\"submit\">Aprovar</button>\n" + "</form>\n"
						+ "<form action=\"/eduardo6ra/api/home/" + ud.getId() + "/refuse\" method=\"POST\">\n"
						+ "    <button type=\"submit\">Reprovar</button>\n" + "</form>";
			}
		}

		return Response.status(200).entity(message).build();
	}

	@POST
	@Path("{id}/approve")
	public Response approve(@PathParam("id") int id) throws Exception {
		@SuppressWarnings("unchecked")
		List<UserData> dataList = entityManager.createQuery("from UserData").getResultList();
		UserData theUser = new UserData();
		boolean foundUser = false;

		for (UserData ud : dataList) {
			if (ud.getId() == id) {
				theUser = ud;
				foundUser = true;
			}
		}

		if (foundUser) {
			String csr = csrInfo(id, theUser.getC(), theUser.getSt(), theUser.getL(), theUser.getO(), theUser.getOu(),
					theUser.getCn(), theUser.getKeylen());
			String pubkey = getPublicKey(id);
			String data = signCertificate(csr, pubkey, id, theUser.getDuration());

			entityManager.getTransaction().begin();
			theUser.setApproved(true);
			entityManager.getTransaction().commit();
			entityManager.close();

			updateUserData(id, data);
			setCertId(id, getCertId(id));

			return Response.status(200).entity("<h3>Certificado aprovado.</h3>\n"
					+ "<a href=\"/eduardo6ra/api/home/requisitions\"><button>Voltar</button></a>").build();
		}

		return Response.status(303).entity("Erro").build();
	}

	@POST
	@Path("{id}/refuse")
	public Response refuse(@PathParam("id") int id) throws Exception {
		@SuppressWarnings("unchecked")
		List<UserData> dataList = entityManager.createQuery("from UserData").getResultList();
		UserData theUser = new UserData();
		boolean foundUser = false;

		for (UserData ud : dataList) {
			if (ud.getId() == id) {
				theUser = ud;
				foundUser = true;
			}
		}

		if (foundUser) {

			entityManager.getTransaction().begin();
			theUser.setRefused(true);
			entityManager.getTransaction().commit();
			entityManager.close();

			return Response.status(200).entity("<h3>Certificado reprovado.</h3>\n"
					+ "<a href=\"/eduardo6ra/api/home/requisitions\"><button>Voltar</button></a>").build();
		}

		return Response.status(303).entity("Erro").build();
	}

	@GET
	@Path("template")
	public String setTemplate(@QueryParam("id") int id, @QueryParam("c") String c, @QueryParam("st") String st,
			@QueryParam("l") String l, @QueryParam("o") String o, @QueryParam("ou") String ou,
			@QueryParam("cn") String cn, @QueryParam("keylen") int keylen, @QueryParam("duration") int duration)
			throws Exception {

		UserData userData = new UserData(id, c, st, l, o, ou, cn, keylen, duration, false, false);

		entityManager.getTransaction().begin();
		entityManager.persist(userData);
		entityManager.getTransaction().commit();
		entityManager.close();

		return "sucess";
	}

	@GET
	@Path("approved")
	public String isApproved(@QueryParam("id") int id) {

		@SuppressWarnings("unchecked")
		List<UserData> dataList = entityManager.createQuery("from UserData").getResultList();

		for (UserData ud : dataList) {
			if (ud.getId() == id) {
				return ud.isApproved() + "";
			}
		}

		return "Error";
	}

	@GET
	@Path("refused")
	public String isRefused(@QueryParam("id") int id) {

		@SuppressWarnings("unchecked")
		List<UserData> dataList = entityManager.createQuery("from UserData").getResultList();

		for (UserData ud : dataList) {
			if (ud.getId() == id) {
				return ud.isRefused() + "";
			}
		}

		return "Error";
	}

	@GET
	@Path("id")
	public String getRaId() {
		@SuppressWarnings("unchecked")
		List<RegistrationAuthority> raList = entityManager.createQuery("from RegistrationAuthority").getResultList();

		RegistrationAuthority ra = raList.get(0);

		return ra.getId() + "";
	}

	@GET
	@Path("raExists")
	public String raExists() {
		raExists = entityManager.createQuery("from RegistrationAuthority").getResultList().size() != 0;
		return raExists + "";
	}
	
	@POST
	@Path("{id}/revoke")
	public Response revoke(@PathParam("id") int id) throws Exception {

		URL url = new URL("http://localhost:8080/eduardo5ca/api/home/"+id+"/revoke");
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer content = new StringBuffer();
		while ((inputLine = in.readLine()) != null) {
			content.append(inputLine);
		}
		in.close();
		
		return Response
				.status(200).entity("<h3>Certificado revogado com sucesso.</h3>\n"
						+ "<h4><a href=\"/eduardo5user/api/home/" + id + "?auth=true\" >Voltar para a pagina do usuario</a></h4>")
				.build();

	}
	
	@GET
	@Path("updateTemplate")
	public String updateTemplate(@QueryParam("id") int id, @QueryParam("duration") int duration, @QueryParam("keylen") int keylen) {
		@SuppressWarnings("unchecked")
		List<UserData> dataList = entityManager.createQuery("from UserData").getResultList();
		UserData theUser = new UserData();
		boolean foundUser = false;

		for (UserData ud : dataList) {
			if (ud.getId() == id) {
				theUser = ud;
				foundUser = true;
			}
		}

		if (foundUser) {

			entityManager.getTransaction().begin();
			theUser.setApproved(false);
			theUser.setRefused(false);
			theUser.setDuration(duration);
			theUser.setKeylen(keylen);
			entityManager.getTransaction().commit();
			entityManager.close();
			
			return "ok";

		}
		
		return "error";
	}
	
	@GET
	@Path("revoked")
	public String isRevoked(@QueryParam("id") int id) throws Exception {
		Map<String, String> parameters = new HashMap<>();
		parameters.put("id", id + "");

		URL url = new URL("http://localhost:8080/eduardo5ca/api/home/revoked?"
				+ ParameterStringBuilder.getParamsString(parameters));
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");

		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer content = new StringBuffer();
		while ((inputLine = in.readLine()) != null) {
			content.append(inputLine);
		}
		in.close();
		return content.toString();
	}

	private String caInfo(String urlString) throws Exception {
		URL url = new URL(urlString);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer content = new StringBuffer();
		while ((inputLine = in.readLine()) != null) {
			content.append(inputLine);
		}
		in.close();
		return content.toString();
	}

	private String csrInfo(int id, String c, String st, String l, String o, String ou, String cn, int keylen)
			throws Exception {
		Map<String, String> parameters = new HashMap<>();
		parameters.put("id", id + "");
		parameters.put("c", c);
		parameters.put("st", st);
		parameters.put("l", l);
		parameters.put("o", o);
		parameters.put("ou", ou);
		parameters.put("cn", cn);
		parameters.put("keylen", keylen + "");

		URL url = new URL("http://localhost:8080/eduardo5crypto/api/home/CSR?"
				+ ParameterStringBuilder.getParamsString(parameters));
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");

		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer content = new StringBuffer();
		while ((inputLine = in.readLine()) != null) {
			content.append(inputLine);
		}
		in.close();
		return content.toString();
	}

	private String signCertificate(String csr, String pubkey, int id, int duration) throws Exception {
		Map<String, String> parameters = new HashMap<>();
		parameters.put("csr", csr);
		parameters.put("pubkey", pubkey);
		parameters.put("id", id + "");
		parameters.put("duration", duration + "");

		URL url = new URL("http://localhost:8080/eduardo5ca/api/home/certificate?"
				+ ParameterStringBuilder.getParamsString(parameters));
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");

		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer content = new StringBuffer();
		while ((inputLine = in.readLine()) != null) {
			content.append(inputLine);
		}
		in.close();
		return content.toString();

	}

	private String getPublicKey(int id) throws Exception {
		Map<String, String> parameters = new HashMap<>();
		parameters.put("id", id + "");

		URL url = new URL("http://localhost:8080/eduardo5crypto/api/home/publicKey?"
				+ ParameterStringBuilder.getParamsString(parameters));
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");

		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer content = new StringBuffer();
		while ((inputLine = in.readLine()) != null) {
			content.append(inputLine);
		}
		in.close();
		return content.toString();

	}

	private String getPrivateKey(int id) throws Exception {
		Map<String, String> parameters = new HashMap<>();
		parameters.put("id", id + "");

		URL url = new URL("http://localhost:8080/eduardo5crypto/api/home/privateKey?"
				+ ParameterStringBuilder.getParamsString(parameters));
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");

		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer content = new StringBuffer();
		while ((inputLine = in.readLine()) != null) {
			content.append(inputLine);
		}
		in.close();
		return content.toString();

	}

	private String updateUserData(int id, String data) throws Exception {

		Map<String, String> parameters = new HashMap<>();
		parameters.put("id", id + "");
		parameters.put("data", data);

		URL url = new URL("http://localhost:8080/eduardo5user/api/home/updateData?"
				+ ParameterStringBuilder.getParamsString(parameters));
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");

		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer content = new StringBuffer();
		while ((inputLine = in.readLine()) != null) {
			content.append(inputLine);
		}
		in.close();
		return content.toString();
	}

	private String getCertificate(int id) throws Exception {
		URL url = new URL("http://localhost:8080/eduardo5ca/api/home/" + id + "/certificate");
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");

		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer content = new StringBuffer();
		while ((inputLine = in.readLine()) != null) {
			content.append(inputLine);
		}
		in.close();
		return content.toString();
	}

	private String getCertId(int id) throws Exception {
		URL url = new URL("http://localhost:8080/eduardo5ca/api/home/" + id + "/certificate/certId");
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");

		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer content = new StringBuffer();
		while ((inputLine = in.readLine()) != null) {
			content.append(inputLine);
		}
		in.close();
		return content.toString();
	}

	private String setCertId(int id, String certId) throws Exception {
		URL url = new URL("http://localhost:8080/eduardo5crypto/api/home/" + id + "/certId/" + certId);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");

		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer content = new StringBuffer();
		while ((inputLine = in.readLine()) != null) {
			content.append(inputLine);
		}
		in.close();
		return content.toString();
	}


}
