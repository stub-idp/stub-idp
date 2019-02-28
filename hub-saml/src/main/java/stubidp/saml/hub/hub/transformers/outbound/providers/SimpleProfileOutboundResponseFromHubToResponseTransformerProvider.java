package stubidp.saml.hub.hub.transformers.outbound.providers;

import com.google.inject.Provider;
import stubidp.saml.utils.core.domain.OutboundResponseFromHub;
import stubidp.saml.hub.hub.transformers.outbound.SimpleProfileOutboundResponseFromHubToSamlResponseTransformer;

import javax.inject.Inject;
import java.util.function.Function;

public class SimpleProfileOutboundResponseFromHubToResponseTransformerProvider implements
        Provider<Function<OutboundResponseFromHub, String>> {

    private final Function<OutboundResponseFromHub, String> outboundResponseFromHubToStringTransformer;

    @Inject
    public SimpleProfileOutboundResponseFromHubToResponseTransformerProvider(
            SimpleProfileOutboundResponseFromHubToSamlResponseTransformer outboundToResponseTransformer,
            ResponseToUnsignedStringTransformer responseToStringTransformer) {

        this.outboundResponseFromHubToStringTransformer = responseToStringTransformer.compose(outboundToResponseTransformer);
    }

    public Function<OutboundResponseFromHub, String> get() {
        return outboundResponseFromHubToStringTransformer;
    }
}