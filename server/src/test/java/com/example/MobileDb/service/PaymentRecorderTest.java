package com.example.MobileDb.service;

import com.example.MobileDb.entity.kisenyi.Buying;
import com.example.MobileDb.entity.kisenyi.ProviderTransaction;
import com.example.MobileDb.entity.kisenyi.Selling;
import com.example.MobileDb.payments.DarajaCallbacks;
import com.example.MobileDb.payments.JengaCallbacks;
import com.example.MobileDb.payments.PaymentStatus;
import com.example.MobileDb.repository.kisenyi.BuyingRepository;
import com.example.MobileDb.repository.kisenyi.ProviderTransactionRepository;
import com.example.MobileDb.repository.kisenyi.SellingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PaymentRecorderTest {

    private ProviderTransactionRepository txns;
    private BuyingRepository buyings;
    private SellingRepository sellings;
    private PaymentRecorder recorder;

    @BeforeEach
    void setUp() {
        txns = mock(ProviderTransactionRepository.class);
        buyings = mock(BuyingRepository.class);
        sellings = mock(SellingRepository.class);
        when(txns.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(buyings.save(any())).thenAnswer(inv -> {
            Buying b = inv.getArgument(0);
            b.setBuyingId(41);
            return b;
        });
        when(sellings.save(any())).thenAnswer(inv -> {
            Selling s = inv.getArgument(0);
            s.setSellingId(77);
            return s;
        });
        recorder = new PaymentRecorder(txns, buyings, sellings);
    }

    private ProviderTransaction pendingPrompt() {
        ProviderTransaction t = new ProviderTransaction();
        t.setProvider(ProviderTransaction.MPESA);
        t.setKind(ProviderTransaction.MPESA_STK_PUSH);
        t.setReference("ABC123");
        t.setProviderRequestId("ws_CO_1");
        t.setPhone("254708374149");
        t.setAmount(new BigDecimal("100"));
        t.setRate(new BigDecimal("28.5"));
        t.setStatus(PaymentStatus.PENDING);
        return t;
    }

    @Test
    void paidPromptRecordsOneBuyAndIgnoresRepeats() {
        ProviderTransaction t = pendingPrompt();
        DarajaCallbacks.StkResult paid = new DarajaCallbacks.StkResult("m1", "ws_CO_1", 0, "Success",
                new BigDecimal("100"), "NLJ7RT61SV", "254708374149");

        recorder.applyStkResult(t, paid, "{}");
        recorder.applyStkResult(t, paid, "{}"); // Safaricom retry

        ArgumentCaptor<Buying> buy = ArgumentCaptor.forClass(Buying.class);
        verify(buyings, times(1)).save(buy.capture());
        assertEquals(PaymentStatus.SUCCESS, t.getStatus());
        assertEquals("NLJ7RT61SV", t.getReceipt());
        assertEquals(41, t.getBuyingId());
        assertEquals(0, new BigDecimal("28.5").compareTo(buy.getValue().getBuyingRate()));
        assertEquals("254708374149", buy.getValue().getBuyerName());
    }

    @Test
    void cancelledPromptRecordsNoBuy() {
        ProviderTransaction t = pendingPrompt();
        recorder.applyStkResult(t, new DarajaCallbacks.StkResult("m1", "ws_CO_1", 1032,
                "Request cancelled by user", null, null, null), "{}");
        verify(buyings, never()).save(any());
        assertEquals(PaymentStatus.CANCELLED, t.getStatus());
    }

    @Test
    void equityPendingCallbackDoesNotCloseThePrompt() {
        ProviderTransaction t = pendingPrompt();
        t.setProvider(ProviderTransaction.EQUITY);
        t.setKind(ProviderTransaction.EQUITY_STK_PUSH);
        recorder.applyJengaStkResult(t, new JengaCallbacks.JengaResult("ABC123", PaymentStatus.PENDING, "0",
                "Pending", null, null), "{}");
        assertEquals(PaymentStatus.PENDING, t.getStatus());
        verify(buyings, never()).save(any());
    }

    @Test
    void b2cResultMarksTheSale() {
        ProviderTransaction t = pendingPrompt();
        t.setKind(ProviderTransaction.MPESA_B2C);
        t.setReference("FMREF");
        t.setSellingId(77);
        Selling sale = new Selling();
        sale.setSellingId(77);
        sale.setStatus(PaymentStatus.PENDING);
        when(sellings.findById(77)).thenReturn(Optional.of(sale));

        recorder.applyB2cResult(t, new DarajaCallbacks.B2cResult("FMREF", "AG_1", "NLJ41HAY6Q", 0, "Success",
                new BigDecimal("100"), "NLJ41HAY6Q", "254708374149 - John Doe"), "{}");

        assertEquals(PaymentStatus.SUCCESS, sale.getStatus());
        assertEquals("NLJ41HAY6Q", sale.getTransactionId());
        assertEquals("John Doe", sale.getRecipientName());
    }

    @Test
    void b2cTimeoutMarksPendingSaleOnly() {
        ProviderTransaction t = pendingPrompt();
        t.setKind(ProviderTransaction.MPESA_B2C);
        recorder.applyB2cTimeout(t, "{}");
        assertEquals(PaymentStatus.TIMEOUT, t.getStatus());

        t.setStatus(PaymentStatus.SUCCESS);
        recorder.applyB2cTimeout(t, "{}");
        assertEquals(PaymentStatus.SUCCESS, t.getStatus()); // a late timeout never undoes a success
    }
}
