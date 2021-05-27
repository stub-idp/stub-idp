package uk.gov.ida.matchingserviceadapter.services;

import com.google.inject.name.Named;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import uk.gov.ida.matchingserviceadapter.domain.AssertionData;
import uk.gov.ida.matchingserviceadapter.validators.CountryConditionsValidator;
import uk.gov.ida.matchingserviceadapter.validators.InstantValidator;
import uk.gov.ida.matchingserviceadapter.validators.SubjectValidator;
import uk.gov.ida.saml.core.IdaConstants;
import uk.gov.ida.saml.core.transformers.AuthnContextFactory;
import uk.gov.ida.saml.core.transformers.EidasMatchingDatasetUnmarshaller;
import uk.gov.ida.saml.core.transformers.EidasUnsignedMatchingDatasetUnmarshaller;
import uk.gov.ida.saml.core.transformers.MatchingDatasetUnmarshaller;
import uk.gov.ida.saml.core.transformers.inbound.Cycle3DatasetFactory;
import uk.gov.ida.saml.core.validation.SamlResponseValidationException;
import uk.gov.ida.saml.metadata.MetadataResolverRepository;
import uk.gov.ida.saml.security.MetadataBackedSignatureValidator;
import uk.gov.ida.saml.security.SamlAssertionsSignatureValidator;
import uk.gov.ida.saml.security.SamlMessageSignatureValidator;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.singletonList;

public class EidasAssertionService extends AssertionService {

    private final CountryConditionsValidator conditionsValidator;
    private final MetadataResolverRepository metadataResolverRepository;
    private final List<String> acceptableHubConnectorEntityIds;
    private final String hubEntityId;
    private final EidasMatchingDatasetUnmarshaller matchingDatasetUnmarshaller;
    private final EidasUnsignedMatchingDatasetUnmarshaller matchingUnsignedDatasetUnmarshaller;
    private final AuthnContextFactory authnContextFactory = new AuthnContextFactory();

    @Inject
    public EidasAssertionService(InstantValidator instantValidator,
                                 SubjectValidator subjectValidator,
                                 CountryConditionsValidator conditionsValidator,
                                 SamlAssertionsSignatureValidator hubSignatureValidator,
                                 Cycle3DatasetFactory cycle3DatasetFactory,
                                 MetadataResolverRepository metadataResolverRepository,
                                 @Named("AllAcceptableHubConnectorEntityIds") List<String> acceptableHubConnectorEntityIds,
                                 String hubEntityId,
                                 EidasMatchingDatasetUnmarshaller matchingDatasetUnmarshaller,
                                 EidasUnsignedMatchingDatasetUnmarshaller matchingUnsignedDatasetUnmarshaller) {
        super(instantValidator, subjectValidator, conditionsValidator, hubSignatureValidator, cycle3DatasetFactory);
        this.conditionsValidator = conditionsValidator;
        this.metadataResolverRepository = metadataResolverRepository;
        this.acceptableHubConnectorEntityIds = acceptableHubConnectorEntityIds;
        this.hubEntityId = hubEntityId;
        this.matchingDatasetUnmarshaller = matchingDatasetUnmarshaller;
        this.matchingUnsignedDatasetUnmarshaller = matchingUnsignedDatasetUnmarshaller;
    }

    @Override
    void validate(String expectedInResponseTo, List<Assertion> assertions) {
        for (Assertion assertion : assertions) {
            if (isCountryAssertion(assertion)) {
                validateCountryAssertion(assertion, expectedInResponseTo);
            } else if (isHubAssertion(assertion)) {
                validateCycle3Assertion(assertion, expectedInResponseTo, hubEntityId);
            } else {
                throw new SamlResponseValidationException("Unknown Issuer for eIDAS Assertion: " + assertion.getIssuer().getValue());
            }
        }
    }

    @Override
    public AssertionData translate(List<Assertion> assertions) {
        Assertion countryAssertion = assertions
            .stream()
            .filter(this::isCountryAssertion)
            .findFirst()
            .orElseThrow(() -> new SamlResponseValidationException("No matching dataset assertion present."));
        Optional<Assertion> cycle3Assertion = assertions.stream()
            .filter(a -> !isCountryAssertion(a))
            .findFirst();

        AuthnStatement authnStatement = countryAssertion.getAuthnStatements().get(0);
        String levelOfAssurance = authnStatement.getAuthnContext().getAuthnContextClassRef().getAuthnContextClassRef();
        MatchingDatasetUnmarshaller unmarshaller = getUnmarshaller(countryAssertion);
        return new AssertionData(
                countryAssertion.getIssuer().getValue(),
            authnContextFactory.mapFromEidasToLoA(levelOfAssurance),
            getCycle3Data(cycle3Assertion),
            unmarshaller.fromAssertion(countryAssertion));
    }

    private MatchingDatasetUnmarshaller getUnmarshaller(Assertion countryAssertion) {
        if (containsUnsignedAssertionSamlResponse(countryAssertion)) {
            return matchingUnsignedDatasetUnmarshaller;
        } else {
            return matchingDatasetUnmarshaller;
        }
    }

    protected void validateCountryAssertion(Assertion assertion, String expectedInResponseTo) {
        instantValidator.validate(assertion.getIssueInstant(), "Country Assertion IssueInstant");
        subjectValidator.validate(assertion.getSubject(), expectedInResponseTo);
        conditionsValidator.validate(assertion.getConditions(), acceptableHubConnectorEntityIds.toArray(new String[0]));
        validateAssertionSignature(assertion);
    }

    private boolean containsUnsignedAssertionSamlResponse(Assertion assertion) {
        return assertion.getAttributeStatements().stream()
                .flatMap(as -> as.getAttributes().stream())
                .anyMatch(attribute -> IdaConstants.Eidas_Attributes.UnsignedAssertions.EidasSamlResponse.NAME.equals(attribute.getName()));
    }

    private void validateAssertionSignature(Assertion assertion) {

        if (containsUnsignedAssertionSamlResponse(assertion)) {
            if (assertion.getSignature() == null) {
                return;
            } else {
                throw new SamlResponseValidationException("Assertion contains unsigned saml response but assertion is signed");
            }
        }

        metadataResolverRepository.getSignatureTrustEngine(assertion.getIssuer().getValue())
                .map(MetadataBackedSignatureValidator::withoutCertificateChainValidation)
                .map(SamlMessageSignatureValidator::new)
                .map(SamlAssertionsSignatureValidator::new)
                .orElseThrow(() -> new SamlResponseValidationException("Unable to find metadata resolver for entity Id " + assertion.getIssuer().getValue()))
                .validate(singletonList(assertion), IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
    }

    public Boolean isCountryAssertion(Assertion assertion) {
        return metadataResolverRepository.getResolverEntityIds().contains(assertion.getIssuer().getValue());
    }

    public Boolean isHubAssertion(Assertion assertion) {
        return assertion.getIssuer().getValue().equals(hubEntityId);
    }

}