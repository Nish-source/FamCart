package com.example.testing;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.famcart.R;
import com.example.testing.adapters.NotificationAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationsActivity extends AppCompatActivity {

    private RecyclerView rvNotifications;
    private LinearLayout layoutEmpty;
    private NotificationAdapter adapter;
    private DatabaseReference notifRef;
    private ValueEventListener notifListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        rvNotifications = findViewById(R.id.rv_notifications);
        layoutEmpty = findViewById(R.id.layout_empty);

        adapter = new NotificationAdapter();
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        rvNotifications.setAdapter(adapter);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        loadNotifications();
    }

    private void loadNotifications() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            showEmptyState();
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        notifRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("notifications");

        notifListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Map<String, Object>> notifications = new ArrayList<>();

                for (DataSnapshot child : snapshot.getChildren()) {
                    Map<String, Object> notif = new HashMap<>();
                    notif.put("key", child.getKey());
                    notif.put("title", child.child("title").getValue(String.class));
                    notif.put("message", child.child("message").getValue(String.class));
                    notif.put("timestamp", child.child("timestamp").getValue(Long.class));
                    notif.put("read", child.child("read").getValue(Boolean.class));
                    notifications.add(notif);

                    // Mark as read
                    if (Boolean.FALSE.equals(child.child("read").getValue(Boolean.class))) {
                        child.getRef().child("read").setValue(true);
                    }
                }

                // Sort by timestamp descending (newest first)
                Collections.sort(notifications, (a, b) -> {
                    Long ta = (Long) a.get("timestamp");
                    Long tb = (Long) b.get("timestamp");
                    if (ta == null) ta = 0L;
                    if (tb == null) tb = 0L;
                    return Long.compare(tb, ta);
                });

                if (notifications.isEmpty()) {
                    showEmptyState();
                } else {
                    rvNotifications.setVisibility(View.VISIBLE);
                    layoutEmpty.setVisibility(View.GONE);
                    adapter.updateItems(notifications);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(NotificationsActivity.this, "Failed to load notifications", Toast.LENGTH_SHORT).show();
            }
        };
        notifRef.addValueEventListener(notifListener);
    }

    private void showEmptyState() {
        rvNotifications.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (notifRef != null && notifListener != null) {
            notifRef.removeEventListener(notifListener);
        }
    }
}
