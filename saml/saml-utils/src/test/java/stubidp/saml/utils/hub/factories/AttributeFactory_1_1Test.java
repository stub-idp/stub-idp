package stubidp.saml.utils.hub.factories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.saml2.core.Attribute;
import stubidp.saml.domain.assertions.Address;
import stubidp.saml.domain.assertions.Gender;
import stubidp.saml.domain.assertions.SimpleMdsValue;
import stubidp.saml.extensions.extensions.Date;
import stubidp.saml.extensions.extensions.Gpg45Status;
import stubidp.saml.extensions.extensions.IPAddress;
import stubidp.saml.extensions.extensions.IdpFraudEventId;
import stubidp.saml.extensions.extensions.PersonName;
import stubidp.saml.extensions.extensions.StringBasedMdsAttributeValue;
import stubidp.saml.test.OpenSAMLRunner;
import stubidp.saml.test.builders.AddressBuilder;
import stubidp.saml.test.builders.SimpleMdsValueBuilder;
import stubidp.saml.utils.core.OpenSamlXmlObjectFactory;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class AttributeFactory_1_1Test extends OpenSAMLRunner {

    private AttributeFactory_1_1 attributeFactory;

    @BeforeEach
    void setup() {
        attributeFactory = new AttributeFactory_1_1(new OpenSamlXmlObjectFactory());
    }

    @Test
    void createFirstNameAttribute_shouldSetUpTheAttribute() {
        SimpleMdsValue<String> firstName = new SimpleMdsValue<>("Bob",
                LocalDate.parse("2012-03-02"),
                LocalDate.parse("2013-09-04"),
                true);

        Attribute createdAttribute = attributeFactory.createFirstnameAttribute(Collections.singletonList(firstName));

        assertThat(createdAttribute.getName()).isEqualTo("MDS_firstname");
        assertThat(createdAttribute.getFriendlyName()).isEqualTo("Firstname");
        assertThat(createdAttribute.getNameFormat()).isEqualTo(Attribute.UNSPECIFIED);
        assertThat(createdAttribute.getAttributeValues().size()).isEqualTo(1);
        PersonName firstnameAttributeValue = (PersonName) createdAttribute.getAttributeValues().get(0);
        assertThat(firstnameAttributeValue.getValue()).isEqualTo(firstName.getValue());
        assertThat(firstnameAttributeValue.getFrom()).isEqualTo(firstName.getFrom());
        assertThat(firstnameAttributeValue.getTo()).isEqualTo(firstName.getTo());
        assertThat(firstnameAttributeValue.getLanguage()).isEqualTo("en-GB");
    }

    @Test
    void createFirstNameAttribute_shouldHandleMultipleValues() {
        List<SimpleMdsValue<String>> firstNames = asList(
                SimpleMdsValueBuilder.<String>aSimpleMdsValue().build(),
                SimpleMdsValueBuilder.<String>aSimpleMdsValue().build());

        Attribute createdAttribute = attributeFactory.createFirstnameAttribute(firstNames);

        assertThat(createdAttribute.getAttributeValues().size()).isEqualTo(2);
    }

    @Test
    void createMiddlenameAttribute_shouldSetUpTheAttribute() {
        SimpleMdsValue<String> middlename = new SimpleMdsValue<>("Robert",
                LocalDate.parse("2012-03-02"),
                LocalDate.parse("2013-09-04"),
                false);

        Attribute createdAttribute = attributeFactory.createMiddlenamesAttribute(Collections.singletonList(middlename));

        assertThat(createdAttribute.getName()).isEqualTo("MDS_middlename");
        assertThat(createdAttribute.getFriendlyName()).isEqualTo("Middlename(s)");
        assertThat(createdAttribute.getNameFormat()).isEqualTo(Attribute.UNSPECIFIED);
        assertThat(createdAttribute.getAttributeValues().size()).isEqualTo(1);
        PersonName middleNameAttributeValue = (PersonName) createdAttribute.getAttributeValues().get(0);
        assertThat(middleNameAttributeValue.getValue()).isEqualTo(middlename.getValue());
        assertThat(middleNameAttributeValue.getFrom()).isEqualTo(middlename.getFrom());
        assertThat(middleNameAttributeValue.getTo()).isEqualTo(middlename.getTo());
        assertThat(middleNameAttributeValue.getLanguage()).isEqualTo("en-GB");
    }

    @Test
    void createSurnameAttribute_shouldSetUpTheAttribute() {
        SimpleMdsValue<String> surname = new SimpleMdsValue<>("McBoberson",
                LocalDate.parse("2012-03-02"),
                LocalDate.parse("2013-09-04"),
                false);

        Attribute createdAttribute = attributeFactory.createSurnameAttribute(Collections.singletonList(surname));

        assertThat(createdAttribute.getName()).isEqualTo("MDS_surname");
        assertThat(createdAttribute.getFriendlyName()).isEqualTo("Surname");
        assertThat(createdAttribute.getNameFormat()).isEqualTo(Attribute.UNSPECIFIED);
        assertThat(createdAttribute.getAttributeValues().size()).isEqualTo(1);
        PersonName surnameAttributeValue = (PersonName) createdAttribute.getAttributeValues().get(0);
        assertThat(surnameAttributeValue.getValue()).isEqualTo(surname.getValue());
        assertThat(surnameAttributeValue.getFrom()).isEqualTo(surname.getFrom());
        assertThat(surnameAttributeValue.getTo()).isEqualTo(surname.getTo());
        assertThat(surnameAttributeValue.getLanguage()).isEqualTo("en-GB");
    }

    @Test
    void createGenderAttribute_shouldSetUpTheAttribute() {
        SimpleMdsValue<Gender> genderSimpleMdsValue = new SimpleMdsValue<>(Gender.FEMALE,
                LocalDate.parse("2012-03-02"),
                LocalDate.parse("2013-09-04"),
                false);

        Attribute createdAttribute = attributeFactory.createGenderAttribute(genderSimpleMdsValue);

        assertThat(createdAttribute.getName()).isEqualTo("MDS_gender");
        assertThat(createdAttribute.getFriendlyName()).isEqualTo("Gender");
        assertThat(createdAttribute.getNameFormat()).isEqualTo(Attribute.UNSPECIFIED);
        assertThat(createdAttribute.getAttributeValues().size()).isEqualTo(1);
        stubidp.saml.extensions.extensions.Gender expectedGenderAttributeValue = (stubidp.saml.extensions.extensions.Gender) createdAttribute.getAttributeValues().get(0);
        assertThat(expectedGenderAttributeValue.getValue()).isEqualTo(genderSimpleMdsValue.getValue().getValue());
        assertThat(expectedGenderAttributeValue.getFrom()).isEqualTo(genderSimpleMdsValue.getFrom());
        assertThat(expectedGenderAttributeValue.getTo()).isEqualTo(genderSimpleMdsValue.getTo());
        assertThat(expectedGenderAttributeValue.getVerified()).isEqualTo(genderSimpleMdsValue.isVerified());
    }

    @Test
    void createCycle3Attribute_shouldSetUpTheAttribute() {
        String value = "some value";
        String attributeName = "someAttributeName";
        Attribute createdAttribute = attributeFactory.createCycle3DataAttribute(attributeName, value);

        assertThat(createdAttribute.getName()).isEqualTo(attributeName);
        assertThat(createdAttribute.getNameFormat()).isEqualTo(Attribute.UNSPECIFIED);
        assertThat(createdAttribute.getAttributeValues().size()).isEqualTo(1);
        assertThat(((StringBasedMdsAttributeValue) createdAttribute.getAttributeValues().get(0)).getValue()).isEqualTo(value);
    }

    @Test
    void createDateOfBirthAttribute_shouldSetUpTheAttribute() {
        SimpleMdsValue<LocalDate> dateOfBirth = new SimpleMdsValue<>(LocalDate.parse("1981-03-29"),
                LocalDate.parse("2012-03-02"),
                LocalDate.parse("2013-09-04"),
                true);

        Attribute createdAttribute = attributeFactory.createDateOfBirthAttribute(Collections.singletonList(dateOfBirth));

        assertThat(createdAttribute.getName()).isEqualTo("MDS_dateofbirth");
        assertThat(createdAttribute.getFriendlyName()).isEqualTo("Date of Birth");
        assertThat(createdAttribute.getNameFormat()).isEqualTo(Attribute.UNSPECIFIED);
        assertThat(createdAttribute.getAttributeValues().size()).isEqualTo(1);
        Date dateOfBirthAttributeValue = (Date) createdAttribute.getAttributeValues().get(0);
        LocalDate dateOfBirthFromDom = LocalDate.parse(dateOfBirthAttributeValue.getValue());
        assertThat(dateOfBirthFromDom).isEqualTo(dateOfBirth.getValue());
        assertThat(dateOfBirthAttributeValue.getFrom()).isEqualTo(dateOfBirth.getFrom());
        assertThat(dateOfBirthAttributeValue.getTo()).isEqualTo(dateOfBirth.getTo());
        assertThat(dateOfBirthAttributeValue.getVerified()).isEqualTo(dateOfBirth.isVerified());
    }

    @Test
    void createCurrentAddressAttribute_shouldSetUpTheAttribute() {
        String line1Value = "1 Cherry Cottage";
        String line2Value = "Wurpel Lane";
        String postCodeValue = "RG99 1YY";
        String internationalPostCodeValue = "RG88 1ZZ";
        String uprnValue = "RG88 1ZZ";
        LocalDate fromDateValue = LocalDate.parse("2012-09-09");
        boolean verified = true;
        LocalDate toDateValue = LocalDate.parse("2012-10-11");
        Address currentAddress = AddressBuilder.anAddress()
                .withLines(asList(line1Value, line2Value))
                .withPostCode(postCodeValue)
                .withInternationalPostCode(internationalPostCodeValue)
                .withUPRN(uprnValue)
                .withFromDate(fromDateValue)
                .withToDate(toDateValue)
                .withVerified(verified)
                .build();

        Attribute createdAttribute = attributeFactory.createCurrentAddressesAttribute(List.of(currentAddress));

        assertThat(createdAttribute.getName()).isEqualTo("MDS_currentaddress");
        assertThat(createdAttribute.getFriendlyName()).isEqualTo("Current Address");
        assertThat(createdAttribute.getNameFormat()).isEqualTo(Attribute.UNSPECIFIED);

        stubidp.saml.extensions.extensions.Address addressAttributeValue = getAddress(createdAttribute);
        assertThat(addressAttributeValue.getFrom()).isEqualTo(fromDateValue);
        assertThat(addressAttributeValue.getTo()).isEqualTo(toDateValue);
        assertThat(addressAttributeValue.getVerified()).isEqualTo(verified);
        assertThat(addressAttributeValue.getLines().get(0).getValue()).isEqualTo(line1Value);
        assertThat(addressAttributeValue.getLines().get(1).getValue()).isEqualTo(line2Value);
        assertThat(addressAttributeValue.getPostCode().getValue()).isEqualTo(postCodeValue);
        assertThat(addressAttributeValue.getInternationalPostCode().getValue()).isEqualTo(internationalPostCodeValue);
        assertThat(addressAttributeValue.getUPRN().getValue()).isEqualTo(uprnValue);
    }

    @Test
    void createCurrentAddressAttribute_shouldHandleMissingToDate() {
        Address currentAddress = AddressBuilder.anAddress().withLines(asList("Flat 15", "Dalton Tower")).withToDate(null).build();

        attributeFactory.createCurrentAddressesAttribute(List.of(currentAddress));
    }

    @Test
    void createCurrentAddressAttribute_shouldHandleMissingPostCode() {
        Address currentAddress = AddressBuilder.anAddress().withPostCode(null).build();

        final Attribute createdAttribute = attributeFactory.createCurrentAddressesAttribute(List.of(currentAddress));

        stubidp.saml.extensions.extensions.Address addressAttributeValue = getAddress(createdAttribute);
        assertThat(addressAttributeValue.getPostCode()).isNull();
    }

    @Test
    void createCurrentAddressAttribute_shouldHandleMissingInternationalPostCode() {
        Address currentAddress = AddressBuilder.anAddress().withInternationalPostCode(null).build();

        final Attribute createdAttribute = attributeFactory.createCurrentAddressesAttribute(List.of(currentAddress));

        stubidp.saml.extensions.extensions.Address addressAttributeValue = getAddress(createdAttribute);
        assertThat(addressAttributeValue.getInternationalPostCode()).isNull();
    }

    @Test
    void createCurrentAddressAttribute_shouldHandleMissingUPRN() {
        Address currentAddress = AddressBuilder.anAddress().withUPRN(null).build();

        final Attribute createdAttribute = attributeFactory.createCurrentAddressesAttribute(List.of(currentAddress));

        stubidp.saml.extensions.extensions.Address addressAttributeValue = getAddress(createdAttribute);
        assertThat(addressAttributeValue.getUPRN()).isNull();
    }

    @Test
    void createPreviousAddressAttribute_shouldSetUpTheAttribute() {
        String line1Value = "1 Cherry Cottage";
        String line2Value = "Wurpel Lane";
        String postCodeValue = "RG99 1YY";
        String internationalPostCodeValue = "RG88 1ZZ";
        LocalDate fromDateValue = LocalDate.parse("2012-11-12");
        LocalDate toDateValue = LocalDate.parse("2012-09-09");
        String uprnValue = "134279";
        Address previousAddress = AddressBuilder.anAddress()
                .withLines(asList(line1Value, line2Value))
                .withPostCode(postCodeValue)
                .withInternationalPostCode(internationalPostCodeValue)
                .withUPRN(uprnValue)
                .withToDate(toDateValue)
                .withFromDate(fromDateValue)
                .build();

        Attribute createdAttribute = attributeFactory.createPreviousAddressesAttribute(Collections.singletonList(previousAddress));

        assertThat(createdAttribute.getName()).isEqualTo("MDS_previousaddress");
        assertThat(createdAttribute.getFriendlyName()).isEqualTo("Previous Address");
        assertThat(createdAttribute.getNameFormat()).isEqualTo(Attribute.UNSPECIFIED);

        stubidp.saml.extensions.extensions.Address addressAttributeValue = getAddress(createdAttribute);
        assertThat(addressAttributeValue.getFrom()).isEqualTo(fromDateValue);
        assertThat(addressAttributeValue.getTo()).isEqualTo(toDateValue);
        assertThat(addressAttributeValue.getLines().get(0).getValue()).isEqualTo(line1Value);
        assertThat(addressAttributeValue.getLines().get(1).getValue()).isEqualTo(line2Value);
        assertThat(addressAttributeValue.getPostCode().getValue()).isEqualTo(postCodeValue);
        assertThat(addressAttributeValue.getInternationalPostCode().getValue()).isEqualTo(internationalPostCodeValue);
        assertThat(addressAttributeValue.getUPRN().getValue()).isEqualTo(uprnValue);
    }

    @Test
    void createPreviousAddressAttribute_shouldHandleMultipleValues() {
        List<Address> previousAddresses = asList(AddressBuilder.anAddress().build(), AddressBuilder.anAddress().build());

        Attribute createdAttribute = attributeFactory.createPreviousAddressesAttribute(previousAddresses);

        assertThat(createdAttribute.getAttributeValues().size()).isEqualTo(2);
    }

    @Test
    void createPreviousAddressAttribute_shouldHandleMissingToDate() {
        Address previousAddress = AddressBuilder.anAddress().withLines(asList("Flat 15", "Dalton Tower")).withToDate(null).build();

        attributeFactory.createPreviousAddressesAttribute(Collections.singletonList(previousAddress));
    }



    @Test
    void createGpg45StatusAttribute_shouldSetUpTheAttribute() {
        String gpg45Status = "waiting";
        Attribute createdAttribute = attributeFactory.createGpg45StatusAttribute(gpg45Status);

        assertThat(createdAttribute.getName()).isEqualTo("FECI_GPG45Status");
        assertThat(createdAttribute.getFriendlyName()).isEqualTo("GPG45Status");
        assertThat(createdAttribute.getNameFormat()).isEqualTo(Attribute.UNSPECIFIED);
        assertThat(createdAttribute.getAttributeValues().size()).isEqualTo(1);

        Gpg45Status gpg45StatusAttribute = (Gpg45Status) createdAttribute.getAttributeValues().get(0);

        assertThat(gpg45StatusAttribute.getValue()).isEqualTo(gpg45Status);
    }

    @Test
    void createIpAddressAttribute_shouldSetUpTheAttribute() {
        String ipAddressValue = "0.9.8.7";
        Attribute createdAttribute = attributeFactory.createUserIpAddressAttribute(ipAddressValue);

        assertThat(createdAttribute.getName()).isEqualTo("TXN_IPaddress");
        assertThat(createdAttribute.getFriendlyName()).isEqualTo("IPAddress");
        assertThat(createdAttribute.getNameFormat()).isEqualTo(Attribute.UNSPECIFIED);
        assertThat(createdAttribute.getAttributeValues().size()).isEqualTo(1);

        IPAddress ipAddressAttributeValue = (IPAddress) createdAttribute.getAttributeValues().get(0);

        assertThat(ipAddressAttributeValue.getValue()).isEqualTo(ipAddressValue);
    }

    @Test
    void createIdpFraudEventIdAttribute_shouldSetUpTheAttribute() {
        String fraudEventId = "fraud-event";
        Attribute createdAttribute = attributeFactory.createIdpFraudEventIdAttribute(fraudEventId);

        assertThat(createdAttribute.getName()).isEqualTo("FECI_IDPFraudEventID");
        assertThat(createdAttribute.getFriendlyName()).isEqualTo("IDPFraudEventID");
        assertThat(createdAttribute.getNameFormat()).isEqualTo(Attribute.UNSPECIFIED);
        assertThat(createdAttribute.getAttributeValues().size()).isEqualTo(1);

        IdpFraudEventId idpFraudEventId = (IdpFraudEventId) createdAttribute.getAttributeValues().get(0);

        assertThat(idpFraudEventId.getValue()).isEqualTo(fraudEventId);
    }

    private stubidp.saml.extensions.extensions.Address getAddress(Attribute createdAttribute) {
        List<XMLObject> addressAttributeValues = createdAttribute.getAttributeValues();
        assertThat(addressAttributeValues.size()).isEqualTo(1);
        return (stubidp.saml.extensions.extensions.Address) addressAttributeValues.get(0);
    }
}
