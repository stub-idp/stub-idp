package stubidp.saml.extensions.extensions.impl;

import org.junit.jupiter.api.Test;
import stubidp.saml.Utils;
import stubidp.saml.extensions.extensions.Verified;
import stubidp.saml.test.OpenSAMLRunner;

import static org.assertj.core.api.Assertions.assertThat;

class VerifiedUnmarshallerTest extends OpenSAMLRunner {
    @Test
    void unmarshall_shouldSetValueWhenTrue() throws Exception {
        Verified verified = Utils.unmarshall("""
                <saml:AttributeValue xmlns:ida="http://www.cabinetoffice.gov.uk/resource-library/ida/attributes"
                        xmlns:saml="urn:oasis:names:tc:SAML:2.0:assertion"
                       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                       xsi:type="ida:VerifiedType">
                   true</saml:AttributeValue>"""
        );

        assertThat(verified.getValue()).isEqualTo(true);
    }

    @Test
    void unmarshall_shouldSetValueWhenFalse() throws Exception {
        Verified verified = Utils.unmarshall("""
                <saml:AttributeValue xmlns:ida="http://www.cabinetoffice.gov.uk/resource-library/ida/attributes"
                        xmlns:saml="urn:oasis:names:tc:SAML:2.0:assertion"
                       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                       xsi:type="ida:VerifiedType">
                   false</saml:AttributeValue>"""
        );

        assertThat(verified.getValue()).isEqualTo(false);
    }

}
