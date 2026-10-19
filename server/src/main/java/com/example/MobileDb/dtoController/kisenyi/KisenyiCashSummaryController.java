package com.example.MobileDb.dtoController.kisenyi;

import com.example.MobileDb.dto.kisenyi.KisenyiCashSummaryDto;
import com.example.MobileDb.dtoRepository.kisenyi.KisenyiCashSummaryRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cash")
public class KisenyiCashSummaryController {

    @Autowired
    private KisenyiCashSummaryRepository repo;

    @GetMapping("/summary")
    public KisenyiCashSummaryDto getCashSummary(@RequestParam String date) {
        int b = repo.getSellingTotal(date) != null ? repo.getSellingTotal(date) : 0;
        int d = repo.getUgandaOpeningCash(date) != null ? repo.getUgandaOpeningCash(date) : 0;
        int a = repo.getBuyingTotal(date) != null ? repo.getBuyingTotal(date) : 0;
        int c = repo.getExpensesTotal(date) != null ? repo.getExpensesTotal(date) : 0;
        int e = (b + d) - (a + c);
        int f = repo.getFinalClosingCash(date) != null ? repo.getFinalClosingCash(date) : 0;

        KisenyiCashSummaryDto dto = new KisenyiCashSummaryDto();
        dto.setSellingTotal(b);
        dto.setUgandaOpeningCash(d);
        dto.setBuyingTotal(a);
        dto.setExpensesTotal(c);
        dto.setNetCash(e);
        dto.setFinalClosingCash(f);
        return dto;
    }
}
