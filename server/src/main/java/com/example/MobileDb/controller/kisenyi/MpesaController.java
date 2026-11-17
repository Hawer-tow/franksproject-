package com.example.MobileDb.controller.kisenyi;

import com.example.MobileDb.config.DarajaProperties;
import com.example.MobileDb.entity.kisenyi.ProviderTransaction;
import com.example.MobileDb.entity.kisenyi.Selling;
import com.example.MobileDb.payments.DarajaCallbacks;
import com.example.MobileDb.payments.PaymentFormat;
import com.example.MobileDb.payments.PaymentStatus;
import com.example.MobileDb.payments.ProviderException;
import com.example.MobileDb.service.MpesaSendmoneyService;
import com.example.MobileDb.service.MpesaStkpush_Service;
import com.example.MobileDb.service.PaymentRecorder;
import com.example.MobileDb.service.dto.PaymentRequest;
import com.example.MobileDb.service.dto.PaymentResponse;
import com.example.MobileDb.service.dto.mpesa2phoneStkpush_Request;
import com.example.MobileDb.service.dto.mpesa2phoneStkpush_Response;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Safaricom M-Pesa (Daraja).
 *   POST /mpesa/stkpush     payment prompt on the customer's phone (money in)
 *   POST /mpesa/sendMoney   B2C payment to the customer's phone (money out)
 *   POST /mpesa/callback/** results from Safaricom (no app encryption; see UserAccessFilter)
 */
@RestController
@RequestMapping("/mpesa")
public class MpesaController {

    private static final Logger log = LoggerFactory.getLogger(MpesaController.class);
    private static final Map<String, Object> ACK = Map.of("ResultCode", 0, "ResultDesc", "Accepted");

    private final MpesaSendmoneyService b2cService;
    private final MpesaStkpush_Service stkService;
    private final PaymentRecorder recorder;
    private final DarajaProperties props;
    private final ObjectMapper json;

    public MpesaController(MpesaSendmoneyService b2cService, MpesaStkpush_Service stkService,
                           PaymentRecorder recorder, DarajaProperties props, ObjectMapper json) {
        this.b2cService = b2cService;
        this.stkService = stkService;
        this.recorder = recorder;
        this.props = props;
        this.json = json;
    }

    // ======================================================================
    // Requests from the app
    // ======================================================================

    /** Payment prompt. The buy is recorded when Safaricom confirms the customer paid. */
    @PostMapping("/stkpush")
    public ResponseEntity<?> initiateStkPush(@RequestBody mpesa2phoneStkpush_Request request) {
        String msisdn;
        BigDecimal amount;
        BigDecimal rate;
        try {
            msisdn = PaymentFormat.kenyanMsisdn(request.getPhoneNumber());
            amount = PaymentFormat.positiveAmount(request.getAmount(), "Amount");
            rate = request.getRate() > 0 ? BigDecimal.valueOf(request.getRate()) : null;
        } catch (IllegalArgumentException e) {
            return badRequest(e.getMessage());
        }
        int shillings = PaymentFormat.wholeShillings(amount);

        String reference = recorder.newReference(10);
        ProviderTransaction txn = recorder.start(ProviderTransaction.MPESA, ProviderTransaction.MPESA_STK_PUSH,
                reference, msisdn, props.getShortcode(), BigDecimal.valueOf(shillings), rate, "KES");
        try {
            mpesa2phoneStkpush_Response response = stkService.initiate(msisdn, shillings, "FM" + reference, "FrankMpesa");
            recorder.accepted(txn, response.getCheckoutRequestId(), response.getMerchantRequestId(), toJson(response));
            log.info("M-Pesa prompt sent: ref={} checkout={}", reference, response.getCheckoutRequestId());
            return ResponseEntity.ok(response);
        } catch (ProviderException e) {
            recorder.rejected(txn, e.getMessage(), e.getProviderBody());
            return providerError(e.getMessage(), reference);
        }
    }

    /** B2C: send money to the customer's M-Pesa. The sale is created as PENDING once Safaricom accepts. */
    @PostMapping("/sendMoney")
    public ResponseEntity<?> sendMoneyDaraja(@RequestBody Selling selling) {
        String msisdn;
        BigDecimal amount;
        try {
            msisdn = PaymentFormat.kenyanMsisdn(selling.getPhoneNo());
            amount = PaymentFormat.positiveAmount(selling.getSellingAmount(), "Amount");
        } catch (IllegalArgumentException e) {
            return badRequest(e.getMessage());
        }
        int shillings = PaymentFormat.wholeShillings(amount);
        BigDecimal rate = selling.getSellingRate() != null && selling.getSellingRate() > 0
                ? BigDecimal.valueOf(selling.getSellingRate()) : null;

        String reference = "FM" + recorder.newReference(16);
        ProviderTransaction txn = recorder.start(ProviderTransaction.MPESA, ProviderTransaction.MPESA_B2C,
                reference, msisdn, props.getB2cShortcode(), BigDecimal.valueOf(shillings), rate, "KES");

        PaymentRequest req = new PaymentRequest();
        req.setOriginatorConversationID(reference);
        req.setInitiatorName(props.getInitiatorName());
        req.setCommandID("BusinessPayment");
        req.setAmount((double) shillings);
        req.setPartyA(props.getB2cShortcode());
        req.setPartyB(msisdn);
        req.setRemarks(selling.getSellDescription() != null ? selling.getSellDescription() : "FrankMpesa payment");
        req.setOccasion("Sale");

        try {
            PaymentResponse resp = b2cService.sendB2C(req);
            if (!"0".equals(resp.getResponseCode())) {
                String why = resp.getResponseDescription() != null ? resp.getResponseDescription()
                        : describeRaw(resp.getRaw());
                recorder.rejected(txn, "Safaricom refused the payment: " + why, toJson(resp.getRaw()));
                return providerError("Safaricom refused the payment: " + why, reference);
            }
            recorder.accepted(txn, resp.getConversationID(), resp.getOriginatorConversationID(), toJson(resp.getRaw()));
            Selling sale = recorder.recordPendingSale(txn, "MPESA", msisdn, selling.getRecipientName(),
                    props.getB2cShortcode(), msisdn,
                    selling.getSellDescription() != null ? selling.getSellDescription() : "M-Pesa payment");

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("status", PaymentStatus.PENDING);
            body.put("message", resp.getResponseDescription());
            body.put("reference", reference);
            body.put("conversationId", resp.getConversationID());
            body.put("sellingId", sale.getSellingId());
            return ResponseEntity.ok(body);
        } catch (IllegalArgumentException e) {
            recorder.rejected(txn, e.getMessage(), null);
            return badRequest(e.getMessage());
        } catch (RuntimeException e) {
            log.error("B2C request {} failed", reference, e);
            recorder.rejected(txn, e.getMessage(), null);
            return providerError("Could not send the payment: " + e.getMessage(), reference);
        }
    }

