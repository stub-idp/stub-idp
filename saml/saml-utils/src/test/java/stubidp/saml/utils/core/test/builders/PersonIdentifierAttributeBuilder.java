package stubidp.saml.utils.core.test.builders;

import org.opensaml.saml.saml2.core.Attribute;
import stubidp.saml.extensions.IdaConstants;
import stubidp.saml.extensions.extensions.eidas.PersonIdentifier;
import stubidp.saml.test.OpenSamlXmlObjectFactory;

public class PersonIdentifierAttributeBuilder {
    private PersonIdentifier pid = null;
    private final OpenSamlXmlObjectFactory openSamlXmlObjectFactory = new OpenSamlXmlObjectFactory();

    public static PersonIdentifierAttributeBuilder aPersonIdentifier() {
        return new PersonIdentifierAttributeBuilder();
    }

    public PersonIdentifierAttributeBuilder withValue(PersonIdentifier personIdentifier) {
        this.pid = personIdentifier;
        return this;
    }

    public Attribute build() {
        Attribute attribute = openSamlXmlObjectFactory.createAttribute();

        attribute.getAttributeValues().add(pid);

        attribute.setName(IdaConstants.Eidas_Attributes.PersonIdentifier.NAME);
        attribute.setFriendlyName(IdaConstants.Eidas_Attributes.PersonIdentifier.FRIENDLY_NAME);
        attribute.setNameFormat(Attribute.UNSPECIFIED);

        return attribute;
    }
}
