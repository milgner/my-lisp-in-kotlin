sealed class Cell {
    object NIL : Cell() {
        override fun toString() = "#NIL"
    }

    class Int(val value: Long)  : Cell() {
        override fun toString() = value.toString()
    }

    class Real(val value: Double) : Cell() {
        override fun toString() = value.toString()
    }

    class Bool(val value: Boolean) : Cell() {
        override fun toString() = value.toString()
    }

    class Str(val value: String) : Cell() {
        override fun toString() = "\"$value\""
    }

    class Symbol(val value: String) : Cell() {
        override fun toString() = value
    }

    class Cons(val head: Cell, val tail: Cell): Cell() {
        override fun toString() = "($head, $tail)"
    }
}