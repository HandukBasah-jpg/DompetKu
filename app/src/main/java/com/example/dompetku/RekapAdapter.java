package com.example.dompetku;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RekapAdapter extends RecyclerView.Adapter<RekapAdapter.ViewHolder> {

    private List<Transaksi> transaksiList;

    public RekapAdapter(List<Transaksi> transaksiList) {
        this.transaksiList = transaksiList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaksi, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaksi transaksi = transaksiList.get(position);

        holder.jenis.setText(transaksi.getJenis());
        holder.kategori.setText(transaksi.getKategori());
        holder.nominal.setText("Rp " + transaksi.getNominal());
        holder.tanggal.setText(transaksi.getTanggal());
        holder.catatan.setText(transaksi.getCatatan());
    }

    @Override
    public int getItemCount() {
        return transaksiList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView jenis, kategori, nominal, tanggal, catatan;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            jenis = itemView.findViewById(R.id.textJenis);
            kategori = itemView.findViewById(R.id.textKategori);
            nominal = itemView.findViewById(R.id.textNominal);
            tanggal = itemView.findViewById(R.id.textTanggal);
            catatan = itemView.findViewById(R.id.textCatatan);
        }
    }
}
