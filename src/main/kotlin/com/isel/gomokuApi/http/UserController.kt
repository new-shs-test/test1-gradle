package com.isel.gomokuApi.http

import com.isel.gomokuApi.domain.AuthenticatedUser
import com.isel.gomokuApi.domain.model.Users.UserOutput
import com.isel.gomokuApi.http.model.Problem
import com.isel.gomokuApi.http.model.StatusCode
import com.isel.gomokuApi.http.model.Uris
import com.isel.gomokuApi.services.UserValidationError
import com.isel.gomokuApi.services.UserServices
import com.isel.gomokuApi.utils.Failure
import com.isel.gomokuApi.utils.Success
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

data class UserRegisterInputModel(val nickname: String, val email: String, val password: String)

data class UserLoginInputModel(val nickname: String,val password: String)
@RestController
class UserController(private val userServices : UserServices) {
    @PostMapping(Uris.Users.REGISTER)
    fun register(@RequestBody input : UserRegisterInputModel): ResponseEntity<*>{
        return when (val res  = userServices.register(input.nickname,input.email,input.password)){
            is Success -> {
                val tokenCookie = ResponseCookie.from("authToken",res.value.tokenInfo.tokenValue)
                    .maxAge(res.value.tokenInfo.maxAge).path("/").httpOnly(true).build()
                ResponseEntity.status(StatusCode.CREATED)
                    .header(HttpHeaders.SET_COOKIE,tokenCookie.toString())
                    .body(res.value)
            }
            is Failure -> when(res.value){
                UserValidationError.NicknameAlreadyExists -> Problem.response(StatusCode.BAD_REQUEST,Problem.nicknameAlreadyExists)
                UserValidationError.EmailAlreadyExists -> Problem.response(StatusCode.BAD_REQUEST,Problem.emailAlreadyExists)
                UserValidationError.InvalidEmailFormat -> Problem.response(StatusCode.BAD_REQUEST,Problem.invalidEmailFormat)
                UserValidationError.InvalidPassword -> Problem.response(StatusCode.BAD_REQUEST, Problem.invalidPassword)
                else -> Problem.response(StatusCode.INTERNAL_SERVER_ERROR,Problem.unexpectedError)
            }
        }
    }

    @PostMapping(Uris.Users.LOGIN)
    fun login(@RequestBody input : UserLoginInputModel): ResponseEntity<*>{
        return when (val res = userServices.login(input.nickname,input.password)){
            is Success -> {
                val tokenCookie = ResponseCookie.from("authToken",res.value.tokenInfo.tokenValue)
                    .maxAge(res.value.tokenInfo.maxAge).path("/").httpOnly(true).build()
                ResponseEntity.status(StatusCode.OK)
                    .header(HttpHeaders.SET_COOKIE,tokenCookie.toString())
                    .body(res.value)
            }
            is Failure -> when(res.value){
                UserValidationError.UserAlreadyLogged -> Problem.response(StatusCode.BAD_REQUEST,Problem.userAlreadyLogged)
                UserValidationError.UnregisteredUser -> Problem.response(StatusCode.BAD_REQUEST,Problem.unregisteredUser)
                UserValidationError.IncorrectPassword -> Problem.response(StatusCode.BAD_REQUEST,Problem.invalidPassword)
                UserValidationError.InvalidPassword -> Problem.response(StatusCode.BAD_REQUEST,Problem.invalidPassword)
                else -> Problem.response(StatusCode.INTERNAL_SERVER_ERROR, Problem.unexpectedError)
            }
        }
    }

    @PostMapping(Uris.Users.LOGOUT)
    fun logout(user : AuthenticatedUser) : ResponseEntity<Unit>{
        userServices.logout(user.token)
        return ResponseEntity.status(StatusCode.OK).header(HttpHeaders.SET_COOKIE,null).build()
    }

    @GetMapping(Uris.Users.INFO_BY_NAME)
    fun getUserInfoByName(@PathVariable nickname: String) : ResponseEntity<*>{
        val res = userServices.getUserInfoByName(nickname)
        return if (res == null ) Problem.response(StatusCode.NOT_FOUND,Problem.resourceNotFound)
        else ResponseEntity.status(StatusCode.OK).body(res)
    }

    @GetMapping(Uris.Users.INFO_BY_ID)
    fun getUserInfoById( @PathVariable userId: Int) : ResponseEntity<*>{
        val res = userServices.getUserInfoById(userId)
        return if (res == null ) Problem.response(StatusCode.NOT_FOUND,Problem.resourceNotFound)
        else ResponseEntity.status(StatusCode.OK).body(mapOf("userInfo" to res ))
    }
    @GetMapping(Uris.Users.PROFILE)
    fun getUserProfile(user : AuthenticatedUser) : ResponseEntity<*>{
        println(user)
        val res = userServices.getUserProfile(user.token)
        return if (res == null ) Problem.response(StatusCode.NOT_FOUND,Problem.resourceNotFound)
        else ResponseEntity.status(StatusCode.OK).body(mapOf("userProfile" to res ))
    }

    @GetMapping(Uris.Users.IS_LOGIN)
    fun isLogin(user : AuthenticatedUser) : ResponseEntity<*>{
        return ResponseEntity.status(StatusCode.OK).body(UserOutput(user.user.id,user.user.nickname))
    }

}


