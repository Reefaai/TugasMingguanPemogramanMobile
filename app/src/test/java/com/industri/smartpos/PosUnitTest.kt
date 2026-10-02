package com.industri.smartpos

import org.junit.Assert.*
import org.junit.Test

class PosUnitTest {

    @Test
    fun testDefensiveCalculation_validInput() {
        val nominalStr = "25000"
        val qtyStr = "4"

        val kalkulasiResult = runCatching {
            val nominal = nominalStr.toDouble()
            val qty = qtyStr.toInt()
            if (qty <= 0) throw IllegalArgumentException("Kuantitas barang minimal 1")
            nominal * qty
        }

        assertTrue(kalkulasiResult.isSuccess)
        assertEquals(100000.0, kalkulasiResult.getOrNull()!!, 0.001)
    }

    @Test
    fun testDefensiveCalculation_invalidZeroQty() {
        val nominalStr = "25000"
        val qtyStr = "0"

        val kalkulasiResult = runCatching {
            val nominal = nominalStr.toDouble()
            val qty = qtyStr.toInt()
            if (qty <= 0) throw IllegalArgumentException("Kuantitas barang minimal 1")
            nominal * qty
        }

        assertTrue(kalkulasiResult.isFailure)
        assertTrue(kalkulasiResult.exceptionOrNull() is IllegalArgumentException)
        assertEquals("Kuantitas barang minimal 1", kalkulasiResult.exceptionOrNull()?.message)
    }

    @Test
    fun testDefensiveCalculation_emptyString() {
        val nominalStr = ""
        val qtyStr = "2"

        val kalkulasiResult = runCatching {
            val nominal = nominalStr.toDouble()
            val qty = qtyStr.toInt()
            if (qty <= 0) throw IllegalArgumentException("Kuantitas barang minimal 1")
            nominal * qty
        }

        assertTrue(kalkulasiResult.isFailure)
        assertTrue(kalkulasiResult.exceptionOrNull() is NumberFormatException)
    }

    @Test
    fun testProductBarcodeNotFoundException() {
        val barcode = "8999999"
        val exception = ProductBarcodeNotFoundException(barcode)

        assertEquals("Barang dengan barcode [8999999] tidak terdaftar pada katalog sistem.", exception.message)
        assertEquals(barcode, exception.barcode)
    }

    @Test
    fun testPaymentGatewayTimeoutException() {
        val gateway = "QRIS Bank Settlement"
        val exception = PaymentGatewayTimeoutException(gateway)

        assertEquals("Layanan pembayaran QRIS Bank Settlement tidak merespons dalam 15 detik.", exception.message)
        assertEquals(gateway, exception.gatewayName)
    }

    @Test
    fun testCashierLimitExceededException() {
        val maxLimit = 10000000.0
        val exception = CashierLimitExceededException(maxLimit)

        val message = exception.message ?: ""
        assertTrue(message.contains("10.000.000") || message.contains("10,000,000"))
        assertEquals(maxLimit, exception.maxLimit, 0.001)
    }

    @Test
    fun testUiStateHierarchy() {
        val idleState: UiState<PosTransaction> = UiState.Idle
        val loadingState: UiState<PosTransaction> = UiState.Loading
        val errorState: UiState<PosTransaction> = UiState.Error("Timeout", canRetry = true)

        val dummyTrx = PosTransaction(
            transactionId = "TRX-001",
            barcode = "8991201",
            productName = "Susu UHT",
            totalAmount = 21500.0,
            status = "SUCCESS"
        )
        val successState: UiState<PosTransaction> = UiState.Success(dummyTrx)

        assertTrue(idleState is UiState.Idle)
        assertTrue(loadingState is UiState.Loading)
        assertTrue(errorState is UiState.Error)
        assertTrue(successState is UiState.Success)
        assertEquals(21500.0, (successState as UiState.Success).data.totalAmount, 0.001)
    }
}
