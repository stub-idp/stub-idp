package uk.gov.ida.integrationtest.helpers;

import com.google.common.collect.ImmutableList;
import org.apache.commons.codec.binary.Base64;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.Audience;
import org.opensaml.saml.saml2.core.AudienceRestriction;
import org.opensaml.saml.saml2.core.Conditions;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.saml.saml2.core.SubjectConfirmation;
import org.opensaml.saml.saml2.core.SubjectConfirmationData;
import org.opensaml.saml.saml2.core.impl.AudienceBuilder;
import org.opensaml.saml.saml2.core.impl.AudienceRestrictionBuilder;
import org.opensaml.saml.saml2.core.impl.ConditionsBuilder;
import org.opensaml.saml.saml2.encryption.Decrypter;
import org.opensaml.xmlsec.signature.Signature;
import stubidp.saml.extensions.extensions.IdaAuthnContext;
import stubidp.saml.security.AssertionDecrypter;
import stubidp.saml.security.DecrypterFactory;
import stubidp.saml.security.IdaKeyStore;
import stubidp.saml.security.IdaKeyStoreCredentialRetriever;
import stubidp.saml.security.validators.encryptedelementtype.EncryptionAlgorithmValidator;
import stubidp.saml.test.TestCredentialFactory;
import stubidp.saml.test.builders.AddressAttributeBuilder_1_1;
import stubidp.saml.test.builders.AddressAttributeValueBuilder_1_1;
import stubidp.saml.test.builders.AttributeQueryBuilder;
import stubidp.saml.test.builders.AuthnStatementBuilder;
import stubidp.saml.test.builders.DateAttributeBuilder_1_1;
import stubidp.saml.test.builders.GenderAttributeBuilder_1_1;
import stubidp.saml.test.builders.SubjectConfirmationBuilder;
import stubidp.saml.test.builders.SubjectConfirmationDataBuilder;
import stubidp.utils.security.security.PrivateKeyFactory;
import stubidp.utils.security.security.PublicKeyFactory;
import stubidp.utils.security.security.X509CertificateFactory;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static stubidp.saml.test.builders.AssertionBuilder.anAssertion;
import static stubidp.saml.test.builders.AuthnContextBuilder.anAuthnContext;
import static stubidp.saml.test.builders.AuthnContextClassRefBuilder.anAuthnContextClassRef;
import static stubidp.saml.test.builders.AuthnStatementBuilder.anAuthnStatement;
import static stubidp.saml.test.builders.AuthnStatementBuilder.anEidasAuthnStatement;
import static stubidp.saml.test.builders.IPAddressAttributeBuilder.anIPAddress;
import static stubidp.saml.test.builders.IssuerBuilder.anIssuer;
import static stubidp.saml.test.builders.NameIdBuilder.aNameId;
import static stubidp.saml.test.builders.PersonNameAttributeBuilder_1_1.aPersonName_1_1;
import static stubidp.saml.test.builders.PersonNameAttributeValueBuilder.aPersonNameValue;
import static stubidp.saml.test.builders.SignatureBuilder.aSignature;
import static stubidp.saml.test.builders.SimpleStringAttributeBuilder.aSimpleStringAttribute;
import static stubidp.saml.test.builders.SubjectBuilder.aSubject;
import static stubidp.saml.test.builders.SubjectConfirmationBuilder.aSubjectConfirmation;
import static stubidp.saml.test.builders.SubjectConfirmationDataBuilder.aSubjectConfirmationData;
import static stubidp.test.devpki.PemCertificateStrings.HUB_TEST_PUBLIC_ENCRYPTION_CERT;
import static stubidp.test.devpki.TestCertificateStrings.HUB_TEST_PRIVATE_ENCRYPTION_KEY;
import static stubidp.test.devpki.TestCertificateStrings.HUB_TEST_PRIVATE_SIGNING_KEY;
import static stubidp.test.devpki.TestCertificateStrings.HUB_TEST_PUBLIC_SIGNING_CERT;
import static stubidp.test.devpki.TestCertificateStrings.PRIVATE_SIGNING_KEYS;
import static stubidp.test.devpki.TestCertificateStrings.STUB_COUNTRY_PUBLIC_PRIMARY_CERT;
import static stubidp.test.devpki.TestCertificateStrings.STUB_COUNTRY_PUBLIC_PRIMARY_PRIVATE_KEY;
import static stubidp.test.devpki.TestCertificateStrings.STUB_IDP_PUBLIC_PRIMARY_CERT;
import static stubidp.test.devpki.TestCertificateStrings.STUB_IDP_PUBLIC_PRIMARY_PRIVATE_KEY;
import static stubidp.test.devpki.TestCertificateStrings.TEST_RP_MS_PRIVATE_ENCRYPTION_KEY;
import static stubidp.test.devpki.TestCertificateStrings.TEST_RP_MS_PUBLIC_ENCRYPTION_CERT;
import static stubidp.test.devpki.TestCertificateStrings.TEST_RP_PRIVATE_SIGNING_KEY;
import static stubidp.test.devpki.TestCertificateStrings.TEST_RP_PUBLIC_SIGNING_CERT;
import static stubidp.test.devpki.TestCertificateStrings.getPrimaryPublicEncryptionCert;
import static stubidp.test.devpki.TestEntityIds.HUB_ENTITY_ID;
import static stubidp.test.devpki.TestEntityIds.HUB_SECONDARY_ENTITY_ID;
import static stubidp.test.devpki.TestEntityIds.STUB_IDP_ONE;
import static uk.gov.ida.matchingserviceadapter.builders.AttributeStatementBuilder.aPersonIdentifierAttribute;
import static uk.gov.ida.matchingserviceadapter.builders.AttributeStatementBuilder.anAttributeStatement;
import static uk.gov.ida.matchingserviceadapter.builders.AttributeStatementBuilder.anEidasAttributeStatement;

