package stubidp.saml.hub.validators.response.matchingservice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.NameIDType;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Status;
import org.opensaml.saml.saml2.core.StatusCode;
import stubidp.saml.extensions.domain.SamlStatusCode;
import stubidp.saml.extensions.validation.SamlValidationSpecificationFailure;
import stubidp.saml.hub.core.errors.SamlTransformationErrorFactory;
import stubidp.saml.test.OpenSAMLRunner;

import static stubidp.saml.hub.core.errors.SamlTransformationErrorFactory.emptyIssuer;
import static stubidp.saml.hub.core.errors.SamlTransformationErrorFactory.illegalIssuerFormat;
import static stubidp.saml.hub.core.errors.SamlTransformationErrorFactory.missingId;
import static stubidp.saml.hub.core.errors.SamlTransformationErrorFactory.missingInResponseTo;
import static stubidp.saml.hub.core.errors.SamlTransformationErrorFactory.missingIssuer;
import static stubidp.saml.hub.core.errors.SamlTransformationErrorFactory.missingSignature;
import static stubidp.saml.hub.core.errors.SamlTransformationErrorFactory.missingSuccessUnEncryptedAssertions;
import static stubidp.saml.hub.core.errors.SamlTransformationErrorFactory.nonSuccessHasUnEncryptedAssertions;
import static stubidp.saml.hub.core.errors.SamlTransformationErrorFactory.signatureNotSigned;
import static stubidp.saml.hub.core.errors.SamlTransformationErrorFactory.subStatusMustBeOneOf;
import static stubidp.saml.hub.core.errors.SamlTransformationErrorFactory.unencryptedAssertion;
import static stubidp.saml.hub.core.errors.SamlTransformationErrorFactory.unexpectedNumberOfAssertions;
import static stubidp.saml.hub.validators.response.helpers.ResponseValidatorTestHelper.createStatus;
import static stubidp.saml.hub.validators.response.helpers.ResponseValidatorTestHelper.createSubStatusCode;
import static stubidp.saml.test.builders.AssertionBuilder.anAssertion;
import static stubidp.saml.test.builders.IssuerBuilder.anIssuer;
import static stubidp.saml.test.builders.ResponseBuilder.aResponse;
import static stubidp.saml.test.support.SamlTransformationErrorManagerTestHelper.validateFail;

class EncryptedResponseFromMatchingServiceValidatorTest extends OpenSAMLRunner {

    private Status happyStatus;

    private EncryptedResponseFromMatchingServiceValidator validator;

    @BeforeEach
    void setUp() {
        happyStatus = createStatus(StatusCode.SUCCESS, createSubStatusCode(SamlStatusCode.MATCH));
        validator = new EncryptedResponseFromMatchingServiceValidator();
    }

    @Test
    void validate_shouldThrowExceptionIfIdIsMissing() throws Exception {
        Response response = aResponse().withId(null).build();

        assertValidationFailure(response, missingId());
    }

    @Test
    void validate_shouldThrowInvalidSamlExceptionIfIssuerElementIsMissing() throws Exception {
        Response response = aResponse().withIssuer(null).build();

        assertValidationFailure(response, missingIssuer());
    }

    @Test
    void validate_shouldThrowInvalidSamlExceptionIfIssuerIdIsMissing() throws Exception {
        Issuer issuer = anIssuer().withIssuerId(null).build();
        Response response = aResponse().withIssuer(issuer).build();

        assertValidationFailure(response, emptyIssuer());
    }

    @Test
    void validateRequest_shouldDoNothingIfResponseIsSigned() throws Exception {
        Response response = aResponse().withStatus(happyStatus).build();

        validator.validate(response);
    }

    @Test
    void validateRequest_shouldThrowExceptionIfResponseDoesNotContainASignature() throws Exception {
        Response response = aResponse().withoutSignatureElement().build();

        assertValidationFailure(response, missingSignature());
    }

    @Test
    void validateRequest_shouldThrowExceptionIfResponseIsNotSigned() throws Exception {
        Response response = aResponse().withoutSigning().build();

        assertValidationFailure(response, signatureNotSigned());
    }

    @Test
    void validateIssuer_shouldThrowExceptionIfFormatAttributeHasInvalidValue() throws Exception {
        String invalidFormat = "goo";
        Response response = aResponse().withIssuer(anIssuer().withFormat(invalidFormat).build()).build();

        assertValidationFailure(response, illegalIssuerFormat(invalidFormat, NameIDType.ENTITY));
    }

    @Test
    void validateIssuer_shouldDoNothingIfFormatAttributeIsMissing() throws Exception {
        Issuer issuer = anIssuer().withFormat(null).build();
        Response response = aResponse().withIssuer(issuer).withStatus(happyStatus).build();

        validator.validate(response);
    }

