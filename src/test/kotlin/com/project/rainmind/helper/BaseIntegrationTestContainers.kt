package com.project.rainmind.helper

import org.junit.jupiter.api.BeforeAll
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@Testcontainers
abstract class BaseIntegrationTestContainers {
    // static
    companion object {
        private val mysql = MySQLContainer("mysql:8.0.28")
            .withDatabaseName("RainMind")
            .withUsername("RainMindTest")
            .withPassword("RainMindTest")

        private val redis = GenericContainer(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379)

        @JvmStatic
        @BeforeAll
        fun startMysqlAndRedis(){
            mysql.start()
            redis.start()
        }

        @JvmStatic
        @DynamicPropertySource
        fun registerProperties(
            registry: DynamicPropertyRegistry
        ) {
            registry.add("spring.datasource.url") {
                mysql.jdbcUrl
            }
            registry.add("spring.datasource.username") {
                mysql.username
            }
            registry.add("spring.datasource.password") {
                mysql.password
            }
            registry.add("spring.data.redis.host") {
                redis.host
            }
            registry.add("spring.data.redis.port") {
                redis.getMappedPort(6379)
            }
            registry.add("spring.profiles.active") {
                "test"
            }
            // 테스트용 JWT 서명 키(HS256 은 32바이트 이상 필요). 운영 값은 환경변수 JWT_SECRET 으로 주입한다
            registry.add("security.jwt.secret") {
                "rainmind-test-jwt-secret-key-at-least-32-bytes"
            }
            // 워커는 테스트에서 직접 호출한다(백그라운드 스케줄 실행 방지)
            registry.add("app.scheduling.enabled") {
                "false"
            }
        }
    }
}