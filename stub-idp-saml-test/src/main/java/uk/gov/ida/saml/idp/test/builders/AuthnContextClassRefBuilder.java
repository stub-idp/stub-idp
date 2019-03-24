package uk.gov.ida.saml.idp.test.builders;

import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import uk.gov.ida.saml.core.OpenSamlXmlObjectFactory;
import stubidp.saml.extensions.extensions.IdaAuthnContext;

public class AuthnContextClassRefBuilder {

    private String value = IdaAuthnContext.LEVEL_2_AUTHN_CTX;

    public static AuthnContextClassRefBuilder anAuthnContextClassRef() {
        return new AuthnContextClassRefBuilder();
    }

    public AuthnContextClassRefBuilder withAuthnContextClasRefValue(String value) {
        this.value = value;
        return this;
    }

    public AuthnContextClassRef build() {
        return new OpenSamlXmlObjectFactory().createAuthnContextClassReference(value);
    }

}
