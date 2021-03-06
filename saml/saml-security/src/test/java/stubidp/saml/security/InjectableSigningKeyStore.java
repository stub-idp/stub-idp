package stubidp.saml.security;

import stubidp.utils.security.security.PublicKeyFactory;
import stubidp.utils.security.security.X509CertificateFactory;

import java.security.PublicKey;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class InjectableSigningKeyStore implements SigningKeyStore {

    private final Map<String, List<String>> publicSigningKeys;
    private final PublicKeyFactory publicKeyFactory = new PublicKeyFactory(new X509CertificateFactory());

    public InjectableSigningKeyStore(Map<String, List<String>> publicSigningKeys) {
        this.publicSigningKeys = publicSigningKeys;
    }

    @Override
    public List<PublicKey> getVerifyingKeysForEntity(String entityId) {
        List<String> certs = publicSigningKeys.get(entityId);
        return certs.stream().map(publicKeyFactory::createPublicKey).collect(Collectors.toList());
    }
}
