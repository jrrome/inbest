package gal.usc.etse.inbest.model.dto;

import gal.usc.etse.inbest.model.entity.EntityType;
import gal.usc.etse.inbest.model.entity.EventType;

import java.time.OffsetDateTime;
import java.util.Map;

public record Event(
        Long id,
        OffsetDateTime occurredAt,
        EventType eventType,
        Long actorUserId,
        EntityType entityType,
        Long entityId,
        String txHash,
        Long chainId,
        Map<String, Object> details
) {
    public static Event from(gal.usc.etse.inbest.model.entity.Event event) {
        return new Event(
                event.getId(),
                event.getOccurredAt(),
                event.getEventType(),
                event.getActorUser().getId(),
                event.getEntityType(),
                event.getEntityId(),
                event.getTxHash(),
                event.getChainId(),
                event.getDetails()
        );
    }
}
