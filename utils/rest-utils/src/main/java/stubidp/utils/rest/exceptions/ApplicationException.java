package stubidp.utils.rest.exceptions;

import stubidp.utils.rest.common.ErrorStatusDto;
import stubidp.utils.rest.common.ExceptionType;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

import static java.text.MessageFormat.format;

public final class ApplicationException extends RuntimeException {

    private static final String ERROR_MESSAGE_FORMAT = "{0}\nClient Message: {1}";
    private final UUID errorId;
    private final ExceptionType exceptionType;
    private final boolean audited;
    private final Optional<URI> uri;
    private final Optional<String> clientMessage;

    private ApplicationException(
            ExceptionType exceptionType,
            boolean audited,
            UUID errorId) {

        this(exceptionType, audited, errorId, null, Optional.empty(), Optional.empty());
    }

    private ApplicationException(
            ExceptionType exceptionType,
            boolean audited,
            UUID errorId,
            Throwable cause) {

        this(exceptionType, audited, errorId, cause, Optional.empty(), Optional.empty());
    }

    private ApplicationException(
            ExceptionType exceptionType,
            boolean audited,
            UUID errorId,
            Throwable cause,
            Optional<URI> uri,
            Optional<String> clientMessage) {

        super(format("Exception of type [{0}] {1}", exceptionType, getUriErrorMessage(uri)), cause);

        this.exceptionType = exceptionType;
        this.errorId = errorId;
        this.audited = audited;
        this.uri = uri;
        this.clientMessage = clientMessage;
    }

    @Override
    public String getMessage() {
        final String message = super.getMessage();
        return clientMessage.map(s -> format(ERROR_MESSAGE_FORMAT, message, s)).orElse(message);
    }

    private ApplicationException(ErrorStatusDto errorStatus, URI uri) {
        this(
                errorStatus.getExceptionType(),
                errorStatus.isAudited(),
                errorStatus.getErrorId(),
                null,
                Optional.ofNullable(uri),
                Optional.ofNullable(errorStatus.getClientMessage())
        );
    }

    public static ApplicationException createUnauditedException(ExceptionType exceptionType, UUID errorId) {
        return new ApplicationException(exceptionType, false, errorId);
    }

    public static ApplicationException createUnauditedException(ExceptionType exceptionType, String message, Throwable cause) {
        return new ApplicationException(exceptionType, false, UUID.randomUUID(), cause, Optional.empty(), Optional.of(message));
    }

    public static ApplicationException createUnauditedException(ExceptionType exceptionType, UUID errorId, Throwable cause, URI uri) {
        return new ApplicationException(exceptionType, false, errorId, cause, Optional.ofNullable(uri), Optional.empty());
    }

    public static ApplicationException createUnauditedException(ExceptionType exceptionType, UUID errorId, Throwable cause) {
        return new ApplicationException(exceptionType, false, errorId, cause);
    }

    public static ApplicationException createAuditedException(ExceptionType exceptionType, UUID errorId) {
        return new ApplicationException(exceptionType, true, errorId);
    }

    public static ApplicationException createUnauditedException(ExceptionType exceptionType, UUID errorId, URI uri) {
        return new ApplicationException(exceptionType, false, errorId, null, Optional.ofNullable(uri), Optional.empty());
    }

    public static ApplicationException createExceptionFromErrorStatusDto(ErrorStatusDto errorStatusDto, final URI uri) {
        return new ApplicationException(errorStatusDto, uri);
    }

    private static String getUriErrorMessage(Optional<URI> uri) {
        return uri.map(value -> format("whilst contacting uri: {0}", value)).orElse("");
    }

    public UUID getErrorId() {
        return errorId;
    }

    public ExceptionType getExceptionType() {
        return exceptionType;
    }

    public boolean isAudited() {
        return audited;
    }

    public Optional<URI> getUri() {
        return uri;
    }

    public boolean requiresAuditing() {
        return exceptionType != ExceptionType.NETWORK_ERROR;
    }
}
