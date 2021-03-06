package stubidp.saml.test.builders;

import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static stubidp.saml.test.builders.AttributeStatementBuilder.anAttributeStatement;

public class MatchingDatasetAttributeStatementBuilder_1_1 {
    private Optional<Attribute> dateOfBirthAttribute = Optional.ofNullable(DateAttributeBuilder_1_1.aDate_1_1().buildAsDateOfBirth());
    private Optional<Attribute> currentAddressAttribute = Optional.ofNullable(AddressAttributeBuilder_1_1.anAddressAttribute().addAddress(AddressAttributeValueBuilder_1_1.anAddressAttributeValue().build()).buildCurrentAddress());
    private Optional<Attribute> surnameAttribute = Optional.ofNullable(PersonNameAttributeBuilder_1_1.aPersonName_1_1().buildAsSurname());
    private Optional<Attribute> firstnameAttribute = Optional.ofNullable(PersonNameAttributeBuilder_1_1.aPersonName_1_1().buildAsFirstname());
    private final List<Attribute> customAttributes = new ArrayList<>();
    private final List<Attribute> previousAddressAttributes = new ArrayList<>();
    private Optional<Attribute> middleNamesAttribute = Optional.ofNullable(PersonNameAttributeBuilder_1_1.aPersonName_1_1().buildAsMiddlename());
    private Optional<Attribute> genderAttribute = Optional.ofNullable(GenderAttributeBuilder_1_1.aGender_1_1().build());

    private MatchingDatasetAttributeStatementBuilder_1_1() {}

    public static MatchingDatasetAttributeStatementBuilder_1_1 aMatchingDatasetAttributeStatement_1_1() {
        return new MatchingDatasetAttributeStatementBuilder_1_1();
    }

    public static MatchingDatasetAttributeStatementBuilder_1_1 anEmptyMatchingDatasetAttributeStatement_1_1() {
        final MatchingDatasetAttributeStatementBuilder_1_1 builder = new MatchingDatasetAttributeStatementBuilder_1_1();
        builder.withCurrentAddress(null);
        builder.withDateOfBirth(null);
        builder.withoutFirstnames();
        builder.withoutSurnames();
        builder.withoutMiddleNames();
        builder.withGender(null);
        return builder;
    }

    public AttributeStatement build() {
        AttributeStatementBuilder attributeStatementBuilder = anAttributeStatement();

        firstnameAttribute.ifPresent(attributeStatementBuilder::addAttribute);

        middleNamesAttribute.ifPresent(attributeStatementBuilder::addAttribute);

        surnameAttribute.ifPresent(attributeStatementBuilder::addAttribute);

        dateOfBirthAttribute.ifPresent(attributeStatementBuilder::addAttribute);

        genderAttribute.ifPresent(attributeStatementBuilder::addAttribute);

        currentAddressAttribute.ifPresent(attributeStatementBuilder::addAttribute);

        attributeStatementBuilder.addAllAttributes(previousAddressAttributes);
        attributeStatementBuilder.addAllAttributes(customAttributes);

        return attributeStatementBuilder.build();
    }

    public MatchingDatasetAttributeStatementBuilder_1_1 withMiddleNames(Attribute attribute) {
        this.middleNamesAttribute = Optional.ofNullable(attribute);
        return this;
    }

    public MatchingDatasetAttributeStatementBuilder_1_1 withoutFirstnames() {
        this.firstnameAttribute = Optional.empty();
        return this;
    }

    public MatchingDatasetAttributeStatementBuilder_1_1 withoutMiddleNames() {
        this.middleNamesAttribute = Optional.empty();
        return this;
    }

    public MatchingDatasetAttributeStatementBuilder_1_1 withoutSurnames() {
        this.surnameAttribute = Optional.empty();
        return this;
    }

    public MatchingDatasetAttributeStatementBuilder_1_1 withSurname(Attribute attribute) {
        this.surnameAttribute = Optional.ofNullable(attribute);
        return this;
    }

    public MatchingDatasetAttributeStatementBuilder_1_1 withFirstname(Attribute attribute) {
        this.firstnameAttribute = Optional.ofNullable(attribute);
        return this;
    }

    public MatchingDatasetAttributeStatementBuilder_1_1 withDateOfBirth(Attribute dateOfBirthAttribute) {
        this.dateOfBirthAttribute = Optional.ofNullable(dateOfBirthAttribute);
        return this;
    }

    public MatchingDatasetAttributeStatementBuilder_1_1 withDateOfBirth() {
        this.dateOfBirthAttribute = Optional.ofNullable(DateAttributeBuilder_1_1.aDate_1_1().buildAsDateOfBirth());
        return this;
    }

    public MatchingDatasetAttributeStatementBuilder_1_1 withGender(Attribute genderAttribute) {
        this.genderAttribute = Optional.ofNullable(genderAttribute);
        return this;
    }

    public MatchingDatasetAttributeStatementBuilder_1_1 withGender() {
        this.dateOfBirthAttribute = Optional.ofNullable(GenderAttributeBuilder_1_1.aGender_1_1().build());
        return this;
    }

    public MatchingDatasetAttributeStatementBuilder_1_1 withCurrentAddress(Attribute currentAddressAttribute) {
        this.currentAddressAttribute = Optional.ofNullable(currentAddressAttribute);
        return this;
    }

    public MatchingDatasetAttributeStatementBuilder_1_1 withCurrentAddress() {
        this.currentAddressAttribute = Optional.ofNullable(AddressAttributeBuilder_1_1.anAddressAttribute().addAddress(AddressAttributeValueBuilder_1_1.anAddressAttributeValue().build()).buildCurrentAddress());
        return this;
    }

    public MatchingDatasetAttributeStatementBuilder_1_1 addPreviousAddress(Attribute previousAddressAttribute) {
        this.previousAddressAttributes.add(previousAddressAttribute);
        return this;
    }

    public MatchingDatasetAttributeStatementBuilder_1_1 addPreviousAddress() {
        this.previousAddressAttributes.add(AddressAttributeBuilder_1_1.anAddressAttribute().addAddress(AddressAttributeValueBuilder_1_1.anAddressAttributeValue().build()).buildPreviousAddress());
        return this;
    }

    public MatchingDatasetAttributeStatementBuilder_1_1 addCustomAttribute(Attribute customAttribute) {
        this.customAttributes.add(customAttribute);
        return this;
    }
}
