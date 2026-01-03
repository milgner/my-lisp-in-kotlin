import com.varabyte.kotter.foundation.text.*
import com.varabyte.kotter.runtime.render.RenderScope

context(scope: RenderScope)
fun Cell.Cons.renderRecursive() {
    head.render()
    when (tail) {
        is Cell.Cons -> {
            scope.blue(isBright = true) { text(", ") }
            tail.renderRecursive()
        }
        Cell.NIL -> {}
        else -> {
            scope.blue(isBright = true) { text(" . ") }
            tail.render()
        }
    }
}

/// Adds some pizzazz when rendering cells (parse results) in the terminal
/// by omitting trailing `#NIL` elements and adding colours and parentheses.
/// Uses `RenderScope` for brevity and to conform to kotter's DSL style.
context(scope: RenderScope)
fun Cell.render() {
    val renderToString: RenderScope.() -> Unit = { text(this@render.toString()) }
    when (this) {
        is Cell.Bool -> scope.cyan { renderToString() }
        is Cell.Cons -> {
            scope.blue(isBright = true) { text("(") }
            renderRecursive()
            scope.blue(isBright = true) { text(")") }
        }
        // make integers orange
        is Cell.Int -> scope.hsv(35, 1.0f, 1.0f) { renderToString() }
        Cell.NIL -> scope.magenta { renderToString() }
        is Cell.Real -> scope.yellow { renderToString() }
        // strings are already recognizable due to the quotes
        is Cell.Str -> scope.renderToString()
        is Cell.Symbol -> scope.green { renderToString() }
    }
}
