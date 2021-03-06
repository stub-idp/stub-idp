package uk.gov.ida.matchingserviceadapter.builders;

import stubidp.saml.domain.assertions.AssertionRestrictions;
import stubidp.saml.domain.assertions.IdentityProviderAssertion;
import stubidp.saml.domain.assertions.IdentityProviderAuthnStatement;
import stubidp.saml.domain.assertions.MatchingDataset;
import stubidp.saml.domain.assertions.PersistentId;
import stubidp.saml.test.builders.AssertionRestrictionsBuilder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public class IdentityProviderAssertionBuilder {

    private String id = "assertion-id" + UUID.randomUUID();
    private String issuerId = "assertion issuer id";
    private Instant issueInstant = Instant.now();
    private PersistentId persistentId = PersistentIdBuilder.aPersistentId().build();
    private AssertionRestrictions assertionRestrictions = AssertionRestrictionsBuilder.anAssertionRestrictions().build();
    private Optional<MatchingDataset> matchingDataset = Optional.empty();
    private Optional<IdentityProviderAuthnStatement> authnStatement = Optional.empty();

    public static IdentityProviderAssertionBuilder anIdentityProviderAssertion() {
        return new IdentityProviderAssertionBuilder();
    }

    public IdentityProviderAssertion build() {
        return new IdentityProviderAssertion(
                id,
                issuerId,
                issueInstant,
                persistentId,
                assertionRestrictions,
                matchingDataset,
                authnStatement);
    }

    public IdentityProviderAssertionBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public IdentityProviderAssertionBuilder withPersistentId(PersistentId persistentId) {
        this.persistentId = persistentId;
        return this;
    }

    public IdentityProviderAssertionBuilder withAuthnStatement(IdentityProviderAuthnStatement idaAuthnStatement) {
        this.authnStatement = Optional.of(idaAuthnStatement);
        return this;
    }

    public IdentityProviderAssertionBuilder withMatchingDataset(MatchingDataset matchingDataset) {
        this.matchingDataset = Optional.of(matchingDataset);
        return this;
    }

    public IdentityProviderAssertionBuilder withIssuerId(String issuerId) {
        this.issuerId = issuerId;
        return this;
    }
}
