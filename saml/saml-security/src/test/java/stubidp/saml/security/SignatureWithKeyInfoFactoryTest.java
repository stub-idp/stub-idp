package stubidp.saml.security;

import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.xmlsec.algorithm.descriptors.DigestSHA256;
import org.opensaml.xmlsec.algorithm.descriptors.SignatureRSASHA256;
import stubidp.saml.test.OpenSAMLRunner;
import stubidp.test.devpki.TestCertificateStrings;
import stubidp.test.devpki.TestEntityIds;
import stubidp.utils.security.security.PrivateKeyFactory;
import stubidp.utils.security.security.PublicKeyFactory;
import stubidp.utils.security.security.X509CertificateFactory;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static stubidp.saml.test.builders.AssertionBuilder.anAssertion;

public class SignatureWithKeyInfoFactoryTest extends OpenSAMLRunner {

    private PublicKeyFactory publicKeyFactory;

	@Test
    void shouldCreateMultipleSignaturesWithoutThrowingExceptions() {
        final String id = UUID.randomUUID().toString();
        publicKeyFactory = new PublicKeyFactory(new X509CertificateFactory());
        PrivateKey privateKey = new PrivateKeyFactory().createPrivateKey(Base64.getMimeDecoder().decode(TestCertificateStrings.PRIVATE_SIGNING_KEYS.get(
		        TestEntityIds.HUB_ENTITY_ID)));
        PublicKey publicKey = publicKeyFactory.createPublicKey(TestCertificateStrings.getPrimaryPublicEncryptionCert(TestEntityIds.HUB_ENTITY_ID));

        PrivateKey privateEncryptionKey = new PrivateKeyFactory().createPrivateKey(Base64.getMimeDecoder().decode(TestCertificateStrings.HUB_TEST_PRIVATE_ENCRYPTION_KEY));
        PublicKey publicEncryptionKey = publicKeyFactory.createPublicKey(TestCertificateStrings.HUB_TEST_PUBLIC_ENCRYPTION_CERT);

        KeyPair encryptionKeyPair = new KeyPair(publicEncryptionKey, privateEncryptionKey);

        KeyPair signingKeyPair = new KeyPair(publicKey, privateKey);
		IdaKeyStore keystore = new IdaKeyStore(signingKeyPair, Collections.singletonList(encryptionKeyPair));
		IdaKeyStoreCredentialRetriever keyStoreCredentialRetriever = new IdaKeyStoreCredentialRetriever(keystore);
		SignatureWithKeyInfoFactory keyInfoFactory = new SignatureWithKeyInfoFactory(keyStoreCredentialRetriever, new SignatureRSASHA256(), new DigestSHA256(), "", "");

		Assertion assertion1 = anAssertion().withSignature(keyInfoFactory.createSignature()).buildUnencrypted();
        Assertion assertion2 = anAssertion().withId(id).withSignature(keyInfoFactory.createSignature(id)).buildUnencrypted();

        assertThat(assertion1.getSignature()).isNotNull();
        assertThat(assertion2.getSignature()).isNotNull();
    }
}
