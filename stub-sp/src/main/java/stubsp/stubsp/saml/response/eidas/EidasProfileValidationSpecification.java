package stubsp.stubsp.saml.response.eidas;

import org.slf4j.event.Level;
import stubidp.saml.extensions.validation.SamlTransformationErrorException;

import java.text.MessageFormat;

public class EidasProfileValidationSpecification {

    public static SamlTransformationErrorException authnResponseAssertionIssuerFormatMismatch(String responseIssuerFormat, String assertionIssuerFormat) {
        return new SamlTransformationErrorException(
                MessageFormat.format("The Authn Response issuer format [{0}] does not match the assertion issuer format [{1}]", responseIssuerFormat, assertionIssuerFormat),
                Level.ERROR
        );
    }

    public static SamlTransformationErrorException authnResponseAssertionIssuerValueMismatch(String responseIssuer, String assertionIssuer) {
        return new SamlTransformationErrorException(
                MessageFormat.format("The Authn Response issuer [{0}] does not match the assertion issuer [{1}]", responseIssuer, assertionIssuer),
                Level.ERROR
        );
    }

}
