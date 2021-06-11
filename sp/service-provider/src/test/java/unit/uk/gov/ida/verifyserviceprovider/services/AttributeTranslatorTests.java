package unit.uk.gov.ida.verifyserviceprovider.services;

import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;
import stubidp.saml.test.OpenSAMLRunner;
import stubidp.saml.test.builders.AddressAttributeBuilder_1_1;
import stubidp.saml.test.builders.AddressAttributeValueBuilder_1_1;
import stubidp.saml.test.builders.SimpleStringAttributeBuilder;
import uk.gov.ida.verifyserviceprovider.dto.Attributes;
import uk.gov.ida.verifyserviceprovider.exceptions.FailedToRequestVerifiedException;
import uk.gov.ida.verifyserviceprovider.exceptions.RequestedOnlyVerifiedException;
import uk.gov.ida.verifyserviceprovider.services.AttributeTranslator;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import static common.uk.gov.ida.verifyserviceprovider.utils.SamlResponseHelper.createVerifiedAttribute;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static stubidp.saml.test.builders.AttributeStatementBuilder.anAttributeStatement;

public class AttributeTranslatorTests extends OpenSAMLRunner {

    @Test
    public void shouldReturnCorrectRequestedAttributes() {
        AttributeStatement attributeStatement = anAttributeStatement()
                .addAttribute(new SimpleStringAttributeBuilder()
                        .withName("firstname")
                        .withSimpleStringValue("Bob")
                        .build())
                .addAttribute(createVerifiedAttribute("firstname_verified", false))
                .build();

        Attributes result = AttributeTranslator.translateAttributes(attributeStatement);

        assertThat(result.getFirstName()).isNotNull();
    }

    @Test
    public void shouldReturnAllSimpleRequestedAttributes() {
        AttributeStatement attributeStatement = anAttributeStatement()
                .addAttribute(new SimpleStringAttributeBuilder()
                        .withName("firstname")
                        .withSimpleStringValue("Joe")
                        .build())
                .addAttribute(createVerifiedAttribute("firstname_verified", true))
                .addAttribute(new SimpleStringAttributeBuilder()
                        .withName("middlename")
                        .withSimpleStringValue("Bob")
                        .build())
                .addAttribute(createVerifiedAttribute("middlename_verified", false))
                .addAttribute(new SimpleStringAttributeBuilder()
                        .withName("surname")
                        .withSimpleStringValue("Bloggs")
                        .build())
                .addAttribute(createVerifiedAttribute("surname_verified", true))
                .addAttribute(new SimpleStringAttributeBuilder()
                        .withName("dateofbirth")
                        .withSimpleStringValue("1977-07-21")
                        .build())
                .addAttribute(createVerifiedAttribute("dateofbirth_verified", true))
                .addAttribute(new SimpleStringAttributeBuilder()
                        .withName("cycle_3")
                        .withSimpleStringValue("1")
                        .build())
                .build();

        Attributes result = AttributeTranslator.translateAttributes(attributeStatement);

        assertThat(result.getFirstName()).isNotNull();
        assertThat(result.getMiddleName()).isNotNull();
        assertThat(result.getSurname()).isNotNull();
        assertThat(result.getDateOfBirth()).isNotNull();
        assertThat(result.getCycle3()).isNotEmpty();
    }

    @Test
    public void shouldReturnAddressAttribute() {
        Attribute addressAttribute = new AddressAttributeBuilder_1_1()
                .addAddress(new AddressAttributeValueBuilder_1_1()
                        .addLines(Arrays.asList("10 Whitechapel High St", "London"))
                        .withPostcode("E1 8DX")
                        .withFrom(LocalDate.parse("2017-07-03"))
                        .withTo(LocalDate.parse("2017-07-30"))
                        .build())
                .buildCurrentAddress();
        addressAttribute.setName("currentaddress");

        AttributeStatement attributeStatement = anAttributeStatement()
                .addAttribute(addressAttribute)
                .addAttribute(createVerifiedAttribute("currentaddress_verified", true))
                .build();

        Attributes result = AttributeTranslator.translateAttributes(attributeStatement);

        assertThat(result.getAddress()).isNotNull();
    }

    @Test
    public void shouldReturnAddressHistoryAttribute() {
        Attribute addressHistoryAttribute = new AddressAttributeBuilder_1_1()
                .addAddress(new AddressAttributeValueBuilder_1_1()
                        .addLines(Arrays.asList("10 Whitechapel High St", "London"))
                        .withPostcode("E1 8DX")
                        .withFrom(LocalDate.parse("2017-07-03"))
                        .withTo(LocalDate.parse("2017-07-30"))
                        .withVerified(true)
                        .build())
                .addAddress(new AddressAttributeValueBuilder_1_1()
                        .addLines(Arrays.asList("42 Old Road", "London"))
                        .withPostcode("W1 0AA")
                        .withFrom(LocalDate.parse("2015-01-01"))
                        .withTo(LocalDate.parse("2017-07-03"))
                        .withVerified(true)
                        .build())
                .buildPreviousAddress();
        addressHistoryAttribute.setName("addresshistory");

        AttributeStatement attributeStatement = anAttributeStatement()
                .addAttribute(addressHistoryAttribute)
                .build();

        Attributes result = AttributeTranslator.translateAttributes(attributeStatement);

        assertThat(result.getAddressHistory()).isNotNull();
        assertThat(result.getAddressHistory().size()).isEqualTo(2);
    }

