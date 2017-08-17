package kubed.timer

fun timer(delay: Int = 0, callback: Timer.(Long) -> Unit) = Timer(callback, delay)