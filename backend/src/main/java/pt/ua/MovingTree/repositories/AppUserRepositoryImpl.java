package pt.ua.MovingTree.repositories;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.util.Objects;
import org.springframework.stereotype.Repository;
import pt.ua.MovingTree.entities.AppUser;

@Repository
@Transactional(Transactional.TxType.SUPPORTS)
public class AppUserRepositoryImpl implements AppUserRepositoryCustom {
    private final EntityManager entityManager;

    public AppUserRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public AppUser save(Long municipalityId, AppUser user) {
        Objects.requireNonNull(municipalityId, "municipalityId");
        Objects.requireNonNull(user, "user");
        if (user.getMunicipality() == null || !municipalityId.equals(user.getMunicipality().getId())) {
            throw new IllegalArgumentException("user municipality does not match the repository municipality");
        }
        if (user.getTeam() == null || user.getTeam().getMunicipality() == null
                || !user.getMunicipality().getId().equals(user.getTeam().getMunicipality().getId())) {
            throw new IllegalArgumentException("user team does not belong to the user municipality");
        }
        return entityManager.merge(user);
    }
}
