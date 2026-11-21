package gal.usc.etse.inbest.service;

import gal.usc.etse.inbest.exception.EventLogException;
import gal.usc.etse.inbest.model.entity.*;
import gal.usc.etse.inbest.repository.EventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@Validated
@Slf4j
public class EventService {
    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {this.eventRepository = eventRepository;}

    // Es posible preferir tener una función por tipo de evento, y reducir el acoplamiento sacrificando más código en
    // el service
    public void logBlockchainEvent(EventType eventType, User actorUser, EntityType entityType, Long entityId,
                                    String txHash, Long chainId, Map<String, Object> details) {
        try {
            log.info("Logging new {} event", eventType);
            Event event = new Event(eventType, actorUser, entityType, entityId, txHash, chainId, details);
            eventRepository.save(event);
            log.info("{} event logged successfully", eventType.toString());
        } catch (Exception e) {
            log.error("Error logging {} event", eventType.toString());
            throw new EventLogException("Error logging " + eventType.toString() + " event", e);
        }
    }

    public void logTokenDeployEvent(User actorUser, Token token, String txHash) {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("name", token.getName());
        details.put("symbol", token.getSymbol());
        details.put("contractAddress", token.getContractAddress());
        details.put("createdAt", token.getCreatedAt());
        details.put("decimals", token.getDecimals());
        details.put("description", token.getDescription());
        logBlockchainEvent(EventType.TOKEN_DEPLOY, actorUser, EntityType.TOKEN, token.getId(), txHash,
                token.getChainId(), details);
    }
}
