package stubidp.test.integration;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import stubidp.stubidp.Urls;
import stubidp.stubidp.cookies.StubIdpCookieNames;
import stubidp.stubidp.domain.IdpHint;
import stubidp.test.integration.steps.AuthnRequestSteps;
import stubidp.test.integration.support.IntegrationTestHelper;
import stubidp.test.integration.support.StubIdpAppExtension;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static stubidp.test.integration.support.StubIdpBuilder.aStubIdp;

@ExtendWith(DropwizardExtensionsSupport.class)
public class HintsIntegrationTests extends IntegrationTestHelper {

    private static final String IDP_NAME = "stub-idp-one";
    private static final String DISPLAY_NAME = "Hints Identity Service";

    private AuthnRequestSteps authnRequestSteps;

    private static final StubIdpAppExtension applicationRule = new StubIdpAppExtension()
            .withStubIdp(aStubIdp().withId(IDP_NAME).withDisplayName(DISPLAY_NAME).build());

    private final Client client = JerseyClientBuilder.createClient().property(ClientProperties.FOLLOW_REDIRECTS, false);

    @BeforeEach
    void setUp() {
        client.target("http://localhost:" + applicationRule.getAdminPort() + "/tasks/metadata-refresh").request().post(Entity.text(""));
        authnRequestSteps = new AuthnRequestSteps(
                client,
                IDP_NAME,
                applicationRule.getLocalPort());
    }

    @Test
    void debugPageShowsHintsTest() {
        List<String> hints = List.of(IdpHint.has_apps.name(), "snakes", "plane");
        final Optional<Boolean> registration = Optional.of(true);
        final Optional<String> language = Optional.empty();
        final AuthnRequestSteps.Cookies cookies = authnRequestSteps.userPostsAuthnRequestToStubIdp(hints, language, registration, Optional.empty());
        Response response = aUserVisitsTheDebugPage(IDP_NAME, cookies);

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        Document doc = Jsoup.parse(response.readEntity(String.class));
        assertThat(getListItems(doc, "known-hints")).containsExactly(IdpHint.has_apps.name());
        assertThat(getListItems(doc, "unknown-hints")).containsExactlyInAnyOrder("snakes", "plane");

        assertThat(doc.getElementById("language-hint").text()).isEqualTo("No language hint was set.");

        assertThat(doc.getElementById("registration").text()).isEqualTo("\"registration\" hint is \"true\"");
    }

    @Test
    void debugPageShowsLanguageHintTest() {
        List<String> hints = List.of();
        final Optional<Boolean> registration = Optional.empty();
        final Optional<String> language = Optional.of("cy");
        final AuthnRequestSteps.Cookies cookies = authnRequestSteps.userPostsAuthnRequestToStubIdp(hints, language, registration, Optional.empty());
        Response response = aUserVisitsTheDebugPage(IDP_NAME, cookies);

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        Document doc = Jsoup.parse(response.readEntity(String.class));
        Element languageHintElement = doc.getElementById("language-hint");
        assertThat(languageHintElement).isNotNull();
        assertThat(languageHintElement.text()).contains("\"cy\"");

        assertThat(doc.getElementById("registration").text()).isEqualTo("\"registration\" hint not received");
    }

    private List<String> getListItems(Document doc, String parentClass) {
        return doc.getElementsByClass(parentClass).stream()
                .flatMap(ul -> ul.getElementsByTag("li").stream())
                .map(Element::text).collect(Collectors.toList());
    }

    Response aUserVisitsTheDebugPage(String idp, AuthnRequestSteps.Cookies cookies) {
        return client.target(getDebugPath(idp))
                .request()
                .cookie(StubIdpCookieNames.SESSION_COOKIE_NAME, cookies.getSessionId())
                .cookie(StubIdpCookieNames.SECURE_COOKIE_NAME, cookies.getSecure())
                .get();
    }

    private String getDebugPath(String idp) {
        UriBuilder uriBuilder = UriBuilder.fromPath("http://localhost:"+applicationRule.getLocalPort()+Urls.IDP_DEBUG_RESOURCE.replace("{idpId}", IDP_NAME));
        return uriBuilder.build(idp).toASCIIString();
    }

}
