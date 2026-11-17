package com.example.MobileDb.service;

import com.example.MobileDb.entity.kisenyi.Buying;
import com.example.MobileDb.entity.kisenyi.ProviderTransaction;
import com.example.MobileDb.entity.kisenyi.Selling;
import com.example.MobileDb.payments.DarajaCallbacks;
import com.example.MobileDb.payments.JengaCallbacks;
import com.example.MobileDb.payments.PaymentFormat;
import com.example.MobileDb.payments.PaymentStatus;
import com.example.MobileDb.repository.kisenyi.BuyingRepository;
import com.example.MobileDb.repository.kisenyi.ProviderTransactionRepository;
import com.example.MobileDb.repository.kisenyi.SellingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.Optional;

/**
 * Single place where payment results reach the database.
 *
 * Money in (payment prompts): a Buying row is created only once the provider confirms the customer
 * paid, so cancelled or unanswered prompts never show up as buys.
 * Money out (send money): the Selling row is created as PENDING when the provider accepts the
 * request and moved to SUCCESS / FAILED / TIMEOUT when the result arrives.
 * Repeated callbacks (providers retry) are ignored once a transaction is final.
 */
@Service
public class PaymentRecorder {

    private static final Logger log = LoggerFactory.getLogger(PaymentRecorder.class);

    private final ProviderTransactionRepository txns;
    private final BuyingRepository buyings;
    private final SellingRepository sellings;

    public PaymentRecorder(ProviderTransactionRepository txns, BuyingRepository buyings, SellingRepository sellings) {
        this.txns = txns;
        this.buyings = buyings;
        this.sellings = sellings;
    }

    // ------------------------------------------------------------------ requests

    public ProviderTransaction start(String provider, String kind, String reference, String phone, String account,
                                     BigDecimal amount, BigDecimal rate, String currency) {
        ProviderTransaction t = new ProviderTransaction();
        t.setProvider(provider);
        t.setKind(kind);
        t.setReference(reference);
        t.setPhone(phone);
        t.setAccount(account);
        t.setAmount(amount);
        t.setRate(rate);
        t.setCurrency(currency);
        t.setStatus(PaymentStatus.PENDING);
        return txns.save(t);
    }

    /** Unique reference of the given length that is not used by any earlier request. */
    public String newReference(int length) {
        for (int i = 0; i < 20; i++) {
            String ref = PaymentFormat.reference(length);
            if (!txns.existsByReference(ref)) return ref;
        }
        throw new IllegalStateException("Could not generate a unique payment reference");
    }

    public ProviderTransaction accepted(ProviderTransaction t, String providerRequestId, String secondaryId, String response) {
        t.setProviderRequestId(providerRequestId);
        t.setProviderSecondaryId(secondaryId);
        t.setResponsePayload(response);
        return txns.save(t);
    }

    public ProviderTransaction rejected(ProviderTransaction t, String message, String response) {
        t.setStatus(PaymentStatus.FAILED);
        t.setResultDesc(message);
        t.setResponsePayload(response);
        return txns.save(t);
    }

    public Optional<ProviderTransaction> byReference(String reference) {
        return txns.findByReference(reference);
    }

    public Optional<ProviderTransaction> byProviderRequestId(String providerRequestId) {
        return providerRequestId == null ? Optional.empty() : txns.findFirstByProviderRequestId(providerRequestId);
    }

    // ------------------------------------------------------------------ money in

    /** M-Pesa payment prompt result (callback or status query). */
    public ProviderTransaction applyStkResult(ProviderTransaction t, DarajaCallbacks.StkResult r, String raw) {
        if (raw != null) t.setCallbackPayload(raw);
        if (PaymentStatus.isFinal(t.getStatus())) {
            log.info("STK result for {} ignored, already {}", t.getReference(), t.getStatus());
            return txns.save(t);
        }
        t.setStatus(r.status());
        t.setResultCode(String.valueOf(r.resultCode()));
        t.setResultDesc(r.resultDesc());
        if (r.receipt() != null) t.setReceipt(r.receipt());
        if (PaymentStatus.SUCCESS.equals(t.getStatus())) {
            BigDecimal amount = r.amount() != null ? r.amount() : t.getAmount();
            recordBuy(t, amount, "M-Pesa payment " + (t.getReceipt() != null ? t.getReceipt() : ""));
        }
        return txns.save(t);
    }

    /** Equity payment prompt result (callback or IPN). */
    public ProviderTransaction applyJengaStkResult(ProviderTransaction t, JengaCallbacks.JengaResult r, String raw) {
        if (raw != null) t.setCallbackPayload(raw);
        if (PaymentStatus.isFinal(t.getStatus()) || PaymentStatus.PENDING.equals(r.status())) {
            if (!PaymentStatus.isFinal(t.getStatus())) {
                t.setResultCode(r.code());
                t.setResultDesc(r.message());
            }
            return txns.save(t);
        }
        t.setStatus(r.status());
        t.setResultCode(r.code());
        t.setResultDesc(r.message());
        if (r.providerReference() != null) t.setReceipt(r.providerReference());
        if (PaymentStatus.SUCCESS.equals(t.getStatus())) {
            BigDecimal amount = r.amount() != null ? r.amount() : t.getAmount();
            recordBuy(t, amount, "Equity payment " + (t.getReceipt() != null ? t.getReceipt() : t.getReference()));
        }
        return txns.save(t);
    }

