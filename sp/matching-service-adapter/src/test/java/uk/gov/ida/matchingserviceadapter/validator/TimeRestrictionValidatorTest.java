package uk.gov.ida.matchingserviceadapter.validator;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.gov.ida.matchingserviceadapter.validators.DateTimeComparator;
import uk.gov.ida.matchingserviceadapter.validators.AssertionTimeRestrictionValidator;
import uk.gov.ida.saml.core.validation.SamlResponseValidationException;

import static org.joda.time.DateTimeZone.UTC;
import static org.joda.time.format.ISODateTimeFormat.dateHourMinuteSecond;

public class TimeRestrictionValidatorTest {

    private DateTimeComparator dateTimeComparator;

    private AssertionTimeRestrictionValidator validator;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() {
        dateTimeComparator = new DateTimeComparator(new Duration(5000)  );

        validator = new AssertionTimeRestrictionValidator(dateTimeComparator);
    }

    @Test
    public void validateNotOnOrAfterShouldThrowExceptionWhenNotOnOrAfterIsBeforeNow() {
        DateTime notOnOrAfter = new DateTime();
        exception.expect(SamlResponseValidationException.class);
        exception.expectMessage(String.format(
            "Assertion is not valid on or after %s",
            notOnOrAfter.withZone(UTC).toString(dateHourMinuteSecond())
        ));

        validator.validateNotOnOrAfter(notOnOrAfter);
    }

    @Test
    public void validateNotBeforeShouldThrowExceptionWhenNotBeforeIsAfterNow() {
        DateTime notBefore = new DateTime().plus(6000);
        exception.expect(SamlResponseValidationException.class);
        exception.expectMessage(String.format(
            "Assertion is not valid before %s",
            notBefore.withZone(UTC).toString(dateHourMinuteSecond())
        ));

        validator.validateNotBefore(notBefore);
    }
}