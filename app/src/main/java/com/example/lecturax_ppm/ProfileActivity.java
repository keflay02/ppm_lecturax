package com.example.lecturax_ppm;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    EditText email, username, instansi, prodi;
    Button btnKonfirmasi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile); // Pastikan nama XML sesuai

        email = findViewById(R.id.email);
        username = findViewById(R.id.username);
        instansi = findViewById(R.id.instansi);
        prodi = findViewById(R.id.prodi);
        btnKonfirmasi = findViewById(R.id.btnKonfirmasi);

        btnKonfirmasi.setOnClickListener(v -> {
            String data = "Email: " + email.getText().toString() + "\n" +
                    "Username: " + username.getText().toString() + "\n" +
                    "Instansi: " + instansi.getText().toString() + "\n" +
                    "Prodi: " + prodi.getText().toString();

            Toast.makeText(ProfileActivity.this, data, Toast.LENGTH_LONG).show();
        });
    }
}
