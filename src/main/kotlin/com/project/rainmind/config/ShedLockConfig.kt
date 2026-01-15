package com.project.rainmind.config

import net.javacrumbs.shedlock.core.LockProvider
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcTemplate
import javax.sql.DataSource

@Configuration // 내부 bean 찾아서 등록
@EnableSchedulerLock(defaultLockAtMostFor = "PT30S") // period time 30 seconds
class ShedLockConfig {
    @Bean
    fun lockProvide(
        dataSource: DataSource // .properties 파일의 spring.datasource...어쩌고가 bean 등록되어 DI됨
    ): LockProvider = JdbcTemplateLockProvider( // configuration 기반 생성자 사용
        JdbcTemplateLockProvider.Configuration.builder()
            .withJdbcTemplate(JdbcTemplate(dataSource))
            .usingDbTime() // DB 시간 기준(pod 여러대이므로)
            .build()
    )
}