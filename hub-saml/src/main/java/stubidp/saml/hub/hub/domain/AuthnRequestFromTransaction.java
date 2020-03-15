package stubidp.saml.hub.hub.domain;

import org.opensaml.xmlsec.signature.Signature;
import stubidp.saml.utils.core.domain.IdaSamlMessage;
import stubidp.utils.security.security.IdGenerator;

import java.net.URI;
import java.time.Instant;
import java.util.Optional;

public class AuthnRequestFromTransaction extends IdaSamlMessage {
    private Optional<Boolean> forceAuthentication;
    private Optional<URI> assertionConsumerServiceUrl;
    private Optional<Integer> assertionConsumerServiceIndex;
    private Optional<Signature> signature;

    protected AuthnRequestFromTransaction() {
    }

    public AuthnRequestFromTransaction(
            String id,
            String issuer,
            Instant issueInstant,
            Optional<Boolean> forceAuthentication,
            Optional<URI> assertionConsumerServiceUrl,
            Optional<Integer> assertionConsumerServiceIndex,
            Optional<Signature> signature,
            URI destination) {

        super(id, issuer, issueInstant, destination);

        this.forceAuthentication = forceAuthentication;
        this.assertionConsumerServiceUrl = assertionConsumerServiceUrl;
        this.assertionConsumerServiceIndex = assertionConsumerServiceIndex;
        this.signature = signature;
    }

    public static AuthnRequestFromTransaction createRequestReceivedFromTransaction(
        String id,
        String issuerId,
        Instant issueInstant,
        boolean forceAuthentication,
        Optional<URI> assertionConsumerServiceUrl,
        Optional<Integer> assertionConsumerServiceIndex,
        Optional<Signature> signature,
        URI destination) {

        return new AuthnRequestFromTransaction(
                id,
                issuerId,
                issueInstant,
                Optional.of(forceAuthentication),
                assertionConsumerServiceUrl,
                assertionConsumerServiceIndex,
                signature,
                destination);
    }

    // NOTE: this method is only used for the fake relying parties we use
    public static AuthnRequestFromTransaction createRequestToSendToHub(
        String issuerId,
        boolean forceAuthentication,
        Optional<URI> assertionConsumerServiceUrl,
        Optional<Integer> assertionConsumerServiceIndex,
        Optional<Signature> signature,
        URI destination) {

        return createRequestReceivedFromTransaction(
                new IdGenerator().getId(),
                issuerId,
                Instant.now(),
                forceAuthentication,
                assertionConsumerServiceUrl,
                assertionConsumerServiceIndex,
                signature,
                destination);
    }

    public Optional<Boolean> getForceAuthentication() {
        return forceAuthentication;
    }

    public Optional<Integer> getAssertionConsumerServiceIndex() {
        return assertionConsumerServiceIndex;
    }

    public Optional<Signature> getSignature() {
        return signature;
    }

    public Optional<URI> getAssertionConsumerServiceUrl() {
        return assertionConsumerServiceUrl;
    }
}
