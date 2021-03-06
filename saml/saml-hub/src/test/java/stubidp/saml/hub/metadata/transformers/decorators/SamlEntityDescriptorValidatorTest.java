package stubidp.saml.hub.metadata.transformers.decorators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import stubidp.saml.extensions.validation.SamlValidationSpecificationFailure;
import stubidp.saml.hub.core.errors.SamlTransformationErrorFactory;
import stubidp.saml.test.OpenSAMLRunner;
import stubidp.saml.test.builders.IdpSsoDescriptorBuilder;
import stubidp.saml.test.builders.KeyDescriptorBuilder;
import stubidp.saml.test.builders.KeyInfoBuilder;
import stubidp.saml.test.builders.X509CertificateBuilder;
import stubidp.saml.test.support.SamlTransformationErrorManagerTestHelper;

import static stubidp.saml.test.builders.EndpointBuilder.anEndpoint;
import static stubidp.saml.test.builders.EntityDescriptorBuilder.anEntityDescriptor;
import static stubidp.saml.test.builders.X509DataBuilder.aX509Data;

public class SamlEntityDescriptorValidatorTest extends OpenSAMLRunner {

    private SamlEntityDescriptorValidator validator;

    @BeforeEach
    void setUp() {
        validator = new SamlEntityDescriptorValidator();
    }

    @Test
    void decorate_shouldThrowExceptionWhenEntityIdIsMissing() throws Exception {
        EntityDescriptor entityDescriptor = anEntityDescriptor().withEntityId(null).build();

        assertExceptionMessage(entityDescriptor, SamlTransformationErrorFactory.missingOrEmptyEntityID());

    }

    @Test
    void decorate_shouldThrowExceptionWhenEntityIdIsEmpty() throws Exception {
        EntityDescriptor entityDescriptor = anEntityDescriptor().withEntityId("").build();

        assertExceptionMessage(entityDescriptor, SamlTransformationErrorFactory.missingOrEmptyEntityID());

    }

    @Test
    void decorate_shouldThrowExceptionWhenRoleDescriptorDoesNotHaveAKeyDescriptorElement() throws Exception {
        EntityDescriptor entityDescriptor = anEntityDescriptor().withIdpSsoDescriptor(IdpSsoDescriptorBuilder.anIdpSsoDescriptor().withoutDefaultSigningKey().build()).build();

        assertExceptionMessage(entityDescriptor, SamlTransformationErrorFactory.missingKeyDescriptor());

    }

    @Test
    void decorate_shouldThrowExceptionWhenRoleDescriptorDoesNotHaveAKeyInfoElement() throws Exception {
        EntityDescriptor entityDescriptor = anEntityDescriptor().withIdpSsoDescriptor(IdpSsoDescriptorBuilder.anIdpSsoDescriptor().withoutDefaultSigningKey().addKeyDescriptor(KeyDescriptorBuilder.aKeyDescriptor().withKeyInfo(null).build()).build()).build();

        assertExceptionMessage(entityDescriptor, SamlTransformationErrorFactory.missingKeyInfo());

    }

    @Test
    void decorate_shouldThrowExceptionWhenRoleDescriptorDoesNotHaveAX509DataElement() throws Exception {
        EntityDescriptor entityDescriptor = anEntityDescriptor().withIdpSsoDescriptor(IdpSsoDescriptorBuilder.anIdpSsoDescriptor().withoutDefaultSigningKey().addKeyDescriptor(KeyDescriptorBuilder.aKeyDescriptor().withKeyInfo(KeyInfoBuilder.aKeyInfo().withX509Data(null).build()).build()).build()).build();

        assertExceptionMessage(entityDescriptor, SamlTransformationErrorFactory.missingX509Data());

    }

    @Test
    void decorate_shouldThrowExceptionWhenRoleDescriptorDoesNotHaveAX509CertificateElement() throws Exception {
        EntityDescriptor entityDescriptor = anEntityDescriptor().withIdpSsoDescriptor(IdpSsoDescriptorBuilder.anIdpSsoDescriptor().withoutDefaultSigningKey().addKeyDescriptor(KeyDescriptorBuilder.aKeyDescriptor().withKeyInfo(KeyInfoBuilder.aKeyInfo().withX509Data(aX509Data().withX509Certificate(null).build()).build()).build()).build()).build();

        assertExceptionMessage(entityDescriptor, SamlTransformationErrorFactory.missingX509Certificate());

    }

    @Test
    void decorate_shouldThrowExceptionWhenX509CertificateElementIsEmpty() throws Exception {
        EntityDescriptor entityDescriptor = anEntityDescriptor().withIdpSsoDescriptor(IdpSsoDescriptorBuilder.anIdpSsoDescriptor().withoutDefaultSigningKey().addKeyDescriptor(KeyDescriptorBuilder.aKeyDescriptor().withKeyInfo(KeyInfoBuilder.aKeyInfo().withX509Data(aX509Data().withX509Certificate(X509CertificateBuilder.aX509Certificate().withCertForEntityId(null).withCert(null).build()).build()).build()).build()).build()).build();

        assertExceptionMessage(entityDescriptor, SamlTransformationErrorFactory.emptyX509Certificiate());
    }

    @Test
    void decorate_shouldThrowExceptionWhenBothValidUntilAndCacheDurationAreMissing() throws Exception {
        EntityDescriptor entityDescriptor = anEntityDescriptor().withValidUntil(null).withCacheDuration(null).build();

        assertExceptionMessage(entityDescriptor, SamlTransformationErrorFactory.missingCacheDurationAndValidUntil());
    }

    @Test
    void decorate_shouldDoNothingWhenEntityDescriptorIsValid() throws Exception {
        EntityDescriptor entityDescriptor = anEntityDescriptor().withIdpSsoDescriptor(IdpSsoDescriptorBuilder.anIdpSsoDescriptor().withSingleSignOnService(anEndpoint().buildSingleSignOnService()).build()).build();

        validator.validate(entityDescriptor);
    }

    @Test
    void decorate_shouldNotThrowExceptionWhenEntityDescriptorIsNotSignedButNotRequired() throws Exception {
        EntityDescriptor entityDescriptor = anEntityDescriptor().withoutSigning().build();

        validator.validate(entityDescriptor);

    }

    void assertExceptionMessage(final EntityDescriptor entityDescriptor, SamlValidationSpecificationFailure failure) {

        SamlTransformationErrorManagerTestHelper.validateFail(
                () -> validator.validate(entityDescriptor),
                failure
        );

    }
}
