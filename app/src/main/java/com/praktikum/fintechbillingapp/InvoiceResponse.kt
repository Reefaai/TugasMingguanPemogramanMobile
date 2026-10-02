package com.praktikum.fintechbillingapp
import com.google.gson.annotations.SerializedName
// 1. Root DTO Faktur Transaksi
data class InvoiceResponse(
    @SerializedName("invoice_number")
    val invoiceNumber: String,
    @SerializedName("transaction_date")
    val transactionDate: String,
    @SerializedName("payment_status")
    val paymentStatus: String,
    @SerializedName("payment_method")
    val paymentMethod: String,
    @SerializedName("merchant")
    val merchant: Merchant,
    @SerializedName("customer")
    val customer: Customer,
    @SerializedName("items")
    val items: List<InvoiceItem>,
    @SerializedName("summary")
    val summary: BillingSummary
)
// 2. DTO Customer Pembayar
data class Customer(
    @SerializedName("customer_id")
    val customerId: String,
    @SerializedName("full_name")
    val fullName: String,
    @SerializedName("phone")
    val phone: String
)
// 3. DTO Item Produk Belanja (JSON Array Element)
data class InvoiceItem(
    @SerializedName("item_id")
    val itemId: String,
    @SerializedName("item_name")
    val itemName: String,
    @SerializedName("qty")
    val qty: Int,
    @SerializedName("unit_price")
    val unitPrice: Double,
    @SerializedName("subtotal")
    val subtotal: Double
)
// 4. DTO Rangkuman Biaya & Pajak
data class BillingSummary(
    @SerializedName("subtotal_amount")
    val subtotalAmount: Double,
    @SerializedName("discount_amount")
    val discountAmount: Double = 0.0,
    @SerializedName("tax_ppn_11")
    val taxPpn11: Double,
    @SerializedName("service_fee")
    val serviceFee: Double,
    @SerializedName("total_paid")
    val totalPaid: Double,
    // Tugas 1: Defensive Nullable Handling
    @SerializedName("voucher_code")
    val voucherCode: String? = null,
    @SerializedName("voucher_discount_percent")
    val voucherDiscountPercent: Int? = null
)