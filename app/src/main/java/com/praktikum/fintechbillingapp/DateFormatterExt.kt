package com.praktikum.fintechbillingapp

import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Tugas 2: Custom Date Formatting
 * Memformat string ISO 'yyyy-MM-dd HH:mm:ss' menjadi format waktu ramah pengguna dalam Bahasa Indonesia.
 * Contoh input: '2026-09-23 10:15:00'
 * Contoh output: 'Rabu, 23 September 2026 - Pukul 10:15 WIB'
 */
fun String.toIndonesianDateTime(): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val date = inputFormat.parse(this) ?: return this
        val outputFormat = SimpleDateFormat("EEEE, d MMMM yyyy - 'Pukul' HH:mm 'WIB'", Locale.forLanguageTag("id-ID"))
        outputFormat.format(date)
    } catch (e: Exception) {
        this
    }
}
