package stubidp.saml.test.builders;

import stubidp.saml.domain.assertions.AuthnContext;
import stubidp.saml.domain.assertions.FraudAuthnDetails;
import stubidp.saml.domain.assertions.IdentityProviderAuthnStatement;
import stubidp.saml.domain.assertions.IpAddress;

import java.util.Optional;

import static stubidp.saml.domain.assertions.IdentityProviderAuthnStatement.createIdentityProviderAuthnStatement;
import static stubidp.saml.domain.assertions.IdentityProviderAuthnStatement.createIdentityProviderFraudAuthnStatement;

public class IdentityProviderAuthnStatementBuilder {
    private Optional<FraudAuthnDetails> fraudAuthnDetails = Optional.empty();
    private AuthnContext authnContext = AuthnContext.LEVEL_1;
    private Optional<IpAddress> userIpAddress = Optional.of(new IpAddress("9.9.8.8"));

    private IdentityProviderAuthnStatementBuilder() {}

    public static IdentityProviderAuthnStatementBuilder anIdentityProviderAuthnStatement() {
        return new IdentityProviderAuthnStatementBuilder();
    }

    public IdentityProviderAuthnStatement build() {
        return fraudAuthnDetails.map(authnDetails -> createIdentityProviderFraudAuthnStatement(authnDetails, userIpAddress.orElse(null)))
                .orElseGet(() -> createIdentityProviderAuthnStatement(authnContext, userIpAddress.orElse(null)));
    }

    public IdentityProviderAuthnStatementBuilder withAuthnContext(AuthnContext authnContext) {
        this.authnContext = authnContext;
        return this;
    }

    public IdentityProviderAuthnStatementBuilder withFraudDetails(FraudAuthnDetails fraudDetails) {
        this.fraudAuthnDetails = Optional.ofNullable(fraudDetails);
        return this;
    }

    public IdentityProviderAuthnStatementBuilder withUserIpAddress(IpAddress userIpAddress) {
        this.userIpAddress = Optional.ofNullable(userIpAddress);
        return this;
    }
}
