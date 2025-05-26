package com.example.tugas_ppm;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;


import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;


public class activity_tambah_file extends AppCompatActivity {

    private static final int FILE_SELECT_CODE = 1;
    private Uri fileUri;
    private Button btnUpload;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambah_file);

        // 👉 Cek izin penyimpanan (WAJIB untuk Android 6 - 10)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        Button btnUpload = findViewById(R.id.btnUploadFile);
        findViewById(R.id.btnChooseFile).setOnClickListener(v -> openFileChooser());

        btnUpload.setOnClickListener(v -> {
            if (fileUri != null) {
                saveFileToLocal(fileUri);  // Fungsi ini untuk menyimpan ke memori lokal
            } else {
                Toast.makeText(this, "Pilih file terlebih dahulu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, FILE_SELECT_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FILE_SELECT_CODE && resultCode == RESULT_OK && data != null) {
            fileUri = data.getData();
        }
    }

    private void saveFileToLocal(Uri sourceUri) {
        try {
            // Buat nama file tujuan
            String fileName = System.currentTimeMillis() + "_" + sourceUri.getLastPathSegment();

            // Lokasi penyimpanan di folder internal app (tersimpan di Android/data/)
            File destinationFile = new File(getExternalFilesDir(null), fileName);

            // Buka input stream dari file yang dipilih
            InputStream inputStream = getContentResolver().openInputStream(sourceUri);
            OutputStream outputStream = new FileOutputStream(destinationFile);

            // Salin file
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            inputStream.close();
            outputStream.close();

            Toast.makeText(this, "File disimpan ke: " + destinationFile.getAbsolutePath(), Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Gagal menyimpan file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

}
