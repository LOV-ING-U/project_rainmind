package com.project.rainmind.user.repository

import com.project.rainmind.user.entity.User
import org.springframework.data.repository.ListCrudRepository

interface UserSignUpRepository : ListCrudRepository<User, Long>{
    fun existsByNickname(
        nickname: String,
    ): Boolean
}