package com.example.MobileDb.controller.kisenyi;

import com.example.MobileDb.entity.kisenyi.Buying;
import com.example.MobileDb.repository.kisenyi.BuyingRepository;

import com.example.MobileDb.entity.kisenyi.Selling;
import com.example.MobileDb.repository.kisenyi.SellingRepository;

import com.example.MobileDb.service.JengaStkPushService;
import com.example.MobileDb.service.dto.EquityStkPushRequest;

import com.example.MobileDb.service.JengaSendMoneyService;
import com.example.MobileDb.service.dto.JengaSendMoneyRequest;

import com.example.MobileDb.service.EquityAccountSendMoneyService;
import com.example.MobileDb.service.dto.EquityAccountSendMoneyRequest;

import com.example.MobileDb.service.dto.JengaCallbackResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.logging.Logger;
import java.util.Optional;


@RestController
@RequestMapping("/equity")
public class EquityController {

    private static final Logger logger = Logger.getLogger(EquityController.class.getName());

    private final JengaStkPushService JengaStkPushService;
    private final JengaSendMoneyService jengaSendMoneyService;
    private final EquityAccountSendMoneyService equityAccountSendMoneyService; // ADD THIS
    private final BuyingRepository repo;
    private final SellingRepository sellingRepo;

    public EquityController(JengaStkPushService JengaStkPushService, 
                            JengaSendMoneyService jengaSendMoneyService,
                            EquityAccountSendMoneyService equityAccountSendMoneyService, // ADD THIS
                            BuyingRepository repo,
                            SellingRepository sellingRepo) {
        this.JengaStkPushService = JengaStkPushService;
        this.jengaSendMoneyService = jengaSendMoneyService;
        this.equityAccountSendMoneyService = equityAccountSendMoneyService; // ADD THIS
        this.repo = repo;
        this.sellingRepo = sellingRepo;
    }


    // DTO for user input (only phone, amount, rate)
   
    
    public static class UserStkPushInput {
        private String phoneNumber;
        private String recipientPhone; // instead of phoneNumber
        private String amount;
        private String rate;
        private String telco;   
        private String recipientAccount; // Changed from AccountNumber to match Android
        private String senderAccount;  

        // Getters and Setters
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


    
    
    // bankcallback endpoint for Jenga HQ


    
    @PostMapping("/sendToAccount")
    public ResponseEntity<String> sendToAccount(@RequestBody UserStkPushInput input) {
        try {
            EquityAccountSendMoneyRequest request = new EquityAccountSendMoneyRequest();
            String reference = "BANK" + System.currentTimeMillis();

            // Source Details (Your Equity Account)
            EquityAccountSendMoneyRequest.Source source = new EquityAccountSendMoneyRequest.Source();
            source.setAccountNumber(input.getSenderAccount()); 
            source.setCountryCode("KE");
            source.setName("Winter transactions");
            request.setSource(source);

            // Destination Details (Recipient Bank Account)
            EquityAccountSendMoneyRequest.Destination dest = new EquityAccountSendMoneyRequest.Destination();
            dest.setType("bank");
            dest.setCountryCode("KE");
            dest.setName("Recipient Name");
         // Inside sendToAccount method
            dest.setAccountNumber(input.getRecipientAccount()); // Use the corrected getter

            request.setDestination(dest);

            // Transfer Details
            EquityAccountSendMoneyRequest.Transfer transfer = new EquityAccountSendMoneyRequest.Transfer();
            transfer.setType("EFT"); 
            transfer.setAmount(String.format("%.2f", Double.valueOf(input.getAmount())));
            transfer.setCurrencyCode("KES");
            transfer.setReference(reference);
            transfer.setDate(LocalDate.now().toString());
            transfer.setDescription("Internal Bank Transfer");
            request.setTransfer(transfer);

            // Call Service
            String response = equityAccountSendMoneyService.sendMoney(request);

            // Record in Selling Table
            Selling selling = new Selling();
            selling.setSellingDate(new java.util.Date());
            selling.setSellingAmount(Double.valueOf(input.getAmount()));
            selling.setSellingRate(Double.valueOf(input.getRate()));
            selling.setPhoneNo("BANK_TRANSFER"); // Or recipient account if you have a field
            selling.setTransactionId(reference); 
            sellingRepo.save(selling);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Bank Transfer Failed: " + e.getMessage());
        }
    }

   
    
    
    

