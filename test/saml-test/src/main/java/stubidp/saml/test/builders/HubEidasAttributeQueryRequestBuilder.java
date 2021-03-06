package stubidp.saml.test.builders;

import stubidp.saml.domain.assertions.AuthnContext;
import stubidp.saml.domain.assertions.HubAssertion;
import stubidp.saml.domain.matching.HubEidasAttributeQueryRequest;
import stubidp.saml.domain.assertions.PersistentId;
import stubidp.saml.domain.assertions.UserAccountCreationAttribute;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HubEidasAttributeQueryRequestBuilder {
    private String id = "id";
    private PersistentId persistentId = new PersistentId("default-name-id");
    private Optional<HubAssertion> cycle3AttributeAssertion = Optional.empty();
    private Optional<List<UserAccountCreationAttribute>> userAccountCreationAttributes = Optional.empty();
    private URI assertionConsumerServiceUrl = URI.create("http://transaction.local");
    private String authnRequestIssuerEntityId = "issuer-id";
    private AuthnContext authnContext = AuthnContext.LEVEL_1;
    private String encryptedIdentityAssertion = "encryptedIdentityAssertion";
    private final String hubEidasEntityId = "hubEntityId";

    private HubEidasAttributeQueryRequestBuilder() {}

    public static HubEidasAttributeQueryRequestBuilder aHubEidasAttributeQueryRequest() {
        return new HubEidasAttributeQueryRequestBuilder();
    }

    public HubEidasAttributeQueryRequest build() {
        return new HubEidasAttributeQueryRequest(
                id,
                hubEidasEntityId,
                Instant.now(),
                persistentId,
                assertionConsumerServiceUrl,
                authnRequestIssuerEntityId,
                encryptedIdentityAssertion,
                authnContext,
                cycle3AttributeAssertion,
                userAccountCreationAttributes
        );
    }

    public HubEidasAttributeQueryRequestBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public HubEidasAttributeQueryRequestBuilder withPersistentId(PersistentId persistentId) {
        this.persistentId = persistentId;
        return this;
    }

    public HubEidasAttributeQueryRequestBuilder withEncryptedIdentityAssertion(String assertion) {
        this.encryptedIdentityAssertion = assertion;
        return this;
    }

    public HubEidasAttributeQueryRequestBuilder withCycle3DataAssertion(HubAssertion cycle3DataAssertion) {
        this.cycle3AttributeAssertion = Optional.ofNullable(cycle3DataAssertion);
        return this;
    }

    public HubEidasAttributeQueryRequestBuilder withAssertionConsumerServiceUrl(URI assertionConsumerServiceUrl) {
        this.assertionConsumerServiceUrl = assertionConsumerServiceUrl;
        return this;
    }

    public HubEidasAttributeQueryRequestBuilder withAuthnRequestIssuerEntityId(String requestIssuer) {
        this.authnRequestIssuerEntityId = requestIssuer;
        return this;
    }

    public HubEidasAttributeQueryRequestBuilder withAuthnContext(AuthnContext authnContext) {
        this.authnContext = authnContext;
        return this;
    }

    public HubEidasAttributeQueryRequestBuilder addUserAccountCreationAttribute(final UserAccountCreationAttribute userAccountCreationAttribute) {
        if(userAccountCreationAttributes.isEmpty()){
           List<UserAccountCreationAttribute> userAccountCreationAttributeList = new ArrayList<>();
           userAccountCreationAttributes = Optional.of(userAccountCreationAttributeList);
        }
        this.userAccountCreationAttributes.get().add(userAccountCreationAttribute);
        return this;
    }

    public HubEidasAttributeQueryRequestBuilder withoutUserAccountCreationAttributes() {
        userAccountCreationAttributes = Optional.empty();
        return this;
    }
}
