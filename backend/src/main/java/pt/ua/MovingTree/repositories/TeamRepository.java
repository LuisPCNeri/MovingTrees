package pt.ua.MovingTree.repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;
import pt.ua.MovingTree.entities.Team;

public interface TeamRepository extends Repository<Team, Long> {
    Optional<Team> findByMunicipality_IdAndId(Long municipalityId, Long id);
    List<Team> findAllByMunicipality_IdOrderByNameAsc(Long municipalityId);
}
