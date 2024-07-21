package com.isel.gomokuApi.http.model.outputModel

import com.isel.gomokuApi.domain.model.Users.UserInfo
import com.isel.gomokuApi.services.model.AwaitingOpponent
import com.isel.gomokuApi.services.model.GameEnded
import com.isel.gomokuApi.services.model.GameRunning
import com.isel.gomokuApi.services.model.GameStatus

sealed class GameStatusOutputModel
class GameDataOutput(val game: GameStatus): GameStatusOutputModel()
class GameRunningOutput(val gameRunning: GameStatus,val opponent:UserInfo?): GameStatusOutputModel()
class GameOpenedOutput(val gameOpened:GameStatus,val opponent:UserInfo?): GameStatusOutputModel()
class AwaitingOpponentOutput(val awaitingOpponent: GameStatus): GameStatusOutputModel()
class WaitingOpponentPiecesOutput(val waitingOpponentPieces: GameStatus,val opponent:UserInfo?): GameStatusOutputModel()
class GameEndedOutput(val gameEnded: GameStatus,val opponent:UserInfo?): GameStatusOutputModel()
class PlayMadeOutput(val playMade: GameStatus): GameStatusOutputModel()
class LobbyClosedOutput(val lobbyClosed: GameStatus): GameStatusOutputModel()