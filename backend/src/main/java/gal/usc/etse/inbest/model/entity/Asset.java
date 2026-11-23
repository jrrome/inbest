package gal.usc.etse.inbest.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

// El nombre y el símbolo se leen del token asociado
@Getter
@Entity
@Table(name = "assets")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Asset {

    // Persistent fields

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Valid
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "token_id", nullable = false, unique = true, updatable = false)
    private Token token;

    // Precio de referencia alrededor del cual la plataforma coloca sus órdenes
    @Setter
    @NotNull
    @DecimalMin(value = "0", inclusive = false)
    @Digits(integer = 12, fraction = 8)
    @Column(name = "price", nullable = false, precision = 20, scale = 8)
    private BigDecimal price;

    // En unidades mínimas del token
    @NotNull
    @Min(1)
    @Digits(integer = 78, fraction = 0)
    @Column(name = "initial_supply", nullable = false, updatable = false, precision = 78, scale = 0)
    private BigInteger initialSupply;

    // Diferencia total entre el precio de venta y el de compra de la plataforma, como fracción del precio
    @Setter
    @NotNull
    @DecimalMin(value = "0", inclusive = false)
    @DecimalMax(value = "1", inclusive = false)
    @Digits(integer = 2, fraction = 4)
    @Column(name = "spread", nullable = false, precision = 6, scale = 4)
    private BigDecimal spread;

    // Cuánto se mueve el precio de referencia por cada fracción de la emisión que compra o vende la plataforma
    @Setter
    @NotNull
    @DecimalMin(value = "0")
    @Digits(integer = 2, fraction = 4)
    @Column(name = "price_impact", nullable = false, precision = 6, scale = 4)
    private BigDecimal priceImpact;

    // Cantidad máxima de cada orden de la plataforma, en unidades mínimas del token
    @Setter
    @NotNull
    @Min(1)
    @Digits(integer = 78, fraction = 0)
    @Column(name = "max_quote_size", nullable = false, precision = 78, scale = 0)
    private BigInteger maxQuoteSize;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    // Constructors

    public Asset(Token token, BigDecimal price, BigInteger initialSupply, BigDecimal spread,
                 BigDecimal priceImpact, BigInteger maxQuoteSize) {
        this.token = token;
        this.price = price;
        this.initialSupply = initialSupply;
        this.spread = spread;
        this.priceImpact = priceImpact;
        this.maxQuoteSize = maxQuoteSize;
    }

    // Persistence lifecycle
    @PrePersist
    private void onCreate() {
        this.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
    }
}
