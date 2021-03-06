package uk.gov.ida.matchingserviceadapter.saml.transformers.outbound.transformers;

import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Status;
import stubidp.saml.domain.matching.MatchingServiceIdaStatus;
import stubidp.saml.hub.transformers.outbound.MatchingServiceIdaStatusMarshaller;
import stubidp.saml.utils.core.OpenSamlXmlObjectFactory;
import stubidp.saml.utils.core.transformers.outbound.IdaResponseToSamlResponseTransformer;
import uk.gov.ida.matchingserviceadapter.domain.HealthCheckResponseFromMatchingService;

public class HealthCheckResponseFromMatchingServiceTransformer extends IdaResponseToSamlResponseTransformer<HealthCheckResponseFromMatchingService> {

    private final MatchingServiceIdaStatusMarshaller statusMarshaller;

    public HealthCheckResponseFromMatchingServiceTransformer(OpenSamlXmlObjectFactory openSamlXmlObjectFactory,
                                                             MatchingServiceIdaStatusMarshaller statusMarshaller) {
        super(openSamlXmlObjectFactory);
        this.statusMarshaller = statusMarshaller;
    }

    @Override
    protected void transformAssertions(HealthCheckResponseFromMatchingService originalResponse, Response transformedResponse) {
        // healthcheck has no assertions
    }

    @Override
    protected Status transformStatus(HealthCheckResponseFromMatchingService originalResponse) {
        return statusMarshaller.toSamlStatus(MatchingServiceIdaStatus.Healthy);
    }

    @Override
    protected void transformDestination(HealthCheckResponseFromMatchingService originalResponse, Response transformedResponse) {
        // healthcheck does not require transformation
    }
}
