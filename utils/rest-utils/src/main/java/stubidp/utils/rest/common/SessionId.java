package stubidp.utils.rest.common;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Objects;
import java.util.UUID;

public class SessionId {

    public static final SessionId SESSION_ID_DOES_NOT_EXIST_YET = new SessionId("SESSION-DOES-NOT-EXIST-YET");
    public static final SessionId NO_SESSION_CONTEXT_IN_ERROR = new SessionId("NO-SESSION-CONTEXT-IN-ERROR");

    private String sessionId;

    @SuppressWarnings("unused")
    private SessionId() {
    }

    public static SessionId createNewSessionId() {
        return new SessionId(UUID.randomUUID().toString());
    }

    public SessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getSessionId() {
        return sessionId;
    }

    @Override
    @JsonValue()
    public String toString() {
        return sessionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SessionId otherSessionId = (SessionId) o;

        return Objects.equals(sessionId, otherSessionId.sessionId);
    }

    @Override
    public int hashCode() {
        return sessionId != null ? sessionId.hashCode() : 0;
    }
}
