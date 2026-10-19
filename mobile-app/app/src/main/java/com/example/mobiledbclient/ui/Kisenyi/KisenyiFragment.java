package com.example.mobiledbclient.ui.Kisenyi;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobiledbclient.Login.SessionManager;
import com.example.mobiledbclient.ui.Kisenyi.buys.BuyingAdapter;
import com.example.mobiledbclient.ui.Kisenyi.buys.BuyingEntity;
import com.example.mobiledbclient.ui.Kisenyi.buys.Buying_db_instance;
import com.example.mobiledbclient.ui.Kisenyi.buys.Buying_queries;
import com.example.mobiledbclient.ui.Kisenyi.expenses.ExpenseAdapter;
import com.example.mobiledbclient.ui.Kisenyi.expenses.ExpenseEntity;
import com.example.mobiledbclient.ui.Kisenyi.expenses.expense_db_instance;
import com.example.mobiledbclient.ui.Kisenyi.expenses.expense_queries;
import com.example.mobiledbclient.ui.Kisenyi.openandclosings.OpenAndCloseAdapter;
import com.example.mobiledbclient.ui.Kisenyi.openandclosings.OpenAndCloseEntity;
import com.example.mobiledbclient.ui.Kisenyi.openandclosings.OpenAndClose_db_instance;
import com.example.mobiledbclient.ui.Kisenyi.openandclosings.OpenAndClose_queries;
import com.example.mobiledbclient.ui.Kisenyi.sales.SellingAdapter;
import com.example.mobiledbclient.ui.Kisenyi.sales.SellingEntity;
import com.example.mobiledbclient.ui.Kisenyi.sales.Selling_db_instance;
import com.example.mobiledbclient.ui.Kisenyi.sales.Selling_queries;

import java.util.ArrayList;
import java.util.List;

import com.example.mobiledbclient.ApiClient;
import com.example.mobiledbclient.ApiService;
import com.example.mobiledbclient.R;
import com.example.mobiledbclient.SyncWorker;

import com.google.android.material.navigation.NavigationView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import android.widget.FrameLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.concurrent.Executors;
import android.widget.PopupMenu;

import com.example.mobiledbclient.ui.Kisenyi.Z_kisenyiFunctionsHandler.*;

public class KisenyiFragment extends Fragment {

    private ImageButton btnFetchExpenses;
    private ImageButton FinalUgandacashbtn;
    private FloatingActionButton Rowadderbtn;
    private TextView status;
    private RecyclerView recyclerView;

    private ExpenseAdapter expenseAdapter;
    private SellingAdapter sellingAdapter;
    private BuyingAdapter buyingAdapter;
    private OpenAndCloseAdapter openAndCloseAdapter;
    
    private ProgressBar loadingIndicator;
    private TextView loadingMessage;
    private View loadingOverlay;

    private ApiService apiService;

    private enum DataView {
        EXPENSES, SALES, BUYS, OPEN_CLOSE,
    }

