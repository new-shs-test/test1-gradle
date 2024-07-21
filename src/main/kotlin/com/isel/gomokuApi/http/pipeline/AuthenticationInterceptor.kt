package com.isel.gomokuApi.http.pipeline

import com.isel.gomokuApi.domain.AuthenticatedUser
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor

@Component
class AuthenticationInterceptor(
    private val authorizationHeaderProcessor : RequestTokenProcessor
) : HandlerInterceptor{
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        if (handler is HandlerMethod && handler.methodParameters.any {
                it.parameterType == AuthenticatedUser::class.java
            }
        ) {
            val isHttpOnly = request.cookies?.firstOrNull { it.name == RequestTokenProcessor.HTTP_ONLY_KEY }
            // enforce authentication
            val user = if (isHttpOnly == null){
                authorizationHeaderProcessor
                    .processAuthorizationHeaderValue(request.getHeader(NAME_AUTHORIZATION_HEADER))
            }else{
                authorizationHeaderProcessor
                .processAuthorizationCookieHeaderValue(isHttpOnly)
            }
            return if (user == null) {
                response.status = 401
                response.addHeader(NAME_WWW_AUTHENTICATE_HEADER, RequestTokenProcessor.SCHEME)
                false
            } else {
                AuthenticatedUserArgumentResolver.addUserTo(user, request)
                true
            }


        }

        return true
    }

    companion object {

        private val logger = LoggerFactory.getLogger(AuthenticationInterceptor::class.java)
        const val NAME_AUTHORIZATION_HEADER = "Authorization"
        private const val NAME_WWW_AUTHENTICATE_HEADER = "WWW-Authenticate"
    }
}