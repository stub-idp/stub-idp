package stubidp.dropwizard.logstash;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.net.SyslogOutputStream;
import ch.qos.logback.core.status.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import stubidp.dropwizard.logstash.SyslogAppender;
import stubidp.dropwizard.logstash.SyslogEventFormatter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SyslogAppenderTest {

    private SyslogAppender appender;

    @Mock
    private SyslogEventFormatter syslogEventFormatter;
    @Mock
    private SyslogOutputStream outputStream;

    @BeforeEach
    void setUp() {
        appender = new SyslogAppender(syslogEventFormatter, outputStream);
        appender.start();
    }

    @Test
    void doAppend_shouldConvertEventToLogstashFormat() throws Exception {
        final String syslogMessage = UUID.randomUUID().toString();
        final ILoggingEvent event = mock(ILoggingEvent.class);
        when(syslogEventFormatter.format(event)).thenReturn(syslogMessage);

        appender.append(event);

        verify(outputStream).write(syslogMessage.getBytes());
    }

    @Test
    void doAppend_shouldDoNothingIfAppenderHasNotBeenStarted() throws Exception {
        appender = new SyslogAppender(syslogEventFormatter, outputStream);

        appender.append(mock(ILoggingEvent.class));

        verify(outputStream, never()).write(any());
    }

    @Test
    void doAppend_shouldRecordAnErrorWhenWritingToSyslogFails() throws Exception {
        when(syslogEventFormatter.format(any(ILoggingEvent.class))).thenReturn("");
        final IOException ioError = new IOException();
        doThrow(ioError).when(outputStream).write(any());
        appender.setContext(new ContextBase());

        appender.append(mock(ILoggingEvent.class));

        final List<Status> statusList = appender.getStatusManager().getCopyOfStatusList();
        assertThat(statusList.size()).isEqualTo(1);
        assertThat(statusList.get(0).getLevel()).isEqualTo(Status.ERROR);
        assertThat(statusList.get(0).getThrowable()).isEqualTo(ioError);
    }
}
