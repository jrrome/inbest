package gal.usc.etse.inbest.repository;

import gal.usc.etse.inbest.model.entity.Asset;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;

public interface AssetRepository extends JpaRepository<Asset, Long> {

    // JpaRepository ya proporciona findById(Long id), que devuelve un Optional.
    // El ID es único, así que no necesita paginación. Además, Spring Data
    // no admite Optional como resultado de una consulta con Pageable.
    // Optional<Asset> findById(Long id, Pageable pageable);

    Page<Asset> findByName(String name, Pageable pageable);

    Page<Asset> findAllByCreatedAtBetween(
            OffsetDateTime createdAtAfter,
            OffsetDateTime createdAtBefore,
            Pageable pageable);
}
