package com.project.demo.rest.transaction;

import com.project.demo.logic.entity.transaction.Transaction;
import com.project.demo.logic.entity.transaction.TransactionRepository;
import com.project.demo.logic.entity.user.User;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
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

import java.util.Optional;

/**
 * Controlador REST para gestionar las transacciones de un usuario.
 * Las operaciones están siempre acotadas al usuario actualmente autenticado.
 */
@RestController
@RequestMapping("/transactions")
public class TransactionRestController {

    @Autowired
    private TransactionRepository transactionRepository;

    /**
     * Crea una nueva transacción para el usuario USER (Farm-Admin). El usuario de la transacción
     * se asigna automáticamente basado en la sesión actual.
     * @param transaction La transacción a crear, enviada en el cuerpo de la solicitud.
     * @param request La solicitud HTTP.
     * @return ResponseEntity con la transacción guardada.
     */
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> createTransaction(@RequestBody Transaction transaction, HttpServletRequest request) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        transaction.setUser(currentUser);
        Transaction savedTransaction = transactionRepository.save(transaction);

        Transaction fullTransaction = transactionRepository.findById(savedTransaction.getId()).orElse(savedTransaction);
        return new GlobalResponseHandler().handleResponse("Transacción realizada exitosamente!", new TransactionDTO(fullTransaction), HttpStatus.CREATED, request);
    }

    /**
     * Obtiene una lista paginada de las transacciones del usuario USER (Farm-Admin).
     * @param page El número de página a solicitar.
     * @param size El número de elementos por página.
     * @param request La solicitud HTTP.
     * @return ResponseEntity con una lista de transacciones y metadatos de paginación.
     */
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getUserTransactions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Pageable pageable = PageRequest.of(page - 1, size);

        Page<Transaction> transactionPage = transactionRepository.findByUserIdWithDetails(currentUser.getId(), pageable);

        Page<TransactionDTO> dtoPage = transactionPage.map(TransactionDTO::new);

        Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
        meta.setTotalPages(dtoPage.getTotalPages());
        meta.setTotalElements(dtoPage.getTotalElements());
        meta.setPageNumber(dtoPage.getNumber() + 1);
        meta.setPageSize(dtoPage.getSize());

        return new GlobalResponseHandler().handleResponse("Transacción obtenida exitosamente!", dtoPage.getContent(), HttpStatus.OK, meta);
    }

    /**
     * Actualiza una transacción específica del usuario USER (Farm-Admin).
     * @param id El ID de la transacción a actualizar.
     * @param transactionDetails Los nuevos datos para la transacción.
     * @param request La solicitud HTTP.
     * @return ResponseEntity con la transacción actualizada o un error si no se encuentra.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> updateUserTransaction(@PathVariable Long id, @RequestBody Transaction transactionDetails, HttpServletRequest request) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<Transaction> optionalTransaction = transactionRepository.findByIdAndUserId(id, currentUser.getId());

        if (optionalTransaction.isPresent()) {
            Transaction existingTransaction = optionalTransaction.get();

            existingTransaction.setTransactionType(transactionDetails.getTransactionType());
            existingTransaction.setQuantity(transactionDetails.getQuantity());
            existingTransaction.setMeasureUnit(transactionDetails.getMeasureUnit());
            existingTransaction.setPricePerUnit(transactionDetails.getPricePerUnit());
            existingTransaction.setTotalValue(transactionDetails.getTotalValue());
            existingTransaction.setTransactionDate(transactionDetails.getTransactionDate());

            existingTransaction.setFarm(transactionDetails.getFarm());
            existingTransaction.setCrop(transactionDetails.getCrop());

            Transaction updatedTransaction = transactionRepository.save(existingTransaction);
            Transaction fullTransaction = transactionRepository.findById(updatedTransaction.getId()).orElse(updatedTransaction);

            return new GlobalResponseHandler().handleResponse("Transacción actualizada exitosamente!", new TransactionDTO(fullTransaction), HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Transacción no encontrada o acceso denegado", HttpStatus.NOT_FOUND, request);
        }
    }

    /**
     * Elimina una transacción específica del usuario USER (Farm-Admin).
     * @param id El ID de la transacción a eliminar.
     * @param request La solicitud HTTP.
     * @return ResponseEntity con el objeto eliminado o un error si no se encuentra.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> deleteUserTransaction(@PathVariable Long id, HttpServletRequest request) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<Transaction> optionalTransaction = transactionRepository.findByIdAndUserId(id, currentUser.getId());

        if (optionalTransaction.isPresent()) {
            Transaction transactionToDeactivate = optionalTransaction.get();
            transactionToDeactivate.setIsActive(false);
            transactionRepository.save(transactionToDeactivate);

            return new GlobalResponseHandler().handleResponse("Transacción eliminada exitosamente!", new TransactionDTO(transactionToDeactivate), HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Transacción no encontrada o acceso denegado.", HttpStatus.NOT_FOUND, request);
        }
    }
}