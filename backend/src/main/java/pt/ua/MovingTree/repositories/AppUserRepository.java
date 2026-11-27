package pt.ua.MovingTree.repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.Repository;
import pt.ua.MovingTree.entities.AppUser;

public interface AppUserRepository extends Repository<AppUser, Long>, JpaSpecificationExecutor<AppUser>,
        AppUserRepositoryCustom {
    Optional<AppUser> findByMunicipality_IdAndId(Long municipalityId, Long userId);
    List<AppUser> findAllByMunicipality_Id(Long municipalityId);
    Optional<AppUser> findByMunicipality_IdAndUsername(Long municipalityId, String username);
    boolean existsByUsername(String username);
    Optional<AppUser> findByUsername(String username);
}
