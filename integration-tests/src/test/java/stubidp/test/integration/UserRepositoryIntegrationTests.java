package stubidp.test.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import stubidp.saml.domain.assertions.Address;
import stubidp.saml.domain.assertions.AuthnContext;
import stubidp.saml.domain.assertions.Gender;
import stubidp.saml.domain.assertions.SimpleMdsValue;
import stubidp.saml.test.builders.SimpleMdsValueBuilder;
import stubidp.stubidp.Urls;
import stubidp.stubidp.dtos.IdpUserDto;
import stubidp.stubidp.security.BCryptHelper;
import stubidp.test.integration.support.IntegrationTestHelper;
import stubidp.test.integration.support.StubIdpAppExtension;
import stubidp.test.integration.support.TestUserCredentials;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.List.of;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static org.assertj.core.api.Assertions.assertThat;
import static stubidp.saml.test.builders.AddressBuilder.anAddress;
import static stubidp.test.integration.UserRepositoryIntegrationTests.UserBuilder.aUser;
import static stubidp.test.integration.support.StubIdpBuilder.aStubIdp;
import static stubidp.utils.rest.common.HttpHeaders.CACHE_CONTROL_KEY;
import static stubidp.utils.rest.common.HttpHeaders.CACHE_CONTROL_NO_CACHE_VALUE;
import static stubidp.utils.rest.common.HttpHeaders.PRAGMA_KEY;
import static stubidp.utils.rest.common.HttpHeaders.PRAGMA_NO_CACHE_VALUE;

@ExtendWith(DropwizardExtensionsSupport.class)
public class UserRepositoryIntegrationTests extends IntegrationTestHelper {

    private static final String IDP_NAME = "user-repository-idp";
    private static final String DISPLAY_NAME = "User Repository Identity Service";
    private static final String USERNAME = "integrationTestUser";
    private static final String PASSWORD = "integrationTestUserPassword";

    private static final StubIdpAppExtension applicationRule = new StubIdpAppExtension(Map.ofEntries(Map.entry("isIdpEnabled", "true"), Map.entry("basicAuthEnabledForUserResource", "true")))
            .withStubIdp(aStubIdp()
                    .withId(IDP_NAME)
                    .withDisplayName(DISPLAY_NAME)
                    .addUserCredentials(new TestUserCredentials(USERNAME, PASSWORD))
                    .build());

    private final Client client = JerseyClientBuilder.createClient().property(ClientProperties.FOLLOW_REDIRECTS, false);

    @BeforeEach
    void setUp() {
        HttpAuthenticationFeature httpAuthenticationFeature = HttpAuthenticationFeature.basic(USERNAME, PASSWORD);
        client.register(httpAuthenticationFeature);
    }

    @Test
    void shouldNotAllowIncorrectCredentialsTest() throws JsonProcessingException {
        Client client = JerseyClientBuilder.createClient().property(ClientProperties.FOLLOW_REDIRECTS, false);
        HttpAuthenticationFeature httpAuthenticationFeature = HttpAuthenticationFeature.basic("USERNAME", "PASSWORD");
        client.register(httpAuthenticationFeature);
        IdpUserDto user = aUser().withUsername("user-11111").build();

        Response response = client.target(getAddAllUsersPath(IDP_NAME))
                .request()
                .accept(APPLICATION_JSON_TYPE)
                .post(entity(getJson(of(user)), APPLICATION_JSON_TYPE));

        assertThat(response.getStatus()).isEqualTo(UNAUTHORIZED.getStatusCode());

        response = client.target(getDeleteUserPath(IDP_NAME))
                .request()
                .accept(APPLICATION_JSON_TYPE)
                .post(entity(getJson(of(user)), APPLICATION_JSON_TYPE));

        assertThat(response.getStatus()).isEqualTo(UNAUTHORIZED.getStatusCode());
    }

