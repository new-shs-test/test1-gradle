package com.isel.gomokuApi.http

import com.isel.gomokuApi.http.model.TokenResponse
import com.isel.gomokuApi.http.model.Uris
import com.isel.gomokuApi.services.UserExternalValue
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.web.reactive.server.WebTestClient
import kotlin.math.abs
import kotlin.random.Random


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserControllerTest {

    // One of the very few places where we use property injection
    @LocalServerPort
    var port: Int = 8080

    @Test
    fun `can create an user`() {
        // given: an HTTP client
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port/").build()

        // and: a random user
        val username = newTestUserName()
        val password = "changeIt20"
        val email = "$username@gmail.com"
        // when: creating an user
        // then: the response is a 201 with a proper Location header
        client.post().uri(Uris.Users.REGISTER)
            .bodyValue(
                mapOf(
                    "nickname" to username,
                    "email" to email,
                    "password" to password
                )
            )
            .exchange()
            .expectStatus().isCreated
    }

    @Test
    fun `can create an user, logout then login`() {
        // given: an HTTP client
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port/").build()

        // and: a random user
        val username = newTestUserName()
        val password = "changeIt20"
        val email = "$username@gmail.com"

        // when: resgistering an user
        // then: the response is a 201 with created token
        val result = client.post().uri(Uris.Users.REGISTER)
            .bodyValue(
                mapOf(
                    "nickname" to username,
                    "email" to email,
                    "password" to password
                )
            )
            .exchange()
            .expectStatus().isCreated
            .expectBody(UserExternalValue::class.java)
            .returnResult()
            .responseBody!!


        // when: revoking the token
        // then: response is a 200
        client.post().uri(Uris.Users.LOGOUT)
            .header("Authorization", "Bearer ${result.tokenInfo.tokenValue}")
            .exchange()
            .expectStatus().isOk

        //when: login an user
        // then: the response is a 200
        client.post().uri(Uris.Users.LOGIN)
            .bodyValue(
                mapOf(
                    "nickname" to username,
                    "password" to password
                )
            )
            .exchange()
            .expectStatus().isOk
    }
    companion object {
         fun newTestUserName() = "user${abs(Random.nextLong())}a"
    }
}
