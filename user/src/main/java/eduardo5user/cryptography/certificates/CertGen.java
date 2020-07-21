package eduardo5user.cryptography.certificates;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.ByteArrayInputStream;

import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;

public class CertGen {

	public static X509Certificate getCertificateFromString(String certString) throws Exception {
		ByteArrayInputStream bIn = new ByteArrayInputStream(Base64.getDecoder().decode(certString));
		CertificateFactory fact = CertificateFactory.getInstance("X.509", new BouncyCastleProvider());
		return (X509Certificate) fact.generateCertificate(bIn);
	}

}
