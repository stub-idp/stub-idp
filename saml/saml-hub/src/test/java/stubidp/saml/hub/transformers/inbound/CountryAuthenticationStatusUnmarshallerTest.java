package stubidp.saml.hub.transformers.inbound;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Status;
import org.opensaml.saml.saml2.core.StatusCode;
import org.opensaml.saml.saml2.core.StatusMessage;
import stubidp.saml.hub.core.test.builders.StatusMessageBuilder;
import stubidp.saml.hub.domain.CountryAuthenticationStatus;
import stubidp.saml.test.OpenSAMLRunner;
import stubidp.saml.serializers.deserializers.StringToOpenSamlObjectTransformer;
import stubidp.saml.utils.core.OpenSamlXmlObjectFactory;
import stubidp.saml.utils.core.api.CoreTransformersFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static stubidp.saml.test.builders.StatusBuilder.aStatus;
import static stubidp.saml.test.builders.StatusCodeBuilder.aStatusCode;

class CountryAuthenticationStatusUnmarshallerTest extends OpenSAMLRunner {

    private OpenSamlXmlObjectFactory samlObjectFactory;
    private StringToOpenSamlObjectTransformer<Response> stringToOpenSamlObjectTransformer;
    private CountryAuthenticationStatusUnmarshaller countryAuthenticationStatusUnmarshaller;

    @BeforeEach
    void setUp() {
        samlObjectFactory = new OpenSamlXmlObjectFactory();
        stringToOpenSamlObjectTransformer = new CoreTransformersFactory().getStringtoOpenSamlObjectTransformer(input -> {});
        countryAuthenticationStatusUnmarshaller = new CountryAuthenticationStatusUnmarshaller();
    }

    @Test
    void shouldTransformSuccessWithNoSubCode() {
        Status originalStatus = samlObjectFactory.createStatus();
        StatusCode successStatusCode = samlObjectFactory.createStatusCode();
        successStatusCode.setValue(StatusCode.SUCCESS);
        originalStatus.setStatusCode(successStatusCode);

        final CountryAuthenticationStatus transformedStatus = countryAuthenticationStatusUnmarshaller.fromSaml(originalStatus);

        assertThat(transformedStatus).isEqualTo(CountryAuthenticationStatus.success());
    }

    @Test
    void shouldTransformSuccessWithMessage() {
        Status originalStatus = samlObjectFactory.createStatus();
        StatusCode successStatusCode = samlObjectFactory.createStatusCode();
        successStatusCode.setValue(StatusCode.SUCCESS);
        StatusMessage statusMessage = samlObjectFactory.createStatusMessage();
        statusMessage.setValue(StatusCode.SUCCESS);
        originalStatus.setStatusCode(successStatusCode);
        originalStatus.setStatusMessage(statusMessage);

        final CountryAuthenticationStatus transformedStatus = countryAuthenticationStatusUnmarshaller.fromSaml(originalStatus);

        assertThat(transformedStatus).isEqualTo(CountryAuthenticationStatus.success());
        assertThat(transformedStatus.getMessage().isPresent()).isEqualTo(false);
    }

    @Test
    void shouldTransformNoAuthenticationContext() {
        Status originalStatus = samlObjectFactory.createStatus();
        StatusCode topLevelStatusCode = samlObjectFactory.createStatusCode();
        topLevelStatusCode.setValue(StatusCode.RESPONDER);
        StatusCode subStatusCode = samlObjectFactory.createStatusCode();
        subStatusCode.setValue(StatusCode.NO_AUTHN_CONTEXT);
        topLevelStatusCode.setStatusCode(subStatusCode);
        originalStatus.setStatusCode(topLevelStatusCode);

        final CountryAuthenticationStatus transformedStatus = countryAuthenticationStatusUnmarshaller.fromSaml(originalStatus);

        assertThat(transformedStatus).isEqualTo(CountryAuthenticationStatus.failure());
    }

    @Test
    void shouldTransformAuthnFailed() {
        Status status = samlObjectFactory.createStatus();
        StatusCode topLevelStatusCode = samlObjectFactory.createStatusCode();
        topLevelStatusCode.setValue(StatusCode.RESPONDER);
        status.setStatusCode(topLevelStatusCode);
        StatusCode subStatusCode = samlObjectFactory.createStatusCode();
        subStatusCode.setValue(StatusCode.AUTHN_FAILED);
        topLevelStatusCode.setStatusCode(subStatusCode);

        final CountryAuthenticationStatus transformedStatus = countryAuthenticationStatusUnmarshaller.fromSaml(status);

        assertThat(transformedStatus).isEqualTo(CountryAuthenticationStatus.failure());
    }

