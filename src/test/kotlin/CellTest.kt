import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CellTest {
    val twoCons = Cell.Cons(Cell.Symbol("foo"), Cell.Cons(Cell.Symbol("bar"), Cell.NIL))
    val aberratedCons = Cell.Cons(Cell.Symbol("foo"), Cell.Cons(Cell.Symbol("bar"), Cell.Int(23)))

    @Test
    fun testConsIteratorAberrated() {
        assertEquals(3, aberratedCons.count())

        val iterator = aberratedCons.iterator()
        assertEquals(Cell.Symbol("foo"), iterator.next())
        assertEquals(Cell.Symbol("bar"), iterator.next())
        assertEquals(Cell.Int(23), iterator.next())
        assert(!iterator.hasNext())
    }

    @Test
    fun testConsIterator() {
        val iterator = twoCons.iterator()
        assertEquals(Cell.Symbol("foo"), iterator.next())
        assertEquals(Cell.Symbol("bar"), iterator.next())
        assert(!iterator.hasNext())
    }

    @Test
    fun testLen() {
        assertEquals(3u, aberratedCons.len)
        assertEquals(2u, twoCons.len)
    }

    @Test
    fun testPlusAsCons() {
        assertEquals(
            Cell.Cons(Cell.Symbol("foo"), Cell.Cons(Cell.Symbol("bar"), Cell.Int(42))),
            Cell.Symbol("foo") + Cell.Symbol("bar") + Cell.Int(42),
        )
    }
}
