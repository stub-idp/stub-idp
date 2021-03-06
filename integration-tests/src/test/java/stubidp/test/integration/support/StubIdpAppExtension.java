package stubidp.test.integration.support;

import certificates.values.CACertificates;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import org.apache.log4j.Logger;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.saml2.metadata.EntitiesDescriptor;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.xmlsec.signature.support.SignatureException;
import stubidp.saml.test.metadata.EntitiesDescriptorFactory;
import stubidp.saml.test.metadata.MetadataFactory;
import stubidp.saml.security.IdaKeyStore;
import stubidp.saml.constants.Constants;
import stubidp.saml.test.builders.AssertionConsumerServiceBuilder;
import stubidp.saml.test.builders.EntityDescriptorBuilder;
import stubidp.saml.test.builders.IdpSsoDescriptorBuilder;
import stubidp.saml.test.builders.KeyDescriptorBuilder;
import stubidp.saml.test.builders.SPSSODescriptorBuilder;
import stubidp.stubidp.StubIdpApplication;
import stubidp.stubidp.configuration.IdpStubsConfiguration;
import stubidp.stubidp.configuration.StubIdp;
import stubidp.stubidp.configuration.StubIdpConfiguration;
import stubidp.stubidp.exceptions.mappers.InvalidAuthnRequestExceptionMapper;
import stubidp.stubidp.exceptions.mappers.InvalidEidasAuthnRequestExceptionMapper;
import stubidp.stubidp.resources.eidas.EidasAuthnRequestReceiverResource;
import stubidp.stubidp.resources.idp.HeadlessIdpResource;
import stubidp.stubidp.resources.idp.IdpAuthnRequestReceiverResource;
import stubidp.stubidp.saml.BaseAuthnRequestValidator;
import stubidp.stubidp.services.AuthnRequestReceiverService;
import stubidp.stubidp.services.EidasAuthnResponseService;
import stubidp.stubidp.services.NonSuccessAuthnResponseService;
import stubidp.stubidp.services.SuccessAuthnResponseService;
import stubidp.test.devpki.TestCertificateStrings;
import stubidp.test.utils.httpstub.HttpStubRule;
import stubidp.test.utils.keystore.KeyStoreResource;
import stubidp.test.utils.keystore.builders.KeyStoreResourceBuilder;
import stubidp.utils.security.security.PrivateKeyFactory;
import stubidp.utils.security.security.PublicKeyFactory;
import stubidp.utils.security.security.X509CertificateFactory;

import javax.ws.rs.core.UriBuilder;
import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;
import static stubidp.test.devpki.TestCertificateStrings.METADATA_SIGNING_A_PRIVATE_KEY;
import static stubidp.test.devpki.TestCertificateStrings.METADATA_SIGNING_A_PUBLIC_CERT;
import static stubidp.test.devpki.TestCertificateStrings.STUB_IDP_PUBLIC_PRIMARY_CERT;
import static stubidp.test.devpki.TestCertificateStrings.STUB_IDP_PUBLIC_PRIMARY_PRIVATE_KEY;
import static stubidp.test.devpki.TestEntityIds.HUB_CONNECTOR_ENTITY_ID;

public class StubIdpAppExtension extends DropwizardAppExtension<StubIdpConfiguration> {

    private static final Logger LOG = Logger.getLogger(StubIdpAppExtension.class);

    private static final String VERIFY_METADATA_PATH = "/saml/metadata/sp";
    private static final String EIDAS_METADATA_PATH = "/saml/metadata/eidas/connector";

    public static final String SP_ENTITY_ID = "http://localhost/stubsp/SAML2/metadata/federation";

    private static final HttpStubRule verifyMetadataServer = new HttpStubRule();
    private static final HttpStubRule eidasMetadataServer = new HttpStubRule();
    private static final KeyStoreResource metadataTrustStore = KeyStoreResourceBuilder.aKeyStoreResource().withCertificate("metadataCA", CACertificates.TEST_METADATA_CA).withCertificate("rootCA", CACertificates.TEST_ROOT_CA).build();
    private static final KeyStoreResource spTrustStore = KeyStoreResourceBuilder.aKeyStoreResource().withCertificate("coreCA", CACertificates.TEST_CORE_CA).withCertificate("rootCA", CACertificates.TEST_ROOT_CA).build();
    private static final File STUB_IDPS_FILE = new File(System.getProperty("java.io.tmpdir"), "stub-idps.yml");

    private final List<StubIdp> stubIdps = new ArrayList<>();

