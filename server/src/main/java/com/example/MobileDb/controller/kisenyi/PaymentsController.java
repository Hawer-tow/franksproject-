package com.example.MobileDb.controller.kisenyi;

import com.example.MobileDb.entity.kisenyi.ProviderTransaction;
import com.example.MobileDb.payments.DarajaCallbacks;
import com.example.MobileDb.payments.PaymentStatus;
import com.example.MobileDb.payments.ProviderException;
import com.example.MobileDb.service.MpesaStkpush_Service;
import com.example.MobileDb.service.PaymentRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * GET /payments/{reference}: current state of an M-Pesa or Equity request.
 * When an M-Pesa prompt is still pending after 30 seconds the result is fetched from Safaricom
 * directly, so a lost callback (e.g. tunnel down) does not leave the payment stuck.
 */
@RestController
@RequestMapping("/payments")
public class PaymentsController {

    private static final Logger log = LoggerFactory.getLogger(PaymentsController.class);

    private final PaymentRecorder recorder;
    private final MpesaStkpush_Service stkService;

    public PaymentsController(PaymentRecorder recorder, MpesaStkpush_Service stkService) {
        this.recorder = recorder;
        this.stkService = stkService;
    }

    @GetMapping("/{reference}")
    public ResponseEntity<?> status(@PathVariable String reference) {
        Optional<ProviderTransaction> found = recorder.byReference(reference);
        if (found.isEmpty()) found = recorder.byProviderRequestId(reference);
        if (found.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "not_found", "message", "No payment with reference " + reference));
        }
        ProviderTransaction t = found.get();

        if (ProviderTransaction.MPESA_STK_PUSH.equals(t.getKind())
                && PaymentStatus.PENDING.equals(t.getStatus())
                && t.getProviderRequestId() != null
                && t.getCreatedAt() != null
                && t.getCreatedAt().isBefore(LocalDateTime.now().minusSeconds(30))) {
            try {
                DarajaCallbacks.StkResult r = stkService.query(t.getProviderRequestId());
                if (r != null) t = recorder.applyStkResult(t, r, null);
            } catch (ProviderException e) {
                log.warn("STK status query for {} failed: {}", reference, e.getMessage());
            }
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("reference", t.getReference());
        body.put("provider", t.getProvider());
        body.put("kind", t.getKind());
        body.put("status", t.getStatus());
        body.put("message", t.getResultDesc());
        body.put("receipt", t.getReceipt());
        body.put("amount", t.getAmount());
        body.put("phone", t.getPhone());
        body.put("buyingId", t.getBuyingId());
        body.put("sellingId", t.getSellingId());
        body.put("createdAt", t.getCreatedAt() != null ? t.getCreatedAt().toString() : null);
        body.put("updatedAt", t.getUpdatedAt() != null ? t.getUpdatedAt().toString() : null);
        return ResponseEntity.ok(body);
    }
}
