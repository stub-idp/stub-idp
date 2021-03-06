package stubidp.saml.test.builders;

import org.opensaml.saml.saml2.metadata.EmailAddress;

import java.net.URI;

public class EmailAddressBuilder {
    private URI value = URI.create("mailto:fred@flintstone.com");

    private EmailAddressBuilder() {}

    public static EmailAddressBuilder anEmailAddress() {
        return new EmailAddressBuilder();
    }

    public EmailAddress build() {
        EmailAddress emailAddress = new org.opensaml.saml.saml2.metadata.impl.EmailAddressBuilder().buildObject();
        if (value != null) {
            emailAddress.setURI(value.toString());
        }
        return emailAddress;
    }

    public EmailAddressBuilder withAddress(URI address) {
        this.value = address;
        return this;
    }
}
