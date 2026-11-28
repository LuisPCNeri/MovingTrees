package pt.ua.MovingTree.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import pt.ua.MovingTree.enums.AppUserRole;

@Component
public class HttpSessionCallerContext implements CallerContext {
    public static final String MUNICIPALITY_ID_ATTRIBUTE = "caller.municipalityId";
    public static final String ROLE_ATTRIBUTE = "caller.role";

    private final HttpServletRequest request;

    public HttpSessionCallerContext(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public Long municipalityId() {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object value = session.getAttribute(MUNICIPALITY_ID_ATTRIBUTE);
        return value instanceof Number number ? number.longValue() : null;
    }

    @Override
    public AppUserRole role() {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object value = session.getAttribute(ROLE_ATTRIBUTE);
        if (value instanceof AppUserRole role) {
            return role;
        }
        if (value instanceof String text) {
            try {
                return AppUserRole.valueOf(text);
            } catch (IllegalArgumentException ignored) {
                return null;
            }
        }
        return null;
    }
}
