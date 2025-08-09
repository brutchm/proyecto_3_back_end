package com.project.demo.rest.marketprice;

import com.project.demo.logic.entity.http.Meta;
import com.project.demo.logic.entity.marketprice.CorporationMarketPrice;
import com.project.demo.logic.entity.marketprice.CorporationMarketPriceRepository;
import com.project.demo.logic.entity.user.User;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Controlador para que los usuarios de tipo CORPORACION gestionen sus precios de mercado
 * y para que otros usuarios puedan consultarlos.
 */
@RestController
@RequestMapping("/market-prices")
public class CorporationMarketPriceRestController {

    @Autowired
    private CorporationMarketPriceRepository marketPriceRepository;

    /**
     * Permite a una corporación autenticada establecer o actualizar el precio de un cultivo.
     * Si ya existe un precio para el cultivo, lo actualiza. Si no, lo crea.
     * @param marketPrice El objeto con la información del precio de mercado (requiere al menos crop.id, price).
     * @param request La solicitud HTTP.
     * @return ResponseEntity con el precio guardado.
     */
    @PostMapping
    @PreAuthorize("hasRole('CORPORATION')")
    public ResponseEntity<?> setMarketPrice(@RequestBody CorporationMarketPrice marketPrice, HttpServletRequest request) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (marketPrice.getCrop() == null || marketPrice.getCrop().getId() == null) {
            return new GlobalResponseHandler().handleResponse("Crop ID is required", HttpStatus.BAD_REQUEST, request);
        }

        Optional<CorporationMarketPrice> existingPrice = marketPriceRepository
                .findByCropIdAndCorporationId(marketPrice.getCrop().getId(), currentUser.getId());

        CorporationMarketPrice priceToSave;
        if (existingPrice.isPresent()) {
            priceToSave = existingPrice.get();
            priceToSave.setPrice(marketPrice.getPrice());
            priceToSave.setMeasureUnit(marketPrice.getMeasureUnit());
        } else {
            priceToSave = marketPrice;
            priceToSave.setCorporation(currentUser);
        }

        CorporationMarketPrice savedPrice = marketPriceRepository.save(priceToSave);
        return new GlobalResponseHandler().handleResponse("Se publicó el precio correctamente", savedPrice, HttpStatus.OK, request);
    }

    /**
     * Obtiene todos los precios de mercado establecidos por la corporación actualmente autenticada.
     * @param request La solicitud HTTP.
     * @return ResponseEntity con una lista de sus precios de mercado.
     */
    @GetMapping("/my-prices")
    @PreAuthorize("hasRole('CORPORATION')")
    public ResponseEntity<?> getMyMarketPrices(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Pageable pageable = PageRequest.of(page - 1, size);

        Page<CorporationMarketPrice> prices = marketPriceRepository.findByCorporationId(currentUser.getId(),pageable);

        Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
        meta.setTotalPages(prices.getTotalPages());
        meta.setTotalElements(prices.getTotalElements());
        meta.setPageNumber(prices.getNumber() + 1);
        meta.setPageSize(prices.getSize());

        return new GlobalResponseHandler().handleResponse("My market prices retrieved successfully",
                prices.getContent(), HttpStatus.OK, meta);
    }

    /**
     * Obtiene todos los precios de mercado de todas las corporaciones (vista pública para usuarios autenticados).
     * @param request La solicitud HTTP.
     * @return ResponseEntity con una lista de todos los precios de mercado.
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAllMarketPrices(HttpServletRequest request) {
        List<CorporationMarketPrice> allPrices = marketPriceRepository.findAll();
        return new GlobalResponseHandler().handleResponse("All market prices retrieved successfully", allPrices, HttpStatus.OK, request);
    }

    /**
     * Elimina un precio de mercado publicado por la corporación autenticada.
     * @param id El ID del precio a eliminar.
     * @param request La solicitud HTTP.
     * @return ResponseEntity indicando el resultado de la operación.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CORPORATION')")
    public ResponseEntity<?> deleteMarketPrice(@PathVariable Long id, HttpServletRequest request) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Optional<CorporationMarketPrice> priceOpt = marketPriceRepository.findById(id);

        if (priceOpt.isEmpty()) {
            return new GlobalResponseHandler().handleResponse("El precio no fue encontrado", HttpStatus.NOT_FOUND, request);
        }

        CorporationMarketPrice price = priceOpt.get();

        if (!price.getCorporation().getId().equals(currentUser.getId())) {
            return new GlobalResponseHandler().handleResponse("No tienes permiso para eliminar este precio", HttpStatus.FORBIDDEN, request);
        }

        marketPriceRepository.deleteById(id);

        return new GlobalResponseHandler().handleResponse("El precio fue eliminado correctamente", HttpStatus.NO_CONTENT, request);
    }

}