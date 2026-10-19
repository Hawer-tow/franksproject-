package com.example.mobiledbclient;

import com.example.mobiledbclient.ui.Kisenyi.buys.Buying;
import com.example.mobiledbclient.ui.Kisenyi.expenses.Expense;
import com.example.mobiledbclient.ui.Kisenyi.kisenyidtos.KisenyiCashSummaryDto;
import com.example.mobiledbclient.ui.Kisenyi.openandclosings.OpenAndClose;
import com.example.mobiledbclient.ui.Kisenyi.sales.Selling;
import com.example.mobiledbclient.ui.MajorTransactions.EquitySales.EquityAccountSendMoneyRequest;
import com.example.mobiledbclient.ui.MajorTransactions.EquitySales.SendMoneyRequest;
import com.example.mobiledbclient.ui.MajorTransactions.MpesaBuys.StkPushRequest;
import com.example.mobiledbclient.ui.MajorTransactions.MpesaBuys.StkPushResponse;
import com.example.mobiledbclient.ui.TownBranch.Commission.Commission;
import com.example.mobiledbclient.ui.TownBranch.Deposit.Deposit;
import com.example.mobiledbclient.ui.TownBranch.Withdraw.Withdraw;
import com.example.mobiledbclient.ui.OperationRates.kenya.Kenya2UgxRate;
import com.example.mobiledbclient.ui.OperationRates.kenya.Ugx2KenyaRate;
import com.example.mobiledbclient.ui.OperationRates.congo.Congo2UgxRate;
import com.example.mobiledbclient.ui.OperationRates.congo.Ugx2CongoRate;
import com.example.mobiledbclient.ui.OperationRates.tanzania.Ugx2TanzaniaRate;
import com.example.mobiledbclient.ui.OperationRates.tanzania.Tanzania2UgxRate;
import com.example.mobiledbclient.ui.OperationRates.rwanda.Rwanda2UgxRate;
import com.example.mobiledbclient.ui.OperationRates.rwanda.Ugx2RwandaRate;

import com.example.mobiledbclient.Login.User.User;
import com.example.mobiledbclient.Login.LoginRequest;
import com.example.mobiledbclient.Login.PasswordResetRequest;
import com.example.mobiledbclient.Login.LoginResponse;
import com.example.mobiledbclient.ui.Customers.allcustomers.Customers;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Tag;

public interface ApiService {
    @GET("expenses")
    Call<List<Expense>> getExpenses();
    
    @GET("expenses/range")
    Call<List<Expense>> getExpensesInRange(@Query("days") int days);

    @GET("expenses/monthly")
    Call<List<Expense>> getMonthlyExpenses();

    @POST("expenses")
    Call<Expense> addExpense(@Body Expense expense);

    @PUT("expenses/{id}")
    Call<Expense> updateExpense(@Path("id") int id, @Body Expense expense);

    @DELETE("expenses/{id}")
    Call<Void> deleteExpense(@Path("id") int id);

    // Selling endpoints
    @GET("/selling")
    Call<List<Selling>> getSelling();

    @POST("/selling")
    Call<Selling> addSelling(@Body Selling selling);

    @PUT("/selling/{id}")
    Call<Selling> updateSelling(@Path("id") Integer id, @Body Selling selling);

    @DELETE("/selling/{id}")
    Call<Void> deleteSelling(@Path("id") Integer id);
    
    @GET("/selling/range/{days}")
    Call<List<Selling>> getSellingInRange(@Path("days") int days);

    @GET("/selling/monthly")
    Call<List<Selling>> getMonthlySelling();


    // Buying endpoints
    @GET("buying")
    Call<List<Buying>> getBuying();

    @POST("buying")
    Call<Buying> addBuying(@Body Buying buying);

    @PUT("buying/{id}")
    Call<Buying> updateBuying(@Path("id") int id, @Body Buying buying);

    @DELETE("buying/{id}")
    Call<Void> deleteBuying(@Path("id") int id);
    
