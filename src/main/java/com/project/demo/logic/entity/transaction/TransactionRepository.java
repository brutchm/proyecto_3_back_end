package com.project.demo.logic.entity.transaction;

import com.project.demo.logic.dashboard.DailySummaryProjection;
import com.project.demo.logic.dashboard.MonthlySummary;
import com.project.demo.logic.dashboard.MonthlySummaryProjection;
import com.project.demo.rest.dashboard.CropYieldDTO;
import com.project.demo.rest.dashboard.PlotYieldDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Page<Transaction> findByUserId(Long userId, Pageable pageable);
    List<Transaction> findByFarmId(Long farmId);

    Optional<Transaction> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT COALESCE(SUM(t.totalValue), 0.0) FROM Transaction t WHERE t.user.id = :userId AND t.transactionType = 'VENTA'")
    Double sumTotalIncomeByUserId(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(t.totalValue), 0.0) FROM Transaction t WHERE t.user.id = :userId AND t.transactionType IN ('COMPRA', 'GASTO_INSUMO')")
    Double sumTotalExpensesByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.user.id = :userId")
    Long countTransactionsByUserId(@Param("userId") Long userId);

    /**
     * Obtiene un resumen de ingresos y egresos agrupados por mes usando una consulta SQL nativa.
     * Esta consulta es más robusta para funciones específicas de la base de datos como DATE_FORMAT.
     *
     * @return Una lista de proyecciones con los datos del resumen.
     */
    @Query(value = "SELECT " +
            "    DATE_FORMAT(t.transaction_date, '%Y-%m') AS month, " +
            "    COALESCE(SUM(CASE WHEN t.transaction_type = 'VENTA' THEN t.total_value ELSE 0 END), 0.0) AS totalIncome, " +
            "    COALESCE(SUM(CASE WHEN t.transaction_type IN ('COMPRA', 'GASTO_INSUMO') THEN t.total_value ELSE 0 END), 0.0) AS totalExpenses " +
            "FROM transactions t " +
            "WHERE t.user_id = :userId " +
            "  AND (:farmId IS NULL OR t.farm_id = :farmId) " +
            "  AND t.transaction_date BETWEEN :startDate AND :endDate " +
            "GROUP BY month " +
            "ORDER BY month ASC",
            nativeQuery = true)
    List<MonthlySummaryProjection> getIncomeVsExpensesSummary(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("farmId") Long farmId);

    /**
     * Obtiene un resumen diario de ingresos y egresos para un usuario desde una fecha específica.
     * Utiliza una consulta SQL nativa para un rendimiento óptimo con funciones de fecha.
     * @return Una lista de proyecciones con los datos del resumen diario.
     */
    @Query(value = "SELECT " +
            "    CAST(t.transaction_date AS DATE) AS date, " +
            "    COALESCE(SUM(CASE WHEN t.transaction_type = 'VENTA' THEN t.total_value ELSE 0 END), 0.0) AS totalIncome, " +
            "    COALESCE(SUM(CASE WHEN t.transaction_type IN ('COMPRA', 'GASTO_INSUMO') THEN t.total_value ELSE 0 END), 0.0) AS totalExpenses " +
            "FROM transactions t " +
            "WHERE t.user_id = :userId " +
            "  AND t.transaction_date >= :startDate " +
            "GROUP BY date " +
            "ORDER BY date ASC",
            nativeQuery = true)
    List<DailySummaryProjection> getDailyIncomeVsExpensesSummary(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate);

    @Query("SELECT COALESCE(SUM(t.totalValue), 0.0) FROM Transaction t WHERE t.user.id = :userId AND t.transactionType = 'VENTA' AND t.transactionDate BETWEEN :startDate AND :endDate")
    Double sumTotalIncomeByUserIdAndDateRange(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COALESCE(SUM(t.totalValue), 0.0) FROM Transaction t WHERE t.user.id = :userId AND t.transactionType IN ('COMPRA', 'GASTO_INSUMO') AND t.transactionDate BETWEEN :startDate AND :endDate")
    Double sumTotalExpensesByUserIdAndDateRange(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT new com.project.demo.rest.dashboard.CropYieldDTO(c.cropName, SUM(t.quantity)) " +
            "FROM Transaction t JOIN t.crop c " +
            "WHERE t.user.id = :userId AND t.transactionType = 'VENTA' " +
            "GROUP BY c.id, c.cropName " +
            "ORDER BY SUM(t.quantity) DESC " +
            "LIMIT 5")
    List<CropYieldDTO> findTop5CropYieldsByUserId(@Param("userId") Long userId);

    /**
     * Obtiene un resumen de rendimiento por parcela usando una consulta SQL nativa.
     * Es más robusto para uniones complejas que no están directamente mapeadas en las entidades.
     */
    @Query(value = "SELECT " +
            "    fp.plot_name AS plotName, " +
            "    ph.record_name AS cropName, " +
            "    SUM(t.quantity) AS totalQuantitySold, " +
            "    t.measure_unit AS measureUnit " +
            "FROM transactions t " +
            "JOIN crops c ON t.crop_id = c.id " +
            "JOIN plot_history ph ON c.crop_name = ph.record_name AND ph.record_type = 'CROP' " +
            "JOIN farm_plots fp ON ph.plot_id = fp.id " +
            "WHERE t.user_id = :userId " +
            "  AND t.farm_id = :farmId " +
            "  AND t.transaction_type = 'VENTA' " +
            "  AND t.transaction_date BETWEEN :startDate AND :endDate " +
            "GROUP BY fp.plot_name, ph.record_name, t.measure_unit " +
            "ORDER BY fp.plot_name, ph.record_name",
            nativeQuery = true)
    List<PlotYieldDTO> getPlotYieldSummary(
            @Param("userId") Long userId,
            @Param("farmId") Long farmId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

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