package stubidp.eidas.metadata;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opensaml.core.config.InitializationService;
import org.opensaml.saml.common.SignableSAMLObject;
import org.slf4j.LoggerFactory;
import stubidp.eidas.utils.FileReader;
import stubidp.eidas.utils.keyloader.FileKeyLoader;

import java.io.File;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class MetadataSignatureValidatorTest {

    private PrivateKey privateKeyForSigning;
    private X509Certificate certificateForSigning;
    private X509Certificate wrongCertificate;

    @BeforeEach
    void setUp() throws Exception {
        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.OFF);

        InitializationService.initialize();
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        privateKeyForSigning = FileKeyLoader.loadECKey(new File(Objects.requireNonNull(getClass().getClassLoader().getResource("pki/ecdsa.test.pk8")).getFile()));
        certificateForSigning = FileKeyLoader.loadCert(new File(Objects.requireNonNull(getClass().getClassLoader().getResource("pki/ecdsa.test.crt")).getFile()));
        wrongCertificate = FileKeyLoader.loadCert(new File(Objects.requireNonNull(getClass().getClassLoader().getResource("pki/diff_ecdsa.test.crt")).getFile()));
    }

    @Test
    void shouldReturnTrueIfSignatureMatchesKeyPair() throws Exception {
        SignableSAMLObject signedMetadataSaml = loadMetadataAndSign("metadata/unsigned/metadata.xml", certificateForSigning);

        MetadataSignatureValidator signatureValidator = new MetadataSignatureValidator(certificateForSigning.getPublicKey(), privateKeyForSigning);
        boolean result = signatureValidator.validate(signedMetadataSaml);

        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalseIfSignatureDoesNotMatchKeyPair() throws Exception {
        SignableSAMLObject signedMetadataSaml = loadMetadataAndSign("metadata/unsigned/metadata.xml", wrongCertificate);

        MetadataSignatureValidator signatureValidator = new MetadataSignatureValidator(wrongCertificate.getPublicKey(), privateKeyForSigning);
        boolean result = signatureValidator.validate(signedMetadataSaml);

        assertThat(result).isFalse();
    }

    private SignableSAMLObject loadMetadataAndSign(String resourceFilePath, X509Certificate certificateForSigning) throws Exception {
        File file = new File(Objects.requireNonNull(getClass().getClassLoader().getResource(resourceFilePath)).getFile());
        String metadataString = FileReader.readFileContent(file);
        return new ConnectorMetadataSigner(certificateForSigning, privateKeyForSigning, AlgorithmType.ECDSA).sign(metadataString);
    }
}