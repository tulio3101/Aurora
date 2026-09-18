package edu.eci.aurora.repository;

import edu.eci.aurora.model.entity.Ticket;
import edu.eci.aurora.model.entity.enums.Category;
import edu.eci.aurora.model.entity.enums.Severity;
import edu.eci.aurora.model.entity.enums.Source;
import edu.eci.aurora.model.entity.enums.Team;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;

public final class TicketSpecifications {

    private TicketSpecifications() {
    }

    public static Specification<Ticket> hasCategory(Category category) {
        return (root, query, cb) -> category == null ? null : cb.equal(root.get("category"), category);
    }

    public static Specification<Ticket> hasSeverity(Severity severity) {
        return (root, query, cb) -> severity == null ? null : cb.equal(root.get("severity"), severity);
    }

    public static Specification<Ticket> hasTeam(Team team) {
        return (root, query, cb) -> team == null ? null : cb.equal(root.get("team"), team);
    }

    public static Specification<Ticket> hasSource(Source source) {
        return (root, query, cb) -> source == null ? null : cb.equal(root.get("source"), source);
    }

    public static Specification<Ticket> receivedFrom(Instant from) {
        return (root, query, cb) -> from == null ? null : cb.greaterThanOrEqualTo(root.get("receivedAt"), from);
    }

    public static Specification<Ticket> receivedTo(Instant to) {
        return (root, query, cb) -> to == null ? null : cb.lessThanOrEqualTo(root.get("receivedAt"), to);
    }
}
