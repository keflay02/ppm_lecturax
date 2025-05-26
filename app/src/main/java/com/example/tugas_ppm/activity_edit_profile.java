package com.example.tugas_ppm;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class activity_edit_profile extends AppCompatActivity {

    private EditText editName, editUsername, editPassword;
    private Button btnSave;
    private ImageView btnBack;
    private DatabaseReference usersRef;
    private String userKey;
    private SharedPreferences sharedPreferences;
    private String currentUsername = ""; // ✅ Tambahkan variabel global untuk username

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Inisialisasi UI
        editName = findViewById(R.id.editName);
        editUsername = findViewById(R.id.editUsername);
        editPassword = findViewById(R.id.editPassword);
        btnSave = findViewById(R.id.saveButton);
        btnBack = findViewById(R.id.btnBackProfile);

        // Ambil userKey dari SharedPreferences
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userKey = sharedPreferences.getString("userKey", null);

        if (userKey == null) {
            Toast.makeText(this, "Gagal memuat profil. Data tidak ditemukan.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Ambil data user langsung berdasarkan userKey
        usersRef.child(userKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    editName.setText(snapshot.child("nama").getValue(String.class));
                    currentUsername = snapshot.child("username").getValue(String.class); // ✅ Simpan ke variabel global
                    editUsername.setText(currentUsername);
                    editPassword.setText(snapshot.child("password").getValue(String.class));
                } else {
                    Toast.makeText(activity_edit_profile.this, "Data pengguna tidak ditemukan.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(activity_edit_profile.this, "Gagal mengambil data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Tombol kembali ke MainActivity
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(activity_edit_profile.this, MainActivity.class);
            intent.putExtra("username", currentUsername); // ✅ Kirim username
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        // Tombol Simpan
        btnSave.setOnClickListener(v -> {
            String nama = editName.getText().toString().trim();
            String usernameBaru = editUsername.getText().toString().trim();
            String password = editPassword.getText().toString().trim();

            if (nama.isEmpty() || usernameBaru.isEmpty() || password.isEmpty()) {
                Toast.makeText(activity_edit_profile.this, "Semua kolom harus diisi", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update data di Firebase
            usersRef.child(userKey).child("nama").setValue(nama);
            usersRef.child(userKey).child("username").setValue(usernameBaru);
            usersRef.child(userKey).child("password").setValue(password)
                    .addOnSuccessListener(unused -> {
                        // Update SharedPreferences
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("username", usernameBaru);
                        editor.apply();

                        currentUsername = usernameBaru; // ✅ Update variabel global juga

                        Toast.makeText(activity_edit_profile.this, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(activity_edit_profile.this, MainActivity.class);
                        intent.putExtra("username", currentUsername);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(activity_edit_profile.this, "Gagal menyimpan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }
}
