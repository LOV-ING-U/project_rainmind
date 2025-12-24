package com.project.rainmind.user

import com.project.rainmind.exception.GlobalException
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode

sealed class UserException (
    http_errCode: HttpStatusCode,
    http_errCode_cause: Int,
    errMessage: String,
    cause: Throwable? = null,
): GlobalException(http_errCode, http_errCode_cause, errMessage, cause)

class UsernameAlreadyExistException :
        UserException(
            http_errCode = HttpStatus.CONFLICT,
            http_errCode_cause = 0,
            errMessage = "This name already exists. Try again."
        )

class NonExistingUsernameException :
        UserException(
            http_errCode = HttpStatus.NOT_FOUND,
            http_errCode_cause = 0,
            errMessage = "Username not exists. Try again."
        )

class InvalidUsernameFormatException :
    UserException(
        http_errCode = HttpStatus.BAD_REQUEST,
        http_errCode_cause = 1,
        errMessage = "Invalid username format. Try again."
    )

class InvalidPasswordFormatException :
        UserException(
            http_errCode = HttpStatus.BAD_REQUEST,
            http_errCode_cause = 1,
            errMessage = "Invalid password format. Try again."
        )

class PasswordNotCorrectException :
    UserException(
        http_errCode = HttpStatus.UNAUTHORIZED,
        http_errCode_cause = 1,
        errMessage = "Password is not correct. Try again."
    )

class AuthorizedFailureException :
        UserException(
            http_errCode = HttpStatus.UNAUTHORIZED,
            http_errCode_cause = 4,
            errMessage = "Authorized failed. Try again."
        )