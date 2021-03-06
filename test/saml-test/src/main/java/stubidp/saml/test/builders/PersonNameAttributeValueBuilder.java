package stubidp.saml.test.builders;

import org.opensaml.saml.saml2.core.AttributeValue;
import stubidp.saml.extensions.extensions.PersonName;
import stubidp.saml.test.OpenSamlXmlObjectFactory;

import java.time.LocalDate;
import java.util.Optional;

public class PersonNameAttributeValueBuilder {
    private static final OpenSamlXmlObjectFactory openSamlXmlObjectFactory = new OpenSamlXmlObjectFactory();

    private Optional<LocalDate> from = Optional.empty();
    private Optional<LocalDate> to = Optional.empty();
    private String value = "John";
    private final Optional<String> language = Optional.empty();
    private Optional<Boolean> verified = Optional.empty();

    private PersonNameAttributeValueBuilder() {}

    public static PersonNameAttributeValueBuilder aPersonNameValue() {
        return new PersonNameAttributeValueBuilder();
    }

    public AttributeValue build() {
        PersonName personNameAttributeValue = openSamlXmlObjectFactory.createPersonNameAttributeValue(value);

        from.ifPresent(personNameAttributeValue::setFrom);
        to.ifPresent(personNameAttributeValue::setTo);
        verified.ifPresent(personNameAttributeValue::setVerified);
        language.ifPresent(personNameAttributeValue::setLanguage);

        return personNameAttributeValue;
    }

    public PersonNameAttributeValueBuilder withFrom(LocalDate from) {
        this.from = Optional.ofNullable(from);
        return this;
    }

    public PersonNameAttributeValueBuilder withTo(LocalDate to) {
        this.to = Optional.ofNullable(to);
        return this;
    }

    public PersonNameAttributeValueBuilder withValue(String name) {
        this.value = name;
        return this;
    }

    public PersonNameAttributeValueBuilder withVerified(Boolean verified) {
        this.verified = Optional.ofNullable(verified);
        return this;
    }
}
