package com.project.demo.logic.entity.transaction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Page<Transaction> findByUserId(Long userId, Pageable pageable);
    List<Transaction> findByFarmId(Long farmId);

    Optional<Transaction> findByIdAndUserId(Long id, Long userId);

    /**
     * Encuentra todas las transacciones para un usuario específico con paginación.
     * Utiliza JOIN FETCH para cargar de forma proactiva las entidades Farm y Crop relacionadas,
     * evitando así problemas de N+1 y errores de carga perezosa (lazy loading).
     *
     * @param userId El ID del usuario.
     * @param pageable La información de paginación.
     * @return Una página de transacciones con sus fincas y cultivos asociados.
     */
    @Query("SELECT t FROM Transaction t LEFT JOIN FETCH t.farm LEFT JOIN FETCH t.crop WHERE t.user.id = :userId AND t.isActive = true")
    Page<Transaction> findByUserIdWithDetails(@Param("userId") Long userId, Pageable pageable);
}