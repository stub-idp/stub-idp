package uk.gov.ida.verifyserviceprovider.exceptions;

import stubidp.saml.utils.core.validation.SamlResponseValidationException;

public class FailedToRequestVerifiedException extends SamlResponseValidationException {
    public FailedToRequestVerifiedException() {
        super("Invalid attributes request: Cannot request attribute without requesting verification status. Please check your MSA configuration settings.");
    }
}
