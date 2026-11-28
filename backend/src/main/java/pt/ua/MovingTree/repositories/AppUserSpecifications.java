package pt.ua.MovingTree.repositories;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;
import pt.ua.MovingTree.dtos.UserFilter;
import pt.ua.MovingTree.entities.AppUser;

public final class AppUserSpecifications {
    private AppUserSpecifications() {
    }

    public static Specification<AppUser> forListing(Long municipalityId, UserFilter filter) {
        return (root, query, cb) -> {
            var predicates = new ArrayList<Predicate>();
            predicates.add(cb.equal(root.get("municipality").get("id"), municipalityId));
            if (filter.role() != null) {
                predicates.add(cb.equal(root.get("role"), filter.role()));
            }
            if (filter.teamId() != null) {
                predicates.add(cb.equal(root.get("team").get("id"), filter.teamId()));
            }
            if (filter.status() != null) {
                predicates.add(cb.equal(root.get("status"), filter.status()));
            }
            if (filter.name() != null && !filter.name().isBlank()) {
                String pattern = "%" + filter.name().trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.like(cb.lower(root.get("name")), pattern));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
