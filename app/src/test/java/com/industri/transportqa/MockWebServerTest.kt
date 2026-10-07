package com.industri.transportqa

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface FlightApiService {
    @GET("api/v1/flights/availabilities")
    suspend fun checkAvailability(): List<String>
}

class MockWebServerTest {
    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: FlightApiService

    @Before
    fun startServer() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        apiService = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FlightApiService::class.java)
    }

    @After
    fun stopServer() {
        mockWebServer.shutdown()
    }

    @Test
    fun `pemanggilan API ketersediaan jadwal sukses HTTP 200 mengembalikan daftar rute`() = runBlocking {
        // Siapkan respons tiruan server
        val mockJson = """["GA-102 (CGK - SUB)", "GA-204 (CGK - DPS)"]"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(mockJson))

        val result = apiService.checkAvailability()

        assertThat(result).hasSize(2)
        assertThat(result[0]).contains("CGK - SUB")
    }
}
