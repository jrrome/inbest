package gal.usc.etse.inbest.model.entity;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Table(name = "cash_accounts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CashAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Valid
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @NotNull
    @Pattern(regexp = "EUR")
    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "EUR";

    @Setter
    @NotNull
    @DecimalMin("0")
    @Digits(integer = 12, fraction = 8)
    @Column(name = "available_cash", nullable = false, precision = 20, scale = 8)
    private BigDecimal availableCash = BigDecimal.ZERO;

    @Setter
    @NotNull
    @DecimalMin("0")
    @Digits(integer = 12, fraction = 8)
    @Column(name = "reserved_cash", nullable = false, precision = 20, scale = 8)
    private BigDecimal reservedCash = BigDecimal.ZERO;

    public CashAccount(User user) {
        this.user = user;
    }
}
