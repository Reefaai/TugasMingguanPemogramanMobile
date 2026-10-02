package com.praktikum.fintechbillingapp

import com.google.gson.Gson
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testIndonesianDateFormatting() {
        val rawDate = "2026-09-23 10:15:00"
        val formatted = rawDate.toIndonesianDateTime()
        assertEquals("Rabu, 23 September 2026 - Pukul 10:15 WIB", formatted)
    }

    @Test
    fun testInvoiceDtoWithVoucher() {
        val json = """
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
              "item_name": "Kabel Type-C 65W",
              "qty": 2,
              "unit_price": 75000.0,
              "subtotal": 150000.0
            }
          ],
          "summary": {
            "subtotal_amount": 150000.0,
            "discount_amount": 15000.0,
            "tax_ppn_11": 14850.0,
            "service_fee": 2500.0,
            "total_paid": 152350.0,
            "voucher_code": "HEMAT10",
            "voucher_discount_percent": 10
          }
        }
        """.trimIndent()

        val invoice = Gson().fromJson(json, InvoiceResponse::class.java)
        assertNotNull(invoice)
        assertEquals("INV-2026-FT9012", invoice.invoiceNumber)
        assertEquals("HEMAT10", invoice.summary.voucherCode)
        assertEquals(10, invoice.summary.voucherDiscountPercent)
    }

    @Test
    fun testInvoiceDtoNullableVoucher() {
        val json = """
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
          "items": [],
          "summary": {
            "subtotal_amount": 100000.0,
            "discount_amount": 0.0,
            "tax_ppn_11": 11000.0,
            "service_fee": 2500.0,
            "total_paid": 113500.0,
            "voucher_code": null,
            "voucher_discount_percent": null
          }
        }
        """.trimIndent()

        val invoice = Gson().fromJson(json, InvoiceResponse::class.java)
        assertNotNull(invoice)
        assertNull(invoice.summary.voucherCode)
        assertNull(invoice.summary.voucherDiscountPercent)
    }
}