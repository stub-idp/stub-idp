package stubidp.saml.test.builders;

import org.opensaml.core.xml.XMLObjectBuilderFactory;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.Conditions;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.saml.saml2.encryption.Encrypter;
import org.opensaml.security.credential.Credential;
import org.opensaml.xmlsec.algorithm.descriptors.DigestSHA256;
import org.opensaml.xmlsec.algorithm.descriptors.SignatureRSASHA256;
import org.opensaml.xmlsec.encryption.support.DataEncryptionParameters;
import org.opensaml.xmlsec.encryption.support.EncryptionConstants;
import org.opensaml.xmlsec.encryption.support.EncryptionException;
import org.opensaml.xmlsec.encryption.support.KeyEncryptionParameters;
import org.opensaml.xmlsec.encryption.support.RSAOAEPParameters;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.Signer;
import stubidp.saml.test.TestCredentialFactory;
import stubidp.test.devpki.TestCertificateStrings;
import stubidp.test.devpki.TestEntityIds;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AssertionBuilder {
    private static final XMLObjectBuilderFactory factory = XMLObjectProviderRegistrySupport.getBuilderFactory();

    private boolean shouldSign = true;
    private SAMLVersion version = SAMLVersion.VERSION_20;
    private final List<AttributeStatement> attributeStatements = new ArrayList<>();
    private final List<AuthnStatement> authnStatements = new ArrayList<>();

    private Optional<String> id = Optional.of("some-assertion-id");
    private Optional<Subject> subject = Optional.ofNullable(SubjectBuilder.aSubject().build());
    private Optional<Issuer> issuer = Optional.ofNullable(IssuerBuilder.anIssuer().build());
    private Optional<Signature> signature = Optional.ofNullable(SignatureBuilder.aSignature().build());
    private Optional<Conditions> conditions = Optional.ofNullable(ConditionsBuilder.aConditions().build());
    private Optional<Instant> issueInstant = Optional.of(Instant.now());

    private AssertionBuilder() {}

    public static AssertionBuilder anAssertion() {
        return new AssertionBuilder();
    }

    public static AssertionBuilder anEidasAssertion() {
        return anAssertion()
                .withConditions(
                        ConditionsBuilder.aConditions()
                                .validFor(Duration.ofMinutes(10))
                                .restrictedToAudience(TestEntityIds.HUB_CONNECTOR_ENTITY_ID)
                                .build())
                .withIssuer(
                        IssuerBuilder.anIssuer()
                                .withIssuerId(TestEntityIds.STUB_COUNTRY_ONE)
                                .build())
                .addAttributeStatement(AttributeStatementBuilder.anEidasAttributeStatement().build())
                .addAuthnStatement(AuthnStatementBuilder.anEidasAuthnStatement().build())
                .withSubject(
                        SubjectBuilder.aSubject()
                                .withSubjectConfirmation(
                                        SubjectConfirmationBuilder.aSubjectConfirmation()
                                                .withSubjectConfirmationData(SubjectConfirmationDataBuilder.aSubjectConfirmationData().withRecipient(TestEntityIds.HUB_CONNECTOR_ENTITY_ID).build())
                                                .build())
                                .build());
    }

    public static AssertionBuilder anAuthnStatementAssertion() {
        return anAssertion()
                .addAuthnStatement(AuthnStatementBuilder.anAuthnStatement().build())
                .addAttributeStatement(AttributeStatementBuilder.anAttributeStatement().addAttribute(IPAddressAttributeBuilder.anIPAddress().build()).build());
    }

    public static AssertionBuilder aMatchingDatasetAssertion(
            Attribute firstName,
            Attribute middlenames,
            Attribute surname,
            Attribute gender,
            Attribute dateOfBirth,
            Attribute currentAddress,
            Attribute previousAddresses) {
        AttributeStatementBuilder attributeStatementBuilder = AttributeStatementBuilder.anAttributeStatement()
                .addAttribute(firstName)
                .addAttribute(middlenames)
                .addAttribute(surname)
                .addAttribute(gender)
                .addAttribute(dateOfBirth)
                .addAttribute(currentAddress)
                .addAttribute(previousAddresses);

        return anAssertion()
                .addAttributeStatement(attributeStatementBuilder.build());
    }

    public static AssertionBuilder anEidasMatchingDatasetAssertion(
            Attribute firstName,
            Attribute surname,
            Attribute dateOfBirth,
            Attribute personalIdentifier) {
        return anEidasMatchingDatasetAssertion(firstName, surname, dateOfBirth, personalIdentifier, Optional.empty());
    }

    public static AssertionBuilder anEidasMatchingDatasetAssertion(
            Attribute firstName,
            Attribute surname,
            Attribute dateOfBirth,
            Attribute personalIdentifier,
            Optional<Attribute> gender) {
        AttributeStatementBuilder attributeStatementBuilder = AttributeStatementBuilder.anAttributeStatement()
                .addAttribute(firstName)
                .addAttribute(surname)
                .addAttribute(dateOfBirth)
                .addAttribute(personalIdentifier);

        gender.ifPresent(attributeStatementBuilder::addAttribute);

        return anAssertion()
                .addAttributeStatement(attributeStatementBuilder.build());
    }

    public static AssertionBuilder aCycle3DatasetAssertion(String name, String value) {
        SimpleStringAttributeBuilder attribute = SimpleStringAttributeBuilder.aSimpleStringAttribute()
                .withName(name)
                .withSimpleStringValue(value);

        AttributeStatementBuilder attributeStatementBuilder = AttributeStatementBuilder.anAttributeStatement()
                .addAttribute(attribute.build());

        return anAssertion()
                .withIssuer(IssuerBuilder.anIssuer().withIssuerId(TestEntityIds.HUB_ENTITY_ID).build()) // Hub entity id defines the ability to parse an attribute query
                .addAttributeStatement(attributeStatementBuilder.build());
    }

    public static AssertionBuilder aCycle3DatasetAssertion(List<Attribute> attributes) {
        AttributeStatementBuilder attributeStatementBuilder = AttributeStatementBuilder.anAttributeStatement();
        for (Attribute attribute : attributes) {
            attributeStatementBuilder.addAttribute(attribute);
        }
        return anAssertion()
                .withIssuer(IssuerBuilder.anIssuer().withIssuerId(TestEntityIds.HUB_ENTITY_ID).build()) // Hub entity id defines the ability to parse an attribute query
                .addAttributeStatement(attributeStatementBuilder.build());
    }

    public Assertion buildUnencrypted() {
        Assertion assertion = (Assertion) factory
                .getBuilder(Assertion.DEFAULT_ELEMENT_NAME)
                .buildObject(Assertion.DEFAULT_ELEMENT_NAME, Assertion.TYPE_NAME);

        id.ifPresent(assertion::setID);
        assertion.setVersion(version);
        subject.ifPresent(assertion::setSubject);
        issueInstant.ifPresent(assertion::setIssueInstant);
        assertion.getAttributeStatements().addAll(attributeStatements);
        assertion.getAuthnStatements().addAll(authnStatements);
        issuer.ifPresent(assertion::setIssuer);
        conditions.ifPresent(assertion::setConditions);
        try {
            if (signature.isPresent()) {
                assertion.setSignature(signature.get());
                XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(assertion).marshall(assertion);
                if (shouldSign) {
                    Signer.signObject(assertion.getSignature());
                }
            }
        } catch (SignatureException | MarshallingException e) {
            throw new RuntimeException(e);
        }

        return assertion;
    }

    public EncryptedAssertion build() {
        TestCredentialFactory credentialFactory = new TestCredentialFactory(TestCertificateStrings.TEST_PUBLIC_CERT, null);
        Credential credential = credentialFactory.getEncryptingCredential();

        return buildWithEncrypterCredential(credential);
    }

    public EncryptedAssertion buildWithEncrypterCredential(Credential credential) {
        return buildWithEncrypterCredential(createEncrypter(credential));
    }

    public EncryptedAssertion buildWithEncrypterCredential(Credential credential, String encryptionAlgorithm) {
        return buildWithEncrypterCredential(createEncrypter(credential, encryptionAlgorithm));
    }

    public EncryptedAssertion buildWithEncrypterCredential(Credential credential,
                                                           String encryptionAlgorithm,
                                                           String keyTransportEncryptionAlgorithm) {
        return buildWithEncrypterCredential(createEncrypter(credential, encryptionAlgorithm, keyTransportEncryptionAlgorithm));
    }

    public Encrypter createEncrypter(Credential credential) {
        return createEncrypter(credential, EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES128);
    }

    public Encrypter createEncrypter(Credential credential, String encryptionAlgorithm) {
        return createEncrypter(credential, encryptionAlgorithm, EncryptionConstants.ALGO_ID_KEYTRANSPORT_RSAOAEP);
    }

    public Encrypter createEncrypter(Credential credential, String encryptionAlgorithm, String keyTransportEncryptionAlgorithm) {
        DataEncryptionParameters encParams = new DataEncryptionParameters();
        encParams.setAlgorithm(encryptionAlgorithm);

        KeyEncryptionParameters kekParams = new KeyEncryptionParameters();
        kekParams.setEncryptionCredential(credential);
        kekParams.setAlgorithm(keyTransportEncryptionAlgorithm);
        final RSAOAEPParameters params = new RSAOAEPParameters();
        params.setDigestMethod(new DigestSHA256().getURI());
        kekParams.setRSAOAEPParameters(params);

        Encrypter encrypter = new Encrypter(encParams, kekParams);
        encrypter.setKeyPlacement(Encrypter.KeyPlacement.PEER);

        return encrypter;
    }

    public EncryptedAssertion buildWithEncrypterCredential(Encrypter encrypter) {
        Assertion assertion = buildUnencrypted();
        try {
            return encrypter.encrypt(assertion);
        } catch (EncryptionException e) {
            throw new RuntimeException(e);
        }
    }

    public AssertionBuilder withId(String id) {
        this.id = Optional.ofNullable(id);
        return this;
    }

    public AssertionBuilder withSubject(Subject subject) {
        this.subject = Optional.ofNullable(subject);
        return this;
    }

    public AssertionBuilder withIssuer(Issuer issuer) {
        this.issuer = Optional.ofNullable(issuer);
        return this;
    }

    public AssertionBuilder addAttributeStatement(AttributeStatement attributeStatement) {
        this.attributeStatements.add(attributeStatement);
        return this;
    }

    public AssertionBuilder withoutAttributeStatements() {
        attributeStatements.clear();
        return this;
    }

    public AssertionBuilder addAuthnStatement(AuthnStatement authnStatement) {
        authnStatements.add(authnStatement);
        return this;
    }

    public AssertionBuilder withoutAuthnStatements() {
        authnStatements.clear();
        return this;
    }

    public AssertionBuilder withoutSigning() {
        shouldSign = false;
        return this;
    }

    public AssertionBuilder withSignature(Signature signature) {
        this.signature = Optional.ofNullable(signature);
        return this;
    }

    public AssertionBuilder withIssueInstant(Instant issueInstant) {
        this.issueInstant = Optional.ofNullable(issueInstant);
        return this;
    }

    public AssertionBuilder withVersion(SAMLVersion version) {
        this.version = version;
        return this;
    }

    public AssertionBuilder withConditions(Conditions conditions) {
        this.conditions = Optional.ofNullable(conditions);
        return this;
    }
}