public class AssertionHelper {

    private static Signature aValidSignature(String cert, String privateKey) {
        return aSignature()
                .withSigningCredential(
                        new TestCredentialFactory(
                                cert,
                                privateKey
                        ).getSigningCredential()
                ).build();
    }

    private static Signature aValidSignature() {
        return aValidSignature(STUB_IDP_PUBLIC_PRIMARY_CERT, STUB_IDP_PUBLIC_PRIMARY_PRIVATE_KEY);
    }

    public static AssertionDecrypter anAssertionDecrypter() {
        PublicKeyFactory publicKeyFactory = new PublicKeyFactory(new X509CertificateFactory());
        PrivateKey privateKey = new PrivateKeyFactory().createPrivateKey(Base64.decodeBase64(PRIVATE_SIGNING_KEYS.get(HUB_ENTITY_ID)));
        PublicKey publicKey = publicKeyFactory.createPublicKey(getPrimaryPublicEncryptionCert(HUB_ENTITY_ID));

        PrivateKey privateEncryptionKey = new PrivateKeyFactory().createPrivateKey(Base64.decodeBase64(HUB_TEST_PRIVATE_ENCRYPTION_KEY));
        PublicKey publicEncryptionKey = publicKeyFactory.createPublicKey(HUB_TEST_PUBLIC_ENCRYPTION_CERT);

        KeyPair encryptionKeyPair = new KeyPair(publicEncryptionKey, privateEncryptionKey);

        KeyPair signingKeyPair = new KeyPair(publicKey, privateKey);
        IdaKeyStore keyStore = new IdaKeyStore(signingKeyPair, singletonList(encryptionKeyPair));

        IdaKeyStoreCredentialRetriever idaKeyStoreCredentialRetriever = new IdaKeyStoreCredentialRetriever(keyStore);
        Decrypter decrypter = new DecrypterFactory().createDecrypter(idaKeyStoreCredentialRetriever.getDecryptingCredentials());
        return new AssertionDecrypter(new EncryptionAlgorithmValidator(), decrypter);
    }