    @Test
    void shouldTransformAuthnFailedWithoutSubstatus() {
        String message = "error detail";
        StatusCode topLevelStatusCode = aStatusCode().withValue(StatusCode.AUTHN_FAILED).build();
        Status status = aStatus()
                .withStatusCode(topLevelStatusCode)
                .withMessage(StatusMessageBuilder.aStatusMessage().withMessage(message).build())
                .build();

        final CountryAuthenticationStatus transformedStatus = countryAuthenticationStatusUnmarshaller.fromSaml(status);

        assertThat(transformedStatus).isEqualTo(CountryAuthenticationStatus.failure());
        assertThat(transformedStatus.getMessage().isPresent()).isEqualTo(true);
        assertThat(transformedStatus.getMessage().get()).isEqualTo(message);
    }

    @Test
    void shouldTransformRequestDeniedWithoutSubstatus() {
        String message = "error detail";
        StatusCode topLevelStatusCode = aStatusCode().withValue(StatusCode.REQUEST_DENIED).build();
        Status status = aStatus()
                .withStatusCode(topLevelStatusCode)
                .withMessage(StatusMessageBuilder.aStatusMessage().withMessage(message).build())
                .build();

        final CountryAuthenticationStatus transformedStatus = countryAuthenticationStatusUnmarshaller.fromSaml(status);

        assertThat(transformedStatus).isEqualTo(CountryAuthenticationStatus.failure());
        assertThat(transformedStatus.getMessage().isPresent()).isEqualTo(true);
        assertThat(transformedStatus.getMessage().get()).isEqualTo(message);
    }

    @Test
    void shouldTransformRequesterErrorWithoutMessage() {
        Status status = samlObjectFactory.createStatus();
        StatusCode topLevelStatusCode = samlObjectFactory.createStatusCode();
        topLevelStatusCode.setValue(StatusCode.REQUESTER);
        status.setStatusCode(topLevelStatusCode);

        final CountryAuthenticationStatus transformedStatus = countryAuthenticationStatusUnmarshaller.fromSaml(status);

        assertThat(transformedStatus).isEqualTo(CountryAuthenticationStatus.failure());
        assertThat(transformedStatus.getMessage().isPresent()).isEqualTo(false);
    }

    @Test
    void shouldTransformRequesterErrorWithMessage() {
        String message = "some message";

        StatusCode topLevelStatusCode = aStatusCode().withValue(StatusCode.REQUESTER).build();
        Status status = aStatus()
                .withStatusCode(topLevelStatusCode)
                .withMessage(StatusMessageBuilder.aStatusMessage().withMessage(message).build())
                .build();

        final CountryAuthenticationStatus transformedStatus = countryAuthenticationStatusUnmarshaller.fromSaml(status);

        assertThat(transformedStatus).isEqualTo(CountryAuthenticationStatus.failure());
        assertThat(transformedStatus.getMessage().isPresent()).isEqualTo(true);
        assertThat(transformedStatus.getMessage().get()).isEqualTo(message);
    }

    @Test
    void shouldTransformRequesterErrorWithRequestDeniedSubstatus() {
        Status status = samlObjectFactory.createStatus();
        StatusCode topLevelStatusCode = samlObjectFactory.createStatusCode();
        topLevelStatusCode.setValue(StatusCode.REQUESTER);
        StatusCode subStatusCode = samlObjectFactory.createStatusCode();
        subStatusCode.setValue(StatusCode.REQUEST_DENIED);
        status.setStatusCode(topLevelStatusCode);

        final CountryAuthenticationStatus transformedStatus = countryAuthenticationStatusUnmarshaller.fromSaml(status);

        assertThat(transformedStatus).isEqualTo(CountryAuthenticationStatus.failure());
    }

