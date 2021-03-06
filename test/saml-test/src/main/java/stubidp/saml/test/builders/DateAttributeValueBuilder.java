package stubidp.saml.test.builders;

import org.opensaml.saml.saml2.core.AttributeValue;
import stubidp.saml.extensions.extensions.Date;
import stubidp.saml.test.OpenSamlXmlObjectFactory;

import java.time.LocalDate;
import java.util.Optional;

public class DateAttributeValueBuilder {
    private static final OpenSamlXmlObjectFactory openSamlXmlObjectFactory = new OpenSamlXmlObjectFactory();

    private Optional<LocalDate> from = Optional.empty();
    private Optional<LocalDate> to = Optional.empty();
    private String value = "1991-04-12";
    private Optional<Boolean> verified = Optional.empty();

    private DateAttributeValueBuilder() {}

    public static DateAttributeValueBuilder aDateValue() {
        return new DateAttributeValueBuilder();
    }

    public AttributeValue build() {
        Date dateAttributeValue = openSamlXmlObjectFactory.createDateAttributeValue(value);
        from.ifPresent(dateAttributeValue::setFrom);
        to.ifPresent(dateAttributeValue::setTo);
        verified.ifPresent(dateAttributeValue::setVerified);
        return dateAttributeValue;
    }

    public DateAttributeValueBuilder withFrom(LocalDate from) {
        this.from = Optional.ofNullable(from);
        return this;
    }

    public DateAttributeValueBuilder withTo(LocalDate to) {
        this.to = Optional.ofNullable(to);
        return this;
    }

    public DateAttributeValueBuilder withValue(String name) {
        this.value = name;
        return this;
    }

    public DateAttributeValueBuilder withVerified(Boolean verified) {
        this.verified = Optional.ofNullable(verified);
        return this;
    }
}
