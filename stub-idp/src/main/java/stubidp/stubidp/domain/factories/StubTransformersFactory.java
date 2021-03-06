package stubidp.stubidp.domain.factories;

import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.xmlsec.algorithm.DigestAlgorithm;
import org.opensaml.xmlsec.algorithm.SignatureAlgorithm;
import stubidp.saml.utils.core.OpenSamlXmlObjectFactory;
import stubidp.saml.utils.core.api.CoreTransformersFactory;
import stubidp.saml.utils.core.transformers.outbound.OutboundAssertionToSubjectTransformer;
import stubidp.saml.utils.core.transformers.outbound.ResponseToSignedStringTransformer;
import stubidp.saml.serializers.deserializers.StringToOpenSamlObjectTransformer;
import stubidp.saml.domain.request.IdaAuthnRequestFromHub;
import stubidp.saml.utils.hub.factories.AttributeFactory_1_1;
import stubidp.saml.utils.hub.transformers.inbound.decorators.AuthnRequestSizeValidator;
import stubidp.saml.utils.hub.validators.StringSizeValidator;
import stubidp.stubidp.saml.transformers.inbound.AuthnRequestToIdaRequestFromHubTransformer;
import stubidp.stubidp.saml.transformers.inbound.IdaAuthnRequestFromHubUnmarshaller;
import stubidp.stubidp.saml.transformers.outbound.IdentityProviderAssertionToAssertionTransformer;
import stubidp.stubidp.saml.transformers.outbound.IdentityProviderAuthnStatementToAuthnStatementTransformer;
import stubidp.saml.security.EncryptionKeyStore;
import stubidp.saml.security.EntityToEncryptForLocator;
import stubidp.saml.security.IdaKeyStore;
import stubidp.saml.security.SigningKeyStore;
import stubidp.stubidp.domain.IdpIdaStatusMarshaller;
import stubidp.saml.domain.response.OutboundResponseFromIdp;
import stubidp.stubidp.saml.transformers.outbound.OutboundResponseFromIdpToSamlResponseTransformer;

import java.util.function.Function;

public class StubTransformersFactory {

    private static final CoreTransformersFactory coreTransformersFactory = new CoreTransformersFactory();
    private static final OpenSamlXmlObjectFactory openSamlXmlObjectFactory = new OpenSamlXmlObjectFactory();

    public StubTransformersFactory() {}

    public Function<String, IdaAuthnRequestFromHub> getStringToIdaAuthnRequestFromHub(
            final SigningKeyStore signingKeyStore){
        AuthnRequestSizeValidator sizeValidator = new AuthnRequestSizeValidator(new StringSizeValidator());

        StringToOpenSamlObjectTransformer<AuthnRequest> stringtoOpenSamlObjectTransformer = coreTransformersFactory.getStringtoOpenSamlObjectTransformer(sizeValidator);

        return getAuthnRequestToIdaRequestFromHubTransformer(signingKeyStore).compose(stringtoOpenSamlObjectTransformer);
    }

    public Function<String, AuthnRequest> getStringToAuthnRequest() {
        AuthnRequestSizeValidator sizeValidator = new AuthnRequestSizeValidator(new StringSizeValidator());

        return coreTransformersFactory.getStringtoOpenSamlObjectTransformer(sizeValidator);
    }

    public AuthnRequestToIdaRequestFromHubTransformer getAuthnRequestToIdaRequestFromHubTransformer(SigningKeyStore signingKeyStore) {
        return new AuthnRequestToIdaRequestFromHubTransformer(
                new IdaAuthnRequestFromHubUnmarshaller(),
                coreTransformersFactory.getSamlRequestSignatureValidator(signingKeyStore)
        );
    }

    public Function<OutboundResponseFromIdp, String> getOutboundResponseFromIdpToStringTransformer(
            final EncryptionKeyStore publicKeyStore,
            final IdaKeyStore keyStore,
            EntityToEncryptForLocator entityToEncryptForLocator,
            String publicSigningKey,
            String issuerId,
            SignatureAlgorithm signatureAlgorithm,
            DigestAlgorithm digestAlgorithm
    ){

        return coreTransformersFactory.getResponseStringTransformer(
                publicKeyStore,
                keyStore,
                entityToEncryptForLocator,
                publicSigningKey,
                issuerId,
                signatureAlgorithm,
                digestAlgorithm).compose(getOutboundResponseFromIdpToSamlResponseTransformer());
    }

    public Function<OutboundResponseFromIdp, String> getOutboundResponseFromIdpToStringTransformer(
            final EncryptionKeyStore encryptionKeyStore,
            final IdaKeyStore keyStore,
            EntityToEncryptForLocator entityToEncryptForLocator,
            SignatureAlgorithm signatureAlgorithm,
            DigestAlgorithm digestAlgorithm
    ){
        ResponseToSignedStringTransformer responseStringTransformer = coreTransformersFactory.getResponseStringTransformer(
                encryptionKeyStore,
                keyStore,
                entityToEncryptForLocator,
                signatureAlgorithm,
                digestAlgorithm);

        return responseStringTransformer.compose(getOutboundResponseFromIdpToSamlResponseTransformer());
    }

    public OutboundResponseFromIdpToSamlResponseTransformer getOutboundResponseFromIdpToSamlResponseTransformer() {
        return new OutboundResponseFromIdpToSamlResponseTransformer(
                new IdpIdaStatusMarshaller(openSamlXmlObjectFactory),
                openSamlXmlObjectFactory,
                getIdpAssertionToAssertionTransformer()
        );
    }

    private IdentityProviderAssertionToAssertionTransformer getIdpAssertionToAssertionTransformer() {
        return new IdentityProviderAssertionToAssertionTransformer(
                openSamlXmlObjectFactory,
                new AttributeFactory_1_1(openSamlXmlObjectFactory),
                new IdentityProviderAuthnStatementToAuthnStatementTransformer(openSamlXmlObjectFactory),
                new OutboundAssertionToSubjectTransformer(openSamlXmlObjectFactory)
        );
    }
}
