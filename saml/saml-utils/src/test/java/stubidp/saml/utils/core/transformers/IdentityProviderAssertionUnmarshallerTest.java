package stubidp.saml.utils.core.transformers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.SubjectConfirmationData;
import stubidp.saml.domain.assertions.AssertionRestrictions;
import stubidp.saml.domain.assertions.IdentityProviderAssertion;
import stubidp.saml.domain.assertions.IdentityProviderAuthnStatement;
import stubidp.saml.domain.assertions.MatchingDataset;
import stubidp.saml.test.OpenSAMLRunner;
import stubidp.saml.test.builders.AddressAttributeBuilder_1_1;
import stubidp.saml.test.builders.AddressAttributeValueBuilder_1_1;
import stubidp.saml.test.builders.AssertionBuilder;
import stubidp.saml.test.builders.DateAttributeBuilder_1_1;
import stubidp.saml.test.builders.GenderAttributeBuilder_1_1;
import stubidp.saml.test.builders.MatchingDatasetBuilder;
import stubidp.saml.test.builders.PersonNameAttributeBuilder_1_1;
import stubidp.saml.test.builders.PersonNameAttributeValueBuilder;
import stubidp.saml.utils.core.test.builders.IdentityProviderAuthnStatementBuilder;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class IdentityProviderAssertionUnmarshallerTest extends OpenSAMLRunner {

    @Mock
    private VerifyMatchingDatasetUnmarshaller matchingDatasetUnmarshaller;

    @Mock
    private IdentityProviderAuthnStatementUnmarshaller idaAuthnStatementUnmarshaller;

    @Mock
    private EidasMatchingDatasetUnmarshaller eidasMatchingDatasetUnmarshaller;

    private IdentityProviderAssertionUnmarshaller unmarshaller;

    @BeforeEach
    void setUp() {
        unmarshaller = new IdentityProviderAssertionUnmarshaller(
                matchingDatasetUnmarshaller,
                eidasMatchingDatasetUnmarshaller,
                idaAuthnStatementUnmarshaller,
                "hubEntityId");
    }

    @Test
    void transform_shouldTransformResponseWhenNoMatchingDatasetIsPresent() {
        Assertion originalAssertion = AssertionBuilder.anAssertion().buildUnencrypted();

        IdentityProviderAssertion transformedAssertion = unmarshaller.fromVerifyAssertion(originalAssertion);
        assertThat(transformedAssertion.getMatchingDataset()).isEqualTo(Optional.empty());
    }

    @Test
    void transform_shouldDelegateMatchingDatasetTransformationWhenAssertionContainsMatchingDataset() {
        Attribute firstName = PersonNameAttributeBuilder_1_1.aPersonName_1_1().addValue(PersonNameAttributeValueBuilder.aPersonNameValue().withTo(LocalDate.parse("1066-01-05")).build()).buildAsFirstname();
        Assertion assertion = AssertionBuilder.aMatchingDatasetAssertion(
                firstName,
                PersonNameAttributeBuilder_1_1.aPersonName_1_1().buildAsMiddlename(),
                PersonNameAttributeBuilder_1_1.aPersonName_1_1().buildAsSurname(),
                GenderAttributeBuilder_1_1.aGender_1_1().build(),
                DateAttributeBuilder_1_1.aDate_1_1().buildAsDateOfBirth(),
                AddressAttributeBuilder_1_1.anAddressAttribute().buildCurrentAddress(),
                AddressAttributeBuilder_1_1.anAddressAttribute().addAddress(AddressAttributeValueBuilder_1_1.anAddressAttributeValue().build()).buildPreviousAddress())
                .buildUnencrypted();

        MatchingDataset matchingDataset = MatchingDatasetBuilder.aMatchingDataset().build();

        when(matchingDatasetUnmarshaller.fromAssertion(assertion)).thenReturn(matchingDataset);

        IdentityProviderAssertion identityProviderAssertion = unmarshaller.fromVerifyAssertion(assertion);
        verify(matchingDatasetUnmarshaller).fromAssertion(assertion);
        assertThat(identityProviderAssertion.getMatchingDataset().get()).isEqualTo(matchingDataset);
    }

    @Test
    void transform_shouldDelegateAuthnStatementTransformationWhenAssertionContainsAuthnStatement() {
        Assertion assertion = AssertionBuilder.anAuthnStatementAssertion().buildUnencrypted();
        IdentityProviderAuthnStatement authnStatement = IdentityProviderAuthnStatementBuilder.anIdentityProviderAuthnStatement().build();

        when(idaAuthnStatementUnmarshaller.fromAssertion(assertion)).thenReturn(authnStatement);
        IdentityProviderAssertion identityProviderAssertion = unmarshaller.fromVerifyAssertion(assertion);

        verify(idaAuthnStatementUnmarshaller).fromAssertion(assertion);

        assertThat(identityProviderAssertion.getAuthnStatement().get()).isEqualTo(authnStatement);
    }

    @Test
    void transform_shouldTransformSubjectConfirmationData() {
        Assertion assertion = AssertionBuilder.anAssertion().buildUnencrypted();
        SubjectConfirmationData subjectConfirmationData = assertion.getSubject().getSubjectConfirmations().get(0).getSubjectConfirmationData();

        final IdentityProviderAssertion identityProviderAssertion = unmarshaller.fromVerifyAssertion(assertion);

        final AssertionRestrictions assertionRestrictions = identityProviderAssertion.getAssertionRestrictions();

        assertThat(assertionRestrictions.getInResponseTo()).isEqualTo(subjectConfirmationData.getInResponseTo());
        assertThat(assertionRestrictions.getRecipient()).isEqualTo(subjectConfirmationData.getRecipient());
        assertThat(assertionRestrictions.getNotOnOrAfter()).isEqualTo(subjectConfirmationData.getNotOnOrAfter());
    }
}