    @Test
    public void shouldIncludeEmptyAttributes() {
        AttributeStatement attributeStatement = anAttributeStatement()
                .addAttribute(new SimpleStringAttributeBuilder()
                        .withName("firstname")
                        .withSimpleStringValue("")
                        .build())
                .addAttribute(createVerifiedAttribute("firstname_verified", true))
                .build();

        Attributes result = AttributeTranslator.translateAttributes(attributeStatement);

        assertThat(result.getFirstName().getValue()).isEmpty();
    }

    @Test
    public void shouldNotReturnUnrequestedAttributes() {
        AttributeStatement attributeStatement = anAttributeStatement()
                .addAttribute(new SimpleStringAttributeBuilder()
                        .withName("firstname")
                        .withSimpleStringValue("Bob")
                        .build())
                .addAttribute(createVerifiedAttribute("firstname_verified", true))
                .build();

        Attributes result = AttributeTranslator.translateAttributes(attributeStatement);

        assertThat(result.getSurname()).isNull();
    }

    @Test
    public void shouldReturnCorrectValuesForSimpleAttributes() {
        String firstName = "Joe";
        String middleName = "Bob";
        String surname = "Bloggs";
        String cycle3 = "123456";

        AttributeStatement attributeStatement = anAttributeStatement()
                .addAttribute(new SimpleStringAttributeBuilder()
                        .withName("firstname")
                        .withSimpleStringValue(firstName)
                        .build())
                .addAttribute(createVerifiedAttribute("firstname_verified", true))
                .addAttribute(new SimpleStringAttributeBuilder()
                        .withName("middlename")
                        .withSimpleStringValue(middleName)
                        .build())
                .addAttribute(createVerifiedAttribute("middlename_verified", false))
                .addAttribute(new SimpleStringAttributeBuilder()
                        .withName("surname")
                        .withSimpleStringValue(surname)
                        .build())
                .addAttribute(createVerifiedAttribute("surname_verified", true))
                .addAttribute(new SimpleStringAttributeBuilder()
                        .withName("dateofbirth")
                        .withSimpleStringValue("1977-07-21")
                        .build())
                .addAttribute(createVerifiedAttribute("dateofbirth_verified", true))
                .addAttribute(new SimpleStringAttributeBuilder()
                        .withName("cycle_3")
                        .withSimpleStringValue(cycle3)
                        .build())
                .build();

        Attributes result = AttributeTranslator.translateAttributes(attributeStatement);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        assertThat(result.getFirstName().getValue()).isEqualTo(firstName);
        assertThat(result.getMiddleName().getValue()).isEqualTo(middleName);
        assertThat(result.getSurname().getValue()).isEqualTo(surname);
        assertThat(result.getDateOfBirth().getValue().format(formatter)).isEqualTo("1977-07-21");
        assertThat(result.getCycle3()).isEqualTo(cycle3);
    }

    @Test
    public void shouldReturnCorrectValuesForAddressAttribute() {
        List<String> lines = Arrays.asList("10 Whitechapel High St", "London");
        String postCode = "E1 8DX";
        LocalDate from = LocalDate.parse("2017-07-03");
        LocalDate to = LocalDate.parse("2017-07-30");

        Attribute addressAttribute = new AddressAttributeBuilder_1_1()
                .addAddress(new AddressAttributeValueBuilder_1_1()
                        .addLines(lines)
                        .withPostcode(postCode)
                        .withFrom(from)
                        .withTo(to)
                        .build())
                .buildCurrentAddress();
        addressAttribute.setName("currentaddress");

        AttributeStatement attributeStatement = anAttributeStatement()
                .addAttribute(addressAttribute)
                .addAttribute(createVerifiedAttribute("currentaddress_verified", true))
                .build();

        Attributes result = AttributeTranslator.translateAttributes(attributeStatement);

        assertThat(result.getAddress().getValue().getLines()).isEqualTo(lines);
        assertThat(result.getAddress().getValue().getPostCode()).isEqualTo(postCode);
        assertThat(result.getAddress().getValue().getFromDate()).hasToString(from.toString());
        assertThat(result.getAddress().getValue().getToDate()).hasToString(to.toString());
    }

    @Test
    public void shouldThrowExceptionWhenVerifiedNotRequested() {
        AttributeStatement attributeStatement = anAttributeStatement()
                .addAttribute(new SimpleStringAttributeBuilder()
                        .withName("firstname")
                        .withSimpleStringValue("Bob")
                        .build())
                .build();

        assertThatExceptionOfType(FailedToRequestVerifiedException.class)
                .isThrownBy(() -> AttributeTranslator.translateAttributes(attributeStatement));
    }

    @Test
    public void shouldThrowExceptionWhenOnlyRequestingVerified() {
        AttributeStatement attributeStatement = anAttributeStatement()
                .addAttribute(createVerifiedAttribute("firstname_verified", true))
                .build();

        assertThatExceptionOfType(RequestedOnlyVerifiedException.class)
                .isThrownBy(() -> AttributeTranslator.translateAttributes(attributeStatement));
    }
}
