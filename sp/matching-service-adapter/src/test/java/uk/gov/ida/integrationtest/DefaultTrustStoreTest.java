package uk.gov.ida.integrationtest;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import stubidp.saml.metadata.EidasMetadataConfiguration;
import stubidp.saml.metadata.MetadataResolverConfiguration;
import uk.gov.ida.integrationtest.helpers.MatchingServiceAdapterAppExtension;

import java.net.URI;
import java.security.KeyStoreException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.matchingserviceadapter.configuration.ConfigurationConstants.INTEGRATION_EIDAS_METADATA_SOURCE_URI;
import static uk.gov.ida.matchingserviceadapter.configuration.ConfigurationConstants.INTEGRATION_TRUST_ANCHOR_URI;
import static uk.gov.ida.matchingserviceadapter.configuration.ConfigurationConstants.PRODUCTION_EIDAS_METADATA_SOURCE_URI;
import static uk.gov.ida.matchingserviceadapter.configuration.ConfigurationConstants.PRODUCTION_TRUST_ANCHOR_URI;

@ExtendWith(DropwizardExtensionsSupport.class)
public class DefaultTrustStoreTest {

    public static final MatchingServiceAdapterAppExtension prodMsaWithDefaultTruststoresApplicationRule = new MatchingServiceAdapterAppExtension(
            true,
            "verify-matching-service-adapter-default-truststores.yml",
            false,
            Map.of("metadata.environment", "PRODUCTION",
                    "europeanIdentity.aggregatedMetadata.environment", "PRODUCTION")
    );

    public static final MatchingServiceAdapterAppExtension integrationMsaWithDefaultTruststoresApplicationRule = new MatchingServiceAdapterAppExtension(
            true,
            "verify-matching-service-adapter-default-truststores.yml",
            false,
            Map.of("metadata.environment", "INTEGRATION",
                    "europeanIdentity.aggregatedMetadata.environment", "INTEGRATION")
    );

    @Test
    public void prodTruststoresShouldContainCorrectCertificates() throws KeyStoreException {
        MetadataResolverConfiguration metadataConfiguration = prodMsaWithDefaultTruststoresApplicationRule
                .getConfiguration()
                .getMetadataConfiguration()
                .orElseThrow(
                        () -> new RuntimeException("No metadata configuration found")
                );
        List<String> hubAliases = Collections.list(metadataConfiguration
                .getSpTrustStore()
                .orElseThrow(
                        () -> new RuntimeException("No hub trust store found")
                ).aliases()
        );
        List<String> idpAliases = Collections.list(metadataConfiguration
                .getIdpTrustStore()
                .orElseThrow(
                        () -> new RuntimeException("No IDP trust store found")
                ).aliases()
        );
        List<String> metadataAliases = Collections.list(metadataConfiguration
                .getTrustStore()
                .aliases()
        );

        EidasMetadataConfiguration eidasaMetadataConfiguration = prodMsaWithDefaultTruststoresApplicationRule
                .getConfiguration()
                .getEuropeanIdentity()
                .getAggregatedMetadata();
        List<String> eidasAliases = Collections.list(eidasaMetadataConfiguration
                .getTrustStore()
                .aliases()
        );
        URI trustAnchorUri = eidasaMetadataConfiguration.getTrustAnchorUri();
        URI metadataSourceUri = eidasaMetadataConfiguration.getMetadataSourceUri();

        assertThat(hubAliases).containsExactlyInAnyOrder(
                "root-ca",
                "hub-ca",
                "root-ca-g3",
                "core-ca-g3"
        );
        assertThat(idpAliases).containsExactlyInAnyOrder(
                "root-ca",
                "idp-ca",
                "root-ca-g3",
                "idp-ca-g3"
        );
        assertThat(metadataAliases).containsExactlyInAnyOrder(
                "root-ca",
                "metadata-ca",
                "core-ca-g2",
                "root-ca-g3",
                "metadata-ca-g3",
                "core-ca-g3"
        );
        assertThat(eidasAliases).containsExactlyInAnyOrder(
                "root-ca",
                "metadata-ca",
                "core-ca-g2",
                "root-ca-g3",
                "metadata-ca-g3",
                "core-ca-g3"
        );
        assertThat(trustAnchorUri).isEqualTo(PRODUCTION_TRUST_ANCHOR_URI);
        assertThat(metadataSourceUri).isEqualTo(PRODUCTION_EIDAS_METADATA_SOURCE_URI);

    }

    @Test
    public void integrationTruststoresShouldContainCorrectCertificates() throws KeyStoreException {
        MetadataResolverConfiguration metadataConfiguration = integrationMsaWithDefaultTruststoresApplicationRule
                .getConfiguration()
                .getMetadataConfiguration()
                .orElseThrow(
                        () -> new RuntimeException("No metadata configuration found")
                );
        List<String> hubAliases = Collections.list(metadataConfiguration
                .getSpTrustStore()
                .orElseThrow(
                        () -> new RuntimeException("No hub trust store found")
                ).aliases()
        );
        List<String> idpAliases = Collections.list(metadataConfiguration
                .getIdpTrustStore()
                .orElseThrow(
                        () -> new RuntimeException("No IDP trust store found")
                ).aliases()
        );
        List<String> metadataAliases = Collections.list(metadataConfiguration
                .getTrustStore()
                .aliases()
        );

        EidasMetadataConfiguration eidasaMetadataConfiguration = integrationMsaWithDefaultTruststoresApplicationRule
                .getConfiguration()
                .getEuropeanIdentity()
                .getAggregatedMetadata();
        List<String> eidasAliases = Collections.list(eidasaMetadataConfiguration
                .getTrustStore()
                .aliases()
        );
        URI trustAnchorUri = eidasaMetadataConfiguration.getTrustAnchorUri();
        URI metadataSourceUri = eidasaMetadataConfiguration.getMetadataSourceUri();

        assertThat(hubAliases).containsExactlyInAnyOrder(
                "test-root-ca",
                "test-hub-old-ca",
                "test-hub-ca",
                "test-root-ca-g3",
                "test-core-ca-g3",
                "test-dev-pki-core-ca",
                "test-dev-pki-root-ca"
        );
        assertThat(idpAliases).containsExactlyInAnyOrder(
                "test-root-ca",
                "test-idp-ca",
                "test-root-ca-g3",
                "test-idp-ca-g3",
                "test-dev-pki-root-ca",
                "test-dev-pki-intermediary-ca"
        );
        assertThat(metadataAliases).containsExactlyInAnyOrder(
                "test-root-ca",
                "core-test-ca",
                "test-ca",
                "metadata-ca",
                "test-root-ca-g3",
                "test-core-ca-g3",
                "test-idp-ca-g3",
                "test-metadata-ca-g3",
                "test-dev-pki-root-ca",
                "test-dev-pki-core-ca",
                "test-dev-pki-intermediary-ca",
                "test-dev-pki-metadata-ca"
        );
        assertThat(eidasAliases).containsExactlyInAnyOrder(
                "test-root-ca",
                "core-test-ca",
                "test-ca",
                "metadata-ca",
                "test-root-ca-g3",
                "test-core-ca-g3",
                "test-idp-ca-g3",
                "test-metadata-ca-g3",
                "test-dev-pki-root-ca",
                "test-dev-pki-core-ca",
                "test-dev-pki-intermediary-ca",
                "test-dev-pki-metadata-ca"
        );
        assertThat(trustAnchorUri).isEqualTo(INTEGRATION_TRUST_ANCHOR_URI);
        assertThat(metadataSourceUri).isEqualTo(INTEGRATION_EIDAS_METADATA_SOURCE_URI);
    }
}
