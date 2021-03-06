package stubsp.stubsp.integration.support;

import certificates.values.CACertificates;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import org.apache.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.saml2.metadata.EntitiesDescriptor;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.xmlsec.signature.support.SignatureException;
import stubidp.saml.constants.Constants;
import stubidp.saml.extensions.IdaSamlBootstrap;
import stubidp.saml.security.EncryptionKeyStore;
import stubidp.saml.security.IdaKeyStore;
import stubidp.saml.test.builders.EntityDescriptorBuilder;
import stubidp.saml.test.builders.IdpSsoDescriptorBuilder;
import stubidp.saml.test.builders.KeyDescriptorBuilder;
import stubidp.saml.test.metadata.EntitiesDescriptorFactory;
import stubidp.saml.test.metadata.MetadataFactory;
import stubidp.test.utils.httpstub.HttpStubRule;
import stubidp.test.utils.keystore.KeyStoreResource;
import stubidp.test.utils.keystore.builders.KeyStoreResourceBuilder;
import stubidp.utils.security.security.PrivateKeyFactory;
import stubidp.utils.security.security.PublicKeyFactory;
import stubidp.utils.security.security.X509CertificateFactory;
import stubsp.stubsp.StubSpApplication;
import stubsp.stubsp.Urls;
import stubsp.stubsp.configuration.StubSpConfiguration;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static stubidp.test.devpki.TestCertificateStrings.METADATA_SIGNING_A_PRIVATE_KEY;
import static stubidp.test.devpki.TestCertificateStrings.METADATA_SIGNING_A_PUBLIC_CERT;
import static stubidp.test.devpki.TestCertificateStrings.STUB_IDP_PUBLIC_PRIMARY_CERT;
import static stubidp.test.devpki.TestCertificateStrings.STUB_IDP_PUBLIC_PRIMARY_PRIVATE_KEY;
import static stubidp.test.devpki.TestCertificateStrings.TEST_RP_PRIVATE_ENCRYPTION_KEY;
import static stubidp.test.devpki.TestCertificateStrings.TEST_RP_PRIVATE_SIGNING_KEY;
import static stubidp.test.devpki.TestCertificateStrings.TEST_RP_PUBLIC_ENCRYPTION_CERT;
import static stubidp.test.devpki.TestCertificateStrings.TEST_RP_PUBLIC_SIGNING_CERT;

public class StubSpAppExtension extends DropwizardAppExtension<StubSpConfiguration> {

    private static final X509CertificateFactory x509CertificateFactory = new X509CertificateFactory();
    private static final PublicKeyFactory publicKeyFactory = new PublicKeyFactory(x509CertificateFactory);

    static {
        IdaSamlBootstrap.bootstrap();
    }

    private static final Logger LOG = Logger.getLogger(StubSpAppExtension.class);
    private static final HttpStubRule metadataServer = new HttpStubRule();
    private static final String IDP_METADATA_PATH = "/saml/metadata/idp";
    public static final String SP_ENTITY_ID = String.format("http://stub_sp.acme.org/%s/SSO/POST", "stub-sp");
    public static final String IDP_ENTITY_ID = String.format("http://stub_idp.acme.org/%s/SSO/POST", "stub-idp-one");

    private static final KeyStoreResource metadataTrustStore = KeyStoreResourceBuilder.aKeyStoreResource().withCertificate("metadataCA", CACertificates.TEST_METADATA_CA).withCertificate("rootCA", CACertificates.TEST_ROOT_CA).build();
    private static final KeyStoreResource idpTrustStore = KeyStoreResourceBuilder.aKeyStoreResource().withCertificate("idpCA", CACertificates.TEST_IDP_CA).withCertificate("rootCA", CACertificates.TEST_ROOT_CA).build();

