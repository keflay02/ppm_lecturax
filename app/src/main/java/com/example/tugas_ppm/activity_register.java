package com.example.tugas_ppm;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class activity_register extends AppCompatActivity {

    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        // Padding untuk sistem insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Referensi komponen UI
        EditText namaInput = findViewById(R.id.namaInput);
        EditText usernameInput = findViewById(R.id.usernameInput);
        EditText passwordInput = findViewById(R.id.passwordInput);
        Button confirmButton = findViewById(R.id.confirmButton);

        // Inisialisasi Firebase Realtime Database
        databaseRef = FirebaseDatabase.getInstance().getReference("users");

        // Aksi tombol konfirmasi
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nama = namaInput.getText().toString().trim();
                String username = usernameInput.getText().toString().trim();
                String password = passwordInput.getText().toString().trim();

                if (nama.isEmpty() || username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(activity_register.this, "Semua kolom harus diisi", Toast.LENGTH_SHORT).show();
                } else {
                    // Buat ID unik
                    String userId = databaseRef.push().getKey();

                    // Buat objek user
                    User user = new User(nama, username, password);

                    if (userId != null) {
                        databaseRef.child(userId).setValue(user)
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(activity_register.this, "Registrasi berhasil!", Toast.LENGTH_SHORT).show();

                                    // Pindah ke login
                                    Intent intent = new Intent(activity_register.this, activity_login.class);
                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(activity_register.this, "Gagal menyimpan data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                }
            }
        });
    }

    // Kelas User sebagai model data
    public static class User {
        public String nama;
        public String username;
        public String password;

        public User() {} // Diperlukan Firebase

        public User(String nama, String username, String password) {
            this.nama = nama;
            this.username = username;
            this.password = password;
        }
    }
}
