package com.example.tugas_ppm;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.net.Uri;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.content.pm.PackageManager;
import android.os.Build;
import android.Manifest;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import java.util.concurrent.TimeUnit;




import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.*;
import java.time.Duration;

public class MainActivity extends AppCompatActivity {

    TextView tvNamaPengguna, btnTambahMatkul, btnTambahTugas;
    LinearLayout layoutMataKuliah, layoutDeadline;
    ImageView menu_icon;
    DatabaseReference dbRef;
    String username;
    SearchView searchView;
    List<MataKuliah> semuaMataKuliah = new ArrayList<>();

    List<Tugas> semuaTugas = new ArrayList<>();
    Map<String, String> semuaMatkul = new HashMap<>();

    @Override
    protected void onResume() {
        super.onResume();
        if (username == null) {
            username = getIntent().getStringExtra("username");
        }
        loadUserData();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "deadline_channel",
                    "Deadline Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifikasi pengingat deadline tugas");

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        1001); // 1001 adalah requestCode, bisa kamu sesuaikan
            }
        }




        searchView = findViewById(R.id.search_bar);

        tvNamaPengguna = findViewById(R.id.user_name);
        btnTambahMatkul = findViewById(R.id.see_all_button);
        btnTambahTugas = findViewById(R.id.tambah_tugas);
        menu_icon = findViewById(R.id.menu_icon);

        HorizontalScrollView scrollViewMatkul = findViewById(R.id.mata_kuliah_scroll);
        layoutMataKuliah = (LinearLayout) ((ViewGroup) scrollViewMatkul.getChildAt(0));

        HorizontalScrollView scrollViewDeadline = findViewById(R.id.deadline_scroll);
        layoutDeadline = (LinearLayout) ((ViewGroup) scrollViewDeadline.getChildAt(0));

        username = getIntent().getStringExtra("username");
        if (username == null) username = "default";

        loadUserData();

        btnTambahMatkul.setOnClickListener(v -> showTambahMatkulPopup());
        btnTambahTugas.setOnClickListener(v -> showTambahTugasPopup());

        menu_icon.setOnClickListener(this::showPopupMenu);

        createCustomNotificationChannel(this);


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                String query = newText.toLowerCase();

                List<Tugas> hasilTugas = new ArrayList<>();
                for (Tugas tugas : semuaTugas) {
                    if (tugas.judul.toLowerCase().contains(query)) {
                        hasilTugas.add(tugas);
                    }
                }

                List<MataKuliah> hasilMatkul = new ArrayList<>();
                for (MataKuliah mk : semuaMataKuliah) {
                    if (mk.nama.toLowerCase().contains(query)) {
                        hasilMatkul.add(mk);
                    }
                }

                tampilkanTugas(hasilTugas);
                tampilkanMataKuliah(hasilMatkul);

                return true;
            }
        });

    }

    private void showPopupMenu(View view) {
        PopupMenu popup = new PopupMenu(MainActivity.this, view);
        popup.getMenuInflater().inflate(R.menu.main_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_edit_profil) {
                startActivity(new Intent(MainActivity.this, activity_edit_profile.class)
                        .putExtra("username", username));
                return true;

            } else if (id == R.id.menu_logout) {
                // ✅ Bersihkan SharedPreferences saat logout
                SharedPreferences.Editor editor = getSharedPreferences("UserPrefs", MODE_PRIVATE).edit();
                editor.clear(); // atau editor.remove("userKey") jika ingin lebih spesifik
                editor.apply();

                // Navigasi ke login
                Intent intent = new Intent(MainActivity.this, activity_login.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Hapus back stack
                startActivity(intent);
                finish();
                return true;
            }

            return false;
        });

        popup.show();
    }


    private void loadUserData() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        usersRef.orderByChild("username").equalTo(username)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot userSnap : snapshot.getChildren()) {
                                dbRef = userSnap.getRef();
                                String nama = userSnap.child("nama").getValue(String.class);
                                tvNamaPengguna.setText(nama);

                                layoutMataKuliah.removeAllViews();
                                semuaMataKuliah.clear();
                                for (DataSnapshot matkulSnap : userSnap.child("mataKuliah").getChildren()) {
                                    String matkulId = matkulSnap.getKey();
                                    String matkul = matkulSnap.getValue(String.class);
                                    semuaMataKuliah.add(new MataKuliah(matkulId, matkul));
                                }
                                tampilkanMataKuliah(semuaMataKuliah);


                                semuaTugas.clear();
                                for (DataSnapshot tugasSnap : userSnap.child("tugas").getChildren()) {
                                    String tugasId = tugasSnap.getKey();
                                    String judul = tugasSnap.child("judul").getValue(String.class);
                                    String tanggal = tugasSnap.child("tanggal").getValue(String.class);
                                    String jam = tugasSnap.child("jam").getValue(String.class);
                                    String matkul = tugasSnap.child("matkul").getValue(String.class);

                                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                                    try {
                                        Date deadline = sdf.parse(tanggal + " " + jam);
                                        if (deadline != null && deadline.before(new Date())) {
                                            // Tugas sudah lewat deadline, hapus dari database
                                            userSnap.getRef().child("tugas").child(tugasId).removeValue();
                                        } else {
                                            semuaTugas.add(new Tugas(tugasId, judul, tanggal, jam, matkul));
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }


                                Collections.sort(semuaTugas, Comparator.comparing(Tugas::getDeadlineDate));

                                for (Tugas tugas : semuaTugas) {
                                    try {
                                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                                        Date deadline = sdf.parse(tugas.tanggal + " " + tugas.jam);
                                        if (deadline != null && deadline.after(new Date())) {
                                            scheduleNotification(deadline.getTime(), tugas.judul, MainActivity.this);
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }

                                tampilkanTugas(semuaTugas);


                                break;
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "User tidak ditemukan", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(MainActivity.this, "Gagal mengambil data user", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void createCustomNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "deadline_channel_custom";
            CharSequence name = "Custom Deadline Reminder";
            String description = "Notifikasi dengan suara custom untuk deadline tugas";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(channelId, name, importance);
            channel.setDescription(description);

            Uri soundUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.lecturaxsound);
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            channel.setSound(soundUri, audioAttributes);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void tampilkanTugas(List<Tugas> daftar) {
        layoutDeadline.removeAllViews();
        for (Tugas tugas : daftar) {
            addTugasCard(tugas.id, tugas.judul, tugas.tanggal, tugas.jam, tugas.matkul);
        }
    }

    private void tampilkanMataKuliah(List<MataKuliah> daftar) {
        layoutMataKuliah.removeAllViews();
        for (MataKuliah mk : daftar) {
            addMatkulCard(mk.id, mk.nama);
        }
    }

    public static void scheduleNotification(long deadlineMillis, String judul, Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // ID tetap berdasarkan judul dan waktu notifikasi
        int requestCode24 = ("notif24_" + judul).hashCode();
        int requestCode1 = ("notif1_" + judul).hashCode();

        // Notifikasi 24 jam sebelum deadline
        long waktu24Jam = deadlineMillis - (24 * 60 * 60 * 1000);
        if (waktu24Jam > System.currentTimeMillis()) {
            Intent intent24 = new Intent(context, NotificationReceiver.class);
            intent24.putExtra("judul", "Pengingat Tugas");
            intent24.putExtra("message", "Deadline \"" + judul + "\" tinggal 24 jam lagi!");
            PendingIntent pendingIntent24 = PendingIntent.getBroadcast(
                    context, requestCode24, intent24,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, waktu24Jam, pendingIntent24);
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, waktu24Jam, pendingIntent24);
            }
        }

        // Notifikasi 1 jam sebelum deadline
        long waktu1Jam = deadlineMillis - (1 * 60 * 60 * 1000);
        if (waktu1Jam > System.currentTimeMillis()) {
            Intent intent1 = new Intent(context, NotificationReceiver.class);
            intent1.putExtra("judul", "Pengingat Tugas");
            intent1.putExtra("message", "Deadline \"" + judul + "\" tinggal 1 jam lagi!");
            PendingIntent pendingIntent1 = PendingIntent.getBroadcast(
                    context, requestCode1, intent1,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, waktu1Jam, pendingIntent1);
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, waktu1Jam, pendingIntent1);
            }
        }
    }



    private void addMatkulCard(String matkulId, String matkul) {
        TextView tv = new TextView(this);
        tv.setText(matkul);
        tv.setPadding(20, 20, 20, 20);
        tv.setBackgroundColor(Color.parseColor("#B2EBF2"));
        tv.setTextColor(Color.BLACK);
        tv.setTextSize(16);
        tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(400, 300);
        lp.setMargins(8, 8, 8, 8);
        tv.setLayoutParams(lp);

        tv.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ActivityCatatanKuliah.class);
            intent.putExtra("username", username);
            intent.putExtra("matkulId", matkulId);
            startActivity(intent);
        });

        layoutMataKuliah.addView(tv);
    }

    private void addTugasCard(String tugasId, String judul, String tanggal, String jam, String matkul) {
        TextView tv = new TextView(this);
        tv.setText(judul + "\n" + tanggal + " " + jam + "\n" + matkul);
        tv.setPadding(20, 20, 20, 20);

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            Date deadline = sdf.parse(tanggal + " " + jam);
            long timeDiff = deadline.getTime() - System.currentTimeMillis();

            if (timeDiff <= 86400000) {
                tv.setBackgroundColor(Color.parseColor("#FFCDD2"));
            } else {
                tv.setBackgroundColor(Color.parseColor("#B2EBF2"));
            }
        } catch (Exception e) {
            tv.setBackgroundColor(Color.parseColor("#B2EBF2"));
        }

        tv.setTextColor(Color.BLACK);
        tv.setTextSize(16);
        tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(400, 300);
        lp.setMargins(8, 8, 8, 8);
        tv.setLayoutParams(lp);

        // Tambahkan aksi klik untuk membuka detail tugas
        tv.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ActivityDetailTugas.class);
            intent.putExtra("tugasId", tugasId);
            intent.putExtra("judul", judul);
            intent.putExtra("tanggal", tanggal);
            intent.putExtra("jam", jam);
            intent.putExtra("matkul", matkul);
            intent.putExtra("username", username);
            startActivity(intent);
        });

        layoutDeadline.addView(tv);
    }

    private void showTambahMatkulPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        View view = getLayoutInflater().inflate(R.layout.activity_popup_mk, null);

        EditText etNama = view.findViewById(R.id.etNama);
        EditText etHari = view.findViewById(R.id.etHari);
        EditText etJam = view.findViewById(R.id.etJam);
        EditText etRuangan = view.findViewById(R.id.etRuangan);
        Button btnKonfirmasi = view.findViewById(R.id.btnKonfirmasi);
        ImageButton btnBack = view.findViewById(R.id.btnBack);

        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialog.show();

        btnBack.setOnClickListener(v -> dialog.dismiss());

        btnKonfirmasi.setOnClickListener(v -> {
            String nama = etNama.getText().toString().trim();
            String hari = etHari.getText().toString().trim();
            String jam = etJam.getText().toString().trim();
            String ruangan = etRuangan.getText().toString().trim();

            if (nama.isEmpty()) {
                etNama.setError("Wajib diisi");
                return;
            }

            String matkulInfo = nama + " - " + hari + " - " + jam + " - " + ruangan;
            DatabaseReference matkulRef = dbRef.child("mataKuliah");
            String id = matkulRef.push().getKey();

            matkulRef.child(id).setValue(matkulInfo).addOnSuccessListener(aVoid -> {
                Toast.makeText(MainActivity.this, "Mata kuliah ditambahkan", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                loadUserData();
            }).addOnFailureListener(e -> {
                Toast.makeText(MainActivity.this, "Gagal tambah: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void showTambahTugasPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        View view = getLayoutInflater().inflate(R.layout.activity_popup_tambah_tugas, null);

        EditText etJudul = view.findViewById(R.id.etJudulTugas);
        EditText etTanggal = view.findViewById(R.id.etTanggal);
        EditText etJam = view.findViewById(R.id.etJam);
        EditText etMatkul = view.findViewById(R.id.etMatkul);
        Button btnKonfirmasi = view.findViewById(R.id.btnKonfirmasiTugas);
        ImageButton btnBack = view.findViewById(R.id.btnBackTugas);

        final Calendar calendar = Calendar.getInstance();

        etTanggal.setFocusable(false);
        etTanggal.setOnClickListener(v -> {
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this,
                    (view1, year1, month1, dayOfMonth) -> {
                        String formattedDate = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month1 + 1, year1);
                        etTanggal.setText(formattedDate);
                    }, year, month, day);
            datePickerDialog.show();
        });

        etJam.setFocusable(false);
        etJam.setOnClickListener(v -> {
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(MainActivity.this,
                    (view12, hourOfDay, minute1) -> {
                        String formattedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute1);
                        etJam.setText(formattedTime);
                    }, hour, minute, true);
            timePickerDialog.show();
        });

        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialog.show();

        btnBack.setOnClickListener(v -> dialog.dismiss());

        btnKonfirmasi.setOnClickListener(v -> {
            String judul = etJudul.getText().toString().trim();
            String tanggal = etTanggal.getText().toString().trim();
            String jam = etJam.getText().toString().trim();
            String matkul = etMatkul.getText().toString().trim();

            if (judul.isEmpty()) {
                etJudul.setError("Judul wajib diisi");
                return;
            }
            if (tanggal.isEmpty()) {
                etTanggal.setError("Tanggal wajib diisi");
                return;
            }
            if (jam.isEmpty()) {
                etJam.setError("Jam wajib diisi");
                return;
            }
            if (matkul.isEmpty()) {
                etMatkul.setError("Mata kuliah wajib diisi");
                return;
            }

            DatabaseReference tugasRef = dbRef.child("tugas");
            String id = tugasRef.push().getKey();

            tugasRef.child(id).child("judul").setValue(judul);
            tugasRef.child(id).child("tanggal").setValue(tanggal);
            tugasRef.child(id).child("jam").setValue(jam);
            tugasRef.child(id).child("matkul").setValue(matkul)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(MainActivity.this, "Tugas ditambahkan", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        loadUserData();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(MainActivity.this, "Gagal menambah tugas: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }

    public static class MataKuliah {
        String id;
        String nama;

        public MataKuliah(String id, String nama) {
            this.id = id;
            this.nama = nama;
        }
    }

    public static class Tugas {
        String id, judul, tanggal, jam, matkul;

        public Tugas(String id, String judul, String tanggal, String jam, String matkul) {
            this.id = id;
            this.judul = judul;
            this.tanggal = tanggal;
            this.jam = jam;
            this.matkul = matkul;
        }

        public Date getDeadlineDate() {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                return sdf.parse(tanggal + " " + jam);
            } catch (Exception e) {
                e.printStackTrace();
                return new Date(Long.MAX_VALUE); // fallback jika gagal
            }
        }
    }
}
