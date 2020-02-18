package com.br.ml.brpathfinder

interface FeedbackInterface {
    enum class Direction { LEFT, RIGHT }


    fun signalUser(direction: Direction, severity: Float) {
        if (severity > 1.0 || severity < 0.0) throw IllegalArgumentException()
        // TODO - Magic


    }

}