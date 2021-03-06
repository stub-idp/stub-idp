package stubidp.saml.extensions.extensions.eidas.impl;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.schema.impl.XSAnyImpl;
import stubidp.saml.extensions.extensions.eidas.CurrentAddress;

import java.util.List;

public class CurrentAddressImpl extends XSAnyImpl implements CurrentAddress {

    /** String to hold the address in base64 encoded. */
    private String currentAddressInBase64Encoded;

    /**
     * Constructor.
     *
     * @param namespaceURI the namespace the element is in
     * @param elementLocalName the local name of the XML element this Object represents
     * @param namespacePrefix the prefix for the given namespace
     */
    CurrentAddressImpl(String namespaceURI, String elementLocalName, String namespacePrefix) {
        super(namespaceURI, elementLocalName, namespacePrefix);
    }

    /** {@inheritDoc} */
    public String getCurrentAddress() {
        return currentAddressInBase64Encoded;
    }

    /** {@inheritDoc} */
    public void setCurrentAddress(String s) {

        currentAddressInBase64Encoded = prepareForAssignment(currentAddressInBase64Encoded, s);
    }

    @Nullable
    @Override
    public List<XMLObject> getOrderedChildren() {
        return null;
    }
}
