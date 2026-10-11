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
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Table(name = "wallets", uniqueConstraints = {
        @UniqueConstraint(name = "uq_wallets_chain_address", columnNames = {"chain_id", "address"}),
        @UniqueConstraint(name = "uq_wallets_id_user", columnNames = {"id", "user_id"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Wallet {

    // Persistent fields

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @NotNull
    @Valid
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Setter
    @NotNull
    @Min(1)
    @Column(name = "chain_id", nullable = false)
    private Long chainId;

    @Setter
    @NotBlank
    @Pattern(regexp = "^0x[0-9a-f]{40}$")
    @Column(name = "address", nullable = false, length = 42)
    private String address;

    // Constructors

    public Wallet(User user, Long chainId, String address) {
        this.user = user;
        this.chainId = chainId;
        this.address = address;
    }
}
