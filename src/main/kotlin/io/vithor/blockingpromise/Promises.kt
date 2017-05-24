@file:JvmName("BlockingPromises")
package io.vithor.blockingpromise

import java.util.concurrent.*

/**
 * Created by Hazer on 24/05/17.
 */

internal class BlockingPromise<T> internal constructor(private var asyncBlock: Runnable) : Future<T> {

    sealed class State {
        class Success<out T>(val result: T) : State()
        class Failed(val throwable: Throwable) : State()
        object NotExecuted : State()
    }

    @Volatile private var state: State = State.NotExecuted

    @Volatile var _isCancelled = false
        private set

    private val countDownLatch: CountDownLatch = CountDownLatch(1)

    override fun cancel(mayInterruptIfRunning: Boolean): Boolean {
        if (isDone) {
            return false
        } else {
            countDownLatch.countDown()
            _isCancelled = true
            return !isDone
        }
    }

    private fun getOrThrow(): T? = state.let {
        when (it) {
            is State.Failed -> throw ExecutionException(it.throwable)
            is State.Success<*> -> it.result as T?
            is State.NotExecuted -> null
        }
    }

    @Throws(InterruptedException::class, ExecutionException::class)
    override fun get(): T? = when (state) {
        is State.NotExecuted -> {
            asyncBlock.run()
            countDownLatch.await()
            getOrThrow()
        }
        else -> getOrThrow()
    }

    @Throws(InterruptedException::class, ExecutionException::class, TimeoutException::class)
    override operator fun get(timeout: Long, unit: TimeUnit): T? = when (state) {
        is State.NotExecuted -> {
            asyncBlock.run()
            countDownLatch.await(timeout, unit)
            getOrThrow()
        }
        else -> getOrThrow()
    }

    override fun isCancelled(): Boolean = _isCancelled

    override fun isDone(): Boolean {
        return countDownLatch.count == 0L
    }

    internal fun onResult(result: T) {
        update(State.Success(result))
        countDownLatch.countDown()
    }

    internal fun onException(throwable: Throwable) {
        update(State.Failed(throwable))
        countDownLatch.countDown()
    }

    private fun update(newState: State) {
        if (this.state !is State.NotExecuted) {
            throw IllegalStateException("This promise cannot be executed twice, nor have the completion called twice. This Future works as an Either, if you are receiving 2 results, your implementation is wrong, or this class isn't what you need.")
        }
        this.state = newState
    }

    class Builder<T> {
        private var promise: BlockingPromise<T>? = null

        fun asyncAction(asyncBlock: AsyncAction<T>): Builder<T> = apply {
            promise = BlockingPromise(Runnable {
                try {
                    asyncBlock.execute(object : FirePromise<T> {
                        override fun success(result: T) {
                            promise!!.onResult(result)
                        }

                        override fun exceptionally(throwable: Throwable) {
                            promise!!.onException(throwable)
                        }
                    })
                } catch (e: Throwable) {
                    promise!!.onException(e)
                }
            })
        }

        fun build(): BlockingPromise<T> = promise!!
    }
}

interface FirePromise<in T> {
    fun success(result: T)
    fun exceptionally(throwable: Throwable)
}

inline fun <T> makePromise(crossinline asyncBlock: (FirePromise<T>) -> Unit)
        = makePromise(AsyncAction<T> { asyncBlock(it) })

@JvmName("makeFrom")
fun <T> makePromise(asyncBlock: AsyncAction<T>): Future<T> = BlockingPromise.Builder<T>()
        .asyncAction(asyncBlock)
        .build()