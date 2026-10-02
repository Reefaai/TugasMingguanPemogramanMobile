package com.praktikum.fintechbillingapp

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import java.text.NumberFormat
import java.util.Locale

class InvoiceActivity : AppCompatActivity() {

    private val rupiahFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"))
    private var isWithPromoActive = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invoice)

        // Tombol kembali di Top App Bar
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        val btnModeWithPromo = findViewById<TextView>(R.id.btnModeWithPromo)
        val btnModeWithoutPromo = findViewById<TextView>(R.id.btnModeWithoutPromo)

        // Setup RecyclerView
        val rvItems = findViewById<RecyclerView>(R.id.rvInvoiceItems)
        rvItems.layoutManager = LinearLayoutManager(this)

        // Render invoice awal dengan promo aktif
        renderInvoice(hasPromo = true)

        // Listener untuk demonstrasi Tugas Mandiri 1 (Dengan Promo vs Null)
        btnModeWithPromo.setOnClickListener {
            if (!isWithPromoActive) {
                isWithPromoActive = true
                updateChipState(btnModeWithPromo, btnModeWithoutPromo)
                renderInvoice(hasPromo = true)
            }
        }

        btnModeWithoutPromo.setOnClickListener {
            if (isWithPromoActive) {
                isWithPromoActive = false
                updateChipState(btnModeWithoutPromo, btnModeWithPromo)
                renderInvoice(hasPromo = false)
            }
        }
    }

    private fun updateChipState(selected: TextView, unselected: TextView) {
        selected.setBackgroundResource(R.drawable.bg_chip_selected)
        selected.setTextColor(ContextCompat.getColor(this, R.color.white))

        unselected.setBackgroundResource(R.drawable.bg_chip_unselected)
        unselected.setTextColor(ContextCompat.getColor(this, R.color.slate_600))
    }

    /**
     * Memuat payload JSON mock, mendeserialisasi dengan Gson, dan memetakan ke UI.
     */
    private fun renderInvoice(hasPromo: Boolean) {
        // 1. Eksekusi Parsing JSON Payload Transaksi
        val jsonPayload = loadMockInvoiceJson(includePromo = hasPromo)
        val invoice: InvoiceResponse = Gson().fromJson(jsonPayload, InvoiceResponse::class.java)

        // 2. Hubungkan Informasi Header & Merchant
        findViewById<TextView>(R.id.tvInvoiceNo).text = invoice.invoiceNumber
        findViewById<TextView>(R.id.tvStatusBadge).text = invoice.paymentStatus
        findViewById<TextView>(R.id.tvPaymentMethod).text = invoice.paymentMethod

        // Tugas Mandiri 2: Menampilkan tanggal berformat Indonesia ramah pengguna
        findViewById<TextView>(R.id.tvTransactionDate).text = invoice.transactionDate.toIndonesianDateTime()

        findViewById<TextView>(R.id.tvMerchantName).text =
            "${invoice.merchant.merchantName} • ${invoice.merchant.cityLocation}"
        findViewById<TextView>(R.id.tvCustomerName).text =
            "Penerima: ${invoice.customer.fullName} (${invoice.customer.phone})"

        // 3. Pasangkan Daftar Barang Belanja ke RecyclerView
        val rvItems = findViewById<RecyclerView>(R.id.rvInvoiceItems)
        rvItems.adapter = InvoiceItemAdapter(invoice.items)

        // 4. Hubungkan Rangkuman Biaya & Tugas Mandiri 1 (Defensive Nullable Handling)
        val s = invoice.summary
        findViewById<TextView>(R.id.tvSubtotalAmt).text = rupiahFormat.format(s.subtotalAmount)
        findViewById<TextView>(R.id.tvTaxAmt).text = rupiahFormat.format(s.taxPpn11)
        findViewById<TextView>(R.id.tvFeeAmt).text = rupiahFormat.format(s.serviceFee)
        findViewById<TextView>(R.id.tvTotalPaid).text = rupiahFormat.format(s.totalPaid)

        val layoutDiscountRow = findViewById<LinearLayout>(R.id.layoutDiscountRow)
        val layoutNoPromoBanner = findViewById<LinearLayout>(R.id.layoutNoPromoBanner)
        val tvDiscountAmt = findViewById<TextView>(R.id.tvDiscountAmt)
        val tvVoucherBadge = findViewById<TextView>(R.id.tvVoucherBadge)

        // Logika Tugas 1 Modul Praktikum:
        // "Jika server tidak menyertakan voucher (bernilai null), sembunyikan baris diskon pada UI faktur dan tampilkan keterangan 'Tidak menggunakan promo'."
        if (s.voucherCode != null && s.discountAmount > 0.0) {
            layoutDiscountRow.visibility = View.VISIBLE
            layoutNoPromoBanner.visibility = View.GONE
            tvDiscountAmt.text = "- ${rupiahFormat.format(s.discountAmount)}"
            
            val percentText = s.voucherDiscountPercent?.let { " (-$it%)" } ?: ""
            tvVoucherBadge.text = "${s.voucherCode}$percentText"
            tvVoucherBadge.visibility = View.VISIBLE
        } else {
            layoutDiscountRow.visibility = View.GONE
            layoutNoPromoBanner.visibility = View.VISIBLE
        }
    }

    /**
     * Payload JSON mock transaksi bertingkat 3-lapis.
     */
    private fun loadMockInvoiceJson(includePromo: Boolean): String {
        return if (includePromo) {
            """
            {
              "invoice_number": "INV-2026-FT9012",
              "transaction_date": "2026-09-23 10:15:00",
              "payment_status": "PAID_SETTLED",
              "payment_method": "QRIS_BCA",
              "merchant": {
                "merchant_id": "MCH-99201",
                "merchant_name": "Mega Elektrindo Ritel",
                "city_location": "Bandung",
                "is_verified": true
              },
              "customer": {
                "customer_id": "CUST-4412",
                "full_name": "Ahmad Fauzi",
                "phone": "081298765432"
              },
              "items": [
                {
                  "item_id": "ITM-01",
                  "item_name": "Kabel Type-C 65W Fast Charging",
                  "qty": 2,
                  "unit_price": 75000.0,
                  "subtotal": 150000.0
                },
                {
                  "item_id": "ITM-02",
                  "item_name": "Adaptor GaN Charger 3-Port 100W",
                  "qty": 1,
                  "unit_price": 320000.0,
                  "subtotal": 320000.0
                },
                {
                  "item_id": "ITM-03",
                  "item_name": "Mouse Wireless Silent Click Ergonomis",
                  "qty": 1,
                  "unit_price": 130000.0,
                  "subtotal": 130000.0
                }
              ],
              "summary": {
                "subtotal_amount": 600000.0,
                "discount_amount": 50000.0,
                "tax_ppn_11": 60500.0,
                "service_fee": 2500.0,
                "total_paid": 613000.0,
                "voucher_code": "HEMAT10",
                "voucher_discount_percent": 10
              }
            }
            """.trimIndent()
        } else {
            """
            {
              "invoice_number": "INV-2026-FT9012",
              "transaction_date": "2026-09-23 10:15:00",
              "payment_status": "PAID_SETTLED",
              "payment_method": "QRIS_BCA",
              "merchant": {
                "merchant_id": "MCH-99201",
                "merchant_name": "Mega Elektrindo Ritel",
                "city_location": "Bandung",
                "is_verified": true
              },
              "customer": {
                "customer_id": "CUST-4412",
                "full_name": "Ahmad Fauzi",
                "phone": "081298765432"
              },
              "items": [
                {
                  "item_id": "ITM-01",
                  "item_name": "Kabel Type-C 65W Fast Charging",
                  "qty": 2,
                  "unit_price": 75000.0,
                  "subtotal": 150000.0
                },
                {
                  "item_id": "ITM-02",
                  "item_name": "Adaptor GaN Charger 3-Port 100W",
                  "qty": 1,
                  "unit_price": 320000.0,
                  "subtotal": 320000.0
                },
                {
                  "item_id": "ITM-03",
                  "item_name": "Mouse Wireless Silent Click Ergonomis",
                  "qty": 1,
                  "unit_price": 130000.0,
                  "subtotal": 130000.0
                }
              ],
              "summary": {
                "subtotal_amount": 600000.0,
                "discount_amount": 0.0,
                "tax_ppn_11": 66000.0,
                "service_fee": 2500.0,
                "total_paid": 668500.0,
                "voucher_code": null,
                "voucher_discount_percent": null
              }
            }
            """.trimIndent()
        }
    }
}