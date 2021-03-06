package stubidp.saml.extensions.extensions.eidas.impl;

import org.opensaml.saml.common.AbstractSAMLObjectBuilder;
import stubidp.saml.extensions.extensions.eidas.BirthName;

public class BirthNameBuilder extends AbstractSAMLObjectBuilder<BirthName> {

    /**
     * Constructor.
     */
    public BirthNameBuilder() {

    }

    /** {@inheritDoc} */
    public BirthName buildObject() {
        return buildObject(BirthName.DEFAULT_ELEMENT_NAME, BirthName.TYPE_NAME);
    }

    /** {@inheritDoc} */
    public BirthName buildObject(String namespaceURI, String localName, String namespacePrefix) {
        return new BirthNameImpl(namespaceURI, localName, namespacePrefix);
    }
}
