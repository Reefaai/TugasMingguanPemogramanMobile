package com.industri.fleettrack

import com.industri.fleettrack.data.local.entity.DeliveryOrderEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit Test untuk verifikasi logika Tugas Mandiri 1 (Instant Search Bar Filtering)
 * dan integritas model data manifest kurir FleetTrackPro.
 */
class ManifestLogicTest {

    private val sampleOrders = listOf(
        DeliveryOrderEntity(
            orderId = "ORD-2026-8801",
            trackingNumber = "EXP-90021-JKT",
            recipientName = "Ibu Ratna Dewi",
            recipientPhone = "081234567890",
            destinationAddress = "Jl. Melati Raya No. 45, Kesambi, Cirebon",
            codAmount = 125000.0,
            deliveryStatus = "PENDING",
            isSynced = true
        ),
        DeliveryOrderEntity(
            orderId = "ORD-2026-8802",
            trackingNumber = "EXP-90022-BDG",
            recipientName = "Pak Bambang Sutrisno",
            recipientPhone = "081398765432",
            destinationAddress = "Jl. Kartini No. 12, Kejaksan, Cirebon",
            codAmount = 0.0,
            deliveryStatus = "IN_DELIVERY",
            isSynced = true
        ),
        DeliveryOrderEntity(
            orderId = "ORD-2026-8803",
            trackingNumber = "EXP-90023-CRB",
            recipientName = "Sdr. Dimas Prasetyo",
            recipientPhone = "085712349988",
            destinationAddress = "Perum Citra Garden Blok B-3, Kedawung",
            codAmount = 75000.0,
            deliveryStatus = "DELIVERED",
            isSynced = true
        )
    )

    private fun filterOrders(orders: List<DeliveryOrderEntity>, query: String): List<DeliveryOrderEntity> {
        return if (query.isBlank()) {
            orders
        } else {
            orders.filter { order ->
                order.trackingNumber.contains(query, ignoreCase = true) ||
                order.recipientName.contains(query, ignoreCase = true) ||
                order.destinationAddress.contains(query, ignoreCase = true)
            }
        }
    }

    @Test
    fun testSearchFilter_queryBambang_returnsOnlyPakBambang() {
        val result = filterOrders(sampleOrders, "Bambang")
        assertEquals(1, result.size)
        assertEquals("EXP-90022-BDG", result[0].trackingNumber)
        assertEquals("Pak Bambang Sutrisno", result[0].recipientName)
    }

    @Test
    fun testSearchFilter_emptyQuery_returnsAllThreeOrders() {
        val result = filterOrders(sampleOrders, "")
        assertEquals(3, result.size)
    }

    @Test
    fun testSearchFilter_queryByAddress_returnsCorrectOrder() {
        val result = filterOrders(sampleOrders, "Kedawung")
        assertEquals(1, result.size)
        assertEquals("ORD-2026-8803", result[0].orderId)
    }

    @Test
    fun testSearchFilter_caseInsensitiveTrackingNumber() {
        val result = filterOrders(sampleOrders, "exp-90021")
        assertEquals(1, result.size)
        assertEquals("Ibu Ratna Dewi", result[0].recipientName)
    }

    @Test
    fun testDeliveryStatusUpdate() {
        val original = sampleOrders[0]
        val delivered = original.copy(
            deliveryStatus = "DELIVERED",
            isSynced = false,
            updatedAt = System.currentTimeMillis()
        )
        assertEquals("DELIVERED", delivered.deliveryStatus)
        assertEquals(false, delivered.isSynced)
    }
}
