package com.isel.gomokuApi.http

import com.isel.gomokuApi.domain.localData.Authors
import com.isel.gomokuApi.http.model.StatusCode
import com.isel.gomokuApi.http.model.Uris
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class SystemInfoController(
    private val systemServices : SystemServices
) {

    @GetMapping(Uris.System.INFO)
    fun systemAuthors(): ResponseEntity<*>{
        val authors : List<Contact> = systemServices.getAuthors()
        return ResponseEntity.status(StatusCode.OK).contentType(
            MediaType.parseMediaType("application/json")
        ).body(mapOf("authors" to authors))
    }
}

data class Contact(val name : String,val email : String)

@Component
class SystemServices {
    fun getAuthors(): List<Contact> =
        listOf(
            Contact(Authors.Dev1.dev1Name,Authors.Dev1.dev1Email),
            Contact(Authors.Dev2.dev2Name,Authors.Dev2.dev2Email),
            Contact(Authors.Dev3.dev3Name,Authors.Dev3.dev3Email)
        )

}
