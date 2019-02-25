package stubidp.saml.security.validators.issuer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.NameIDType;
import stubidp.saml.extensions.validation.SamlValidationSpecificationFailure;
import stubidp.saml.security.errors.SamlTransformationErrorFactory;
import stubidp.saml.security.saml.OpenSAMLMockitoRunner;
import stubidp.saml.security.saml.SamlTransformationErrorManagerTestHelper;
import stubidp.saml.security.saml.builders.IssuerBuilder;

@RunWith(OpenSAMLMockitoRunner.class)
public class IssuerValidatorTest {

    private IssuerValidator validator;

    @Before
    public void setUp() throws Exception {
        validator = new IssuerValidator();
    }

    @Test
    public void validate_shouldThrowExceptionIfIssuerElementIsMissing() throws Exception {
        assertExceptionMessage(null, SamlTransformationErrorFactory.missingIssuer());
    }

    @Test
    public void validate_shouldThrowExceptionIfIssuerIdIsMissing() throws Exception {
        Issuer assertionIssuer = IssuerBuilder.anIssuer().withIssuerId(null).build();

        assertExceptionMessage(assertionIssuer, SamlTransformationErrorFactory.emptyIssuer());
    }

    @Test
    public void validate_shouldThrowExceptionIfIssuerFormatAttributeHasInvalidValue() throws Exception {
        String invalidFormat = "invalid";
        Issuer assertionIssuer = IssuerBuilder.anIssuer().withFormat(invalidFormat).build();

        assertExceptionMessage(assertionIssuer, SamlTransformationErrorFactory.illegalIssuerFormat(invalidFormat, NameIDType.ENTITY));
    }

    @Test
    public void validate_shouldDoNothingIfIssuerFormatAttributeIsMissing() throws Exception {
        Issuer assertionIssuer = IssuerBuilder.anIssuer().withFormat(null).build();

        validator.validate(assertionIssuer);
    }

    @Test
    public void validate_shouldDoNothingIfIssuerFormatAttributeHasValidValue() throws Exception {
        Issuer assertionIssuer = IssuerBuilder.anIssuer().withFormat(NameIDType.ENTITY).build();

        validator.validate(assertionIssuer);
    }


    private void assertExceptionMessage(
            final Issuer assertionIssuer,
            SamlValidationSpecificationFailure failure) {

        SamlTransformationErrorManagerTestHelper.validateFail(
                () -> validator.validate(assertionIssuer),
                failure
        );
    }
}