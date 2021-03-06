package stubidp.saml.extensions.extensions.eidas.impl;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.common.AbstractSAMLObjectMarshaller;
import org.w3c.dom.Element;
import stubidp.saml.extensions.extensions.eidas.TransliterableString;

abstract class AbstractTransliterableStringMarshaller extends AbstractSAMLObjectMarshaller {

    /** {@inheritDoc} */
    protected void marshallAttributes(XMLObject samlObject, Element domElement) throws MarshallingException {
        TransliterableString transliterableString = (TransliterableString) samlObject;
        if(!transliterableString.isLatinScript()) {
            domElement.setAttribute(TransliterableString.IS_LATIN_SCRIPT_ATTRIBUTE_NAME, transliterableString.isLatinScript().toString());
        }
        super.marshallAttributes(samlObject, domElement);
    }
}
