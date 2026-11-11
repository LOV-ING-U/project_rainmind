package com.project.rainmind.user

import com.project.rainmind.user.entity.User
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.ListCrudRepository
import org.springframework.data.repository.query.Param

interface UserRepository : ListCrudRepository<User, Long> {
    fun existsByNickname(
        nickname: String
    ): Boolean

    fun findByNickname(
        nickname: String
    ): User?

    // 사용자 행에 X 락(레코드 락)을 건다. 같은 사용자의 일정 생성을 직렬화하는 용도
    @Query("SELECT * FROM users WHERE nickname = :nickname FOR UPDATE")
    fun findByNicknameForUpdate(
        @Param("nickname") nickname: String
    ): User?
}
