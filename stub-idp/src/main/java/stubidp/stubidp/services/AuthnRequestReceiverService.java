package stubidp.stubidp.services;

import io.prometheus.client.Counter;
import org.apache.commons.lang.StringEscapeUtils;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stubidp.stubidp.domain.EidasAuthnRequest;
import stubidp.stubidp.domain.IdpHint;
import stubidp.stubidp.domain.IdpLanguageHint;
import stubidp.stubidp.exceptions.InvalidEidasAuthnRequestException;
import stubidp.stubidp.repositories.EidasSession;
import stubidp.stubidp.repositories.EidasSessionRepository;
import stubidp.stubidp.repositories.IdpSession;
import stubidp.stubidp.repositories.IdpSessionRepository;
import stubidp.utils.rest.common.SessionId;
import stubidp.saml.utils.hub.domain.IdaAuthnRequestFromHub;
import stubidp.stubidp.Urls;

import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

public class AuthnRequestReceiverService {

    private static final Logger LOG = LoggerFactory.getLogger(AuthnRequestReceiverService.class);

    private static final Counter successfulVerifyAuthnRequests = Counter.build()
            .name("stubidp_verify_successfulAuthnRequests_total")
            .help("Number of successful verify authn requests.")
            .register();
    private static final Counter successfulEidasAuthnRequests = Counter.build()
            .name("stubidp_eidas_successfulAuthnRequests_total")
            .help("Number of successful eidas authn requests.")
            .register();

    private final Function<String, IdaAuthnRequestFromHub> samlRequestTransformer;
    private final IdpSessionRepository idpSessionRepository;
    private final EidasSessionRepository eidasSessionRepository;
    private final Function<String, AuthnRequest> stringAuthnRequestTransformer;

    public static class SessionCreated {
        private URI nextLocation;
        private SessionId idpSessionId;

        public SessionCreated(URI nextLocation, SessionId idpSessionId) {
            this.nextLocation = nextLocation;
            this.idpSessionId = idpSessionId;
        }

        public SessionId getIdpSessionId() {
            return idpSessionId;
        }

        public URI getNextLocation() {
            return nextLocation;
        }
    }

    @Inject
    public AuthnRequestReceiverService(
            Function<String, IdaAuthnRequestFromHub> samlRequestTransformer,
            IdpSessionRepository idpSessionRepository,
            EidasSessionRepository eidasSessionRepository,
            Function<String, AuthnRequest> stringToAuthnRequestTransformer) {

        this.samlRequestTransformer = samlRequestTransformer;
        this.idpSessionRepository = idpSessionRepository;
        this.eidasSessionRepository = eidasSessionRepository;
        this.stringAuthnRequestTransformer = stringToAuthnRequestTransformer;
    }

    public SessionCreated handleAuthnRequest(String idpName, String samlRequest, Set<String> idpHints,
                                             Optional<Boolean> registration, String relayState,
                                             Optional<IdpLanguageHint> languageHint, Optional<UUID> singleIdpJourneyId) {
        final List<IdpHint> validHints = new ArrayList<>();
        final List<String> invalidHints = new ArrayList<>();
        validateHints(idpHints, validHints, invalidHints);

        final IdaAuthnRequestFromHub idaRequestFromHub = samlRequestTransformer.apply(samlRequest);
        successfulVerifyAuthnRequests.inc();
        IdpSession session = new IdpSession(SessionId.createNewSessionId(), idaRequestFromHub, relayState, validHints, invalidHints, languageHint, registration, singleIdpJourneyId, null);
        final SessionId idpSessionId = idpSessionRepository.createSession(session);

        UriBuilder uriBuilder;
        if (registration.isPresent() && registration.get()) {
            uriBuilder = UriBuilder.fromPath(Urls.IDP_REGISTER_RESOURCE);
        } else {
            uriBuilder = UriBuilder.fromPath(Urls.IDP_LOGIN_RESOURCE);
        }

        return new SessionCreated(uriBuilder.build(idpName), idpSessionId);
    }

    public SessionCreated handleEidasAuthnRequest(String schemeId, String samlRequest, String relayState, Optional<IdpLanguageHint> languageHint) {
        AuthnRequest authnRequest = stringAuthnRequestTransformer.apply(samlRequest);
        validateEidasAuthnRequest(authnRequest);
        EidasAuthnRequest eidasAuthnRequest = EidasAuthnRequest.buildFromAuthnRequest(authnRequest);
        successfulEidasAuthnRequests.inc();
        EidasSession session = new EidasSession(SessionId.createNewSessionId(), eidasAuthnRequest, relayState, Collections.emptyList(), Collections.emptyList(), languageHint, Optional.empty(), null);
        final SessionId idpSessionId = eidasSessionRepository.createSession(session);

        UriBuilder uriBuilder = UriBuilder.fromPath(Urls.EIDAS_LOGIN_RESOURCE);

        return new SessionCreated(uriBuilder.build(schemeId), idpSessionId);
    }

    private void validateEidasAuthnRequest(AuthnRequest request) {
        if (request.getSignature().getKeyInfo() == null) {
            throw new InvalidEidasAuthnRequestException("KeyInfo cannot be null");
        }
        if (request.getSignature().getKeyInfo().getX509Datas().isEmpty()) {
            throw new InvalidEidasAuthnRequestException("Must contain X509 data");
        }
        if (request.getSignature().getKeyInfo().getX509Datas().get(0).getX509Certificates().isEmpty()) {
            throw new InvalidEidasAuthnRequestException("Must contain X509 certificate");
        }
    }

    private void validateHints(Set<String> idpHints, List<IdpHint> validHints, List<String> invalidHints) {
        if (idpHints != null && !idpHints.isEmpty()) {
            for (String hint : idpHints) {
                try {
                    validHints.add(IdpHint.valueOf(hint));
                } catch (IllegalArgumentException e) {
                    // this is a hint that stub-idp does not know about, and it should be able
                    // to deal with such hints.  Also sanitize string
                    invalidHints.add(StringEscapeUtils.escapeHtml(hint));
                }
            }
            if (!validHints.isEmpty()) {
                LOG.info("Received known hints: {}", validHints);
            }
            if (!invalidHints.isEmpty()) {
                LOG.info("Received unknown hints: {}", invalidHints);
            }
        }
    }
}
