package gal.usc.etse.inbest.model;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.math.BigInteger;
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

    @NotNull
    @Valid
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @Valid
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "side", nullable = false,
            columnDefinition = "order_side") // Tipo SQL
    private OrderSide side;

    @NotNull
    @Min(1)
    @Digits(integer = 78, fraction = 0)
    @Column(name = "quantity", nullable = false,
            precision = 78, scale = 0)
    private BigInteger quantity;

    @Setter
    @NotNull
    @Min(0)
    @Digits(integer = 78, fraction = 0)
    @Column(name = "filled_quantity", nullable = false,
            precision = 78, scale = 0)
    private BigInteger filledQuantity = BigInteger.ZERO;

    @NotNull
    @DecimalMin(value = "0", inclusive = false)
    @Digits(integer = 12, fraction = 8)
    @Column(name = "price", nullable = false,
            precision = 20, scale = 8)
    private BigDecimal price;

    @Setter
    @NotBlank
    @Size(max = 20)
    @Column(name = "status", nullable = false, length = 20)
    private String status = "PENDING";

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public Order(User user, Asset asset, OrderSide side,
                 BigInteger quantity, BigDecimal price) {
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
