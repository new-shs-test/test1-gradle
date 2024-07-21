package com.isel.gomokuApi.http.model

import org.springframework.http.ResponseEntity
import java.net.URI

class Problem(
    typeUri: URI, errorSummary: String
) {
    //Respect the order of as defined in the norm.
    val type = typeUri.toASCIIString()
    val title = errorSummary
    companion object {

        const val MEDIA_TYPE = "application/problem+json"
        const val STATIC_REPO = "https://github.com/isel-leic-daw/2023-daw-leic51n-2023-daw-leic51n-g14/tree/main/"
        fun response(status: Int, problem: Problem) = ResponseEntity
            .status(status)
            .header("Content-Type", MEDIA_TYPE)
            .body<Any>(problem)

        val unregisteredUser = Problem(
            URI(
                STATIC_REPO + "code/jvm/doc/problems/unregisteredUser"
            ), "You have not been registered yet."
        )
        val nicknameAlreadyExists = Problem(
            URI(
                STATIC_REPO + "code/jvm/doc/nicknameAlreadyExists",
            ), "This nickname is already in use."
        )

        val emailAlreadyExists = Problem(
            URI(
                STATIC_REPO + "code/jvm/doc/emailAlreadyExists"
            ), "You already have an account."
        )
        val invalidEmailFormat = Problem(
            URI(
                STATIC_REPO + "code/jvm/doc/invalidEmailFormat"
            ), "Email formatting is incorrect."
        )
        val invalidPassword = Problem(
            URI(
                STATIC_REPO + "code/jvm/doc/invalidPassword"
            ), "Incorrect or Invalid password format."
        )
        val userAlreadyLogged = Problem(
            URI(
                STATIC_REPO + "code/jvm/doc/problems/userAlreadyLogged"
            ), "You must logout before logging in."
        )
        val invalidGrid = Problem(
            URI(
                STATIC_REPO + "code/jvm/doc/invalidGrid"
            ), "The grid dimension is not valid."
        )
        val invalidVariant = Problem(
            URI(
                STATIC_REPO + "code/jvm/doc/invalidVariant"
            ), "The Game variant is not valid."
        )
        val unexpectedError = Problem(
            URI(
                STATIC_REPO + "code/jvm/doc/problems/unexpectedError"
            ), "Unforeseen error."
        )
        val resourceNotFound = Problem(
            URI(
                STATIC_REPO + "code/jvm/doc/problems/notFound"
            ), "Resource not Found"
        )
        val unauthorizedAccess = Problem(
            URI(
                STATIC_REPO + "code/jvm/doc/problems/unauthorizedAccess"
            ), "No permission to access resource"
        )
        val illegalPlay = Problem(
            URI(
                STATIC_REPO + "code/jvm/doc/problems/illegalPlay"
            ), "Position already occupied"
        )
        val invalidTurn = Problem(
            URI(
                STATIC_REPO + "code/jvm/doc/problems/invalidPlay"
            ), "Wait for opponent play"
        )
        val waitingForOpponent = Problem(
            URI(
                STATIC_REPO + "code/jvm/doc/problems/waitingForOpponent"
            ), "Wait for opponent"
        )
        val invalidOpeningRule = Problem(
            URI(
                STATIC_REPO + "code/jvm/doc/problems/invalidOppeningRule"
            ), "Unavailable opening rule"
        )

        val userAlreadyInGame = Problem(
            URI(
                STATIC_REPO + "code/jvm/doc/problems/userAlreadyInGame"
            ), "Already in a game"
        )
    }
}

