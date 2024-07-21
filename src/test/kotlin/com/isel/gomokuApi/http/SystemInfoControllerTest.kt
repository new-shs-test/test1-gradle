package com.isel.gomokuApi.http

import com.isel.gomokuApi.http.model.StatsResponse
import com.isel.gomokuApi.http.model.Uris
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.web.reactive.server.WebTestClient
import kotlin.test.assertEquals


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SystemInfoControllerTest {

    // One of the very few places where we use property injection
    @LocalServerPort
    var port: Int = 0

    @Test
    fun `get system info, authors and system version`() {
        // given: an HTTP client
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port/").build()

        val result = client.get().uri(Uris.System.INFO)
            .exchange()
            .expectStatus().isOk
            .expectBody(StatsResponse::class.java)
            .returnResult()
            .responseBody!!

        assertEquals(3, result.authors.size)
    }

}