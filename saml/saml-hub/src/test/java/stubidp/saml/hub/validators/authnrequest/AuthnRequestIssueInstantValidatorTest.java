package stubidp.saml.hub.validators.authnrequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import stubidp.saml.hub.configuration.SamlAuthnRequestValidityDurationConfiguration;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

class AuthnRequestIssueInstantValidatorTest {

    private AuthnRequestIssueInstantValidator authnRequestIssueInstantValidator;
    private static final int AUTHN_REQUEST_VALIDITY_MINS = 5;
    private final Clock clock = Clock.fixed(Instant.now(), ZoneId.of("UTC"));

    @BeforeEach
    void setup() {
        SamlAuthnRequestValidityDurationConfiguration samlAuthnRequestValidityDurationConfiguration = () -> Duration.ofMinutes(AUTHN_REQUEST_VALIDITY_MINS);
        authnRequestIssueInstantValidator = new AuthnRequestIssueInstantValidator(samlAuthnRequestValidityDurationConfiguration, clock);
    }

    @Test
    void validate_shouldReturnFalseIfIssueInstantMoreThan5MinutesAgo() {
        Instant issueInstant = Instant.now(clock).atZone(ZoneId.of("UTC")).minusMinutes(AUTHN_REQUEST_VALIDITY_MINS).minusSeconds(1).toInstant();
        boolean validity = authnRequestIssueInstantValidator.isValid(issueInstant);
        assertThat(validity).isEqualTo(false);
    }

    @Test
    void validate_shouldReturnTrueIfIssueInstant5MinsAgo() {
        Instant issueInstant = Instant.now(clock).atZone(ZoneId.of("UTC")).minusMinutes(AUTHN_REQUEST_VALIDITY_MINS).toInstant();
        boolean validity = authnRequestIssueInstantValidator.isValid(issueInstant);
        assertThat(validity).isEqualTo(true);
    }

    @Test
    void validate_shouldReturnTrueIfIssueInstantLessThan5MinsAgo() {
        Instant issueInstant = Instant.now(clock).atZone(ZoneId.of("UTC")).minusMinutes(AUTHN_REQUEST_VALIDITY_MINS).plusSeconds(1).toInstant();
        boolean validity = authnRequestIssueInstantValidator.isValid(issueInstant);
        assertThat(validity).isEqualTo(true);
    }
}
