package stubidp.saml.extensions.extensions.eidas.impl;

import org.junit.jupiter.api.Test;
import stubidp.saml.Utils;
import stubidp.saml.extensions.extensions.eidas.CurrentGivenName;
import stubidp.saml.test.OpenSAMLRunner;

import static org.assertj.core.api.Assertions.assertThat;

class CurrentGivenNameUnmarshallerTest extends OpenSAMLRunner {

    @Test
    void shouldUnmarshallCurrentGivenNameValue() throws Exception {
        final CurrentGivenName currentGivenName = Utils.unmarshall(getCurrentGivenNameSamlString(true));

        assertThat(currentGivenName.getFirstName()).isEqualTo("Javier");
    }

    @Test
    void shouldUnmarshallLatinScriptValueWhenAbsent() throws Exception {
        final CurrentGivenName currentGivenName = Utils.unmarshall(getCurrentGivenNameSamlString(true));

        assertThat(currentGivenName.isLatinScript()).isEqualTo(true);
    }

    @Test
    void shouldUnmarshallLatinScriptValueWhenPresent() throws Exception {
        final CurrentGivenName currentGivenName = Utils.unmarshall(getCurrentGivenNameSamlString(false));

        assertThat(currentGivenName.isLatinScript()).isEqualTo(false);
    }

    private String getCurrentGivenNameSamlString(boolean isLatinScript) {
        return String.format("""
                   <saml2:AttributeValue %s xmlns:eidas-natural="http://eidas.europa.eu/attributes/naturalperson"
                       xmlns:saml2="urn:oasis:names:tc:SAML:2.0:assertion"
                       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                       xsi:type="eidas-natural:CurrentGivenNameType">
                   Javier</saml2:AttributeValue>""", isLatinScript ? "" : "LatinScript=\"false\"");
    }
}
