package com.br.ml.brpathfinder.startup

import android.content.Context
import androidx.startup.Initializer

class AppStartupInitializer : Initializer<Unit> {
    override fun create(context: Context) {}

    override fun dependencies() = listOf(
//        TimberStartupInitializer::class.java,
        KoinStartupInitializer::class.java,
    )
}