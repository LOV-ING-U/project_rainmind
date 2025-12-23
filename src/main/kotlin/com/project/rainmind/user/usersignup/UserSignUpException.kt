package com.project.rainmind.user.usersignup

import com.project.rainmind.GlobalException
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode

sealed class UserSignUpException (
    http_errCode: HttpStatusCode,
    http_errCode_cause: Int,
    errMessage: String,
    cause: Throwable? = null,
): GlobalException(http_errCode, http_errCode_cause, errMessage, cause)

class UsernameAlreadyExistException :
        UserSignUpException(
            http_errCode = HttpStatus.CONFLICT,
            http_errCode_cause = 0,
            errMessage = "This name already exists. Try again."
        )

class InvalidPasswordFormatException :
        UserSignUpException(
            http_errCode = HttpStatus.BAD_REQUEST,
            http_errCode_cause = 1,
            errMessage = "Invalid password format. Try again."
        )

class InvalidUsernameFormatException :
        UserSignUpException(
            http_errCode = HttpStatus.BAD_REQUEST,
            http_errCode_cause = 1,
            errMessage = "Invalid username format. Try again."
        )