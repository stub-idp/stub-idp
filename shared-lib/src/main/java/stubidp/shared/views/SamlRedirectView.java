package stubidp.shared.views;

import io.dropwizard.views.View;

import java.net.URI;
import java.util.Optional;

public class SamlRedirectView extends View {
    private final URI targetUri;
    private final String responseBody;
    private final SamlMessageType samlMessageType;
    private final String relayState;
    private final Optional<Boolean> registration;

    public SamlRedirectView(URI targetUri, String base64EncodedResponseBody, SamlMessageType samlMessageType, String relayState, Optional<Boolean> registration) {
        super("samlRedirectView.ftl");
        this.targetUri = targetUri;
        this.responseBody = base64EncodedResponseBody;
        this.samlMessageType = samlMessageType;
        this.relayState = relayState;
        this.registration = registration;
    }

    public URI getTargetUri() {
        return targetUri;
    }

    public String getBody() {
        return responseBody;
    }

    public String getSamlMessageType() {
        return samlMessageType.toString();
    }

    public String getRelayState() {
        return relayState;
    }
    
    public String getRegistration() {
        return registration.get().toString();
    }

    public boolean getShowRegistration(){
        return registration.isPresent();
    }
}
