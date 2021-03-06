package stubidp.saml.utils.core.transformers.inbound;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.SubjectConfirmationData;
import stubidp.saml.domain.assertions.AssertionRestrictions;
import stubidp.saml.test.OpenSAMLRunner;
import stubidp.saml.domain.assertions.Cycle3Dataset;
import stubidp.saml.domain.assertions.HubAssertion;
import stubidp.saml.test.builders.AssertionBuilder;
import stubidp.saml.test.builders.AttributeStatementBuilder;
import stubidp.saml.test.builders.IssuerBuilder;
import stubidp.saml.test.builders.Cycle3DatasetBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static stubidp.test.devpki.TestEntityIds.HUB_ENTITY_ID;

@ExtendWith(MockitoExtension.class)
public class HubAssertionUnmarshallerTest extends OpenSAMLRunner {

    @Mock
    private Cycle3DatasetFactory assertionCycle3DatasetTransformer;

    private HubAssertionUnmarshaller hubAssertionUnmarshaller;

    @BeforeEach
    void setUp() {
        hubAssertionUnmarshaller = new HubAssertionUnmarshaller(
                assertionCycle3DatasetTransformer, HUB_ENTITY_ID);
    }

    @Test
    void transform_shouldDelegateCycle3DataTransformation() {
        Assertion cycle3Assertion = AssertionBuilder.aCycle3DatasetAssertion("name", "value").buildUnencrypted();
        Cycle3Dataset cycle3Data = Cycle3DatasetBuilder.aCycle3Dataset().addCycle3Data("name", "value").build();
        Mockito.when(assertionCycle3DatasetTransformer.createCycle3DataSet(cycle3Assertion)).thenReturn(cycle3Data);

        HubAssertion hubAssertion = hubAssertionUnmarshaller.toHubAssertion(cycle3Assertion);

        assertThat(hubAssertion.getCycle3Data().isPresent()).isEqualTo(true);
        assertThat(hubAssertion.getCycle3Data().get()).isEqualTo(cycle3Data);
    }

    @Test
    void transform_shouldTransformSubjectConfirmationData() {
        Assertion assertion = AssertionBuilder.anAssertion()
                .withIssuer(IssuerBuilder.anIssuer().withIssuerId(HUB_ENTITY_ID).build())
                .addAttributeStatement(AttributeStatementBuilder.anAttributeStatement().build()).buildUnencrypted();
        SubjectConfirmationData subjectConfirmationData = assertion.getSubject().getSubjectConfirmations().get(0).getSubjectConfirmationData();

        final HubAssertion hubAssertion = hubAssertionUnmarshaller.toHubAssertion(assertion);

        final AssertionRestrictions assertionRestrictions = hubAssertion.getAssertionRestrictions();

        assertThat(assertionRestrictions.getInResponseTo()).isEqualTo(subjectConfirmationData.getInResponseTo());
        assertThat(assertionRestrictions.getRecipient()).isEqualTo(subjectConfirmationData.getRecipient());
        assertThat(assertionRestrictions.getNotOnOrAfter()).isEqualTo(subjectConfirmationData.getNotOnOrAfter());
    }
}
