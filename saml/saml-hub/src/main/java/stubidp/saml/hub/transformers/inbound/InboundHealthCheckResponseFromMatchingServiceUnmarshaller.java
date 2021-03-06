package stubidp.saml.hub.transformers.inbound;

import org.opensaml.saml.saml2.core.Response;
import stubidp.saml.hub.domain.InboundHealthCheckResponseFromMatchingService;

public class InboundHealthCheckResponseFromMatchingServiceUnmarshaller {
    private final MatchingServiceIdaStatusUnmarshaller statusUnmarshaller;

    public InboundHealthCheckResponseFromMatchingServiceUnmarshaller(
            MatchingServiceIdaStatusUnmarshaller statusUnmarshaller) {

        this.statusUnmarshaller = statusUnmarshaller;
    }

    public InboundHealthCheckResponseFromMatchingService fromSaml(Response response) {
        MatchingServiceIdaStatus transformedStatus = statusUnmarshaller.fromSaml(response.getStatus());

        return new InboundHealthCheckResponseFromMatchingService(
                response.getID(),
                response.getInResponseTo(),
                response.getIssuer().getValue(),
                response.getIssueInstant(),
                transformedStatus);
    }
}
