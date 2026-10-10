package gal.usc.etse.inbest.model;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "wallet_transactions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WalletTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Valid
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "type", nullable = false, columnDefinition = "wallet_transaction_type")
    private WalletTransactionType type;

    @NotNull
    @DecimalMin(value = "0", inclusive = false)
    @Digits(integer = 12, fraction = 8)
    @Column(name = "amount", nullable = false, precision = 20, scale = 8)
    private BigDecimal amount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public WalletTransaction(Wallet wallet, WalletTransactionType type, BigDecimal amount) {
        this.wallet = wallet;
        this.type = type;
        this.amount = amount;
    }

    @PrePersist
    private void onCreate() {
        this.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
    }
}
