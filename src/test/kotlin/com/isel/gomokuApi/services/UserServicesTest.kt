package com.isel.gomokuApi.services

import com.isel.gomokuApi.domain.UserDomain
import com.isel.gomokuApi.domain.UserDomainConfig
import com.isel.gomokuApi.domain.model.Users.Sha256TokenEncoder
import com.isel.gomokuApi.repository.localRep.LocalMemTransactionManager
import com.isel.gomokuApi.utils.Either
import com.isel.gomokuApi.utils.TestClock
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

class UserServicesTest {

    //given: User data for registration
    private val nickname = "Test1"
    private val email = "email@domain.com"
    private val password = "changeit02"
    @Test
    fun `can register, logout and login`() {
        val clock = TestClock()
        //given: The Normal Domain and User services
        val domain = createDomain()
        val services = createService(domain,clock)

        //when: Trying to register with valid data
        val token = when (val result = services.register(nickname, email, password)) {
            is Either.Left -> {
                fail(result.value.toString())
            }

            is Either.Right -> result.value.tokenInfo.tokenValue
        }
        //Then: a valid token is returned
        assertTrue(domain.canBeToken(token))

        //when: trying to log out
        val logOut = services.logout(token)
        //then: Operation must be a success
        assertTrue(logOut)

        //when: trying to log in

        val newToken = when(
            val result = services.login(nickname, password)
        ){
            is Either.Left -> {
                fail(result.value.toString())
            }
            is Either.Right -> result.value.tokenInfo.tokenValue
        }

        //Then: a valid token is returned and is not repeated
        assertTrue(domain.canBeToken(newToken))
        assertNotEquals(token,newToken)
    }

    @Test
    fun `operation integrity is guaranteed`(){
        val clock = TestClock()
        //given: The Normal Domain and User services
        val domain = createDomain()
        val services = createService(domain,clock)

        //given: A successfully registered User
        val token = (services.register(nickname, email, password) as Either.Right).value.tokenInfo

        //when: The same nickname tries to register again

        val errorDueToName = when(
            val result = services.register(nickname, email, password)
        ){
            is Either.Left -> result.value
            is Either.Right -> fail("Repeated nicknames should not be allowed")
        }
        //Then: Should fail with the NickNameAlreadyExists error
        assertIs<UserValidationError.NicknameAlreadyExists>(errorDueToName)

        //When: The same email tries to register again
        val errorDueToEmail = when(
            val result = services.register("nickname", email, password)
        ){
            is Either.Left -> result.value
            is Either.Right -> fail("Repeated emails should not be allowed")
        }
        //Then: Should fail with EmailAlreadyExists error
        assertIs<UserValidationError.EmailAlreadyExists>(errorDueToEmail)



    }

    companion object {
        private fun createDomain(
            tokenTtl: Duration = 30.days,
            tokenRollingTtl: Duration = 30.minutes
        ) = UserDomain(
            BCryptPasswordEncoder(),
            UserDomainConfig(
                tokenSizeInBytes = 256 / 8,
                tokenTtl,
                tokenRollingTtl
            ), Sha256TokenEncoder()

        )
        private fun createService(domain: UserDomain,testClock: TestClock) = UserServices(domain, LocalMemTransactionManager(), testClock)
    }
}