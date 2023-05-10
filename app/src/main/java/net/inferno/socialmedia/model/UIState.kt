package net.inferno.socialmedia.model

open class UIState<T> private constructor(
    val loading: Boolean = false,
    val error: Exception? = null,
    val data: T? = null,
) {
    fun refresh(): UIState<T> = if (this is Success) {
        Refreshing(this.data!!)
    } else {
        Loading()
    }

    class Loading<T> : UIState<T>(loading = true)

    class Refreshing<T>(
        data: T,
    ) : UIState<T>(
        loading = true,
        data = data,
    )

    class Success<T>(
        data: T?
    ) : UIState<T>(data = data)

    class Failure<T>(
        error: Exception,
    ) : UIState<T>(error = error)

    class RefreshingFailure<T>(
        data: T,
        error: Exception,
    ) : UIState<T>(
        loading = true,
        data = data,
        error = error,
    )
}