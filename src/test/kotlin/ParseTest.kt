import cc.ekblad.konbini.ParserResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertInstanceOf

class ParseTest {
    fun parseOk(input: String) =
        parse(input).let {
            assertInstanceOf<ParserResult.Ok<Cell>>(it)
            it.result
        }

    fun assertParseFails(input: String) = assertInstanceOf<ParserResult.Error>(parse(input))

    @Test
    fun parseFailsForSymbolsWithLeadingNumerics() {
        assertParseFails("23aoe")
    }

    @Test
    fun parseFailsForUnclosedParenthesis() {
        assertParseFails("(true")
        assertParseFails("92)")
    }

    @Test
    fun parseFailsForGarbageInList() {
        assertParseFails("( foo / )")
    }

    @Test
    fun parseFailsForTrailingGarbageInDegenerateList() {
        assertParseFails("( 1 2 . 3 4)")
    }

    @Test
    fun parseFailsForConcatenatedNils() {
        assertParseFails("#NIL#NIL")
        assertParseFails("(#NIL#NIL)")
    }

    @Test
    fun parseQuotedList() =
        assertEquals(
            Cell.Symbol("quote") + Cell.Int(42) + Cell.NIL,
            parseOk("'(42)"),
        )

    @Test
    fun testParseDegenerateList() =
        assertEquals(
            Cell.Cons(Cell.Int(1), Cell.Real(2.34)),
            parseOk("(1 . 2.34)"),
        )

    @Test
    fun parseEmptyListAsNil() = assertEquals(Cell.NIL, parseOk("()"))

    @Test
    fun parseListAsCons() {
        assertEquals(Cell.Cons(Cell.Bool(true), Cell.NIL), parseOk("(true)"))
        assertEquals(Cell.Cons(Cell.Int(42), Cell.Cons(Cell.Real(23.5), Cell.NIL)), parseOk("(42 23.5)"))
    }

    @Test
    fun parseReal() = assertEquals(Cell.Real(2.34), parseOk("2.34"))

    @Test
    fun parseInt() = assertEquals(Cell.Int(23), parseOk("23"))

    @Test
    fun parseNil() = assertEquals(Cell.NIL, parseOk("#NIL"))

    @Test
    fun parseString() = assertEquals(Cell.Str("hello"), parseOk("\"hello\""))

    @Test
    fun parseBoolean() {
        assertEquals(Cell.Bool(true), parseOk("true"))
        assertEquals(Cell.Bool(false), parseOk("false"))
    }

    @Test
    fun parseSymbol() = assertEquals(Cell.Symbol("a"), parseOk("a"))
}
