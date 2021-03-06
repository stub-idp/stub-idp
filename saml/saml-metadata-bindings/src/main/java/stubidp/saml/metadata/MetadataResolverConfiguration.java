package stubidp.saml.metadata;

import io.dropwizard.client.JerseyClientConfiguration;

import java.net.URI;
import java.security.KeyStore;
import java.time.Duration;
import java.util.Optional;

public interface MetadataResolverConfiguration {

    KeyStore getTrustStore();

    default Optional<KeyStore> getSpTrustStore() {
        return Optional.empty();
    }

    default Optional<KeyStore> getIdpTrustStore() {
        return Optional.empty();
    }

    URI getUri();

    Duration getMinRefreshDelay();

    Duration getMaxRefreshDelay();

    String getExpectedEntityId();

    JerseyClientConfiguration getJerseyClientConfiguration();

    String getJerseyClientName();

    String getHubFederationId();
}
