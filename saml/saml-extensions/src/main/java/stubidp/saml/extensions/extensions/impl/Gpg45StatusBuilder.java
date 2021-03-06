package stubidp.saml.extensions.extensions.impl;

import org.opensaml.saml.common.AbstractSAMLObjectBuilder;
import stubidp.saml.extensions.extensions.Gpg45Status;

public class Gpg45StatusBuilder extends AbstractSAMLObjectBuilder<Gpg45Status> {

    public Gpg45StatusBuilder() {
    }

    @Override
    public Gpg45Status buildObject() {
        return buildObject(Gpg45Status.DEFAULT_ELEMENT_NAME);
    }

    @Override
    public Gpg45Status buildObject(String namespaceURI, String localName, String namespacePrefix) {
        return new Gpg45StatusImpl(namespaceURI, localName, namespacePrefix);
    }
}
