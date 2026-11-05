package gal.usc.etse.inbest.repository;

import gal.usc.etse.inbest.model.entity.Token;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {
    Optional<Token> findByName(String name);
    Optional<Token> findBySymbol(String symbol);

    Page<Token> findAllByChainId(Long chainId, Pageable pageable);

    Page<Token> findAllByCreatedAtBetween(
            OffsetDateTime createdAtAfter,
            OffsetDateTime createdAtBefore,
            Pageable pageable);
}
