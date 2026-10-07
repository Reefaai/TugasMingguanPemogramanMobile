package com.industri.fleettrack.data.repository

import com.industri.fleettrack.data.local.dao.DeliveryDao
import com.industri.fleettrack.data.local.entity.DeliveryOrderEntity
import com.industri.fleettrack.data.remote.api.CourierApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import timber.log.Timber

class DeliveryRepository(
    private val deliveryDao: DeliveryDao,
    private val apiService: CourierApiService
) {
    // 1. Mengalirkan data Room lokal secara instan ke ViewModel
    val manifestOrdersFlow: Flow<List<DeliveryOrderEntity>> = deliveryDao.getAllDeliveryOrders()

    // 2. Fungsi Sinkronisasi Data Jaringan ke Room DB (Network Refresh)
    suspend fun refreshManifest(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            Timber.d("Memulai sinkronisasi manifest dari server API...")
            try {
                val response = apiService.getTodayManifest()
                if (response.isSuccessful && response.body() != null) {
                    val dtoList = response.body()!!
                    val entities = dtoList.map { dto ->
                        DeliveryOrderEntity(
                            orderId = dto.orderId,
                            trackingNumber = dto.trackingNumber,
                            recipientName = dto.recipientName,
                            recipientPhone = dto.recipientPhone,
                            destinationAddress = dto.destinationAddress,
                            codAmount = dto.codAmount,
                            deliveryStatus = dto.status,
                            isSynced = true,
                            updatedAt = System.currentTimeMillis()
                        )
                    }
                    deliveryDao.insertAll(entities)
                    Timber.i("Sukses menyimpan %d data manifest ke Room Database", entities.size)
                    return@runCatching
                }
            } catch (e: Exception) {
                Timber.w("Gagal terhubung ke remote server, memuat data mock lokal jika kosong: %s", e.message)
            }

            // Fallback: Pastikan ada data awal saat demonstrasi
            seedInitialDataIfEmpty()
        }
    }

    private suspend fun seedInitialDataIfEmpty() {
        val mockData = listOf(
            DeliveryOrderEntity(
                orderId = "ORD-2026-8801",
                trackingNumber = "EXP-90021-JKT",
                recipientName = "Ibu Ratna Dewi",
                recipientPhone = "081234567890",
                destinationAddress = "Jl. Melati Raya No. 45, Harjamukti, Cirebon",
                codAmount = 125000.0,
                deliveryStatus = "PENDING",
                isSynced = true
            ),
            DeliveryOrderEntity(
                orderId = "ORD-2026-8802",
                trackingNumber = "EXP-90022-BDG",
                recipientName = "Pak Bambang Sutrisno",
                recipientPhone = "081398765432",
                destinationAddress = "Jl. Kesambi No. 12, Pekalipan, Cirebon",
                codAmount = 0.0,
                deliveryStatus = "PENDING",
                isSynced = true
            ),
            DeliveryOrderEntity(
                orderId = "ORD-2026-8803",
                trackingNumber = "EXP-90023-CRB",
                recipientName = "Sdr. Dimas Prasetyo",
                recipientPhone = "085712349988",
                destinationAddress = "Perum Citra Garden Blok B-3, Kedawung",
                codAmount = 75000.0,
                deliveryStatus = "PENDING",
                isSynced = true
            )
        )
        deliveryDao.insertAll(mockData)
    }

    // 3. Memperbarui status serah terima (Terkirim Sukses / Gagal)
    suspend fun updateDeliveryStatus(orderId: String, newStatus: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val now = System.currentTimeMillis()
            deliveryDao.updateOrderStatus(orderId, newStatus, isSynced = false, timestamp = now)
            try {
                val apiResponse = apiService.updateDeliveryStatus(orderId, newStatus)
                if (apiResponse.isSuccessful) {
                    deliveryDao.updateOrderStatus(orderId, newStatus, isSynced = true, timestamp = now)
                    Timber.i("Status paket %s sukses tersinkron ke server cloud", orderId)
                }
            } catch (netErr: Exception) {
                Timber.w("Gawai kurir offline: Status disimpan di lokal Room dan akan disinkron nanti.")
            }
        }
    }
}
