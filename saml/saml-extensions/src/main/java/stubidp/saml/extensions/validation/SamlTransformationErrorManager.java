package stubidp.saml.extensions.validation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SamlTransformationErrorManager {

    private static final String VALIDATION_WARNING_FORMAT = "SAML Validation Warning - message: {}";

    private static final Logger LOG = LoggerFactory.getLogger(SamlTransformationErrorManager.class);

    private SamlTransformationErrorManager() {
    }

    public static void warn(SamlValidationSpecificationFailure warning) {
        LOG.warn(VALIDATION_WARNING_FORMAT, warning.getErrorMessage());
    }

    public static void warn(SamlValidationSpecificationWarning warning) {
        LOG.warn(VALIDATION_WARNING_FORMAT, warning.getErrorMessage());
    }

}
