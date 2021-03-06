package stubidp.saml.hub.core.validators.subjectconfirmation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.core.SubjectConfirmation;
import stubidp.saml.extensions.validation.SamlValidationSpecificationFailure;
import stubidp.saml.hub.core.errors.SamlTransformationErrorFactory;
import stubidp.saml.test.OpenSAMLRunner;
import stubidp.saml.test.support.SamlTransformationErrorManagerTestHelper;
import stubidp.saml.test.builders.SubjectConfirmationDataBuilder;
import stubidp.test.devpki.TestEntityIds;

import static stubidp.saml.test.builders.SubjectConfirmationBuilder.aSubjectConfirmation;

public class AssertionSubjectConfirmationValidatorTest extends OpenSAMLRunner {

    private static final String REQUEST_ID = "some-request-id";

    private AssertionSubjectConfirmationValidator validator;

    @BeforeEach
    void setup() {
        validator = new AssertionSubjectConfirmationValidator();
    }

    @Test
    void validate_shouldThrowExceptionIfSubjectConfirmationDataRecipientAttributeDoesNotMatchTheExpectedIssuerId() {
        final String expectedRecipientId = TestEntityIds.HUB_ENTITY_ID;
        final String actualRecipientId = TestEntityIds.TEST_RP;
        final SubjectConfirmation subjectConfirmation = aSubjectConfirmation()
                .withSubjectConfirmationData(createSubjectConfirmationDataBuilder()
                        .withRecipient(actualRecipientId)
                        .build())
                .build();
        assertExceptionMessage(subjectConfirmation, SamlTransformationErrorFactory.incorrectRecipientFormat(actualRecipientId, expectedRecipientId), expectedRecipientId);
    }

    @Test
    void validate_shouldThrowExceptionIfSubjectConfirmationDataInResponseToAttributeIsNotTheOriginalRequestId() {
        final String subjectInResponseTo = "an-incorrect-request-id";
        final SubjectConfirmation subjectConfirmation = aSubjectConfirmation()
                .withSubjectConfirmationData(SubjectConfirmationDataBuilder.aSubjectConfirmationData()
                        .withInResponseTo(subjectInResponseTo)
                        .build())
                .build();

        assertExceptionMessage(
                subjectConfirmation,
                SamlTransformationErrorFactory.notMatchInResponseTo(subjectInResponseTo, REQUEST_ID),
                subjectConfirmation.getSubjectConfirmationData().getRecipient());
    }

    private void assertExceptionMessage(
            final SubjectConfirmation subjectConfirmation,
            SamlValidationSpecificationFailure failure, final String recipient) {

        SamlTransformationErrorManagerTestHelper.validateFail(
                () -> validator.validate(subjectConfirmation, REQUEST_ID, recipient),
                failure
        );
    }

    private SubjectConfirmationDataBuilder createSubjectConfirmationDataBuilder() {
        return SubjectConfirmationDataBuilder.aSubjectConfirmationData().withInResponseTo(REQUEST_ID);
    }
}
