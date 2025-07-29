package com.example.dompetku;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private ImageView profileIcon;
    private TextView tvFullName, tvEmail;
    private EditText etNomorHp, etTanggalLahir;
    private Button btnSimpanProfil, btnLogout;

    private TextView tvSaldo, tvJumlahTransaksi, tvPengeluaran, tvPemasukan;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    public ProfileFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        profileIcon = view.findViewById(R.id.profileIcon);
        tvFullName = view.findViewById(R.id.tvFullName);
        tvEmail = view.findViewById(R.id.tvEmail);
        etNomorHp = view.findViewById(R.id.etNomorHp);
        etTanggalLahir = view.findViewById(R.id.etTanggalLahir);
        btnSimpanProfil = view.findViewById(R.id.btnSimpanProfil);
        btnLogout = view.findViewById(R.id.btnLogout);

        tvSaldo = view.findViewById(R.id.tvSaldo);
        tvJumlahTransaksi = view.findViewById(R.id.tvJumlahTransaksi);
        tvPengeluaran = view.findViewById(R.id.tvPengeluaran);
        tvPemasukan = view.findViewById(R.id.tvPemasukan);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        loadProfileData();
        loadStatistik();

        etTanggalLahir.setFocusable(false);
        etTanggalLahir.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePicker = new DatePickerDialog(getContext(), (view1, year1, month1, dayOfMonth) -> {
                String selectedDate = dayOfMonth + "/" + (month1 + 1) + "/" + year1;
                etTanggalLahir.setText(selectedDate);
            }, year, month, day);

            datePicker.show();
        });

        btnSimpanProfil.setOnClickListener(v -> {
            String nomorHp = etNomorHp.getText().toString().trim();
            String tanggalLahir = etTanggalLahir.getText().toString().trim();

            if (nomorHp.isEmpty() || tanggalLahir.isEmpty()) {
                Toast.makeText(getContext(), "Isi semua data!", Toast.LENGTH_SHORT).show();
                return;
            }

            updateProfileData(nomorHp, tanggalLahir);
        });

        btnLogout.setOnClickListener(v -> {
            auth.signOut();
            startActivity(new Intent(getActivity(), LoginActivity.class));
            requireActivity().finish();
        });

        return view;
    }

    private void loadProfileData() {
        String userId = auth.getCurrentUser().getUid();

        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String fullName = documentSnapshot.getString("fullName");
                        String email = documentSnapshot.getString("email");
                        String nomorHp = documentSnapshot.getString("nomorHp");
                        String tanggalLahir = documentSnapshot.getString("tanggalLahir");

                        tvFullName.setText(fullName != null ? fullName : "Nama Lengkap");
                        tvEmail.setText(email != null ? email : "email@example.com");
                        etNomorHp.setText(nomorHp != null ? nomorHp : "");
                        etTanggalLahir.setText(tanggalLahir != null ? tanggalLahir : "");
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Gagal memuat profil: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateProfileData(String nomorHp, String tanggalLahir) {
        String userId = auth.getCurrentUser().getUid();
        DocumentReference userRef = db.collection("users").document(userId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("nomorHp", nomorHp);
        updates.put("tanggalLahir", tanggalLahir);

        userRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Gagal update: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadStatistik() {
        String userId = auth.getCurrentUser().getUid();
        String bulanSekarang = new SimpleDateFormat("MM-yyyy", Locale.getDefault()).format(new Date());

        db.collection("users").document(userId).collection("transaksi")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int totalSaldo = 0;
                    int jumlahTransaksi = querySnapshot.size();
                    int pengeluaranBulanIni = 0;
                    int pemasukanBulanIni = 0;

                    for (DocumentSnapshot doc : querySnapshot) {
                        Transaksi t = doc.toObject(Transaksi.class);
                        if (t != null) {
                            if ("Pemasukan".equalsIgnoreCase(t.getJenis())) {
                                totalSaldo += t.getNominal();
                                if (t.getTanggal().contains(bulanSekarang)) {
                                    pemasukanBulanIni += t.getNominal();
                                }
                            } else if ("Pengeluaran".equalsIgnoreCase(t.getJenis())) {
                                totalSaldo -= t.getNominal();
                                if (t.getTanggal().contains(bulanSekarang)) {
                                    pengeluaranBulanIni += t.getNominal();
                                }
                            }
                        }
                    }

                    tvSaldo.setText("Rp " + totalSaldo);
                    tvJumlahTransaksi.setText(String.valueOf(jumlahTransaksi));
                    tvPengeluaran.setText("Rp " + pengeluaranBulanIni);
                    tvPemasukan.setText("Rp " + pemasukanBulanIni);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Gagal memuat statistik", Toast.LENGTH_SHORT).show();
                });
    }
}
