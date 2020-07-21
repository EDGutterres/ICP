package eduardo5user.rest;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
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

import eduardo5user.entities.User;
import eduardo5user.rest.config.Emf;
import eduardo5user.rest.config.ParameterStringBuilder;

@Path("home")
public class RestAPI {

	EntityManager entityManager = Emf.getInstance().getFactory().createEntityManager();

	@GET
	public Response home() throws Exception {

		if (caInfo("http://localhost:8080/eduardo5ca/api/home/caExists").equals("true") && raExists()) {

			return Response.status(200).entity("<h1>Bem vindo a ICP Eduardo!</h1>\n" + "<h2>Login:</h2>\n"
					+ "<p>Insira seu numero de identificacao</p>\n"
					+ "<input type=\"number\" placeholder=\"ID\" id=\"id1\"><br>\n" + "<p>Insira sua senha</p>\n"
					+ "<input type=\"password\" placeholder=\"Senha\" id=\"password1\"><br><br>\n"
					+ "<input type=\"button\" onclick=\"location.href='/eduardo5user/api/home/'+document.getElementById('id1').value+'?auth=false&password='+document.getElementById('password1').value\" value=\"Entrar\" /><br>\n"
					+ "<h2>Criar usuario:</h2>\n" + "\n" + ""
					+ "<h4>Para comecar, crie um numero identificador e uma senha</h4>\n"
					+ "<form action=\"/eduardo5user/api/home/create\" enctype=\"application/x-www-form-urlencoded\" method=\"POST\">\n"
					+ "    <input type=\"number\" id=\"id\" name=\"id\" placeholder=\"ID\" maxlength=\"15\"><br><br>\n"
					+ "    <input type=\"password\" id=\"password\" name=\"password\" placeholder=\"Senha\" maxlength=\"15\"><br>\n"
					+ "    <h4>Agora, forneca as informacoes de seu novo certificado</h4>\n"
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
					+ "    <input type=\"submit\" value=\"Criar usuario\"><br>\n" + "</form>").build();
		}
		return Response.status(200).entity("<h3>Erro: Nao existe uma AC e uma AR criadas.</h3>\n").build();
	}

	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Path("create")
	public Response submitUser(@FormParam("password") String password, @FormParam("id") int id,
			@FormParam("c") String c, @FormParam("st") String st, @FormParam("l") String l, @FormParam("o") String o,
			@FormParam("ou") String ou, @FormParam("cn") String cn, @FormParam("keylen") int keylen,
			@FormParam("duration") int duration) throws Exception {

		if (caInfo("http://localhost:8080/eduardo5ca/api/home/caExists").equals("true")) {

			@SuppressWarnings("unchecked")
			List<User> userList = entityManager.createQuery("from User").getResultList();

			for (User user : userList) {
				if (user.getId() == id || getRaID() == id) {
					return Response.status(303).entity("<h2>Erro: Falha na criacao de usuario</h2>\n"
							+ "<h3>O numero de identificacao escolhido nao esta disponivel. Escolha outro.</h3>\n"
							+ "<a href=\"/eduardo5user/api/home\"><button>Voltar</button></a>\n" + "\n" + "")
							.build();
				}
			}

			/*
			 * String csr = csrInfo(id, c, st, l, o, ou, cn, keylen); String pubkey =
			 * getPublicKey(id); String data = signCertificate(csr, pubkey, id, duration);
			 * String creds = encodeCreds(id, password); User user = new User(id, data, cn,
			 * creds);
			 */

			String creds = encodeCreds(id, password);
			User user = new User(id, null, cn, creds);
			createTemplate(id, c, st, l, o, ou, cn, keylen, duration);

			entityManager.getTransaction().begin();
			entityManager.persist(user);
			entityManager.getTransaction().commit();
			entityManager.close();

			// setCertId(id, getCertId(id));

			return Response.status(200)
					.entity("<h3>Usuario criado com sucesso!</h3>\n" + "<a href=\"/eduardo5user/api/home/" + id
							+ "?auth=true\"><button>Ir para a pagina do usuario</button></a><br><br>"
							+ "<a href=\"/eduardo5user/api/home\"><button>Voltar</button></a>\n")
					.build();

		}
		return Response.status(303).entity("<h3>Nao existe nenhuma AC criada. Va para a pagina inicial.</h3>\n"
				+ "<a href=\"/eduardo5user/api/home\"><button>Voltar</button></a>\n" + "\n" + "").build();
	}

