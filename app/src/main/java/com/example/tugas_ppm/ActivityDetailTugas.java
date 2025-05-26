package com.example.tugas_ppm;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Locale;

public class ActivityDetailTugas extends AppCompatActivity {

    private ImageButton btnBack;
    private Button btnSimpanTugas, btnHapusTugas;
    private TextView btnLihatFile, btnAddFile, txtJudulTugas, txtMatkul, txtTanggal, txtJam;
    private EditText editDeskripsiTugas;
    private ImageView homeButton;

    private DatabaseReference tugasRef;
    private String tugasId, judul, tanggal, jam, matkul, username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_tugas);

        // Ambil data dari Intent
        Intent intent = getIntent();
        tugasId = intent.getStringExtra("tugasId");
        judul = intent.getStringExtra("judul");
        tanggal = intent.getStringExtra("tanggal");
        jam = intent.getStringExtra("jam");
        matkul = intent.getStringExtra("matkul");
        username = intent.getStringExtra("username");

        // Validasi data
        if (tugasId == null || judul == null) {
            Toast.makeText(this, "Data tidak lengkap", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Inisialisasi view
        btnBack = findViewById(R.id.btnBack);
        btnSimpanTugas = findViewById(R.id.btnSimpanTugas);
        btnHapusTugas = findViewById(R.id.btnHapusTugas);
        btnLihatFile = findViewById(R.id.btnLihatFile);
        btnAddFile = findViewById(R.id.btnAddFile);
        txtJudulTugas = findViewById(R.id.txtJudulTugas);
        txtMatkul = findViewById(R.id.titleTugas);
        txtTanggal = findViewById(R.id.tanggaltugas);
        txtJam = findViewById(R.id.jamtugas);
        editDeskripsiTugas = findViewById(R.id.editDeskripsiTugas);
        homeButton = findViewById(R.id.home_button);

        // Tampilkan data
        txtJudulTugas.setText(judul);
        txtMatkul.setText(matkul);
        txtTanggal.setText(tanggal);
        txtJam.setText(jam);

        // Referensi tugas di Firebase
        tugasRef = FirebaseDatabase.getInstance().getReference("tugas").child(tugasId);

        // Load deskripsi
        loadTugas();

        // Tombol kembali
        btnBack.setOnClickListener(v -> finish());

        // Simpan deskripsi
        btnSimpanTugas.setOnClickListener(v -> {
            String deskripsi = editDeskripsiTugas.getText().toString().trim();
            if (deskripsi.isEmpty()) {
                Toast.makeText(this, "Deskripsi tidak boleh kosong", Toast.LENGTH_SHORT).show();
                return;
            }

            tugasRef.child("deskripsi").setValue(deskripsi)
                    .addOnSuccessListener(aVoid ->
                            Toast.makeText(this, "Deskripsi disimpan", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Gagal menyimpan deskripsi", Toast.LENGTH_SHORT).show());
        });

        // Tombol speech-to-text
        ImageButton btnMic = findViewById(R.id.btnMic);
        btnMic.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO}, 1);
            } else {
                startSpeechToText();
            }
        });

        // Hapus tugas
        btnHapusTugas.setOnClickListener(v -> showDeleteConfirmationDialog());

        // Lihat file
        btnLihatFile.setOnClickListener(v -> {
            Intent lihatIntent = new Intent(this, ActivityLihatFile.class);
            lihatIntent.putExtra("tugasId", tugasId);
            startActivity(lihatIntent);
        });

        // Tambah file
        btnAddFile.setOnClickListener(v -> {
            Intent tambahIntent = new Intent(this, activity_tambah_file.class);
            tambahIntent.putExtra("tugasId", tugasId);
            startActivity(tambahIntent);
        });

        // Kembali ke home
        homeButton.setOnClickListener(v -> finish());
    }

    private void startSpeechToText() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Mulai bicara...");

        try {
            startActivityForResult(intent, 100);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Speech recognition tidak tersedia", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String text = result.get(0);
            editDeskripsiTugas.append("\n" + text);
        }
    }

    private void loadTugas() {
        tugasRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String deskripsi = snapshot.child("deskripsi").getValue(String.class);
                if (deskripsi != null) {
                    editDeskripsiTugas.setText(deskripsi);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ActivityDetailTugas.this, "Gagal memuat data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.popup_hapus_tugas, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        Button btnBatal = dialogView.findViewById(R.id.btnBatal);
        Button btnHapus = dialogView.findViewById(R.id.btnHapus);

        btnBatal.setOnClickListener(v -> dialog.dismiss());

        btnHapus.setOnClickListener(v -> {
            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
            usersRef.orderByChild("username").equalTo(username)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                for (DataSnapshot userSnap : snapshot.getChildren()) {
                                    userSnap.getRef().child("tugas").child(tugasId).removeValue()
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(ActivityDetailTugas.this, "Tugas berhasil dihapus", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(ActivityDetailTugas.this, MainActivity.class);
                                                intent.putExtra("username", username);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                startActivity(intent);
                                                finish();
                                            })
                                            .addOnFailureListener(e ->
                                                    Toast.makeText(ActivityDetailTugas.this, "Gagal menghapus tugas", Toast.LENGTH_SHORT).show());
                                    break;
                                }
                            } else {
                                Toast.makeText(ActivityDetailTugas.this, "User tidak ditemukan", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(ActivityDetailTugas.this, "Gagal mengakses database", Toast.LENGTH_SHORT).show();
                        }
                    });

            dialog.dismiss();
        });

        dialog.show();
    }
}
