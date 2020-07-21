package eduardo5crypto.cryptography.signatures;

import org.bouncycastle.asn1.cms.ContentInfo;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.*;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SignatureGenerator {


    private final String algorithm = "SHA256withRSA";

    private final String provider = "BC";

    private X509Certificate cert;
    private PrivateKey privateKey;

    public SignatureGenerator() {
        Security.addProvider(new BouncyCastleProvider());
    }

    public void informSigner(X509Certificate cert,
                                 PrivateKey privateKey) {
        this.cert = cert;
        this.privateKey = privateKey;
    }

    public CMSSignedData sign(byte[] file) {
        CMSSignedDataGenerator cmsSigGen = new CMSSignedDataGenerator();
        List<X509Certificate> certList = new ArrayList<>();
        certList.add(cert);
        try {
            JcaCertStore certs = new JcaCertStore(certList);
            cmsSigGen.addCertificates(certs);
            cmsSigGen.addSignerInfoGenerator(prepareSignerData(privateKey, cert));
            return cmsSigGen.generate(Objects.requireNonNull(prepareData(file)), true);
        } catch (CertificateEncodingException | CMSException e) {
            e.printStackTrace();
        }

        return null;
    }


    private CMSTypedData prepareData(byte[] file) {
        try {
            Signature signature = Signature.getInstance(algorithm,provider);
            signature.initSign(privateKey);
            signature.update(file);
            return new CMSProcessableByteArray(signature.sign());
        } catch (NoSuchAlgorithmException | NoSuchProviderException | SignatureException | InvalidKeyException e) {
            e.printStackTrace();
        }
        return null;
    }


    private SignerInfoGenerator prepareSignerData(PrivateKey privateKey,
                                                            Certificate cert) {
        try {
            ContentSigner sha256Signer = new JcaContentSignerBuilder(algorithm).setProvider(provider).build(privateKey);
            return new JcaSignerInfoGeneratorBuilder(new JcaDigestCalculatorProviderBuilder().setProvider(provider).build()).build(sha256Signer, (X509Certificate) cert);
        } catch (OperatorCreationException | CertificateEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }


    public void writeSignature(File file, CMSSignedData assinatura) {

        try {
        	ContentInfo cmsSignedDataAsASN1 = assinatura.toASN1Structure();
        	JcaPEMWriter writer = new JcaPEMWriter(new FileWriter(file));
        	writer.writeObject(cmsSignedDataAsASN1);
            writer.flush();
            writer.close();
            
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}

