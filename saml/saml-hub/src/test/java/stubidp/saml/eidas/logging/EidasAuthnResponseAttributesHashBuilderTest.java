package stubidp.saml.eidas.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static stubidp.saml.eidas.logging.EidasAuthnResponseAttributesHashLogger.MDC_KEY_EIDAS_DESTINATION;
import static stubidp.saml.eidas.logging.EidasAuthnResponseAttributesHashLogger.MDC_KEY_EIDAS_REQUEST_ID;
import static stubidp.saml.eidas.logging.EidasAuthnResponseAttributesHashLogger.MDC_KEY_EIDAS_USER_HASH;

@ExtendWith(MockitoExtension.class)
class EidasAuthnResponseAttributesHashBuilderTest {

    @Mock
    private Appender<ILoggingEvent> appender;

    @Captor
    private ArgumentCaptor<ILoggingEvent> loggingEventArgumentCaptor;

    @Test
    void testAHashCanBeCreatedWithNoInput() {
        final String hash = buildHash(new HashableResponseAttributes());
        assertThat(hash).isNotBlank();
    }

    @Test
    void testDifferentInputProducesDifferentHashes() {
        final LocalDate now = LocalDate.now();

        final HashableResponseAttributes responseAttributes1 = new HashableResponseAttributes();
        responseAttributes1.setRequestId("a");
        responseAttributes1.setFirstName("fn");
        responseAttributes1.addMiddleName("mn");
        responseAttributes1.addSurname("sn");
        responseAttributes1.setDateOfBirth(now);
        final String hash1 = buildHash(responseAttributes1);

        final HashableResponseAttributes responseAttributes2 = new HashableResponseAttributes();
        responseAttributes2.setRequestId("different pid");
        responseAttributes2.setFirstName("fn");
        responseAttributes2.addMiddleName("mn");
        responseAttributes2.addSurname("sn");
        responseAttributes2.setDateOfBirth(now);
        final String hash2 = buildHash(responseAttributes2);

        assertThat(hash1).isNotEqualTo(hash2);
    }

    @Test
    void testSamePidsProduceSameHashes() {
        final LocalDate now = LocalDate.now();

        final HashableResponseAttributes responseAttributes1 = new HashableResponseAttributes();
        responseAttributes1.setRequestId("a");
        responseAttributes1.setFirstName("fn");
        responseAttributes1.addMiddleName("mn");
        responseAttributes1.addSurname("sn");
        responseAttributes1.setDateOfBirth(now);
        final String hash1 = buildHash(responseAttributes1);

        final HashableResponseAttributes responseAttributes2 = new HashableResponseAttributes();
        responseAttributes2.setRequestId("a");
        responseAttributes2.setFirstName("fn");
        responseAttributes2.addMiddleName("mn");
        responseAttributes2.addSurname("sn");
        responseAttributes2.setDateOfBirth(now);
        final String hash2 = buildHash(responseAttributes2);
        assertThat(hash1).isEqualTo(hash2);
    }

    @Test
    void testOrderingOfMiddleNamesAffectsHash() {
        final HashableResponseAttributes responseAttributes1 = new HashableResponseAttributes();
        responseAttributes1.addMiddleName("mn1");
        responseAttributes1.addMiddleName("mn2");
        final String hash1 = buildHash(responseAttributes1);

        final HashableResponseAttributes responseAttributes2 = new HashableResponseAttributes();
        responseAttributes2.addMiddleName("mn1");
        responseAttributes2.addMiddleName("mn2");
        assertThat(hash1).isEqualTo(buildHash(responseAttributes2));

        final HashableResponseAttributes responseAttributes3 = new HashableResponseAttributes();
        responseAttributes3.addMiddleName("mn2");
        responseAttributes3.addMiddleName("mn1");
        assertThat(hash1).isNotEqualTo(buildHash(responseAttributes3));
    }

    @Test
    void testUpdatingFieldsChangesHash() {
        final HashableResponseAttributes responseAttributes = new HashableResponseAttributes();
        responseAttributes.setRequestId("a");
        final String hash1 = buildHash(responseAttributes);

        responseAttributes.setRequestId("b");
        assertThat(hash1).isNotEqualTo(buildHash(responseAttributes));
    }

    @Test
    void testLoggingOfHashAndLevel() {
        final Logger logger = (Logger) LoggerFactory.getLogger(EidasAuthnResponseAttributesHashLogger.class);
        logger.addAppender(appender);

        final HashableResponseAttributes responseAttributes = new HashableResponseAttributes();
        responseAttributes.setRequestId("a");

        final String hash = buildHash(responseAttributes);
        logHash(responseAttributes);

        verify(appender).doAppend(loggingEventArgumentCaptor.capture());
        final ILoggingEvent loggingEvent = loggingEventArgumentCaptor.getValue();

        assertThat(loggingEvent.getLevel()).isEqualTo(Level.INFO);
        assertThat(loggingEvent.getMessage()).doesNotContain("a request id", "a destination", hash);

        final Map<String, String> mdcPropertyMap = loggingEvent.getMDCPropertyMap();
        assertThat(mdcPropertyMap.get(MDC_KEY_EIDAS_REQUEST_ID)).isEqualTo("a request id");
        assertThat(mdcPropertyMap.get(MDC_KEY_EIDAS_DESTINATION)).isEqualTo("a destination");
        assertThat(mdcPropertyMap.get(MDC_KEY_EIDAS_USER_HASH)).isEqualTo(hash);
    }

    private String buildHash(HashableResponseAttributes responseAttributes) {
        try {
            Method method = EidasAuthnResponseAttributesHashLogger.class.getDeclaredMethod("buildHash", HashableResponseAttributes.class);
            method.setAccessible(true);
            return (String) method.invoke(null, responseAttributes);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private void logHash(HashableResponseAttributes responseAttributes) {
        try {
            Method method = EidasAuthnResponseAttributesHashLogger.class.getDeclaredMethod(
                    "logHash", String.class, String.class, HashableResponseAttributes.class);
            method.setAccessible(true);
            method.invoke(null, "a request id", "a destination", responseAttributes);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}