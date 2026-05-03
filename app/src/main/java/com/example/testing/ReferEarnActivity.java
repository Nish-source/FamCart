package com.example.testing;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.famcart.R;

public class ReferEarnActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refer_earn);

        TextView tvCode = findViewById(R.id.tv_referral_code);
        String referralCode = tvCode.getText().toString();

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        findViewById(R.id.btn_copy_code).setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Referral Code", referralCode);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Code copied to clipboard!", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btn_share_referral).setOnClickListener(v -> {
            String shareMessage = "Use my referral code " + referralCode + " to get ₹100 off on your first order at FamCart! Download now: https://famcart.com/app";
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(shareIntent, "Share via"));
        });
    }
}
