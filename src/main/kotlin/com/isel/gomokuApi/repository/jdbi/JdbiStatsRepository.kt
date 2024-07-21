package com.isel.gomokuApi.repository.jdbi

import com.isel.gomokuApi.domain.model.statistcs.BestPlayerRanking
import com.isel.gomokuApi.domain.model.statistcs.DefeatsRanking
import com.isel.gomokuApi.domain.model.statistcs.GamesRanking
import com.isel.gomokuApi.domain.model.statistcs.VictoriesRanking
import com.isel.gomokuApi.repository.StatsRepository
import com.isel.gomokuApi.repository.model.GlobalStatsData
import com.isel.gomokuApi.repository.model.TimePlayedRankingDB
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo

class JdbiStatsRepository(private val handle: Handle) : StatsRepository {
    override fun fetchGlobalStats(): GlobalStatsData {
        return handle.createQuery(
            """ 
           SELECT  SUM(duration) as total_duration,
                   COUNT(*) AS total_games,
                   COUNT(winner)  as total_wins 
           FROM outcome  
           """.trimIndent()
        )
            .mapTo<GlobalStatsData>()
            .first()
    }

    override fun bestPlayers(start:Int, end:Int): List<BestPlayerRanking> {
        return handle.createQuery(
            """
    SELECT
      id, nickname, total_points,
      ROW_NUMBER() OVER (ORDER BY total_points DESC) AS rank
    FROM (
      SELECT u.id, u.nickname,
        SUM(CASE WHEN u.id = l.player_a THEN o.a_points ELSE o.b_points END) AS total_points
      FROM users u
        LEFT JOIN lobby l ON u.id = l.player_a OR u.id = l.player_b
        LEFT JOIN outcome o ON l.id = o.match_id
      WHERE
        o.winner IS NOT NULL
      GROUP BY
        u.id, u.nickname
    ) AS subquery
    ORDER BY
      total_points DESC
    OFFSET :start ROWS
    FETCH NEXT :rows ROWS ONLY;
    """.trimIndent()
        )
            .bind("start", start)
            .bind("rows", end - start)
            .mapTo<BestPlayerRanking>()
            .list()
    }
    override fun mostVictories(start:Int, end:Int): List<VictoriesRanking> {
        return handle.createQuery(
            """
          SELECT
          id, nickname, total_wins,
          ROW_NUMBER() OVER (ORDER BY total_wins DESC) AS rank
          FROM (
            SELECT u.id, u.nickname,
                   COUNT(o.winner) AS total_wins
            FROM users u left join outcome o ON u.id = o.winner 
            where o.winner IS NOT NULL
            GROUP BY u.id
            ) AS subquery
            ORDER BY total_wins DESC
            OFFSET :start ROWS
            FETCH NEXT :rows ROWS ONLY;
    """.trimIndent()
        )
            .bind("start", start)
            .bind("rows", end - start)
            .mapTo<VictoriesRanking>()
            .list()
    }

    override fun mostGames(start:Int, end:Int): List<GamesRanking> {
        return handle.createQuery(
            """
          SELECT
          id, nickname, total_games,
          ROW_NUMBER() OVER (ORDER BY total_games DESC) AS rank
          FROM (
            SELECT u.id, u.nickname,
                   COUNT(l.id) as total_games
            FROM users u left join lobby l ON u.id = l.player_A OR u.id = l.player_B 
            left join outcome o ON l.id = o.match_id
            GROUP BY u.id
            ) AS subquery
          OFFSET :start ROWS
          FETCH NEXT :rows ROWS ONLY;
    """.trimIndent()
        )
            .bind("start", start)
            .bind("rows", end - start)
            .mapTo<GamesRanking>()
            .list()
    }

    override fun mostTime(start:Int, end:Int): List<TimePlayedRankingDB> {
        return handle.createQuery(
            """
          SELECT
          id, nickname, total_time,
          ROW_NUMBER() OVER (ORDER BY total_time DESC) AS rank
          FROM ( 
            SELECT u.id, u.nickname,
                   SUM(o.duration) as total_time
            FROM users u left join lobby l ON u.id = l.player_A OR u.id = l.player_B 
            left join outcome o ON l.id = o.match_id
            WHERE o.duration IS NOT NULL
            GROUP BY u.id
            ) AS subquery
            ORDER BY total_time DESC 
         OFFSET :start ROWS
         FETCH NEXT :rows ROWS ONLY;
    """.trimIndent()
        )
            .bind("start", start)
            .bind("rows", end - start)
            .mapTo<TimePlayedRankingDB>()
            .list()
    }

    override fun mostDefeats(start:Int, end:Int): List<DefeatsRanking> {
        return handle.createQuery(
            """
          SELECT
          id, nickname, total_games,
          ROW_NUMBER() OVER (ORDER BY total_games DESC) AS rank
          FROM (
            SELECT u.id, u.nickname,
                   COUNT(l.id) as total_games
            FROM users u left join lobby l ON u.id = l.player_A OR u.id = l.player_B 
            left join outcome o ON l.id = o.match_id
            where u.id <> o.winner
            GROUP BY u.id
            ) AS subquery
            ORDER BY total_games DESC
          OFFSET :start ROWS
          FETCH NEXT :rows ROWS ONLY;
    """.trimIndent()
        )
            .bind("start", start)
            .bind("rows", end - start)
            .mapTo<DefeatsRanking>()
            .list()
    }
}