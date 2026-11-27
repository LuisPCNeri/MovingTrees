package pt.ua.MovingTree.services;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.ua.MovingTree.dtos.TeamResponse;
import pt.ua.MovingTree.repositories.TeamRepository;
import pt.ua.MovingTree.security.CallerGuard;

@Service
@Transactional(readOnly = true)
public class TeamQueryService {
    private final TeamRepository teamRepository;
    private final CallerGuard callerGuard;

    public TeamQueryService(TeamRepository teamRepository, CallerGuard callerGuard) {
        this.teamRepository = teamRepository;
        this.callerGuard = callerGuard;
    }

    public List<TeamResponse> list() {
        Long municipalityId = callerGuard.requireMunicipalAdministrator();
        return teamRepository.findAllByMunicipality_IdOrderByNameAsc(municipalityId).stream()
                .map(TeamResponse::from)
                .toList();
    }
}
