package kubed.transition

import javafx.util.Duration
import kubed.selection.Selection

val DEFAULT_DURATION: Duration = Duration.millis(250.0)

fun <T> Selection<T>.transition(): Transition<T> = Transition(null, this)
fun <T> Selection<T>.transition(f: Transition<T>.() -> Unit) {
    f(Transition(null, this))
}