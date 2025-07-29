package com.example.dompetku;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RekapFragment extends Fragment {

    private Spinner filterKategori;
    private Button btnFilter;
    private RecyclerView rekapRecyclerView;
    private PieChart pieChart;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private TransaksiAdapter adapter;
    private List<Transaksi> transaksiList;

    public RekapFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_rekap, container, false);

        filterKategori = view.findViewById(R.id.filterKategori);
        btnFilter = view.findViewById(R.id.btnFilter);
        rekapRecyclerView = view.findViewById(R.id.rekapRecyclerView);
        pieChart = view.findViewById(R.id.pieChart);

        String[] kategoriList = {"Semua", "Makanan", "Transportasi", "Gaji", "Belanja", "Lainnya"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, kategoriList);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterKategori.setAdapter(spinnerAdapter);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        transaksiList = new ArrayList<>();
        adapter = new TransaksiAdapter(getContext(), transaksiList, new TransaksiAdapter.OnItemActionListener() {
            @Override
            public void onEdit(Transaksi transaksi) {
                Toast.makeText(getContext(), "Edit data: " + transaksi.getKategori(), Toast.LENGTH_SHORT).show();
                // TODO: Intent ke TambahTransaksiActivity dengan data transaksi
            }

            @Override
            public void onDelete(Transaksi transaksi) {
                hapusTransaksi(transaksi);
            }
        });

        rekapRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        rekapRecyclerView.setAdapter(adapter);

        btnFilter.setOnClickListener(v -> loadData());

        loadData();

        return view;
    }

    private void loadData() {
        String userId = auth.getCurrentUser().getUid();
        String selectedKategori = filterKategori.getSelectedItem().toString();

        db.collection("users")
                .document(userId)
                .collection("transaksi")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    transaksiList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Transaksi transaksi = doc.toObject(Transaksi.class);
                        transaksi.setId(doc.getId());

                        boolean cocokKategori = selectedKategori.equals("Semua")
                                || transaksi.getKategori().equalsIgnoreCase(selectedKategori);

                        if (cocokKategori) {
                            transaksiList.add(transaksi);
                        }
                    }
                    adapter.updateData(transaksiList);
                    tampilkanPieChart();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Gagal memuat data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void tampilkanPieChart() {
        Map<String, Integer> totalPerKategori = new HashMap<>();

        for (Transaksi t : transaksiList) {
            if (t.getJenis().equalsIgnoreCase("Pengeluaran")) {
                String kategori = t.getKategori();
                int nominal = t.getNominal();
                int total = totalPerKategori.getOrDefault(kategori, 0);
                totalPerKategori.put(kategori, total + nominal);
            }
        }

        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : totalPerKategori.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Pengeluaran");
        dataSet.setColors(Color.RED, Color.BLUE, Color.GREEN, Color.MAGENTA, Color.CYAN);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setCenterText("Ringkasan");
        pieChart.animateY(1000);
        pieChart.invalidate();
    }

    private void hapusTransaksi(Transaksi transaksi) {
        String userId = auth.getCurrentUser().getUid();
        db.collection("users")
                .document(userId)
                .collection("transaksi")
                .document(transaksi.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Transaksi dihapus", Toast.LENGTH_SHORT).show();
                    loadData();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Gagal hapus: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
