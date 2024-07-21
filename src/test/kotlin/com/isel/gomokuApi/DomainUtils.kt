package com.isel.gomokuApi

import com.isel.gomokuApi.domain.gameLogic.boardVariants.StandardBoardVariant
import kotlinx.datetime.Clock
import org.junit.jupiter.api.Test
import org.springframework.http.ResponseCookie
import java.time.Duration
import kotlin.time.Duration.Companion.hours

class DomainUtils {

    private val clock = Clock.System

    @Test
    fun `time tests`() {
        /*val time1 = clock.now().epochSeconds
        Thread.sleep(5000)*/
        val time2 = clock.now()
        println(time2)
        val time4 = time2 + 24.hours
        /*val time3 = time2.epochSeconds + 24.hours.inWholeSeconds
        *//*println(time4)
        println(Instant.fromEpochSeconds(time3))*//*
        println(StandardBoardVariant::class.java.simpleName)
       // Instant.println(dur.toDuration(DurationUnit.SECONDS))
        println(Duration.ofSeconds(time3))*/
        val tokenCookie = ResponseCookie.from("authToken","token")
            .maxAge((time4 - clock.now()).inWholeSeconds).build()
        val tokenCookie1 = ResponseCookie.from("authToken","token")
            .maxAge(1).build()
        println(tokenCookie)
        println(tokenCookie1)
    }

    private val emailRegex = "^[\\w\\.-]+@[a-zA-Z\\d\\.-]+\\.[a-zA-Z]{2,}\$"

    @Test
    fun `email tests`() {


        /*
            Allowed characters: letters (a-z), numbers, underscores, periods, and dashes.
             An underscore, period, or dash must be followed by one or more letter or number.
             */
        val email = "user3257593181774506432a@gmail.com"
        val withoutDomain = email.split("@")[0]
        if (withoutDomain.length > 64) println("incorrect length")
        val char = withoutDomain[withoutDomain.length - 1]
        if (char == '_' || char == '.' || char == '-') println("char validation")
        println(email.matches(emailRegex.toRegex()))

    }
}