package stubidp.saml.test.builders;

import org.opensaml.xmlsec.signature.Signature;
import stubidp.saml.domain.assertions.IdpIdaStatus;
import stubidp.saml.domain.assertions.PassthroughAssertion;
import stubidp.saml.domain.assertions.TransactionIdaStatus;
import stubidp.saml.domain.response.InboundResponseFromIdp;
import stubidp.saml.domain.response.OutboundResponseFromHub;
import stubidp.test.devpki.TestEntityIds;

import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

public class ResponseForHubBuilder {
    private String responseId = "response-id";
    private String inResponseTo = "request-id";
    private String issuerId = "issuer-id";
    private Instant issueInstant = Instant.now();
    private final Optional<Instant> notOnOrAfter = ofNullable(Instant.now().plus(5, ChronoUnit.MINUTES));
    private TransactionIdaStatus transactionIdpStatus = TransactionIdaStatus.Success;
    private IdpIdaStatus idpIdaStatus = IdpIdaStatus.success();
    private Optional<Signature> signature = empty();
    private Optional<PassthroughAssertion> authnStatementAssertion = empty();
    private Optional<PassthroughAssertion> matchingDatasetAssertion = empty();
    private List<String> encryptedAssertions = Collections.emptyList();

    private ResponseForHubBuilder() {}

    public static ResponseForHubBuilder anAuthnResponse() {
        return new ResponseForHubBuilder();
    }

    public InboundResponseFromIdp<PassthroughAssertion> buildInboundFromIdp() {
        return new InboundResponseFromIdp<>(
                responseId,
                inResponseTo,
                issuerId,
                issueInstant,
                notOnOrAfter,
                idpIdaStatus,
                signature,
                matchingDatasetAssertion,
                null,
                authnStatementAssertion
        );
    }

    public InboundResponseFromIdp<PassthroughAssertion> buildSuccessFromIdp() {
        return new InboundResponseFromIdp<>(
                responseId,
                inResponseTo,
                issuerId,
                issueInstant,
                notOnOrAfter,
                idpIdaStatus,
                signature,
                matchingDatasetAssertion,
                null,
                authnStatementAssertion
        );
    }

    public OutboundResponseFromHub buildOutboundResponseFromHub() {
        return new OutboundResponseFromHub(
                responseId,
                inResponseTo,
                TestEntityIds.HUB_ENTITY_ID,
                issueInstant,
                transactionIdpStatus,
                encryptedAssertions,
                URI.create("blah"));
    }


    public ResponseForHubBuilder withResponseId(String responseId) {
        this.responseId = responseId;
        return this;
    }

    public ResponseForHubBuilder withInResponseTo(String inResponseTo) {
        this.inResponseTo = inResponseTo;
        return this;
    }

    public ResponseForHubBuilder withIssuerId(String issuerId) {
        this.issuerId = issuerId;
        return this;
    }

    public ResponseForHubBuilder withIssueInstant(Instant issueInstant) {
        this.issueInstant = issueInstant;
        return this;
    }

    public ResponseForHubBuilder withIdpIdaStatus(IdpIdaStatus status) {
        this.idpIdaStatus = status;
        return this;
    }

    public ResponseForHubBuilder withTransactionIdaStatus(TransactionIdaStatus status) {
        this.transactionIdpStatus = status;
        return this;
    }

    public ResponseForHubBuilder withSignature(Signature signature) {
        this.signature = ofNullable(signature);
        return this;
    }

    public ResponseForHubBuilder withAuthnStatementAssertion(PassthroughAssertion authnStatementAssertion) {
        this.authnStatementAssertion = ofNullable(authnStatementAssertion);
        return this;
    }

    public ResponseForHubBuilder withMatchingDatasetAssertion(PassthroughAssertion matchingDatasetAssertion) {
        this.matchingDatasetAssertion = ofNullable(matchingDatasetAssertion);
        return this;
    }

    public ResponseForHubBuilder withEncryptedAssertions(List<String> encryptedAssertions) {
        this.encryptedAssertions = encryptedAssertions;
        return this;
    }
}
