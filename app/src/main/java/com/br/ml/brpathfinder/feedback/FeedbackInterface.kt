package com.br.ml.brpathfinder.feedback

import com.br.ml.brpathfinder.models.Direction
import com.br.ml.brpathfinder.models.Risk

interface FeedbackInterface {
    fun signalUser(direction: Direction, severity: Float, position: Float)
    fun signalUser(risk: Risk) = signalUser(risk.direction, risk.severity, risk.position)
}