package com.br.ml.brpathfinder.ui

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.br.ml.pathfinder.domain.infrastructure.coroutine.DispatcherProvider
import com.br.ml.pathfinder.domain.infrastructure.logger.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

abstract class BaseViewModel : ViewModel(), KoinComponent {
    protected val dispatcherProvider: DispatcherProvider by inject()
    protected val logger: Logger by inject()
//    protected val toaster: Toaster by inject()

    /**
     * Helper to launch to IO thread quickly
     */
    fun launchIO(block: suspend CoroutineScope.() -> Unit): Job =
        viewModelScope.launch(dispatcherProvider.IO, block = block)

    /**
     * Utility function to switch coroutine context to Main.
     * Useful for making UI updates from IO
     */
    suspend fun runOnMain(block: suspend CoroutineScope.() -> Unit) =
        withContext(dispatcherProvider.Main, block)

    // /////////////////////////////////////////////////////////////////////////
    // Error handling
    // /////////////////////////////////////////////////////////////////////////
    /**
     * Used to display error message with standard UI pattern
     */
    suspend fun handleError(@StringRes messageId: Int) {
        runOnMain {
//            toaster.toast(messageId)
        }
    }

    /**
     * Used to apply default error when handling Status and process a success block.
     */
    suspend inline fun <T : Any> Result<T>.handlingErrors(@StringRes messageId: Int, onSuccess: (T) -> Unit): Result<T> {
        if (isSuccess) {
            onSuccess(this.getOrThrow())
        } else {
            handleError(messageId = messageId)
        }
        return this
    }

//    /**
//     * Use to send [ExternalNavigationEvent]s (from subclasses).
//     *
//     * Note: You probably don't need to be observing this, as [observeNavigationEvents] is likely handling the observer setup for you. Available if necessary.
//     */
//    val externalNavigationEvent: SharedFlow<ExternalNavigationEvent> = MutableSharedFlow<ExternalNavigationEvent>()

//    /** Helper function to avoid needing downcast declarations for public MutableLiveData or LiveEvent */
//    protected fun <T> LiveData<T>.set(value: T?) = (this as? MutableLiveData<T>)?.setValue(value) ?: run { Timber.w("[set] unable to setValue for $this") }
//
//    /** Helper function to avoid needing downcast declarations for public MutableLiveData or LiveEvent */
//    protected fun <T> LiveData<T>.postValue(value: T?) = (this as? MutableLiveData<T>)?.postValue(value) ?: run { Timber.w("[postValue] unable to postValue for $this") }

    /**
     *  Helper functions to get access down casted mutable SharedFlows
     *    due to SharedFlow being covariant we must use templates with upper bounds to show type errors at build instead of run time.
     */
    protected suspend fun <T : Number?> SharedFlow<T>.emit(value: T) =
        (this as? MutableSharedFlow<T>)?.emit(value) ?: run { logger.w("[emitValue] unable to emit value for $this") }
    protected suspend fun <T : CharSequence> SharedFlow<T>.emit(value: T) =
        (this as? MutableSharedFlow<T>)?.emit(value) ?: run { logger.w("[emitValue] unable to emit value for $this") }
    protected suspend fun SharedFlow<Boolean>.emit(value: Boolean) =
        (this as? MutableSharedFlow<Boolean>)?.emit(value) ?: run { logger.w("[emitValue] unable to emit value for $this") }
    protected suspend fun SharedFlow<Unit>.emit(value: Unit) =
        (this as? MutableSharedFlow<Unit>)?.emit(value) ?: run { logger.w("[emitValue] unable to emit value for $this") }

    /** Helper functions to avoid needing downcast declarations for public MutableStateFlow */
    protected fun <T : Number> StateFlow<T>.setValue(value: T) {
        (this as? MutableStateFlow<T>)?.value = value
    }
    protected fun <T : CharSequence> StateFlow<T>.setValue(value: T) {
        (this as? MutableStateFlow<T>)?.value = value
    }
    protected fun StateFlow<Boolean>.setValue(value: Boolean) {
        (this as? MutableStateFlow<Boolean>)?.value = value
    }
    protected fun StateFlow<Unit>.setValue(value: Unit) {
        (this as? MutableStateFlow<Unit>)?.value = value
    }

    // Ties flow to viewModelScope to give StateFlow.
    fun <T> Flow<T>.groundState(initialValue: T) = this.stateIn(viewModelScope, SharingStarted.Lazily, initialValue)
}
