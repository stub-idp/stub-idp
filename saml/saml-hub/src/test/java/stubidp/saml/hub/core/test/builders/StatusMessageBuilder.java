package stubidp.saml.hub.core.test.builders;

import org.opensaml.saml.saml2.core.StatusMessage;
import stubidp.saml.utils.core.OpenSamlXmlObjectFactory;

public class StatusMessageBuilder {

    private static final OpenSamlXmlObjectFactory openSamlXmlObjectFactory = new OpenSamlXmlObjectFactory();

    private String message = "default message";

    public static StatusMessageBuilder aStatusMessage() {
        return new StatusMessageBuilder();
    }

    public StatusMessage build() {
        StatusMessage statusCode = openSamlXmlObjectFactory.createStatusMessage();
        statusCode.setValue(message);
       return statusCode;
    }

    public StatusMessageBuilder withMessage(String message) {
        this.message = message;
        return this;
    }
}
