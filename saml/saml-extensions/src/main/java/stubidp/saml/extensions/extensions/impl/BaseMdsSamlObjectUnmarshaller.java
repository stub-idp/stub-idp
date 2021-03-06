package stubidp.saml.extensions.extensions.impl;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.saml.common.AbstractSAMLObjectUnmarshaller;
import org.w3c.dom.Attr;
import stubidp.saml.extensions.extensions.Address;
import stubidp.saml.extensions.extensions.BaseMdsSamlObject;

import java.time.LocalDate;
import java.util.Objects;

public class BaseMdsSamlObjectUnmarshaller extends AbstractSAMLObjectUnmarshaller {

    public BaseMdsSamlObjectUnmarshaller() {
    }

    protected void processAttribute(XMLObject samlObject, Attr attribute) throws UnmarshallingException {
        BaseMdsSamlObject address = (BaseMdsSamlObject) samlObject;

        switch (attribute.getLocalName()) {
            case Address.FROM_ATTRIB_NAME -> {
                address.setFrom(Objects.isNull(attribute.getValue()) ? null : LocalDate.parse(attribute.getValue()));
                break;
            }
            case Address.TO_ATTRIB_NAME -> {
                address.setTo(Objects.isNull(attribute.getValue()) ? null : LocalDate.parse(attribute.getValue()));
                break;
            }
            case Address.VERIFIED_ATTRIB_NAME -> {
                address.setVerified(Boolean.parseBoolean(attribute.getValue()));
                break;
            }
            default -> {
                super.processAttribute(samlObject, attribute);
            }
        }
    }
}
