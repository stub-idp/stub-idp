package uk.gov.ida.matchingserviceadapter.validators;

import com.google.inject.Inject;
import org.joda.time.DateTime;
import org.opensaml.saml.saml2.core.Conditions;
import uk.gov.ida.matchingserviceadapter.validators.validationrules.ConditionsElementMustNotBeNull;
import uk.gov.ida.matchingserviceadapter.validators.validationrules.ConditionsShouldNotContainProxyRestrictionElement;
import uk.gov.ida.saml.core.validation.conditions.AudienceRestrictionValidator;

public class CountryConditionsValidator implements ConditionsValidator {

    private final AssertionTimeRestrictionValidator timeRestrictionValidator;
    private final AudienceRestrictionValidator audienceRestrictionValidator;

    @Inject
    public CountryConditionsValidator(
        AssertionTimeRestrictionValidator timeRestrictionValidator,
        AudienceRestrictionValidator audienceRestrictionValidator
    ) {
        this.timeRestrictionValidator = timeRestrictionValidator;
        this.audienceRestrictionValidator = audienceRestrictionValidator;
    }

    public void validate(Conditions conditionsElement, String... acceptableEntityIds) {
        ConditionsElementMustNotBeNull.validate(conditionsElement);
        ConditionsShouldNotContainProxyRestrictionElement.validate(conditionsElement);

        DateTime notOnOrAfter = conditionsElement.getNotOnOrAfter();
        if (notOnOrAfter != null) {
            timeRestrictionValidator.validateNotOnOrAfter(notOnOrAfter);
        }

        timeRestrictionValidator.validateNotBefore(conditionsElement.getNotBefore());
        audienceRestrictionValidator.validate(conditionsElement.getAudienceRestrictions(), acceptableEntityIds);
    }
}
