package pt.ua.MovingTree.security;

import pt.ua.MovingTree.enums.AppUserRole;

public interface CallerContext {
    Long municipalityId();
    AppUserRole role();
}
