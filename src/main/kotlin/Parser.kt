import cc.ekblad.konbini.*

private val listStart = parser { whitespace(); char('('); whitespace() }
private val listEnd = parser { whitespace(); char(')'); whitespace() }

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

private val degenerateList = bracket(listStart, listEnd, atomically {
    val init = chain(expressionParser, whitespace).terms
    whitespace1()
    char('.')
    whitespace1()
    val final = expressionParser()
    init.foldRight(final, Cell::Cons)
})

private val sequence = bracket(listStart, listEnd, parser {
    chain(expressionParser, whitespace).terms.foldRight(Cell.NIL, Cell::Cons)
})

private val expressionParser: Parser<Cell> = oneOf(atom, degenerateList, sequence)

/// Parses the input string fully and returns a result that either contains an error or the parsed `Cell`.
fun parse(input: String) = expressionParser.parseToEnd(input)