    // sendmoneycallback endpoint for Jenga HQ
    
    @PostMapping("/sendmoneycallback")
    public ResponseEntity<String> handleSendMoneyCallback(@RequestBody JengaCallbackResponse callback) {
        String ref = callback.getEffectiveReference();
        String status = callback.getEffectiveStatus();

        logger.info("SendMoney Callback - Ref: " + ref + " | Status: " + status);

        if (ref != null) {
            // Use the Optional from your repository
            Optional<Selling> sellingOpt = sellingRepo.findByTransactionId(ref);
            
            if (sellingOpt.isPresent()) {
                Selling txn = sellingOpt.get();
                
                // Check if status is SUCCESS or CREDITED
                boolean isSuccess = "SUCCESS".equalsIgnoreCase(status) || "CREDITED".equalsIgnoreCase(status);
                
                // Assuming you want to update the transaction ID or a status field
                // txn.setTransactionId(callback.getTransactionId()); // if Jenga sends a new ID
                
                sellingRepo.save(txn);
                logger.info("Successfully updated Selling record for Ref: " + ref);
            } else {
                logger.warning("No Selling record found for Reference: " + ref);
            }
        }
        return ResponseEntity.ok("Callback processed");
    }


    
    


    @PostMapping("/sendmoney")
    public ResponseEntity<String> sendMoney(@RequestBody UserStkPushInput input) {
        try {
            // 1. Prepare Jenga Request
            JengaSendMoneyRequest request = new JengaSendMoneyRequest();
            
            // Generate a unique reference for Jenga and for DB lookup
            String reference = "REF" + System.currentTimeMillis();
            
            // Source: Using the dynamic 'senderAccount' from the phone
            JengaSendMoneyRequest.Source source = new JengaSendMoneyRequest.Source();
            source.setAccountNumber(input.getSenderAccount()); 
            source.setCountryCode("KE");
            source.setName("Winter transactions");
            request.setSource(source);

            // Destination: Using 'recipientPhone' from the phone
            JengaSendMoneyRequest.Destination dest = new JengaSendMoneyRequest.Destination();
            dest.setType("Mobile");
            dest.setCountryCode("KE");
            dest.setName("Recipient"); 
            
            // Normalize phone to 254 format
            String phone = input.getRecipientPhone().trim();
            if (phone.startsWith("0")) {
                phone = "254" + phone.substring(1);
            } else if (phone.startsWith("+")) {
                phone = phone.substring(1);
            }
            dest.setMobileNumber(phone);
         // Inside your sendMoney method
            String wallet = input.getTelco().trim();
            if (wallet.equalsIgnoreCase("MPESA")) {
                dest.setWalletName("MPESA"); // Try "MPESA" first, as it is the standard for SendMoney
            } 
            else if (wallet.equalsIgnoreCase("AIRTELMONEY")) {
                dest.setWalletName("AIRTELMONEY"); // Try "MPESA" first, as it is the standard for SendMoney
            }
            else {
                dest.setWalletName(wallet);
            }

            request.setDestination(dest);

            // Transfer details
            JengaSendMoneyRequest.Transfer transfer = new JengaSendMoneyRequest.Transfer();
            transfer.setAmount(input.getAmount());
            transfer.setCurrencyCode("KES");
            transfer.setReference(reference);
            transfer.setDate(LocalDate.now().toString());
            transfer.setDescription("Mobile Remittance");
            transfer.setType("MobileWallet");
            
            // Callback URL for SendMoney
            transfer.setCallbackUrl("https://geomagnetic-confessedly-juanita.ngrok-free.dev/equity/sendmoneycallback");
            request.setTransfer(transfer);

            // 2. Call Jenga Service
            // This generates the signature using: amount + currencyCode + reference + senderAccount
            String response = jengaSendMoneyService.sendMoney(request);
            logger.info("SendMoney Jenga Response: " + response);

            // 3. Save to 'selling' table
            Selling selling = new Selling();
            selling.setSellingDate(new java.util.Date()); 
            selling.setSellingAmount(Double.valueOf(input.getAmount()));
            selling.setSellingRate(Double.valueOf(input.getRate()));
            selling.setReceivingCountry("Kenya");
            selling.setPhoneNo(phone);
            
            // Save the reference in Transaction_ID for callback correlation
            selling.setTransactionId(reference); 
            
            sellingRepo.save(selling);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.severe("SendMoney Error: " + e.getMessage());
            return ResponseEntity.status(500).body("SendMoney Failed: " + e.getMessage());
        }
    }

    
    
    


