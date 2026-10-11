package gal.usc.etse.inbest.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigInteger;

@Entity
@Getter
@Table(name = "positions", uniqueConstraints = {
        @UniqueConstraint(name = "uq_positions_wallet_asset", columnNames = {"wallet_id", "asset_id"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Position {

    // Persistent fields

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @NotNull
    @Valid
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @Setter
    @NotNull
    @Valid
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @Setter
    @NotNull
    @Min(0)
    @Digits(integer = 78, fraction = 0)
    @Column(name = "available_quantity", nullable = false, precision = 78, scale = 0)
    private BigInteger availableQuantity = BigInteger.ZERO;

    @Setter
    @NotNull
    @Min(0)
    @Digits(integer = 78, fraction = 0)
    @Column(name = "reserved_quantity", nullable = false, precision = 78, scale = 0)
    private BigInteger reservedQuantity = BigInteger.ZERO;

    // Constructors

    public Position(Wallet wallet, Asset asset) {
        this.wallet = wallet;
        this.asset = asset;
    }
}
