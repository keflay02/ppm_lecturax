package com.example.tugas_ppm;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class DeadlineWorker extends Worker {
    private final Context context;

    public DeadlineWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d("DeadlineWorker", "Worker dimulai");
        CountDownLatch latch = new CountDownLatch(1);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference usersRef = database.getReference("users");

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long now = System.currentTimeMillis();
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

                for (DataSnapshot userSnap : snapshot.getChildren()) {
                    for (DataSnapshot tugasSnap : userSnap.child("tugas").getChildren()) {
                        String namaTugas = tugasSnap.child("judul").getValue(String.class);
                        String tanggal = tugasSnap.child("tanggal").getValue(String.class); // format: dd/MM/yyyy
                        String jam = tugasSnap.child("jam").getValue(String.class);         // format: HH:mm

                        if (namaTugas != null && tanggal != null && jam != null) {
                            try {
                                Date deadline = sdf.parse(tanggal + " " + jam);
                                if (deadline == null) continue;

                                long millisUntilDeadline = deadline.getTime() - now;
                                long hoursLeft = TimeUnit.MILLISECONDS.toHours(millisUntilDeadline);

                                Log.d("DeadlineWorker", "Cek: " + namaTugas + " - " + hoursLeft + " jam lagi");

                                if (hoursLeft == 24 || hoursLeft == 1) {
                                    sendNotification(namaTugas, hoursLeft);
                                }

                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                latch.countDown(); // Selesai membaca semua tugas
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                latch.countDown(); // Tetap lanjut walau error
            }
        });

        try {
            latch.await(10, TimeUnit.SECONDS); // Tunggu max 10 detik Firebase selesai
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Log.d("DeadlineWorker", "Worker selesai");
        return Result.success();
    }

    private void sendNotification(String namaTugas, long hoursLeft) {
        String message = "Tugas \"" + namaTugas + "\" deadline tinggal " + hoursLeft + " jam lagi!";

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        String channelId = "deadline_channel";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Notifikasi Deadline",
                    NotificationManager.IMPORTANCE_HIGH
            );
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.logolecturaxbg)
                .setContentTitle("Pengingat Tugas")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }
}
