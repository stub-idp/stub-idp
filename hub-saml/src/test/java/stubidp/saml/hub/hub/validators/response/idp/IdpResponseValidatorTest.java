package stubidp.saml.hub.hub.validators.response.idp;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import stubidp.saml.hub.core.validators.DestinationValidator;
import stubidp.saml.hub.hub.validators.response.idp.IdpResponseValidator;
import stubidp.saml.hub.hub.validators.response.idp.components.EncryptedResponseFromIdpValidator;
import stubidp.saml.hub.hub.validators.response.idp.components.ResponseAssertionsFromIdpValidator;
import stubidp.saml.security.AssertionDecrypter;
import stubidp.saml.security.SamlAssertionsSignatureValidator;
import stubidp.saml.security.validators.ValidatedResponse;
import stubidp.saml.security.validators.signature.SamlResponseSignatureValidator;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IdpResponseValidatorTest {

    @Mock
    private SamlResponseSignatureValidator samlResponseSignatureValidator;
    @Mock
    private AssertionDecrypter assertionDecrypter;
    @Mock
    private SamlAssertionsSignatureValidator samlAssertionsSignatureValidator;
    @Mock
    private EncryptedResponseFromIdpValidator encryptedResponseFromIdpValidator;
    @Mock
    private DestinationValidator responseDestinationValidator;
    @Mock
    private ResponseAssertionsFromIdpValidator responseAssertionsFromIdpValidator;
    @Mock
    private Response response;

    private IdpResponseValidator validator;

    @Before
    public void setUp() {
        validator = new IdpResponseValidator(
            samlResponseSignatureValidator,
            assertionDecrypter,
            samlAssertionsSignatureValidator,
            encryptedResponseFromIdpValidator,
            responseDestinationValidator,
            responseAssertionsFromIdpValidator);
    }

    @Test
    public void shouldValidateResponseIsEncrypted() {
        validator.validate(response);
        verify(encryptedResponseFromIdpValidator).validate(response);
    }

    @Test
    public void shouldValidateResponseDestination() {
        validator.validate(response);
        verify(responseDestinationValidator).validate(response.getDestination());
    }

    @Test
    public void shouldValidateSamlResponseSignature() {
        validator.validate(response);
        verify(samlResponseSignatureValidator).validate(response, IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
    }

    @Test
    public void shouldValidateSamlAssertionSignature() {
        Assertion assertion = mock(Assertion.class);
        List<Assertion> assertions = ImmutableList.of(assertion);
        ValidatedResponse validatedResponse = mock(ValidatedResponse.class);

        when(samlResponseSignatureValidator.validate(response, IDPSSODescriptor.DEFAULT_ELEMENT_NAME)).thenReturn(validatedResponse);
        when(assertionDecrypter.decryptAssertions(validatedResponse)).thenReturn(assertions);

        validator.validate(response);

        verify(samlAssertionsSignatureValidator).validate(assertions, IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
    }
}
