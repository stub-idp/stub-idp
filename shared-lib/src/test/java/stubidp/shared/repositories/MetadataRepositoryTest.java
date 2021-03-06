package stubidp.shared.repositories;

import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.resolver.ResolverException;
import net.shibboleth.utilities.java.support.xml.BasicParserPool;
import org.junit.jupiter.api.Test;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.saml.metadata.resolver.impl.FilesystemMetadataResolver;
import stubidp.saml.metadata.factories.CredentialResolverFactory;
import stubidp.test.devpki.TestCertificateStrings;
import stubidp.test.devpki.TestEntityIds;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.text.MessageFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

import static org.assertj.core.api.Assertions.assertThat;

class MetadataRepositoryTest {
    private static final DateTimeFormatter dateTimeFormatter = new DateTimeFormatterBuilder()
            .appendInstant(3) // ensure that 0 millis is printed, and does not get truncated
            .toFormatter()
            .withZone(ZoneId.of("UTC"));

    private static final String ENCRYPTION_CERTIFICATE = TestCertificateStrings.HUB_TEST_PUBLIC_ENCRYPTION_CERT;
    private static final String SIGNING_CERTIFICATE_1 = TestCertificateStrings.HUB_TEST_PUBLIC_SIGNING_CERT;
    private static final String SIGNING_CERTIFICATE_2 = TestCertificateStrings.TEST_RP_PUBLIC_SIGNING_CERT;
    private static final String LOCATION = "http://localhost:50190/SAML2/SSO/Response/POST";

    private static final String METADATA_PATTERN = """
            <?xml version="1.0" encoding="UTF-8"?>
            <md:EntitiesDescriptor xmlns:md="urn:oasis:names:tc:SAML:2.0:metadata" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" ID="_entities">
                <md:EntityDescriptor ID="_9efc2cf0-bca2-43c6-94a4-348a929515d4" entityID="https://signin.service.gov.uk" validUntil="{0}" xsi:type="md:EntityDescriptorType">
                    <md:SPSSODescriptor protocolSupportEnumeration="urn:oasis:names:tc:SAML:2.0:protocol" xsi:type="md:SPSSODescriptorType">
                        <md:KeyDescriptor use="signing" xsi:type="md:KeyDescriptorType">
                            <ds:KeyInfo xmlns:ds="http://www.w3.org/2000/09/xmldsig#" xsi:type="ds:KeyInfoType">
                                <ds:KeyName xmlns:xs="http://www.w3.org/2001/XMLSchema" xsi:type="xs:string">https://signin.service.gov.uk</ds:KeyName>
                                <ds:X509Data xsi:type="ds:X509DataType">
                                    <ds:X509Certificate>{2}</ds:X509Certificate>
                                </ds:X509Data>
                            </ds:KeyInfo>
                        </md:KeyDescriptor>
                        <md:KeyDescriptor use="signing" xsi:type="md:KeyDescriptorType">
                            <ds:KeyInfo xmlns:ds="http://www.w3.org/2000/09/xmldsig#" xsi:type="ds:KeyInfoType">
                                <ds:KeyName xmlns:xs="http://www.w3.org/2001/XMLSchema" xsi:type="xs:string">https://signin.service.gov.uk/another-key</ds:KeyName>
                                <ds:X509Data xsi:type="ds:X509DataType">
                                    <ds:X509Certificate>{3}</ds:X509Certificate>
                                </ds:X509Data>
                            </ds:KeyInfo>
                        </md:KeyDescriptor>
                        <md:KeyDescriptor use="encryption" xsi:type="md:KeyDescriptorType">
                            <ds:KeyInfo xmlns:ds="http://www.w3.org/2000/09/xmldsig#" xsi:type="ds:KeyInfoType">
                                <ds:KeyName xmlns:xs="http://www.w3.org/2001/XMLSchema" xsi:type="xs:string">https://signin.service.gov.uk</ds:KeyName>
                                <ds:X509Data xsi:type="ds:X509DataType">
                                    <ds:X509Certificate>{1}</ds:X509Certificate>
                                </ds:X509Data>
                            </ds:KeyInfo>
                        </md:KeyDescriptor>
                        <md:AssertionConsumerService Binding="urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST" Location="{4}" index="1" isDefault="true" xsi:type="md:IndexedEndpointType"/>
                    </md:SPSSODescriptor>
                    <md:Organization xsi:type="md:OrganizationType">
                        <md:OrganizationName xml:lang="en-GB" xsi:type="md:localizedNameType">GOV.UK</md:OrganizationName>
                        <md:OrganizationDisplayName xml:lang="en-GB" xsi:type="md:localizedNameType">GOV.UK</md:OrganizationDisplayName>
                        <md:OrganizationURL xml:lang="en-GB" xsi:type="md:localizedURIType">https://www.gov.uk</md:OrganizationURL>
                    </md:Organization>
                </md:EntityDescriptor>
            </md:EntitiesDescriptor>
            """;

