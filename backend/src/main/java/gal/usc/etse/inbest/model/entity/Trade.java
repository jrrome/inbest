package gal.usc.etse.inbest.model.entity;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "trades", uniqueConstraints = {
        @UniqueConstraint(name = "uq_trades_chain_tx_hash", columnNames = {"chain_id", "tx_hash"})
})
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

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "settlement_status", nullable = false, length = 20)
    private SettlementStatus settlementStatus = SettlementStatus.PENDING;

    @NotNull
    @Min(1)
    @Column(name = "chain_id", nullable = false)
    private Long chainId;

    @Pattern(regexp = "^0x[0-9a-f]{64}$")
    @Column(name = "tx_hash", length = 66)
    private String txHash;

    @Column(name = "confirmed_at")
    private OffsetDateTime confirmedAt;

    @Column(name = "executed_at", nullable = false, updatable = false)
    private OffsetDateTime executedAt;

    public Trade(Order buyOrder, Order sellOrder, BigInteger quantity, BigDecimal price, Long chainId) {
        this.buyOrder = buyOrder;
        this.sellOrder = sellOrder;
        this.quantity = quantity;
        this.price = price;
        this.chainId = chainId;
    }

    @PrePersist
    private void onCreate() {
        this.executedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }
}
