package com.example.dompetku;

import java.io.Serializable;

public class Transaksi implements Serializable {
    private String id;
    private String jenis;
    private String kategori;
    private String tanggal;
    private String catatan;
    private int nominal;

    public Transaksi() {}

    public Transaksi(String jenis, String kategori, String tanggal, String catatan, int nominal) {
        this.jenis = jenis;
        this.kategori = kategori;
        this.tanggal = tanggal;
        this.catatan = catatan;
        this.nominal = nominal;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getJenis() {
        return jenis;
    }

    public void setJenis(String jenis) {
        this.jenis = jenis;
    }

    public String getKategori() {
        return kategori;
    }

    public void setKategori(String kategori) {
        this.kategori = kategori;
    }

    public String getTanggal() {
        return tanggal;
    }

    public void setTanggal(String tanggal) {
        this.tanggal = tanggal;
    }

    public String getCatatan() {
        return catatan;
    }

    public void setCatatan(String catatan) {
        this.catatan = catatan;
    }

    public int getNominal() {
        return nominal;
    }

    public void setNominal(int nominal) {
        this.nominal = nominal;
    }
}
