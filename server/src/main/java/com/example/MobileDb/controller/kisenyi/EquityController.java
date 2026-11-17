package com.example.MobileDb.controller.kisenyi;

import com.example.MobileDb.entity.kisenyi.ProviderTransaction;
import com.example.MobileDb.payments.JengaCallbacks;
import com.example.MobileDb.payments.PaymentFormat;
import com.example.MobileDb.payments.PaymentStatus;
import com.example.MobileDb.service.EquityAccountSendMoneyService;
import com.example.MobileDb.service.JengaSendMoneyService;
import com.example.MobileDb.service.JengaStkPushService;
import com.example.MobileDb.service.PaymentRecorder;
import com.example.MobileDb.service.dto.EquityAccountSendMoneyRequest;
import com.example.MobileDb.service.dto.EquityStkPushRequest;
import com.example.MobileDb.service.dto.JengaSendMoneyRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

/**
 * Equity Bank (Jenga API v3, developer.jengahq.io).
 *   POST /equity/stkpush        payment prompt to a Safaricom or Equitel phone (money in)
 *   POST /equity/sendmoney      Equity account -> mobile wallet (money out)
 *   POST /equity/sendToAccount  Equity account -> Equity account (money out, result is immediate)
 *   POST /equity/*callback      results from Jenga
 * Request signatures are built in the Jenga services using the documented field order.
 */
@RestController
@RequestMapping("/equity")
public class EquityController {

    private static final Logger log = LoggerFactory.getLogger(EquityController.class);

    private final JengaStkPushService stkService;
    private final JengaSendMoneyService sendMobileService;
    private final EquityAccountSendMoneyService internalTransferService;
    private final PaymentRecorder recorder;
    private final ObjectMapper json;

    @Value("${jenga.merchantAccountNumber}")
    private String merchantAccountNumber;

    @Value("${jenga.merchantName}")
    private String merchantName;

    @Value("${jenga.countryCode:KE}")
    private String countryCode;

    @Value("${jenga.callbackBaseUrl}")
    private String callbackBaseUrl;

    public EquityController(JengaStkPushService stkService, JengaSendMoneyService sendMobileService,
                            EquityAccountSendMoneyService internalTransferService, PaymentRecorder recorder,
                            ObjectMapper json) {
        this.stkService = stkService;
        this.sendMobileService = sendMobileService;
        this.internalTransferService = internalTransferService;
        this.recorder = recorder;
        this.json = json;
    }

    /** What the app sends for every Equity action (fields not used by an action are ignored). */
    public static class UserStkPushInput {
        private String phoneNumber;
        private String recipientPhone;
        private String amount;
        private String rate;
        private String telco;
        private String recipientAccount;
        private String senderAccount;

        public String getRecipientAccount() { return recipientAccount; }
        public void setRecipientAccount(String recipientAccount) { this.recipientAccount = recipientAccount; }
        public String getSenderAccount() { return senderAccount; }
        public void setSenderAccount(String senderAccount) { this.senderAccount = senderAccount; }
        public String getAmount() { return amount; }
        public void setAmount(String amount) { this.amount = amount; }
        public String getRate() { return rate; }
        public void setRate(String rate) { this.rate = rate; }
        public String getTelco() { return telco; }
        public void setTelco(String telco) { this.telco = telco; }
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
        public String getRecipientPhone() { return recipientPhone; }
        public void setRecipientPhone(String recipientPhone) { this.recipientPhone = recipientPhone; }
    }

    // ======================================================================
    // Money in: payment prompt
    // ======================================================================

    @PostMapping("/stkpush")
    public ResponseEntity<?> initiateEquityStkPush(@RequestBody UserStkPushInput input) {
        String msisdn, telco;
        BigDecimal amount, rate;
        try {
            msisdn = PaymentFormat.kenyanMsisdn(input.getPhoneNumber());
            telco = PaymentFormat.jengaTelco(input.getTelco());
            amount = PaymentFormat.positiveAmount(input.getAmount(), "Amount");
            rate = PaymentFormat.optionalRate(input.getRate());
        } catch (IllegalArgumentException e) {
            return MpesaController.badRequest(e.getMessage());
        }

        // Jenga: payment.ref is 6 alphanumeric characters for M-Pesa (6-12 for Equitel)
        String reference = recorder.newReference(6);
        ProviderTransaction txn = recorder.start(ProviderTransaction.EQUITY, ProviderTransaction.EQUITY_STK_PUSH,
                reference, msisdn, merchantAccountNumber, amount, rate, "KES");

        EquityStkPushRequest request = new EquityStkPushRequest();
        EquityStkPushRequest.Merchant merchant = new EquityStkPushRequest.Merchant();
        merchant.setAccountNumber(merchantAccountNumber);
        merchant.setCountryCode(countryCode);
        merchant.setName(merchantName);
        request.setMerchant(merchant);

        EquityStkPushRequest.Payment payment = new EquityStkPushRequest.Payment();
        payment.setRef(reference);
        payment.setAmount(PaymentFormat.twoDecimals(amount));
        payment.setCurrency("KES");
        payment.setTelco(telco);
        payment.setMobileNumber(msisdn);
        payment.setDate(LocalDate.now(PaymentFormat.NAIROBI).toString());
        payment.setCallBackUrl(callbackBaseUrl + "/equity/stkpushcallback");
        payment.setPushType("STK");
        request.setPayment(payment);

        try {
            String response = stkService.initiateStkPush(request);
            if (!JengaCallbacks.isAccepted(response)) {
                String why = JengaCallbacks.message(response);
                recorder.rejected(txn, "Equity did not send the prompt: " + why, response);
                return MpesaController.providerError("Equity did not send the prompt: " + why, reference);
            }
            recorder.accepted(txn, null, null, response);
            log.info("Equity prompt sent: ref={} telco={}", reference, telco);
            return ResponseEntity.ok(response);
        } catch (HttpStatusCodeException e) {
            return refused(txn, "send the prompt", e);
        } catch (RuntimeException e) {
            recorder.rejected(txn, e.getMessage(), null);
            return MpesaController.providerError("Could not reach Equity: " + e.getMessage(), reference);
        }
    }

