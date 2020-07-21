package eduardo5crypto.cryptography.keys;


import java.security.*;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

public class KeyGen {

    private KeyPairGenerator generator;

    /**
     * Construtor.
     *
     * @param algoritmo algoritmo de criptografia assimétrica a ser usado.
     */
    public KeyGen(String algorithm) {
        try {
            generator = KeyPairGenerator.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }


    public KeyPair generateKeyPair(int keylen) {
        generator.initialize(keylen);
        return generator.generateKeyPair();
    }
    
    public static PrivateKey getPrivateKeyFromString(String keyString) throws Exception {
    	KeySpec spec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(keyString));
		KeyFactory kf = KeyFactory.getInstance("rsa");
		return kf.generatePrivate(spec);
		
    }

}