    private void recordBuy(ProviderTransaction t, BigDecimal amount, String description) {
        if (t.getBuyingId() != null) return;
        Buying b = new Buying();
        b.setBuyingDate(LocalDate.now(PaymentFormat.NAIROBI));
        b.setBuyingAmount(amount);
        b.setBuyingRate(t.getRate() != null ? t.getRate() : BigDecimal.ZERO);
        b.setBuyerName(t.getPhone());
        b.setTransactingCountry("Kenya");
        b.setBuyDescription(description.trim());
        b.setCheckoutRequestId(t.getProviderRequestId() != null ? t.getProviderRequestId() : t.getReference());
        b = buyings.save(b);
        t.setBuyingId(b.getBuyingId());
        log.info("Recorded buy {} for paid request {}", b.getBuyingId(), t.getReference());
    }

    // ------------------------------------------------------------------ money out

    /** Creates the PENDING sale for an accepted send-money request and links it. */
    public Selling recordPendingSale(ProviderTransaction t, String saleType, String phone, String recipientName,
                                     String senderAccount, String receiverAccount, String description) {
        Selling s = new Selling();
        s.setSellingDate(new Date());
        s.setSellingAmount(t.getAmount() != null ? t.getAmount().doubleValue() : null);
        s.setSellingRate(t.getRate() != null ? t.getRate().doubleValue() : null);
        s.setReceivingCountry("Kenya");
        s.setPhoneNo(phone);
        s.setRecipientName(recipientName);
        s.setSenderAccount(senderAccount);
        s.setReceiverAccount(receiverAccount);
        s.setSellDescription(description);
        s.setCurrency(t.getCurrency());
        s.setTransactionId(t.getReference());
        s.setOriginatorConversationId(t.getReference());
        s.setConversationId(t.getProviderRequestId());
        s.setClientOriginatorId(t.getReference());
        s.setSaleType(saleType);
        s.setStatus(t.getStatus());
        s.setProviderResponse(t.getResponsePayload());
        s = sellings.save(s);
        t.setSellingId(s.getSellingId());
        txns.save(t);
        return s;
    }

    /** M-Pesa B2C result. */
    public ProviderTransaction applyB2cResult(ProviderTransaction t, DarajaCallbacks.B2cResult r, String raw) {
        if (raw != null) t.setCallbackPayload(raw);
        if (PaymentStatus.isFinal(t.getStatus()) && !PaymentStatus.TIMEOUT.equals(t.getStatus())) {
            return txns.save(t);
        }
        t.setStatus(r.status());
        t.setResultCode(String.valueOf(r.resultCode()));
        t.setResultDesc(r.resultDesc());
        if (r.receipt() != null) t.setReceipt(r.receipt());
        updateSale(t, raw, recipientFromPublicName(r.receiverName()));
        return txns.save(t);
    }

    /** M-Pesa B2C queue timeout: Safaricom did not process the request in time. */
    public ProviderTransaction applyB2cTimeout(ProviderTransaction t, String raw) {
        if (raw != null) t.setCallbackPayload(raw);
        if (!PaymentStatus.isFinal(t.getStatus())) {
            t.setStatus(PaymentStatus.TIMEOUT);
            t.setResultDesc("Safaricom queue timeout: the payment was not processed");
            updateSale(t, raw, null);
        }
        return txns.save(t);
    }

    /** Equity send-to-mobile result, or a within-Equity transfer's final response. */
    public ProviderTransaction applyJengaTransferResult(ProviderTransaction t, JengaCallbacks.JengaResult r, String raw) {
        if (raw != null) t.setCallbackPayload(raw);
        if (PaymentStatus.isFinal(t.getStatus()) || PaymentStatus.PENDING.equals(r.status())) {
            return txns.save(t);
        }
        t.setStatus(r.status());
        t.setResultCode(r.code());
        t.setResultDesc(r.message());
        if (r.providerReference() != null) t.setReceipt(r.providerReference());
        updateSale(t, raw, null);
        return txns.save(t);
    }

    private void updateSale(ProviderTransaction t, String raw, String recipientName) {
        if (t.getSellingId() == null) return;
        sellings.findById(t.getSellingId()).ifPresent(s -> {
            s.setStatus(t.getStatus());
            if (t.getReceipt() != null) s.setTransactionId(t.getReceipt());
            if (t.getResultDesc() != null) s.setSellDescription(t.getResultDesc());
            if (recipientName != null) s.setRecipientName(recipientName);
            if (raw != null) s.setProviderResponse(raw);
            if (t.getResultCode() != null) s.setProviderHttpStatus(t.getResultCode());
            sellings.save(s);
        });
    }

    /** "254708374149 - John Doe" -> "John Doe". */
    static String recipientFromPublicName(String publicName) {
        if (publicName == null) return null;
        int dash = publicName.indexOf(" - ");
        return dash >= 0 ? publicName.substring(dash + 3).trim() : publicName.trim();
    }
}
