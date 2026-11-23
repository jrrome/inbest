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

    // Cifrado en sobre: la clave privada va cifrada con su DEK y la DEK con la clave maestra de versión
    // kekVersion. Los tres son null en la wallet del sistema. Nunca deben salir en un DTO ni en un log
    @Column(name = "encrypted_private_key", updatable = false)
    private String encryptedPrivateKey;

    // Con setter para poder recifrarla al rotar la clave maestra
    @Setter
    @Column(name = "encrypted_dek")
    private String encryptedDek;

    @Setter
    @Min(1)
    @Column(name = "kek_version")
    private Short kekVersion;

    // Constructors

    // Wallet sin clave propia, como la del sistema
    public Wallet(User user, Long chainId, String address) {
        this.user = user;
        this.chainId = chainId;
        this.address = address;
    }

    public Wallet(User user, Long chainId, String address, String encryptedPrivateKey, String encryptedDek,
                  Short kekVersion) {
        this(user, chainId, address);
        this.encryptedPrivateKey = encryptedPrivateKey;
        this.encryptedDek = encryptedDek;
        this.kekVersion = kekVersion;
    }
}
