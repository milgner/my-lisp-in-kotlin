import cc.ekblad.konbini.ParserResult
import com.varabyte.kotter.foundation.input.*
import com.varabyte.kotter.foundation.liveVarOf
import com.varabyte.kotter.foundation.runUntilSignal
import com.varabyte.kotter.foundation.session
import com.varabyte.kotter.foundation.text.*
import com.varabyte.kotter.runtime.render.RenderScope
import java.util.Optional

context(scope: RenderScope)
fun Cell.render() {
    val renderToString: RenderScope.() -> Unit = { text(this@render.toString()) }
    when (this) {
        is Cell.Bool -> scope.cyan { renderToString() }
        is Cell.Cons -> {
            scope.blue(isBright = true) { text("(") }
            head.render()
            scope.blue(isBright = true) { text(", ") }
            tail.render()
            scope.blue(isBright = true) { text(")") }
        }
        // make integers orange
        is Cell.Int -> scope.hsv(35, 1.0f, 1.0f) { renderToString() }
        Cell.NIL -> scope.magenta { renderToString() }
        is Cell.Real -> scope.yellow { renderToString() }
        is Cell.Str -> scope.renderToString()
        is Cell.Symbol -> scope.green { renderToString() }
    }
}

fun main() = session {
    section {
        textLine("Marcus' LISP REPL")
        textLine("=================")
    }.run()

    var running = true

    while (running) {
        var evaluationResult by liveVarOf(Optional.empty<ParserResult<Cell>>())
        section {
            text("> ")
            input()
            if (evaluationResult.isPresent) {
                when (val result = evaluationResult.get()) {
                    is ParserResult.Ok<Cell> -> { text("\n= "); result.result.render() }
                    is ParserResult.Error -> { red { textLine("\n Failed to parse") } }
                }
            }
        }.runUntilSignal {
            onKeyPressed {
                getInput()?.also(History::updateCurrent)
                when (key) {
                    Keys.EOF if getInput().isNullOrBlank() -> {
                        running = false
                        signal()
                    }

                    Keys.UP -> History.up()?.let(::setInput)
                    Keys.DOWN -> History.down()?.let(::setInput)
                }
            }
            onInputEntered {
                val parseResult = parse(input)
                evaluationResult = Optional.of(parseResult)
                when (parseResult) {
                    is ParserResult.Ok -> {
                        // do something
                        History.push(input)
                        signal()
                    }

                    is ParserResult.Error -> {
                        rejectInput()
                    }
                }
            }
        }
    }
    section {
        textLine()
        textLine("Bye bye bye bye bye bye bye")
    }.run()
}