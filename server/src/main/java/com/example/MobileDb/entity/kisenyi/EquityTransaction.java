package com.example.MobileDb.entity.kisenyi;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "equity_transactions")
public class EquityTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String transactionId;
    private String senderAccount;
    private String receiverAccount;
    private Double amount;
    private String currency;
    private String status;
    private LocalDateTime createdAt;

    // Constructors
    public EquityTransaction() {}

    public EquityTransaction(String transactionId, String senderAccount, String receiverAccount,
                             Double amount, String currency, String status) {
        this.transactionId = transactionId;
        this.senderAccount = senderAccount;
        this.receiverAccount = receiverAccount;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and setters
    // ...
}
