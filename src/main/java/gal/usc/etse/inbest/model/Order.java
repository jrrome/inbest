package gal.usc.etse.inbest.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "side", nullable = false,
            columnDefinition = "order_side") // Tipo SQL
    private OrderSide side;

    @Column(name = "quantity", nullable = false,
            precision = 20, scale = 8)
    private BigDecimal quantity;

    @Setter
    @Column(name = "filled_quantity", nullable = false,
            precision = 20, scale = 8)
    private BigDecimal filledQuantity = BigDecimal.ZERO;

    @Column(name = "price", nullable = false,
            precision = 20, scale = 8)
    private BigDecimal price;

    @Setter
    @Column(name = "status", nullable = false, length = 20)
    private String status = "PENDING";

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public Order(User user, Asset asset, OrderSide side,
                 BigDecimal quantity, BigDecimal price) {
        this.user = user;
        this.asset = asset;
        this.side = side;
        this.quantity = quantity;
        this.price = price;
    }

    @PrePersist
    private void onCreate() {
        this.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
    }
}