    // ======================================================================
    // Callbacks from Safaricom (always acknowledged so Safaricom does not keep retrying)
    // ======================================================================

    @PostMapping("/callback/stkpush/result")
    public ResponseEntity<Map<String, Object>> handleStkPushResult(@RequestBody Map<String, Object> payload) {
        try {
            DarajaCallbacks.StkResult r = DarajaCallbacks.parseStk(payload);
            Optional<ProviderTransaction> txn = recorder.byProviderRequestId(r.checkoutRequestId());
            if (txn.isPresent()) {
                ProviderTransaction t = recorder.applyStkResult(txn.get(), r, toJson(payload));
                log.info("M-Pesa prompt {} -> {} ({})", t.getReference(), t.getStatus(), r.resultDesc());
            } else {
                log.warn("STK callback for unknown CheckoutRequestID {}: {}", r.checkoutRequestId(), r.resultDesc());
            }
        } catch (RuntimeException e) {
            log.error("Could not process STK callback: {}", payload, e);
        }
        return ResponseEntity.ok(ACK);
    }

    @PostMapping("/callback/stkpush/timeout")
    public ResponseEntity<Map<String, Object>> handleStkPushTimeout(@RequestBody(required = false) Map<String, Object> payload) {
        log.info("STK push timeout notification: {}", payload);
        return ResponseEntity.ok(ACK);
    }

    @PostMapping("/callback/b2c/result")
    public ResponseEntity<Map<String, Object>> handleB2CResult(@RequestBody(required = false) Map<String, Object> payload) {
        try {
            DarajaCallbacks.B2cResult r = DarajaCallbacks.parseB2c(payload);
            Optional<ProviderTransaction> txn = findB2c(r.originatorConversationId(), r.conversationId());
            if (txn.isPresent()) {
                ProviderTransaction t = recorder.applyB2cResult(txn.get(), r, toJson(payload));
                log.info("M-Pesa B2C {} -> {} ({})", t.getReference(), t.getStatus(), r.resultDesc());
            } else {
                log.warn("B2C result for unknown request {} / {}", r.originatorConversationId(), r.conversationId());
            }
        } catch (RuntimeException e) {
            log.error("Could not process B2C result: {}", payload, e);
        }
        return ResponseEntity.ok(ACK);
    }

    @PostMapping("/callback/timeout")
    public ResponseEntity<Map<String, Object>> handleB2CTimeout(@RequestBody(required = false) Map<String, Object> payload) {
        try {
            DarajaCallbacks.B2cResult r = DarajaCallbacks.parseB2c(payload);
            findB2c(r.originatorConversationId(), r.conversationId())
                    .ifPresent(t -> recorder.applyB2cTimeout(t, toJson(payload)));
        } catch (RuntimeException e) {
            log.warn("B2C timeout notification without a recognisable request: {}", payload);
        }
        return ResponseEntity.ok(ACK);
    }

    // ======================================================================

    private Optional<ProviderTransaction> findB2c(String originatorConversationId, String conversationId) {
        Optional<ProviderTransaction> byOurId = originatorConversationId == null
                ? Optional.empty() : recorder.byReference(originatorConversationId);
        return byOurId.isPresent() ? byOurId : recorder.byProviderRequestId(conversationId);
    }

    private String toJson(Object o) {
        try {
            return o == null ? null : json.writeValueAsString(o);
        } catch (Exception e) {
            return String.valueOf(o);
        }
    }

    private static String describeRaw(Map<String, Object> raw) {
        if (raw == null) return "no details";
        Object m = raw.getOrDefault("errorMessage", raw.get("ResponseDescription"));
        return m != null ? m.toString() : raw.toString();
    }

    static ResponseEntity<Map<String, Object>> badRequest(String message) {
        return ResponseEntity.badRequest().body(Map.of("error", "invalid_request", "message", message));
    }

    static ResponseEntity<Map<String, Object>> providerError(String message, String reference) {
        return ResponseEntity.status(502).body(Map.of("error", "provider_error", "message", message, "reference", reference));
    }
}
