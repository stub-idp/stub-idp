package stubidp.saml.hub.transformers.inbound;

import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import stubidp.saml.hub.core.validators.DestinationValidator;
import stubidp.saml.hub.domain.AuthnRequestFromRelyingParty;
import stubidp.saml.hub.validators.authnrequest.AuthnRequestFromTransactionValidator;
import stubidp.saml.security.validators.signature.SamlRequestSignatureValidator;

import java.util.function.Function;

public class AuthnRequestToIdaRequestFromRelyingPartyTransformer implements Function<AuthnRequest, AuthnRequestFromRelyingParty> {

    private final AuthnRequestFromRelyingPartyUnmarshaller authnRequestFromRelyingPartyUnmarshaller;
    private final SamlRequestSignatureValidator<AuthnRequest> samlRequestSignatureValidator;
    private final DestinationValidator authnRequestDestinationValidator;
    private final AuthnRequestFromTransactionValidator authnRequestFromTransactionValidator;

    public AuthnRequestToIdaRequestFromRelyingPartyTransformer(
        AuthnRequestFromRelyingPartyUnmarshaller authnRequestFromRelyingPartyUnmarshaller,
        SamlRequestSignatureValidator<AuthnRequest> samlRequestSignatureValidator,
        DestinationValidator authnRequestDestinationValidator,
        AuthnRequestFromTransactionValidator authnRequestFromTransactionValidator
    ) {
        this.authnRequestFromRelyingPartyUnmarshaller = authnRequestFromRelyingPartyUnmarshaller;
        this.samlRequestSignatureValidator = samlRequestSignatureValidator;
        this.authnRequestDestinationValidator = authnRequestDestinationValidator;
        this.authnRequestFromTransactionValidator = authnRequestFromTransactionValidator;
    }

    @Override
    public AuthnRequestFromRelyingParty apply(final AuthnRequest authnRequest) {
        authnRequestFromTransactionValidator.validate(authnRequest);
        authnRequestDestinationValidator.validate(authnRequest.getDestination());
        samlRequestSignatureValidator.validate(authnRequest, SPSSODescriptor.DEFAULT_ELEMENT_NAME);
        return authnRequestFromRelyingPartyUnmarshaller.fromSamlMessage(authnRequest);
    }
}