    @GET("buying/range/{days}")
    Call<List<Buying>> getBuyingInRange(@Path("days") int days);

    @GET("buying/monthly")
    Call<List<Buying>> getMonthlyBuying();

    // OpenAndClose endpoints
    @GET("open_and_close")
    Call<List<OpenAndClose>> getOpenAndClose();

    @POST("open_and_close")
    Call<OpenAndClose> addOrUpdateOpenAndClose(@Body OpenAndClose openAndClose);

    @DELETE("open_and_close/{date}")
    Call<Void> deleteOpenAndClose(@Path("date") String date);
    
    @GET("open_and_close/range/{days}")
    Call<List<OpenAndClose>> getOpenAndCloseInRange(@Path("days") int days);

    @GET("open_and_close/monthly")
    Call<List<OpenAndClose>> getMonthlyOpenAndClose();


    // Withdraw endpoints
    @GET("withdraw")
    Call<List<Withdraw>> getWithdraw();

    @POST("withdraw")
    Call<Withdraw> addOrUpdateWithdraw(@Body Withdraw withdraw);

    @DELETE("withdraw/{tid}")
    Call<Void> deleteWithdraw(@Path("tid") String tid);

    // Deposit endpoints
    @GET("deposit")
    Call<List<Deposit>> getDeposit();

    @POST("deposit")
    Call<Deposit> addOrUpdateDeposit(@Body Deposit deposit);

    @DELETE("deposit/{tid}")
    Call<Void> deleteDeposit(@Path("tid") String tid);

    // Commission endpoints
    @GET("commission")
    Call<List<Commission>> getCommission();

    @GET("commission/{cid}")
    Call<Commission> getCommissionById(@Path("cid") String cid);

    @POST("commission")
    Call<Commission> addOrUpdateCommission(@Body Commission commission);

    @DELETE("commission/{cid}")
    Call<Void> deleteCommission(@Path("cid") String cid);


    @GET("cash/summary")
    Call<KisenyiCashSummaryDto> getCashSummary(@Query("date") String date);

    // Rate endpoints
    @GET("api/kenya2ugx")
    Call<List<Kenya2UgxRate>> getKenya2UgxRates();

    @POST("api/kenya2ugx")
    Call<Kenya2UgxRate> addKenya2UgxRate(@Body Kenya2UgxRate rate);

    @GET("api/kenya2ugx/{date}")
    Call<Kenya2UgxRate> getKenya2UgxRateByDate(@Path("date") String date);

    @GET("api/ugx2kenya")
    Call<List<Ugx2KenyaRate>> getUgx2KenyaRates();

    @POST("api/ugx2kenya")
    Call<Ugx2KenyaRate> addUgx2KenyaRate(@Body Ugx2KenyaRate rate);

    @GET("api/ugx2kenya/{date}")
    Call<Ugx2KenyaRate> getUgx2KenyaRateByDate(@Path("date") String date);

    @GET("api/congo2ugx")
    Call<List<Congo2UgxRate>> getCongo2UgxRates();

    @POST("api/congo2ugx")
    Call<Congo2UgxRate> addCongo2UgxRate(@Body Congo2UgxRate rate);

    @GET("api/congo2ugx/{date}")
    Call<Congo2UgxRate> getCongo2UgxRateByDate(@Path("date") String date);

    @GET("api/ugx2congo")
    Call<List<Ugx2CongoRate>> getUgx2CongoRates();

    @POST("api/ugx2congo")
    Call<Ugx2CongoRate> addUgx2CongoRate(@Body Ugx2CongoRate rate);

    @GET("api/ugx2congo/{date}")
    Call<Ugx2CongoRate> getUgx2CongoRateByDate(@Path("date") String date);

    @GET("api/ugx2tanzania")
    Call<List<Ugx2TanzaniaRate>> getUgx2TanzaniaRates();

    @POST("api/ugx2tanzania")
    Call<Ugx2TanzaniaRate> addUgx2TanzaniaRate(@Body Ugx2TanzaniaRate rate);

