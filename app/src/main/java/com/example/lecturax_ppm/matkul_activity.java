import android.os.Bundle;
import android.widget.GridView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class MataKuliahActivity extends AppCompatActivity {

    private GridView gridViewMataKuliah;
    private List<String> daftarMataKuliah; // Ganti dengan model data mata kuliah Anda
    private MataKuliahAdapter adapter; // Buat adapter kustom Anda

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mata_kuliah);

        gridViewMataKuliah = findViewById(R.id.gridViewMataKuliah);

        // Inisialisasi data mata kuliah (ganti dengan data Anda)
        daftarMataKuliah = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            daftarMataKuliah.add("Mata Kuliah " + i);
        }

        // Inisialisasi adapter kustom (Anda perlu membuatnya)
        adapter = new MataKuliahAdapter(this, daftarMataKuliah);
        gridViewMataKuliah.setAdapter(adapter);

        // Tambahkan logika untuk tombol kembali, search, dan FAB di sini
    }
}