package stubidp.utils.security.security;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;

public class PrivateKeyFactory {

    public PrivateKeyFactory() {
    }

    public PrivateKey createPrivateKey(byte[] privateKeyBytes) {
        KeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);

        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException rsaE) {
            try {
                KeyFactory keyFactory = KeyFactory.getInstance("EC");
                return keyFactory.generatePrivate(keySpec);
            } catch (NoSuchAlgorithmException | InvalidKeySpecException ecE) {
                throw new RuntimeException(rsaE);
            }
        }

    }
}
