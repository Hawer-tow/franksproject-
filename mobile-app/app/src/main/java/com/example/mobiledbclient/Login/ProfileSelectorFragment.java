package com.example.mobiledbclient.Login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobiledbclient.R;
import com.example.mobiledbclient.Login.User.UserAdapter;
import com.example.mobiledbclient.Login.User.UserRepository;
import com.example.mobiledbclient.Login.User.User;
import com.example.mobiledbclient.ApiClient;
import com.example.mobiledbclient.ApiService;
import com.google.android.material.button.MaterialButton;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileSelectorFragment extends Fragment {
    private static final String ARG_HIGHLIGHT_USER = "highlight_user";
    private int highlightUserId = -1;
    private UserRepository userRepository;
    private RecyclerView recyclerProfiles;
    private View loadingOverlay;
    private ProgressBar loadingIndicator;
    private TextView loadingMessage;
    private MaterialButton reloadBtn;

    public static ProfileSelectorFragment newInstance(int highlightUserId) {
        ProfileSelectorFragment f = new ProfileSelectorFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_HIGHLIGHT_USER, highlightUserId);
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        if (getArguments() != null) {
            highlightUserId = getArguments().getInt(ARG_HIGHLIGHT_USER, -1);
        }

        View root = inflater.inflate(R.layout.fragment_profile_selector, container, false);

        recyclerProfiles = root.findViewById(R.id.recyclerProfiles);
        recyclerProfiles.setLayoutManager(new GridLayoutManager(getContext(), 2));

        loadingOverlay = root.findViewById(R.id.loadingOverlay);
        loadingIndicator = root.findViewById(R.id.loadingIndicator);
        loadingMessage = root.findViewById(R.id.loadingMessage);
        reloadBtn = root.findViewById(R.id.btnReloadProfiles);

        userRepository = new UserRepository(requireContext());

        reloadBtn.setOnClickListener(v -> fetchProfilesFromServer());

        // Initial load
        fetchProfilesFromServer();

        return root;
    }

    private void showLoading(String message) {
        loadingOverlay.setVisibility(View.VISIBLE);
        loadingIndicator.setVisibility(View.VISIBLE);
        loadingMessage.setVisibility(View.VISIBLE);
        loadingMessage.setText(message);
        recyclerProfiles.setVisibility(View.GONE);
    }

    private void hideLoading() {
        loadingOverlay.setVisibility(View.GONE);
        recyclerProfiles.setVisibility(View.VISIBLE);
    }

    private void fetchProfilesFromServer() {
        showLoading("Fetching profiles...");

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getAllUsers().enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                hideLoading();

                if (response.isSuccessful() && response.body() != null) {
                    List<User> users = response.body();
                    userRepository.syncFromServer(users);
                    updateAdapter(users);
                } else if (isAdded()) {
                    Toast.makeText(requireActivity().getApplicationContext(),
                            "Failed to load profiles", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                hideLoading();
                if (isAdded()) {
                    Toast.makeText(requireActivity().getApplicationContext(),
                            "Network error, showing local data", Toast.LENGTH_SHORT).show();
                }

                new Thread(() -> {
                    List<User> users = userRepository.getAllUsers();
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> updateAdapter(users));
                    }
                }).start();
            }

        });
    }

    private void updateAdapter(List<User> users) {
        if (users != null) {
            recyclerProfiles.setAdapter(new UserAdapter(
                    users,
                    highlightUserId,
                    user -> navigateToLogin(user)   // pass full user object
            ));

        }
    }

    private void navigateToLogin(User selectedUser) {
        if (!isAdded()) return;

        // Save active user ID
        SessionManager.setActiveUser(requireContext(), selectedUser.getId());

        // Build arguments bundle
        Bundle args = new Bundle();
        args.putInt("user_id", selectedUser.getId());
        args.putString("username", selectedUser.getUsername());

        // Navigate with arguments
        NavController navController = Navigation.findNavController(requireActivity(), R.id.auth_nav_host);
        navController.navigate(R.id.loginFragment, args);
    }



    @Override
    public void onResume() {
        super.onResume();
        // Toolbar is hidden by MainActivity.lockAndHideNavigation()
    }

    @Override
    public void onPause() {
        super.onPause();
        // Toolbar management handled by MainActivity unlock/lock methods
    }
}
