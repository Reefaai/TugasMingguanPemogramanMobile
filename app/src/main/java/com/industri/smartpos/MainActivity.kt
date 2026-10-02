package com.industri.smartpos

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etNominal = findViewById<EditText>(R.id.etNominal)
        val etQty = findViewById<EditText>(R.id.etQty)
        val btnHitung = findViewById<Button>(R.id.btnHitung)
        val tvHasil = findViewById<TextView>(R.id.tvHasil)
        val cardResultCalc = findViewById<MaterialCardView>(R.id.cardResultCalc)

        val btnQuickValid = findViewById<Button>(R.id.btnQuickValid)
        val btnQuickInvalid = findViewById<Button>(R.id.btnQuickInvalid)
        val btnOpenPosScreen = findViewById<Button>(R.id.btnOpenPosScreen)
        val cardNavigateToPos = findViewById<MaterialCardView>(R.id.cardNavigateToPos)

        // Presets pengujian cepat
        btnQuickValid.setOnClickListener {
            etNominal.setText("25000")
            etQty.setText("4")
        }

        btnQuickInvalid.setOnClickListener {
            etNominal.setText("35000")
            etQty.setText("0") // sengaja 0 untuk memicu IllegalArgumentException
        }

        // Navigasi ke Tahap 2 & Tugas Mandiri (PosActivity)
        val openPosIntent = {
            val intent = Intent(this, PosActivity::class.java)
            startActivity(intent)
        }
        btnOpenPosScreen.setOnClickListener { openPosIntent() }
        cardNavigateToPos.setOnClickListener { openPosIntent() }

        btnHitung.setOnClickListener {
            // LATIHAN BREAKPOINT: Klik garis margin kiri pada baris di bawah untuk memasang titik merah (Breakpoint)
            val nominalStr = etNominal.text.toString()
            val qtyStr = etQty.text.toString()

            Timber.d("Menghitung transaksi kasir: Nominal=%s, Qty=%s", nominalStr, qtyStr)

            // Menggunakan idiom fungsional runCatching untuk menangkal crash input kosong
            val kalkulasiResult = runCatching {
                val nominal = nominalStr.toDouble()
                val qty = qtyStr.toInt()
                if (qty <= 0) throw IllegalArgumentException("Kuantitas barang minimal 1")
                nominal * qty
            }

            kalkulasiResult.onSuccess { total ->
                Timber.i("Kalkulasi sukses: Total bayar Rp %,.2f", total)
                tvHasil.text = "✓ Total Transaksi: Rp %,.2f".format(total)
                tvHasil.setTextColor(Color.parseColor("#16A34A"))
                cardResultCalc.setCardBackgroundColor(Color.parseColor("#DCFCE7"))
                cardResultCalc.strokeColor = Color.parseColor("#86EFAC")
            }.onFailure { error ->
                Timber.e(error, "Terjadi kesalahan kalkulasi input kasir")
                tvHasil.text = "✗ Error Input: ${error.message ?: "Input tidak valid"}"
                tvHasil.setTextColor(Color.parseColor("#DC2626"))
                cardResultCalc.setCardBackgroundColor(Color.parseColor("#FEE2E2"))
                cardResultCalc.strokeColor = Color.parseColor("#FCA5A5")
            }
        }
    }
}