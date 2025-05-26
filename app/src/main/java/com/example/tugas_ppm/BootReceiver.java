package com.example.tugas_ppm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import com.example.tugas_ppm.MainActivity;



import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            // Ambil userKey dari SharedPreferences
            SharedPreferences prefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            String userKey = prefs.getString("userKey", null);

            if (userKey == null) return; // Tidak ada user yang login sebelumnya

            DatabaseReference tugasRef = FirebaseDatabase.getInstance().getReference("users").child(userKey).child("tugas");
            tugasRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    for (DataSnapshot tugasItem : snapshot.getChildren()) {
                        String judul = tugasItem.child("judul").getValue(String.class);
                        String tanggal = tugasItem.child("tanggal").getValue(String.class);
                        String jam = tugasItem.child("jam").getValue(String.class);

                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                        try {
                            Date deadline = sdf.parse(tanggal + " " + jam);
                            if (deadline != null && deadline.after(new Date())) {
                                MainActivity.scheduleNotification(deadline.getTime(), judul, context);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                }
            });
        }
    }


}
