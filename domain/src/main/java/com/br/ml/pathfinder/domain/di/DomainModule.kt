package com.br.ml.pathfinder.domain.di

import com.br.ml.pathfinder.domain.infrastructure.coroutine.DispatcherProvider
import com.br.ml.pathfinder.domain.infrastructure.coroutine.DispatcherProviderImpl
import org.koin.dsl.module

val DomainModule = module {
    single<DispatcherProvider> { DispatcherProviderImpl() }
}