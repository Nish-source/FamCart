package com.example.testing.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.famcart.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private List<Map<String, Object>> notifications = new ArrayList<>();

    public void updateItems(List<Map<String, Object>> items) {
        this.notifications = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> notif = notifications.get(position);

        String title = (String) notif.get("title");
        String message = (String) notif.get("message");
        Long timestamp = (Long) notif.get("timestamp");
        Boolean read = (Boolean) notif.get("read");

        holder.tvTitle.setText(title != null ? title : "Notification");
        holder.tvMessage.setText(message != null ? message : "");

        if (timestamp != null) {
            holder.tvTime.setText(formatTimestamp(timestamp));
        }

        // Show unread dot
        if (read != null && !read) {
            holder.dotUnread.setVisibility(View.VISIBLE);
        } else {
            holder.dotUnread.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    private String formatTimestamp(long timestamp) {
        long diff = System.currentTimeMillis() - timestamp;
        long minutes = diff / 60000;
        long hours = minutes / 60;
        long days = hours / 24;

        if (minutes < 1) return "Just now";
        if (minutes < 60) return minutes + "m ago";
        if (hours < 24) return hours + "h ago";
        if (days < 7) return days + "d ago";

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvMessage, tvTime;
        ImageView ivIcon;
        View dotUnread;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_notif_title);
            tvMessage = itemView.findViewById(R.id.tv_notif_message);
            tvTime = itemView.findViewById(R.id.tv_notif_time);
            ivIcon = itemView.findViewById(R.id.iv_notif_icon);
            dotUnread = itemView.findViewById(R.id.dot_unread);
        }
    }
}