    @Test
    void shouldTransformResponderErrorWithoutMessage() {
        Status status = samlObjectFactory.createStatus();
        StatusCode topLevelStatusCode = samlObjectFactory.createStatusCode();
        topLevelStatusCode.setValue(StatusCode.RESPONDER);
        status.setStatusCode(topLevelStatusCode);

        final CountryAuthenticationStatus transformedStatus = countryAuthenticationStatusUnmarshaller.fromSaml(status);

        assertThat(transformedStatus).isEqualTo(CountryAuthenticationStatus.failure());
        assertThat(transformedStatus.getMessage().isPresent()).isEqualTo(false);
    }

    @Test
    void shouldTransformResponderErrorWithMessage() {
        String message = "some message";

        StatusCode topLevelStatusCode = aStatusCode().withValue(StatusCode.RESPONDER).build();
        Status status = aStatus()
                .withStatusCode(topLevelStatusCode)
                .withMessage(StatusMessageBuilder.aStatusMessage().withMessage(message).build())
                .build();

        final CountryAuthenticationStatus transformedStatus = countryAuthenticationStatusUnmarshaller.fromSaml(status);

        assertThat(transformedStatus).isEqualTo(CountryAuthenticationStatus.failure());
        assertThat(transformedStatus.getMessage().isPresent()).isEqualTo(true);
        assertThat(transformedStatus.getMessage().get()).isEqualTo(message);
    }

    @Test
    void shouldTransformResponderErrorWithCanceledSubstatus() {
        Status status = samlObjectFactory.createStatus();
        StatusCode topLevelStatusCode = samlObjectFactory.createStatusCode();
        topLevelStatusCode.setValue(StatusCode.RESPONDER);
        StatusCode subStatusCode = samlObjectFactory.createStatusCode();
        subStatusCode.setValue("Canceled");
        status.setStatusCode(topLevelStatusCode);

        final CountryAuthenticationStatus transformedStatus = countryAuthenticationStatusUnmarshaller.fromSaml(status);

        assertThat(transformedStatus).isEqualTo(CountryAuthenticationStatus.failure());
    }

    @Test
    void shouldMapSamlStatusDetailOfAuthnCancelToAuthenticationCancelled() throws Exception {
        String cancelXml = readXmlFile("status-cancel.xml");
        Response cancelResponse = stringToOpenSamlObjectTransformer.apply(cancelXml);

        final CountryAuthenticationStatus transformedStatus = countryAuthenticationStatusUnmarshaller.fromSaml(cancelResponse.getStatus());

        assertThat(transformedStatus.getStatusCode()).isEqualTo(CountryAuthenticationStatus.Status.Failure);
    }

    @Test
    void shouldMapSamlStatusDetailOfLoaPendingToAuthenticationPending() throws Exception {
        String pendingXml = readXmlFile("status-pending.xml");
        Response pendingResponse = stringToOpenSamlObjectTransformer.apply(pendingXml);

        final CountryAuthenticationStatus transformedStatus = countryAuthenticationStatusUnmarshaller.fromSaml(pendingResponse.getStatus());

        assertThat(transformedStatus.getStatusCode()).isEqualTo(CountryAuthenticationStatus.Status.Failure);
    }

    @Test
    void shouldRemainSuccessEvenIfStatusDetailCancelReturned() throws Exception {
        String successWithCancelXml = readXmlFile("status-success-with-cancel.xml");
        Response cancelResponse = stringToOpenSamlObjectTransformer.apply(successWithCancelXml);

        final CountryAuthenticationStatus transformedStatus = countryAuthenticationStatusUnmarshaller.fromSaml(cancelResponse.getStatus());

        assertThat(transformedStatus.getStatusCode()).isEqualTo(CountryAuthenticationStatus.Status.Success);
    }

    @Test
    void shouldRemainNoAuthnContextIfStatusDetailAbsent() throws Exception {
        String successWithCancelXml = readXmlFile("status-noauthncontext.xml");
        Response cancelResponse = stringToOpenSamlObjectTransformer.apply(successWithCancelXml);

        final CountryAuthenticationStatus transformedStatus = countryAuthenticationStatusUnmarshaller.fromSaml(cancelResponse.getStatus());

        assertThat(transformedStatus.getStatusCode()).isEqualTo(CountryAuthenticationStatus.Status.Failure);
    }

    private String readXmlFile(String xmlFile) throws IOException, URISyntaxException {
        Base64.Encoder encoder = Base64.getEncoder();
        URL resource = getClass().getClassLoader().getResource(xmlFile);
        return new String(encoder.encode(Files.readAllBytes(Paths.get(resource.toURI()))));
    }
}
