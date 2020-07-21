package eduardo5crypto.cryptography.certificates;

import java.io.ByteArrayInputStream;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.RFC4519Style;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;


public class CSRGen {

	private static PublicKey publicKey;
	private static PrivateKey privateKey;

	public CSRGen(KeyPair keypair) {
		publicKey = keypair.getPublic();
		privateKey = keypair.getPrivate();
	}

	public String getCSR(String c, String st, String l, String o, String ou, String cn) throws Exception {
		byte[] csr = generatePKCS10(c, st, l, o, ou, cn);
		return Base64.getEncoder().encodeToString(csr);
	}

	private byte[] generatePKCS10(String c, String st, String l, String o, String ou, String cn) throws Exception {
		String sigAlg = "SHA256withRSA";

		X500Name x500name = createStdBuilder(c, st, l, o, ou, cn).build();

		PKCS10CertificationRequestBuilder builder = new JcaPKCS10CertificationRequestBuilder(x500name, publicKey);
		JcaContentSignerBuilder csBuilder = new JcaContentSignerBuilder(sigAlg);
		ContentSigner signer = csBuilder.build(privateKey);
		PKCS10CertificationRequest csr = builder.build(signer);
		return csr.getEncoded();
	}

	private X500NameBuilder createStdBuilder(String c, String st, String l, String o, String ou, String cn) {
		X500NameBuilder builder = new X500NameBuilder(RFC4519Style.INSTANCE);

		builder.addRDN(RFC4519Style.c, c);
		builder.addRDN(RFC4519Style.o, o);
		builder.addRDN(RFC4519Style.ou, ou);
		builder.addRDN(RFC4519Style.l, l);
		builder.addRDN(RFC4519Style.st, st);
		builder.addRDN(RFC4519Style.cn, cn);

		return builder;
	}
	
	public static X509Certificate getCertificateFromString(String certString) throws Exception {
		ByteArrayInputStream bIn = new ByteArrayInputStream(Base64.getDecoder().decode(certString));
		CertificateFactory fact = CertificateFactory.getInstance("X.509", new BouncyCastleProvider());
		return (X509Certificate) fact.generateCertificate(bIn);
	}

}
