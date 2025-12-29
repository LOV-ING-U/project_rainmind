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
        }
    }
}