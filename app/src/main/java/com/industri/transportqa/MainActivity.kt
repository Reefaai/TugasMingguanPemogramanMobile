package com.industri.transportqa

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {

    private val fareCalculator = FareCalculator()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etNik = findViewById<TextInputEditText>(R.id.etNik)
        val etName = findViewById<TextInputEditText>(R.id.etName)
        val btnSubmitBooking = findViewById<MaterialButton>(R.id.btnSubmitBooking)
        val tvBookingStatus = findViewById<TextView>(R.id.tvBookingStatus)

        btnSubmitBooking.setOnClickListener {
            val nik = etNik.text?.toString()?.trim().orEmpty()
            val name = etName.text?.toString()?.trim().orEmpty()

            // Validasi kasus negatif: NIK kosong (Tugas Mandiri 2)
            if (nik.isEmpty()) {
                etNik.error = "NIK tidak boleh kosong"
                tvBookingStatus.text = "NIK tidak boleh kosong"
                tvBookingStatus.visibility = View.VISIBLE
                return@setOnClickListener
            }

            // Validasi format NIK menggunakan logika FareCalculator
            if (!fareCalculator.isValidNik(nik)) {
                etNik.error = "NIK harus terdiri dari 16 karakter angka"
                tvBookingStatus.text = "NIK tidak valid"
                tvBookingStatus.visibility = View.VISIBLE
                return@setOnClickListener
            }

            // Validasi nama penumpang
            if (name.isEmpty()) {
                etName.error = "Nama penumpang tidak boleh kosong"
                tvBookingStatus.text = "Nama tidak boleh kosong"
                tvBookingStatus.visibility = View.VISIBLE
                return@setOnClickListener
            },

            // Alur sukses: Tampilkan konfirmasi tiket diverifikasi (Praktikum Langkah 4.3)
            etNik.error = null
            etName.error = null
            tvBookingStatus.text = "Pemesanan Tiket Berhasil Diverifikasi"
            tvBookingStatus.visibility = View.VISIBLE
        }
    }
}