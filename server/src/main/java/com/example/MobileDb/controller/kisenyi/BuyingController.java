package com.example.MobileDb.controller.kisenyi;

import com.example.MobileDb.entity.kisenyi.Buying;
import com.example.MobileDb.repository.kisenyi.BuyingRepository;
import com.example.MobileDb.service.JengaPaymentService;
import com.example.MobileDb.service.JengaStkPushService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.math.BigDecimal;


@RestController
@RequestMapping("/buying")
public class BuyingController {

	   private final BuyingRepository repo;
	    private final JengaPaymentService jengaPaymentService; // add this

	    // Constructor injection
	    public BuyingController(BuyingRepository repo, JengaPaymentService jengaPaymentService) {
	        this.repo = repo;
	        this.jengaPaymentService = jengaPaymentService;
	    }

    @GetMapping
    public List<Buying> getAll() {
        return repo.findAll();
    }

    @GetMapping("/range/{days}")
    public List<Buying> getBuyingInRange(@PathVariable int days) {
        return repo.findBuyingInRange(days);
    }

    @GetMapping("/monthly")
    public List<Buying> getMonthlyBuying() {
        return repo.findBuyingMonthly();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Buying> getById(@PathVariable Integer id) {
        return repo.findById(id).map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Buying add(@RequestBody Buying buying) {
        return repo.save(buying);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Buying> update(@PathVariable Integer id, @RequestBody Buying buying) {
        return repo.findById(id).map(existing -> {
            existing.setBuyingDate(buying.getBuyingDate());
            existing.setBuyingAmount(buying.getBuyingAmount());
            existing.setBuyingRate(buying.getBuyingRate());
            existing.setTransactingCountry(buying.getTransactingCountry());
            existing.setBuyerName(buying.getBuyerName());
            existing.setBuyDescription(buying.getBuyDescription());
            return ResponseEntity.ok(repo.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
    
  
    

}


