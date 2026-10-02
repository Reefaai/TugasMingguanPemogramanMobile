package com.praktikum.fintechbillingapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnParse = findViewById<Button>(R.id.btnParseBasic)
        val tvOutput = findViewById<TextView>(R.id.tvOutputBasic)
        val btnOpenInvoice = findViewById<Button>(R.id.btnOpenInvoice)

        // Tahap 1: Deserialisasi Dasar Merchant JSON
        btnParse.setOnClickListener {
            val rawJsonString = """
            {
              "merchant_id": "MCH-99201",
              "merchant_name": "Mega Elektrindo Ritel",
              "city_location": "Bandung",
              "is_verified": true
            }
            """.trimIndent()

            // Deserialisasi menggunakan Gson
            val gson = Gson()
            val merchantObj: Merchant = gson.fromJson(rawJsonString, Merchant::class.java)

            // Tampilkan hasil parsing dalam format terstruktur rapi
            val result = """
            [STATUS: BERHASIL DI-DESERIALISASI]
            ------------------------------------
            • ID Mitra      : ${merchantObj.merchantId}
            • Nama Toko     : ${merchantObj.merchantName}
            • Lokasi Kota   : ${merchantObj.cityLocation}
            • Status Mitra  : ${if (merchantObj.isVerified) "RESMI TERVERIFIKASI ✓" else "BELUM VERIFIKASI"}
            ------------------------------------
            Class: ${merchantObj.javaClass.simpleName}
            """.trimIndent()

            tvOutput.text = result
        }

        // Tahap 2 & Tugas Mandiri: Navigasi ke Layar E-Invoice
        btnOpenInvoice.setOnClickListener {
            val intent = Intent(this, InvoiceActivity::class.java)
            startActivity(intent)
        }
    }
}