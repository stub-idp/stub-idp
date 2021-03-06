package stubidp.saml.extensions.extensions.impl;

import net.shibboleth.utilities.java.support.xml.XMLConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.Marshaller;
import org.opensaml.core.xml.io.MarshallingException;
import org.w3c.dom.Element;
import stubidp.saml.extensions.extensions.Gender;
import stubidp.saml.test.OpenSAMLRunner;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static stubidp.saml.extensions.IdaConstants.IDA_NS;
import static stubidp.saml.extensions.IdaConstants.IDA_PREFIX;

class GenderMarshallerTest extends OpenSAMLRunner {

    private Marshaller marshaller;
    private Gender gender;

    @BeforeEach
    void setUp() {
        gender = new GenderBuilder().buildObject();
        marshaller = XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(gender);
    }

    @Test
    void marshall_shouldMarshallPersonName() throws Exception {
        String name = "John";
        gender.setValue(name);

        Element marshalledElement = marshaller.marshall(gender);

        assertThat(marshalledElement.getNamespaceURI()).isEqualTo(Gender.DEFAULT_ELEMENT_NAME.getNamespaceURI());

        assertThat(marshalledElement.getAttributeNS(XMLConstants.XSI_NS, XMLConstants.XSI_TYPE_ATTRIB_NAME.getLocalPart())).isEqualTo(String.format("%s:%s", IDA_PREFIX, Gender.TYPE_LOCAL_NAME));
        assertThat(marshalledElement.getTextContent()).isEqualTo(name);
    }

    @Test
    void marshall_shouldEnsureXsiNamespaceDefinitionIsInluded() throws Exception {
        Element marshalledElement = marshaller.marshall(new GenderBuilder().buildObject());

        assertThat(marshalledElement.hasAttributeNS(XMLConstants.XMLNS_NS, XMLConstants.XSI_PREFIX)).isTrue();
    }

    @Test
    void marshall_shouldMarshallFromDateInCorrectFormat() throws Exception {
        String fromDate = "2012-02-09";
        gender.setFrom(LocalDate.parse(fromDate));

        Element marshalledElement = marshaller.marshall(gender);

        assertThat(marshalledElement.getAttributeNodeNS(IDA_NS, Gender.FROM_ATTRIB_NAME).getValue()).isEqualTo(fromDate);
    }

    @Test
    void marshall_shouldMarshallFromDateWithNamespacePrefix() throws Exception {
        gender.setFrom(LocalDate.parse("2012-02-09"));

        Element marshalledElement = marshaller.marshall(gender);

        assertThat(marshalledElement.getAttributeNodeNS(IDA_NS, Gender.FROM_ATTRIB_NAME).getPrefix()).isEqualTo(IDA_PREFIX);
    }

    @Test
    void marshall_shouldMarshallToDateInCorrectFormat() throws Exception {
        String toDate = "2012-02-09";
        final Gender personName = new GenderBuilder().buildObject();
        personName.setTo(LocalDate.parse(toDate));

        Element marshalledElement = marshaller.marshall(personName);

        assertThat(marshalledElement.getAttributeNodeNS(IDA_NS, Gender.TO_ATTRIB_NAME).getValue()).isEqualTo(toDate);
    }

    @Test
    void marshall_shouldMarshallToDateWithNamespacePrefix() throws Exception {
        gender.setTo(LocalDate.parse("2012-02-09"));

        Element marshalledElement = marshaller.marshall(gender);

        assertThat(marshalledElement.getAttributeNodeNS(IDA_NS, Gender.TO_ATTRIB_NAME).getPrefix()).isEqualTo(IDA_PREFIX);
    }

    @Test
    void marshall_shouldMarshallVerifiedWhenTrue() throws Exception {
        checkMarshallingVerifiedAttributeWithValue(true);
    }

    @Test
    void marshall_shouldMarshallVerifiedWhenFalse() throws Exception {
        checkMarshallingVerifiedAttributeWithValue(false);
    }

    @Test
    void marshall_shouldMarshallVerifiedWithNamespacePrefix() throws Exception {
        gender.setVerified(true);

        Element marshalledElement = marshaller.marshall(gender);

        assertThat(marshalledElement.getAttributeNodeNS(IDA_NS, Gender.VERIFIED_ATTRIB_NAME).getPrefix()).isEqualTo(IDA_PREFIX);
    }

    private void checkMarshallingVerifiedAttributeWithValue(boolean verifiedValue) throws MarshallingException {
        gender.setVerified(verifiedValue);

        Element marshalledElement = marshaller.marshall(gender);

        assertThat(Boolean.parseBoolean(marshalledElement.getAttributeNodeNS(IDA_NS, Gender.VERIFIED_ATTRIB_NAME).getValue())).isEqualTo(verifiedValue);
    }
}
