package stubidp.eidas.metadata;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import net.shibboleth.utilities.java.support.xml.XMLParserException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.saml.common.SignableSAMLObject;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import org.slf4j.LoggerFactory;
import stubidp.eidas.utils.FileReader;
import stubidp.eidas.utils.keyloader.FileKeyLoader;
import stubidp.test.devpki.TestCertificateStrings;
import stubidp.utils.security.security.PrivateKeyFactory;
import stubidp.utils.security.security.X509CertificateFactory;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static stubidp.eidas.metadata.AlgorithmType.ECDSA;
import static stubidp.eidas.metadata.AlgorithmType.RSA;

public class ConnectorMetadataSignerTest {

    private PrivateKey privateKeyForSigning;
    private X509Certificate certificateForSigning;
    private String unsignedMetadataString;

    @BeforeEach
    void setUp() throws InitializationException, IOException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException {
        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.OFF);

        InitializationService.initialize();
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        privateKeyForSigning = FileKeyLoader.loadECKey(new File(getClass().getClassLoader().getResource("pki/ecdsa.test.pk8").getFile()));
        certificateForSigning = FileKeyLoader.loadCert(new File(getClass().getClassLoader().getResource("pki/ecdsa.test.crt").getFile()));
        unsignedMetadataString = loadMetadataString("metadata/unsigned/metadata.xml");
    }

    @Test
    void shouldSignMetadataWithECDSA() throws Exception {
        SignableSAMLObject signedMetadata = new ConnectorMetadataSigner(certificateForSigning, privateKeyForSigning, ECDSA).sign(unsignedMetadataString);
        Signature signature = signedMetadata.getSignature();

        assertThat(unsignedMetadataString).doesNotContain(Base64.getEncoder().encodeToString(certificateForSigning.getEncoded()));
        assertThat(signedMetadata.isSigned()).isTrue();
        assertThat(signature.getSigningCredential().getPrivateKey()).isEqualTo(privateKeyForSigning);
        assertThat(signature.getSigningCredential().getPublicKey()).isEqualTo(certificateForSigning.getPublicKey());
        assertThat(signature.getSignatureAlgorithm()).isEqualTo(SignatureConstants.ALGO_ID_SIGNATURE_ECDSA_SHA256);
    }

    @Test
    void shouldSignMetadataWithRSA() throws Exception {
        privateKeyForSigning = new PrivateKeyFactory().createPrivateKey(Base64.getDecoder().decode(TestCertificateStrings.METADATA_SIGNING_A_PRIVATE_KEY));
        certificateForSigning = new X509CertificateFactory().createCertificate(TestCertificateStrings.METADATA_SIGNING_A_PUBLIC_CERT);

        SignableSAMLObject signedMetadata = new ConnectorMetadataSigner(certificateForSigning, privateKeyForSigning, RSA).sign(unsignedMetadataString);
        Signature signature = signedMetadata.getSignature();

        assertThat(unsignedMetadataString).doesNotContain(Base64.getEncoder().encodeToString(certificateForSigning.getEncoded()));
        assertThat(signedMetadata.isSigned()).isTrue();
        assertThat(signature.getSigningCredential().getPrivateKey()).isEqualTo(privateKeyForSigning);
        assertThat(signature.getSigningCredential().getPublicKey()).isEqualTo(certificateForSigning.getPublicKey());
        assertThat(signature.getSignatureAlgorithm()).isEqualTo(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256);
    }

    @Test
    void shouldErrorWhenMetadataEmpty() {
        assertThrows(XMLParserException.class,
                ()->new ConnectorMetadataSigner(certificateForSigning, privateKeyForSigning, ECDSA).sign(""));
    }

    @Test
    void shouldErrorWhenMetadataNull() {
        assertThrows(NullPointerException.class,
                ()->new ConnectorMetadataSigner(certificateForSigning, privateKeyForSigning, ECDSA).sign(null));
    }

    @Test
    void shouldErrorWhenMetadataInvalid() throws IOException {
        String metadataString = loadMetadataString("metadata/unsigned/bad-metadata.xml");
        assertThrows(UnmarshallingException.class,
                ()->new ConnectorMetadataSigner(certificateForSigning, privateKeyForSigning, ECDSA).sign(metadataString));
    }

    private String loadMetadataString(String resourceFilePath) throws IOException {
        ClassLoader classLoader = this.getClass().getClassLoader();
        File file = new File(classLoader.getResource(resourceFilePath).getFile());

        return FileReader.readFileContent(file);
    }
}
