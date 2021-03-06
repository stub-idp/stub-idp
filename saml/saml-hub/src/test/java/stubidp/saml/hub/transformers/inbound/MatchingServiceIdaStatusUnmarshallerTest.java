package stubidp.saml.hub.transformers.inbound;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.core.Status;
import org.opensaml.saml.saml2.core.StatusCode;
import stubidp.saml.extensions.domain.SamlStatusCode;
import stubidp.saml.test.OpenSAMLRunner;
import stubidp.saml.utils.core.OpenSamlXmlObjectFactory;

import static org.assertj.core.api.Assertions.assertThat;

class MatchingServiceIdaStatusUnmarshallerTest extends OpenSAMLRunner {

    private MatchingServiceIdaStatusUnmarshaller unmarshaller;

    @BeforeEach
    void setUp() {
        unmarshaller = new MatchingServiceIdaStatusUnmarshaller();
    }

    @Test
    void transform_shouldTransformMatchingServiceSuccessfulMatch() {
        OpenSamlXmlObjectFactory samlObjectFactory = new OpenSamlXmlObjectFactory();
        Status originalStatus = samlObjectFactory.createStatus();
        StatusCode successStatusCode = samlObjectFactory.createStatusCode();
        successStatusCode.setValue(StatusCode.SUCCESS);
        originalStatus.setStatusCode(successStatusCode);
        StatusCode matchStatusCode = samlObjectFactory.createStatusCode();
        matchStatusCode.setValue(SamlStatusCode.MATCH);
        successStatusCode.setStatusCode(matchStatusCode);

        MatchingServiceIdaStatus transformedStatus = unmarshaller.fromSaml(originalStatus);

        assertThat(transformedStatus).isEqualTo(MatchingServiceIdaStatus.MatchingServiceMatch);
    }

    @Test
    void transform_shouldTransformNoMatchFromMatchingService() {
        OpenSamlXmlObjectFactory samlObjectFactory = new OpenSamlXmlObjectFactory();
        Status originalStatus = samlObjectFactory.createStatus();
        StatusCode topLevelStatusCode = samlObjectFactory.createStatusCode();
        topLevelStatusCode.setValue(StatusCode.RESPONDER);
        StatusCode subStatusCode = samlObjectFactory.createStatusCode();
        subStatusCode.setValue(SamlStatusCode.NO_MATCH);
        topLevelStatusCode.setStatusCode(subStatusCode);
        originalStatus.setStatusCode(topLevelStatusCode);

        MatchingServiceIdaStatus transformedStatus = unmarshaller.fromSaml(originalStatus);

        assertThat(transformedStatus).isEqualTo(MatchingServiceIdaStatus.NoMatchingServiceMatchFromMatchingService);
    }

    @Test
    void transform_shouldTransformRequesterErrorFromMatchingService() {
        OpenSamlXmlObjectFactory samlObjectFactory = new OpenSamlXmlObjectFactory();
        Status originalStatus = samlObjectFactory.createStatus();
        StatusCode topLevelStatusCode = samlObjectFactory.createStatusCode();
        topLevelStatusCode.setValue(StatusCode.REQUESTER);
        originalStatus.setStatusCode(topLevelStatusCode);

        MatchingServiceIdaStatus transformedStatus = unmarshaller.fromSaml(originalStatus);

        assertThat(transformedStatus).isEqualTo(MatchingServiceIdaStatus.RequesterError);
    }

    @Test
    void transform_shouldTransformHealthyStatusFromMatchingService() {
        OpenSamlXmlObjectFactory samlObjectFactory = new OpenSamlXmlObjectFactory();
        Status status = samlObjectFactory.createStatus();
        StatusCode topLevelStatusCode = samlObjectFactory.createStatusCode();
        topLevelStatusCode.setValue(StatusCode.SUCCESS);
        status.setStatusCode(topLevelStatusCode);
        StatusCode subStatusCode = samlObjectFactory.createStatusCode();
        subStatusCode.setValue(SamlStatusCode.HEALTHY);
        topLevelStatusCode.setStatusCode(subStatusCode);
        MatchingServiceIdaStatus transformedStatus = unmarshaller.fromSaml(status);

        assertThat(transformedStatus).isEqualTo(MatchingServiceIdaStatus.Healthy);
    }

    @Test
    void shouldTransformCreateFailureCaseFromMatchingService() {
        OpenSamlXmlObjectFactory samlObjectFactory = new OpenSamlXmlObjectFactory();
        Status status = samlObjectFactory.createStatus();
        StatusCode topLevelStatusCode = samlObjectFactory.createStatusCode();
        topLevelStatusCode.setValue(StatusCode.RESPONDER);
        status.setStatusCode(topLevelStatusCode);
        StatusCode subStatusCode = samlObjectFactory.createStatusCode();
        subStatusCode.setValue(SamlStatusCode.CREATE_FAILURE);
        topLevelStatusCode.setStatusCode(subStatusCode);
        MatchingServiceIdaStatus transformedStatus = unmarshaller.fromSaml(status);

        assertThat(transformedStatus).isEqualTo(MatchingServiceIdaStatus.UserAccountCreationFailed);
    }
}
