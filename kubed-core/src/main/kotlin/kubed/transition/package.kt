package kubed.transition

import javafx.util.Duration
import kubed.selection.Selection

val DEFAULT_DURATION: Duration = Duration.millis(250.0)

fun Selection.transition(): Transition = Transition(null, this)