package stubidp.stubidp.builders;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.metadata.ArtifactResolutionService;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.KeyDescriptor;
import org.opensaml.saml.saml2.metadata.ManageNameIDService;
import org.opensaml.saml.saml2.metadata.SingleLogoutService;
import org.opensaml.security.SecurityException;
import org.opensaml.security.credential.UsageType;
import org.opensaml.xmlsec.signature.KeyInfo;
import org.opensaml.xmlsec.signature.X509Certificate;
import org.opensaml.xmlsec.signature.X509Data;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.w3c.dom.Document;
import stubidp.saml.extensions.IdaSamlBootstrap;
import stubidp.saml.serializers.serializers.XmlObjectToElementTransformer;
import stubidp.saml.test.OpenSAMLRunner;
import stubidp.test.devpki.TestCertificateStrings;
import stubidp.utils.security.security.X509CertificateFactory;

import java.net.URI;
import java.security.cert.CertificateEncodingException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CountryMetadataBuilderTest extends OpenSAMLRunner {

    private static final Pattern PATTERN = Pattern.compile("\\s");

    @BeforeEach
    void setup() {
        IdaSamlBootstrap.bootstrap();
    }

    private final URI ENTITY_ID = URI.create("https://stub-country-location");
    private final URI SSO_URL = URI.create("https://stub-country-location/sso");
    private final java.security.cert.X509Certificate SIGNING_CERTIFICATE =
        new X509CertificateFactory().createCertificate(TestCertificateStrings.METADATA_SIGNING_A_PUBLIC_CERT);
    private final java.security.cert.X509Certificate ENCRYPTING_CERTIFICATE =
        new X509CertificateFactory().createCertificate(TestCertificateStrings.TEST_PUBLIC_CERT);

    @Mock
    private CountryMetadataSigningHelper metadataSigner;

    private void assertCertificate(Optional<KeyDescriptor> keyDescriptor, String certString) {
        Optional<X509Certificate> signingCertificate = keyDescriptor
            .map(KeyDescriptor::getKeyInfo)
            .map(KeyInfo::getX509Datas)
            .map(x -> x.get(0))
            .map(X509Data::getX509Certificates)
            .map(c -> c.get(0));
        assertThat(signingCertificate).isPresent();
        assertThat(prepareCertString(signingCertificate.get().getValue())).isEqualTo(prepareCertString(certString));
    }

    private String prepareCertString(String cert) {
        return PATTERN.matcher(cert).replaceAll("");
    }

    private EntityDescriptor getMetadata() throws CertificateEncodingException, MarshallingException, SignatureException {
        when(metadataSigner.sign(any(EntityDescriptor.class))).thenAnswer(i -> i.getArguments()[0]);
        CountryMetadataBuilder countryMetadataBuilder = new CountryMetadataBuilder(Duration.ofHours(1), metadataSigner);
        return countryMetadataBuilder.createEntityDescriptorForProxyNodeService(ENTITY_ID, SSO_URL, SIGNING_CERTIFICATE, ENCRYPTING_CERTIFICATE);
    }

    @Test
    void shouldGenerateEntityDescriptor() throws CertificateEncodingException, MarshallingException, SecurityException, SignatureException {
        EntityDescriptor metadata = getMetadata();
        assertThat(metadata).isNotNull();
        assertThat(metadata.getValidUntil()).isNotNull();
        assertThat(metadata.getValidUntil()).isBeforeOrEqualTo(Instant.now().atZone(ZoneId.of("UTC")).plusHours(1).toInstant());
        assertThat(metadata.getEntityID()).isEqualTo(ENTITY_ID.toString());
        assertThat(metadata.getIDPSSODescriptor(SAMLConstants.SAML20P_NS).getSingleSignOnServices().get(0).getLocation()).isEqualTo(SSO_URL.toString());
    }

    @Test
    void shouldGenerateIdpSSODescriptor() throws MarshallingException, SecurityException, SignatureException, CertificateEncodingException {
        EntityDescriptor metadata = getMetadata();
        assertThat(metadata).isNotNull();

        IDPSSODescriptor idpssoDescriptor = metadata.getIDPSSODescriptor(SAMLConstants.SAML20P_NS);
        assertThat(idpssoDescriptor).isNotNull();
        assertThat(idpssoDescriptor.getWantAuthnRequestsSigned()).isTrue();
    }

    @Test
    void shouldGenerateCertificatesInsideDescriptor() throws CertificateEncodingException, MarshallingException, SecurityException, SignatureException {
        EntityDescriptor metadata = getMetadata();
        assertThat(metadata).isNotNull();

        IDPSSODescriptor idpssoDescriptor = metadata.getIDPSSODescriptor(SAMLConstants.SAML20P_NS);
        assertThat(idpssoDescriptor).isNotNull();

        Optional<KeyDescriptor> signingKeyDescriptor = idpssoDescriptor.getKeyDescriptors().stream().filter(k -> k.getUse() == UsageType.SIGNING).findFirst();
        assertThat(signingKeyDescriptor).isPresent();
        assertCertificate(signingKeyDescriptor, TestCertificateStrings.METADATA_SIGNING_A_PUBLIC_CERT);

        Optional<KeyDescriptor> encryptingKeyDescriptor = idpssoDescriptor.getKeyDescriptors().stream().filter(k -> k.getUse() == UsageType.ENCRYPTION).findFirst();
        assertThat(encryptingKeyDescriptor).isPresent();
        assertCertificate(encryptingKeyDescriptor, TestCertificateStrings.TEST_PUBLIC_CERT);
    }

    @Test
    void shouldSignDescriptor() throws CertificateEncodingException, MarshallingException, SecurityException, SignatureException {
        EntityDescriptor metadata = getMetadata();
        verify(metadataSigner, times(1)).sign(metadata);
    }

    @Test
    void shouldNotContainOtherServiceElements() throws CertificateEncodingException, MarshallingException, SecurityException, SignatureException {
        EntityDescriptor metadata = getMetadata();
        assertThat(metadata).isNotNull();

        XmlObjectToElementTransformer<EntityDescriptor> transformer = new XmlObjectToElementTransformer<>();
        Document doc = transformer.apply(metadata).getOwnerDocument();
        assertThat(doc.getElementsByTagName(ArtifactResolutionService.DEFAULT_ELEMENT_LOCAL_NAME).getLength()).isEqualTo(0);
        assertThat(doc.getElementsByTagName(SingleLogoutService.DEFAULT_ELEMENT_LOCAL_NAME).getLength()).isEqualTo(0);
        assertThat(doc.getElementsByTagName(ManageNameIDService.DEFAULT_ELEMENT_LOCAL_NAME).getLength()).isEqualTo(0);
    }
}
