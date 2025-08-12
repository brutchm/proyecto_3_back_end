package com.project.demo.logic.entity.crop;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @interface CropRepository
 * @description
 * Repositorio para la entidad Crop. Proporciona los métodos CRUD básicos
 * y consultas personalizadas para buscar cultivos por usuario.
 */
@Repository
public interface CropRepository extends JpaRepository<Crop, Long> {
    /**
     * Busca una página de cultivos que pertenecen a un usuario específico.
     * @param userId El ID del usuario propietario de los cultivos.
     * @param pageable La información de paginación.
     * @return Una página de cultivos del usuario especificado.
     */
    Page<Crop> findAllByUserId(Long userId, Pageable pageable);

    /**
     * Busca un cultivo específico por su ID y el ID de su usuario propietario.
     * Esto asegura que un usuario no pueda acceder a cultivos de otros.
     * @param id El ID del cultivo.
     * @param userId El ID del usuario propietario.
     * @return Un Optional que contiene el cultivo si se encuentra y pertenece al usuario.
     */
    Optional<Crop> findByIdAndUserId(Long id, Long userId);

    @Query("select crp from Crop crp where not exists(select 1 from CorporationMarketPrice cmp where cmp.crop.id=crp.id and cmp.corporation.id=:userId)")
    Page<Crop> findAllCrops(@Param("userId") Long userId, Pageable pageable);
}