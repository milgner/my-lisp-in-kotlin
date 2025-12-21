import cc.ekblad.konbini.ParserResult
import com.varabyte.kotter.foundation.input.*
import com.varabyte.kotter.foundation.liveVarOf
import com.varabyte.kotter.foundation.runUntilSignal
import com.varabyte.kotter.foundation.session
import com.varabyte.kotter.foundation.text.red
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
        var parseErrorEncountered by liveVarOf(false)

        section {
            text("> ")
            input()
            if (parseErrorEncountered) {
                red { textLine("\n !? Failed to parse input") }
            } else if (lastEvaluationResult.isNotEmpty()) {
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
                when (val result = parse(input)) {
                    is ParserResult.Ok -> {
                        parseErrorEncountered = false
                        lastEvaluationResult = result.result.toString()
                    }

                    is ParserResult.Error -> {
                        parseErrorEncountered = true
                        rejectInput()
                        return@onInputEntered
                    }
                }
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