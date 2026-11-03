package uz.paykap.web.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.javlon.commons.base.SimpleResultData;
import uz.javlon.commons.result.HasData;
import uz.paykap.domain.Transaction;
import uz.paykap.service.cancel.resolver.CancelResolver;
import uz.paykap.service.cancel.resolver.SyncTransactionService;

@RestController
@RequestMapping("/api")
public class CancelResource {

    private final Logger log = LoggerFactory.getLogger(CancelResource.class);
    private final SyncTransactionService syncTransactionService;
    private final CancelResolver cancelResolver;

    public CancelResource(SyncTransactionService syncTransactionService, CancelResolver cancelResolver) {
        this.syncTransactionService = syncTransactionService;
        this.cancelResolver = cancelResolver;
    }

    @GetMapping("/cancel/transaction/sync/{id}")
    public ResponseEntity<HasData<Transaction>> syncTransaction(@PathVariable Long id) {
        log.debug("Call syncTransaction method with param : {}", id);
        HasData<Transaction> result = syncTransactionService.syncTransaction(id);
        log.debug("Sync transaction result : {}", result);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/cancel/transaction/sync-and-cancel/{id}")
    public ResponseEntity<SimpleResultData<Transaction>> syncAndCancel(@PathVariable Long id) {
        log.debug("Call Cancel method with param : {}", id);
        SimpleResultData<Transaction> result = cancelResolver.cancel(id);
        log.debug("Sync transaction result : {}", result);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/transaction/cancel/sender/{id}")
    public ResponseEntity<SimpleResultData<Transaction>> cancelSenderP2pTransaction(@PathVariable Long id) {
        log.debug("Call Cancel method with param : {}", id);
        SimpleResultData<Transaction> result = cancelResolver.cancelSender(id);
        log.debug("transaction result : {}", result);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/transaction/cancel/recipient/{id}")
    public ResponseEntity<SimpleResultData<Transaction>> cancelRecipientP2pTransaction(@PathVariable Long id) {
        log.debug("Call Cancel method with param : {}", id);
        SimpleResultData<Transaction> result = cancelResolver.cancelRecipient(id);
        return ResponseEntity.ok(result);
    }
}
