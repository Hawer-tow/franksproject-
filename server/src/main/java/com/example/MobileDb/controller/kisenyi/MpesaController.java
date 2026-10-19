package com.example.MobileDb.controller.kisenyi;

import com.example.MobileDb.config.DarajaProperties;
import com.example.MobileDb.entity.kisenyi.Buying;
import com.example.MobileDb.entity.kisenyi.Selling;
import com.example.MobileDb.repository.kisenyi.BuyingRepository;
import com.example.MobileDb.repository.kisenyi.SellingRepository;
import com.example.MobileDb.service.MpesaSendmoneyService;
import com.example.MobileDb.service.MpesaStkpush_Service;
import com.example.MobileDb.service.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/mpesa")
public class MpesaController {

    private static final Logger log = LoggerFactory.getLogger(MpesaController.class);

    private final MpesaSendmoneyService darajaPaymentService;
    private final MpesaStkpush_Service stkpushService;
    private final SellingRepository sellingRepo;
    private final BuyingRepository buyingRepo;
    private final DarajaProperties props;

    public MpesaController(SellingRepository sellingRepo,
                          BuyingRepository buyingRepo,
                          MpesaSendmoneyService darajaPaymentService,
                          MpesaStkpush_Service stkpushService,
                          DarajaProperties props) {
        this.sellingRepo = sellingRepo;
        this.buyingRepo = buyingRepo;
        this.darajaPaymentService = darajaPaymentService;
        this.stkpushService = stkpushService;
        this.props = props;
    }

    // ========================================================================
    // 1. BUSINESS LOGIC ENDPOINTS (Called by Android App)
    // ========================================================================


    @PostMapping("/stkpush")
    public ResponseEntity<mpesa2phoneStkpush_Response> initiateStkPush(@RequestBody mpesa2phoneStkpush_Request request) {
        log.info("Initiating STK Push for: {}", request.getPhoneNumber());
        
        // 1. Initial Buying record
        Buying buying = new Buying();
        buying.setBuyingDate(LocalDate.now());
        buying.setBuyingAmount(BigDecimal.valueOf(request.getAmount()));
        buying.setBuyingRate(BigDecimal.valueOf(request.getRate()));
        buying.setBuyerName(request.getPhoneNumber());
        buying.setTransactingCountry("Kenya");
        buying.setBuyDescription("Mpesa STK Push Transaction");
        buyingRepo.save(buying);

        // 2. Call Service
        mpesa2phoneStkpush_Response response = stkpushService.initiateStkPush(request);

        // 3. Update with CheckoutRequestID
        buying.setCheckoutRequestId(response.getCheckoutRequestId());
        buyingRepo.save(buying);

        return ResponseEntity.ok(response);
    }

