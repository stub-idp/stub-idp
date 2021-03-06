package uk.gov.ida.matchingserviceadapter.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.client.JerseyClientConfiguration;
import stubidp.saml.metadata.EidasMetadataConfiguration;
import stubidp.saml.metadata.TrustStoreConfiguration;

import java.net.URI;
import java.security.KeyStore;
import java.time.Duration;

public class MatchingServiceAdapterEidasMetadataConfiguration extends EidasMetadataConfiguration {
    private final TrustStoreConfiguration trustStore;
    private final MatchingServiceAdapterEnvironment environment;

    @JsonCreator
    public MatchingServiceAdapterEidasMetadataConfiguration(
            @JsonProperty("trustAnchorUri") URI trustAnchorUri,
            @JsonProperty("minRefreshDelay") Duration minRefreshDelay,
            @JsonProperty("maxRefreshDelay") Duration maxRefreshDelay,
            @JsonProperty("trustAnchorMaxRefreshDelay") Duration trustAnchorMaxRefreshDelay,
            @JsonProperty("trustAnchorMinRefreshDelay") Duration trustAnchorMinRefreshDelay,
            @JsonProperty("client") JerseyClientConfiguration client,
            @JsonProperty("jerseyClientName") String jerseyClientName,
            @JsonProperty("trustStore") TrustStoreConfiguration trustStore,
            @JsonProperty("metadataSourceUri") URI metadataSourceUri,
            @JsonProperty("environment") MatchingServiceAdapterEnvironment environment) {
        super(trustAnchorUri, minRefreshDelay, maxRefreshDelay, trustAnchorMaxRefreshDelay, trustAnchorMinRefreshDelay, client, jerseyClientName, trustStore, metadataSourceUri);

        this.trustStore = trustStore;
        this.environment = environment;
    }

    @Override
    public KeyStore getTrustStore() {
        return trustStore == null ?
                new DefaultTrustStoreConfiguration(environment.getTrustStoreName(TrustStoreType.METADATA)).getTrustStore() :
                super.getTrustStore();
    }

    @Override
    public URI getTrustAnchorUri() {
        URI trustAnchorUri = super.getTrustAnchorUri();
        return trustAnchorUri == null ?
                environment.getTrustAnchorUri() :
                trustAnchorUri;
    }

    @Override
    public URI getMetadataSourceUri() {
        URI metadataSourceUri = super.getMetadataSourceUri();
        return metadataSourceUri == null ?
                environment.getMetadataSourceUri() :
                metadataSourceUri;
    }
}
