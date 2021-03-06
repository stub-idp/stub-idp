package stubidp.shared.cookies;

import javax.ws.rs.core.NewCookie;

public class HttpOnlyNewCookie extends NewCookie {

    private static final boolean httpOnly = true;
    private static final boolean sameSite = true;

    public HttpOnlyNewCookie(String name, String value, String path, String comment, int maxAge, boolean secure) {
        super(name, value, path, null, comment, maxAge, secure);
    }

    @Override
    public String toString() {
        return super.toString()
                + (httpOnly?"; HttpOnly":"")
                + (sameSite?"; SameSite=Strict;":";");
    }
}
