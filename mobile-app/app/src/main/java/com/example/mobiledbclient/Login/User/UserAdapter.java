package com.example.mobiledbclient.Login.User;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.color.MaterialColors;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

        Context context = holder.itemView.getContext();

        // Bind username and its initial
        String name = user.getUsername() != null ? user.getUsername().trim() : "";
        holder.username.setText(name);
        holder.initial.setText(name.isEmpty() ? "?" : name.substring(0, 1).toUpperCase(Locale.getDefault()));

        // Bind extra info (last login, shown as "2026-05-11 22:32")
        String lastLogin = user.getLastLogin();
        if (lastLogin != null && !lastLogin.isEmpty()) {
            String readable = lastLogin.replace('T', ' ');
            if (readable.length() > 16) readable = readable.substring(0, 16);
            holder.userInfo.setText("Last login " + readable);
        } else {
            holder.userInfo.setText("New profile · first login");
        }

        // Each profile gets its own soft avatar colour
        int[] colors = {
                ContextCompat.getColor(context, R.color.usercolor1),
                ContextCompat.getColor(context, R.color.usercolor2),
                ContextCompat.getColor(context, R.color.usercolor3),
                ContextCompat.getColor(context, R.color.usercolor4),
                ContextCompat.getColor(context, R.color.usercolor5),
                ContextCompat.getColor(context, R.color.usercolor6)
        };
        ViewCompat.setBackgroundTintList(holder.initial,
                ColorStateList.valueOf(colors[position % colors.length]));

        // Outline the profile that was used last
        if (holder.itemView instanceof MaterialCardView) {
            MaterialCardView card = (MaterialCardView) holder.itemView;
            boolean highlighted = user.getId() == highlightUserId;
            float density = context.getResources().getDisplayMetrics().density;
            card.setStrokeWidth(Math.round((highlighted ? 2 : 1) * density));
            card.setStrokeColor(highlighted
                    ? MaterialColors.getColor(card, androidx.appcompat.R.attr.colorPrimary)
                    : MaterialColors.getColor(card, com.google.android.material.R.attr.colorOutlineVariant));
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
        TextView initial;

        UserViewHolder(View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.textUsername);
            userInfo = itemView.findViewById(R.id.textUserInfo);
            initial = itemView.findViewById(R.id.textInitial);
        }
    }
}
