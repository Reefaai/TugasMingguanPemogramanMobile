package com.example.belajarstorage

import android.content.Context
import android.content.SharedPreferences
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    // 1. Deklarasi variable SharedPreferences dan konstanta nama file
    private val PREF_NAME = "MyUserPrefs"
    private val KEY_NAMA = "KEY_NAMA_USER"
    private lateinit var sharedPreferences: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 2. Inisialisasi SharedPreferences dengan mode privat
        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        // Hubungkan widget antarmuka
        val etNama = findViewById<EditText>(R.id.etNama)
        val btnSimpan = findViewById<Button>(R.id.btnSimpan)
        val btnMuat = findViewById<Button>(R.id.btnMuat)
        val btnHapus = findViewById<Button>(R.id.btnHapus)
        val tvHasil = findViewById<TextView>(R.id.tvHasil)

        // 3. Operasi Menulis Data (Write) saat tombol Simpan ditekan
        btnSimpan.setOnClickListener {
            val inputNama = etNama.text.toString().trim()
            if (inputNama.isNotEmpty()) {
                val editor = sharedPreferences.edit()
                editor.putString(KEY_NAMA, inputNama)
                editor.apply() // Menyimpan secara asynchronous (aman di background)

                Toast.makeText(this, "Data berhasil disimpan!",
                    Toast.LENGTH_SHORT).show()
                etNama.text.clear()
            } else {
                Toast.makeText(this, "Silakan isi nama terlebih dahulu!",
                    Toast.LENGTH_SHORT).show()
            }
        }

        // 4. Operasi Membaca Data (Read) saat tombol Muat ditekan
        btnMuat.setOnClickListener {
            val dataTersimpan = sharedPreferences.getString(KEY_NAMA, "Data tidak ditemukan")
            tvHasil.text = dataTersimpan
        }

        // 5. Operasi Menghapus Data (Delete) saat tombol Hapus ditekan
        btnHapus.setOnClickListener {
            val editor = sharedPreferences.edit()
            editor.remove(KEY_NAMA) // Menghapus key spesifik
            editor.apply()

            tvHasil.text = "[Data telah dihapus]"
            Toast.makeText(this, "Data berhasil dihapus!", Toast.LENGTH_SHORT).show()
        }

        // 6. Muat data otomatis saat pertama kali aplikasi dibuka
        val namaAwal = sharedPreferences.getString(KEY_NAMA, null)
        if (namaAwal != null) {
            tvHasil.text = "Selamat datang kembali: $namaAwal"
        }

//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
    }
}