    public static Assertion aCompleteMatchingDatasetAssertion(String requestId) {
        List<Attribute> attributes = asList(
            aPersonName_1_1().addValue(aPersonNameValue().withValue("OldSurname").withFrom(LocalDate.of(1990, 1, 30)).withTo(LocalDate.of(2000, 1, 29)).withVerified(true).build()).buildAsSurname(),
            aPersonName_1_1().addValue(aPersonNameValue().withValue("CurrentSurname").withVerified(true).build()).buildAsSurname(),
            aPersonName_1_1().addValue(aPersonNameValue().withValue("FirstName").withVerified(false).build()).buildAsFirstname(),
            AddressAttributeBuilder_1_1.anAddressAttribute().addAddress(new AddressAttributeValueBuilder_1_1().addLines(ImmutableList.of("address line 1")).withVerified(false).build()).buildCurrentAddress(),
            AddressAttributeBuilder_1_1.anAddressAttribute().addAddress(new AddressAttributeValueBuilder_1_1().addLines(ImmutableList.of("address line 2")).withVerified(true).build()).buildPreviousAddress(),
            GenderAttributeBuilder_1_1.aGender_1_1().build(),
            DateAttributeBuilder_1_1.aDate_1_1().buildAsDateOfBirth());

        return aMatchingDatasetAssertion(attributes, false, requestId);
    }

    public static Assertion assertionWithOnlyFirstName(String requestId) {
        List<Attribute> attributes = asList(
            aPersonName_1_1().addValue(aPersonNameValue().withValue("SteveFirstName").withFrom(LocalDate.of(2000, 1, 30)).withTo(LocalDate.of(2010, 12, 30)).build()).buildAsFirstname(),
            aPersonName_1_1().addValue(aPersonNameValue().withValue("Surname").build()).buildAsSurname());

        return aMatchingDatasetAssertion(attributes, false, requestId);
    }

    public static Assertion anAuthnStatementAssertion(String inResponseTo) {
        return anAuthnStatementAssertion(IdaAuthnContext.LEVEL_2_AUTHN_CTX, inResponseTo);
    }

    public static Assertion anAuthnStatementAssertion(String authnContext, String inResponseTo) {
        return anAssertion()
                .addAuthnStatement(
                    anAuthnStatement()
                        .withAuthnContext(
                            anAuthnContext()
                                .withAuthnContextClassRef(
                                    anAuthnContextClassRef()
                                        .withAuthnContextClasRefValue(authnContext)
                                        .build())
                                .build())
                        .build())
                .withSubject(
                    aSubject()
                        .withSubjectConfirmation(
                            aSubjectConfirmation()
                                .withSubjectConfirmationData(
                                    aSubjectConfirmationData()
                                        .withInResponseTo(inResponseTo)
                                        .build()
                                ).build()
                        ).build())
                .withIssuer(anIssuer().withIssuerId(STUB_IDP_ONE).build())
                .addAttributeStatement(anAttributeStatement().addAttribute(anIPAddress().build()).build())
                .buildUnencrypted();
    }

    public static Subject aSubjectWithAssertions(List<Assertion> assertions, String requestId, String hubEntityId) {
        return aSubjectWithAssertions(assertions, requestId, hubEntityId, "default-pid");
    }

    public static Subject aSubjectWithAssertions(List<Assertion> assertions, String requestId, String hubEntityId, String pid) {
        final NameID nameId = aNameId().withValue(pid).withNameQualifier("").withSpNameQualifier(hubEntityId).build();
        SubjectConfirmationDataBuilder subjectConfirmationDataBuilder = aSubjectConfirmationData().withInResponseTo(requestId);
        assertions.forEach(subjectConfirmationDataBuilder::addAssertion);
        final SubjectConfirmationData subjectConfirmationData = subjectConfirmationDataBuilder.build();
        final SubjectConfirmation subjectConfirmation = SubjectConfirmationBuilder.aSubjectConfirmation()
            .withSubjectConfirmationData(subjectConfirmationData).build();

        return aSubject().withNameId(nameId).withSubjectConfirmation(subjectConfirmation).build();
    }

    public static Subject aSubjectWithEncryptedAssertions(List<EncryptedAssertion> assertions, String requestId, String hubEntityId) {
        final NameID nameId = aNameId().withNameQualifier("").withSpNameQualifier(hubEntityId).build();

        SubjectConfirmationDataBuilder subjectConfirmationDataBuilder = aSubjectConfirmationData().withInResponseTo(requestId);
        assertions.forEach(subjectConfirmationDataBuilder::addAssertion);

        final SubjectConfirmationData subjectConfirmationData = subjectConfirmationDataBuilder.build();
        final SubjectConfirmation subjectConfirmation = SubjectConfirmationBuilder.aSubjectConfirmation()
                .withSubjectConfirmationData(subjectConfirmationData).build();

        return aSubject().withNameId(nameId).withSubjectConfirmation(subjectConfirmation).build();
    }

