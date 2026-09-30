package gal.usc.etse.inbest.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Getter
@Entity
@Table(name = "assets")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Asset {

    // Persistent fields

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(name = "symbol", nullable = false, unique = true, length = 20)
    private String symbol;

    @Setter
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Setter
    @Column(name = "price", nullable = false, precision = 20, scale = 8)
    private BigDecimal price;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    // Constructors
    public Asset(String symbol, String name, BigDecimal price) {
        this.symbol = symbol;
        this.name = name;
        this.price = price;
    }

    // Persistence lifecycle
    @PrePersist
    private void onCreate() {
        this.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
    }
}