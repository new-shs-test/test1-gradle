package com.isel.gomokuApi.http

import com.isel.gomokuApi.domain.AuthenticatedUser
import com.isel.gomokuApi.domain.model.game.Position
import com.isel.gomokuApi.http.model.Problem
import com.isel.gomokuApi.http.model.StartGameInput
import com.isel.gomokuApi.http.model.StatusCode
import com.isel.gomokuApi.http.model.Uris
import com.isel.gomokuApi.http.model.outputModel.*
import com.isel.gomokuApi.services.GameServices
import com.isel.gomokuApi.services.GameValidationError
import com.isel.gomokuApi.services.UserServices
import com.isel.gomokuApi.services.model.*
import com.isel.gomokuApi.utils.Either
import com.isel.gomokuApi.utils.Failure
import com.isel.gomokuApi.utils.Success
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class GameController(private val gameService: GameServices, private val userSevice:UserServices) {
    private fun getOutputModel(res: Either.Right<GameStatus>) =
        when (val status = res.value) {
            is AwaitingOpponent -> AwaitingOpponentOutput(status)
            is Game -> GameDataOutput(status)
            is GameEnded -> GameEndedOutput(status,userSevice.getUserInfoById(status.opponentId))
            is GameOpened -> GameOpenedOutput(status,userSevice.getUserInfoById(status.opponentId))
            is GameRunning -> GameRunningOutput(status,userSevice.getUserInfoById(status.opponentId))
            is LobbyClosed -> LobbyClosedOutput(status)
            is PlayMade -> PlayMadeOutput(status)
            is WaitingOpponentPieces -> WaitingOpponentPiecesOutput(status,userSevice.getUserInfoById(status.opponentId))
        }
    @PostMapping(Uris.Game.START_GAME)
    fun startGame(@RequestBody input: StartGameInput, authorisedUser: AuthenticatedUser): ResponseEntity<*> {
        return when (val res = gameService.startGame(input.grid, input.openingRule, input.variant, authorisedUser.user.id)) {
            is Success -> {
                val output = getOutputModel(res)
                ResponseEntity.status(StatusCode.CREATED).body(output)
            }
            is Failure -> when (res.value) {
                GameValidationError.WaitingForOpponent -> Problem.response(StatusCode.BAD_REQUEST, Problem.userAlreadyInGame)
                GameValidationError.UserAlreadyInGame -> Problem.response(StatusCode.BAD_REQUEST, Problem.userAlreadyInGame)
                GameValidationError.InvalidVariant -> Problem.response(StatusCode.BAD_REQUEST, Problem.invalidVariant)
                GameValidationError.InvalidOpeningRule -> Problem.response(StatusCode.BAD_REQUEST,Problem.invalidOpeningRule)
                GameValidationError.InvalidGrid -> Problem.response(StatusCode.BAD_REQUEST, Problem.invalidGrid)
                else -> Problem.response(StatusCode.INTERNAL_SERVER_ERROR, Problem.unexpectedError)
            }
        }
    }

    @PostMapping(Uris.Game.MAKE_PLAY)
    fun makePlay(
        @PathVariable lobbyId: Int,
        @RequestBody pos: Position,
        authorisedUser: AuthenticatedUser
    ): ResponseEntity<*> {
        return when (val res = gameService.makePlay(pos, lobbyId, authorisedUser.user.id)) {
            is Success -> {
                val output = getOutputModel(res)
                ResponseEntity.status(StatusCode.OK).body(output)
            }

            is Failure -> when(res.value){
                GameValidationError.WaitingForOpponent -> Problem.response(StatusCode.BAD_REQUEST,Problem.waitingForOpponent)
                GameValidationError.InvalidPosition -> Problem.response(StatusCode.BAD_REQUEST,Problem.illegalPlay)
                GameValidationError.InvalidTurn -> Problem.response(StatusCode.BAD_REQUEST,Problem.invalidTurn)
                GameValidationError.LobbyNotFound -> Problem.response(StatusCode.NOT_FOUND, Problem.resourceNotFound)
                GameValidationError.UnauthorizedPlayer -> Problem.response(StatusCode.FORBIDDEN, Problem.unauthorizedAccess)
                else -> Problem.response(StatusCode.INTERNAL_SERVER_ERROR, Problem.unexpectedError)
            }
        }
    }

    @PostMapping(Uris.Game.QUIT_GAME)
    fun quitGame(@PathVariable lobbyId: Int, authorisedUser: AuthenticatedUser): ResponseEntity<*> {
        return when (val res = gameService.quitGame(lobbyId, authorisedUser.user.id)) {
            is Success -> {
                val output = getOutputModel(res)
                ResponseEntity.status(StatusCode.OK).body(output)
            }
            is Failure -> when (res.value) {
                GameValidationError.LobbyNotFound -> Problem.response(StatusCode.NOT_FOUND, Problem.resourceNotFound)
                GameValidationError.UnauthorizedPlayer -> Problem.response(StatusCode.FORBIDDEN, Problem.unauthorizedAccess)
                else -> Problem.response(StatusCode.INTERNAL_SERVER_ERROR, Problem.unexpectedError)
            }
        }
    }
    //TODO:add to doc
    @GetMapping(Uris.Game.GET_BY_USER)
    fun getActiveGameByUser(authorisedUser: AuthenticatedUser): ResponseEntity<*>{
        return when(val res = gameService.fetchActiveGameByUser(authorisedUser.user.id)){
            is Success -> {
                val output = getOutputModel(res)
                ResponseEntity.status(StatusCode.OK).body(output)
            }
            is Failure -> when(res.value){
                GameValidationError.LobbyNotFound -> Problem.response(StatusCode.NOT_FOUND, Problem.resourceNotFound)
                else -> Problem.response(StatusCode.INTERNAL_SERVER_ERROR, Problem.unexpectedError)
            }
        }
    }

    @GetMapping(Uris.Game.GET_GAME_STATE)
    fun getGameState(@PathVariable lobbyId: Int,authorisedUser: AuthenticatedUser): ResponseEntity<*>{
        return when (val res = gameService.matchState(lobbyId,authorisedUser.user.id)){
            is Success -> {
                val output = getOutputModel(res)
                ResponseEntity.status(StatusCode.OK).body(output)
            }
            is Failure -> when(res.value){
                GameValidationError.LobbyNotFound -> Problem.response(StatusCode.NOT_FOUND, Problem.resourceNotFound)
                GameValidationError.UnauthorizedPlayer -> Problem.response(StatusCode.FORBIDDEN, Problem.unauthorizedAccess)
                else -> Problem.response(StatusCode.INTERNAL_SERVER_ERROR, Problem.unexpectedError)
            }
        }
    }
    @GetMapping(Uris.Game.GET_GAME_INFO)
    fun getGameInfo(@PathVariable lobbyId: Int,authorisedUser: AuthenticatedUser): ResponseEntity<*>{
        return when (val res = gameService.fetchLobbyById(lobbyId,authorisedUser.user.id)){
            is Success -> {
                val output = getOutputModel(res)
                ResponseEntity.status(StatusCode.OK).body(output)
            }
            is Failure -> when(res.value){
                GameValidationError.LobbyNotFound -> Problem.response(StatusCode.NOT_FOUND, Problem.resourceNotFound)
                GameValidationError.UnauthorizedPlayer -> Problem.response(StatusCode.FORBIDDEN, Problem.unauthorizedAccess)
                else -> Problem.response(StatusCode.INTERNAL_SERVER_ERROR, Problem.unexpectedError)
            }
        }
    }



}