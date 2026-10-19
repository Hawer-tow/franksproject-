package com.example.MobileDb.controller.kisenyi;

import com.example.MobileDb.config.DarajaProperties;
import com.example.MobileDb.entity.kisenyi.Selling;
import com.example.MobileDb.repository.kisenyi.SellingRepository;
import com.example.MobileDb.service.MpesaSendmoneyService;
import com.example.MobileDb.service.JengaPaymentService;
import com.example.MobileDb.service.dto.PaymentRequest;
import com.example.MobileDb.service.dto.PaymentResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.transaction.Transactional;

import java.util.*;

@RestController
@RequestMapping("/selling")
public class SellingController {

    private static final Logger log = LoggerFactory.getLogger(SellingController.class);

    private final SellingRepository repo;
    private final JengaPaymentService jengaPaymentService;
    private final MpesaSendmoneyService darajaPaymentService;
    private final DarajaProperties props;

    public SellingController(SellingRepository repo,
                             JengaPaymentService jengaPaymentService,
                             MpesaSendmoneyService darajaPaymentService,
                             DarajaProperties props) {
        this.repo = repo;
        this.jengaPaymentService = jengaPaymentService;
        this.darajaPaymentService = darajaPaymentService;
        this.props = props;
    }

    @GetMapping
    public List<Selling> getAll() {
        return repo.findAll();
    }

    @PostMapping
    public ResponseEntity<Selling> addSelling(@RequestBody Selling selling) {
        if (selling.getSaleType() == null) {
            selling.setSaleType("USER_INITIATED");
        }
        if (selling.getSellingDate() == null) {
            selling.setSellingDate(new Date());
        }
        Selling saved = repo.save(selling);
        return ResponseEntity.ok(saved);
    }

	/*
	 * @PostMapping("/sendMoney/jenga") public ResponseEntity<Selling>
	 * sendMoneyJenga(@RequestBody Selling selling) { selling.setSaleType("BANK");
	 * Selling processed = jengaPaymentService.sendMoney(selling); return
	 * ResponseEntity.ok(processed); }
	 */


  

    @PutMapping("/{id}")
    public ResponseEntity<Selling> update(@PathVariable Integer id, @RequestBody Selling selling) {
        return repo.findById(id).map(existing -> {
            existing.setSellingDate(selling.getSellingDate());
            existing.setSellingAmount(selling.getSellingAmount());
            existing.setSellingRate(selling.getSellingRate());
            existing.setReceivingCountry(selling.getReceivingCountry());
            existing.setPhoneNo(selling.getPhoneNo());
            existing.setRecipientName(selling.getRecipientName());
            existing.setSellDescription(selling.getSellDescription());
            existing.setSenderAccount(selling.getSenderAccount());
            existing.setReceiverAccount(selling.getReceiverAccount());
            existing.setBankCode(selling.getBankCode());
            existing.setCurrency(selling.getCurrency());
            existing.setStatus(selling.getStatus());
            existing.setSaleType(selling.getSaleType());
            return ResponseEntity.ok(repo.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/range/{days}")
    public List<Selling> getSellingInRange(@PathVariable("days") int days) {
        return repo.findSellingInRange(days);
    }

    @GetMapping("/monthly")
    public List<Selling> getSellingMonthly() {
        return repo.findSellingMonthly();
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        repo.flush();
        return ResponseEntity.noContent().build();
    }
}
