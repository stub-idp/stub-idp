package stubidp.saml.hub.transformers.inbound;

import org.opensaml.saml.saml2.core.Status;
import org.opensaml.saml.saml2.core.StatusCode;
import stubidp.saml.domain.IdaStatus;
import stubidp.saml.extensions.domain.SamlStatusCode;

import java.util.Map;

import static java.text.MessageFormat.format;

public abstract class IdaStatusUnmarshaller<T extends IdaStatus> {

    private final Map<IdaStatusMapperStatus, T> statusMap;

    public IdaStatusUnmarshaller(Map<IdaStatusMapperStatus, T> statusMap) {
        this.statusMap = statusMap;
    }

    public T fromSaml(Status status) {
        IdaStatusMapperStatus statusMapperStatus = getStatusMapperStatus(status);

        if (!statusMap.containsKey(statusMapperStatus)) {
            throw new IllegalArgumentException(format("{0} is not valid in this context", statusMapperStatus));
        }

        String message = null;
        if (status.getStatusMessage() != null) {
            message = status.getStatusMessage().getValue();
        }
        return transformStatus(statusMapperStatus, message);
    }

    protected T transformStatus(IdaStatusMapperStatus statusMapperStatus, String message) {
        return statusMap.get(statusMapperStatus);
    }

    private IdaStatusMapperStatus getStatusMapperStatus(Status status) {
        StatusCode topLevelStatusCode = status.getStatusCode();

        switch (topLevelStatusCode.getValue()) {
            case StatusCode.SUCCESS:
                return getSubStatusForTopLevelSuccessStatus(topLevelStatusCode);
            case StatusCode.RESPONDER:
                return getResponderStatus(topLevelStatusCode);
            case StatusCode.REQUESTER:
                return IdaStatusMapperStatus.RequesterError;
        }
        throw new IllegalArgumentException(format("Unrecognised top-level status code: {0}", topLevelStatusCode.getValue()));
    }

    private IdaStatusMapperStatus getResponderStatus(StatusCode topLevelStatusCode) {
        StatusCode subStatusCode = topLevelStatusCode.getStatusCode();
        return switch (subStatusCode.getValue()) {
            case StatusCode.AUTHN_FAILED -> IdaStatusMapperStatus.AuthenticationFailed;
            case SamlStatusCode.NO_MATCH -> IdaStatusMapperStatus.NoMatchingServiceMatchFromMatchingService;
            case StatusCode.NO_AUTHN_CONTEXT -> IdaStatusMapperStatus.NoAuthenticationContext;
            case StatusCode.REQUESTER -> IdaStatusMapperStatus.RequesterErrorFromIdpAsSentByHub;
            case SamlStatusCode.CREATE_FAILURE -> IdaStatusMapperStatus.CreateFailed;
            default -> throw new IllegalArgumentException(format("{0} - Unrecognised sub-status code.", topLevelStatusCode.getValue()));
        };
    }

    private IdaStatusMapperStatus getSubStatusForTopLevelSuccessStatus(StatusCode topLevelStatusCode) {
        StatusCode subStatusCode = topLevelStatusCode.getStatusCode();

        if (subStatusCode != null) {
            if (SamlStatusCode.MATCH.equals(subStatusCode.getValue())) {
                return IdaStatusMapperStatus.MatchingServiceMatch;
            }
            if (SamlStatusCode.NO_MATCH.equals(subStatusCode.getValue())) {
                return IdaStatusMapperStatus.NoMatchingServiceMatchFromHub;
            }
            if (SamlStatusCode.HEALTHY.equals(subStatusCode.getValue())) {
                return IdaStatusMapperStatus.Healthy;
            }
            if (SamlStatusCode.CREATED.equals(subStatusCode.getValue())) {
                return IdaStatusMapperStatus.Created;
            }
        }

        return IdaStatusMapperStatus.Success;
    }

    protected enum IdaStatusMapperStatus {
        Success,
        NoAuthenticationContext,
        RequesterError,
        RequesterErrorFromIdpAsSentByHub,
        AuthenticationFailed,
        NoMatchingServiceMatchFromHub,
        NoMatchingServiceMatchFromMatchingService,
        MatchingServiceMatch,
        Healthy,
        Created,
        CreateFailed
        }
}
