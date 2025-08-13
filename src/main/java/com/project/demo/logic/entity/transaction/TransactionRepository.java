package com.project.demo.logic.entity.transaction;

import com.project.demo.logic.dashboard.*;
import com.project.demo.rest.dashboard.DTO.TopCropYieldDTO;
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
    List<IMonthlySummary> getIncomeVsExpensesSummary(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("farmId") Long farmId);

    @Query("SELECT COALESCE(SUM(t.totalValue), 0.0) FROM Transaction t WHERE t.user.id = :userId AND t.transactionType = 'VENTA' AND t.transactionDate BETWEEN :startDate AND :endDate")
    Double sumTotalIncomeByUserIdAndDateRange(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COALESCE(SUM(t.totalValue), 0.0) FROM Transaction t WHERE t.user.id = :userId AND t.transactionType IN ('COMPRA', 'GASTO_INSUMO') AND t.transactionDate BETWEEN :startDate AND :endDate")
    Double sumTotalExpensesByUserIdAndDateRange(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT new com.project.demo.rest.dashboard.DTO.TopCropYieldDTO(c.cropName, SUM(t.quantity)) " +
            "FROM Transaction t JOIN t.crop c " +
            "WHERE t.user.id = :userId AND t.transactionType = 'VENTA' " +
            "GROUP BY c.id, c.cropName " +
            "ORDER BY SUM(t.quantity) DESC " +
            "LIMIT 5")
    List<TopCropYieldDTO> findTop5CropYieldsByUserId(@Param("userId") Long userId);

    /**
     * Obtiene un resumen de rendimiento por parcela usando una consulta SQL nativa.
     * Esta versión une las transacciones con los registros de manejo de cultivos para
     * determinar en qué parcela se produjo cada venta.
     */
    @Query(value = "SELECT " +
            "    fp.plot_name AS plotName, " +
            "    c.crop_name AS cropName, " +
            "    SUM(t.quantity) AS totalQuantitySold, " +
            "    t.measure_unit AS measureUnit " +
            "FROM transactions t " +
            "JOIN crops c ON t.crop_id = c.id " +
            "JOIN crops_management cm ON c.id = cm.id_crop AND t.farm_id = cm.id_farm " +
            "JOIN farm_plots fp ON cm.id_farm_plot = fp.id " +
            "WHERE t.user_id = :userId " +
            "  AND t.farm_id = :farmId " +
            "  AND t.transaction_type = 'VENTA' " +
            "  AND t.transaction_date BETWEEN :startDate AND :endDate " +
            "GROUP BY fp.plot_name, c.crop_name, t.measure_unit " +
            "ORDER BY fp.plot_name, c.crop_name",
            nativeQuery = true)
    List<IPlotYield> getPlotYieldSummary(
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

    /**
     * Obtiene un resumen de rendimiento por cultivo usando una consulta SQL nativa.
     */
    @Query(value = "SELECT " +
            "    c.crop_name AS cropName, " +
            "    COALESCE(SUM(CASE WHEN t.transaction_type = 'VENTA' THEN t.quantity ELSE 0 END), 0.0) AS totalQuantitySold, " +
            "    t.measure_unit AS measureUnit, " +
            "    COALESCE(SUM(CASE WHEN t.transaction_type = 'VENTA' THEN t.total_value ELSE 0 END), 0.0) AS totalIncome, " +
            "    COALESCE(SUM(CASE WHEN t.transaction_type IN ('COMPRA', 'GASTO_INSUMO') THEN t.total_value ELSE 0 END), 0.0) AS totalExpenses " +
            "FROM transactions t " +
            "JOIN crops c ON t.crop_id = c.id " +
            "WHERE t.user_id = :userId " +
            "  AND (:farmId IS NULL OR t.farm_id = :farmId) " +
            "  AND (:cropId IS NULL OR t.crop_id = :cropId) " +
            "  AND t.transaction_date BETWEEN :startDate AND :endDate " +
            "GROUP BY c.crop_name, t.measure_unit " +
            "ORDER BY c.crop_name ASC",
            nativeQuery = true)
    List<ICropYield> getCropYieldSummary(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("farmId") Long farmId,
            @Param("cropId") Long cropId);

    /**
     * Obtiene un resumen de costos por cultivo usando una consulta SQL nativa.
     */
    @Query(value = "SELECT " +
            "    c.crop_name AS cropName, " +
            "    COALESCE(SUM(t.total_value), 0.0) AS totalCost " +
            "FROM transactions t " +
            "JOIN crops c ON t.crop_id = c.id " +
            "WHERE t.user_id = :userId " +
            "  AND t.transaction_type IN ('COMPRA', 'GASTO_INSUMO') " +
            "  AND (:farmId IS NULL OR t.farm_id = :farmId) " +
            "  AND t.transaction_date BETWEEN :startDate AND :endDate " +
            "GROUP BY c.crop_name " +
            "ORDER BY c.crop_name ASC",
            nativeQuery = true)
    List<ICropCost> getCropCostSummary(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("farmId") Long farmId);

    /**
     * Obtiene un resumen de costos operativos por mes, combinando datos de transacciones y manejo de cultivos.
     */
    @Query(value = "SELECT month, SUM(cost) AS totalCost FROM (" +
            "    SELECT DATE_FORMAT(t.transaction_date, '%Y-%m') AS month, t.total_value AS cost " +
            "    FROM transactions t " +
            "    WHERE t.user_id = :userId " +
            "      AND t.transaction_type IN ('COMPRA', 'GASTO_INSUMO') " +
            "      AND t.transaction_date BETWEEN :startDate AND :endDate " +
            "      AND (:farmId IS NULL OR t.farm_id = :farmId) " +
            "    UNION ALL " +
            "    SELECT DATE_FORMAT(cm.action_date, '%Y-%m') AS month, cm.value_spent AS cost " +
            "    FROM crops_management cm " +
            "    JOIN user_x_farm uxf ON cm.id_farm = uxf.farm_id " +
            "    WHERE uxf.user_id = :userId " +
            "      AND cm.value_spent IS NOT NULL AND cm.value_spent > 0 " +
            "      AND cm.action_date BETWEEN :startDate AND :endDate " +
            "      AND (:farmId IS NULL OR cm.id_farm = :farmId) " +
            ") AS all_costs " +
            "GROUP BY month " +
            "ORDER BY month ASC",
            nativeQuery = true)
    List<IOperationalCost> getOperationalCostSummary(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("farmId") Long farmId);

    /**
     * Obtiene un resumen de costos por finca, combinando datos de transacciones y manejo de cultivos.
     */
    @Query(value = "SELECT f.farm_name AS farmName, SUM(cost) AS totalCost FROM (" +
            "    SELECT t.farm_id, t.total_value AS cost " +
            "    FROM transactions t " +
            "    WHERE t.user_id = :userId " +
            "      AND t.transaction_type IN ('COMPRA', 'GASTO_INSUMO') " +
            "      AND t.transaction_date BETWEEN :startDate AND :endDate " +
            "    UNION ALL " +
            "    SELECT cm.id_farm, cm.value_spent AS cost " +
            "    FROM crops_management cm " +
            "    JOIN user_x_farm uxf ON cm.id_farm = uxf.farm_id " +
            "    WHERE uxf.user_id = :userId " +
            "      AND cm.value_spent IS NOT NULL AND cm.value_spent > 0 " +
            "      AND cm.action_date BETWEEN :startDate AND :endDate " +
            ") AS all_costs " +
            "JOIN farms f ON all_costs.farm_id = f.id " +
            "GROUP BY f.id, f.farm_name " +
            "ORDER BY f.farm_name ASC",
            nativeQuery = true)
    List<IFarmCost> getFarmCostSummary(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}