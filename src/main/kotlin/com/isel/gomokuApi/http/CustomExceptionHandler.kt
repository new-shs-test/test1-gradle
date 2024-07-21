package com.isel.gomokuApi.http

import com.isel.gomokuApi.http.model.Problem
import com.isel.gomokuApi.http.model.StatusCode
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.sql.SQLException

@ControllerAdvice

class CustomExceptionHandler : ResponseEntityExceptionHandler() {

    @ExceptionHandler(
        Exception::class
    )
    fun handleAll(ex:Exception): ResponseEntity<*> {
        logger.error("UNEXPECTED EXCEPTION", ex)
        return Problem.response(StatusCode.INTERNAL_SERVER_ERROR, Problem.unexpectedError)
    }

}