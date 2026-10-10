package gal.usc.etse.inbest.model;

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

import java.math.BigInteger;

@Entity
@Table(name = "positions", uniqueConstraints = {
        @UniqueConstraint(name = "uq_positions_wallet_asset", columnNames = {"wallet_id", "asset_id"})
})
public class Position {

    // Persistent fields

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Valid
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @NotNull
    @Valid
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @NotNull
    @Min(0)
    @Digits(integer = 78, fraction = 0)
    @Column(name = "available_quantity", nullable = false, precision = 78, scale = 0)
    private BigInteger availableQuantity = BigInteger.ZERO;

    @NotNull
    @Min(0)
    @Digits(integer = 78, fraction = 0)
    @Column(name = "reserved_quantity", nullable = false, precision = 78, scale = 0)
    private BigInteger reservedQuantity = BigInteger.ZERO;

    // Constructors

    protected Position() {
    }

    public Position(Wallet wallet, Asset asset) {
        this.wallet = wallet;
        this.asset = asset;
    }

    // Accessors

    public Long getId() {
        return id;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }

    public Asset getAsset() {
        return asset;
    }

    public void setAsset(Asset asset) {
        this.asset = asset;
    }

    public BigInteger getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(BigInteger availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public BigInteger getReservedQuantity() {
        return reservedQuantity;
    }

    public void setReservedQuantity(BigInteger reservedQuantity) {
        this.reservedQuantity = reservedQuantity;
    }
}
