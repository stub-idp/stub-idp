package stubidp.utils.security.security;

import stubidp.utils.security.configuration.PrivateEncryptionKeys;
import stubidp.utils.security.configuration.PrivateSigningKey;

import java.security.PrivateKey;
import java.util.List;

@SuppressWarnings("unused")
public class PrivateKeyStore {
    private final PrivateKey signingPrivateKey;
    private final List<PrivateKey> encryptionPrivateKeys;

    public PrivateKeyStore(
            @PrivateSigningKey PrivateKey signingPrivateKey,
            @PrivateEncryptionKeys List<PrivateKey> encryptionPrivateKeys) {
        this.signingPrivateKey = signingPrivateKey;
        this.encryptionPrivateKeys = encryptionPrivateKeys;
    }

    public PrivateKey getSigningPrivateKey() {
        return signingPrivateKey;
    }

    public List<PrivateKey> getEncryptionPrivateKeys() {
        return encryptionPrivateKeys;
    }
}
