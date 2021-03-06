package stubidp.eidas.trustanchor;

import certificates.values.CACertificates;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.shaded.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import stubidp.test.devpki.TestCertificateStrings;
import stubidp.utils.security.security.PrivateKeyFactory;
import stubidp.utils.security.security.X509CertificateFactory;

import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.apache.commons.codec.binary.Base64.encodeInteger;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class GeneratorTest {

    private Generator generator;
    private RSAPublicKey publicKeyForSigning;
    private X509Certificate certificateForSigning;

    @BeforeEach
    void setUp() {
        PrivateKey privateKeyForSigning = new PrivateKeyFactory().createPrivateKey(Base64.getMimeDecoder().decode(TestCertificateStrings.METADATA_SIGNING_A_PRIVATE_KEY));
        certificateForSigning = new X509CertificateFactory().createCertificate(TestCertificateStrings.METADATA_SIGNING_A_PUBLIC_CERT);
        generator = new Generator(privateKeyForSigning, certificateForSigning);
        publicKeyForSigning = (RSAPublicKey) certificateForSigning.getPublicKey();
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldHandleZeroInput() throws ParseException, JOSEException, CertificateEncodingException {
        List<String> files = new ArrayList<>();

        JWSObject output = generator.generate(files);

        assertSigned(output, publicKeyForSigning);
        assertThat(output.getPayload().toJSONObject().containsKey("keys")).isTrue();
        assertThat(((List<Object>)output.getPayload().toJSONObject().get("keys"))).isEmpty();
    }

    @Test
    @SuppressWarnings({"unchecked", "cast"})
    void shouldHandleOneString() throws JOSEException, CertificateEncodingException {
        String countryPublicCert = CACertificates.TEST_ROOT_CA;
        X509Certificate countryCertificate = new X509CertificateFactory().createCertificate(countryPublicCert);
        HashMap<String, List<X509Certificate>> trustAnchorMap = new HashMap<>();
        trustAnchorMap.put("https://generator.test", List.of(countryCertificate));
        JWSObject output = generator.generateFromMap(trustAnchorMap);

        assertSigned(output, publicKeyForSigning);
        assertThat(output.getPayload().toJSONObject()).containsKey("keys");

        List<Map<String, String>> keys = (List<Map<String, String>>)output.getPayload().toJSONObject().get("keys");
        assertThat(keys.size()).isEqualTo(1);
        assertThat(keys.get(0).get("kid")).isEqualTo("https://generator.test");
        assertThat(output.getHeader().getX509CertChain().get(0).decode()).containsExactlyInAnyOrder(certificateForSigning.getEncoded());
    }

    @Test
    @SuppressWarnings({"unchecked", "cast"})
    void shouldHandleChainOfCertificates() throws CertificateEncodingException, JOSEException {
        List<String> certificates = asList(
            CACertificates.TEST_ROOT_CA,
            CACertificates.TEST_IDP_CA,
            TestCertificateStrings.STUB_COUNTRY_PUBLIC_PRIMARY_CERT
        );
        X509CertificateFactory factory = new X509CertificateFactory();
        List<X509Certificate> certs = certificates.stream().map(factory::createCertificate).collect(Collectors.toList());
        HashMap<String, List<X509Certificate>> trustAnchorMap = new HashMap<>();
        trustAnchorMap.put("https://generator.test", certs);
        JWSObject output = generator.generateFromMap(trustAnchorMap);

        assertSigned(output, publicKeyForSigning);
        assertThat(output.getPayload().toJSONObject()).containsKey("keys");

        List<Map<String, String>> keys = (List<Map<String, String>>)output.getPayload().toJSONObject().get("keys");
        assertThat(keys.size()).isEqualTo(1);
        assertThat(keys.get(0).get("kid")).isEqualTo("https://generator.test");
    }

    @Test
    void shouldThrowOnMissingValue() {
        List<String> valueList = asList("kty", "key_ops", "kid", "alg", "e", "n", "x5c");

        for (String attribute : valueList) {
            JSONObject invalid = createJsonObject();
            invalid.remove(attribute);
            final Throwable throwable = catchThrowable(() -> generator.generate(Collections.singletonList(invalid.toJSONString())));
            assertThat(throwable).isInstanceOfAny(IllegalArgumentException.class, ParseException.class);
        }
    }

    @Test
    void shouldThrowOnIncorrectValue() {
        final Map<String, Object> incorrectValues = new HashMap<>(2);
        incorrectValues.put("alg","A128KW");
        incorrectValues.put("kty", "oct");

        for (final Map.Entry<String, Object> entry : incorrectValues.entrySet()) {
            JSONObject jsonObject = createJsonObject();
            jsonObject.replace(entry.getKey(), entry.getValue());
            final Throwable throwable = catchThrowable(() -> generator.generate(Collections.singletonList(jsonObject.toJSONString())));
            assertThat(throwable).isInstanceOfAny(IllegalArgumentException.class, ParseException.class);
        }
    }

    @Test
    void shouldThrowOnIncorrectKeyopsValues() {
        List<Object> incorrectValues = asList(Collections.emptyList(), Collections.singletonList("sign"), asList("verify", "sign"), "verify");

        for (Object attribute: incorrectValues) {
            JSONObject jsonObject = createJsonObject();
            jsonObject.replace("key_ops", attribute);
            assertThrows(ParseException.class, () -> generator.generate(Collections.singletonList(jsonObject.toJSONString())));
        }
    }

    @Test
    void shouldThrowWhenCertificateDoesNotMatchKeyParameters() {
        JSONObject jsonObject = createJsonObject();
        jsonObject.replace("x5c", Collections.singletonList(TestCertificateStrings.TEST_PUBLIC_CERT));

        assertThrows(ParseException.class, () -> generator.generate(Collections.singletonList(jsonObject.toJSONString())));
    }

    @Test
    void shouldCheckAllX509Certificates(){
        JSONObject jsonObject = createJsonObject();
        jsonObject.replace("x5c", asList(TestCertificateStrings.UNCHAINED_PUBLIC_CERT, TestCertificateStrings.TEST_PUBLIC_CERT));

        assertThrows(ParseException.class, () -> generator.generate(Collections.singletonList(jsonObject.toJSONString())));
    }

    @Test
    void shouldThrowWhenDateOfX509CertificateIsInvalid(){

        String expiredCert = TestCertificateStrings.STUB_COUNTRY_PUBLIC_EXPIRED_CERT;

        X509Certificate x509Certificate = new X509CertificateFactory().createCertificate(expiredCert);
        RSAPublicKey rsaPublicKey = (RSAPublicKey) x509Certificate.getPublicKey();

        JSONObject jsonObject = createJsonObject();
        jsonObject.replace("x5c", Collections.singletonList(expiredCert));
        jsonObject.replace("e", new String(encodeInteger(rsaPublicKey.getPublicExponent())));
        jsonObject.replace("n", new String(encodeInteger(rsaPublicKey.getModulus())));

        assertThatExceptionOfType(ParseException.class)
                .isThrownBy(() -> generator.generate(Collections.singletonList(jsonObject.toJSONString())))
                .withMessageContaining("X.509 certificate has expired");
    }

    @Test
    void shouldThrowOnOneInvalidKey() {
        List<String> files = new ArrayList<>();
        files.add(createJsonObject("https://1.generator.test").toJSONString());
        JSONObject invalidObject = createJsonObject("https://2.generator.test");
        invalidObject.remove("kid");
        files.add(invalidObject.toJSONString());

        assertThrows(ParseException.class, () -> generator.generate(files));
    }

    @Test
    @SuppressWarnings({"unchecked", "cast"})
    void shouldHandleMultipleStrings() throws ParseException, JOSEException, CertificateEncodingException {
        List<String> files = new ArrayList<>();
        for (int i = 0; i < 1024; i++) {
            files.add(createJsonObject(String.format("https://%d.generator.test", i)).toJSONString());
        }
        JWSObject output = generator.generate(files);

        assertSigned(output, publicKeyForSigning);
        assertThat(output.getPayload().toJSONObject()).containsKey("keys");

        List<Map<String, String>> keys = (List<Map<String, String>>)output.getPayload().toJSONObject().get("keys");
        Set<String> kidSet = keys.stream()
                .map(x -> x.get("kid"))
                .collect(Collectors.toSet());

        assertThat(kidSet.size()).isEqualTo(1024);
        for (int i = 0; i < 1024; i++) {
            assertThat(kidSet).contains(String.format("https://%d.generator.test", i));
        }
    }

    private JSONObject createJsonObject() {
        return createJsonObject("https://generator.test");
    }

    private JSONObject createJsonObject(String kid) {
        String countryPublicCert = CACertificates.TEST_ROOT_CA;
        countryPublicCert = countryPublicCert.replace("-----BEGIN CERTIFICATE-----\n", "").replace("\n-----END CERTIFICATE-----", ""
        ).replace("\n", "").trim();
        X509Certificate x509Certificate = new X509CertificateFactory().createCertificate(countryPublicCert);
        RSAPublicKey rsaPublicKey = (RSAPublicKey) x509Certificate.getPublicKey();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("kty", "RSA");
        jsonObject.put("key_ops", Collections.singletonList("verify"));
        jsonObject.put("kid", kid);
        jsonObject.put("alg", "RS256");
        jsonObject.put("e", new String(encodeInteger(rsaPublicKey.getPublicExponent())));
        jsonObject.put("n", new String(encodeInteger(rsaPublicKey.getModulus())));
        jsonObject.put("x5c", Collections.singletonList(countryPublicCert));

        return jsonObject;
    }

    private void assertSigned(JWSObject output, RSAPublicKey signedKey) throws JOSEException {
        assertThat(output.getState()).isEqualTo(JWSObject.State.SIGNED);
        assertThat(output.getSignature()).isNotNull();
        assertThat(output.getSignature().decodeToString()).isNotEmpty();
        assertThat(output.verify(new RSASSAVerifier(signedKey))).isTrue();
    }
}
