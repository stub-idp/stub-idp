package stubidp.saml.utils.hub.transformers.outbound;

import com.google.common.collect.ImmutableMap;
import stubidp.saml.utils.core.domain.DetailedStatusCode;
import stubidp.saml.utils.core.domain.MatchingServiceIdaStatus;
import stubidp.saml.utils.core.transformers.outbound.IdaStatusMarshaller;
import stubidp.saml.utils.core.OpenSamlXmlObjectFactory;

import javax.inject.Inject;

public class MatchingServiceIdaStatusMarshaller extends IdaStatusMarshaller<MatchingServiceIdaStatus> {

    private static final ImmutableMap<MatchingServiceIdaStatus, DetailedStatusCode> REST_TO_SAML_CODES =
            ImmutableMap.<MatchingServiceIdaStatus, DetailedStatusCode>builder()
                    .put(MatchingServiceIdaStatus.MatchingServiceMatch, DetailedStatusCode.MatchingServiceMatch)
                    .put(MatchingServiceIdaStatus.NoMatchingServiceMatchFromMatchingService, DetailedStatusCode.NoMatchingServiceMatchFromMatchingService)
                    .put(MatchingServiceIdaStatus.RequesterError, DetailedStatusCode.RequesterErrorFromIdp)
                    .put(MatchingServiceIdaStatus.Healthy, DetailedStatusCode.Healthy)
                    .build();

    @Inject
    public MatchingServiceIdaStatusMarshaller(OpenSamlXmlObjectFactory samlObjectFactory) {
        super(samlObjectFactory);
    }

    @Override
    protected DetailedStatusCode getDetailedStatusCode(MatchingServiceIdaStatus originalStatus) {
        return REST_TO_SAML_CODES.get(originalStatus);
    }
}