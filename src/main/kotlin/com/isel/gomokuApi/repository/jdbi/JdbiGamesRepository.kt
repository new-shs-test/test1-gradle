package com.isel.gomokuApi.repository.jdbi

import com.isel.gomokuApi.domain.gameLogic.boardVariants.Variant
import com.isel.gomokuApi.domain.gameLogic.openingRules.Rule
import com.isel.gomokuApi.domain.model.game.GameOutcome
import com.isel.gomokuApi.domain.model.game.Position
import com.isel.gomokuApi.repository.GamesRepository
import com.isel.gomokuApi.repository.model.Lobby
import com.isel.gomokuApi.repository.model.Match
import com.isel.gomokuApi.repository.model.Play
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
class JdbiGamesRepository(private val handle: Handle ): GamesRepository {
    override fun searchLobby(grid: Int, openingRule: Rule, variant: Variant): Lobby? {
       return handle.createQuery(
           """
               SELECT * FROM Lobby
               WHERE player_B is null and grid = :grid and opening_rules = :opening_rules and variant = :variant
           """.trimIndent()
       )
           .bind("grid",grid)
           .bind("opening_rules", openingRule.name )
           .bind("variant",variant.name)
           .mapTo<Lobby>()
           .singleOrNull()
    }

    override fun getLobby(lobbyId: Int): Lobby? =
        handle.createQuery(
        "select * from lobby where id = :lobbyId")
        .bind("lobbyId", lobbyId)
        .mapTo<Lobby>()
        .singleOrNull()


    override fun updateLobby(lobbyId: Int, userBId: Int){
       handle.createUpdate("UPDATE Lobby SET player_B = :user_id where id = :id")
           .bind("user_id", userBId)
           .bind("id",lobbyId)
           .execute()
    }

    override fun createLobby(userAId: Int, grid: Int, openingRule: Rule, variant: Variant): Int {

        return handle.createUpdate(
                """
                INSERT INTO Lobby (player_A,grid,opening_rules,variant)
                VALUES (:player_A, :grid, :opening_rule, :variant)
                """.trimMargin())
            .bind("player_A",userAId)
            .bind("grid",grid)
            .bind("opening_rule",openingRule.name)
            .bind("variant", variant.name)
            .executeAndReturnGeneratedKeys("id")
            .mapTo<Int>()
            .single()
    }

    override fun createMatch(matchId: Int, blackPlayerId: Int?, initialTime: Long) {
       handle.createUpdate("INSERT INTO Match VALUES (:match_id,:blackId, :start_time)")
           .bind("match_id", matchId)
           .bind("blackId",blackPlayerId)
           .bind("start_time", initialTime)
           .execute()
    }

    override fun storePlay(play: Play) {
        handle.createUpdate( "INSERT INTO Play VALUES (:match_id, :line, :col)")
            .bind("match_id", play.matchId)
            .bind("line",play.position.lin)
            .bind("col",play.position.col)
            .execute()
    }

    override fun getPlays(matchId: Int): List<Play> {
        return handle.createQuery(
            """ 
                select match_id, line, col  
                from play where match_id = :match_id
            """.trimMargin())
            .bind("match_id", matchId)
            .mapTo<Play>()
            .list()
    }

    override fun getOutcome(lobbyId: Int): GameOutcome? {
       return  handle.createQuery("select * from outcome where match_id = :match_id")
           .bind("match_id",lobbyId)
           .mapTo<GameOutcome>()
           .singleOrNull()
    }

    override fun closeLobby(lobbyId: Int) {
        handle.createUpdate("DELETE FROM LOBBY WHERE id = :lobby_id")
            .bind("lobby_id",lobbyId)
            .execute()
    }

    override fun getStartTime(lobbyId: Int): Long? {
        return  handle.createQuery("select start_time from MATCH where match_id = :match_id")
            .bind("match_id",lobbyId)
            .mapTo<Long>()
            .singleOrNull()
    }

    override fun getMatchByUser(userId: Int): Match? {
        return handle.createQuery(
        """
            select * from 
            (select id from lobby where player_a = :user_id or player_b = :user_id)
             as lobby left join match m on lobby.id=m.match_id 
            where end_time is null
        """.trimIndent()
        ).bind("user_id",userId).mapTo<Match>().singleOrNull()
    }

    override fun getMatch(lobbyId: Int): Match?{
        return handle.createQuery(
            """
            select * from 
            match where match_id = :id  
        """.trimIndent()
        ).bind("id",lobbyId).mapTo<Match>().singleOrNull()
    }

    override fun getRunningMatch(lobbyId: Int): Match? {
        return handle.createQuery(
            """
           select * from 
            (select id from lobby where id = :id)
             as lobby right join match m on lobby.id=m.match_id 
            where m.end_time is null
        """.trimIndent()
        ).bind("id",lobbyId).mapTo<Match>().singleOrNull()
    }

    override fun assignBlackPlayer(lobbyId: Int, userId: Int) {
        TODO("Not yet implemented")
    }

    override fun storePieces(lobbyId: Int, positions: List<Position>) {
        var insertString = ""
        positions.forEach {
            insertString += "insert into play (match_id, line, col) values ($lobbyId,${it.lin},${it.col});"
        }
        handle.createUpdate(insertString).execute()
    }

    override fun cleanPlays() {
        handle.createUpdate(
            """
                delete from play;
            """.trimIndent()
        ).execute()
    }

    override fun getIncompleteLobbyByUser(userId: Int): Lobby? =
        handle.createQuery(
            """
                select * from lobby where player_a = :id and player_b is null
            """.trimIndent()
        ).bind("id",userId).mapTo<Lobby>()
            .singleOrNull()


    override fun createOutcome(outcome: GameOutcome) {
        handle.createUpdate("update match set end_time = (select start_time ) + :duration where match_id = :match_id;")
            .bind("duration",outcome.duration)
            .bind("match_id",outcome.id)
            .execute()

        handle.createUpdate("INSERT INTO OUTCOME VALUES (:match_id, :winner, :a_points, :b_points, :duration) ")
            .bind("match_id",outcome.id)
            .bind("winner",outcome.winner)
            .bind("a_points",outcome.playerAPoints)
            .bind("b_points",outcome.playerBPoints)
            .bind("duration",outcome.duration)
            .execute()
    }
}