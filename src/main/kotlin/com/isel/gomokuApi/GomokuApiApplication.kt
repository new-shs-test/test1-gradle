package com.isel.gomokuApi

import com.isel.gomokuApi.domain.UserDomainConfig
import com.isel.gomokuApi.domain.model.Users.Sha256TokenEncoder
import com.isel.gomokuApi.http.pipeline.AuthenticatedUserArgumentResolver
import com.isel.gomokuApi.http.pipeline.AuthenticationInterceptor
import com.isel.gomokuApi.repository.jdbi.configureWithAppRequirements
import kotlinx.datetime.Clock
import org.jdbi.v3.core.Jdbi
import org.postgresql.ds.PGSimpleDataSource
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import kotlin.time.Duration.Companion.hours

@SpringBootApplication
class GomokuApiApplication{

	@Bean
	fun jdbi() = Jdbi.create(
		PGSimpleDataSource().apply {
			setURL(Environment.getDbUrl())
		}
	).configureWithAppRequirements()

	@Bean
	fun passwordEncoder() = BCryptPasswordEncoder()

	@Bean
	fun clock() = Clock.System
	@Bean
	fun tokenEncoder() = Sha256TokenEncoder()

	@Bean
	fun usersDomainConfig() = UserDomainConfig(
		tokenSizeInBytes = 256 / 8,
		tokenTtl = 24.hours,
		tokenRollingTtl = 1.hours
	)
}

@Configuration
class PipelineConfigurer(
	val authenticationInterceptor: AuthenticationInterceptor,
	val authenticatedUserArgumentResolver: AuthenticatedUserArgumentResolver
) : WebMvcConfigurer {

	override fun addInterceptors(registry: InterceptorRegistry) {
		registry.addInterceptor(authenticationInterceptor)
	}

	override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
		resolvers.add(authenticatedUserArgumentResolver)
	}
}
fun main(args: Array<String>) {
	runApplication<GomokuApiApplication>(*args)
}
