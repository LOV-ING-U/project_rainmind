package com.project.rainmind.user.repository

import com.project.rainmind.user.entity.User
import org.springframework.data.repository.ListCrudRepository

interface UserLogInRepository : ListCrudRepository<User, Long> {
    fun findByNickname(
        nickname: String
    ): User?
}