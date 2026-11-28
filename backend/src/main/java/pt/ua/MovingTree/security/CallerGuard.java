package pt.ua.MovingTree.security;

import org.springframework.stereotype.Component;
import pt.ua.MovingTree.enums.AppUserRole;
import pt.ua.MovingTree.exceptions.ForbiddenOperationException;

@Component
public class CallerGuard {
    private final CallerContext callerContext;

    public CallerGuard(CallerContext callerContext) {
        this.callerContext = callerContext;
    }

    public Long requireMunicipalAdministrator() {
        if (callerContext.role() != AppUserRole.MUNICIPAL_ADMINISTRATOR || callerContext.municipalityId() == null) {
            throw new ForbiddenOperationException("Only municipal administrators can access this operation");
        }
        return callerContext.municipalityId();
    }
}
