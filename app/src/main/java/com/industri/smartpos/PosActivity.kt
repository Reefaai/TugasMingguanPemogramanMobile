package com.industri.smartpos

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PosActivity : AppCompatActivity() {

    private lateinit var etBarcode: EditText
    private lateinit var btnProcessPayment: Button
    private lateinit var pbPosLoading: ProgressBar
    private lateinit var cardPosResult: MaterialCardView
    private lateinit var tvPosStatus: TextView
    private lateinit var tvPosDetails: TextView
    private lateinit var tvPosPaymentMethod: TextView
    private lateinit var rgSimulation: RadioGroup

    // Elemen Pembeda UI & Tugas Mandiri
    private lateinit var tvRetryBadge: TextView
    private lateinit var tvLiveLogOutput: TextView
    private lateinit var btnBackToMain: ImageButton
    private lateinit var btnResetTransaction: Button

    // Quick Chips
    private lateinit var chipUhtMilk: Button
    private lateinit var chipNotFound: Button
    private lateinit var chipTimeout: Button
    private lateinit var chipOverLimit: Button

    // Tugas 1: Counter Percobaan Ulang (Max Retry Threshold)
    private var retryAttempt: Int = 0
    private val maxRetryThreshold: Int = 3

    // Tree khusus untuk menyalurkan log Timber langsung ke antarmuka aplikasi
    private val uiLogTree = object : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            val levelPrefix = when (priority) {
                Log.DEBUG -> "🔵 [DEBUG]"
                Log.INFO -> "🟢 [INFO]"
                Log.WARN -> "🟡 [WARN]"
                Log.ERROR -> "🔴 [ERROR]"
                else -> "⚪ [LOG]"
            }
            val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            val formatted = "$time $levelPrefix\n${tag ?: "PosActivity"}: $message"
            runOnUiThread {
                if (::tvLiveLogOutput.isInitialized) {
                    tvLiveLogOutput.text = formatted
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pos)

        // Pasang Tree audit internal
        Timber.plant(uiLogTree)

        initViews()
        setupListeners()
        setupQuickChips()
        updateRetryBadge()

        Timber.d("PosActivity berhasil dimuat dan siap menerima transaksi kasir")
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.uproot(uiLogTree)
    }

    private fun initViews() {
        etBarcode = findViewById(R.id.etBarcode)
        btnProcessPayment = findViewById(R.id.btnProcessPayment)
        pbPosLoading = findViewById(R.id.pbPosLoading)
        cardPosResult = findViewById(R.id.cardPosResult)
        tvPosStatus = findViewById(R.id.tvPosStatus)
        tvPosDetails = findViewById(R.id.tvPosDetails)
        tvPosPaymentMethod = findViewById(R.id.tvPosPaymentMethod)
        rgSimulation = findViewById(R.id.rgSimulation)

        tvRetryBadge = findViewById(R.id.tvRetryBadge)
        tvLiveLogOutput = findViewById(R.id.tvLiveLogOutput)
        btnBackToMain = findViewById(R.id.btnBackToMain)
        btnResetTransaction = findViewById(R.id.btnResetTransaction)

        chipUhtMilk = findViewById(R.id.chipUhtMilk)
        chipNotFound = findViewById(R.id.chipNotFound)
        chipTimeout = findViewById(R.id.chipTimeout)
        chipOverLimit = findViewById(R.id.chipOverLimit)
    }

    private fun setupListeners() {
        btnBackToMain.setOnClickListener {
            finish()
        }

        btnResetTransaction.setOnClickListener {
            cardPosResult.visibility = View.GONE
            etBarcode.text?.clear()
            rgSimulation.check(R.id.rbSuccess)
            retryAttempt = 0
            updateRetryBadge()
            Timber.i("Status kasir di-reset untuk transaksi baru")
        }

        btnProcessPayment.setOnClickListener {
            val barcode = etBarcode.text.toString().trim()
            if (barcode.isNotEmpty()) {
                eksekusiTransaksi(barcode)
            } else {
                Toast.makeText(this, "Barcode wajib diisi atau dipindai", Toast.LENGTH_SHORT).show()
                Timber.w("Kasir menekan tombol bayar tanpa memasukkan barcode produk")
            }
        }
    }

    private fun setupQuickChips() {
        chipUhtMilk.setOnClickListener {
            etBarcode.setText("8991201")
            rgSimulation.check(R.id.rbSuccess)
        }
        chipNotFound.setOnClickListener {
            etBarcode.setText("8999999")
            rgSimulation.check(R.id.rbBarcodeError)
        }
        chipTimeout.setOnClickListener {
            etBarcode.setText("8992222")
            rgSimulation.check(R.id.rbNetworkTimeout)
        }
        chipOverLimit.setOnClickListener {
            etBarcode.setText("8998888")
            rgSimulation.check(R.id.rbCashierLimit)
        }
    }

    private fun updateRetryBadge() {
        tvRetryBadge.text = "Retry: $retryAttempt / $maxRetryThreshold"
        if (retryAttempt >= maxRetryThreshold) {
            tvRetryBadge.setTextColor(Color.parseColor("#991B1B"))
            tvRetryBadge.setBackgroundColor(Color.parseColor("#FEE2E2"))
        } else if (retryAttempt > 0) {
            tvRetryBadge.setTextColor(Color.parseColor("#D97706"))
            tvRetryBadge.setBackgroundColor(Color.parseColor("#FEF3C7"))
        } else {
            tvRetryBadge.setTextColor(Color.parseColor("#0F766E"))
            tvRetryBadge.setBackgroundResource(R.drawable.bg_badge_tag)
        }
    }

    private fun eksekusiTransaksi(barcode: String) {
        pbPosLoading.visibility = View.VISIBLE
        cardPosResult.visibility = View.GONE
        btnProcessPayment.isEnabled = false

        // Coroutine Exception Handler: Pengaman terakhir agar aplikasi tidak force-close
        val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
            Timber.e(exception, "FATAL COROUTINE ERROR TERTANGKAP: %s", exception.message)
            runOnUiThread {
                pbPosLoading.visibility = View.GONE
                btnProcessPayment.isEnabled = true
                tampilkanSnackbarError("Kesalahan sistem tak terduga: ${exception.localizedMessage}", canRetry = false)
            }
        }

        lifecycleScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            Timber.d("Memulai alur pembayaran untuk Barcode: %s (Percobaan: %d)", barcode, retryAttempt)

            // Menggunakan idiom runCatching untuk menangani logika transaksi perbankan
            val hasil = runCatching {
                delay(1200) // Simulasi latensi jaringan

                // Simulasi kondisi berdasarkan pilihan RadioButton
                when (rgSimulation.checkedRadioButtonId) {
                    R.id.rbBarcodeError -> {
                        throw ProductBarcodeNotFoundException(barcode)
                    }
                    R.id.rbNetworkTimeout -> {
                        throw PaymentGatewayTimeoutException("QRIS Bank Settlement")
                    }
                    R.id.rbCashierLimit -> {
                        // Tugas Mandiri 2: Eksepsi transaksi > Rp 10.000.000
                        throw CashierLimitExceededException(10000000.0)
                    }
                    else -> {
                        // Jika barcode khusus grosir besar tanpa radio button
                        if (barcode == "8998888") {
                            throw CashierLimitExceededException(10000000.0)
                        }
                        PosTransaction(
                            transactionId = "TRX-2026-9901",
                            barcode = barcode,
                            productName = "Susu UHT Full Cream 1 Liter",
                            totalAmount = 21500.0,
                            status = "SETTLED_SUCCESS"
                        )
                    }
                }
            }

            withContext(Dispatchers.Main) {
                pbPosLoading.visibility = View.GONE
                btnProcessPayment.isEnabled = true

                hasil.onSuccess { trx ->
                    retryAttempt = 0
                    updateRetryBadge()
                    Timber.i("Transaksi kasir berhasil dicatat: %s", trx.transactionId)

                    cardPosResult.visibility = View.VISIBLE
                    tvPosStatus.text = "TRANSAKSI BERHASIL (LUNAS)"
                    tvPosStatus.setTextColor(Color.parseColor("#16A34A"))
                    tvPosPaymentMethod.text = "QRIS SETTLED"
                    cardPosResult.strokeColor = Color.parseColor("#86EFAC")

                    tvPosDetails.text = """
                        ID Transaksi : ${trx.transactionId}
                        Barcode      : ${trx.barcode}
                        Produk       : ${trx.productName}
                        Total Bayar  : Rp %,.2f
                        Status       : ${trx.status}
                        Waktu        : Baru Saja
                    """.trimIndent().format(trx.totalAmount)
                }.onFailure { err ->
                    Timber.w("Transaksi kasir ditolak / gagal: %s", err.message)
                    when (err) {
                        is ProductBarcodeNotFoundException -> {
                            retryAttempt = 0
                            updateRetryBadge()
                            tampilkanSnackbarError(err.message ?: "Barcode tidak ditemukan", canRetry = false)
                        }
                        is PaymentGatewayTimeoutException -> {
                            // Error Jaringan: Berikan tombol aksi RETRY pada Snackbar
                            tampilkanSnackbarError(err.message ?: "Koneksi gateway terputus", canRetry = true)
                        }
                        is CashierLimitExceededException -> {
                            retryAttempt = 0
                            updateRetryBadge()
                            // Tugas Mandiri 2: Tampilkan Dialog PIN Supervisor
                            tampilkanDialogPinSupervisor(barcode, 12500000.0)
                        }
                        else -> {
                            tampilkanSnackbarError("Kegagalan operasional: ${err.message}", canRetry = true)
                        }
                    }
                }
            }
        }
    }

    private fun tampilkanSnackbarError(pesan: String, canRetry: Boolean) {
        val root = findViewById<View>(R.id.coordinatorLayout)
        val snackbar = Snackbar.make(root, pesan, Snackbar.LENGTH_LONG)
        snackbar.setBackgroundTint(Color.parseColor("#991B1B")) // Merah gelap
        snackbar.setTextColor(Color.WHITE)

        if (canRetry) {
            snackbar.setAction("COBA LAGI") {
                val barcode = etBarcode.text.toString().trim()
                if (barcode.isNotEmpty()) {
                    retryAttempt++
                    updateRetryBadge()
                    Timber.d("Pengguna menekan tombol COBA LAGI (Percobaan ke-%d)", retryAttempt)

                    // TUGAS MANDIRI 1: Jika retry telah mencapai 3 kali berturut-turut
                    if (retryAttempt >= maxRetryThreshold) {
                        Timber.w("Batas retry tercapai (%d kali). Memicu Fallback Mode Pembayaran Tunai.", retryAttempt)
                        tampilkanDialogFallbackTunai(barcode)
                    } else {
                        eksekusiTransaksi(barcode)
                    }
                }
            }
            snackbar.setActionTextColor(Color.parseColor("#FEF08A")) // Kuning
        }
        snackbar.show()
    }

    /**
     * TUGAS MANDIRI 1: Max Retry Threshold & Fallback Mode
     * Jika penekanan tombol 'COBA LAGI' telah mencapai 3 kali berturut-turut namun jaringan
     * perbankan masih tetap gagal, tampilkan AlertDialog yang merekomendasikan kasir
     * untuk mengalihkan pelanggan ke 'Pembayaran Tunai Manual'.
     */
    private fun tampilkanDialogFallbackTunai(barcode: String) {
        AlertDialog.Builder(this)
            .setTitle("⚠️ Gangguan Jaringan Pembayaran (3x Gagal)")
            .setMessage(
                "Koneksi gateway perbankan telah gagal sebanyak $retryAttempt kali berturut-turut.\n\n" +
                "Sistem merekomendasikan kasir untuk mengalihkan pelanggan ke:\n" +
                "👉 'Pembayaran Tunai Manual'\n\n" +
                "Apakah pelanggan bersedia membayar secara tunai?"
            )
            .setCancelable(false)
            .setPositiveButton("Alihkan ke Tunai Manual") { dialog, _ ->
                dialog.dismiss()
                Timber.i("Kasir memilih opsi Fallback Mode: Pembayaran Tunai Manual untuk barcode %s", barcode)

                // Transaksi diselesaikan secara tunai manual
                val cashTransactionId = "TRX-CASH-${System.currentTimeMillis() % 100000}"
                val nominalTunai = 21500.0

                cardPosResult.visibility = View.VISIBLE
                tvPosStatus.text = "TRANSAKSI TUNAI MANUAL (LUNAS)"
                tvPosStatus.setTextColor(Color.parseColor("#0F766E"))
                tvPosPaymentMethod.text = "TUNAI / CASH"
                cardPosResult.strokeColor = Color.parseColor("#0D9488")

                tvPosDetails.text = """
                    ID Transaksi : $cashTransactionId
                    Barcode      : $barcode
                    Produk       : Susu UHT Full Cream 1 Liter
                    Total Bayar  : Rp %,.2f
                    Metode       : Kasir Tunai Manual (Fallback)
                    Catatan      : Sukses via offline settlement
                """.trimIndent().format(nominalTunai)

                // Reset counter percobaan
                retryAttempt = 0
                updateRetryBadge()

                Toast.makeText(this, "Transaksi berhasil dialihkan ke Tunai Manual", Toast.LENGTH_LONG).show()
                Timber.i("Transaksi Tunai Manual %s berhasil dicetak dan diselesaikan", cashTransactionId)
            }
            .setNegativeButton("Batalkan Transaksi") { dialog, _ ->
                dialog.dismiss()
                retryAttempt = 0
                updateRetryBadge()
                Timber.w("Kasir membatalkan transaksi setelah 3x percobaan gagal.")
                Toast.makeText(this, "Transaksi dibatalkan", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    /**
     * TUGAS MANDIRI 2: Custom Authorization Exception & Supervisor PIN Dialog
     * Dipicu apabila total nominal transaksi belanja di atas Rp 10.000.000,-
     * Tampilkan dialog PIN Supervisor kasir untuk otorisasi transaksi bernilai besar.
     */
    private fun tampilkanDialogPinSupervisor(barcode: String, nominal: Double) {
        Timber.w("Membuka dialog verifikasi PIN Supervisor untuk transaksi Rp %,.0f", nominal)

        val inputPin = EditText(this).apply {
            hint = "Masukkan 4 digit PIN Supervisor (Default: 1234)"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
            textSize = 14f
            setPadding(40, 30, 40, 30)
        }

        val container = FrameLayout(this).apply {
            setPadding(50, 10, 50, 10)
            addView(inputPin)
        }

        AlertDialog.Builder(this)
            .setTitle("🔐 Otorisasi Transaksi Bernilai Besar")
            .setMessage(
                "Total belanja: Rp %,.2f\n".format(nominal) +
                "Transaksi ini melebihi limit mandiri kasir (Rp 10.000.000,-).\n\n" +
                "Silakan panggil Supervisor Toko untuk memasukkan kode otorisasi PIN:"
            )
            .setView(container)
            .setCancelable(false)
            .setPositiveButton("Verifikasi & Otorisasi") { dialog, _ ->
                val pin = inputPin.text.toString().trim()
                if (pin == "1234") {
                    dialog.dismiss()
                    Timber.i("Otorisasi Supervisor BERHASIL: PIN valid. Transaksi Rp %,.2f disetujui.", nominal)

                    val supervisorTrxId = "TRX-VIP-${System.currentTimeMillis() % 100000}"
                    cardPosResult.visibility = View.VISIBLE
                    tvPosStatus.text = "OTORISASI SUPERVISOR: DISETUJUI"
                    tvPosStatus.setTextColor(Color.parseColor("#16A34A"))
                    tvPosPaymentMethod.text = "VIP SUPERVISOR OK"
                    cardPosResult.strokeColor = Color.parseColor("#16A34A")

                    tvPosDetails.text = """
                        ID Transaksi : $supervisorTrxId
                        Barcode      : $barcode
                        Produk       : Paket Pengadaan Grosir Ritel
                        Total Bayar  : Rp %,.2f
                        Otorisasi    : Disetujui Supervisor Toko
                        Kode PIN     : Terverifikasi [1234]
                    """.trimIndent().format(nominal)

                    Toast.makeText(this, "Otorisasi Supervisor Berhasil! Transaksi disetujui.", Toast.LENGTH_LONG).show()
                } else {
                    Timber.e("Otorisasi DITOLAK: PIN Supervisor yang dimasukkan salah ($pin)")
                    tampilkanSnackbarError("Otorisasi Ditolak: PIN Supervisor salah! Hubungi Store Manager.", canRetry = false)
                }
            }
            .setNegativeButton("Tolak Transaksi") { dialog, _ ->
                dialog.dismiss()
                Timber.w("Transaksi belanja bernilai besar dibatalkan oleh kasir/supervisor.")
                Toast.makeText(this, "Transaksi bernilai besar dibatalkan", Toast.LENGTH_SHORT).show()
            }
            .show()
    }
}
