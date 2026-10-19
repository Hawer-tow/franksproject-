package com.example.mobiledbclient.ui.TownBranch;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.app.AlertDialog;

import com.example.mobiledbclient.ApiClient;
import com.example.mobiledbclient.ApiService;
import com.example.mobiledbclient.R;
import com.example.mobiledbclient.ui.TownBranch.Commission.Commission;
import com.example.mobiledbclient.ui.TownBranch.Commission.CommissionAdapter;
import com.example.mobiledbclient.ui.TownBranch.Commission.CommissionEntity;
import com.example.mobiledbclient.ui.TownBranch.Commission.Commission_db_instance;
import com.example.mobiledbclient.ui.TownBranch.Commission.Commission_queries;
import com.example.mobiledbclient.ui.TownBranch.Commission.Commission_syncManager;
import com.example.mobiledbclient.ui.TownBranch.Deposit.Deposit;
import com.example.mobiledbclient.ui.TownBranch.Deposit.DepositAdapter;
import com.example.mobiledbclient.ui.TownBranch.Deposit.DepositEntity;
import com.example.mobiledbclient.ui.TownBranch.Deposit.Deposit_db_instance;
import com.example.mobiledbclient.ui.TownBranch.Deposit.Deposit_queries;
import com.example.mobiledbclient.ui.TownBranch.Deposit.Deposit_syncManager;
import com.example.mobiledbclient.ui.TownBranch.Withdraw.Withdraw;
import com.example.mobiledbclient.ui.TownBranch.Withdraw.WithdrawAdapter;
import com.example.mobiledbclient.ui.TownBranch.Withdraw.WithdrawEntity;
import com.example.mobiledbclient.ui.TownBranch.Withdraw.Withdraw_db_instance;
import com.example.mobiledbclient.ui.TownBranch.Withdraw.Withdraw_queries;
import com.example.mobiledbclient.ui.TownBranch.Withdraw.Withdraw_syncManager;
import com.google.android.material.navigation.NavigationView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * TownBranchFragment — minimal behavioral change:
 * Uses a small ViewModel to persist the "withdrawInitialized" flag across fragment recreation
 * and navigation so fetchWithdraw() is not called repeatedly when returning to this fragment.
 *
 * This keeps the rest of your existing logic intact.
 */
public class TownBranchFragment extends Fragment {
    private static final String TAG = "TownBranchFragment";

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private RecyclerView recyclerView;
    private ImageButton townrowaddfab;
    private TextView status;
    private Button towndbseekerbtn;

    private WithdrawAdapter withdrawAdapter;
    private Withdraw_syncManager withdrawSyncManager;

    private DepositAdapter depositAdapter;
    private Deposit_syncManager depositSyncManager;

    private CommissionAdapter commissionAdapter;
    private Commission_syncManager commissionSyncManager;

    private enum DataView {
        WITHDRAW, DEPOSIT, COMMISSION
    }

    private DataView currentView = DataView.WITHDRAW;

