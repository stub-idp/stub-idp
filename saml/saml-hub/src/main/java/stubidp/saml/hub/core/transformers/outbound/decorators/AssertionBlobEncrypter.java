package stubidp.saml.hub.core.transformers.outbound.decorators;

import org.opensaml.saml.saml2.core.Assertion;
import stubidp.saml.serializers.deserializers.StringToOpenSamlObjectTransformer;
import stubidp.saml.serializers.serializers.XmlObjectToBase64EncodedStringTransformer;
import stubidp.saml.utils.core.transformers.outbound.decorators.AssertionEncrypter;

import javax.inject.Inject;
import java.util.Optional;

public class AssertionBlobEncrypter {
    private final StringToOpenSamlObjectTransformer<Assertion> stringToAssertionTransformer;
    private final XmlObjectToBase64EncodedStringTransformer<Assertion> assertionToBase64EncodedStringTransformer;
    private final AssertionEncrypter assertionEncrypter;

    @Inject
    public AssertionBlobEncrypter(StringToOpenSamlObjectTransformer<Assertion> stringToAssertionTransformer,
                                  XmlObjectToBase64EncodedStringTransformer<Assertion> assertionToBase64EncodedStringTransformer,
                                  AssertionEncrypter assertionEncrypter)
    {
        this.stringToAssertionTransformer = stringToAssertionTransformer;
        this.assertionToBase64EncodedStringTransformer = assertionToBase64EncodedStringTransformer;
        this.assertionEncrypter = assertionEncrypter;
    }


    public String encryptAssertionBlob(String entityId, String matchingDatasetAssertionBlob) {
        return Optional
                .ofNullable(matchingDatasetAssertionBlob)
                .map(stringToAssertionTransformer)
                .map(assertion -> assertionEncrypter.encrypt(assertion, entityId))
                .map(assertionToBase64EncodedStringTransformer::apply)
                .get();
    }
}
