package uk.gov.ida.matchingserviceadapter.validators;

import org.opensaml.saml.saml2.core.AttributeQuery;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import stubidp.saml.extensions.validation.SamlTransformationErrorException;
import stubidp.saml.extensions.validation.SamlValidationResponse;
import stubidp.saml.extensions.validation.SamlValidationSpecificationFailure;
import stubidp.saml.security.SamlMessageSignatureValidator;

import javax.inject.Inject;

public class AttributeQuerySignatureValidator {

    private final SamlMessageSignatureValidator samlMessageSignatureValidator;

    @Inject
    public AttributeQuerySignatureValidator(SamlMessageSignatureValidator samlMessageSignatureValidator) {
        this.samlMessageSignatureValidator = samlMessageSignatureValidator;
    }

    public void validate(AttributeQuery samlMessage) {
        SamlValidationResponse samlValidationResponse = samlMessageSignatureValidator.validate(samlMessage, SPSSODescriptor.DEFAULT_ELEMENT_NAME);
        if (!samlValidationResponse.isOK()) {
            SamlValidationSpecificationFailure failure = samlValidationResponse.getSamlValidationSpecificationFailure();
            if (samlValidationResponse.getCause() != null) {
                throw new SamlTransformationErrorException(failure.getErrorMessage(), samlValidationResponse.getCause(), failure.getLogLevel());
            }
            throw new SamlTransformationErrorException(failure.getErrorMessage(), failure.getLogLevel());
        }
    }
}