    private static final HttpStubRule fakeFrontend = new HttpStubRule();
    private static final URI assertionConsumerServices = UriBuilder.fromUri("https://somedomain/destination").build();

    public StubIdpAppExtension() {
        this(Map.of());
    }

    public StubIdpAppExtension(Map<String, String> configOverrides) {
        super(StubIdpApplication.class, "../configuration/stub-idp.yml", withDefaultOverrides(configOverrides));
        fakeFrontend.register("/get-available-services", 200, "application/json", "[]");
    }

    private static ConfigOverride[] withDefaultOverrides(Map<String, String> configOverrides) {
        Map<String, String> config = Map.ofEntries(
                Map.entry("basicAuthEnabledForUserResource", "true"),
                Map.entry("isPrometheusEnabled", "false"),
                Map.entry("isHeadlessIdpEnabled", "false"),
                Map.entry("isIdpEnabled", "true"),
                Map.entry("dynamicReloadOfStubIdpYmlEnabled", "false"),
                Map.entry("server.requestLog.appenders[0].type", "console"),
                Map.entry("server.applicationConnectors[0].port", "0"),
                Map.entry("server.adminConnectors[0].port", "0"),
                Map.entry("logging.level", "WARN"),
                Map.entry("logging.appenders[0].type", "console"),
                Map.entry("stubIdpsYmlFileLocation", STUB_IDPS_FILE.getAbsolutePath()),
                Map.entry("signingKeyPairConfiguration.privateKeyConfiguration.type", "encoded"),
                Map.entry("signingKeyPairConfiguration.privateKeyConfiguration.key", STUB_IDP_PUBLIC_PRIMARY_PRIVATE_KEY),
                Map.entry("signingKeyPairConfiguration.publicKeyConfiguration.type", "x509"),
                Map.entry("signingKeyPairConfiguration.publicKeyConfiguration.cert", STUB_IDP_PUBLIC_PRIMARY_CERT),
                Map.entry("idpMetadataSigningKeyPairConfiguration.privateKeyConfiguration.type", "encoded"),
                Map.entry("idpMetadataSigningKeyPairConfiguration.privateKeyConfiguration.key", METADATA_SIGNING_A_PRIVATE_KEY),
                Map.entry("idpMetadataSigningKeyPairConfiguration.publicKeyConfiguration.type", "x509"),
                Map.entry("idpMetadataSigningKeyPairConfiguration.publicKeyConfiguration.cert", METADATA_SIGNING_A_PUBLIC_CERT),
                Map.entry("metadata.uri", "http://localhost:" + verifyMetadataServer.getPort() + VERIFY_METADATA_PATH),
                Map.entry("metadata.expectedEntityId", SP_ENTITY_ID),
                Map.entry("metadata.trustStore.store", metadataTrustStore.getAbsolutePath()),
                Map.entry("metadata.trustStore.password", metadataTrustStore.getPassword()),
                Map.entry("metadata.spTrustStore.store", spTrustStore.getAbsolutePath()),
                Map.entry("metadata.spTrustStore.password", spTrustStore.getPassword()),
                // FIXME: add port...
                Map.entry("saml.expectedDestination", "http://localhost:0"),
                Map.entry("europeanIdentity.enabled", "false"),
                Map.entry("europeanIdentity.hubConnectorEntityId", HUB_CONNECTOR_ENTITY_ID),
                Map.entry("europeanIdentity.stubCountryBaseUrl", "http://localhost:0"),
                Map.entry("europeanIdentity.metadata.uri", "http://localhost:" + eidasMetadataServer.getPort() + EIDAS_METADATA_PATH),
                Map.entry("europeanIdentity.metadata.expectedEntityId", HUB_CONNECTOR_ENTITY_ID),
                Map.entry("europeanIdentity.metadata.trustStore.store", metadataTrustStore.getAbsolutePath()),
                Map.entry("europeanIdentity.metadata.trustStore.password", metadataTrustStore.getPassword()),
                Map.entry("europeanIdentity.metadata.spTrustStore.store", spTrustStore.getAbsolutePath()),
                Map.entry("europeanIdentity.metadata.spTrustStore.password", spTrustStore.getPassword()),
                Map.entry("europeanIdentity.signingKeyPairConfiguration.privateKeyConfiguration.type", "encoded"),
                Map.entry("europeanIdentity.signingKeyPairConfiguration.privateKeyConfiguration.key", STUB_IDP_PUBLIC_PRIMARY_PRIVATE_KEY),
                Map.entry("europeanIdentity.signingKeyPairConfiguration.publicKeyConfiguration.type", "x509"),
                Map.entry("europeanIdentity.signingKeyPairConfiguration.publicKeyConfiguration.cert", STUB_IDP_PUBLIC_PRIMARY_CERT),
                Map.entry("database.url", "jdbc:h2:mem:"+ UUID.randomUUID().toString()+";MODE=PostgreSQL;DB_CLOSE_DELAY=-1"),
                Map.entry("singleIdpJourney.enabled", "false"),
                Map.entry("singleIdpJourney.serviceListUri", "http://localhost:"+fakeFrontend.getPort()+"/get-available-services"),
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

    @Override
    public void before() throws Exception {
        resetStaticMetrics(); // a hack to reset any prometheus metrics that are static as they will be re-used between test classes
        metadataTrustStore.create();
        spTrustStore.create();

        IdpStubsConfiguration idpStubsConfiguration = new TestIdpStubsConfiguration(stubIdps);
        try {
            Files.writeString(STUB_IDPS_FILE.toPath(), Jackson.newObjectMapper().writeValueAsString(idpStubsConfiguration), UTF_8);
            STUB_IDPS_FILE.deleteOnExit();

            InitializationService.initialize();

            verifyMetadataServer.reset();
            eidasMetadataServer.reset();
            verifyMetadataServer.register(VERIFY_METADATA_PATH, 200, Constants.APPLICATION_SAMLMETADATA_XML, getVerifyMetadata());
            eidasMetadataServer.register(EIDAS_METADATA_PATH, 200, Constants.APPLICATION_SAMLMETADATA_XML, getEidasMetadata());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        super.before();
    }

    private String getVerifyMetadata() throws MarshallingException, SignatureException {
        List<EntityDescriptor> entityDescriptors = new ArrayList<>();
        entityDescriptors.add(EntityDescriptorBuilder.anEntityDescriptor()
                .withEntityId(SP_ENTITY_ID)
                .withSpSsoDescriptor(SPSSODescriptorBuilder.anSpServiceDescriptor()
                        .withoutDefaultEncryptionKey()
                        .withoutDefaultSigningKey()
                        .addKeyDescriptor(KeyDescriptorBuilder.aKeyDescriptor().withX509ForEncryption(TestCertificateStrings.HUB_TEST_PUBLIC_ENCRYPTION_CERT).build())
                        .addKeyDescriptor(KeyDescriptorBuilder.aKeyDescriptor().withX509ForSigning(TestCertificateStrings.HUB_TEST_PUBLIC_SIGNING_CERT).build())
                        .addAssertionConsumerService(AssertionConsumerServiceBuilder.anAssertionConsumerService().withLocation(getAssertionConsumerServices().toASCIIString()).build()).build())
                .build());
        for(StubIdp stubIdp : stubIdps) {
            entityDescriptors.add(EntityDescriptorBuilder.anEntityDescriptor()
                    .withEntityId(String.format("http://stub_idp.acme.org/%s/SSO/POST", stubIdp.getFriendlyId()))
                    .withIdpSsoDescriptor(IdpSsoDescriptorBuilder.anIdpSsoDescriptor()
                            .addKeyDescriptor(KeyDescriptorBuilder.aKeyDescriptor().withX509ForSigning(STUB_IDP_PUBLIC_PRIMARY_CERT).build())
                            .withoutDefaultSigningKey()
                            .build()).build());
        }
        EntitiesDescriptor entitiesDescriptor = new EntitiesDescriptorFactory()
                .signedEntitiesDescriptor(entityDescriptors, METADATA_SIGNING_A_PUBLIC_CERT, METADATA_SIGNING_A_PRIVATE_KEY);
        return new MetadataFactory().metadata(entitiesDescriptor);
    }

    private String getEidasMetadata() throws MarshallingException, SignatureException {
        List<EntityDescriptor> entityDescriptorList = List.of(EntityDescriptorBuilder.anEntityDescriptor()
                .withEntityId(HUB_CONNECTOR_ENTITY_ID)
                .withSpSsoDescriptor(SPSSODescriptorBuilder.anSpServiceDescriptor()
                        .withoutDefaultEncryptionKey()
                        .withoutDefaultSigningKey()
                        .addKeyDescriptor(KeyDescriptorBuilder.aKeyDescriptor().withX509ForEncryption(TestCertificateStrings.HUB_CONNECTOR_TEST_PUBLIC_ENCRYPTION_CERT).build())
                        .addKeyDescriptor(KeyDescriptorBuilder.aKeyDescriptor().withX509ForSigning(TestCertificateStrings.HUB_CONNECTOR_TEST_PUBLIC_SIGNING_CERT).build())
                        .addAssertionConsumerService(AssertionConsumerServiceBuilder.anAssertionConsumerService().withLocation(getAssertionConsumerServices().toASCIIString()).build())
                        .build())
                .build());
        EntitiesDescriptor entitiesDescriptor = new EntitiesDescriptorFactory()
                .signedEntitiesDescriptor(entityDescriptorList, METADATA_SIGNING_A_PUBLIC_CERT, METADATA_SIGNING_A_PRIVATE_KEY);
        return new MetadataFactory().metadata(entitiesDescriptor);
    }

    @Override
    public void after() {
        metadataTrustStore.delete();
        spTrustStore.delete();
        STUB_IDPS_FILE.delete();

        super.after();
    }

    private void resetStaticMetrics() {
        List<Counter> countersToReset = List.of(AuthnRequestReceiverService.successfulEidasAuthnRequests,
                AuthnRequestReceiverService.successfulVerifyAuthnRequests,
                SuccessAuthnResponseService.sentVerifyAuthnResponses,
                EidasAuthnResponseService.sentEidasAuthnFailureResponses,
                EidasAuthnResponseService.sentEidasAuthnSuccessResponses,
                NonSuccessAuthnResponseService.sentVerifyAuthnFailureResponses,
                EidasAuthnRequestReceiverResource.receivedEidasAuthnRequests,
                IdpAuthnRequestReceiverResource.receivedVerifyAuthnRequests,
                InvalidAuthnRequestExceptionMapper.invalidVerifyAuthnRequests,
                InvalidEidasAuthnRequestExceptionMapper.invalidEidasAuthnRequests,
                HeadlessIdpResource.receivedHeadlessAuthnRequests,
                HeadlessIdpResource.successfulHeadlessAuthnRequests);
        // this wipes _all_ metrics from the app, which unfortunately means that
        // static metrics aren't re-initialised.
        CollectorRegistry.defaultRegistry.clear();
        try {
            CollectorRegistry.defaultRegistry.unregister(BaseAuthnRequestValidator.replayCacheCollector);
        } catch(NullPointerException e) {
            // unregistering may fail in this way but if it does we can continue
        }
        BaseAuthnRequestValidator.replayCacheCollector.register();
        countersToReset.forEach(c -> {
            c.clear();
            CollectorRegistry.defaultRegistry.register(c);
        });
    }

    public StubIdpAppExtension withStubIdp(StubIdp stubIdp) {
        this.stubIdps.add(stubIdp);
        return this;
    }

    public URI getVerifyMetadataPath() {
        return URI.create("http://localhost:" + verifyMetadataServer.getPort() + VERIFY_METADATA_PATH);
    }

    public URI getEidasMetadataPath() {
        return URI.create("http://localhost:" + eidasMetadataServer.getPort() + EIDAS_METADATA_PATH);
    }

    public URI getAssertionConsumerServices() {
        return assertionConsumerServices;
    }

    private static class TestIdpStubsConfiguration extends IdpStubsConfiguration {
        TestIdpStubsConfiguration(List<StubIdp> idps) {
            this.stubIdps = idps;
        }
    }

    public IdaKeyStore getHubKeyStore() {
        PrivateKey privateKey = new PrivateKeyFactory().createPrivateKey(Base64.getDecoder().decode(TestCertificateStrings.HUB_TEST_PRIVATE_ENCRYPTION_KEY));

        PublicKey publicKey = new PublicKeyFactory(new X509CertificateFactory()).createPublicKey(TestCertificateStrings.HUB_TEST_PUBLIC_ENCRYPTION_CERT);

        List<KeyPair> encryptionKeys = List.of(new KeyPair(publicKey, privateKey));
        return new IdaKeyStore(null, encryptionKeys);
    }

    public IdaKeyStore getEidasKeyStore() {
        PrivateKey privateKey = new PrivateKeyFactory().createPrivateKey(Base64.getDecoder().decode(TestCertificateStrings.HUB_CONNECTOR_TEST_PRIVATE_ENCRYPTION_KEY));

        PublicKey publicKey = new PublicKeyFactory(new X509CertificateFactory()).createPublicKey(TestCertificateStrings.HUB_CONNECTOR_TEST_PUBLIC_SIGNING_CERT);

        List<KeyPair> encryptionKeys = List.of(new KeyPair(publicKey, privateKey));
        return new IdaKeyStore(null, encryptionKeys);
    }

}
