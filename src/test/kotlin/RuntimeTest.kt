import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RuntimeTest {
    val runtime = Runtime()

    fun <T : Any?> assertSuccessEquals(
        expected: T,
        actual: Result<T>,
    ) {
        assertTrue(actual.isSuccess, "$expected should be a success")
        assertEquals(expected, actual.getOrNull())
    }

    @Test
    fun testPrimitiveEval() {
        assertSuccessEquals(Cell.NIL, runtime.eval(Cell.NIL))
        assertSuccessEquals(Cell.Int(23), runtime.eval(Cell.Int(23)))
        assertSuccessEquals(Cell.Real(23.5), runtime.eval(Cell.Real(23.5)))
        assertSuccessEquals(Cell.Str("foo"), runtime.eval(Cell.Str("foo")))
        assertSuccessEquals(Cell.Bool(true), runtime.eval(Cell.Bool(true)))
    }

    @Test
    fun testEvalKnownSymbol() {
        runtime.environment[Cell.Symbol("foo")] = Cell.Int(42)
        assertSuccessEquals(Cell.Int(42), runtime.eval(Cell.Symbol("foo")))
    }

    @Test
    fun testEvalUnknownSymbol() {
        assertTrue(runtime.eval(Cell.Symbol("foo")).isFailure)
    }

    @Test
    fun testEvalDefineKeyword() {
        assertSuccessEquals(
            Cell.Symbol("foobar"),
            runtime.eval(Cell.Symbol("define") + Cell.Symbol("foobar") + Cell.Int(42)),
        )
        assertEquals(Cell.Int(42), runtime.environment[Cell.Symbol("foobar")])
    }

    @Test
    fun testEvalDefineKeywordError() {
        assertTrue(runtime.eval(Cell.Symbol("define")).isFailure)
        assertTrue(runtime.eval(Cell.Symbol("define") + Cell.Symbol("blabla")).isFailure)
    }
}
