package de.teamnoco.books.config

import com.fasterxml.jackson.databind.ObjectMapper
import de.teamnoco.books.service.UserService
import de.teamnoco.books.web.exception.base.NotFoundException
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.BeanClassLoaderAware
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.session.SessionRegistry
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.jackson2.SecurityJackson2Modules
import org.springframework.security.web.context.HttpSessionSecurityContextRepository
import org.springframework.security.web.context.SecurityContextRepository
import org.springframework.session.data.redis.RedisIndexedSessionRepository
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisIndexedHttpSession
import org.springframework.session.security.SpringSessionBackedSessionRegistry
import kotlin.properties.Delegates

@Configuration
@EnableWebSecurity
@EnableRedisIndexedHttpSession(
    redisNamespace = "spring:session:books", maxInactiveIntervalInSeconds = SecurityConfig.SESSION_LIFETIME
)
@EnableMethodSecurity(securedEnabled = true)
class SecurityConfig : BeanClassLoaderAware {
    private var jacksonMapper: ObjectMapper by Delegates.notNull()
    private var classLoader: ClassLoader by Delegates.notNull()

    @PostConstruct
    fun onEnabled() {
        jacksonMapper = ObjectMapper().apply {
            registerModules(SecurityJackson2Modules.getModules(classLoader))
        }
    }

    @Bean
    fun redisSerializer(): RedisSerializer<Any> = GenericJackson2JsonRedisSerializer(jacksonMapper)

    @Bean
    fun redisSessionTemplate(factory: RedisConnectionFactory): RedisTemplate<String, Any> {
        val template = RedisTemplate<String, Any>()
        template.connectionFactory = factory
        template.hashKeySerializer = StringRedisSerializer()
        template.hashValueSerializer = GenericJackson2JsonRedisSerializer(jacksonMapper)
        return template
    }

    @Bean
    fun authenticationManager(
        httpSecurity: HttpSecurity, authenticationProvider: AuthenticationProvider
    ): AuthenticationManager = httpSecurity.getSharedObject(AuthenticationManagerBuilder::class.java)
        .authenticationProvider(authenticationProvider)
        .build()

    @Bean
    fun securityContextRepository(): SecurityContextRepository = HttpSessionSecurityContextRepository()

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun userDetailsService(userService: UserService): UserDetailsService = UserDetailsService {
        try {
            userService.getByEmail(it)
        } catch (e: NotFoundException) {
            throw BadCredentialsException("User details not found by email")
        }
    }

    @Bean
    fun sessionRegistry(sessionRepository: RedisIndexedSessionRepository): SessionRegistry =
        SpringSessionBackedSessionRegistry(sessionRepository)

    override fun setBeanClassLoader(classLoader: ClassLoader) {
        this.classLoader = classLoader
    }

    companion object {
        const val SESSION_LIFETIME = 2 * 24 * 60 * 60
    }
}