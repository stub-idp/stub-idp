package stubidp.saml.extensions.extensions.impl;

import org.junit.jupiter.api.Test;
import stubidp.saml.Utils;
import stubidp.saml.extensions.extensions.Address;
import stubidp.saml.extensions.extensions.Line;
import stubidp.saml.test.OpenSAMLRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AddressUnmarshallerTest extends OpenSAMLRunner {

    @Test
    void unmarshall_shouldUnmarshallAnAddress() throws Exception {
        String line1Value = "1 Cherry Cottage";
        String line2Value = "Wurpel Lane";
        String postCodeValue = "RG99 1YY";
        String fromDateValue = "1969-01-11";
        String toDateValue = "1969-02-11";
        String internationalPostCodeValue = "RG98 1ZZ";
        String uprn = "672347923456";
        boolean verifiedValue = true;
        String addressXmlString = createAddressXmlString(
                line1Value,
                line2Value,
                internationalPostCodeValue,
                postCodeValue,
                fromDateValue,
                toDateValue,
                verifiedValue,
                uprn);

        Address address = Utils.unmarshall(addressXmlString);

        assertThat(address.getFrom()).isEqualTo(fromDateValue);
        assertThat(address.getTo()).isEqualTo(toDateValue);
        assertThat(address.getVerified()).isEqualTo(verifiedValue);
        List<Line> lines = address.getLines();
        assertThat(lines.size()).isEqualTo(2);
        assertThat(lines.get(0).getValue()).isEqualTo(line1Value);
        assertThat(lines.get(1).getValue()).isEqualTo(line2Value);
        assertThat(address.getPostCode().getValue()).isEqualTo(postCodeValue);
        assertThat(address.getInternationalPostCode().getValue()).isEqualTo(internationalPostCodeValue);
        assertThat(address.getUPRN().getValue()).isEqualTo(uprn);
    }

    @Test
    void unmarshall_shouldUnmarshallVerifiedWhenTrue() throws Exception {
        String addressXmlString = createAddressXmlString("", "", "", "", "2020-03-15", "2020-03-15", true, "");

        Address address = Utils.unmarshall(addressXmlString);

        assertThat(address.getVerified()).isEqualTo(true);
    }

    @Test
    void unmarshall_shouldUnmarshallVerifiedWhenFalse() throws Exception {
        String addressXmlString = createAddressXmlString("", "", "", "", "2020-03-15", "2020-03-15", false, "");

        Address address = Utils.unmarshall(addressXmlString);

        assertThat(address.getVerified()).isEqualTo(false);
    }

    @Test
    void unmarshall_shouldSetVerifiedToDefaultValueWhenAbsent() throws Exception {
        String addressXmlString = """
                <saml:AttributeValue xmlns:saml="urn:oasis:names:tc:SAML:2.0:assertion" xmlns:ida="http://www.cabinetoffice.gov.uk/resource-library/ida/attributes" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="ida:AddressType">
                  <ida:Line>a</ida:Line>
                  <ida:Line>a</ida:Line>
                </saml:AttributeValue>""".indent(2);

        Address address = Utils.unmarshall(addressXmlString);

        assertThat(address.getVerified()).isEqualTo(false);
    }

    private static String createAddressXmlString(
            String line1Value,
            String line2Value,
            String internationalPostCodeValue,
            String postCodeValue,
            String fromDateValue,
            String toDateValue,
            boolean verifiedValue,
            String uprn) {

        return String.format("""
                        <saml:AttributeValue  ida:From="%s"  ida:To="%s"  ida:Verified="%b"  xmlns:saml="urn:oasis:names:tc:SAML:2.0:assertion"  xmlns:ida="http://www.cabinetoffice.gov.uk/resource-library/ida/attributes"  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"  xsi:type="ida:AddressType">
                          <ida:Line>%s</ida:Line>
                          <ida:Line>%s</ida:Line>
                          <ida:PostCode>%s</ida:PostCode>
                          <ida:InternationalPostCode>%s</ida:InternationalPostCode>
                          <ida:UPRN>%s</ida:UPRN>
                        </saml:AttributeValue>""".indent(2),
                fromDateValue,
                toDateValue,
                verifiedValue,
                line1Value,
                line2Value,
                postCodeValue,
                internationalPostCodeValue,
                uprn);
    }
}
