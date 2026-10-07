package com.industri.transportqa

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BookingActivityEspressoTest {

    // Luncurkan BookingActivity (MainActivity) secara otomatis sebelum test dijalankan
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    // --- PRAKTIKUM UTAMA (Langkah 4.3) ---
    @Test
    fun isiFormPemesanan_danTekanPesan_harusMenampilkanKonfirmasiTiket() {
        // 1. Ketik NIK Penumpang pada EditText
        onView(withId(R.id.etNik))
            .perform(typeText("3209123456780001"), closeSoftKeyboard())

        // 2. Ketik Nama Penumpang
        onView(withId(R.id.etName))
            .perform(typeText("Ahmad Dahlan"), closeSoftKeyboard())

        // 3. Tekan Tombol Pesan Tiket
        onView(withId(R.id.btnSubmitBooking))
            .perform(click())

        // 4. Verifikasi (Assert) bahwa status konfirmasi sukses muncul di layar
        onView(withId(R.id.tvBookingStatus))
            .check(matches(isDisplayed()))
            .check(matches(withText("Pemesanan Tiket Berhasil Diverifikasi")))
    }

    // --- TUGAS MANDIRI 2 (Negative Flow UI Automation) ---
    @Test
    fun formPemesanan_nikKosongLaluTekanSubmit_harusMenampilkanPesanErrorNIKTidakBolehKosong() {
        // 1. Kosongkan kolom NIK
        onView(withId(R.id.etNik))
            .perform(clearText(), closeSoftKeyboard())

        // 2. Isi Nama Penumpang
        onView(withId(R.id.etName))
            .perform(typeText("Ahmad Dahlan"), closeSoftKeyboard())

        // 3. Langsung tekan tombol pesan tiket tanpa mengisi NIK
        onView(withId(R.id.btnSubmitBooking))
            .perform(click())

        // 4. Verifikasi bahwa pesan error peringatan 'NIK tidak boleh kosong' muncul di layar
        onView(withId(R.id.tvBookingStatus))
            .check(matches(isDisplayed()))
            .check(matches(withText("NIK tidak boleh kosong")))

        // 5. Verifikasi bahwa transaksi TIDAK diproses (status sukses tidak muncul)
        onView(withId(R.id.tvBookingStatus))
            .check(matches(not(withText("Pemesanan Tiket Berhasil Diverifikasi"))))
    }
}
