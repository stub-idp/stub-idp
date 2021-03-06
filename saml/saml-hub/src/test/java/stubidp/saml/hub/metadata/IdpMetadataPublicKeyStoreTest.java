package stubidp.saml.hub.metadata;

import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.xml.BasicParserPool;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.KeyDescriptor;
import org.opensaml.xmlsec.signature.KeyInfo;
import org.opensaml.xmlsec.signature.X509Certificate;
import org.opensaml.xmlsec.signature.X509Data;
import org.opensaml.xmlsec.signature.support.SignatureException;
import stubidp.saml.hub.metadata.exceptions.NoKeyConfiguredForEntityException;
import stubidp.saml.security.StringBackedMetadataResolver;
import stubidp.saml.test.OpenSAMLRunner;
import stubidp.saml.test.builders.EntityDescriptorBuilder;
import stubidp.saml.test.builders.IdpSsoDescriptorBuilder;
import stubidp.saml.test.builders.KeyDescriptorBuilder;
import stubidp.saml.test.builders.KeyInfoBuilder;
import stubidp.saml.test.builders.X509CertificateBuilder;
import stubidp.saml.test.builders.X509DataBuilder;
import stubidp.saml.test.metadata.EntityDescriptorFactory;
import stubidp.saml.test.metadata.MetadataFactory;
import stubidp.test.devpki.TestCertificateStrings;
import stubidp.test.devpki.TestEntityIds;

import java.io.ByteArrayInputStream;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Base64;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class IdpMetadataPublicKeyStoreTest extends OpenSAMLRunner {

    private static MetadataResolver metadataResolver;

    @BeforeAll
    static void setUp() {
        metadataResolver = initializeMetadata();
    }

    private static MetadataResolver initializeMetadata() {
        try {
            EntityDescriptorFactory descriptorFactory = new EntityDescriptorFactory();
            String metadata = new MetadataFactory().metadata(asList(
                    descriptorFactory.hubEntityDescriptor(),
                    idpEntityDescriptor(TestEntityIds.STUB_IDP_ONE, TestCertificateStrings.STUB_IDP_PUBLIC_PRIMARY_CERT)
            ));
            InitializationService.initialize();
            StringBackedMetadataResolver stringBackedMetadataResolver = new StringBackedMetadataResolver(metadata);
            BasicParserPool basicParserPool = new BasicParserPool();
            basicParserPool.initialize();
            stringBackedMetadataResolver.setParserPool(basicParserPool);
            stringBackedMetadataResolver.setMinRefreshDelay(Duration.ofMillis(14400000));
            stringBackedMetadataResolver.setRequireValidMetadata(true);
            stringBackedMetadataResolver.setId("testResolver");
            stringBackedMetadataResolver.initialize();
            return stringBackedMetadataResolver;
        } catch (InitializationException | ComponentInitializationException e) {
            throw new RuntimeException(e);
        }
    }

    private static PublicKey getX509Key(String encodedCertificate) throws CertificateException {
        byte[] derValue = Base64.getMimeDecoder().decode(encodedCertificate);
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        Certificate certificate = certificateFactory.generateCertificate(new ByteArrayInputStream(derValue));
        return certificate.getPublicKey();
    }

    private static EntityDescriptor idpEntityDescriptor(String idpEntityId, String public_signing_certificate) {
        KeyDescriptor keyDescriptor = buildKeyDescriptor(public_signing_certificate);
        IDPSSODescriptor idpssoDescriptor = IdpSsoDescriptorBuilder.anIdpSsoDescriptor().addKeyDescriptor(keyDescriptor).withoutDefaultSigningKey().build();
        try {
            return EntityDescriptorBuilder.anEntityDescriptor()
                    .withEntityId(idpEntityId)
                    .withIdpSsoDescriptor(idpssoDescriptor)
                    .withValidUntil(Instant.now().atZone(ZoneId.of("UTC")).plusWeeks(2).toInstant())
                    .withSignature(null)
                    .withoutSigning()
                    .setAddDefaultSpServiceDescriptor(false)
                    .build();
        } catch (MarshallingException | SignatureException e) {
            throw new RuntimeException(e);
        }
    }

    private static KeyDescriptor buildKeyDescriptor(String certificate) {
        X509Certificate x509Certificate = X509CertificateBuilder.aX509Certificate().withCert(certificate).build();
        X509Data build = X509DataBuilder.aX509Data().withX509Certificate(x509Certificate).build();
        KeyInfo signing_one = KeyInfoBuilder.aKeyInfo().withKeyName("signing_one").withX509Data(build).build();
        return KeyDescriptorBuilder.aKeyDescriptor().withKeyInfo(signing_one).build();
    }

    @Test
    void shouldReturnTheSigningKeysForAnEntity() throws Exception {
        IdpMetadataPublicKeyStore idpMetadataPublicKeyStore = new IdpMetadataPublicKeyStore(metadataResolver);

        PublicKey expectedPublicKey = getX509Key(TestCertificateStrings.STUB_IDP_PUBLIC_PRIMARY_CERT);
        assertThat(idpMetadataPublicKeyStore.getVerifyingKeysForEntity(TestEntityIds.STUB_IDP_ONE)).containsExactly(expectedPublicKey);
    }

    @Test
    void shouldRaiseAnExceptionWhenThereIsNoEntityDescriptor() {
        IdpMetadataPublicKeyStore idpMetadataPublicKeyStore = new IdpMetadataPublicKeyStore(metadataResolver);
        Assertions.assertThrows(NoKeyConfiguredForEntityException.class, () -> idpMetadataPublicKeyStore.getVerifyingKeysForEntity("my-invented-entity-id"));
    }

    @Test
    void shouldRaiseAnExceptionWhenAttemptingToRetrieveAnSPSSOFromMetadata() {
        IdpMetadataPublicKeyStore idpMetadataPublicKeyStore = new IdpMetadataPublicKeyStore(metadataResolver);
        Assertions.assertThrows(NoKeyConfiguredForEntityException.class, () -> idpMetadataPublicKeyStore.getVerifyingKeysForEntity("https://signin.service.gov.uk"));
    }
}
