package stubidp.stubidp.domain.factories;

import stubidp.saml.domain.assertions.AssertionRestrictions;
import stubidp.saml.domain.assertions.IdentityProviderAssertion;
import stubidp.saml.domain.assertions.IdentityProviderAuthnStatement;
import stubidp.saml.domain.assertions.MatchingDataset;
import stubidp.saml.domain.assertions.PersistentId;
import stubidp.utils.security.security.IdGenerator;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Optional;

public class IdentityProviderAssertionFactory {

    private final IdGenerator idGenerator;

    @Inject
    public IdentityProviderAssertionFactory(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    public IdentityProviderAssertion createMatchingDatasetAssertion(
            PersistentId persistentId,
            String issuerId,
            MatchingDataset matchingDataset,
            AssertionRestrictions assertionRestrictions) {

        return new IdentityProviderAssertion(
                idGenerator.getId(),
                issuerId,
                Instant.now(),
                persistentId,
                assertionRestrictions,
                Optional.ofNullable(matchingDataset),
                Optional.empty());
    }

    public IdentityProviderAssertion createAuthnStatementAssertion(
            PersistentId persistentId,
            String issuerId,
            IdentityProviderAuthnStatement idaAuthnStatement,
            AssertionRestrictions assertionRestrictions) {

        return new IdentityProviderAssertion(
                idGenerator.getId(),
                issuerId,
                Instant.now(),
                persistentId,
                assertionRestrictions,
                Optional.empty(),
                Optional.ofNullable(idaAuthnStatement));
    }
}
