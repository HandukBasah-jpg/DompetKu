package com.example.dompetku;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransaksiAdapter extends RecyclerView.Adapter<TransaksiAdapter.ViewHolder> {

    private Context context;
    private List<Transaksi> transaksiList;
    private OnItemActionListener actionListener;

    public interface OnItemActionListener {
        void onEdit(Transaksi transaksi);
        void onDelete(Transaksi transaksi);
    }

    public TransaksiAdapter(Context context, List<Transaksi> transaksiList, OnItemActionListener actionListener) {
        this.context = context;
        this.transaksiList = transaksiList;
        this.actionListener = actionListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView jenis, kategori, nominal, tanggal, catatan;
        LinearLayout iconContainer;
        ImageView iconTransaction;

        public ViewHolder(View view) {
            super(view);
            jenis = view.findViewById(R.id.textJenis);
            kategori = view.findViewById(R.id.textKategori);
            nominal = view.findViewById(R.id.textNominal);
            tanggal = view.findViewById(R.id.textTanggal);
            catatan = view.findViewById(R.id.textCatatan);
            iconContainer = view.findViewById(R.id.iconContainer);
            iconTransaction = view.findViewById(R.id.iconTransaction);

            view.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Transaksi transaksi = transaksiList.get(position);
                    showPopupMenu(view, transaksi);
                }
            });
        }
    }

    private void showPopupMenu(View view, Transaksi transaksi) {
        PopupMenu popup = new PopupMenu(context, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_transaksi, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_edit) {
                if (actionListener != null) actionListener.onEdit(transaksi);
                return true;
            } else if (id == R.id.menu_hapus) {
                if (actionListener != null) actionListener.onDelete(transaksi);
                return true;
            }
            return false;
        });
        popup.show();
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
        Transaksi t = transaksiList.get(position);
        holder.jenis.setText(t.getJenis());
        holder.kategori.setText(t.getKategori());
        holder.catatan.setText(t.getCatatan());

        NumberFormat formatRupiah = NumberFormat.getInstance(new Locale("id", "ID"));
        holder.nominal.setText("Rp " + formatRupiah.format(t.getNominal()));

        holder.tanggal.setText(formatDate(t.getTanggal()));

        if (t.getJenis().equalsIgnoreCase("Pengeluaran")) {
            setupPengeluaranTheme(holder);
        } else if (t.getJenis().equalsIgnoreCase("Pemasukan")) {
            setupPemasukanTheme(holder);
        } else {
            setupDefaultTheme(holder);
        }
    }

    private void setupPengeluaranTheme(ViewHolder holder) {
        holder.jenis.setTextColor(Color.parseColor("#D32F2F"));
        holder.nominal.setTextColor(Color.parseColor("#D32F2F"));
        holder.tanggal.setTextColor(Color.parseColor("#EF5350"));
        holder.catatan.setTextColor(Color.parseColor("#E57373"));
        holder.iconContainer.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.circle_background_red));
        holder.iconTransaction.setImageResource(R.drawable.ic_arrow_downward);
        holder.kategori.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.category_chip_background_red));
        holder.kategori.setTextColor(Color.parseColor("#D32F2F"));
    }

    private void setupPemasukanTheme(ViewHolder holder) {
        holder.jenis.setTextColor(Color.parseColor("#2E7D32"));
        holder.nominal.setTextColor(Color.parseColor("#2E7D32"));
        holder.tanggal.setTextColor(Color.parseColor("#66BB6A"));
        holder.catatan.setTextColor(Color.parseColor("#81C784"));
        holder.iconContainer.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.circle_background_green));
        holder.iconTransaction.setImageResource(R.drawable.ic_arrow_upward);
        holder.kategori.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.category_chip_background_green));
        holder.kategori.setTextColor(Color.parseColor("#2E7D32"));
    }

    private void setupDefaultTheme(ViewHolder holder) {
        holder.jenis.setTextColor(Color.parseColor("#1A1A1A"));
        holder.nominal.setTextColor(Color.parseColor("#1A1A1A"));
        holder.tanggal.setTextColor(Color.parseColor("#757575"));
        holder.catatan.setTextColor(Color.parseColor("#9E9E9E"));
        holder.iconContainer.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.category_chip_background));
        holder.iconTransaction.setImageResource(R.drawable.ic_arrow_upward);
        holder.iconTransaction.setColorFilter(Color.parseColor("#757575"));
        holder.kategori.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.category_chip_background));
        holder.kategori.setTextColor(Color.parseColor("#6B7280"));
    }

    private String formatDate(String dateString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy", new Locale("id", "ID"));
            Date date = inputFormat.parse(dateString);
            return outputFormat.format(date != null ? date : new Date());
        } catch (Exception e) {
            return dateString;
        }
    }

    @Override
    public int getItemCount() {
        return transaksiList.size();
    }

    public void updateData(List<Transaksi> newTransaksiList) {
        this.transaksiList = newTransaksiList;
        notifyDataSetChanged();
    }
}
