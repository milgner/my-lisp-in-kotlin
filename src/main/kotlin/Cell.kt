sealed class Cell {
    abstract val truthy: Boolean

    object NIL : Cell() {
        override fun toString() = "#NIL"

        override val truthy: Boolean
            get() = false
    }

    class Int(
        val value: Long,
    ) : Cell() {
        override fun toString() = value.toString()

        override fun equals(other: Any?) = other is Int && other.value == value

        override fun hashCode() = value.hashCode()

        override val truthy: Boolean
            get() = value != 0L
    }

    class Real(
        val value: Double,
    ) : Cell() {
        override fun toString() = value.toString()

        override fun equals(other: Any?) = other is Real && other.value == value

        override fun hashCode() = value.hashCode()

        override val truthy: Boolean
            get() = value != 0.0
    }

    class Bool(
        val value: Boolean,
    ) : Cell() {
        override fun toString() = value.toString()

        override fun equals(other: Any?) = other is Bool && other.value == value

        override fun hashCode() = value.hashCode()

        override val truthy: Boolean
            get() = value
    }

    class Str(
        val value: String,
    ) : Cell() {
        override fun toString() = "\"$value\""

        override fun equals(other: Any?) = other is Str && other.value == value

        override fun hashCode() = value.hashCode()

        override val truthy: Boolean
            get() = value.isNotBlank()
    }

    class Symbol(
        val value: String,
    ) : Cell() {
        override fun toString() = value

        override fun equals(other: Any?) = other is Symbol && other.value == value

        override fun hashCode() = value.hashCode()

        override val truthy: Boolean
            get() = true
    }

    class Cons(
        val head: Cell,
        val tail: Cell,
    ) : Cell(),
        Sequence<Cell> {
        override fun toString() = "($head, $tail)"

        override fun equals(other: Any?) = other is Cons && other.head == head && other.tail == tail

        override fun hashCode() = head.hashCode() * 31 + tail.hashCode()

        val len: UInt get() = len(this, 1u)

        private tailrec fun len(
            cell: Cons,
            startWith: UInt,
        ): UInt =
            when (cell.tail) {
                is Cons -> len(cell.tail, startWith + 1u)
                is NIL -> startWith
                else -> startWith + 1u
            }

        internal class ConsIterator(
            source: Cons,
        ) : AbstractIterator<Cell>() {
            private var current: Cell? = source

            override fun computeNext() {
                current?.let {
                    if (it is Cons) {
                        setNext(it.head)
                        current =
                            if (it.tail is NIL) {
                                null
                            } else {
                                it.tail
                            }
                    } else {
                        current = null
                        setNext(it)
                    }
                } ?: done()
            }
        }

        override fun iterator(): Iterator<Cell> = ConsIterator(this)

        override val truthy: Boolean
            get() = true
    }

    // / fixme: naming things...
    fun effective(): Cell = if (this is Cons && this.tail == NIL) this.head else this

    operator fun plus(other: Cell) =
        if (this is Cons) {
            Cons(this.head, Cons(this.tail, other))
        } else {
            Cons(this, other)
        }
}