	@GET
	@Path("{id}")
	public Response userInterface(@PathParam("id") int id, @QueryParam("auth") boolean auth,
			@QueryParam("password") String password) throws Exception {

		@SuppressWarnings("unchecked")
		List<User> userList = entityManager.createQuery("from User").getResultList();
		User user = new User();
		String creds;
		boolean foundUser = false;
		boolean foundRevoked = false;
		boolean authenticated = false;

		for (User newUser : userList) {
			if (newUser.getId() == id && ((isRevoked(id).equals("false") || isRevoked(id).equals("not created")) || !isApproved(id))) {
				user = newUser;
				foundUser = true;
				break;
			}
			if (newUser.getId() == id && isRevoked(id).equals("true")) {
				user = newUser;
				foundRevoked = true;
			}
		}

		creds = user.getCreds();

		if (auth == true || authenticate(id, password, creds)) {
			authenticated = true;
		}

		if (authenticated) {

			if (foundUser) {

				if (!isApproved(id)) {
					if (isRefused(id)) {
						return Response.status(200).entity("<h2>Pagina de " + user.getUsername()
						+ "</h2><h3>Seu certificado foi reprovado. Verifique seus dados e requisite um novo certificado.</h3>" + "<a href=\"/eduardo5user/api/home\"><button>Voltar</button></a>\n")
						.build();
					}
					return Response.status(200).entity("<h2>Pagina de " + user.getUsername()
							+ "</h2><h3>Seu certificado foi requisitado e esta aguardando aprovacao. Volte mais tarde.</h3>" + "<a href=\"/eduardo5user/api/home\"><button>Voltar</button></a>\n")
							.build();
				}

				String certificate = getCertificate(id);

				String userInfo = user.getData();
				String privateKey = getPrivateKey(id);
				String publicKey = getPublicKey(id);

				Map<String, String> parameters = new HashMap<>();
				parameters.put("certificate", certificate);

				return Response.status(200).entity("<h2>Pagina de " + user.getUsername()
						+ "</h2>\n<p>Dados do usuario: " + userInfo + "</p>\n" + "<p>Certificado: " + certificate
						+ "</p><form action=\"/eduardo6ra/api/home/" + id + "/revoke\" method=\"POST\">\n"
						+ "    <button type=\"submit\">Clique aqui se deseja revogar seu certificado</button>\n"
						+ "</form>" + "<p>Chave Publica: " + publicKey + "</p>\n"
						+ "</p><form action=\"/eduardo5crypto/api/home/" + id
						+ "/publicKey\" method=\"POST\" enctype=\"multipart/form-data\">\n"
						+ "    <button type=\"submit\">Baixar chave publica</button>\n" + "</form>"
						+ "<p>Chave Privada: " + privateKey + "</p>\n" + "</p><form action=\"/eduardo5crypto/api/home/"
						+ id + "/privateKey\" method=\"POST\" enctype=\"multipart/form-data\">\n"
						+ "    <button type=\"submit\">Baixar chave privada</button>\n" + "</form>"
						+ "<h3>Assinatura de documento</h3>\n" + "<form action=\"/eduardo5crypto/api/home/" + id
						+ "/file?" + ParameterStringBuilder.getParamsString(parameters)
						+ "\" method=\"POST\" enctype=\"multipart/form-data\">\n"
						+ "    <label for=\"file\">Escolha um arquivo para ser assinado</label><br><br>\n"
						+ "    <input type=\"file\" name=\"file\" id=\"file\"><br><br><input type=\"submit\" value=\"Assinar\">\n"
						+ "</form><a href=\"/eduardo5user/api/home\"><button>Voltar</button></a>").build();

			}
			
			if (foundRevoked) {

				return Response.status(200).entity("<h2>Criacao de novo certificado</h2>\n"
						+ "<form action=\"/eduardo5user/api/home/" + id
						+ "/update\" method=\"POST\" enctype=\"application/x-www-form-urlencoded\">\n"
						+ "    <label for=\"keylen\">Escolha o tamanho do par de chaves</label><br>\n"
						+ "    <input type=\"number\" name=\"keylen\" id=\"keylen\" min=\"600\" max=\"5000\" placeholder=\"Tamanho\" style=\"margin-top: 10px;\"><br><br>\n"
						+ "	<label for=\"duration\">Qual a duracao do certificado (em dias)?</label><br>\n"
						+ "    <input type=\"number\" name=\"duration\" id=\"duration\" min=\"1\" placeholder=\"Duracao\" style=\"margin-top: 10px;\"><br><br>\n"
						+ "    <input type=\"submit\" value=\"Criar\">\n" + "</form>").build();

			}

		}


		return Response.status(303).entity("<h3>Usuario nao encontrado</h3>\n"
				+ "<a href=\"/eduardo5user/api/home\"><button>Voltar</button></a>\n" + "\n" + "").build();
	}