    @PostMapping("/stkpushcallback")
    public ResponseEntity<String> handleStkPushCallback(@RequestBody Map<String, Object> payload) {
        try {
            JengaCallbacks.JengaResult r = JengaCallbacks.parseStkPush(payload);
            Optional<ProviderTransaction> txn = r.reference() == null ? Optional.empty() : recorder.byReference(r.reference());
            if (txn.isPresent()) {
                ProviderTransaction t = recorder.applyJengaStkResult(txn.get(), r, toJson(payload));
                log.info("Equity prompt {} -> {} ({})", t.getReference(), t.getStatus(), r.message());
            } else {
                log.warn("Equity prompt callback for unknown reference {}: {}", r.reference(), payload);
            }
        } catch (RuntimeException e) {
            log.error("Could not process Equity prompt callback: {}", payload, e);
        }
        return ResponseEntity.ok("Callback processed");
    }

    // ======================================================================
    // Money out: to a mobile wallet
    // ======================================================================

    @PostMapping("/sendmoney")
    public ResponseEntity<?> sendMoney(@RequestBody UserStkPushInput input) {
        String msisdn, wallet;
        BigDecimal amount, rate;
        try {
            msisdn = PaymentFormat.kenyanMsisdn(input.getRecipientPhone());
            wallet = PaymentFormat.jengaWallet(input.getTelco());
            amount = PaymentFormat.positiveAmount(input.getAmount(), "Amount");
            rate = PaymentFormat.optionalRate(input.getRate());
            requireText(input.getSenderAccount(), "Sender account");
        } catch (IllegalArgumentException e) {
            return MpesaController.badRequest(e.getMessage());
        }

        String reference = recorder.newReference(12);
        ProviderTransaction txn = recorder.start(ProviderTransaction.EQUITY, ProviderTransaction.EQUITY_SEND_MOBILE,
                reference, msisdn, input.getSenderAccount().trim(), amount, rate, "KES");

        JengaSendMoneyRequest request = new JengaSendMoneyRequest();
        JengaSendMoneyRequest.Source source = new JengaSendMoneyRequest.Source();
        source.setAccountNumber(input.getSenderAccount().trim());
        source.setCountryCode(countryCode);
        source.setName(merchantName);
        request.setSource(source);

        JengaSendMoneyRequest.Destination dest = new JengaSendMoneyRequest.Destination();
        dest.setType("mobile");
        dest.setCountryCode(countryCode);
        dest.setName("Customer");
        dest.setMobileNumber(msisdn);
        dest.setWalletName(wallet);
        request.setDestination(dest);

        JengaSendMoneyRequest.Transfer transfer = new JengaSendMoneyRequest.Transfer();
        transfer.setType("MobileWallet");
        transfer.setAmount(PaymentFormat.twoDecimals(amount));
        transfer.setCurrencyCode("KES");
        transfer.setReference(reference);
        transfer.setDate(LocalDate.now(PaymentFormat.NAIROBI).toString());
        transfer.setDescription("FrankMpesa payment");
        transfer.setCallbackUrl(callbackBaseUrl + "/equity/sendmoneycallback");
        request.setTransfer(transfer);

        try {
            String response = sendMobileService.sendMoney(request);
            if (!JengaCallbacks.isAccepted(response)) {
                String why = JengaCallbacks.message(response);
                recorder.rejected(txn, "Equity refused the payment: " + why, response);
                return MpesaController.providerError("Equity refused the payment: " + why, reference);
            }
            Object transactionId = JengaCallbacks.readJson(response).get("transactionId");
            recorder.accepted(txn, transactionId != null ? transactionId.toString() : null, null, response);
            recorder.recordPendingSale(txn, "EQUITY_MOBILE", msisdn, null, input.getSenderAccount().trim(), msisdn,
                    "Equity to " + wallet);
            return ResponseEntity.ok(response);
        } catch (HttpStatusCodeException e) {
            return refused(txn, "send the payment", e);
        } catch (RuntimeException e) {
            recorder.rejected(txn, e.getMessage(), null);
            return MpesaController.providerError("Could not reach Equity: " + e.getMessage(), reference);
        }
    }

