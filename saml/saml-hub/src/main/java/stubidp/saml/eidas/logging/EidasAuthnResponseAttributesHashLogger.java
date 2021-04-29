package stubidp.saml.eidas.logging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.codec.binary.Hex;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.security.crypto.JCAConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import stubidp.saml.domain.matching.assertions.NonMatchingAttributes;
import stubidp.saml.domain.matching.assertions.NonMatchingVerifiableAttribute;
import stubidp.saml.utils.core.transformers.MatchingDatasetToNonMatchingAttributesMapper;
import stubidp.saml.utils.core.transformers.VerifyMatchingDatasetUnmarshaller;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static stubidp.saml.utils.core.transformers.MatchingDatasetToNonMatchingAttributesMapper.attributeComparator;

public final class EidasAuthnResponseAttributesHashLogger {

    public static final String MDC_KEY_EIDAS_REQUEST_ID = "hubRequestId";
    public static final String MDC_KEY_EIDAS_DESTINATION = "destination";
    public static final String MDC_KEY_EIDAS_USER_HASH = "eidasUserHash";

    private static final VerifyMatchingDatasetUnmarshaller MATCHING_DATASET_UNMARSHALLER = new VerifyMatchingDatasetUnmarshaller();
    private static final MatchingDatasetToNonMatchingAttributesMapper MATCHING_DATASET_MAPPER = new MatchingDatasetToNonMatchingAttributesMapper();
    private static final Logger LOG = LoggerFactory.getLogger(EidasAuthnResponseAttributesHashLogger.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());
    private static final MessageDigest MESSAGE_DIGEST;

    static {
        try {
            MESSAGE_DIGEST = MessageDigest.getInstance(JCAConstants.DIGEST_SHA256);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private EidasAuthnResponseAttributesHashLogger() {
    }

    public static void logEidasAttributesHash(NonMatchingAttributes attributes, String requestId, URI destination) {
        logExtractedAttributes(attributes, requestId, destination.toString());
    }

    public static void logEidasAttributesHash(Assertion assertion, Response response, String hashingEntityId) {
        final NonMatchingAttributes attributes = MATCHING_DATASET_MAPPER.mapToNonMatchingAttributes(MATCHING_DATASET_UNMARSHALLER.fromAssertion(assertion));
        logExtractedAttributes(attributes, response.getInResponseTo(), response.getDestination());
    }

    private static void logExtractedAttributes(NonMatchingAttributes attributes, String requestId, String destination) {
        final HashableResponseAttributes attributesToHash = new HashableResponseAttributes();

        if (attributes != null) {
            attributes.getFirstNames().stream()
                    .filter(NonMatchingVerifiableAttribute::isVerified)
                    .min(attributeComparator())
                    .ifPresent(firstName -> attributesToHash.setFirstName(firstName.getValue()));

            attributes.getMiddleNames().stream()
                    .sorted(attributeComparator())
                    .forEach(middleName -> attributesToHash.addMiddleName(middleName.getValue()));

            attributes.getSurnames().stream()
                    .sorted(attributeComparator())
                    .forEach(surname -> attributesToHash.addSurname(surname.getValue()));

            attributes.getDatesOfBirth().stream()
                    .filter(NonMatchingVerifiableAttribute::isVerified)
                    .min(attributeComparator())
                    .ifPresent(dateOfBirth -> attributesToHash.setDateOfBirth(dateOfBirth.getValue()));
        }

        attributesToHash.setRequestId(requestId);
        logHash(requestId, destination, attributesToHash);
    }

    private static void logHash(String requestId, String destination, HashableResponseAttributes responseAttributes) {
        try {
            MDC.put(MDC_KEY_EIDAS_REQUEST_ID, requestId);
            MDC.put(MDC_KEY_EIDAS_DESTINATION, destination);
            MDC.put(MDC_KEY_EIDAS_USER_HASH, buildHash(responseAttributes));
            LOG.info("Hash of eIDAS user attributes");
        } finally {
            MDC.remove(MDC_KEY_EIDAS_REQUEST_ID);
            MDC.remove(MDC_KEY_EIDAS_DESTINATION);
            MDC.remove(MDC_KEY_EIDAS_USER_HASH);
        }
    }

    private static String buildHash(HashableResponseAttributes responseAttributes) {
        String attributesString;
        try {
            attributesString = OBJECT_MAPPER.writeValueAsString(responseAttributes);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }

        return Hex.encodeHexString(MESSAGE_DIGEST.digest(attributesString.getBytes(StandardCharsets.UTF_8)));
    }
}