	@POST
	@Path("{id}/update")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response updateCertificate(@PathParam("id") int id, @FormParam("keylen") int keylen,
			@FormParam("duration") int duration) throws Exception {

		@SuppressWarnings("unchecked")
		List<User> userList = entityManager.createQuery("from User").getResultList();
		boolean foundUser = false;

		for (User newUser : userList) {
			if (newUser.getId() == id && isRevoked(id).equals("true")) {
				foundUser = true;
				break;
			}

		}

		if (foundUser) {
			/*String certificate = getCertificate(id);
			String csr = updateCSR(id, certificate, keylen);
			String pubkey = getPublicKey(id);
			signCertificate(csr, pubkey, id, duration);
			
			setCertId(id, getCertId(id));*/
			
			updateTemplate(id, keylen, duration);
			
			return Response.status(200)
					.entity("<h3>Novo certificado requisitado com sucesso. Aguardando aprovacao.</h3>\n" + "<a href=\"/eduardo5user/api/home/" + id
							+ "?auth=true\"><button>Ir para a pagina do usuario</button></a><br><br>"
							+ "<a href=\"/eduardo5user/api/home\"><button>Voltar</button></a>\n")
					.build();
		}

		return Response.status(200).entity("<h3>Erro: Usuario nao encontrado</h3>").build();

	}
	
	@GET
	@Path("updateData")
	public String updateData(@QueryParam("id") int id, @QueryParam("data") String data) throws Exception {
		@SuppressWarnings("unchecked")
		List<User> userList = entityManager.createQuery("from User").getResultList();
		User theUser = new User();
		boolean foundUser = false;

		for (User user : userList) {
			if (user.getId() == id) {
				theUser = user;
				foundUser = true;
			}
		}
		
		if (foundUser) {
			entityManager.getTransaction().begin();
			theUser.setData(data);
			entityManager.getTransaction().commit();
			entityManager.close();
		}
		
		return "Error";
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
	
	private boolean raExists() throws Exception {
		URL url = new URL("http://localhost:8080/eduardo6ra/api/home/raExists");
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");

		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer content = new StringBuffer();
		while ((inputLine = in.readLine()) != null) {
			content.append(inputLine);
		}
		in.close();
		return Boolean.parseBoolean(content.toString());
	}

	private String isRevoked(int id) throws Exception {
		Map<String, String> parameters = new HashMap<>();
		parameters.put("id", id + "");

		URL url = new URL("http://localhost:8080/eduardo6ra/api/home/revoked?"
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

	private int getRaID() throws Exception {
		URL url = new URL("http://localhost:8080/eduardo6ra/api/home/id");
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");

		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer content = new StringBuffer();
		while ((inputLine = in.readLine()) != null) {
			content.append(inputLine);
		}
		in.close();

		return Integer.parseInt(content.toString());
	}

	private boolean isApproved(int id) throws Exception {
		Map<String, String> parameters = new HashMap<>();
		parameters.put("id", id + "");

		URL url = new URL("http://localhost:8080/eduardo6ra/api/home/approved?"
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
		return Boolean.parseBoolean(content.toString());
	}
	
	private boolean isRefused(int id) throws Exception {
		Map<String, String> parameters = new HashMap<>();
		parameters.put("id", id + "");

		URL url = new URL("http://localhost:8080/eduardo6ra/api/home/refused?"
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
		return Boolean.parseBoolean(content.toString());
	}

	private String createTemplate(int id, String c, String st, String l, String o, String ou, String cn, int keylen, int duration)
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
		parameters.put("duration", duration + "");

		URL url = new URL("http://localhost:8080/eduardo6ra/api/home/template?"
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
	
	private String updateTemplate(int id, int keylen, int duration) throws Exception {
		Map<String, String> parameters = new HashMap<>();
		parameters.put("id", id + "");
		parameters.put("keylen", keylen + "");
		parameters.put("duration", duration + "");

		URL url = new URL("http://localhost:8080/eduardo6ra/api/home/updateTemplate?"
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

	private String encodeCreds(int id, String pass) {
		return Base64.getEncoder().encodeToString((id + "-" + pass).getBytes());
	}

	private Map<Integer, String> decodeCreds(String encodedCreds) {
		Map<Integer, String> map = new HashMap<>();

		String decodedCreds = new String(Base64.getDecoder().decode(encodedCreds));
		System.out.println(decodedCreds);
		int id = Integer.parseInt(decodedCreds.substring(0, decodedCreds.indexOf("-")));
		String password = decodedCreds.substring(decodedCreds.indexOf("-") + 1);
		map.put(id, password);
		return map;

	}

	private boolean authenticate(int id, String password, String creds) {
		Map<Integer, String> map = decodeCreds(creds);

		return map.get(id).equals(password);
	}
	

}