    @Test
    void addedUserShouldBePersistedTest() throws Exception {
        IdpUserDto user = new IdpUserDto(
                Optional.of("a pid"),
                "some test user",
                "some password",
                createOptionalMdsValue(Optional.of("some user firstname")),
                createOptionalMdsValue(Optional.of("some user middlename")),
                Collections.singletonList(SimpleMdsValueBuilder.<String>aSimpleMdsValue().withValue("some user addSurname").build()),
                createOptionalMdsValue(Optional.of(Gender.FEMALE)),
                createOptionalMdsValue(Optional.of(LocalDate.now())),
                Optional.ofNullable(anAddress().withLines(asList("blah", "blah2")).withPostCode("WC1V7AA").withVerified(true).build()),
                AuthnContext.LEVEL_4.toString()
        );

        aUserIsCreatedForIdp(IDP_NAME, user);

        IdpUserDto returnedUser = readEntity(aUserIsRequestedForIdp(user.getUsername(), IDP_NAME));

        assertThat(returnedUser.getPid()).isEqualTo(user.getPid());
        assertThat(returnedUser.getFirstName().get().getValue()).isEqualTo(user.getFirstName().get().getValue());
        assertThat(returnedUser.getFirstName().get().isVerified()).isEqualTo(user.getFirstName().get().isVerified());
        assertThat(returnedUser.getMiddleNames().get().getValue()).isEqualTo(user.getMiddleNames().get().getValue());
        assertThat(returnedUser.getMiddleNames().get().isVerified()).isEqualTo(user.getMiddleNames().get().isVerified());
        assertThat(returnedUser.getSurnames().get(0).getValue()).isEqualTo(user.getSurnames().get(0).getValue());
        assertThat(returnedUser.getSurnames().get(0).isVerified()).isEqualTo(user.getSurnames().get(0).isVerified());
        assertThat(returnedUser.getGender().get().getValue()).isEqualTo(user.getGender().get().getValue());
        assertThat(returnedUser.getGender().get().isVerified()).isEqualTo(user.getGender().get().isVerified());
        assertThat(returnedUser.getDateOfBirth().get().getValue()).isEqualTo(user.getDateOfBirth().get().getValue());
        assertThat(returnedUser.getDateOfBirth().get().isVerified()).isEqualTo(user.getDateOfBirth().get().isVerified());
        assertThat(returnedUser.getAddress().get().getLines().get(0)).isEqualTo(user.getAddress().get().getLines().get(0));
        assertThat(returnedUser.getAddress().get().getLines().get(1)).isEqualTo(user.getAddress().get().getLines().get(1));
        assertThat(returnedUser.getAddress().get().getPostCode()).isEqualTo(user.getAddress().get().getPostCode());
        assertThat(returnedUser.getAddress().get().isVerified()).isEqualTo(user.getAddress().get().isVerified());
        assertThat(BCryptHelper.alreadyCrypted(returnedUser.getPassword())).isTrue();
        assertThat(returnedUser.getLevelOfAssurance()).isEqualTo(user.getLevelOfAssurance());
    }

    @Test
    void allAddedUsersShouldBePersistedTest() throws Exception {
        IdpUserDto user1 = aUser().withUsername("user-1").build();
        IdpUserDto user2 = aUser().withUsername("user-2").build();

        someUsersAreCreatedForIdp(IDP_NAME, user1, user2);
        IdpUserDto returnedUser1 = readEntity(aUserIsRequestedForIdp(user1.getUsername(), IDP_NAME));
        IdpUserDto returnedUser2 = readEntity(aUserIsRequestedForIdp(user2.getUsername(), IDP_NAME));

        assertThat(returnedUser1.getUsername()).isEqualTo(user1.getUsername());
        assertThat(returnedUser2.getUsername()).isEqualTo(user2.getUsername());
    }

