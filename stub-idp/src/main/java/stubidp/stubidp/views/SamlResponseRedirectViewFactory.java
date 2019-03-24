package stubidp.stubidp.views;

import javax.inject.Inject;

/*
 * Generate the HTML for a SAML Redirect
 */
public class SamlResponseRedirectViewFactory extends SamlMessageRedirectViewFactory {

    @Inject
    public SamlResponseRedirectViewFactory(){
        super(SamlMessageType.SAML_RESPONSE);
    }
}