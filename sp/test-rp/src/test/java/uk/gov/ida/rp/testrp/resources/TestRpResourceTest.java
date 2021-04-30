package uk.gov.ida.rp.testrp.resources;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import stubidp.saml.domain.assertions.TransactionIdaStatus;
import stubidp.utils.rest.common.SessionId;
import uk.gov.ida.rp.testrp.builders.AccessTokenBuilder;
import uk.gov.ida.rp.testrp.contract.LevelOfAssuranceDto;
import uk.gov.ida.rp.testrp.domain.AccessToken;
import uk.gov.ida.rp.testrp.domain.PageErrorMessageDetails;
import uk.gov.ida.rp.testrp.domain.PageErrorMessageDetailsFactory;
import uk.gov.ida.rp.testrp.repositories.Session;
import uk.gov.ida.rp.testrp.repositories.SessionRepository;
import uk.gov.ida.rp.testrp.tokenservice.TokenService;
import uk.gov.ida.rp.testrp.views.TestRpSuccessPageView;

import java.net.URI;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.ida.rp.testrp.builders.TestRpConfigurationBuilder.aTestRpConfiguration;

@ExtendWith({MockitoExtension.class, DropwizardExtensionsSupport.class})
public class TestRpResourceTest {

    @Mock
    private SessionRepository sessionRepository;
    @Mock
    private PageErrorMessageDetailsFactory pageErrorMessageDetailsFactory;
    @Mock
    private TokenService tokenService;

    private TestRpResource resource;

    @BeforeEach
    public void setErrorMessageGeneratorDummyErrorMessage() {
        PageErrorMessageDetails nullErrorMessage = new PageErrorMessageDetails(Optional.empty(), Optional.empty());
        when(pageErrorMessageDetailsFactory.getErrorMessage(ArgumentMatchers.<Optional<TransactionIdaStatus>>any())).thenReturn(nullErrorMessage);

        resource = new TestRpResource(sessionRepository, aTestRpConfiguration().build(), pageErrorMessageDetailsFactory, tokenService);
    }

    @Test
    public void getMain_withQueryParamTokenAndCookie_shouldValidateQueryParamToken() {
        AccessToken queryParamToken = AccessTokenBuilder.anAccessToken().withValue("query-param-token").build();
        AccessToken cookieToken = AccessTokenBuilder.anAccessToken().withValue("cookie-token").build();
        resource.getMain(Optional.empty(), Optional.of(queryParamToken), cookieToken);

        verify(tokenService).validate(Optional.of(queryParamToken));
    }

    @Test
    public void getMain_withNoQueryParamButWithCookie_shouldValidateCookieToken() {
        AccessToken cookieToken = AccessTokenBuilder.anAccessToken().withValue("cookie-token").build();
        resource.getMain(Optional.empty(), Optional.empty(), cookieToken);

        verify(tokenService).validate(Optional.of(cookieToken));
    }

    @Test
    public void getSuccessfulRegister_shouldReturnSucessfulPageView(){
        Session session = new Session(
                SessionId.createNewSessionId(),
                "requestId",
                URI.create("pathUserWasTryingToAccess"),
                "issuerId",
                Optional.ofNullable(1),
                Optional.empty(),
                false,
                false,
                false);

        TestRpSuccessPageView successPageView = resource.getSuccessfulRegister(session, Optional.empty(), Optional.of("unit-test-rp"), Optional.of(LevelOfAssuranceDto.LEVEL_1));

        assertThat(successPageView.getLoa()).isEqualTo(LevelOfAssuranceDto.LEVEL_1.name());
        assertThat(successPageView.getRpName()).isEqualTo("unit-test-rp");

    }

}