    private DataView currentView = DataView.OPEN_CLOSE; // Start with Open/Close info

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.kisenyi_choices_view, container, false);

        // Bind views
        btnFetchExpenses = view.findViewById(R.id.dataseekerbtn);
        FinalUgandacashbtn = view.findViewById(R.id.FinalUgandacashbtn);
        Rowadderbtn = view.findViewById(R.id.AddNewRecords);
        status = view.findViewById(R.id.nav_status);
        recyclerView = view.findViewById(R.id.Gridview);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));
        loadingIndicator = view.findViewById(R.id.loadingIndicator);
        loadingMessage = view.findViewById(R.id.loadingMessage);
        loadingOverlay = view.findViewById(R.id.loadingOverlay);

        DrawerLayout drawerLayout = view.findViewById(R.id.drawer_layout_2);
        NavigationView navigationView = view.findViewById(R.id.kisenyi_view_choices);

        if (drawerLayout != null && navigationView != null) {
            btnFetchExpenses.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
            navigationView.setNavigationItemSelectedListener(item -> {
                handleMenuSelection(item.getItemId(), view, drawerLayout);
                return true;
            });
        } else {
            btnFetchExpenses.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(requireContext(), v);
                popup.getMenu().add(0, R.id.Expense_exps, 0, "Expenses");
                popup.getMenu().add(0, R.id.Sales_sls, 1, "Sales");
                popup.getMenu().add(0, R.id.Buys_bys, 2, "Buys");
                popup.getMenu().add(0, R.id.OpenCloseCash, 3, "Open/Close");
                popup.setOnMenuItemClickListener(item -> {
                    handleMenuSelection(item.getItemId(), view, null);
                    return true;
                });
                popup.show();
            });
        }

        Rowadderbtn.setOnClickListener(v -> rowadderfunction());

        FinalUgandacashbtn.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    requireContext(),
                    (view1, year, month, dayOfMonth) -> {
                        String selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                        shopsummary_Functions summaryFunctions = new shopsummary_Functions();
                        summaryFunctions.runSalesQueries(
                                requireContext(),
                                selectedDate,
                                loadingOverlay,
                                loadingMessage,
                                recyclerView,
                                openAndCloseAdapter
                        );
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });

        view.findViewById(R.id.placeholder_btn).setOnClickListener(v -> {
            View cardView = LayoutInflater.from(requireContext()).inflate(R.layout.kisenyimore, null);
            AlertDialog dialog = new AlertDialog.Builder(requireContext()).setView(cardView).create();
            dialog.show();

            cardView.findViewById(R.id.btn_sync_server).setOnClickListener(btnView -> {
                Toast.makeText(requireContext(), "Syncing changes to server...", Toast.LENGTH_SHORT).show();
                OneTimeWorkRequest syncRequest = new OneTimeWorkRequest.Builder(SyncWorker.class).build();
                WorkManager.getInstance(requireContext()).enqueue(syncRequest);
                dialog.dismiss();
            });
        });

        // Setup adapter but don't fetch until we are sure we have a user
        openAndCloseAdapter = new OpenAndCloseAdapter(new ArrayList<>(), null);
        recyclerView.setAdapter(openAndCloseAdapter);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initial state
        status.setText("Opening and Closing Data");

        // ✅ ONLY fetch if we have a valid logged-in user to prevent 403 loops
        if (SessionManager.getActiveUser(requireContext()) != -1) {
            apiService = ApiClient.getApiService(requireContext());
            OpenAndClose_Functions ocFunctions = new OpenAndClose_Functions();
            ocFunctions.fetchOpenAndClose(requireContext(),
                    requireActivity(),
                    loadingOverlay,
                    loadingMessage,
                    recyclerView,
                    openAndCloseAdapter);
        }
    }

    private void handleMenuSelection(int id, View rootView, DrawerLayout drawerLayout) {
        if (SessionManager.getActiveUser(requireContext()) == -1) return;
        
        FrameLayout headerContainer = rootView.findViewById(R.id.headerContainer);
        if (id == R.id.Expense_exps) {
            currentView = DataView.EXPENSES;
            expenseAdapter = new ExpenseAdapter(new ArrayList<>(), null);
            recyclerView.setAdapter(expenseAdapter);
            new expenses_Functions().fetchExpenses(requireContext(), requireActivity(), loadingOverlay, loadingMessage, recyclerView, expenseAdapter);
            status.setText("Expenses");
            headerContainer.removeAllViews();
            LayoutInflater.from(requireContext()).inflate(R.layout.expense_item_header, headerContainer, true);
        } else if (id == R.id.Sales_sls) {
            currentView = DataView.SALES;
            sellingAdapter = new SellingAdapter(new ArrayList<>(), null);
            recyclerView.setAdapter(sellingAdapter);
            new selling_Functions().fetchSelling(requireContext(), requireActivity(), loadingOverlay, loadingMessage, recyclerView, sellingAdapter);
            status.setText("Sales");
            headerContainer.removeAllViews();
            LayoutInflater.from(requireContext()).inflate(R.layout.sales_item_header, headerContainer, true);
        } else if (id == R.id.Buys_bys) {
            currentView = DataView.BUYS;
            buyingAdapter = new BuyingAdapter(new ArrayList<>(), null);
            recyclerView.setAdapter(buyingAdapter);
            new buying_Functions().fetchBuying(requireContext(), requireActivity(), loadingOverlay, loadingMessage, recyclerView, buyingAdapter);
            status.setText("Buys");
            headerContainer.removeAllViews();
            LayoutInflater.from(requireContext()).inflate(R.layout.buying_item_header, headerContainer, true);
        } else if (id == R.id.OpenCloseCash) {
            currentView = DataView.OPEN_CLOSE;
            new OpenAndClose_Functions().fetchOpenAndClose(requireContext(), requireActivity(), loadingOverlay, loadingMessage, recyclerView, openAndCloseAdapter);
            status.setText("Open/Close Cash");
            headerContainer.removeAllViews();
            LayoutInflater.from(requireContext()).inflate(R.layout.openclose_item_header, headerContainer, true);
        }

        if (drawerLayout != null) drawerLayout.closeDrawer(GravityCompat.START);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (SessionManager.getActiveUser(requireContext()) != -1) {
            OneTimeWorkRequest syncRequest = new OneTimeWorkRequest.Builder(SyncWorker.class)
                    .setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                    .build();
            WorkManager.getInstance(requireContext()).enqueue(syncRequest);
        }
    }

    private void rowadderfunction() {
        if (SessionManager.getActiveUser(requireContext()) == -1) return;
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        switch (currentView) {
            case EXPENSES:
                Executors.newSingleThreadExecutor().execute(() -> {
                    expense_queries expDao = expense_db_instance.getInstance(requireContext()).expense_Queries();
                    ExpenseEntity newExpense = new ExpenseEntity();
                    newExpense.setDescription("expense");
                    newExpense.setAmount(0);
                    newExpense.setDate(today);
                    newExpense.setSynced(false);
                    expDao.insert(newExpense);
                    List<ExpenseEntity> all = expDao.getAll();
                    if (isAdded()) requireActivity().runOnUiThread(() -> { if (expenseAdapter != null) expenseAdapter.updateData(all); });
                });
                break;
            case SALES:
                new AlertDialog.Builder(requireContext())
                        .setTitle("Select Transaction Type")
                        .setItems(new CharSequence[]{"Bank", "M-PESA"}, (dialog, which) -> {
                            Executors.newSingleThreadExecutor().execute(() -> {
                                Selling_queries sellDao = Selling_db_instance.getInstance(requireContext()).selling_Queries();
                                SellingEntity newSale = new SellingEntity();
                                newSale.setSellingDate(today);
                                newSale.setSellingAmount(0.0);
                                newSale.setSellingRate(0.0);
                                newSale.setReceivingCountry("Kenya");
                                newSale.setSynced(false);
                                if (which == 0) { newSale.setSaleType("USER_INITIATED_BANK"); newSale.setSellDescription("bank sale"); }
                                else { newSale.setSaleType("USER_INITIATED_MPESA"); newSale.setSellDescription("mpesa sale"); }
                                sellDao.insert(newSale);
                                List<SellingEntity> all = sellDao.getAll();
                                if (isAdded()) requireActivity().runOnUiThread(() -> { if (sellingAdapter != null) sellingAdapter.updateData(all); });
                            });
                        }).show();
                break;
            case BUYS:
                Executors.newSingleThreadExecutor().execute(() -> {
                    Buying_queries buyDao = Buying_db_instance.getInstance(requireContext()).buying_Queries();
                    BuyingEntity newBuy = new BuyingEntity();
                    newBuy.setBuyingDate(today);
                    newBuy.setBuyingAmount(0.0);
                    newBuy.setBuyingRate(0.0);
                    newBuy.setBuyDescription("buy");
                    newBuy.setSynced(false);
                    buyDao.insert(newBuy);
                    List<BuyingEntity> all = buyDao.getAll();
                    if (isAdded()) requireActivity().runOnUiThread(() -> { if (buyingAdapter != null) buyingAdapter.updateData(all); });
                });
                break;
            case OPEN_CLOSE:
                Executors.newSingleThreadExecutor().execute(() -> {
                    OpenAndClose_queries ocDao = OpenAndClose_db_instance.getInstance(requireContext()).openAndClose_Queries();
                    OpenAndCloseEntity newOC = new OpenAndCloseEntity();
                    newOC.setDate(today);
                    newOC.setUgandaOpening(0.0);
                    newOC.setKenyaOpening(0.0);
                    newOC.setUgandaCashIn(0.0);
                    newOC.setKenyaCashIn(0.0);
                    newOC.setUgandaClosing(0.0);
                    newOC.setKenyaClosing(0.0);
                    newOC.setDescription("open/close");
                    newOC.setSynced(false);
                    ocDao.insert(newOC);
                    List<OpenAndCloseEntity> all = ocDao.getAll();
                    if (isAdded()) requireActivity().runOnUiThread(() -> { if (openAndCloseAdapter != null) openAndCloseAdapter.updateData(all); });
                });
                break;
        }
    }
}
