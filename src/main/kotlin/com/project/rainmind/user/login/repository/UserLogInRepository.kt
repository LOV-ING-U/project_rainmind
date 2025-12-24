package com.project.rainmind.user.login.repository

import com.project.rainmind.user.signup.entity.User
import org.springframework.data.repository.ListCrudRepository

interface UserLogInRepository : ListCrudRepository<User, Long> {
    fun findByNickname(
        nickname: String
    ): User?
}