    @Test
    void validateIssuer_shouldDoNothingIfFormatAttributeHasValidValue() throws Exception {
        Issuer issuer = anIssuer().withFormat(NameIDType.ENTITY).build();
        Response response = aResponse().withIssuer(issuer).withStatus(happyStatus).build();

        validator.validate(response);
    }

    @Test
    void validateResponse_shouldThrowExceptionIfResponseHasUnencryptedAssertion() throws Exception {
        Assertion assertion = anAssertion().buildUnencrypted();
        Response response = aResponse().withStatus(happyStatus).addAssertion(assertion).build();

        assertValidationFailure(response, unencryptedAssertion());
    }

    @Test
    void validateResponse_shouldThrowExceptionForSuccessResponsesWithNoAssertions() throws Exception {
        Response response = aResponse().withStatus(happyStatus).withNoDefaultAssertion().build();

        assertValidationFailure(response, missingSuccessUnEncryptedAssertions());
    }

    @Test
    void validateResponse_shouldThrowExceptionForFailureResponsesWithAssertions() throws Exception {
        Status status = createStatus(StatusCode.RESPONDER, createSubStatusCode(SamlStatusCode.NO_MATCH));
        Response response = aResponse().withStatus(status).build();

        assertValidationFailure(response, nonSuccessHasUnEncryptedAssertions());
    }

    @Test
    void validateResponse_shouldThrowExceptionIfThereIsNoInResponseToAttribute() throws Exception {
        Response response = aResponse().withInResponseTo(null).build();

        assertValidationFailure(response, missingInResponseTo());
    }

    @Test
    void validate_shouldThrowExceptionIfSuccessResponseDoesNotContainSubStatusOfMatchOrNoMatchOrCreated() throws Exception {
        Status status = createStatus(StatusCode.SUCCESS, createSubStatusCode(SamlStatusCode.MULTI_MATCH));
        Response response = aResponse().withStatus(status).withNoDefaultAssertion().build();

        assertValidationFailure(response, subStatusMustBeOneOf("Success", "Match", "No Match", "Created"));
    }

    @Test
    void validate_shouldDoNothingIfAResponderStatusContainsASubStatusOfNoMatch() throws Exception {
        Status status = createStatus(StatusCode.RESPONDER, createSubStatusCode(SamlStatusCode.NO_MATCH));
        Response response = aResponse().withStatus(status).withNoDefaultAssertion().build();

        validator.validate(response);
    }

    @Test
    void validate_shouldDoNothingIfASuccessStatusContainsASubStatusOfMatch() throws Exception {
        Response response = aResponse().withStatus(happyStatus).build();

        validator.validate(response);
    }

    @Test
    void validate_shouldDoNothingIfASuccessStatusContainsASubStatusOfNoMatch() throws Exception {
        Status status = createStatus(StatusCode.SUCCESS, createSubStatusCode(SamlStatusCode.NO_MATCH));
        Response response = aResponse().withStatus(status).build();

        validator.validate(response);
    }

    @Test
    void validate_shouldDoNothingIfAResponderStatusContainsASubStatusOfMultiMatch() throws Exception {
        Status status = createStatus(StatusCode.RESPONDER, createSubStatusCode(SamlStatusCode.MULTI_MATCH));
        Response response = aResponse().withStatus(status).withNoDefaultAssertion().build();

        validator.validate(response);
    }

    @Test
    void validate_shouldThrowExceptionIfAResponderStatusContainsAnInvalidSubStatus() throws Exception {
        Status status = createStatus(StatusCode.RESPONDER, createSubStatusCode("invalid, yo."));
        Response response = aResponse().withStatus(status).withNoDefaultAssertion().build();

        assertValidationFailure(response, subStatusMustBeOneOf("Responder", "No Match", "Multi Match", "Create Failure"));
    }

    @Test
    void validate_shouldThrowExceptionIfSubStatusIsNull() throws Exception {
        Response response = aResponse().withStatus(createStatus(StatusCode.SUCCESS)).build();

        assertValidationFailure(response, SamlTransformationErrorFactory.missingSubStatus());
    }

    @Test
    void validate_shouldThrowIfResponseContainsTooManyAssertions() throws Exception {
        Response response = aResponse().withStatus(happyStatus)
            .addEncryptedAssertion(anAssertion().build())
            .addEncryptedAssertion(anAssertion().build())
            .build();

        assertValidationFailure(response, unexpectedNumberOfAssertions(1, 2));
    }

    private void assertValidationFailure(Response response, SamlValidationSpecificationFailure failure) {
        validateFail(() -> validator.validate(response), failure);
    }
}
