package gal.usc.etse.inbest.model;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "trades")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Valid
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "buy_order_id", nullable = false)
    private Order buyOrder;

    @NotNull
    @Valid
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sell_order_id", nullable = false)
    private Order sellOrder;

    @NotNull
    @Min(1)
    @Digits(integer = 78, fraction = 0)
    @Column(name = "quantity", nullable = false, precision = 78, scale = 0)
    private BigInteger quantity;

    @NotNull
    @DecimalMin(value = "0", inclusive = false)
    @Digits(integer = 12, fraction = 8)
    @Column(name = "price", nullable = false, precision = 20, scale = 8)
    private BigDecimal price;

    @Column(name = "executed_at", nullable = false, updatable = false)
    private OffsetDateTime executedAt;

    public Trade(Order buyOrder, Order sellOrder, BigInteger quantity, BigDecimal price) {
        this.buyOrder = buyOrder;
        this.sellOrder = sellOrder;
        this.quantity = quantity;
        this.price = price;
    }

    @PrePersist
    private void onCreate() {
        this.executedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }
}
