package stubidp.saml.security;

import java.security.PublicKey;
import java.util.List;

public interface SigningKeyStore {
    List<PublicKey> getVerifyingKeysForEntity(String entityId);
}
