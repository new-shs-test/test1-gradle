package com.isel.gomokuApi.http

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GlobalStatsControllerTest {
    // One of the very few places where we use property injection
    @LocalServerPort
    var port: Int = 0
    @Test
    fun `get global stats`() {
        // given: an HTTP client
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port/api").build()

        client.get().uri("/statistics")
            .exchange()
            .expectStatus().isOk
    }

    @Test
    fun `get ranking stats`() {
        // given: an HTTP client
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port/api").build()

        client.get().uri("/statistics/ranking/0")
            .exchange()
            .expectStatus().isOk
    }

}