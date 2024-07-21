package com.isel.gomokuApi.repository

import com.isel.gomokuApi.domain.gameLogic.boardVariants.Variant
import com.isel.gomokuApi.domain.gameLogic.openingRules.Rule
import com.isel.gomokuApi.domain.model.game.GameOutcome
import com.isel.gomokuApi.domain.model.game.Position
import com.isel.gomokuApi.repository.model.Lobby
import com.isel.gomokuApi.repository.model.Match
import com.isel.gomokuApi.repository.model.Play

interface GamesRepository {

    /**
     * Returns a [Lobby] with the set of rules passed as parameters.
     *
     * @return [Lobby] or null if it does not exist.
     * */
    fun searchLobby (grid:Int, openingRule:Rule, variant: Variant): Lobby?

    /**
     * Returns a [Lobby] with [lobbyId].
     *
     * @return [Lobby] or null if it does not exist.
     * */
    fun getLobby(lobbyId: Int): Lobby?

    /**
     * Assigns player B to the desired [Lobby].
     * */
    fun updateLobby (lobbyId : Int, userBId : Int)

    /**
     * Initiates a lobby inserting [userAId] as the first player that wants to find an opponent
     * searching for a match with the same game rules.
     *
     * @return Id of the [Lobby].
     * */
    fun createLobby(userAId :Int, grid:Int, openingRule:Rule, variant: Variant):Int

    /**
     * Creates a match representing an active game.
     * */
    fun createMatch (matchId:Int,blackPlayerId : Int?, initialTime: Long)
    fun storePlay (play: Play)

    /**
     * Updates [Match] with its ending time and creates a [GameOutcome] in storage.
     * */
    fun createOutcome(outcome: GameOutcome)

    /**
     * Searches all the plays associated to the [Match] with [matchId].
     * */
    fun getPlays (matchId: Int):List<Play>
    /**
     * Searches for a finished game represented as with [lobbyId].
     *
     * @return A finished game represented as [GameOutcome]
     * */
    fun getOutcome(lobbyId: Int): GameOutcome?

    /**
     * Deletes the lobby from storage.
     * */
    fun closeLobby(lobbyId: Int)
    /**
     * Fetches the start time of the [Match] from the Storage.
     *
     * @return Time passed in seconds as [Long].
     * */
    fun getStartTime(lobbyId: Int): Long?
    /**
     * Searches for an active [Match] with [userId].
     *
     * @return [Match] when the user is currently in a running game or null if not.
     * */
    fun getMatchByUser(userId : Int): Match?

    /**
     * Searches for a [Match] with the given [lobbyId]
     * */
    fun getMatch(lobbyId: Int): Match?
    fun getRunningMatch(lobbyId: Int): Match?
    fun assignBlackPlayer(lobbyId: Int,userId: Int)
    fun storePieces(lobbyId: Int,positions: List<Position>)
    /**
     * Test dedicated function
     * */
    fun cleanPlays()
    /**
     * Searches for a incomplete lobby where the [userId] is waiting for opponent.
     *
     * @return [Lobby] or null if user is not waiting for an opponent.
     * */
    fun getIncompleteLobbyByUser(userId: Int): Lobby?
}