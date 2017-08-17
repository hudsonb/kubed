package kubed.timer

import javafx.animation.AnimationTimer
import java.util.concurrent.TimeUnit

/**
 * Provides a simple abstraction over JavaFX's [AnimationTimer].
 *
 * These timers are triggered in response
 */
class Timer(val callback: Timer.(Long) -> Unit, var delay: Int = 0, var interval: Int = -1, val timeout: Boolean = false) {
    private val scheduledTime = System.nanoTime()
    private var startTime = -1L
    private var lastTick = -1L
    var isRunning = false

    private val timer = object : AnimationTimer() {
        override fun handle(now: Long) {
            if(TimeUnit.NANOSECONDS.toMillis(now - scheduledTime) > delay) {
                if(startTime == -1L) {
                    startTime = System.nanoTime()
                    lastTick = startTime
                }
                else
                {
                    val elapsed = TimeUnit.NANOSECONDS.toMillis(now - startTime)
                    if(interval != -1 && now - lastTick > interval) {
                        callback(elapsed)
                        lastTick = now
                    }
                    else
                    {
                        callback(elapsed)
                        if(timeout) stop()
                    }
                }
            }
        }
    }

    init {
        isRunning = true
        timer.start()
    }

    fun stop() {
        timer.stop()
        isRunning = false
    }
}

fun main(args: Array<String>) {
    timer { elapsed ->
        println(elapsed)
    }
}
