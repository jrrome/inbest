package gal.usc.etse.inbest.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Getter
@Entity
@Table(name = "tokens", uniqueConstraints = {
        @UniqueConstraint(name = "uq_tokens_chain_address", columnNames = {"chain_id", "contract_address"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @NotNull
    @Min(1)
    @Column(name = "chain_id", nullable = false)
    private Long chainId;

    @Setter
    @NotBlank
    @Pattern(regexp = "^0x[0-9a-f]{40}$")
    @Column(name = "contract_address", nullable = false, length = 42)
    private String contractAddress;

    @Setter
    @NotBlank
    @Size(max = 100)
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Setter
    @NotBlank
    @Size(max = 20)
    @Column(name = "symbol", nullable = false, length = 20)
    private String symbol;

    @Setter
    @NotNull
    @Min(0)
    @Max(255)
    @Column(name = "decimals", nullable = false)
    private Short decimals = 18;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Setter
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    public Token(Long chainId, String contractAddress, String name, String symbol) {
        this.chainId = chainId;
        this.contractAddress = contractAddress;
        this.name = name;
        this.symbol = symbol;
    }

    @PrePersist
    private void onCreate() {
        this.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
    }
}
