import com.varabyte.kotter.foundation.session
import com.varabyte.kotter.foundation.text.textLine

// / Entry point which provides a REPL
fun main() =
    session {
        section {
            textLine("Marcus' LISP REPL")
            textLine("=================")
        }.run()

        val runtime = Runtime()
        runtime.repl()

        section {
            textLine()
            textLine("Bye bye bye bye bye bye bye")
        }.run()
    }