    @PostMapping("/sendmoneycallback")
    public ResponseEntity<String> handleSendMoneyCallback(@RequestBody Map<String, Object> payload) {
        try {
            JengaCallbacks.JengaResult r = JengaCallbacks.parseSendMobile(payload);
            Optional<ProviderTransaction> txn = r.reference() == null ? Optional.empty() : recorder.byReference(r.reference());
            if (txn.isPresent()) {
                ProviderTransaction t = recorder.applyJengaTransferResult(txn.get(), r, toJson(payload));
                log.info("Equity send-to-mobile {} -> {} ({})", t.getReference(), t.getStatus(), r.message());
            } else {
                log.warn("Equity send-money callback for unknown reference {}: {}", r.reference(), payload);
            }
        } catch (RuntimeException e) {
            log.error("Could not process Equity send-money callback: {}", payload, e);
        }
        return ResponseEntity.ok("Callback processed");
    }

    // ======================================================================
    // Money out: to another Equity account (no callback; response is final)
    // ======================================================================

    @PostMapping("/sendToAccount")
    public ResponseEntity<?> sendToAccount(@RequestBody UserStkPushInput input) {
        BigDecimal amount, rate;
        try {
            requireText(input.getSenderAccount(), "Sender account");
            requireText(input.getRecipientAccount(), "Receiver account");
            amount = PaymentFormat.positiveAmount(input.getAmount(), "Amount");
            rate = PaymentFormat.optionalRate(input.getRate());
        } catch (IllegalArgumentException e) {
            return MpesaController.badRequest(e.getMessage());
        }
        String sender = input.getSenderAccount().trim();
        String receiver = input.getRecipientAccount().trim();

        String reference = recorder.newReference(12);
        ProviderTransaction txn = recorder.start(ProviderTransaction.EQUITY, ProviderTransaction.EQUITY_INTERNAL_TRANSFER,
                reference, null, sender, amount, rate, "KES");

        EquityAccountSendMoneyRequest request = new EquityAccountSendMoneyRequest();
        EquityAccountSendMoneyRequest.Source source = new EquityAccountSendMoneyRequest.Source();
        source.setAccountNumber(sender);
        source.setCountryCode(countryCode);
        source.setName(merchantName);
        request.setSource(source);

        EquityAccountSendMoneyRequest.Destination dest = new EquityAccountSendMoneyRequest.Destination();
        dest.setType("bank");
        dest.setCountryCode(countryCode);
        dest.setName("Customer");
        dest.setAccountNumber(receiver);
        request.setDestination(dest);

        EquityAccountSendMoneyRequest.Transfer transfer = new EquityAccountSendMoneyRequest.Transfer();
        transfer.setType("EFT");
        transfer.setAmount(PaymentFormat.twoDecimals(amount));
        transfer.setCurrencyCode("KES");
        transfer.setReference(reference);
        transfer.setDate(LocalDate.now(PaymentFormat.NAIROBI).toString());
        transfer.setDescription("FrankMpesa transfer");
        request.setTransfer(transfer);

        try {
            String response = internalTransferService.sendMoney(request);
            JengaCallbacks.JengaResult r = JengaCallbacks.parseInternalTransfer(response);
            if (PaymentStatus.FAILED.equals(r.status())) {
                recorder.rejected(txn, "Equity refused the transfer: " + r.message(), response);
                return MpesaController.providerError("Equity refused the transfer: " + r.message(), reference);
            }
            recorder.accepted(txn, r.providerReference(), null, response);
            recorder.recordPendingSale(txn, "EQUITY_BANK", null, null, sender, receiver, "Equity account transfer");
            recorder.applyJengaTransferResult(txn, r, response);
            return ResponseEntity.ok(response);
        } catch (HttpStatusCodeException e) {
            return refused(txn, "make the transfer", e);
        } catch (RuntimeException e) {
            recorder.rejected(txn, e.getMessage(), null);
            return MpesaController.providerError("Could not reach Equity: " + e.getMessage(), reference);
        }
    }

    // ======================================================================

    private ResponseEntity<?> refused(ProviderTransaction txn, String action, HttpStatusCodeException e) {
        String body = e.getResponseBodyAsString();
        String why = JengaCallbacks.message(body);
        log.warn("Jenga refused to {} for {}: HTTP {} {}", action, txn.getReference(), e.getStatusCode().value(), body);
        recorder.rejected(txn, "Equity could not " + action + ": " + why, body);
        return MpesaController.providerError("Equity could not " + action + ": " + why, txn.getReference());
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " is required");
    }

    private String toJson(Object o) {
        try {
            return json.writeValueAsString(o);
        } catch (Exception e) {
            return String.valueOf(o);
        }
    }
}
