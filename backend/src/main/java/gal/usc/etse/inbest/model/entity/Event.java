package gal.usc.etse.inbest.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;

// Tabla de solo inserción: ninguna columna se actualiza
@Entity
@Getter
@Table(name = "event_log")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "occurred_at", nullable = false, updatable = false)
    private OffsetDateTime occurredAt;

    @NotBlank
    @Size(max = 64)
    @Column(name = "event_type", nullable = false, updatable = false, length = 64)
    private String eventType;

    // null cuando el hecho lo origina el sistema
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_user_id", updatable = false)
    private User actorUser;

    @NotBlank
    @Size(max = 64)
    @Column(name = "entity_type", nullable = false, updatable = false, length = 64)
    private String entityType;

    @Column(name = "entity_id", updatable = false)
    private Long entityId;

    @Pattern(regexp = "^0x[0-9a-f]{64}$")
    @Column(name = "tx_hash", updatable = false, length = 66)
    private String txHash;

    @Min(1)
    @Column(name = "chain_id", updatable = false)
    private Long chainId;

    @NotNull
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "details", nullable = false, updatable = false)
    private Map<String, Object> details;

    // con tantos campos opcionales del mismo tipo, el builder evita confundir entityId y chainId
    @Builder
    public Event(String eventType, User actorUser, String entityType, Long entityId,
                 String txHash, Long chainId, Map<String, Object> details) {
        this.eventType = eventType;
        this.actorUser = actorUser;
        this.entityType = entityType;
        this.entityId = entityId;
        this.txHash = txHash;
        this.chainId = chainId;
        this.details = details == null ? Map.of() : details;
    }

    @PrePersist
    private void onCreate() {
        this.occurredAt = OffsetDateTime.now(ZoneOffset.UTC);
    }
}