    /**
     * B2C SEND MONEY (Send Money to Phone - Selling)
     * Path: POST /mpesa/sendMoney
     */
    @PostMapping("/sendMoney")
    public ResponseEntity<?> sendMoneyDaraja(@RequestBody Selling selling) {
        log.info("Received B2C request: phone={} amount={}", selling.getPhoneNo(), selling.getSellingAmount());

        if (selling.getPhoneNo() == null || selling.getPhoneNo().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "missing_field", "field", "phoneNo"));
        }

        PaymentRequest req = new PaymentRequest();
        String clientOriginatorId = "APP_" + UUID.randomUUID().toString();
        req.setOriginatorConversationID(clientOriginatorId);
        req.setInitiatorName(props.getInitiatorName());
        req.setCommandID("BusinessPayment");
        req.setAmount(selling.getSellingAmount());
        req.setPartyA(props.getB2cShortcode()); 
        req.setPartyB(selling.getPhoneNo()); // Using phone from body

        req.setRemarks(selling.getSellDescription() != null ? selling.getSellDescription() : "Payment");
        req.setQueueTimeOutURL(props.getB2cTimeoutUrl()); 
        req.setResultURL(props.getB2cResultUrl());        
        req.setOccasion("Sale");

        try {
            PaymentResponse resp = darajaPaymentService.sendB2C(req);
            
            // Map Response to Entity
            selling.setProviderHttpStatus(String.valueOf(resp.getStatus()));
            selling.setProviderResponse(resp.getRaw() != null ? resp.getRaw().toString() : null);
            selling.setClientOriginatorId(clientOriginatorId);
            selling.setOriginatorConversationId(resp.getOriginatorConversationID());
            selling.setConversationId(resp.getConversationID());
            selling.setStatus("PENDING");
            selling.setSaleType("MPESA");
            if (selling.getSellingDate() == null) selling.setSellingDate(new Date());

            sellingRepo.save(selling);

            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("status", resp.getStatus());
            responseMap.put("message", resp.getMessage());
            responseMap.put("transactionId", resp.getTransactionId());
            responseMap.put("clientOriginatorID", clientOriginatorId);

            return ResponseEntity.ok(responseMap);
        } catch (Exception ex) {
            log.error("B2C Error", ex);
            return ResponseEntity.status(500).body(Map.of("error", "internal_error", "message", ex.getMessage()));
        }
    }
  

    // ========================================================================
    // 2. CALLBACK ENDPOINTS (Called by Safaricom Daraja)
    // ========================================================================

    @PostMapping("/callback/stkpush/result")
    public ResponseEntity<String> handleStkPushResult(@RequestBody Map<String, Object> payload) {
        log.info("STK Push Result Payload: {}", payload);

        try {
            Map<String, Object> body = (Map<String, Object>) payload.get("Body");
            Map<String, Object> stkCallback = (Map<String, Object>) body.get("stkCallback");

            String checkoutRequestId = (String) stkCallback.get("CheckoutRequestID");
            Integer resultCode = (Integer) stkCallback.get("ResultCode");
            String resultDesc = (String) stkCallback.get("ResultDesc");

            Buying buying = buyingRepo.findByCheckoutRequestId(checkoutRequestId);
            if (buying != null) {
                buying.setBuyDescription("STK Push Result: " + resultDesc);
                log.info("Updating Buying record with CheckoutRequestID={} and ResultDesc={}", checkoutRequestId, resultDesc);

                Map<String, Object> callbackMetadata = (Map<String, Object>) stkCallback.get("CallbackMetadata");
                if (callbackMetadata != null) {
                    var items = (Iterable<Map<String, Object>>) callbackMetadata.get("Item");
                    for (Map<String, Object> item : items) {
                        String name = (String) item.get("Name");
                        Object value = item.get("Value");
                        if ("Amount".equals(name)) {
                            buying.setBuyingAmount(new BigDecimal(value.toString()));
                        } else if ("PhoneNumber".equals(name)) {
                            buying.setBuyerName(value.toString());
                        }
                    }
                }
                log.info("Before save, BuyDescription={}", buying.getBuyDescription());

                buyingRepo.save(buying);
            }

            return ResponseEntity.ok("STK Push callback processed successfully");
        } catch (Exception e) {
            log.error("Error processing STK Push callback", e);
            return ResponseEntity.status(500).body("Error processing STK Push callback");
        }
    }
    
    @PostMapping("/callback/stkpush/timeout")
    public ResponseEntity<String> handleStkPushTimeout(@RequestBody Map<String, Object> payload) {
        log.info("STK Push Timeout Payload: {}", payload);
        return ResponseEntity.ok("STK Push timeout callback received");
    }

    @PostMapping("/callback/b2c/result")
    public ResponseEntity<Map<String, Object>> handleB2CResult(
            @RequestBody(required = false) Map<String, Object> payload) {

        log.info("Daraja B2C result callback received: {}", payload);

        if (payload == null || payload.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "empty_payload"));
        }

        Map<String, Object> resultMap = (Map<String, Object>) payload.get("Result");
        String originatorId = Objects.toString(resultMap.get("OriginatorConversationID"), null);
        String txnId = Objects.toString(resultMap.get("TransactionID"), null);
        String resultCode = Objects.toString(resultMap.get("ResultCode"), null);
        String resultDesc = Objects.toString(resultMap.get("ResultDesc"), null);

        if (originatorId == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "originator_id_missing",
                    "ResultCode", resultCode != null ? resultCode : "UNKNOWN",
                    "ResultDesc", resultDesc != null ? resultDesc : "UNKNOWN"
            ));
        }

        Optional<Selling> maybeSelling = sellingRepo.findByOriginatorConversationId(originatorId);
        if (maybeSelling.isPresent()) {
            Selling s = maybeSelling.get();
            s.setTransactionId(txnId);
            s.setStatus("0".equals(resultCode) ? "SUCCESS" : "FAILED");
            s.setSaleType("MPESA");
            s.setSellDescription(resultDesc);
            s.setProviderHttpStatus(resultCode);
            s.setProviderResponse(payload.toString());
            sellingRepo.save(s);
        } else {
            log.warn("No Selling entity found with OriginatorConversationID {}", originatorId);
        }

        Map<String, Object> ack = Map.of(
                "ResultCode", resultCode != null ? resultCode : "UNKNOWN",
                "ResultDesc", resultDesc != null ? resultDesc : "UNKNOWN",
                "TransactionID", txnId
        );

        return ResponseEntity.ok(ack);
    }

    @PostMapping("/callback/timeout")
    public ResponseEntity<Map<String, Object>> handleB2CTimeout(
            @RequestBody(required = false) Map<String, Object> payload) {
        log.info("Daraja B2C timeout callback received: {}", payload);
        return ResponseEntity.ok(Map.of("ResultCode", 0, "ResultDesc", "Accepted"));
    }
}
