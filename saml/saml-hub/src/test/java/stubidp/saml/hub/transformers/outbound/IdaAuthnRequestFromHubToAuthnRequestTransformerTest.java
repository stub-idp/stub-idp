package stubidp.saml.hub.transformers.outbound;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnContextComparisonTypeEnumeration;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.NameIDPolicy;
import org.opensaml.saml.saml2.core.NameIDType;
import org.opensaml.saml.saml2.core.RequestedAuthnContext;
import stubidp.saml.domain.assertions.AuthnContext;
import stubidp.saml.domain.request.IdaAuthnRequestFromHub;
import stubidp.saml.extensions.extensions.IdaAuthnContext;
import stubidp.saml.test.OpenSAMLRunner;
import stubidp.saml.utils.core.OpenSamlXmlObjectFactory;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static stubidp.saml.test.builders.IdaAuthnRequestBuilder.anIdaAuthnRequest;

class IdaAuthnRequestFromHubToAuthnRequestTransformerTest extends OpenSAMLRunner {

    private IdaAuthnRequestFromHubToAuthnRequestTransformer transformer;

    @BeforeEach
    void setup() {
        transformer = new IdaAuthnRequestFromHubToAuthnRequestTransformer(new OpenSamlXmlObjectFactory());
    }

    @Test
    void shouldUseTheOriginalRequestIdForTheTransformedRequest() {
        String originalRequestId = UUID.randomUUID().toString();
        IdaAuthnRequestFromHub originalRequestFromHub = anIdaAuthnRequest().withId(originalRequestId).buildFromHub();

        AuthnRequest transformedRequest = transformer.apply(originalRequestFromHub);

        assertThat(transformedRequest.getID()).isEqualTo(originalRequestId);
    }

    @Test
    void shouldUseTheOriginalExpiryTimestampToSetTheNotOnOrAfter() {
        Instant sessionExpiry = Instant.now().atZone(ZoneId.of("UTC")).plusHours(2).toInstant();
        IdaAuthnRequestFromHub originalRequestFromHub = anIdaAuthnRequest().withSessionExpiryTimestamp(sessionExpiry).buildFromHub();

        AuthnRequest transformedRequest = transformer.apply(originalRequestFromHub);
        assertThat(transformedRequest.getConditions().getNotOnOrAfter()).isEqualTo(sessionExpiry);
    }

    @Test
    void shouldUseTheOriginalRequestIssuerIdForTheTransformedRequest() {
        String originalIssuerId = UUID.randomUUID().toString();
        IdaAuthnRequestFromHub originalRequestFromHub = anIdaAuthnRequest().withIssuer(originalIssuerId).buildFromHub();

        AuthnRequest transformedRequest = transformer.apply(originalRequestFromHub);

        assertThat(transformedRequest.getIssuer().getValue()).isEqualTo(originalIssuerId);
    }

    @Test
    void shouldCreateAProxyElementWithAProxyCountOfZeroInTheTransformedRequest() {
        AuthnRequest transformedRequest = transformer.apply(anIdaAuthnRequest().buildFromHub());

        assertThat(transformedRequest.getScoping().getProxyCount()).isEqualTo(0);
    }

    @Test
    void shouldCreateANameIdPolicyElementWithAFormatOfPersistentInTheTransformedRequest() {
        AuthnRequest transformedRequest = transformer.apply(anIdaAuthnRequest().buildFromHub());

        assertThat(transformedRequest.getNameIDPolicy().getFormat()).isEqualTo(NameIDType.PERSISTENT);
    }
    
    @Test
    void shouldCorrectlyMapLevelsOfAssurance() {
        List<AuthnContext> levelsOfAssurance = Arrays.asList(AuthnContext.LEVEL_1, AuthnContext.LEVEL_2);
        List<String> expected = Arrays.asList(IdaAuthnContext.LEVEL_1_AUTHN_CTX, IdaAuthnContext.LEVEL_2_AUTHN_CTX);


        IdaAuthnRequestFromHub originalRequestFromHub = anIdaAuthnRequest()
                .withLevelsOfAssurance(levelsOfAssurance).buildFromHub();
        AuthnRequest transformedRequest = transformer.apply(originalRequestFromHub);
        RequestedAuthnContext requestedAuthnContext = transformedRequest.getRequestedAuthnContext();

        List<String> actual = requestedAuthnContext.getAuthnContextClassRefs().stream()
                .map(AuthnContextClassRef::getURI)
                .collect(Collectors.toList());

        assertThat(actual).containsAll(expected);
    }