    @GET("api/ugx2tanzania/{date}")
    Call<Ugx2TanzaniaRate> getUgx2TanzaniaRateByDate(@Path("date") String date);

    @GET("api/tanzania2ugx")
    Call<List<Tanzania2UgxRate>> getTanzania2UgxRates();

    @POST("api/tanzania2ugx")
    Call<Tanzania2UgxRate> addTanzania2UgxRate(@Body Tanzania2UgxRate rate);

    @GET("api/tanzania2ugx/{date}")
    Call<Tanzania2UgxRate> getTanzania2UgxRateByDate(@Path("date") String date);

    @GET("api/ugx2rwanda")
    Call<List<Ugx2RwandaRate>> getUgx2RwandaRates();

    @POST("api/ugx2rwanda")
    Call<Ugx2RwandaRate> addUgx2RwandaRate(@Body Ugx2RwandaRate rate);

    @GET("api/ugx2rwanda/{date}")
    Call<Ugx2RwandaRate> getUgx2RwandaRateByDate(@Path("date") String date);

    @GET("api/rwanda2ugx")
    Call<List<Rwanda2UgxRate>> getRwanda2UgxRates();

    @POST("api/rwanda2ugx")
    Call<Rwanda2UgxRate> addRwanda2UgxRate(@Body Rwanda2UgxRate rate);

    @GET("api/rwanda2ugx/{date}")
    Call<Rwanda2UgxRate> getRwanda2UgxRateByDate(@Path("date") String date);


    // customer endpoints
    @GET("api/customers")
    Call<List<Customers>> getAllCustomers();

    @GET("api/customers/{id}")
    Call<Customers> getCustomerById(@Path("id") int id);

    @POST("api/customers")
    Call<Customers> addCustomer(@Body Customers customer);

    @PUT("api/customers/{id}")
    Call<Customers> updateCustomer(@Path("id") int id, @Body Customers customer);

    @DELETE("api/customers/{id}")
    Call<Void> deleteCustomer(@Path("id") int id, @Query("deletedBy") String deletedBy);

    @DELETE("api/customers")
    Call<Void> clearAllCustomers();

    // user endpoints
    @GET("api/users")
    Call<List<User>> getAllUsers();

    @GET("api/users/{id}")
    Call<User> getUserById(@Path("id") int id);

    @POST("api/users/login")
    Call<LoginResponse> login(@Body LoginRequest request, @Tag LoginRequest tag);

    @POST("api/users/{id}/enable-biometrics")
    Call<Void> enableBiometrics(@Path("id") int userId, @Body Map<String, String> data);

    @PUT("api/users/{id}/biometric-status")
    Call<Void> updateBiometricStatus(@Path("id") int userId, @Body Map<String, Boolean> status);

    @POST("api/users")
    Call<User> createUser(@Body User user);

    @POST("api/users/{id}/set-password")
    Call<Void> setPassword(@Path("id") int userId, @Body PasswordResetRequest request, @Tag PasswordResetRequest tag);
    
    @GET("api/public-key")
    Call<Map<String, String>> getPublicKey();


    // Safaricom transactions
    @POST("/mpesa/sendMoney")
    Call<Selling> sendMoneyDaraja(@Body Selling selling);
    
    @POST("/mpesa/stkpush")
    Call<StkPushResponse> initiateStkPush(@Body StkPushRequest request);


    // Equity Transactions
    @POST("/equity/stkpush")
    Call<com.example.mobiledbclient.ui.MajorTransactions.EquityBuys.StkPushResponse> initiateEquityStkPush(
            @Body com.example.mobiledbclient.ui.MajorTransactions.EquityBuys.StkPushRequest request
    );
    
    @POST("/equity/sendToAccount")
    Call<String> sendToAccount(@Body EquityAccountSendMoneyRequest request);
    
    @POST("/equity/sendmoney")
    Call<String> sendMoney(@Body SendMoneyRequest request);
}
