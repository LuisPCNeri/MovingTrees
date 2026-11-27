package pt.ua.MovingTree.exceptions;

public class TeamNotInMunicipalityException extends RuntimeException {
    public TeamNotInMunicipalityException(Long teamId, Long municipalityId) {
        super("Team " + teamId + " does not belong to municipality " + municipalityId);
    }
}
