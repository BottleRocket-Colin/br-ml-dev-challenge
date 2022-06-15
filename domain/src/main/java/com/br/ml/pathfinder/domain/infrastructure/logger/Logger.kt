package com.br.ml.pathfinder.domain.infrastructure.logger

// TODO - Connect to Timber in data module.
interface Logger {
    fun w(message: String)
}