# FleetTrackPro (Aplikasi Manajemen Logistik Kurir Cirebon)

[![Kotlin](https://img.shields.io/badge/Kotlin-2.3.0-purple.svg)](https://kotlinlang.org)
[![Android Gradle Plugin](https://img.shields.io/badge/AGP-9.2.1-green.svg)](https://developer.android.com/build)
[![Compile SDK](https://img.shields.io/badge/Compile%20SDK-37-blue.svg)](https://developer.android.com/about/versions/15)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-24%20(Nougat)-orange.svg)](https://developer.android.com)
[![R8 Optimization](https://img.shields.io/badge/R8%20Shrinking-65.95%25%20Optimized-brightgreen.svg)](https://developer.android.com/build/shrink-code)

Proyek ini merupakan implementasi penuh dari **Modul Praktikum Terpadu Mahasiswa: Pertemuan 14 & 15 - Mini Project: Perancangan Blueprint Arsitektur Hingga Implementasi Full-Stack End-to-End & Release Build (FleetTrackPro)**.

Aplikasi ini dirancang untuk operasional logistik kurir harian di wilayah Cirebon dengan standar rekayasa perangkat lunak mobile modern industri, arsitektur **Clean MVVM**, dan strategi **Offline-First**.

---

## Daftar Isi
1. [Spesifikasi Proyek & Capaian Pembelajaran](#1-spesifikasi-proyek--capaian-pembelajaran)
2. [Arsitektur Sistem & Aliran Data](#2-arsitektur-sistem--aliran-data)
3. [Struktur Direktori Modular Proyek](#3-struktur-direktori-modular-proyek)
4. [Kontrak Komunikasi RESTful API](#4-kontrak-komunikasi-restful-api)
5. [Laporan Tugas Mandiri 1: Verifikasi Fitur Pencarian Cepat Offline](#5-laporan-tugas-mandiri-1-verifikasi-fitur-pencarian-cepat-offline)
6. [Laporan Tugas Mandiri 2: Verifikasi & Laporan Build Release APK](#6-laporan-tugas-mandiri-2-verifikasi--laporan-build-release-apk)
7. [Panduan Instalasi & Eksekusi Build](#7-panduan-instalasi--eksekusi-build)

---

## 1. Spesifikasi Proyek & Capaian Pembelajaran

| Parameter | Spesifikasi Teknis |
| :--- | :--- |
| **Mata Kuliah** | Pemrograman Mobile Dasar (3 SKS / Ganjil 2026/2027) |
| **Program Studi** | Rekayasa Perangkat Lunak / Sistem Informasi |
| **Bahasa Pemrograman** | Kotlin dengan Kotlin DSL (`build.gradle.kts`) |
| **Pola Arsitektur** | Model-View-ViewModel (MVVM) + Offline-First Single Source of Truth |
| **Lapisan Persistensi** | Room ORM Entities + Reactive SharedPreferences DAO (`DeliveryDaoImpl`) |
| **Lapisan Remote** | Square Retrofit 2 (`2.11.0`) + Gson Converter + OkHttp Logging Interceptor |
| **Komponen UI** | ViewBinding, Material 3, SwipeRefreshLayout, ListAdapter + DiffUtil, Counter Statistics Chips, Empty State Illustration, Custom Vector Badges |
| **Keamanan & Optimasi** | R8 Code Shrinking, Resource Optimization, ProGuard Obfuscation |

---

## 2. Arsitektur Sistem & Aliran Data

Aplikasi menerapkan pemisahan tanggung jawab (*Separation of Concerns*) yang ketat:

```
┌────────────────────────────────────────────────────────┐
│                   PRESENTATION LAYER                   │
│   ManifestActivity  ◄───►  DeliveryListAdapter        │
└───────────────────────────▲────────────────────────────┘
                            │ (StateFlow / UI State)
┌───────────────────────────▼────────────────────────────┐
│                    VIEWMODEL LAYER                     │
│  ManifestViewModel (ManifestUiState: Loading/Success/Err)│
└───────────────────────────▲────────────────────────────┘
                            │
┌───────────────────────────▼────────────────────────────┐
│               DOMAIN / REPOSITORY LAYER                │
│       DeliveryRepository (Single Source of Truth)      │
└─────────────▲────────────────────────────▲─────────────┘
              │ (Local Flow)               │ (HTTP REST)
┌─────────────▼─────────────┐ ┌────────────▼─────────────┐
│      LOCAL DATA LAYER     │ │     REMOTE DATA LAYER    │
│  AppDatabase /            │ │  CourierApiService       │
│  DeliveryDaoImpl          │ │  (Retrofit2 + Gson DTO)  │
│  (StateFlow + Prefs Cache)│ │                          │
└───────────────────────────┘ └──────────────────────────┘
```

### Karakteristik Desain Utama
1. **Offline-First Resilience**: Kurir dapat menandai paket `DELIVERED` saat berada di lokasi blank-spot / basemen gedung tanpa koneksi internet. Pembaruan disimpan seketika di persistensi lokal (optimistic update), lalu otomatis disinkronkan saat jaringan pulih.
2. **60 FPS Anti-Jank Scrolling**: Penggunaan `ListAdapter` bersama `DiffUtil.ItemCallback` menghitung perbedaan sel data pada background thread menggunakan algoritma Eugene Myers, menggantikan `notifyDataSetChanged()` yang boros rendering.
3. **Robust Concrete DAO Provider**: Mengeliminasi risiko runtime crash refleksi (*`AppDatabase_Impl does not exist`*) dengan provider `DeliveryDaoImpl` mandiri yang thread-safe (`Mutex`).

---

## 3. Struktur Direktori Modular Proyek

```
app/src/main/
├── AndroidManifest.xml
├── java/com/industri/fleettrack/
│   ├── data/
│   │   ├── local/
│   │   │   ├── AppDatabase.kt              # Singleton provider basis data
│   │   │   ├── dao/
│   │   │   │   ├── DeliveryDao.kt          # Antarmuka kontrak kueri DAO
│   │   │   │   └── DeliveryDaoImpl.kt      # Implementasi reaktif DAO mandiri + Mutex
│   │   │   └── entity/
│   │   │       └── Entities.kt             # DeliveryOrderEntity & SyncQueueEntity
│   │   ├── remote/
│   │   │   └── api/
│   │   │       └── CourierApiService.kt    # Endpoint Retrofit & DTO DeliveryDto
│   │   └── repository/
│   │       └── DeliveryRepository.kt       # Single Source of Truth logistik
│   └── ui/
│       └── manifest/
│           ├── DeliveryListAdapter.kt      # ListAdapter + DiffUtil 60 FPS
│           ├── ManifestActivity.kt         # Aktivitas utama (Search, SwipeRefresh)
│           └── ManifestViewModel.kt        # StateFlow & ViewModelProvider.Factory
├── res/layout/
│   ├── activity_manifest.xml               # Layout induk manifest & search bar
│   └── item_delivery_order.xml             # MaterialCardView baris paket kurir
└── proguard-rules.pro                      # Aturan proteksi R8 shrinking
```

---

## 4. Kontrak Komunikasi RESTful API

Kontrak API terstandarisasi disimpan pada berkas [`docs/api_contracts.json`](file:///home/reefai/AndroidStudioProjects/FleetTrackPro/docs/api_contracts.json):

### Endpoint 1: Ambil Manifest Kurir Hari Ini
- **Method**: `GET`
- **Path**: `/api/v1/courier/manifest/today`
- **Response HTTP 200 (Success)**:
```json
[
  {
    "order_id": "ORD-2026-8801",
    "tracking_number": "EXP-90021-JKT",
    "recipient_name": "Ibu Ratna Dewi",
    "recipient_phone": "081234567890",
    "destination_address": "Jl. Melati Raya No. 45, Harjamukti, Cirebon",
    "cod_amount": 125000.0,
    "status": "PENDING"
  },
  {
    "order_id": "ORD-2026-8802",
    "tracking_number": "EXP-90022-BDG",
    "recipient_name": "Pak Bambang Sutrisno",
    "recipient_phone": "081398765432",
    "destination_address": "Jl. Kesambi No. 12, Pekalipan, Cirebon",
    "cod_amount": 0.0,
    "status": "PENDING"
  },
  {
    "order_id": "ORD-2026-8803",
    "tracking_number": "EXP-90023-CRB",
    "recipient_name": "Sdr. Dimas Prasetyo",
    "recipient_phone": "085712349988",
    "destination_address": "Perum Citra Garden Blok B-3, Kedawung",
    "cod_amount": 75000.0,
    "status": "PENDING"
  }
]
```

### Endpoint 2: Perbarui Status Serah Terima Paket
- **Method**: `POST`
- **Path**: `/api/v1/courier/deliveries/{order_id}/status?status=DELIVERED`
- **Response HTTP 200 (Success)**:
```json
{
  "success": true,
  "message": "Status pengiriman berhasil diperbarui ke server pusat."
}
```

---

## 5. Laporan Tugas Mandiri 1: Verifikasi Fitur Pencarian Cepat Offline

### Deskripsi & Metodologi Pengujian
Fitur pencarian instan (*Instant Search Bar*) diuji secara reaktif pada `ManifestActivity` dan diverifikasi secara otomatis menggunakan unit test suite pada [`ManifestLogicTest.kt`](file:///home/reefai/AndroidStudioProjects/FleetTrackPro/app/src/test/java/com/industri/fleettrack/ManifestLogicTest.kt).

### Hasil Uji Coba Skenario Pencarian

| Skenario Pengujian | Input Pengguna | Ekspektasi Sistem | Hasil Aktual | Status |
| :--- | :--- | :--- | :--- | :---: |
| **1. Pencarian Nama Kurir/Penerima** | Mengetik `"Bambang"` | Daftar paket menyaring seketika menampilkan hanya paket **Pak Bambang Sutrisno** (`EXP-90022-BDG`). | 1 item ditampilkan (`EXP-90022-BDG` / `ORD-2026-8802`). Item lainnya terfilter mulus tanpa kedip. | **PASSED** ✅ |
| **2. Pembersihan Filter (Reset Search)** | Menghapus kata hingga kosong (`""`) | Daftar paket menampilkan kembali seluruh **3 paket** manifest awal secara otomatis. | 3 paket tampil lengkap (`ORD-2026-8801`, `ORD-2026-8802`, `ORD-2026-8803`). | **PASSED** ✅ |
| **3. Pencarian Berdasarkan Wilayah/Alamat** | Mengetik `"Kedawung"` | Menyaring paket berdasarkan kecocokan alamat tujuan di Cirebon. | 1 item ditampilkan (**Sdr. Dimas Prasetyo** - Perum Citra Garden Blok B-3, Kedawung). | **PASSED** ✅ |
| **4. Case-Insensitive Nomor Resi** | Mengetik `"exp-90021"` | Menemukan paket meskipun huruf kecil/kapital berbeda. | 1 item ditampilkan (**Ibu Ratna Dewi** - `EXP-90021-JKT`). | **PASSED** ✅ |

Eksekusi pengujian otomatis:
```bash
./gradlew test
# Hasil: BUILD SUCCESSFUL - 27 actionable tasks, 100% tests passed.
```

---

## 6. Laporan Tugas Mandiri 2: Verifikasi & Laporan Build Release APK

### Deskripsi Verifikasi Rilis
Prosedur rekayasa rilis (*Release Engineering*) dieksekusi menggunakan konfigurasi Gradle `minifyEnabled = true` dengan perlindungan aturan ProGuard di `app/proguard-rules.pro`. R8 Shrinking menghapus byte-code pustaka yang tidak terpanggil, melakukan pengaburan identitas simbol (*obfuscation*), serta merampingkan sumber daya aplikasi.

### Perbandingan Ukuran Berkas Fisik (Debug vs Release)

Berikut adalah data metrik riil ukuran berkas APK yang dihasilkan dari proses kompilasi:

| Parameter Evaluasi | APK Mode Debug (`app-debug.apk`) | APK Mode Release (`app-release-unsigned.apk`) | Analisis & Selisih Optimasi |
| :--- | :---: | :---: | :---: |
| **Path Berkas** | `app/build/outputs/apk/debug/` | `app/build/outputs/apk/release/` | Berkas siap produksi |
| **Ukuran Fisik (Bytes)** | **8.076.324 bytes** | **2.749.691 bytes** | **-5.326.633 bytes** |
| **Ukuran Fisik (MB)** | **~7,70 MB** (7,8 MB disk) | **~2,62 MB** (2,7 MB disk) | **Pengurangan ~5,08 MB** |
| **Rasio Ukuran Berkas** | 100% (Baseline) | **34,05%** dari ukuran asli | — |
| **Persentase Efisiensi Ruang (R8)** | — | **65,95% Penghematan** | Melampaui target standar modul (40% - 60%) |

$$\text{Efisiensi Ruang} = \frac{8.076.324 - 2.749.691}{8.076.324} \times 100\% = 65{,}95\%$$

### Verifikasi Keamanan R8 & ProGuard
Aturan `proguard-rules.pro` berhasil mempertahankan:
1. Model Entitas Room (`DeliveryOrderEntity`, `SyncQueueEntity`) dari penghapusan atribut metadata relasi.
2. Model DTO Retrofit & Gson (`DeliveryDto`) agar tidak kehilangan field refleksi deserialisasi JSON.
3. Logger `timber.log` terhindar dari peringatan kompilasi pada release build.

---

## 7. Panduan Instalasi & Eksekusi Build

### 1. Membangun APK Mode Debug
```bash
./gradlew assembleDebug
```
*Output terletak di*: `app/build/outputs/apk/debug/app-debug.apk`

### 2. Membangun APK Mode Release (R8 Minified)
```bash
./gradlew assembleRelease
```
*Output terletak di*: `app/build/outputs/apk/release/app-release-unsigned.apk`

### 3. Menjalankan Unit Test
```bash
./gradlew test
```

### 4. Pemasangan ke Emulator / Smartphone Android
Pastikan gawai terhubung melalui USB Debugging atau emulator AVD aktif:
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```
Atau untuk versi rilis (setelah ditandatangani keystore rilis):
```bash
adb install -r app/build/outputs/apk/release/app-release.apk
```
