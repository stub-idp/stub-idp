package uk.gov.ida.stub.idp;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opensaml.saml.saml2.core.Response;
import stubidp.saml.utils.core.OpenSamlXmlObjectFactory;
import stubidp.saml.utils.core.domain.IdentityProviderAssertion;
import stubidp.saml.utils.core.test.builders.MatchingDatasetBuilder;
import stubidp.saml.utils.core.transformers.outbound.OutboundAssertionToSubjectTransformer;
import stubidp.saml.utils.hub.factories.AttributeFactory_1_1;
import uk.gov.ida.saml.idp.stub.transformers.outbound.IdentityProviderAssertionToAssertionTransformer;
import uk.gov.ida.saml.idp.stub.transformers.outbound.IdentityProviderAuthnStatementToAuthnStatementTransformer;
import uk.gov.ida.stub.idp.domain.IdpIdaStatusMarshaller;
import uk.gov.ida.stub.idp.domain.OutboundResponseFromIdp;
import uk.gov.ida.stub.idp.saml.transformers.OutboundResponseFromIdpToSamlResponseTransformer;

import static org.assertj.core.api.Assertions.assertThat;
import static stubidp.saml.stubidp.test.builders.IdentityProviderAssertionBuilder.anIdentityProviderAssertion;

@RunWith(OpenSAMLRunner.class)
public class OutboundResponseFromIdpToSamlResponseTransformerTest {

    private OpenSamlXmlObjectFactory openSamlXmlObjectFactory = new OpenSamlXmlObjectFactory();
    private OutboundResponseFromIdpToSamlResponseTransformer transformer;

    @Before
    public void setup() {
        IdpIdaStatusMarshaller statusTransformer = new IdpIdaStatusMarshaller(openSamlXmlObjectFactory);
        OutboundAssertionToSubjectTransformer outboundAssertionToSubjectTransformer = new OutboundAssertionToSubjectTransformer(openSamlXmlObjectFactory);
        IdentityProviderAssertionToAssertionTransformer assertionTransformer = new IdentityProviderAssertionToAssertionTransformer(
                openSamlXmlObjectFactory,
                new AttributeFactory_1_1(openSamlXmlObjectFactory),
                new IdentityProviderAuthnStatementToAuthnStatementTransformer(openSamlXmlObjectFactory),
                outboundAssertionToSubjectTransformer);
        transformer = new OutboundResponseFromIdpToSamlResponseTransformer(
                statusTransformer,
                openSamlXmlObjectFactory,
                assertionTransformer);
    }

    @Test
    public void transform_shouldTransformMatchingDataAssertion() throws Exception {
        Response response = openSamlXmlObjectFactory.createResponse();
        IdentityProviderAssertion assertion = anIdentityProviderAssertion().withMatchingDataset(MatchingDatasetBuilder.aMatchingDataset().build()).build();
        OutboundResponseFromIdp originalResponse = OutboundResponseFromIdp.createSuccessResponseFromIdp(
                "in-response-to",
                "issuer-id",
                assertion,
                null,
                null);

        transformer.transformAssertions(originalResponse, response);

        assertThat(response.getAssertions().size()).isEqualTo(1);
        assertThat(response.getAssertions().get(0).getAttributeStatements().size()).isEqualTo(1);
    }
}
