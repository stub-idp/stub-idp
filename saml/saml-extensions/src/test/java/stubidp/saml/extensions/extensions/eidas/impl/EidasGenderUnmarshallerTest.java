package stubidp.saml.extensions.extensions.eidas.impl;

import org.junit.jupiter.api.Test;
import stubidp.saml.Utils;
import stubidp.saml.extensions.extensions.eidas.EidasGender;
import stubidp.saml.test.OpenSAMLRunner;

import static org.assertj.core.api.Assertions.assertThat;

class EidasGenderUnmarshallerTest extends OpenSAMLRunner {
    @Test
    void shouldUnmarshallGender() throws Exception {
        final EidasGender eidasGender = Utils.unmarshall("""
                <saml2:AttributeValue xmlns:eidas-natural="http://eidas.europa.eu/attributes/naturalperson"
                    xmlns:saml2="urn:oasis:names:tc:SAML:2.0:assertion"
                    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                    xsi:type="eidas-natural:GenderType">
                Male</saml2:AttributeValue>"""
        );

        assertThat(eidasGender.getValue()).isEqualTo("Male");
    }
}
