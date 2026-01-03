import cc.ekblad.konbini.ParserResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ParseTest {
    @Test
    fun parseFailsForSymbolsWithLeadingNumerics() =
        assertTrue(parse("23aoe") is ParserResult.Error)

    @Test
    fun parseFailsForUnclosedParenthesis() {
        assertTrue(parse("(true") is ParserResult.Error)
        assertTrue(parse("92)") is ParserResult.Error)
    }

    fun parseOk(input: String) = parse(input).let {
        assertTrue(it is ParserResult.Ok<Cell>)
        (it as ParserResult.Ok<Cell>).result
    }

    @Test
    fun testParseDegenerateList() =
        assertEquals(
            Cell.Cons(Cell.Int(1), Cell.Real(2.34)),
            parseOk("(1 . 2.34)")
        )

    @Test
    fun parseEmptyListAsNil() =
        assertEquals(Cell.NIL, parseOk("()"))

    @Test
    fun parseListAsCons() {
        assertEquals(Cell.Cons(Cell.Bool(true), Cell.NIL), parseOk("(true)"))
        assertEquals(Cell.Cons(Cell.Int(42), Cell.Cons(Cell.Real(23.5), Cell.NIL)), parseOk("(42, 23.5)"))
    }

    @Test
    fun parsePlusAsCons() =
        assertEquals(Cell.Cons(Cell.Symbol("foo"), Cell.Symbol("bar")), Cell.Symbol("foo") + Cell.Symbol("bar"))

    @Test
    fun parseReal() =
        assertEquals(Cell.Real(2.34), parseOk("2.34"))

    @Test
    fun parseInt() =
        assertEquals(Cell.Int(23), parseOk("23"))

    @Test
    fun parseString() =
        assertEquals(Cell.Str("hello"), parseOk("\"hello\""))

    @Test
    fun parseBoolean() {
        assertEquals(Cell.Bool(true), parseOk("true"))
        assertEquals(Cell.Bool(false), parseOk("false"))
    }

    @Test
    fun parseSymbol() =
        assertEquals(Cell.Symbol("a"), parseOk("a"))
}