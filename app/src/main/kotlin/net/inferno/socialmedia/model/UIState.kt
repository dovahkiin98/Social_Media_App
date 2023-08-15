package net.inferno.socialmedia.model

open class UIState<T> private constructor(
    val loading: Boolean = false,
    val error: Exception? = null,
    val data: T? = null,
) {
    val isRefreshing get() = this is Loading && this.data != null

    fun loading(): UIState<T> = Loading(this.data)

    class Loading<T>(data: T? = null) : UIState<T>(data = data, loading = true)

    class Success<T>(
        data: T? = null
    ) : UIState<T>(data = data)

    class Failure<T>(
        error: Exception,
    ) : UIState<T>(error = error)
}