package com.example.lecturax; // Ganti dengan nama package aplikasi Anda

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private EditText searchBar;
    private TextView seeAll;
    private LinearLayout listMatkul; // Menggunakan LinearLayout yang ada di HorizontalScrollView
    private List<MataKuliahItem> allMataKuliahItems = new ArrayList<>();
    private List<MataKuliahItem> displayedMataKuliahItems = new ArrayList<>();

    // Inner class untuk merepresentasikan item mata kuliah
    private static class MataKuliahItem {
        String hari;
        String waktu;
        String namaMatkul;
        String detailMatkul;

        public MataKuliahItem(String hari, String waktu, String namaMatkul, String detailMatkul) {
            this.hari = hari;
            this.waktu = waktu;
            this.namaMatkul = namaMatkul;
            this.detailMatkul = detailMatkul;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home); // Pastikan nama layout Anda benar

        // Inisialisasi View
        searchBar = findViewById(R.id.searchBar);
        seeAll = findViewById(R.id.seeAll);
        listMatkul = findViewById(R.id.listMatkul);

        // Inisialisasi daftar semua item mata kuliah (contoh data)
        allMataKuliahItems.add(new MataKuliahItem("Senin", "09:00", "Lab Jaringan", "Analisis Desain Sistem Info..."));
        allMataKuliahItems.add(new MataKuliahItem("Senin", "09:00", "Lab Jaringan", "Pemrograman Perangkat Mobile"));
        allMataKuliahItems.add(new MataKuliahItem("Senin", "09:00", "Lab Jaringan", "Teknologi multimedia"));
        // Tambahkan data mata kuliah lainnya sesuai kebutuhan

        displayedMataKuliahItems.addAll(allMataKuliahItems);
        updateMataKuliahDisplay();

        // Logika SearchView
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Tidak diperlukan
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterMataKuliah(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Tidak diperlukan
            }
        });

        // Logika TextView "See All"
        seeAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent untuk menuju Activity yang menampilkan semua mata kuliah
                Intent seeAllIntent = new Intent(HomeActivity.this, AllMataKuliahActivity.class);
                startActivity(seeAllIntent);
            }
        });
    }

    private void filterMataKuliah(String searchText) {
        displayedMataKuliahItems.clear();
        if (searchText.isEmpty()) {
            displayedMataKuliahItems.addAll(allMataKuliahItems);
        } else {
            searchText = searchText.toLowerCase();
            for (MataKuliahItem item : allMataKuliahItems) {
                if (item.namaMatkul.toLowerCase().contains(searchText) || item.detailMatkul.toLowerCase().contains(searchText)) {
                    displayedMataKuliahItems.add(item);
                }
            }
        }
        updateMataKuliahDisplay();
    }

    private void updateMataKuliahDisplay() {
        listMatkul.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(this);

        for (MataKuliahItem item : displayedMataKuliahItems) {
            View itemView = inflater.inflate(R.layout.item_mata_kuliah, listMatkul, false);

            TextView hariTextView = itemView.findViewById(R.id.hariTextView);
            TextView waktuTextView = itemView.findViewById(R.id.waktuTextView);
            TextView namaMatkulTextView = itemView.findViewById(R.id.namaMatkulTextView);
            TextView detailMatkulTextView = itemView.findViewById(R.id.detailMatkulTextView);

            hariTextView.setText(item.hari);
            waktuTextView.setText(item.waktu);
            namaMatkulTextView.setText(item.namaMatkul);
            detailMatkulTextView.setText(item.detailMatkul);

            listMatkul.addView(itemView);
        }
    }
}