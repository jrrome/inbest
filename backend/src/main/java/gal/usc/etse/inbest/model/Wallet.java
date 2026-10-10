package gal.usc.etse.inbest.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

@Entity
@Table(name = "wallets", uniqueConstraints = {
        @UniqueConstraint(name = "uq_wallets_chain_address", columnNames = {"chain_id", "address"})
})
public class Wallet {

    // Persistent fields

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Valid
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @NotNull
    @Min(1)
    @Column(name = "chain_id", nullable = false)
    private Long chainId;

    @NotBlank
    @Pattern(regexp = "^0x[0-9a-f]{40}$")
    @Column(name = "address", nullable = false, length = 42)
    private String address;

    @NotNull
    @DecimalMin("0")
    @Digits(integer = 12, fraction = 8)
    @Column(name = "available_cash", nullable = false, precision = 20, scale = 8)
    private BigDecimal availableCash = BigDecimal.ZERO;

    @NotNull
    @DecimalMin("0")
    @Digits(integer = 12, fraction = 8)
    @Column(name = "reserved_cash", nullable = false, precision = 20, scale = 8)
    private BigDecimal reservedCash = BigDecimal.ZERO;

    // Constructors

    protected Wallet() {
    }

    public Wallet(User user, Long chainId, String address) {
        this.user = user;
        this.chainId = chainId;
        this.address = address;
    }

    // Accessors

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Long getChainId() {
        return chainId;
    }

    public void setChainId(Long chainId) {
        this.chainId = chainId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public BigDecimal getAvailableCash() {
        return availableCash;
    }

    public void setAvailableCash(BigDecimal availableCash) {
        this.availableCash = availableCash;
    }

    public BigDecimal getReservedCash() {
        return reservedCash;
    }

    public void setReservedCash(BigDecimal reservedCash) {
        this.reservedCash = reservedCash;
    }
}