    public static Assertion aMatchingDatasetAssertion(List<Attribute> attributes, boolean shouldBeExpired, String requestId) {
        return aMatchingDatasetAssertionWithSignature(attributes, aValidSignature(), shouldBeExpired, requestId);
    }

    public static Assertion aMatchingDatasetAssertionWithSignature(List<Attribute> attributes, Signature signature, boolean shouldBeExpired, String requestId) {
        return anAssertion()
                .withId("mds-assertion")
                .withIssuer(anIssuer().withIssuerId(STUB_IDP_ONE).build())
                .withSubject(
                        anAssertionSubject(requestId, shouldBeExpired)
                )
                .withSignature(signature)
                .addAttributeStatement(
                        anAttributeStatement()
                                .addAllAttributes(attributes)
                                .build()
                )
//                .withConditions(aConditions())
                .buildUnencrypted();
    }

    public static EncryptedAssertion aCycle3Assertion(String attributeName, String attributeValue, String requestId) {
        return anAssertion()
            .withId("cycle3-assertion")
            .withIssuer(anIssuer().withIssuerId(HUB_ENTITY_ID).build())
            .withSubject(
                anAssertionSubject(requestId, false)
            )
            .withSignature(aValidSignature(HUB_TEST_PUBLIC_SIGNING_CERT, HUB_TEST_PRIVATE_SIGNING_KEY))
            .addAttributeStatement(
                anAttributeStatement()
                    .addAllAttributes(singletonList(
                            aSimpleStringAttribute()
                                    .withName(attributeName)
                                    .withSimpleStringValue(attributeValue)
                                    .build()
                    ))
                    .build()
            ).buildWithEncrypterCredential(
                new TestCredentialFactory(
                    TEST_RP_MS_PUBLIC_ENCRYPTION_CERT,
                    TEST_RP_MS_PRIVATE_ENCRYPTION_KEY
                ).getEncryptingCredential());
    }

    public static EncryptedAssertion anEidasEncryptedAssertion(String requestId, String issuerId, Signature assertionSignature, List<Attribute> assertions) {
        return anAssertion()
            .withSubject(
                aSubject().withSubjectConfirmation(
                    aSubjectConfirmation().withSubjectConfirmationData(
                        aSubjectConfirmationData()
                            .withInResponseTo(requestId)
                            .build())
                        .build())
                    .build())
                .withIssuer(
                    anIssuer()
                        .withIssuerId(issuerId)
                        .build())
                .addAttributeStatement(anEidasAttributeStatement().addAllAttributes(assertions).build())
                .addAuthnStatement(anEidasAuthnStatement().build())
                .withSignature(assertionSignature)
                .withConditions(aConditions())
                .buildWithEncrypterCredential(
                    new TestCredentialFactory(
                        TEST_RP_MS_PUBLIC_ENCRYPTION_CERT,
                        TEST_RP_MS_PRIVATE_ENCRYPTION_KEY
                    ).getEncryptingCredential()
                );
    }

    public static EncryptedAssertion anEidasEncryptedAssertionWithInvalidSignature(String assertionIssuerId) {
        return anAssertion()
            .addAuthnStatement(AuthnStatementBuilder.anAuthnStatement().build())
            .withIssuer(
                anIssuer()
                    .withIssuerId(assertionIssuerId)
                    .build())
            .withSignature(aSignature()
                .withSigningCredential(
                    new TestCredentialFactory(
                        TEST_RP_PUBLIC_SIGNING_CERT,
                        TEST_RP_PRIVATE_SIGNING_KEY
                    ).getSigningCredential()
                ).build())
            .withConditions(aConditions())
            .buildWithEncrypterCredential(
                new TestCredentialFactory(
                    TEST_RP_MS_PUBLIC_ENCRYPTION_CERT,
                    TEST_RP_MS_PRIVATE_ENCRYPTION_KEY
                ).getEncryptingCredential()
            );
    }

