package com.br.ml.brpathfinder.startup

import android.content.Context
import androidx.startup.Initializer
import com.br.ml.brpathfinder.BuildConfig
import com.br.ml.brpathfinder.di.AppModule
import com.br.ml.pathfinder.domain.di.DomainModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class KoinStartupInitializer : Initializer<KoinApplication> {
    override fun create(context: Context): KoinApplication = startKoin {
        androidContext(context)
        androidLogger(
            // FIXME: Change to back to INFO when koin 3.2.0 is released: https://github.com/InsertKoinIO/koin/issues/1188
            if (BuildConfig.DEBUG) Level.ERROR else Level.NONE
        )

        allowOverride(override = false)
        modules(
            listOf(
                AppModule,
                DomainModule,
            )
        )
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
