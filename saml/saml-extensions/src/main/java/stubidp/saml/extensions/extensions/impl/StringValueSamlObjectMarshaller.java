package stubidp.saml.extensions.extensions.impl;


import net.shibboleth.utilities.java.support.xml.ElementSupport;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.common.AbstractSAMLObjectMarshaller;
import org.w3c.dom.Element;
import stubidp.saml.extensions.extensions.StringValueSamlObject;

public class StringValueSamlObjectMarshaller extends AbstractSAMLObjectMarshaller {

    public StringValueSamlObjectMarshaller() {
    }

    @Override
    protected void marshallElementContent(XMLObject xmlObject, Element domElement) {
        StringValueSamlObject stringValueSamlObject = (StringValueSamlObject) xmlObject;
        ElementSupport.appendTextContent(domElement, stringValueSamlObject.getValue());
    }
}
