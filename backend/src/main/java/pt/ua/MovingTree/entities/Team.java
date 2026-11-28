package pt.ua.MovingTree.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "team", uniqueConstraints = {
    @UniqueConstraint(name = "uq_team_municipality_name", columnNames = {"municipality_id", "name"}),
    @UniqueConstraint(name = "uq_team_id_municipality", columnNames = {"id", "municipality_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "municipality_id", nullable = false, updatable = false)
    private Municipality municipality;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private Instant createdAt;

    public Team(Municipality municipality, String name) {
        this.municipality = municipality;
        this.name = name;
    }
}
