package uk.gov.ida.integrationTest;

import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.gov.ida.integrationTest.support.IntegrationTestHelper;
import uk.gov.ida.integrationTest.support.TestRpAppRule;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(DropwizardExtensionsSupport.class)
public class AdminPortAppRuleTests extends IntegrationTestHelper {
    private static final Client client = JerseyClientBuilder.createClient().property(ClientProperties.FOLLOW_REDIRECTS, false);

    public static TestRpAppRule testRp = TestRpAppRule.newTestRpAppRule(
            ConfigOverride.config("clientTrustStoreConfiguration.path", ResourceHelpers.resourceFilePath("ida_truststore.ts")),
            ConfigOverride.config("msaMetadataUri", "http://localhost:"+getMsaStubRule().getPort()+"/metadata"),
            ConfigOverride.config("allowInsecureMetadataLocation", "true"));

    @Test
    public void ensureHealthCheckRegistered() {
        final Response response = client.target("http://localhost:" + testRp.getAdminPort() + "/healthcheck").request().get();
        assertThat(response.getStatus()).isEqualTo(200);
        final String entity = response.readEntity(String.class);
        assertThat(entity).contains("metadata");
    }

    @Test
    public void ensureMetadataRefreshTaskRegistered() {
        final Response response = client.target("http://localhost:" + testRp.getAdminPort() + "/tasks/metadata-refresh").request().post(Entity.text(""));
        assertThat(response.getStatus()).isEqualTo(200);
    }

}
