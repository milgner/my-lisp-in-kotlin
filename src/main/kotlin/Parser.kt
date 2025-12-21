import cc.ekblad.konbini.*

private val listStart = parser { whitespace(); char('('); whitespace() }
private val listEnd = parser { whitespace(); char(')'); whitespace() }
private val comma = parser { whitespace(); char(','); whitespace() }

private val decimal = Regex("[+\\-]?(?:0|[1-9]\\d*)(\\.\\d+)(?:[eE][+\\-]?\\d+)?")

private val atom = oneOf(
    parser { regex(decimal).toDouble() }.map(Cell::Real),
    integer.map(Cell::Int),
    doubleQuotedString.map(Cell::Str),
    boolean.map(Cell::Bool),
    string("#NIL").map { Cell.NIL },
    regex("\\:[a-zA-Z]\\w*").map { Cell.Symbol(it.drop(1)) }
)

private val sequence = bracket(listStart, listEnd, parser { chain(expressionParser, comma).terms })

private val expressionParser: Parser<Any?> = oneOf(atom, sequence)

fun parse(input: String) = expressionParser.parseToEnd(input)
