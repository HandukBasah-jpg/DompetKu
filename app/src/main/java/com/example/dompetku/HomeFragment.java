package com.example.dompetku;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private TransaksiAdapter adapter;
    private List<Transaksi> transaksiList;
    private TextView tvTotalPemasukan, tvTotalPengeluaran;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    public HomeFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        tvTotalPemasukan = view.findViewById(R.id.tvTotalPemasukan);
        tvTotalPengeluaran = view.findViewById(R.id.tvTotalPengeluaran);

        transaksiList = new ArrayList<>();

        //Edit & Hapus
        adapter = new TransaksiAdapter(getContext(), transaksiList, new TransaksiAdapter.OnItemActionListener() {
            @Override
            public void onEdit(Transaksi transaksi) {
                Intent intent = new Intent(getContext(), TambahTransaksiActivity.class);
                intent.putExtra("EDIT_DATA", transaksi); // ✅ Kirim Serializable
                startActivity(intent);
            }

            @Override
            public void onDelete(Transaksi transaksi) {
                new AlertDialog.Builder(getContext())
                        .setTitle("Hapus Transaksi")
                        .setMessage("Yakin ingin menghapus transaksi ini?")
                        .setPositiveButton("Hapus", (dialog, which) -> hapusTransaksi(transaksi))
                        .setNegativeButton("Batal", null)
                        .show();
            }
        });


        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        FloatingActionButton fab = view.findViewById(R.id.fabTambahTransaksi);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), TambahTransaksiActivity.class);
            startActivity(intent);
        });

        loadData();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        String userId = auth.getCurrentUser().getUid();
        db.collection("users")
                .document(userId)
                .collection("transaksi")
                .orderBy("tanggal", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    transaksiList.clear();
                    int totalPemasukan = 0;
                    int totalPengeluaran = 0;

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Transaksi transaksi = doc.toObject(Transaksi.class);
                        if (transaksi != null) {
                            transaksi.setId(doc.getId());
                            transaksiList.add(transaksi);

                            if ("Pemasukan".equalsIgnoreCase(transaksi.getJenis())) {
                                totalPemasukan += transaksi.getNominal();
                            } else if ("Pengeluaran".equalsIgnoreCase(transaksi.getJenis())) {
                                totalPengeluaran += transaksi.getNominal();
                            }
                        }
                    }

                    NumberFormat formatRupiah = NumberFormat.getInstance(new Locale("id", "ID"));
                    tvTotalPemasukan.setText("Total Pemasukan: Rp " + formatRupiah.format(totalPemasukan));
                    tvTotalPengeluaran.setText("Total Pengeluaran: Rp " + formatRupiah.format(totalPengeluaran));

                    adapter.updateData(transaksiList);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Gagal memuat data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
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
