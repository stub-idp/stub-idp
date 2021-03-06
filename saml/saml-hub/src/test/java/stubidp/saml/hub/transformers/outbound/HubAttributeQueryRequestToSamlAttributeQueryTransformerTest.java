package stubidp.saml.hub.transformers.outbound;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.impl.EncryptedAssertionBuilder;
import stubidp.saml.domain.assertions.HubAssertion;
import stubidp.saml.domain.assertions.PersistentId;
import stubidp.saml.test.OpenSAMLRunner;
import stubidp.saml.hub.core.test.builders.PassthroughAssertionBuilder;
import stubidp.saml.domain.matching.HubAttributeQueryRequest;
import stubidp.saml.hub.factories.AttributeQueryAttributeFactory;
import stubidp.saml.utils.core.OpenSamlXmlObjectFactory;
import stubidp.saml.utils.core.transformers.outbound.OutboundAssertionToSubjectTransformer;
import stubidp.saml.utils.hub.factories.AttributeFactory_1_1;

import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static stubidp.saml.domain.assertions.UserAccountCreationAttribute.CURRENT_ADDRESS;
import static stubidp.saml.domain.assertions.UserAccountCreationAttribute.DATE_OF_BIRTH;
import static stubidp.saml.test.builders.HubAttributeQueryRequestBuilder.aHubAttributeQueryRequest;
import static stubidp.saml.test.builders.HubAssertionBuilder.aHubAssertion;

@ExtendWith(MockitoExtension.class)
class HubAttributeQueryRequestToSamlAttributeQueryTransformerTest extends OpenSAMLRunner {

    private static final String ENCRYPTED_MDS_ASSERTION = "encrypted-mds-assertion!";
    private static final String ENCRYPTED_AUTHN_ASSERTION = "encrypted-authn-statement-assertion!";
    public static final String AUTHN_STATEMENT_ID = "AUTHEN_STATEMENT_ID";

    private HubAttributeQueryRequestToSamlAttributeQueryTransformer transformer;

    private OpenSamlXmlObjectFactory openSamlXmlObjectFactory;

    @Mock
    private AttributeFactory_1_1 attributeFactory;
    @Mock
    private OutboundAssertionToSubjectTransformer outboundAssertionToSubjectTransformer;
    @Mock
    private AttributeQueryAttributeFactory attributeQueryAttributeFactory;
    @Mock
    private EncryptedAssertionUnmarshaller encryptedAssertionUnmarshaller;

    @BeforeEach
    void setup() {
        openSamlXmlObjectFactory = new OpenSamlXmlObjectFactory();
        HubAssertionMarshaller assertionTransformer = new HubAssertionMarshaller(openSamlXmlObjectFactory, attributeFactory, outboundAssertionToSubjectTransformer);

        transformer = new HubAttributeQueryRequestToSamlAttributeQueryTransformer(
                openSamlXmlObjectFactory,
                assertionTransformer,
                attributeQueryAttributeFactory,
                encryptedAssertionUnmarshaller);
    }

    @Test
    void transform_shouldProperlyTransform() {
        PersistentId persistentId = new PersistentId("default-name-id");
        HubAttributeQueryRequest originalQuery = aHubAttributeQueryRequest()
                .withId("originalId")
                .withPersistentId(persistentId)
                .build();

        AttributeQuery transformedQuery = transformer.apply(originalQuery);

        assertThat(transformedQuery.getID()).isEqualTo(originalQuery.getId());
        assertThat(transformedQuery.getSubject().getNameID().getValue()).isEqualTo(persistentId.getNameId());
        assertThat(transformedQuery.getIssuer().getValue()).isEqualTo(originalQuery.getIssuer());
        assertThat(transformedQuery.getVersion()).isEqualTo(SAMLVersion.VERSION_20);
    }

    @Test
    void transform_shouldOnlyIncludeCycle3Unencrypted() {
        final HubAssertion cycle3DataAssertion = aHubAssertion().build();

        HubAttributeQueryRequest originalQuery = aHubAttributeQueryRequest()
                .withCycle3DataAssertion(cycle3DataAssertion)
                .build();

        AttributeQuery transformedQuery = transformer.apply(originalQuery);

        List<XMLObject> unknownXMLObjects = transformedQuery.getSubject().getSubjectConfirmations().get(0).getSubjectConfirmationData().getUnknownXMLObjects(Assertion.DEFAULT_ELEMENT_NAME);
        assertThat(unknownXMLObjects.size()).isEqualTo(1);
        assertThat(((Assertion)unknownXMLObjects.get(0)).getID()).isEqualTo(cycle3DataAssertion.getId());
    }

