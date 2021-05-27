package uk.gov.ida.matchingserviceadapter.saml.transformers.outbound.transformers;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Status;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceAssertion;
import uk.gov.ida.matchingserviceadapter.domain.OutboundResponseFromMatchingService;
import uk.gov.ida.saml.core.OpenSamlXmlObjectFactory;
import uk.gov.ida.saml.core.transformers.outbound.IdaResponseToSamlResponseTransformer;
import uk.gov.ida.saml.hub.transformers.outbound.MatchingServiceIdaStatusMarshaller;

import java.util.Optional;

public class OutboundResponseFromMatchingServiceToSamlResponseTransformer extends IdaResponseToSamlResponseTransformer<OutboundResponseFromMatchingService> {

    private final MatchingServiceIdaStatusMarshaller statusMarshaller;
    private final MatchingServiceAssertionToAssertionTransformer assertionTransformer;

    public OutboundResponseFromMatchingServiceToSamlResponseTransformer(
            MatchingServiceIdaStatusMarshaller statusMarshaller,
            OpenSamlXmlObjectFactory openSamlXmlObjectFactory,
            MatchingServiceAssertionToAssertionTransformer assertionTransformer) {
        super(openSamlXmlObjectFactory);
        this.statusMarshaller = statusMarshaller;
        this.assertionTransformer = assertionTransformer;
    }

    @Override
    protected void transformAssertions(OutboundResponseFromMatchingService originalResponse, Response transformedResponse) {
        Optional<MatchingServiceAssertion> assertion = originalResponse.getMatchingServiceAssertion();
        if (assertion.isPresent()) {
            Assertion transformedAssertion = assertionTransformer.apply(assertion.get());
            transformedResponse.getAssertions().add(transformedAssertion);
        }
    }

    @Override
    protected Status transformStatus(OutboundResponseFromMatchingService originalResponse) {
        return statusMarshaller.toSamlStatus(originalResponse.getStatus());
    }

    @Override
    protected void transformDestination(OutboundResponseFromMatchingService originalResponse, Response transformedResponse) {
        // this method intentionally left blank
    }
}