    @Test
    void shouldPropagateComparisonType() {
        IdaAuthnRequestFromHub originalRequestFromHub = anIdaAuthnRequest()
                .withComparisonType(AuthnContextComparisonTypeEnumeration.MINIMUM)
                .buildFromHub();
        AuthnRequest transformedRequest = transformer.apply(originalRequestFromHub);

        RequestedAuthnContext requestedAuthnContext = transformedRequest.getRequestedAuthnContext();

        assertThat(requestedAuthnContext.getComparison()).isEqualTo(AuthnContextComparisonTypeEnumeration.MINIMUM);
    }

    @Test
    void shouldMaintainTheAuthnContextsInPreferenceOrder() {
        IdaAuthnRequestFromHub originalRequestFromHub = anIdaAuthnRequest()
                .withLevelsOfAssurance(Arrays.asList(AuthnContext.LEVEL_1, AuthnContext.LEVEL_2))
                .buildFromHub();
        AuthnRequest transformedRequest = transformer.apply(originalRequestFromHub);

        RequestedAuthnContext requestedAuthnContext = transformedRequest.getRequestedAuthnContext();

        List<AuthnContextClassRef> authnContextClassRefs = requestedAuthnContext.getAuthnContextClassRefs();
        List<String> authnContexts = authnContextClassRefs.stream()
                .map(AuthnContextClassRef::getURI).collect(Collectors.toList());

        assertThat(authnContexts).containsSequence(IdaAuthnContext.LEVEL_1_AUTHN_CTX, IdaAuthnContext.LEVEL_2_AUTHN_CTX);
    }

    @Test
    void shouldSetAllowCreateToTrue() {
        IdaAuthnRequestFromHub originalRequestFromHub = anIdaAuthnRequest().buildFromHub();
        AuthnRequest transformedRequest = transformer.apply(originalRequestFromHub);

        NameIDPolicy nameIDPolicy = transformedRequest.getNameIDPolicy();

        assertThat(nameIDPolicy.getAllowCreate()).isEqualTo(true);
    }

    @Test
    void shouldSetForceAuthnToTrue() {
        IdaAuthnRequestFromHub originalRequestFromTransaction = anIdaAuthnRequest()
                .withForceAuthentication(Optional.of(true))
                .buildFromHub();

        AuthnRequest transformedRequest = transformer.apply(originalRequestFromTransaction);

        assertThat(transformedRequest.isForceAuthn()).isEqualTo(true);

    }

    @Test
    void shouldSetForceAuthnToFalse() {
        IdaAuthnRequestFromHub originalRequestFromTransaction = anIdaAuthnRequest()
                .withForceAuthentication(Optional.of(false))
                .buildFromHub();

        AuthnRequest transformedRequest = transformer.apply(originalRequestFromTransaction);

        assertThat(transformedRequest.isForceAuthn()).isEqualTo(false);

        originalRequestFromTransaction = anIdaAuthnRequest()
                .withForceAuthentication(Optional.empty())
                .buildFromHub();

        transformedRequest = transformer.apply(originalRequestFromTransaction);

        assertThat(transformedRequest.isForceAuthn()).isEqualTo(false);

    }

    @Test
    void shouldSetProtocolBindingToPost() {
        IdaAuthnRequestFromHub originalRequestFromTransaction = anIdaAuthnRequest()
            .buildFromHub();

        AuthnRequest transformedRequest = transformer.apply(originalRequestFromTransaction);

        assertThat(transformedRequest.getProtocolBinding()).isEqualTo(SAMLConstants.SAML2_POST_BINDING_URI);
    }
}
