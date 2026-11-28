package pt.ua.MovingTree.dtos;

import pt.ua.MovingTree.entities.Team;

public record TeamResponse(Long id, String name) {
    public static TeamResponse from(Team team) {
        return new TeamResponse(team.getId(), team.getName());
    }
}
