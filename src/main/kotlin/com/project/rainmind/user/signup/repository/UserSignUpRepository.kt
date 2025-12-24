package com.project.rainmind.user.signup.repository

import com.project.rainmind.user.signup.entity.User
import org.springframework.data.repository.ListCrudRepository

interface UserSignUpRepository : ListCrudRepository<User, Long>{
    fun findByNickname(
        nickname: String,
    ): Boolean
}