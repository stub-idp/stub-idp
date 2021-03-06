package stubsp.stubsp.saml.response.eidas;

import org.opensaml.saml.saml2.core.Assertion;
import stubidp.saml.security.validators.ValidatedResponse;

import java.util.Objects;

public class EidasAuthnResponseIssuerValidator {
    public void validate(ValidatedResponse validatedResponse, Assertion validatedIdentityAssertion) {
        if ( !Objects.equals(validatedResponse.getIssuer().getFormat(), validatedIdentityAssertion.getIssuer().getFormat()) ) {
            throw EidasProfileValidationSpecification.authnResponseAssertionIssuerFormatMismatch(
                    validatedResponse.getIssuer().getFormat(),
                    validatedIdentityAssertion.getIssuer().getFormat()
            );
        }

        if ( !Objects.equals(validatedResponse.getIssuer().getValue(), validatedIdentityAssertion.getIssuer().getValue()) ) {
            throw EidasProfileValidationSpecification.authnResponseAssertionIssuerValueMismatch(
                    validatedResponse.getIssuer().getValue(),
                    validatedIdentityAssertion.getIssuer().getValue()
            );
        }
    }
}
