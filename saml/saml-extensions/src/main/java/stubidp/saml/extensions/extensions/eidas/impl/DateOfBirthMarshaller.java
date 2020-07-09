package stubidp.saml.extensions.extensions.eidas.impl;

import net.shibboleth.utilities.java.support.xml.ElementSupport;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.Marshaller;
import org.opensaml.saml.common.AbstractSAMLObjectMarshaller;
import org.w3c.dom.Element;
import stubidp.saml.extensions.extensions.eidas.DateOfBirth;
import stubidp.saml.extensions.extensions.impl.BaseMdsSamlObjectMarshaller;

public class DateOfBirthMarshaller extends AbstractSAMLObjectMarshaller {

    public static final Marshaller MARSHALLER = new DateOfBirthMarshaller();

    /** {@inheritDoc} */
    protected void marshallElementContent(XMLObject samlObject, Element domElement) {
        DateOfBirth dateOfBirth = (DateOfBirth) samlObject;
        ElementSupport.appendTextContent(domElement, BaseMdsSamlObjectMarshaller.DateFromInstant.of(dateOfBirth.getDateOfBirth()));
    }
}