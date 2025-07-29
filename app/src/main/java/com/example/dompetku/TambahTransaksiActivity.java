package com.example.dompetku;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TambahTransaksiActivity extends AppCompatActivity {

    private RadioGroup jenisGroup;
    private Spinner kategoriSpinner;
    private EditText nominalInput, tanggalInput, catatanInput;
    private Button simpanBtn;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private String currentNominal = "";

    private Transaksi transaksiEdit;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambah_transaksi);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        jenisGroup = findViewById(R.id.jenisGroup);
        kategoriSpinner = findViewById(R.id.kategoriSpinner);
        nominalInput = findViewById(R.id.nominalInput);
        tanggalInput = findViewById(R.id.tanggalInput);
        catatanInput = findViewById(R.id.catatanInput);
        simpanBtn = findViewById(R.id.simpanBtn);

        String[] kategoriList = {"Makanan", "Transportasi", "Gaji", "Belanja", "Lainnya"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, kategoriList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        kategoriSpinner.setAdapter(adapter);

        nominalInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().equals(currentNominal)) {
                    nominalInput.removeTextChangedListener(this);

                    String cleanString = s.toString().replaceAll("[Rp,.]", "");
                    if (!cleanString.isEmpty()) {
                        double parsed = Double.parseDouble(cleanString);
                        String formatted = NumberFormat.getInstance(new Locale("id", "ID")).format(parsed);
                        currentNominal = formatted;
                        nominalInput.setText(formatted);
                        nominalInput.setSelection(formatted.length());
                    }

                    nominalInput.addTextChangedListener(this);
                }
            }
        });

        tanggalInput.setFocusable(false);
        tanggalInput.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePicker = new DatePickerDialog(this, (view, year1, month1, dayOfMonth) -> {
                String selectedDate = dayOfMonth + "/" + (month1 + 1) + "/" + year1;
                tanggalInput.setText(selectedDate);
            }, year, month, day);

            datePicker.show();
        });

        if (getIntent().hasExtra("EDIT_DATA")) {
            transaksiEdit = (Transaksi) getIntent().getSerializableExtra("EDIT_DATA");
            if (transaksiEdit != null) {
                isEditMode = true;
                isiFormUntukEdit(transaksiEdit);
                simpanBtn.setText("Update Transaksi");
            }
        }

        simpanBtn.setOnClickListener(v -> {
            int selectedId = jenisGroup.getCheckedRadioButtonId();
            if (selectedId == -1) {
                Toast.makeText(this, "Pilih jenis transaksi!", Toast.LENGTH_SHORT).show();
                return;
            }

            String jenis = ((RadioButton) findViewById(selectedId)).getText().toString();
            String kategori = kategoriSpinner.getSelectedItem().toString();
            String nominalStr = nominalInput.getText().toString().replace(".", "");
            String tanggal = tanggalInput.getText().toString().trim();
            String catatan = catatanInput.getText().toString().trim();

            if (nominalStr.isEmpty() || tanggal.isEmpty()) {
                Toast.makeText(this, "Isi semua field wajib!", Toast.LENGTH_SHORT).show();
                return;
            }

            int nominal = Integer.parseInt(nominalStr);
            String userId = auth.getCurrentUser().getUid();

            Map<String, Object> data = new HashMap<>();
            data.put("jenis", jenis);
            data.put("kategori", kategori);
            data.put("nominal", nominal);
            data.put("tanggal", tanggal);
            data.put("catatan", catatan);

            if (isEditMode && transaksiEdit != null) {
                //Update Data
                db.collection("users")
                        .document(userId)
                        .collection("transaksi")
                        .document(transaksiEdit.getId())
                        .update(data)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Transaksi berhasil diupdate!", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Gagal update: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } else {
                //Tambah Data Baru
                db.collection("users")
                        .document(userId)
                        .collection("transaksi")
                        .add(data)
                        .addOnSuccessListener(documentReference -> {
                            Toast.makeText(this, "Transaksi berhasil disimpan!", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Gagal menyimpan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        });
    }

    //Isi form jika Edit Mode
    private void isiFormUntukEdit(Transaksi transaksi) {
        // Set jenis transaksi
        if ("Pemasukan".equalsIgnoreCase(transaksi.getJenis())) {
            ((RadioButton) findViewById(R.id.radioPemasukan)).setChecked(true);
        } else if ("Pengeluaran".equalsIgnoreCase(transaksi.getJenis())) {
            ((RadioButton) findViewById(R.id.radioPengeluaran)).setChecked(true);
        }

        // Set kategori spinner
        String[] kategoriList = {"Makanan", "Transportasi", "Gaji", "Belanja", "Lainnya"};
        for (int i = 0; i < kategoriList.length; i++) {
            if (kategoriList[i].equalsIgnoreCase(transaksi.getKategori())) {
                kategoriSpinner.setSelection(i);
                break;
            }
        }

        nominalInput.setText(String.valueOf(transaksi.getNominal()));
        tanggalInput.setText(transaksi.getTanggal());
        catatanInput.setText(transaksi.getCatatan());
    }
}