    // stkpushcallback endpoint for Jenga HQ
    @PostMapping("/stkpushcallback")
    public ResponseEntity<String> handleCallback(@RequestBody JengaCallbackResponse callback) {
        String ref = callback.getEffectiveReference();
        String status = callback.getEffectiveStatus();
        
        logger.info("Processing Callback - Ref: " + ref + " | Status: " + status);

        if (ref != null) {
            Buying txn = repo.findByCheckoutRequestId(ref);
            if (txn != null) {
                // Update status based on either "SUCCESS" (IPN) or "0"/"true" (Flat)
                boolean isSuccess = "SUCCESS".equalsIgnoreCase(status) || "true".equalsIgnoreCase(status) || "0".equals(status);
                txn.setBuyDescription(isSuccess ? "Paid Successfully" : "Failed: " + status);
                repo.save(txn);
            }
        }

        return ResponseEntity.ok("Callback processed");
    }


    @PostMapping("/stkpush")
    public ResponseEntity<String> initiateEquityStkPush(@RequestBody UserStkPushInput input) {
        EquityStkPushRequest request = new EquityStkPushRequest();

        // Merchant details must match HQ dashboard exactly
		
		  EquityStkPushRequest.Merchant merchant = new EquityStkPushRequest.Merchant();
		  merchant.setAccountNumber("1450160649886");
		  merchant.setCountryCode("KE");
		  merchant.setName("Winter transactions");//Winter transactions   Jenga Test
		  request.setMerchant(merchant);
		 

        EquityStkPushRequest.Payment payment = new EquityStkPushRequest.Payment();
       // payment.setRef("TXN" + System.currentTimeMillis());
        payment.setRef("TXN" + System.currentTimeMillis());
        
        // Format amount with two decimals
        BigDecimal amount = new BigDecimal(input.getAmount());
		
		  String formattedAmount = String.format("%.0f", amount.doubleValue());
		  payment.setAmount(formattedAmount);
		 
        
        

        payment.setCurrency("KES");
        
        String Telco = input.getTelco().trim();
        payment.setTelco(Telco); //Equitel && Safaricom

        // Normalize phone number to international format
        String phone = input.getPhoneNumber();
        if (phone.startsWith("0")) {
            phone = "254" + phone.substring(1);
        }
        payment.setMobileNumber(phone);

        payment.setDate(LocalDate.now().toString());
        // Use your ngrok callback URL
        payment.setCallBackUrl("https://geomagnetic-confessedly-juanita.ngrok-free.dev/equity/stkpushcallback");
        payment.setPushType("STK");
        request.setPayment(payment);
        
       
        logger.info("request: " + request);

     // Inside EquityController.java
        try {
            // Call service
            String response = JengaStkPushService.initiateStkPush(request);
            logger.info("Response: " + response);
            
            // Save transaction only if successful
            Buying buying = new Buying();
            buying.setBuyingAmount(amount);
            buying.setTransactingCountry("Kenya");
            buying.setBuyerName(merchant.getName());
            buying.setCheckoutRequestId(response);
            buying.setBuyingDate(LocalDate.now());
         // Inside initiateEquityStkPush
            buying.setCheckoutRequestId(payment.getRef()); // Save the clean Reference ID

            buying.setBuyingRate(new BigDecimal(input.getRate()));
            repo.save(buying);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.severe("STK Push failed to reach debug line: " + e.getMessage());
            return ResponseEntity.status(401).body("Error: " + e.getMessage());
        }

    }
}
