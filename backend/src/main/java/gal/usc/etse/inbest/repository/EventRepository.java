package gal.usc.etse.inbest.repository;

import gal.usc.etse.inbest.model.entity.Event;
import gal.usc.etse.inbest.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;

public interface EventRepository extends JpaRepository<Event, Long> {
    // De momento usamos pageable, pero en este caso podría ser útil el uso de cursores
    Page<Event> findAllByActorUser(User actorUser, Pageable pageable);

    Page<Event> findAllByOccurredAtBetween(
            OffsetDateTime occurredAtAfter,
            OffsetDateTime occurredAtBefore,
            Pageable pageable);

}