    // Use a small ViewModel to persist the initialization flag across fragment recreation/navigation
    private TownStateViewModel stateViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.townbranch_choices_view, container, false);

        // Obtain ViewModel scoped to the activity so the flag survives fragment recreation and navigation
        stateViewModel = new ViewModelProvider(requireActivity()).get(TownStateViewModel.class);

        // Layout references (IDs must match your XML)
        drawerLayout = root.findViewById(R.id.town_drawer_layout);
        navigationView = root.findViewById(R.id.town_view_choices);
        recyclerView = root.findViewById(R.id.TownGridview);
        townrowaddfab = root.findViewById(R.id.TownAddNewRecords);
        status = root.findViewById(R.id.Town_nav_status);
        towndbseekerbtn = root.findViewById(R.id.Towndbseekerbtn);

        // Defensive checks
        if (recyclerView == null) {
            Log.e(TAG, "RecyclerView with id TownGridview not found in layout.");
            return root;
        }
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Open drawer when Towndbseekerbtn is clicked
        if (towndbseekerbtn != null && drawerLayout != null) {
            towndbseekerbtn.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        } else if (towndbseekerbtn == null) {
            Log.w(TAG, "Towndbseekerbtn not found in layout.");
        }

        // Navigation menu selection
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.withdrawoption) {
                    currentView = DataView.WITHDRAW;
                    // Use ViewModel flag instead of a transient field so the flag persists
                    if (!stateViewModel.isWithdrawInitialized()) {
                        fetchWithdraw();
                        stateViewModel.setWithdrawInitialized(true);
                    } else if (withdrawAdapter != null) {
                        recyclerView.setAdapter(withdrawAdapter);
                    }
                    if (status != null) status.setText("Withdrawals");
                    if (drawerLayout != null) drawerLayout.closeDrawer(GravityCompat.START);
                    return true;
                } else if (id == R.id.depositoption) {
                    currentView = DataView.DEPOSIT;
                    fetchDeposit();
                    if (depositAdapter != null) recyclerView.setAdapter(depositAdapter);
                    if (status != null) status.setText("Deposits");
                    if (drawerLayout != null) drawerLayout.closeDrawer(GravityCompat.START);
                    return true;
                } else if (id == R.id.commissionoption) {
                    currentView = DataView.COMMISSION;
                    fetchCommission();
                    if (commissionAdapter != null) recyclerView.setAdapter(commissionAdapter);
                    if (status != null) status.setText("Commissions");
                    if (drawerLayout != null) drawerLayout.closeDrawer(GravityCompat.START);
                    return true;
                }
                return false;
            });
        } else {
            Log.w(TAG, "NavigationView with id town_view_choices not found.");
        }

        // Floating action button to add new records
        if (townrowaddfab != null) {
            townrowaddfab.setOnClickListener(v -> {
                switch (currentView) {
                    case WITHDRAW:
                        addNewWithdraw();
                        break;
                    case DEPOSIT:
                        addNewDeposit();
                        break;
                    case COMMISSION:
                        addNewCommission();
                        break;
                }
            });
        } else {
            Log.w(TAG, "FloatingActionButton TownAddNewRecords not found.");
        }

        // Default view: only fetch withdraw once per app/activity lifecycle (persisted in ViewModel)
        if (!stateViewModel.isWithdrawInitialized()) {
            fetchWithdraw();
            stateViewModel.setWithdrawInitialized(true);
        } else if (withdrawAdapter != null) {
            recyclerView.setAdapter(withdrawAdapter);
        }
        if (status != null) status.setText("Withdrawals");

        return root;
    }

    // --- Add new helpers (unchanged behavior) ---

    private void addNewWithdraw() {
        Withdraw_db_instance wDb = Withdraw_db_instance.getInstance(requireContext());
        Withdraw_queries wDao = wDb.withdraw_Queries();
        WithdrawEntity newW = new WithdrawEntity();
        newW.setTid(String.valueOf(System.currentTimeMillis()));
        newW.setTdate(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
        newW.setSynced(false);
        wDao.insert(newW);

        if (withdrawAdapter == null) {
            fetchWithdraw();
        } else {
            withdrawAdapter.updateData(wDao.getAll());
        }
        if (withdrawSyncManager != null) withdrawSyncManager.sync();
    }

    private void addNewDeposit() {
        Deposit_db_instance dDb = Deposit_db_instance.getInstance(requireContext());
        Deposit_queries dDao = dDb.deposit_Queries();
        DepositEntity newD = new DepositEntity();
        newD.setTid(String.valueOf(System.currentTimeMillis()));
        newD.setTdate(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
        newD.setSynced(false);
        dDao.insert(newD);

        if (depositAdapter == null) {
            fetchDeposit();
        } else {
            depositAdapter.updateData(dDao.getAll());
        }
        if (depositSyncManager != null) depositSyncManager.sync();
    }

    private void addNewCommission() {
        Commission_db_instance cDb = Commission_db_instance.getInstance(requireContext());
        Commission_queries cDao = cDb.commission_Queries();
        CommissionEntity newC = new CommissionEntity();
        newC.setCid(String.valueOf(System.currentTimeMillis()));
        newC.setCdate(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
        newC.setSynced(false);
        cDao.insert(newC);

        if (commissionAdapter == null) {
            fetchCommission();
        } else {
            commissionAdapter.updateData(cDao.getAll());
        }
        if (commissionSyncManager != null) commissionSyncManager.sync();
    }

    // --- Fetch methods (keeps your existing logic; only called once for withdraw unless you reset the flag) ---

    private void fetchWithdraw() {
        Withdraw_db_instance db = Withdraw_db_instance.getInstance(requireContext());
        Withdraw_queries dao = db.withdraw_Queries();
        ApiService api = ApiClient.getClient().create(ApiService.class);

        List<WithdrawEntity> local = dao.getAll();

        if (withdrawAdapter == null) {
            withdrawAdapter = new WithdrawAdapter(local, new WithdrawAdapter.OnWithdrawActionListener() {
                @Override
                public void onEdit(WithdrawEntity entity) {

                    new AlertDialog.Builder(requireContext())
                            .setTitle("Confirm Save")
                            .setMessage("Do you want to permanently save these changes?")
                            .setPositiveButton("Yes", (dialog, which) -> {
                                entity.setSynced(false);
                                dao.update(entity);
                                // Immediate UI update
                                withdrawAdapter.updateWithdraw(entity);

                                // Background sync
                                if (withdrawSyncManager != null) withdrawSyncManager.sync();
                            })
                            .setNegativeButton("No", (dialog, which) -> {
                                // Revert changes: reload the entity from DB or restore old values
                                WithdrawEntity oldEntity = dao.findByTid(entity.getTid());
                                withdrawAdapter.updateWithdraw(oldEntity);
                            })
                            .show();

                }

                @Override
                public void onDelete(WithdrawEntity entity) {

                    new AlertDialog.Builder(requireContext())
                            .setTitle("Confirm Delete")
                            .setMessage("Are you sure you want to permanently delete this row?")
                            .setPositiveButton("Yes", (dialog, which) -> {
                                entity.setMarkedForDelete(true);
                                entity.setSynced(false);
                                dao.update(entity);

                                // Immediate UI update
                                withdrawAdapter.removeWithdraw(entity);

                                // Background sync
                                if (withdrawSyncManager != null) withdrawSyncManager.sync();
                            })
                            .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                            .show();
              }
            });
            recyclerView.setAdapter(withdrawAdapter);
        } else {
            withdrawAdapter.updateData(local);
        }

        // Initialize sync manager with adapter
        withdrawSyncManager = new Withdraw_syncManager(dao, api, withdrawAdapter);
        withdrawSyncManager.sync();

        // Optional: fetch from server and merge into local DB (keeps your existing pattern)
        api.getWithdraw().enqueue(new Callback<List<Withdraw>>() {
            @Override
            public void onResponse(Call<List<Withdraw>> call, Response<List<Withdraw>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Withdraw> serverList = response.body();
                    // merge logic: adapt to your existing pattern (find by id, insert or update)
                    // After merging into DB:
                    withdrawAdapter.updateData(dao.getAll());
                }
            }

            @Override
            public void onFailure(Call<List<Withdraw>> call, Throwable t) {
                Log.e(TAG, "Fetch Withdraw failed", t);
            }
        });
    }

    private void fetchDeposit() {
        Deposit_db_instance db = Deposit_db_instance.getInstance(requireContext());
        Deposit_queries dao = db.deposit_Queries();
        ApiService api = ApiClient.getClient().create(ApiService.class);

        List<DepositEntity> local = dao.getAll();

        if (depositAdapter == null) {
            depositAdapter = new DepositAdapter(local, new DepositAdapter.OnDepositActionListener() {
                @Override
                public void onEdit(DepositEntity entity) {
                    entity.setSynced(false);
                    dao.update(entity);
                    depositAdapter.updateDeposit(entity);
                    if (depositSyncManager != null) depositSyncManager.sync();
                }

                @Override
                public void onDelete(DepositEntity entity) {
                    entity.setMarkedForDelete(true);
                    entity.setSynced(false);
                    dao.update(entity);
                    depositAdapter.removeDeposit(entity);
                    if (depositSyncManager != null) depositSyncManager.sync();
                }
            });
            recyclerView.setAdapter(depositAdapter);
        } else {
            depositAdapter.updateData(local);
        }

        depositSyncManager = new Deposit_syncManager(dao, api, depositAdapter);
        depositSyncManager.sync();

        api.getDeposit().enqueue(new Callback<List<Deposit>>() {
            @Override
            public void onResponse(Call<List<Deposit>> call, Response<List<Deposit>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    depositAdapter.updateData(dao.getAll());
                }
            }

            @Override
            public void onFailure(Call<List<Deposit>> call, Throwable t) {
                Log.e(TAG, "Fetch Deposit failed", t);
            }
        });
    }

    private void fetchCommission() {
        Commission_db_instance db = Commission_db_instance.getInstance(requireContext());
        Commission_queries dao = db.commission_Queries();
        ApiService api = ApiClient.getClient().create(ApiService.class);

        List<CommissionEntity> local = dao.getAll();

        if (commissionAdapter == null) {
            commissionAdapter = new CommissionAdapter(local, new CommissionAdapter.OnCommissionActionListener() {
                @Override
                public void onEdit(CommissionEntity entity) {
                    entity.setSynced(false);
                    dao.update(entity);
                    commissionAdapter.updateCommission(entity);
                    if (commissionSyncManager != null) commissionSyncManager.sync();
                }

                @Override
                public void onDelete(CommissionEntity entity) {
                    entity.setMarkedForDelete(true);
                    entity.setSynced(false);
                    dao.update(entity);
                    commissionAdapter.removeCommission(entity);
                    if (commissionSyncManager != null) commissionSyncManager.sync();
                }
            });
            recyclerView.setAdapter(commissionAdapter);
        } else {
            commissionAdapter.updateData(local);
        }

        commissionSyncManager = new Commission_syncManager(dao, api, commissionAdapter);
        commissionSyncManager.sync();

        api.getCommission().enqueue(new Callback<List<Commission>>() {
            @Override
            public void onResponse(Call<List<Commission>> call, Response<List<Commission>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    commissionAdapter.updateData(dao.getAll());
                }
            }

            @Override
            public void onFailure(Call<List<Commission>> call, Throwable t) {
                Log.e(TAG, "Fetch Commission failed", t);
            }
        });
    }

    /**
     * Small ViewModel to persist the withdrawInitialized flag across fragment recreation/navigation.
     * Kept minimal to avoid changing your app architecture.
     */
    public static class TownStateViewModel extends ViewModel {
        private boolean withdrawInitialized = false;

        public boolean isWithdrawInitialized() {
            return withdrawInitialized;
        }

        public void setWithdrawInitialized(boolean withdrawInitialized) {
            this.withdrawInitialized = withdrawInitialized;
        }
    }
}
