package com.example.tugas_ppm;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.view.View;
import android.app.AlertDialog;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ActivityLihatFile extends AppCompatActivity {

    private ListView listView;
    private List<String> fileList;
    private ArrayAdapter<String> adapter;
    private File directory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lihat_file);

        listView = findViewById(R.id.listViewFiles);
        fileList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, fileList);
        listView.setAdapter(adapter);

        directory = getExternalFilesDir(null);

        loadFileList();

        // Klik untuk membuka file
        listView.setOnItemClickListener((parent, view, position, id) -> {
            String fileName = fileList.get(position);
            File file = new File(directory, fileName);
            openFile(file);
        });

        // Tekan lama untuk menghapus file
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            String fileName = fileList.get(position);
            File file = new File(directory, fileName);

            new AlertDialog.Builder(this)
                    .setTitle("Hapus File")
                    .setMessage("Apakah Anda yakin ingin menghapus \"" + fileName + "\"?")
                    .setPositiveButton("Hapus", (dialog, which) -> {
                        if (file.exists() && file.delete()) {
                            Toast.makeText(this, "File dihapus", Toast.LENGTH_SHORT).show();
                            loadFileList(); // Refresh list
                        } else {
                            Toast.makeText(this, "Gagal menghapus file", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Batal", null)
                    .show();
            return true;
        });
    }

    private void loadFileList() {
        fileList.clear();
        if (directory != null && directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    fileList.add(file.getName());
                }
            }
            adapter.notifyDataSetChanged();
        }
    }

    private void openFile(File file) {
        try {
            Uri uri = FileProvider.getUriForFile(
                    this,
                    getApplicationContext().getPackageName() + ".provider",
                    file
            );

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, getMimeType(file.getName()));
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(intent, "Buka file dengan"));
        } catch (Exception e) {
            Toast.makeText(this, "Tidak bisa membuka file", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private String getMimeType(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        switch (extension) {
            case "pdf": return "application/pdf";
            case "txt": return "text/plain";
            case "jpg":
            case "jpeg": return "image/jpeg";
            case "png": return "image/png";
            case "mp4": return "video/mp4";
            case "mp3": return "audio/mpeg";
            case "doc":
            case "docx": return "application/msword";
            case "xls":
            case "xlsx": return "application/vnd.ms-excel";
            case "ppt":
            case "pptx": return "application/vnd.ms-powerpoint";
            default: return "*/*";
        }
    }
}
