package com.example.tugas_ppm;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.content.SharedPreferences;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class activity_login extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private AppCompatButton btnRegister, btnLogin;
    private EditText usernameInput, passwordInput;

    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btnRegister = findViewById(R.id.btnRegister);
        btnLogin = findViewById(R.id.btnLogin);
        usernameInput = findViewById(R.id.editTextUsername);
        passwordInput = findViewById(R.id.editTextPassword);

        databaseRef = FirebaseDatabase.getInstance().getReference("users");

        btnRegister.setOnClickListener(view -> {
            Intent i = new Intent(activity_login.this, activity_register.class);
            startActivity(i);
        });

        btnLogin.setOnClickListener(view -> {
            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(activity_login.this, "Isi username dan password!", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d(TAG, "Mencari user dengan username: " + username);

            databaseRef.orderByChild("username").equalTo(username).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    Log.d(TAG, "DataSnapshot ada? " + snapshot.exists());

                    if (snapshot.exists()) {
                        boolean passwordMatch = false;
                        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                            String dbPassword = userSnapshot.child("password").getValue(String.class);
                            Log.d(TAG, "Password dari DB: " + dbPassword);

                            if (password.equals(dbPassword)) {
                                passwordMatch = true;
                                Log.d(TAG, "Password cocok, login berhasil");

                                String uid = userSnapshot.getKey(); // Ambil UID (key Firebase)

                                // ✅ SIMPAN userKey ke SharedPreferences
                                SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("userKey", uid);
                                editor.putString("username", username);
                                editor.apply();

                                Intent i = new Intent(activity_login.this, MainActivity.class);
                                i.putExtra("uid", uid);
                                i.putExtra("username", username); // Optional
                                startActivity(i);
                                finish(); // Penting agar login tidak kembali saat back
                            }

                        }
                        if (!passwordMatch) {
                            Log.d(TAG, "Password salah");
                            Toast.makeText(activity_login.this, "Password salah!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.d(TAG, "Username tidak ditemukan");
                        Toast.makeText(activity_login.this, "Username tidak ditemukan!", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Log.e(TAG, "Database error: " + error.getMessage());
                    Toast.makeText(activity_login.this, "Terjadi kesalahan: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
