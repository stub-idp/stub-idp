package stubidp.utils.security.security;

import io.prometheus.client.Gauge;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

/**
 * <p>A class to register prometheus metrics for expiry dates of certificates in truststores.</p>
 * <p>The metrics will be in the form:</p>
 * <pre>
 * verify_trust_store_certificate_expiry_date{truststore="$name",subject="$subject_dn",serial="$serial"}
 * </pre>
 */
public class TrustStoreMetrics {
    private final Gauge expiryDateGauge;

    /**
     * Initialises the TrustStoreMetrics gauge with a reference to a different gauge
     * @param gauge
     */
    public TrustStoreMetrics(Gauge gauge) {
        expiryDateGauge = gauge;
    }

    /**
     * Create a new TrustStoreMetrics.  This will automatically register metrics with the default Prometheus
     * CollectorRegistry.
     * @param gaugeName
     *  The name of the Prometheus Label to be used
     */
    public TrustStoreMetrics() {
        this("verify_trust_store_certificate_expiry_date");
    }

    public TrustStoreMetrics(String gaugeName) {
        expiryDateGauge = Gauge.build(gaugeName, "Expiry date (in unix time milliseconds) of a certificate in a Java truststore")
                .labelNames("truststore", "subject", "serial")
                .register();
    }

    /**
     * Captures metrics for the certificates in a truststore.
     * @param name
     *   A friendly name for the truststore. This will be set as the <code>truststore</code> label on the metric
     * @param trustStore
     *   The truststore containing certificates to output metrics for
     */
    public void registerTrustStore(String name, KeyStore trustStore) {
        try {
            Enumeration<String> aliases = trustStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                X509Certificate certificate = (X509Certificate) trustStore.getCertificate(alias);
                expiryDateGauge.labels(name, certificate.getSubjectX500Principal().getName(), certificate.getSerialNumber().toString(10))
                        .set(certificate.getNotAfter().getTime());
            }
        } catch (KeyStoreException e) {
            throw new RuntimeException(e);
        }
    }
}