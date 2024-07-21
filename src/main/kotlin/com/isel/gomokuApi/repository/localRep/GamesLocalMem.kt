package com.isel.gomokuApi.repository.localRep

import com.isel.gomokuApi.domain.gameLogic.boardVariants.Variant
import com.isel.gomokuApi.domain.gameLogic.openingRules.Rule
import com.isel.gomokuApi.domain.model.game.GameOutcome
import com.isel.gomokuApi.domain.model.game.Position
import com.isel.gomokuApi.repository.GamesRepository
import com.isel.gomokuApi.repository.localRep.mem.GameMem
import com.isel.gomokuApi.repository.model.Lobby
import com.isel.gomokuApi.repository.model.Match
import com.isel.gomokuApi.repository.model.Play

class GamesLocalMem : GamesRepository {

    val gameMem = GameMem()
    fun reset(){
        gameMem.reset()
    }
    override fun searchLobby(grid: Int, openingRule: Rule, variant: Variant): Lobby? {
        val lobby = gameMem.lobbys.filter {
            it.userBId == null && it.grid == grid && it.openingRule == openingRule && it.variant == variant
        }
        //Mimicking jdbi behavior
        return if (lobby.size != 1) null else lobby.first()
    }

    override fun getLobby(lobbyId: Int): Lobby? {
        return gameMem.lobbys.getOrNull(lobbyId)
    }

    override fun updateLobby(lobbyId: Int, userBId: Int) {
        val lobby = gameMem.lobbys.get(lobbyId)
        gameMem.lobbys.set(lobbyId,lobby.copy(userBId = userBId))
    }

    override fun createLobby(userAId: Int, grid: Int, openingRule: Rule, variant: Variant): Int {
        val index = gameMem.lobbys.lastOrNull()?.id?.plus(1)  ?: 0
        gameMem.lobbys.add(Lobby(index,userAId,null,grid, openingRule, variant))
        return index
    }

    override fun createMatch(matchId: Int, blackPlayerId: Int?, initialTime: Long) {
        gameMem.matches[matchId] = Match(matchId,blackPlayerId,initialTime,null)
    }

    override fun storePlay(play: Play) {
        gameMem.plays.add(play)
    }

    override fun createOutcome(outcome: GameOutcome) {
        gameMem.matches[outcome.id]?.let {
            gameMem.matches[outcome.id] = it.copy(endTime = it.startTime + outcome.duration)
            gameMem.outcomes.add(outcome)
        }
    }

    override fun getPlays(matchId: Int): List<Play> =
        gameMem.plays.filter {
            it.matchId == matchId
        }


    override fun getOutcome(lobbyId: Int): GameOutcome? =
        gameMem.outcomes.getOrNull(lobbyId)

    override fun closeLobby(lobbyId: Int) {
        gameMem.lobbys.removeAt(lobbyId)
    }

    override fun getStartTime(lobbyId: Int): Long? =
        gameMem.matches[lobbyId]?.startTime

    override fun getMatchByUser(userId: Int): Match? {
        var match : Match? = null
        gameMem.lobbys.forEach {
            val runningMatch = gameMem.matches[it.id]
            if ( runningMatch != null && (it.userBId == userId || it.userAId == userId)){
                match = runningMatch
            }
        }
        return match
    }

    override fun getMatch(lobbyId: Int): Match? =
        gameMem.matches[lobbyId]

    override fun getRunningMatch(lobbyId: Int): Match? {
        TODO("Not yet implemented")
    }

    override fun assignBlackPlayer(lobbyId: Int, userId: Int) {
        val currentMatch = gameMem.matches[lobbyId] ?: throw IllegalStateException("Match not found")
        gameMem.matches[lobbyId] = currentMatch.copy(blackPieceId = userId)
    }

    override fun storePieces(lobbyId: Int, positions: List<Position>) {
        positions.forEach {
            gameMem.plays.add(
                Play(
                    lobbyId,
                    it
                )
            )
        }
    }

    override fun cleanPlays() {
        gameMem.plays.clear()
    }

    override fun getIncompleteLobbyByUser(userId: Int): Lobby? {
        TODO("Not yet implemented")
    }


}
