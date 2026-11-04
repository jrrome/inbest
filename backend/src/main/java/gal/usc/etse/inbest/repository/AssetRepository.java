package gal.usc.etse.inbest.repository;

import gal.usc.etse.inbest.model.entity.Asset;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface AssetRepository extends JpaRepository<Asset, Long> {
    Optional<Asset> findById(Long id, Pageable pageable);

    Optional<Asset> findByName(String name, Pageable pageable);

    Page<Asset> findAllByCreatedAtBetween(
            OffsetDateTime createdAtAfter,
            OffsetDateTime createdAtBefore,
            Pageable pageable);
}
