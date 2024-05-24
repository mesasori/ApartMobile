package com.example.apart.features.apartments.data

data class ApartmentApiState(val status: Status, val data: List<ApartmentModelBackend>, val error: Error) {
    companion object {
        fun success(data: List<ApartmentModelBackend>): ApartmentApiState {
            return ApartmentApiState(Status.SUCCESS, data, Error.NONE)
        }

        fun error(err: Error): ApartmentApiState {
            return ApartmentApiState(Status.ERROR, listOf(), err)
        }

        fun loading(): ApartmentApiState {
            return ApartmentApiState(Status.LOADING, listOf(), Error.NONE)
        }
    }
}


enum class Status {
    SUCCESS,
    ERROR,
    LOADING
}

enum class Error {
    INTERNET,
    REQUEST,
    NONE
}