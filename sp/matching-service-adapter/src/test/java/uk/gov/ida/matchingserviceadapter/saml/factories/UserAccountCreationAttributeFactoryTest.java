package uk.gov.ida.matchingserviceadapter.saml.factories;

import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.core.Attribute;
import stubidp.saml.domain.assertions.Address;
import stubidp.saml.domain.assertions.SimpleMdsValue;
import stubidp.saml.extensions.extensions.impl.AddressImpl;
import stubidp.saml.extensions.extensions.impl.StringBasedMdsAttributeValueImpl;
import stubidp.saml.extensions.extensions.impl.StringValueSamlObjectImpl;
import stubidp.saml.extensions.extensions.impl.VerifiedImpl;
import stubidp.saml.test.OpenSAMLRunner;
import stubidp.saml.utils.core.OpenSamlXmlObjectFactory;
import uk.gov.ida.matchingserviceadapter.builders.AddressBuilder;
import uk.gov.ida.matchingserviceadapter.builders.SimpleMdsValueBuilder;
import uk.gov.ida.matchingserviceadapter.domain.UserAccountCreationAttribute;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

public class UserAccountCreationAttributeFactoryTest extends OpenSAMLRunner {

    private final UserAccountCreationAttributeFactory factory = new UserAccountCreationAttributeFactory(new OpenSamlXmlObjectFactory());

    @Test
    public void firstNameAttribute() {
        assertSimpleMdsAttribute(factory::createUserAccountCreationFirstNameAttribute, "firstname", "Joe");
    }

    @Test
    public void middleNameAttribute() {
        assertSimpleMdsAttribute(factory::createUserAccountCreationMiddleNameAttribute, "middlename", "John");
    }

    @Test
    public void surnameAttribute() {
        assertSimpleMdsAttribute(factory::createUserAccountCreationSurnameAttribute, "surname", "Bloggs");
    }

    @Test
    public void dateOfBirthAttribute() {
        assertSimpleMdsAttribute(factory::createUserAccountCreationDateOfBirthAttribute, "dateofbirth", LocalDate.parse("2016-04-29"));
    }

    @Test
    public void currentAddressAttribute() {
        Address address = AddressBuilder.aCurrentAddress().withPostCode("HD7 5UZ").build();
        Attribute addressAttribute = factory.createUserAccountCreationCurrentAddressAttribute(address);

        assertThat(addressAttribute.getName()).isEqualTo("currentaddress");
        AddressImpl castXMLObject = (AddressImpl) addressAttribute.getAttributeValues().get(0);
        assertThat(castXMLObject.getPostCode().getValue()).isEqualTo("HD7 5UZ");
    }

    @Test
    public void addressHistoryAttribute() {
        List<Address> historicalAddresses = new ArrayList<>();
        String[] postcodes = {"SW1A 0AA", "SW1A 1AA", "SW1A 2AA"};
        for (String postcode : postcodes) {
            historicalAddresses.add(AddressBuilder.aHistoricalAddress().withPostCode(postcode).build());
        }
        Attribute addressHistoryAttribute = factory.createUserAccountCreationAddressHistoryAttribute(historicalAddresses);

        assertThat(addressHistoryAttribute.getName()).isEqualTo("addresshistory");
        for (int i=0; i<3; i++) {
            AddressImpl castXMLObject = (AddressImpl) addressHistoryAttribute.getAttributeValues().get(i);
            assertThat(castXMLObject.getPostCode().getValue()).isEqualTo(String.format("SW1A %dAA", i));
        }
    }

    @Test
    public void verifiedAttribute() {
        Attribute verifiedAttribute = factory.createUserAccountCreationVerifiedAttribute(UserAccountCreationAttribute.FIRST_NAME, true);

        assertThat(verifiedAttribute.getName()).isEqualTo("firstname");
        VerifiedImpl castXMLObject = (VerifiedImpl) verifiedAttribute.getAttributeValues().get(0);
        assertThat(castXMLObject.getValue()).isTrue();
    }

    @Test
    public void cycle3DataAttributes() {
        List<String> attributes = Arrays.asList("Attribute 0", "Attribute 1", "Attribute 2");
        Attribute cycle3DataAttributes = factory.createUserAccountCreationCycle3DataAttributes(attributes);

        assertThat(cycle3DataAttributes.getName()).isEqualTo("cycle_3");
        for (int i=0; i<3; i++) {
            StringBasedMdsAttributeValueImpl castXMLObject = (StringBasedMdsAttributeValueImpl) cycle3DataAttributes.getAttributeValues().get(i);
            assertThat(castXMLObject.getValue()).isEqualTo(String.format("Attribute %d", i));
        }
    }

    private <T, E extends StringValueSamlObjectImpl> void assertSimpleMdsAttribute(
                Function<SimpleMdsValue<T>, Attribute> attributeFunction,
                String attributeName,
                T attributeValue) {
        SimpleMdsValue<T> mdsValue = SimpleMdsValueBuilder.<T>aCurrentSimpleMdsValue().withValue(attributeValue).build();
        Attribute attribute = attributeFunction.apply(mdsValue);

        assertThat(attribute.getName()).isEqualTo(attributeName);
        StringValueSamlObjectImpl attributeValueAsType = (StringValueSamlObjectImpl) attribute.getAttributeValues().get(0);
        assertThat(attributeValueAsType.getValue()).isEqualTo(attributeValue.toString());
    }

}
