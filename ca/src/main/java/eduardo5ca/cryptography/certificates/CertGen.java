package eduardo5ca.cryptography.certificates;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.RFC4519Style;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.bc.BcRSAContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;

import eduardo5ca.cryptography.keys.KeyGen;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Date;

public class CertGen {

	private String c, st, l, o, ou, cn;

	public CertGen(String c, String st, String l, String o, String ou, String cn) {
		setC(c);
		setSt(st);
		setL(l);
		setO(o);
		setOu(ou);
		setCn(cn);
	}

	public X509Certificate generate(PrivateKey privKey, PublicKey pubKey, int duration, String signAlg,
			boolean selfsigned, String caB64, int serial) throws Exception {
		Provider BC = new BouncyCastleProvider();

		// distinguished name table.
		X500Name subject = createStdBuilder().build();
		X500Name issuer;

		if (selfsigned) {
			issuer = subject;
		} else {
			issuer = new JcaX509CertificateHolder(getCertificateFromString(caB64)).getSubject();
		}

		// create the certificate
		ContentSigner sigGen = new JcaContentSignerBuilder(signAlg).build(privKey);
		X509v3CertificateBuilder certGen = new JcaX509v3CertificateBuilder(issuer, // Issuer
				BigInteger.valueOf(serial), // Serial
				new Date(System.currentTimeMillis() - 50000), // Valid from
				new Date((long) (System.currentTimeMillis() + duration * 8.65 * Math.pow(10, 7))), // Valid to
				subject, // Subject
				pubKey // Publickey to be associated with the certificate
		);

		return new JcaX509CertificateConverter().setProvider(BC).getCertificate(certGen.build(sigGen));

	}

	private X500NameBuilder createStdBuilder() {
		X500NameBuilder builder = new X500NameBuilder(RFC4519Style.INSTANCE);

		builder.addRDN(RFC4519Style.c, c);
		builder.addRDN(RFC4519Style.o, o);
		builder.addRDN(RFC4519Style.ou, ou);
		builder.addRDN(RFC4519Style.l, l);
		builder.addRDN(RFC4519Style.st, st);
		builder.addRDN(RFC4519Style.cn, cn);

		return builder;
	}

	public static String sign(String inputCSR, String caPrivate, String caB64, String pubkey, int id, int duration)
			throws Exception {

		AlgorithmIdentifier sigAlgId = new DefaultSignatureAlgorithmIdentifierFinder().find("SHA1withRSA");
		AlgorithmIdentifier digAlgId = new DefaultDigestAlgorithmIdentifierFinder().find(sigAlgId);

		X500Name issuer = new JcaX509CertificateHolder(getCertificateFromString(caB64)).getSubject();

		AsymmetricKeyParameter foo = PrivateKeyFactory
				.createKey(KeyGen.getPrivateKeyFromString(caPrivate).getEncoded());
		SubjectPublicKeyInfo keyInfo = SubjectPublicKeyInfo
				.getInstance(KeyGen.getPublicKeyFromString(pubkey).getEncoded());

		PKCS10CertificationRequest pk10Holder = new PKCS10CertificationRequest(Base64.getDecoder().decode(inputCSR));

		X509v3CertificateBuilder myCertificateGenerator = new X509v3CertificateBuilder(issuer, BigInteger.valueOf(id),
				new Date(System.currentTimeMillis() - 50000),
				new Date((long) (System.currentTimeMillis() + duration * 8.65 * Math.pow(10, 7))),
				pk10Holder.getSubject(), keyInfo);

		ContentSigner sigGen = new BcRSAContentSignerBuilder(sigAlgId, digAlgId).build(foo);

		X509CertificateHolder holder = myCertificateGenerator.build(sigGen);
		org.bouncycastle.asn1.x509.Certificate eeX509CertificateStructure = holder.toASN1Structure();

		CertificateFactory cf = CertificateFactory.getInstance("X.509", "BC");

		// Read Certificate
		InputStream is1 = new ByteArrayInputStream(eeX509CertificateStructure.getEncoded());
		X509Certificate theCert = (X509Certificate) cf.generateCertificate(is1);
		is1.close();

		return Base64.getEncoder().encodeToString(theCert.getEncoded());
	}

	public static X509Certificate getCertificateFromString(String certString) throws Exception {
		ByteArrayInputStream bIn = new ByteArrayInputStream(Base64.getDecoder().decode(certString));
		CertificateFactory fact = CertificateFactory.getInstance("X.509", new BouncyCastleProvider());
		return (X509Certificate) fact.generateCertificate(bIn);
	}

	public String getC() {
		return c;
	}

	public void setC(String c) {
		this.c = c;
	}

	public String getSt() {
		return st;
	}

	public void setSt(String st) {
		this.st = st;
	}

	public String getL() {
		return l;
	}

	public void setL(String l) {
		this.l = l;
	}

	public String getO() {
		return o;
	}

	public void setO(String o) {
		this.o = o;
	}

	public String getOu() {
		return ou;
	}

	public void setOu(String ou) {
		this.ou = ou;
	}

	public String getCn() {
		return cn;
	}

	public void setCn(String cn) {
		this.cn = cn;
	}

}
