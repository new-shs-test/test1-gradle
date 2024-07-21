package com.isel.gomokuApi.domain.model.game

import kotlin.random.Random

data class Position (val lin: Int, val col: Int){
    companion object{
        fun randomPosition(gridSize : Int): Position{
            return Position(Random.nextInt(0,gridSize),Random.nextInt(0,gridSize))
        }
    }
}