    @Test
    void deletedUserShouldBeRemovedTest() throws IOException {
        IdpUserDto deletableUser = aUser().withUsername("deletable-user").build();
        someUsersAreCreatedForIdp(IDP_NAME, deletableUser);
        IdpUserDto returnedUser1 = readEntity(aUserIsRequestedForIdp(deletableUser.getUsername(), IDP_NAME));
        assertThat(returnedUser1.getUsername()).isEqualTo(deletableUser.getUsername());

        aUserIsDeletedFromIdp(IDP_NAME, deletableUser);
        final Response response = aUserIsRequestedForIdp(deletableUser.getUsername(), IDP_NAME);
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void addedUserShouldHavePidGeneratedWhenNotSpecifiedTest() throws Exception {
        IdpUserDto user = aUser().withPid(null).build();

        aUserIsCreatedForIdp(IDP_NAME, user);
        IdpUserDto returnedUser = readEntity(aUserIsRequestedForIdp(user.getUsername(), IDP_NAME));

        assertThat(returnedUser.getPid().isPresent()).isTrue();
    }

    @Test
    void userWithMissingLevelOfAssuranceShouldReturnBadRequestWithErrorMessage() throws Exception {
        IdpUserDto user = aUser().withLevelOfAssurance(null).build();

        Response response = aUserIsCreatedForIdpWithoutResponseChecking(IDP_NAME, user);

        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        assertThat(response.readEntity(new GenericType<List<String>>() {})).containsOnly("Level of Assurance was not specified.");
    }

    @Test
    void userWithMissingUsernameShouldReturnBadRequestWithErrorMessage() throws Exception {
        IdpUserDto user = aUser().withUsername(null).build();

        Response response = aUserIsCreatedForIdpWithoutResponseChecking(IDP_NAME, user);

        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        assertThat(response.readEntity(new GenericType<List<String>>() {})).containsOnly("Username was not specified or was empty.");
    }

    @Test
    void userWithMissingPasswordShouldReturnBadRequestWithErrorMessageTest() throws Exception {
        IdpUserDto user = aUser().withPassword(null).build();

        Response response = aUserIsCreatedForIdpWithoutResponseChecking(IDP_NAME, user);

        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        assertThat(response.readEntity(new GenericType<List<String>>() {})).containsOnly("Password was not specified or was empty.");
    }

    // convert object to json using apprule's object mapper because it can serialize guava correctly
    private String getJson(Object o) throws JsonProcessingException {
        return applicationRule.getObjectMapper().writeValueAsString(o);
    }

    // convert object from json using apprule's object mapper because it can deserialize guava correctly
    private IdpUserDto readEntity(Response response) throws IOException {
        // ensure data not stored by browser
        assertThat(response.getHeaderString(CACHE_CONTROL_KEY)).isEqualTo(CACHE_CONTROL_NO_CACHE_VALUE);
        assertThat(response.getHeaderString(PRAGMA_KEY)).isEqualTo(PRAGMA_NO_CACHE_VALUE);
        return applicationRule.getObjectMapper().readValue(response.readEntity(String.class), IdpUserDto.class);
    }

    private static <T> Optional<SimpleMdsValue<T>> createOptionalMdsValue(Optional<T> value) {
        return value.map(t -> new SimpleMdsValue<>(t, null, null, true));
    }

    static class UserBuilder {
        private Optional<String> levelOfAssurance = Optional.ofNullable(AuthnContext.LEVEL_1.toString());
        private Optional<String> username = Optional.of("default-username");
        private final Optional<Address> address = Optional.ofNullable(anAddress().withLines(asList("line-1", "line-2")).build());
        private Optional<String> password = Optional.of("default-password");
        private Optional<String> pid = Optional.of("default-pid");

        static UserBuilder aUser() {
            return new UserBuilder();
        }

        IdpUserDto build() {
            return new IdpUserDto(
                    pid,
                    username.orElse(null),
                    password.orElse(null),
                    Optional.empty(),
                    Optional.empty(),
                    Collections.emptyList(),
                    Optional.empty(),
                    Optional.empty(),
                    address,
                    levelOfAssurance.orElse(null));
        }

        UserBuilder withLevelOfAssurance(String levelOfAssurance) {
            this.levelOfAssurance = Optional.ofNullable(levelOfAssurance);
            return this;
        }

        UserBuilder withUsername(String username) {
            this.username = Optional.ofNullable(username);
            return this;
        }

        UserBuilder withPassword(String password) {
            this.password = Optional.ofNullable(password);
            return this;
        }

        UserBuilder withPid(String pid) {
            this.pid = Optional.ofNullable(pid);
            return this;
        }
    }

    void aUserIsCreatedForIdp(String idpFriendlyId, IdpUserDto user) throws JsonProcessingException {
        Response response = createARequest(getAddUserPath(idpFriendlyId)).post(entity(getJson(Collections.singletonList(user)), APPLICATION_JSON_TYPE));
        assertThat(response.getStatus()).isEqualTo(201);
    }

    Response aUserIsCreatedForIdpWithoutResponseChecking(String idpFriendlyId, IdpUserDto user) throws JsonProcessingException {
        return createARequest(getAddUserPath(idpFriendlyId)).post(entity(getJson(Collections.singletonList(user)), APPLICATION_JSON_TYPE));
    }

    Response someUsersAreCreatedForIdp(String idpFriendlyId, IdpUserDto... users) throws JsonProcessingException {
        return createARequest(getAddAllUsersPath(idpFriendlyId)).post(entity(getJson(asList(users)), APPLICATION_JSON_TYPE));
    }

    void aUserIsDeletedFromIdp(String idpFriendlyId, IdpUserDto deletableUser) throws JsonProcessingException {
        final Response response = createARequest(getDeleteUserPath(idpFriendlyId)).post(entity(getJson(deletableUser), APPLICATION_JSON_TYPE));
        assertThat(response.getStatus()).isEqualTo(200);
    }

    Response aUserIsRequestedForIdp(String username, String idpFriendlyId) {
        return createARequest(getUserPath(username, idpFriendlyId)).get();
    }

    private Invocation.Builder createARequest(String path) {
        return client.target(path).request().accept(APPLICATION_JSON_TYPE);
    }

    private String getAddUserPath(String idpFriendlyId) {
        return MessageFormat.format("{0}", getUserResourcePath(idpFriendlyId));
    }

    private String getAddAllUsersPath(String idpFriendlyId) {
        return MessageFormat.format("{0}", getUserResourcePath(idpFriendlyId));
    }

    private String getUserPath(String username, String idpFriendlyId) {
        return MessageFormat.format("{0}{1}", getUserResourcePath(idpFriendlyId), UriBuilder.fromPath(Urls.GET_USER_PATH).build(username));
    }

    private String getDeleteUserPath(String idpFriendlyId) {
        return MessageFormat.format("{0}{1}", getUserResourcePath(idpFriendlyId), UriBuilder.fromPath(Urls.DELETE_USER_PATH).build());
    }

    private String getUserResourcePath(String idpFriendlyId) {
        return UriBuilder.fromPath("http://localhost:"+applicationRule.getLocalPort()+Urls.USERS_RESOURCE).build(idpFriendlyId).toASCIIString();
    }
}
