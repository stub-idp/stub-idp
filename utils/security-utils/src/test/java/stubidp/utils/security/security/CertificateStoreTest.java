package stubidp.utils.security.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import stubidp.utils.security.configuration.PublicKeyFileConfiguration;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static stubidp.utils.security.security.Certificate.BEGIN_CERT;
import static stubidp.utils.security.security.Certificate.END_CERT;

@ExtendWith(MockitoExtension.class)
public class CertificateStoreTest {

    private PublicKeyFileConfiguration publicKeyConfiguration;
    private PublicKeyFileConfiguration publicKeyConfiguration2;

    @BeforeEach
    void setup() {
        publicKeyConfiguration = getPublicKey("public_key.crt");
        publicKeyConfiguration2 = getPublicKey("public_key_2.crt");
    }

    private PublicKeyFileConfiguration getPublicKey(String publicKey) {
        return new PublicKeyFileConfiguration(getClass().getClassLoader().getResource(publicKey).getFile(), "TestCertificateName");
    }

    @Test
    void getEncryptionCertificateValue_shouldStripOutHeadersIfPresent() {
        CertificateStore certificateStore = new CertificateStore(List.of(publicKeyConfiguration), List.of(publicKeyConfiguration));
        String encryptionCertificateValue = certificateStore.getEncryptionCertificates().get(0).getCertificate();

        assertThat(encryptionCertificateValue.contains("BEGIN")).isEqualTo(false);
        assertThat(encryptionCertificateValue.contains("END")).isEqualTo(false);
        assertThat(publicKeyConfiguration.getCert()).contains(encryptionCertificateValue);
    }

    @Test
    void getEncryptionCertificateValue_shouldHandleMultipleCertificateValues() {
        CertificateStore certificateStore = new CertificateStore(List.of(publicKeyConfiguration, publicKeyConfiguration2), List.of(publicKeyConfiguration));
        final List<Certificate> encryptionCertificates = certificateStore.getEncryptionCertificates();

        encryptionCertificates.forEach(cert -> assertThat(List.of(
                stripHeaders(publicKeyConfiguration.getCert()),
                stripHeaders(publicKeyConfiguration2.getCert()))).contains(cert.getCertificate()));
    }

    private String stripHeaders(final String originalCertificate) {
        return originalCertificate.replace(BEGIN_CERT, "").replace(END_CERT, "").replace(" ","");
    }

    @Test
    void getSigningCertificateValue_shouldStripOutHeadersIfPresent() {
        CertificateStore certificateStore = new CertificateStore(List.of(publicKeyConfiguration), List.of(publicKeyConfiguration2));
        List<Certificate> signingCertificateValues = certificateStore.getSigningCertificates();

        assertThat(signingCertificateValues).hasSize(1);

        Certificate primaryCertificate = signingCertificateValues.get(0);
        assertThat(primaryCertificate.getIssuerId()).isEqualTo("TestCertificateName");
        assertThat(publicKeyConfiguration2.getCert()).contains(primaryCertificate.getCertificate());
    }
}