    private static final String METADATA_WITHOUT_HUB_PATTERN = """
            <?xml version="1.0" encoding="UTF-8"?>
            <md:EntitiesDescriptor xmlns:md="urn:oasis:names:tc:SAML:2.0:metadata" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" ID="_entities">
            </md:EntitiesDescriptor>
            """;

    private static final String DEFAULT_METADATA = MessageFormat.format(METADATA_PATTERN, dateTimeFormatter.format(Instant.now().atZone(ZoneId.of("UTC")).plusHours(1)), ENCRYPTION_CERTIFICATE, SIGNING_CERTIFICATE_1, SIGNING_CERTIFICATE_2, LOCATION);
    private static final String METADATA_WITHUOUT_HUB = MessageFormat.format(METADATA_WITHOUT_HUB_PATTERN, dateTimeFormatter.format(Instant.now().atZone(ZoneId.of("UTC")).plusHours(1)));

    private MetadataRepository metadataRepository;

    @Test
    void shouldReturnTheAssertionConsumerService() throws Exception {
        metadataRepository = initializeMetadata(DEFAULT_METADATA);

        assertThat(metadataRepository.getAssertionConsumerServiceLocation()).isEqualTo(URI.create(LOCATION));
    }
    @Test
    void shouldReturnTheEncryptionCertificateMetadata() throws Exception {
        metadataRepository = initializeMetadata(DEFAULT_METADATA);

        assertThat(metadataRepository.getEncryptionCertificate()).isEqualTo(ENCRYPTION_CERTIFICATE);
    }

    @Test
    void shouldReturnTheSigningCertificates() throws Exception {
        metadataRepository = initializeMetadata(DEFAULT_METADATA);

        Iterable<String> signingCertificates = metadataRepository.getSigningCertificates();
        assertThat(signingCertificates).containsExactlyInAnyOrder(SIGNING_CERTIFICATE_1, SIGNING_CERTIFICATE_2);
    }

    @Test
    void shouldReturnNoCertificatesIfMetadataIsOld() throws Exception {
        String expiredDateTime = dateTimeFormatter.format(Instant.now().atZone(ZoneId.of("UTC")).minusYears(100));
        metadataRepository = initializeMetadata(MessageFormat.format(METADATA_PATTERN, expiredDateTime, ENCRYPTION_CERTIFICATE, SIGNING_CERTIFICATE_1, SIGNING_CERTIFICATE_2, LOCATION));
        assertThat(metadataRepository.getSigningCertificates()).isEmpty();
    }

    @Test
    void shouldReturnNoCertificatesIfMetadataIsMissingHubEntityDescriptor() throws Exception {
        metadataRepository = initializeMetadata(METADATA_WITHUOUT_HUB);
        assertThat(metadataRepository.getSigningCertificates()).isEmpty();
    }

    private MetadataRepository initializeMetadata(String metadata) throws IOException, InitializationException, ComponentInitializationException, ResolverException {
        File metadataFile = File.createTempFile("metadata", ".xml");
        FileWriter fileWriter = new FileWriter(metadataFile);
        fileWriter.write(metadata);
        fileWriter.flush();
        InitializationService.initialize();
        FilesystemMetadataResolver filesystemMetadataResolver = new FilesystemMetadataResolver(metadataFile);
        BasicParserPool pool = new BasicParserPool();
        pool.initialize();
        filesystemMetadataResolver.setParserPool(pool);
        filesystemMetadataResolver.setRequireValidMetadata(true);
        filesystemMetadataResolver.setId("some id");
        filesystemMetadataResolver.initialize();
        return new MetadataRepository(new CredentialResolverFactory().create(filesystemMetadataResolver), TestEntityIds.HUB_ENTITY_ID);
    }

}
