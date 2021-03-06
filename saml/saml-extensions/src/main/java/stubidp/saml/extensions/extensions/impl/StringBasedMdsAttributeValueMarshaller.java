package stubidp.saml.extensions.extensions.impl;

import net.shibboleth.utilities.java.support.xml.ElementSupport;
import org.opensaml.core.xml.XMLObject;
import org.w3c.dom.Element;
import stubidp.saml.extensions.extensions.StringBasedMdsAttributeValue;

public class StringBasedMdsAttributeValueMarshaller extends BaseMdsSamlObjectMarshaller {

    public StringBasedMdsAttributeValueMarshaller(){
        super();
    }

    public StringBasedMdsAttributeValueMarshaller(String xsiType){
        super(xsiType);
    }

    @Override
    protected void marshallElementContent(XMLObject xmlObject, Element domElement) {
        StringBasedMdsAttributeValue stringBasedMdsAttributeValue = (StringBasedMdsAttributeValue) xmlObject;
        ElementSupport.appendTextContent(domElement, stringBasedMdsAttributeValue.getValue());
    }
}
