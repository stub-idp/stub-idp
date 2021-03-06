package stubidp.saml.extensions.extensions.eidas.impl;

import net.shibboleth.utilities.java.support.xml.ElementSupport;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.Marshaller;
import org.w3c.dom.Element;
import stubidp.saml.extensions.extensions.eidas.CurrentFamilyName;

public class CurrentFamilyNameMarshaller extends AbstractTransliterableStringMarshaller {

    public static final Marshaller MARSHALLER = new CurrentFamilyNameMarshaller();

    public CurrentFamilyNameMarshaller() {
    }

    /** {@inheritDoc} */
    protected void marshallElementContent(XMLObject samlObject, Element domElement) {
        CurrentFamilyName currentFamilyName = (CurrentFamilyName) samlObject;
        ElementSupport.appendTextContent(domElement, currentFamilyName.getFamilyName());
    }
}
