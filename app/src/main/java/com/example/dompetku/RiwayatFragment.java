package com.example.dompetku;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class RiwayatFragment extends Fragment {

    private RecyclerView recyclerView;
    private TransaksiAdapter adapter;
    private List<Transaksi> transaksiList;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_riwayat, container, false);

        recyclerView = view.findViewById(R.id.recyclerRiwayat);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        transaksiList = new ArrayList<>();

        //untuk Edit & Hapus
        adapter = new TransaksiAdapter(getContext(), transaksiList, new TransaksiAdapter.OnItemActionListener() {
            @Override
            public void onEdit(Transaksi transaksi) {
                // Pindah ke TambahTransaksiActivity untuk edit
                Intent intent = new Intent(getContext(), TambahTransaksiActivity.class);
                intent.putExtra("EDIT_DATA", transaksi); // Kirim data transaksi
                startActivity(intent);
            }

            @Override
            public void onDelete(Transaksi transaksi) {
                // Hapus data di Firestore
                hapusTransaksi(transaksi);
            }
        });

        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        loadData();

        return view;
    }

    private void loadData() {
        String userId = auth.getCurrentUser().getUid();
        db.collection("users").document(userId).collection("transaksi")
                .orderBy("tanggal", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) return;

                    transaksiList.clear();
                    for (DocumentSnapshot doc : snapshots) {
                        Transaksi t = doc.toObject(Transaksi.class);
                        if (t != null) {
                            t.setId(doc.getId()); // Simpan ID untuk hapus
                            transaksiList.add(t);
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    private void hapusTransaksi(Transaksi transaksi) {
        String userId = auth.getCurrentUser().getUid();
        db.collection("users").document(userId)
                .collection("transaksi")
                .document(transaksi.getId())
                .delete()
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(getContext(), "Transaksi dihapus", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Gagal menghapus: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
