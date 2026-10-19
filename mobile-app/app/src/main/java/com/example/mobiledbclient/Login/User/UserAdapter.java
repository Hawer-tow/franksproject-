package com.example.mobiledbclient.Login.User;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import com.example.mobiledbclient.R;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    public interface OnUserClickListener {
        void onUserClick(User user);
    }

    private List<User> users;
    private OnUserClickListener listener;
    private int highlightUserId = -1;

    public UserAdapter(List<User> users, int highlightUserId, OnUserClickListener listener) {
        this.users = users != null ? users : new ArrayList<>();
        this.listener = listener;
        this.highlightUserId = highlightUserId;
    }

    /**
     * Update the adapter’s dataset and refresh the RecyclerView.
     */
    public void updateData(List<User> newUsers) {
        this.users.clear();
        if (newUsers != null) {
            this.users.addAll(newUsers);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_box, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);

        // Bind username
        holder.username.setText(user.getUsername());

        // Bind extra info (role or last login)
        if (user.getLastLogin() != null && !user.getLastLogin().isEmpty()) {
            holder.userInfo.setText("Last login: " + user.getLastLogin());
        } else {
            holder.userInfo.setText("Log in for the first time"); // fallback
        }

        // Highlight selected user
        if (user.getId() == highlightUserId) {
            holder.itemView.setBackgroundResource(R.drawable.user_box_highlight_bg);
        } else {
            // Alternate background colors based on position
            int[] colors = {
                    holder.itemView.getContext().getResources().getColor(R.color.usercolor1),
                    holder.itemView.getContext().getResources().getColor(R.color.usercolor2),
                    holder.itemView.getContext().getResources().getColor(R.color.usercolor3),
                    holder.itemView.getContext().getResources().getColor(R.color.usercolor4),
                    holder.itemView.getContext().getResources().getColor(R.color.usercolor5),
                    holder.itemView.getContext().getResources().getColor(R.color.usercolor6)

            };
            int colorIndex = position % colors.length;
            holder.itemView.setBackgroundColor(colors[colorIndex]);
        }

        // Click listener
        holder.itemView.setOnClickListener(v -> listener.onUserClick(user));
    }


    @Override
    public int getItemCount() {
        return users.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView username;
        TextView userInfo;

        UserViewHolder(View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.textUsername);
            userInfo = itemView.findViewById(R.id.textUserInfo);
        }
    }
}
