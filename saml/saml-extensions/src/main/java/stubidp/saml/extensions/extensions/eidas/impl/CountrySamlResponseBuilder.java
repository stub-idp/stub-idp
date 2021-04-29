package stubidp.saml.extensions.extensions.eidas.impl;

import org.opensaml.saml.common.AbstractSAMLObjectBuilder;
import stubidp.saml.extensions.extensions.eidas.CountrySamlResponse;

public class CountrySamlResponseBuilder extends AbstractSAMLObjectBuilder<CountrySamlResponse> {
    @Override
    public CountrySamlResponse buildObject() { return buildObject(CountrySamlResponse.DEFAULT_ELEMENT_NAME, CountrySamlResponse.TYPE_NAME); }

    public CountrySamlResponse buildObject(String namespaceURI, String localName, String namespacePrefix) {
        return new CountrySamlResponseImpl(namespaceURI, localName, namespacePrefix);
    }
}
