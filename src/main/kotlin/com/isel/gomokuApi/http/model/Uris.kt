package com.isel.gomokuApi.http.model

object Uris {
    private const val PREFIX = "api"

    object System{
        const val INFO = "$PREFIX/systemInfo"
    }
    object Users{
        private const val USR_PREFIX = "$PREFIX/user/"
        const val PROFILE = "${USR_PREFIX}profile"
        const val INFO_BY_NAME = "${USR_PREFIX}nickname-info/{nickname}"
        const val INFO_BY_ID = "${USR_PREFIX}id-info/{userId}"
        const val REGISTER = USR_PREFIX + "register"
        const val LOGIN = USR_PREFIX + "login"
        const val LOGOUT = USR_PREFIX +  "logout"
        const val IS_LOGIN = USR_PREFIX + "is-login"
    }
    object Statistics{
        const val GLOBAL = "$PREFIX/statistics"
        const val RANKING = "$GLOBAL/ranking/{page}"
        const val RANKING_ANDROID = "$GLOBAL/ranking/android/{page}"
    }
    object Game{
        private const val GAME_PREFIX = "${PREFIX}/game/"
        const val START_GAME = "${GAME_PREFIX}start"
        const val GET_GAME_STATE = "${GAME_PREFIX}{lobbyId}/state"
        const val GET_GAME_INFO = "${GAME_PREFIX}{lobbyId}"
        const val MAKE_PLAY = "${GAME_PREFIX}play/{lobbyId}"
        const val QUIT_GAME = "${GAME_PREFIX}quit/{lobbyId}"
        const val GET_BY_USER = "${GAME_PREFIX}active-match"
    }
}