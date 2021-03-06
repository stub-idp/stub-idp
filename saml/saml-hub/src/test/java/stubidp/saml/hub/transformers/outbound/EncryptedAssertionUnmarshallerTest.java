package stubidp.saml.hub.transformers.outbound;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.core.impl.EncryptedAssertionBuilder;
import stubidp.saml.test.OpenSAMLRunner;
import stubidp.saml.serializers.deserializers.StringToOpenSamlObjectTransformer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EncryptedAssertionUnmarshallerTest extends OpenSAMLRunner {

    private static final String ENCRYPTED_ASSERTION_BLOB = "BLOB";

    @Mock
    public StringToOpenSamlObjectTransformer<EncryptedAssertion> stringToEncryptedAssertionTransformer;

    @Test
    void shouldCreateAEncryptedAssertionObjectFromAGivenString() {
        EncryptedAssertionUnmarshaller encryptedAssertionUnmarshaller = new EncryptedAssertionUnmarshaller(stringToEncryptedAssertionTransformer);
        final EncryptedAssertion expected = new EncryptedAssertionBuilder().buildObject();
        when(stringToEncryptedAssertionTransformer.apply(ENCRYPTED_ASSERTION_BLOB)).thenReturn(expected);
        final EncryptedAssertion encryptedAssertion = encryptedAssertionUnmarshaller.transform(ENCRYPTED_ASSERTION_BLOB);
        assertThat(encryptedAssertion).isEqualTo(expected);

    }
}
