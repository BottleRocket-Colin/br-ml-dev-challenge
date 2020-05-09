package com.br.ml.brpathfinder.models

import androidx.annotation.FloatRange

data class Risk(val direction: Direction,
                @FloatRange(from=0.0,to=1.0)
                val severity: Float,
                val position: Float,
                val id: Int = 0)


enum class Direction { LEFT, RIGHT, BOTH }