    private static ConfigOverride[] withDefaultOverrides(Map<String, String> configOverrides) {
        Map<String, String> config = Map.ofEntries(
                Map.entry("server.applicationConnectors[0].port", "0"),
                Map.entry("server.adminConnectors[0].port", "0"),
                Map.entry("logging.appenders[0].type", "console"),
                Map.entry("server.requestLog.appenders[0].type", "console"),
                Map.entry("saml.entityId", SP_ENTITY_ID),
                Map.entry("saml.expectedDestination", getExpectedDestinationHost()),
                Map.entry("metadata.uri", "http://localhost:" + metadataServer.getPort() + IDP_METADATA_PATH),
                Map.entry("metadata.expectedEntityId", IDP_ENTITY_ID),
                Map.entry("metadata.trustStore.store", metadataTrustStore.getAbsolutePath()),
                Map.entry("metadata.trustStore.password", metadataTrustStore.getPassword()),
                Map.entry("metadata.idpTrustStore.store", idpTrustStore.getAbsolutePath()),
                Map.entry("metadata.idpTrustStore.password", idpTrustStore.getPassword()),
                Map.entry("signingKeyPairConfiguration.privateKeyConfiguration.type", "encoded"),
                Map.entry("signingKeyPairConfiguration.privateKeyConfiguration.key", TEST_RP_PRIVATE_SIGNING_KEY),
                Map.entry("signingKeyPairConfiguration.publicKeyConfiguration.type", "x509"),
                Map.entry("signingKeyPairConfiguration.publicKeyConfiguration.cert", TEST_RP_PUBLIC_SIGNING_CERT),
                Map.entry("encryptionKeyPairConfiguration.privateKeyConfiguration.type", "encoded"),
                Map.entry("encryptionKeyPairConfiguration.privateKeyConfiguration.key", TEST_RP_PRIVATE_ENCRYPTION_KEY),
                Map.entry("encryptionKeyPairConfiguration.publicKeyConfiguration.type", "x509"),
                Map.entry("encryptionKeyPairConfiguration.publicKeyConfiguration.cert", TEST_RP_PUBLIC_ENCRYPTION_CERT),
                Map.entry("spMetadataSigningKeyPairConfiguration.privateKeyConfiguration.type", "encoded"),
                Map.entry("spMetadataSigningKeyPairConfiguration.privateKeyConfiguration.key", METADATA_SIGNING_A_PRIVATE_KEY),
                Map.entry("spMetadataSigningKeyPairConfiguration.publicKeyConfiguration.type", "x509"),
                Map.entry("spMetadataSigningKeyPairConfiguration.publicKeyConfiguration.cert", METADATA_SIGNING_A_PUBLIC_CERT),
                Map.entry("secureCookieConfiguration.secure", "true"),
                Map.entry("secureCookieConfiguration.keyConfiguration.base64EncodedKey", Base64.getEncoder().encodeToString(new byte[64]))
        );
        config = new HashMap<>(config);
        config.putAll(configOverrides);
        final List<ConfigOverride> overrides = config.entrySet().stream()
                .map(o -> ConfigOverride.config(o.getKey(), o.getValue()))
                .collect(Collectors.toUnmodifiableList());
        return overrides.toArray(new ConfigOverride[config.size()]);
    }

    public static URI getExpectedDestination() {
        return UriBuilder.fromUri(getExpectedDestinationHost() + Urls.SAML_SSO_RESPONSE_RESOURCE).build();
    }

    private static String getExpectedDestinationHost() {
        return "http://localhost:0";
    }

    public StubSpAppExtension() {
        this(Map.of());
    }

    private StubSpAppExtension(Map<String, String> configOverrides) {
        super(StubSpApplication.class, "../configuration/stub-sp.yml", withDefaultOverrides(configOverrides));
    }

    @BeforeAll
    public void before() throws Exception {
        metadataTrustStore.create();
        idpTrustStore.create();
        metadataServer.reset();
        metadataServer.register(IDP_METADATA_PATH, 200, Constants.APPLICATION_SAMLMETADATA_XML, getIdpMetadata());

        super.before();
    }

    @Override
    public void after() {
        metadataTrustStore.delete();
        idpTrustStore.delete();

        super.after();
    }

    private static String getIdpMetadata() throws MarshallingException, SignatureException {
        List<EntityDescriptor> entityDescriptors = new ArrayList<>();
        entityDescriptors.add(EntityDescriptorBuilder.anEntityDescriptor()
                .withEntityId(IDP_ENTITY_ID)
                .withIdpSsoDescriptor(IdpSsoDescriptorBuilder.anIdpSsoDescriptor()
                        .addKeyDescriptor(KeyDescriptorBuilder.aKeyDescriptor().withX509ForSigning(STUB_IDP_PUBLIC_PRIMARY_CERT).build())
                        .withoutDefaultSigningKey()
                        .build())
                .build());
        EntitiesDescriptor entitiesDescriptor = new EntitiesDescriptorFactory()
                .signedEntitiesDescriptor(entityDescriptors, METADATA_SIGNING_A_PUBLIC_CERT, METADATA_SIGNING_A_PRIVATE_KEY);
        return new MetadataFactory().metadata(entitiesDescriptor);
    }

    public EncryptionKeyStore getSpEncryptionTrustStore() {
        return x -> publicKeyFactory.createPublicKey(getConfiguration().getEncryptionKeyPairConfiguration().getCert());
    }

    public IdaKeyStore getIdpSigningKeyStore() {
        return new IdaKeyStore(x509CertificateFactory.createCertificate(STUB_IDP_PUBLIC_PRIMARY_CERT),
                new KeyPair(publicKeyFactory.createPublicKey(STUB_IDP_PUBLIC_PRIMARY_CERT), new PrivateKeyFactory().createPrivateKey(Base64.getMimeDecoder().decode(STUB_IDP_PUBLIC_PRIMARY_PRIVATE_KEY))),
                List.of());
    }
}
