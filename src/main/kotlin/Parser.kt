import cc.ekblad.konbini.*

private val listStart = parser { whitespace(); char('('); whitespace() }
private val listEnd = parser { whitespace(); char(')'); whitespace() }
private val comma = parser { whitespace(); char(','); whitespace() }

private val decimal = parser {
    regex(Regex("[+\\-]?(?:0|[1-9]\\d*)(\\.\\d+)(?:[eE][+\\-]?\\d+)?")).toDouble()
}

private val atom = oneOf(
    decimal.map(Cell::Real),
    integer.map(Cell::Int),
    doubleQuotedString.map(Cell::Str),
    boolean.map(Cell::Bool),
    string("#NIL").map { Cell.NIL },
    regex("[a-zA-Z]\\w*").map(Cell::Symbol)
)

private val sequence = bracket(listStart, listEnd, parser {
    chain(expressionParser, comma).terms.reduce(Cell::Cons)
})

private val expressionParser: Parser<Cell> = oneOf(atom, sequence)

fun parse(input: String) = expressionParser.parseToEnd(input)
