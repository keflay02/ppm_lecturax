package com.example.tugas_ppm;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import androidx.annotation.Nullable;
import java.util.ArrayList;


import com.google.firebase.database.*;

import java.util.Locale;

public class ActivityCatatanKuliah extends AppCompatActivity {

    TextView tvNamaMatkul;
    EditText editCatatan;
    Button btnSimpan;
    ImageView btnhome;
    ImageButton btnBack;
    String matkulId, username;
    private DatabaseReference dbUserRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_matkul);

        tvNamaMatkul = findViewById(R.id.judulCatatan);
        editCatatan = findViewById(R.id.editCatatan);
        btnSimpan = findViewById(R.id.btnSimpanCatatan);
        btnBack = findViewById(R.id.btnBack);
        btnhome = findViewById(R.id.home_button);
        Button btnHapusMK = findViewById(R.id.btnhapusMK); // Pastikan ID ini sesuai dengan di XML

        // Ambil data dari Intent
        matkulId = getIntent().getStringExtra("matkulId");
        username = getIntent().getStringExtra("username");

        if (matkulId == null || username == null) {
            Toast.makeText(this, "Data tidak lengkap", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Tombol speech-to-text
        ImageButton btnMic = findViewById(R.id.btnMic);
        btnMic.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO}, 1);
            } else {
                startSpeechToText();
            }
        });

        // Tombol kembali ke MainActivity
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityCatatanKuliah.this, MainActivity.class);
            intent.putExtra("username", username);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        btnhome.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityCatatanKuliah.this, MainActivity.class);
            intent.putExtra("username", username);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        // Tombol hapus MK
        btnHapusMK.setOnClickListener(v -> showHapusPopup());

        loadMatkulDetail();

        // Tombol simpan catatan
        btnSimpan.setOnClickListener(v -> {
            String isiCatatan = editCatatan.getText().toString().trim();

            if (dbUserRef != null) {
                dbUserRef.child("catatan").child(matkulId).setValue(isiCatatan)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(ActivityCatatanKuliah.this, "Catatan berhasil disimpan", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(ActivityCatatanKuliah.this, "Gagal menyimpan catatan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } else {
                Toast.makeText(ActivityCatatanKuliah.this, "User belum ditemukan", Toast.LENGTH_SHORT).show();
            }
        });
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

    private void showHapusPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.popup_hapusmk, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        Button btnBatal = dialogView.findViewById(R.id.btnBatal);
        Button btnHapus = dialogView.findViewById(R.id.btnHapus);

        btnBatal.setOnClickListener(v -> dialog.dismiss());

        btnHapus.setOnClickListener(v -> {
            if (dbUserRef != null) {
                dbUserRef.child("mataKuliah").child(matkulId).removeValue();
                dbUserRef.child("catatan").child(matkulId).removeValue();
                Toast.makeText(this, "Mata kuliah berhasil dihapus", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(ActivityCatatanKuliah.this, MainActivity.class);
                intent.putExtra("username", username);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Data user tidak ditemukan", Toast.LENGTH_SHORT).show();
            }
            dialog.dismiss();
        });

        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                editCatatan.append("\n" + result.get(0));
            }
        }
    }

    private void loadMatkulDetail() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        usersRef.orderByChild("username").equalTo(username)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot userSnap : snapshot.getChildren()) {
                                DatabaseReference matkulRef = userSnap.child("mataKuliah").child(matkulId).getRef();

                                matkulRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot matkulSnap) {
                                        String data = matkulSnap.getValue(String.class);
                                        if (data != null) {
                                            String[] parts = data.split(" - ");
                                            if (parts.length == 4) {
                                                tvNamaMatkul.setText(parts[0]);
                                            }
                                        }

                                        DataSnapshot catatanSnap = userSnap.child("catatan").child(matkulId);
                                        String catatan = catatanSnap.getValue(String.class);
                                        if (catatan != null) {
                                            editCatatan.setText(catatan);
                                        } else {
                                            editCatatan.setText("");
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Toast.makeText(ActivityCatatanKuliah.this, "Gagal memuat mata kuliah", Toast.LENGTH_SHORT).show();
                                    }
                                });

                                dbUserRef = userSnap.getRef();
                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ActivityCatatanKuliah.this, "Gagal mengambil data user", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