    @Test
    void transform_shouldContainBothMdsAndAuthnAssertionsEncrypted() {
        HubAttributeQueryRequest originalQuery = aHubAttributeQueryRequest()
                .withEncryptedMatchingDatasetAssertion(ENCRYPTED_MDS_ASSERTION)
                .withEncryptedAuthnAssertion(ENCRYPTED_AUTHN_ASSERTION)
                .build();

        final EncryptedAssertion value1 = new EncryptedAssertionBuilder().buildObject();
        final EncryptedAssertion value2 = new EncryptedAssertionBuilder().buildObject();
        when(encryptedAssertionUnmarshaller.transform(ENCRYPTED_MDS_ASSERTION)).thenReturn(value1);
        when(encryptedAssertionUnmarshaller.transform(ENCRYPTED_AUTHN_ASSERTION)).thenReturn(value2);
        AttributeQuery transformedQuery = transformer.apply(originalQuery);

        List<XMLObject> encryptedAssertions = transformedQuery.getSubject()
                .getSubjectConfirmations().get(0).getSubjectConfirmationData().getUnknownXMLObjects(EncryptedAssertion.DEFAULT_ELEMENT_NAME);
        assertThat(encryptedAssertions.size()).isEqualTo(2);
        assertThat(encryptedAssertions).contains(value1, value2);
    }

    @Test
    void transform_shouldSetTheSPNameQualifierAndNameQualifierToValuesThatShouldntBeThereButCurrentlyHaveNoWhereBetterToBe() {
        final String authnStatementAssertion = PassthroughAssertionBuilder.aPassthroughAssertion().withUnderlyingAssertion(ENCRYPTED_AUTHN_ASSERTION).buildAuthnStatementAssertionAsString();
        final HubAssertion cycle3DataAssertion = aHubAssertion().build();

        HubAttributeQueryRequest originalQuery = aHubAttributeQueryRequest()
                .withEncryptedAuthnAssertion(authnStatementAssertion)
                .withCycle3DataAssertion(cycle3DataAssertion)
                .withAssertionConsumerServiceUrl(URI.create("/foo"))
                .withAuthnRequestIssuerEntityId("authn-request-issuer")
                .build();

        AttributeQuery transformedQuery = transformer.apply(originalQuery);

        NameID nameID = transformedQuery.getSubject().getNameID();

        assertThat(nameID.getSPNameQualifier()).isEqualTo("authn-request-issuer");
        assertThat(nameID.getNameQualifier()).isEqualTo("/foo");
    }

    @Test
    void transform_shouldSetAttributesToUserAccountCreationAttributes(){
        Attribute attribute1 = openSamlXmlObjectFactory.createAttribute();
        Attribute attribute2 = openSamlXmlObjectFactory.createAttribute();
        when(attributeQueryAttributeFactory.createAttribute(CURRENT_ADDRESS)).thenReturn(attribute1);
        when(attributeQueryAttributeFactory.createAttribute(DATE_OF_BIRTH)).thenReturn(attribute2);

        HubAttributeQueryRequest hubAttributeQueryRequest = aHubAttributeQueryRequest()
                .addUserAccountCreationAttribute(CURRENT_ADDRESS)
                .addUserAccountCreationAttribute(DATE_OF_BIRTH)
                .build();

        AttributeQuery transformedQuery = transformer.apply(hubAttributeQueryRequest);

        List<Attribute> transformedQueryAttributes = transformedQuery.getAttributes();
        assertThat(transformedQueryAttributes.size()).isEqualTo(2);
        assertThat(transformedQueryAttributes).contains(attribute1);
        assertThat(transformedQueryAttributes).contains(attribute2);
    }

    @Test
    void transform_shouldNotExplodeWhenUserAccountCreationAttributesAreAbsent(){
        HubAttributeQueryRequest hubAttributeQueryRequest = aHubAttributeQueryRequest()
                .withoutUserAccountCreationAttributes()
                .build();

        AttributeQuery transformedQuery = transformer.apply(hubAttributeQueryRequest);

        List<Attribute> transformedQueryAttributes = transformedQuery.getAttributes();
        assertThat(transformedQueryAttributes.size()).isEqualTo(0);
    }
}
