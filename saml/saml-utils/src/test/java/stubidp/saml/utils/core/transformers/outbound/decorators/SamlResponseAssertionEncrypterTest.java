package stubidp.saml.utils.core.transformers.outbound.decorators;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.encryption.Encrypter;
import org.opensaml.security.credential.Credential;
import org.opensaml.xmlsec.encryption.support.EncryptionException;
import stubidp.saml.security.EncrypterFactory;
import stubidp.saml.security.EntityToEncryptForLocator;
import stubidp.saml.security.KeyStoreBackedEncryptionCredentialResolver;
import stubidp.saml.test.OpenSAMLRunner;

import java.util.LinkedList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SamlResponseAssertionEncrypterTest extends OpenSAMLRunner {

    @Mock
    private KeyStoreBackedEncryptionCredentialResolver credentialFactory;
    @Mock
    private Credential credential;
    @Mock
    private EntityToEncryptForLocator entityToEncryptForLocator;
    @Mock
    private EncrypterFactory encrypterFactory;
    @Mock
    private Encrypter encrypter;
    @Mock
    private Response response;
    @Mock
    private Assertion assertion;
    @Mock
    private EncryptedAssertion encryptedAssertion;

    @Test
    void shouldConvertAssertionIntoEncryptedAssertion() throws EncryptionException {
        when(entityToEncryptForLocator.fromRequestId(ArgumentMatchers.anyString())).thenReturn("some id");
        when(credentialFactory.getEncryptingCredential("some id")).thenReturn(credential);
        when(encrypterFactory.createEncrypter(credential)).thenReturn(encrypter);
        List<Assertion> assertionList = new LinkedList<>();
        assertionList.add(assertion);
        when(response.getAssertions()).thenReturn(assertionList);
        when(encrypter.encrypt(assertion)).thenReturn(encryptedAssertion);
        final List<EncryptedAssertion> encryptedAssertions = new LinkedList<>();
        List<EncryptedAssertion> encryptedAssertionSpy = spy(encryptedAssertions);
        when(response.getEncryptedAssertions()).thenReturn(encryptedAssertionSpy);
        when(response.getInResponseTo()).thenReturn("some id");

        SamlResponseAssertionEncrypter assertionEncrypter = new SamlResponseAssertionEncrypter(
                credentialFactory,
                encrypterFactory,
                entityToEncryptForLocator
        );

        assertionEncrypter.encryptAssertions(response);

        verify(encryptedAssertionSpy, times(1)).add(encryptedAssertion);
    }

    @Test
    void decorate_shouldWrapEncryptionAssertionInSamlExceptionWhenEncryptionFails() throws EncryptionException {
        when(entityToEncryptForLocator.fromRequestId(ArgumentMatchers.anyString())).thenReturn("some id");
        when(credentialFactory.getEncryptingCredential("some id")).thenReturn(credential);
        List<Assertion> assertionList = List.of(assertion);
        when(response.getAssertions()).thenReturn(assertionList);
        when(encrypterFactory.createEncrypter(credential)).thenReturn(encrypter);
        EncryptionException encryptionException = new EncryptionException("BLAM!");
        when(encrypter.encrypt(assertion)).thenThrow(encryptionException);
        when(response.getInResponseTo()).thenReturn("some id");

        SamlResponseAssertionEncrypter assertionEncrypter =
                new SamlResponseAssertionEncrypter(credentialFactory, encrypterFactory,
                        entityToEncryptForLocator
                );

        final Exception e = Assertions.assertThrows(Exception.class, () -> assertionEncrypter.encryptAssertions(response));
        assertThat(e.getCause()).isEqualTo(encryptionException);
    }
}
