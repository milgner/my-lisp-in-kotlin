import com.varabyte.kotter.foundation.input.Keys
import com.varabyte.kotter.foundation.input.getInput
import com.varabyte.kotter.foundation.input.input
import com.varabyte.kotter.foundation.input.onInputEntered
import com.varabyte.kotter.foundation.input.onKeyPressed
import com.varabyte.kotter.foundation.input.setInput
import com.varabyte.kotter.foundation.liveVarOf
import com.varabyte.kotter.foundation.runUntilSignal
import com.varabyte.kotter.foundation.session
import com.varabyte.kotter.foundation.text.text
import com.varabyte.kotter.foundation.text.textLine

fun main() = session {
    section {
        textLine("Marcus' LISP REPL")
        textLine("=================")
    }.run()

    var running = true

    while (running) {
        var lastEvaluationResult by liveVarOf("")
        section {
            text("> ")
            input()
            if (lastEvaluationResult.isNotEmpty()) {
                textLine("\n= $lastEvaluationResult")
            }
        }.runUntilSignal {
            onKeyPressed {
                when (key) {
                    Keys.EOF if getInput().isNullOrBlank() -> {
                        running = false
                        signal()
                    }

                    Keys.UP -> {
                        History.up()?.let(::setInput)
                    }

                    Keys.DOWN -> {
                        History.down()?.let(::setInput)
                    }
                }
            }
            onInputEntered {
                lastEvaluationResult = "Result"
                History.push(input)
                signal()
            }
        }
    }
    section {
        textLine()
        textLine("Bye bye bye bye bye bye bye")
    }.run()
}