package uk.gov.ida.verifyserviceprovider.services;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnContext;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnStatement;
import stubidp.saml.domain.assertions.MatchingDataset;
import stubidp.saml.domain.matching.assertions.NonMatchingAttributes;
import stubidp.saml.utils.core.transformers.MatchingDatasetToNonMatchingAttributesMapper;
import stubidp.saml.utils.core.transformers.MatchingDatasetUnmarshaller;
import stubidp.saml.utils.core.validation.SamlResponseValidationException;
import stubidp.saml.utils.hub.factories.UserIdHashFactory;
import uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance;
import uk.gov.ida.verifyserviceprovider.dto.TranslatedNonMatchingResponseBody;
import uk.gov.ida.verifyserviceprovider.dto.TranslatedResponseBody;
import uk.gov.ida.verifyserviceprovider.validators.SubjectValidator;

import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.ida.verifyserviceprovider.dto.NonMatchingScenario.IDENTITY_VERIFIED;

public abstract class IdentityAssertionTranslator implements AssertionTranslator {

    private final MatchingDatasetUnmarshaller matchingDatasetUnmarshaller;
    private final MatchingDatasetToNonMatchingAttributesMapper mdsMapper;

    protected final UserIdHashFactory userIdHashFactory;

    final SubjectValidator subjectValidator;

    IdentityAssertionTranslator(
            UserIdHashFactory userIdHashFactory,
            SubjectValidator subjectValidator,
            MatchingDatasetUnmarshaller matchingDatasetUnmarshaller,
            MatchingDatasetToNonMatchingAttributesMapper mdsMapper) {
        this.userIdHashFactory = userIdHashFactory;
        this.subjectValidator = subjectValidator;
        this.matchingDatasetUnmarshaller = matchingDatasetUnmarshaller;
        this.mdsMapper = mdsMapper;
    }

    abstract public TranslatedResponseBody translateSuccessResponse(
            List<Assertion> assertions,
            String expectedInResponseTo,
            LevelOfAssurance expectedLevelOfAssurance,
            String entityId
    );

    TranslatedNonMatchingResponseBody translateAssertion(
            Assertion assertion,
            LevelOfAssurance levelOfAssurance,
            Optional<stubidp.saml.domain.assertions.AuthnContext> authnContext) {
        final String nameID = getNameIdFrom(assertion);
        final String issuerID = assertion.getIssuer().getValue();
        final String hashId = userIdHashFactory.hashId(issuerID, nameID, authnContext);
        final NonMatchingAttributes attributes = translateAttributes(assertion);

        return new TranslatedNonMatchingResponseBody(IDENTITY_VERIFIED, hashId, levelOfAssurance, attributes);
    }

    String extractLevelOfAssuranceUriFrom(Assertion assertion) {
        AuthnStatement authnStatement = getAuthnStatementFrom(assertion);
        return ofNullable(authnStatement.getAuthnContext())
                .map(AuthnContext::getAuthnContextClassRef)
                .map(AuthnContextClassRef::getURI)
                .orElseThrow(() -> new SamlResponseValidationException("Expected a level of assurance."));
    }

    private NonMatchingAttributes translateAttributes(Assertion mdsAssertion) {
        MatchingDataset matchingDataset = matchingDatasetUnmarshaller.fromAssertion(mdsAssertion);
        return mdsMapper.mapToNonMatchingAttributes(matchingDataset);
    }

    private String getNameIdFrom(Assertion assertion) {
        return assertion.getSubject().getNameID().getValue();
    }

    private AuthnStatement getAuthnStatementFrom(Assertion assertion) {
        return assertion.getAuthnStatements().get(0);
    }
}
