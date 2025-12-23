package com.project.rainmind.user.usersignup.repository

import com.project.rainmind.user.usersignup.entity.User
import org.springframework.data.repository.ListCrudRepository

interface UserSignUpRepository : ListCrudRepository<User, Long>{
    fun findByUsername(
        username: String,
    ): User?
}