package stubidp.saml.security;

import java.security.PublicKey;

public interface EncryptionKeyStore {
    PublicKey getEncryptionKeyForEntity(String entityId);
}
