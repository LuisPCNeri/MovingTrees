package pt.ua.MovingTree.controllers;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.ua.MovingTree.dtos.TeamResponse;
import pt.ua.MovingTree.services.TeamQueryService;

@RestController
@RequestMapping("/teams")
public class TeamsController {
    private final TeamQueryService queryService;

    public TeamsController(TeamQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping
    public List<TeamResponse> list() {
        return queryService.list();
    }
}
