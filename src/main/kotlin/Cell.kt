sealed class Cell {
    object NIL : Cell() {
        override fun toString() = "#NIL"
    }

    class Int(val value: Long)  : Cell() {
        override fun toString() = value.toString()
        override fun equals(other: Any?) = other is Int && other.value == value
        override fun hashCode() = value.hashCode()
    }

    class Real(val value: Double) : Cell() {
        override fun toString() = value.toString()
        override fun equals(other: Any?) = other is Real && other.value == value
        override fun hashCode() = value.hashCode()
    }

    class Bool(val value: Boolean) : Cell() {
        override fun toString() = value.toString()
        override fun equals(other: Any?) = other is Bool && other.value == value
        override fun hashCode() = value.hashCode()
    }

    class Str(val value: String) : Cell() {
        override fun toString() = "\"$value\""
        override fun equals(other: Any?) = other is Str && other.value == value
        override fun hashCode() = value.hashCode()
    }

    class Symbol(val value: String) : Cell() {
        override fun toString() = value
        override fun equals(other: Any?) = other is Symbol && other.value == value
        override fun hashCode() = value.hashCode()
    }

    class Cons(val head: Cell, val tail: Cell): Cell() {
        override fun toString() = "($head, $tail)"
        override fun equals(other: Any?) = other is Cons && other.head == head && other.tail == tail
        override fun hashCode() = head.hashCode() * 31 + tail.hashCode()
    }

    operator fun plus(other: Cell) = Cons(this, other)
}