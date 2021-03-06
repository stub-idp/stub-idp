package stubidp.saml.metadata;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.util.X509CertUtils;
import net.shibboleth.utilities.java.support.component.AbstractInitializableComponent;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import org.apache.commons.collections.CollectionUtils;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.xmlsec.signature.support.impl.ExplicitKeySignatureTrustEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stubidp.eidas.trustanchor.CountryTrustAnchor;
import stubidp.saml.metadata.factories.DropwizardMetadataResolverFactory;
import stubidp.saml.metadata.factories.MetadataSignatureTrustEngineFactory;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class EidasMetadataResolverRepository implements MetadataResolverRepository {

    private static final Logger log = LoggerFactory.getLogger(EidasMetadataResolverRepository.class);
    private final EidasTrustAnchorResolver trustAnchorResolver;
    private final DropwizardMetadataResolverFactory dropwizardMetadataResolverFactory;
    private final MetadataResolverConfigBuilder metadataResolverConfigBuilder;
    private Map<String, MetadataResolverContainer> metadataResolvers = Map.of();
    private List<JWK> trustAnchors = new ArrayList<>();
    private final EidasMetadataConfiguration eidasMetadataConfiguration;
    private final Timer timer;
    private final MetadataSignatureTrustEngineFactory metadataSignatureTrustEngineFactory;
    private Duration delayBeforeNextRefresh;
    private final Client client;

    @Inject
    public EidasMetadataResolverRepository(EidasTrustAnchorResolver trustAnchorResolver,
                                           EidasMetadataConfiguration eidasMetadataConfiguration,
                                           DropwizardMetadataResolverFactory dropwizardMetadataResolverFactory,
                                           Timer timer,
                                           MetadataSignatureTrustEngineFactory metadataSignatureTrustEngineFactory,
                                           MetadataResolverConfigBuilder metadataResolverConfigBuilder,
                                           Client client
    ) {
        this.timer = timer;
        this.trustAnchorResolver = trustAnchorResolver;
        this.eidasMetadataConfiguration = eidasMetadataConfiguration;
        this.dropwizardMetadataResolverFactory = dropwizardMetadataResolverFactory;
        this.metadataSignatureTrustEngineFactory = metadataSignatureTrustEngineFactory;
        this.metadataResolverConfigBuilder = metadataResolverConfigBuilder;
        this.client = client;
        setMinTrustAnchorRefreshDelay();
        refresh();
    }

    @Override
    public Optional<MetadataResolver> getMetadataResolver(String entityId) {
        return Optional.ofNullable(metadataResolvers.get(entityId))
                .map(MetadataResolverContainer::getMetadataResolver);
    }

    @Override
    public List<String> getResolverEntityIds() {
        return List.copyOf(metadataResolvers.keySet());
    }

    @Override
    public Optional<ExplicitKeySignatureTrustEngine> getSignatureTrustEngine(String entityId) {
        return Optional.ofNullable(metadataResolvers.get(entityId))
                .map(MetadataResolverContainer::getSignatureTrustEngine);
    }

    @Override
    public Map<String, MetadataResolver> getMetadataResolvers() {
        return metadataResolvers.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().getMetadataResolver()
                ));
    }

    @Override
    public List<String> getTrustAnchorsEntityIds() {
        return trustAnchors.stream()
                .map(JWK::getKeyID)
                .collect(toList());
    }

    private JWK getTrustAnchorFromKeyId(String keyId) {
        return trustAnchors.stream()
                .filter(e -> e.getKeyID().equals(keyId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Cannot find " + keyId + " in trust anchors"));
    }

    @Override
    public void refresh() {
        try {
            List<JWK> newTrustAnchors = trustAnchorResolver.getTrustAnchors();
            if (trustAnchorsAreDifferent(trustAnchors, newTrustAnchors)) {
                log.info("Trust anchors have changed. Refreshing metadata resolvers");
                trustAnchors = newTrustAnchors;
                setMaxTrustAnchorRefreshDelay();
                refreshMetadataResolvers();
            }
        } catch (Exception e) {
            log.error("Error fetching trust anchor or validating it", e);
            setMinTrustAnchorRefreshDelay();
        } finally {
            log.info("Scheduling refresh in " + delayBeforeNextRefresh + " ms");
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    refresh();
                }
            }, delayBeforeNextRefresh.toMillis());
        }
    }

    private void refreshMetadataResolvers() {
        List<String> trustAnchorsEntityIds = getTrustAnchorsEntityIds();
        Map<String, MetadataResolverContainer> newMetadataResolvers = new HashMap<>();

        trustAnchorsEntityIds.forEach(trustAnchorsEntityId -> {
            try {
                newMetadataResolvers.put(trustAnchorsEntityId, createMetadataResolverContainer(trustAnchorsEntityId));
            } catch (Exception e) {
                log.error("Error creating MetadataResolver for " + trustAnchorsEntityId, e);
            }
        });

        Map<String, MetadataResolverContainer> oldMetadataResolvers = this.metadataResolvers;
        this.metadataResolvers = newMetadataResolvers;

        oldMetadataResolvers.forEach((key, value) -> value.getMetadataResolver().destroy());
    }

    private MetadataResolverContainer createMetadataResolverContainer(String resolverToAddEntityId) throws CertificateException, ComponentInitializationException {
        JWK trustAnchor = getTrustAnchorFromKeyId(resolverToAddEntityId);

        Collection<String> errors = CountryTrustAnchor.findErrors(trustAnchor);

        if (!errors.isEmpty()) {
            throwExceptionIfCertificateExpiredMessagePresent(errors);
            throw new Error(String.format("Managed to generate an invalid anchor: %s", String.join(", ", errors)));
        }

        Instant metadataSigningCertExpiryDate = sortCertsByDate(trustAnchor).get(0).getNotAfter().toInstant();
        Instant nextRunTime = Instant.now().plus(delayBeforeNextRefresh);
        if (metadataSigningCertExpiryDate.isBefore(nextRunTime)) {
            setMinTrustAnchorRefreshDelay();
        }

        return createMetadataResolverContainer(trustAnchor);
    }

    @Override
    public List<X509Certificate> sortCertsByDate(JWK trustAnchor) {
        return trustAnchor.getX509CertChain().stream()
                .map(base64 -> X509CertUtils.parse(Base64.getMimeDecoder().decode(String.valueOf(base64))))
                .sorted(Comparator.comparing(X509Certificate::getNotAfter))
                .collect(toList());
    }

    private MetadataResolverContainer createMetadataResolverContainer(JWK trustAnchor) throws CertificateException, ComponentInitializationException {
        MetadataResolverConfiguration metadataResolverConfiguration = metadataResolverConfigBuilder.createMetadataResolverConfiguration(trustAnchor, eidasMetadataConfiguration);
        JerseyClientMetadataResolver metadataResolver = (JerseyClientMetadataResolver) dropwizardMetadataResolverFactory.createMetadataResolverWithClient(metadataResolverConfiguration, true, client);
        return new MetadataResolverContainer(metadataResolver, metadataSignatureTrustEngineFactory.createSignatureTrustEngine(metadataResolver));
    }

    private void setMinTrustAnchorRefreshDelay() {
        delayBeforeNextRefresh = eidasMetadataConfiguration.getTrustAnchorMinRefreshDelay();
    }

    private void setMaxTrustAnchorRefreshDelay() {
        delayBeforeNextRefresh = eidasMetadataConfiguration.getTrustAnchorMaxRefreshDelay();
    }

    private static class MetadataResolverContainer {
        private final JerseyClientMetadataResolver metadataResolver;
        private final ExplicitKeySignatureTrustEngine explicitKeySignatureTrustEngine;

        private MetadataResolverContainer(JerseyClientMetadataResolver metadataResolver,
                                          ExplicitKeySignatureTrustEngine explicitKeySignatureTrustEngine) {
            this.metadataResolver = metadataResolver;
            this.explicitKeySignatureTrustEngine = explicitKeySignatureTrustEngine;
        }

        private ExplicitKeySignatureTrustEngine getSignatureTrustEngine() {
            return explicitKeySignatureTrustEngine;
        }

        private JerseyClientMetadataResolver getMetadataResolver() {
            return metadataResolver;
        }
    }

    private void throwExceptionIfCertificateExpiredMessagePresent(Collection<String> errors) throws CertificateException {
        Optional<String> certExpiryErrorMessage = errors.stream()
            .filter(message -> message.contains("X.509 certificate has expired"))
            .findFirst();
        if (certExpiryErrorMessage.isPresent()) throw new CertificateException(certExpiryErrorMessage.get());
    }

    private boolean trustAnchorsAreDifferent(List<JWK> list1, List<JWK> list2) {
        return !CollectionUtils.isEqualCollection(list1, list2);
    }
}
