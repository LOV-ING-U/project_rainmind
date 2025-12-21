package com.project.rainmind.user.usersignup.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table(name = "users")
class User (
    @Id
    var id: Long? = null,
    @Column("password_hash")
    var passwordHash: String,
    @Column("nickname")
    var nickname: String,
    @Column("location")
    var location: String,
)