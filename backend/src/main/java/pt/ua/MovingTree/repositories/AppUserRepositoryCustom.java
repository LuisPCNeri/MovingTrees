package pt.ua.MovingTree.repositories;

import pt.ua.MovingTree.entities.AppUser;

public interface AppUserRepositoryCustom {
    AppUser save(Long municipalityId, AppUser user);
}
