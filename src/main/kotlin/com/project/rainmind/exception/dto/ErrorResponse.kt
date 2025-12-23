package com.project.rainmind.exception.dto

data class ErrorResponse (
    val errMessage: String,
    val err_code_internal: Int
)