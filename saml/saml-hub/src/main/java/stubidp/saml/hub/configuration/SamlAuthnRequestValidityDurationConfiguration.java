package stubidp.saml.hub.configuration;

import java.time.Duration;

public interface SamlAuthnRequestValidityDurationConfiguration {
    Duration getAuthnRequestValidityDuration();
}