    public static Subject anEidasSubject(String requestId, String assertionIssuerId, Signature assertionSignature) {
        return aSubjectWithEncryptedAssertions(
                singletonList(anEidasEncryptedAssertion(requestId, assertionIssuerId, assertionSignature, List.of(aPersonIdentifierAttribute()))),
                requestId, HUB_ENTITY_ID);
    }

    public static AttributeQueryBuilder aValidEidasAttributeQuery(String requestId, String assertionIssuerId) {
        return AttributeQueryBuilder.anAttributeQuery()
                .withId(requestId)
                .withIssuer(anIssuer().withIssuerId(HUB_ENTITY_ID).build())
                .withSubject(aSubjectWithEncryptedAssertions(
                        singletonList(
                                anEidasEncryptedAssertion(requestId, assertionIssuerId, anEidasSignature(), List.of(aPersonIdentifierAttribute()))
                        ), requestId, HUB_ENTITY_ID))
                .withSignature(aHubSignature());
    }

    public static AttributeQueryBuilder aValidEidasAttributeQueryWithCycle3Attributes(String requestId, String assertionIssuerId, List<Attribute> attributes) {
        return AttributeQueryBuilder.anAttributeQuery()
                .withId(requestId)
                .withIssuer(anIssuer().withIssuerId(HUB_ENTITY_ID).build())
                .withSubject(aSubjectWithEncryptedAssertions(
                        asList(
                                aCycle3Assertion("NI", "123456", requestId),
                                anEidasEncryptedAssertion(requestId, assertionIssuerId, anEidasSignature(), attributes)
                        ), requestId, HUB_ENTITY_ID))
                .withSignature(aHubSignature());
    }

    public static Signature aHubSignature() {
        return aSignature()
                .withSigningCredential(
                        new TestCredentialFactory(
                                HUB_TEST_PUBLIC_SIGNING_CERT,
                                HUB_TEST_PRIVATE_SIGNING_KEY
                        ).getSigningCredential()
                ).build();
    }

    public static Signature anIdpSignature() {
        return aSignature()
                .withSigningCredential(
                        new TestCredentialFactory(
                                STUB_IDP_PUBLIC_PRIMARY_CERT,
                                STUB_IDP_PUBLIC_PRIMARY_PRIVATE_KEY
                        ).getSigningCredential()
                ).build();
    }

    public static Signature anEidasSignature() {
        return aSignature()
                .withSigningCredential(
                        new TestCredentialFactory(
                                STUB_COUNTRY_PUBLIC_PRIMARY_CERT,
                                STUB_COUNTRY_PUBLIC_PRIMARY_PRIVATE_KEY
                        ).getSigningCredential()
                ).build();
    }

    private static Subject anAssertionSubject(final String inResponseTo, boolean shouldBeExpired) {
        final Instant notOnOrAfter;
        if (shouldBeExpired) {
            notOnOrAfter = Instant.now().minus(5, ChronoUnit.MINUTES);
        } else {
            notOnOrAfter = Instant.now().plusMillis(1000000);
        }
        return aSubject()
                .withSubjectConfirmation(
                        aSubjectConfirmation()
                                .withSubjectConfirmationData(
                                        aSubjectConfirmationData()
                                                .withNotOnOrAfter(notOnOrAfter)
                                                .withInResponseTo(inResponseTo)
                                                .build()
                                ).build()
                ).build();
    }

    private static Conditions aConditions() {
        Conditions conditions = new ConditionsBuilder().buildObject();
        conditions.setNotBefore(Instant.now());
        conditions.setNotOnOrAfter(Instant.now().plus(10, ChronoUnit.MINUTES));
        AudienceRestriction audienceRestriction= new AudienceRestrictionBuilder().buildObject();
        Audience audience = new AudienceBuilder().buildObject();
        audience.setURI(HUB_SECONDARY_ENTITY_ID);
        audienceRestriction.getAudiences().add(audience);
        conditions.getAudienceRestrictions().add(audienceRestriction);
        return conditions;
    }
}
