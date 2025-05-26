package com.example.tugas_ppm;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.app.PendingIntent;
import android.app.NotificationChannel;
import android.app.NotificationManager;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import java.util.Random;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String judul = intent.getStringExtra("judul");
        String message = intent.getStringExtra("message");


        Intent notificationIntent = new Intent(context.getApplicationContext(), activity_splash.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context.getApplicationContext(),
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0)
        );

        // Bangun notifikasi
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "deadline_channel_custom")
                .setSmallIcon(R.drawable.logolecturaxbg)
                .setContentTitle(judul)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        // Cek permission POST_NOTIFICATIONS untuk Android 13+
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                        == PackageManager.PERMISSION_GRANTED) {

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            int notificationId = (judul + message).hashCode();
            notificationManager.notify(notificationId, builder.build());
        }
    }
}
