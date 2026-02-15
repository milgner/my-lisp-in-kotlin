import cc.ekblad.konbini.ParserResult
import com.varabyte.kotter.foundation.input.*
import com.varabyte.kotter.foundation.liveVarOf
import com.varabyte.kotter.foundation.runUntilSignal
import com.varabyte.kotter.foundation.text.red
import com.varabyte.kotter.foundation.text.text
import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.runtime.Session
import java.util.*
import kotlin.Result.Companion.failure
import kotlin.Result.Companion.success

/**
 * Encapsulates all runtime information
 */
class Runtime {
    val environment = mutableMapOf<Cell.Symbol, Cell>()

    fun eval(cell: Cell): Result<Cell> =
        when (cell) {
            is Cell.NIL,
            is Cell.Int,
            is Cell.Real,
            is Cell.Bool,
            is Cell.Str,
            -> {
                success(cell)
            }

            is Cell.Symbol -> {
                environment[cell]?.let { success(it) }
                    ?: failure(Exception("Undefined symbol ${cell.value}"))
            }

            is Cell.Cons -> {
                apply(cell)
            }
        }

    fun define(
        what: Cell.Symbol,
        value: Cell,
    ): Result<Cell.Symbol> {
        environment[what] = value
        return success(what)
    }

    private fun evalIf(
        condition: Cell,
        onTruthy: Cell,
        onFalsy: Cell,
    ): Result<Cell> {
        val result = eval(condition)
        if (result.isFailure) {
            return failure(Exception("Failed to evaluate condition", result.exceptionOrNull()))
        }
        return if (result.getOrNull()!!.truthy) {
            eval(onTruthy)
        } else {
            eval(onFalsy)
        }
    }

    private val keywords: Map<Cell.Symbol, (args: Cell.Cons) -> Result<Cell>> =
        mapOf(
            Cell.Symbol("quote") to { args -> success(args.effective()) },
            Cell.Symbol("if") to { args ->
                val argList = args.toList()
                if (argList.size != 3) {
                    failure(Exception("Needs 3 arguments"))
                } else {
                    evalIf(argList[0], argList[1], argList[2])
                }
            },
            Cell.Symbol("define") to { args ->
                (args.head as? Cell.Symbol)?.let {
                    val what = eval(args.tail.effective())
                    if (what.isSuccess) {
                        define(it, what.getOrNull()!!)
                    } else {
                        what
                    }
                } ?: failure(Exception("nope"))
            },
        )

    private fun apply(cons: Cell.Cons): Result<Cell> {
        val keyword = cons.head as? Cell.Symbol
        val args = cons.tail as? Cell.Cons
        return if (keyword != null && keywords.containsKey(keyword) && args != null) {
            keywords[keyword]!!.invoke(args)
        } else {
            failure(Exception("Incorrect syntax"))
        }
    }

    sealed class EvalResult {
        class ParseFail(
            val parserError: ParserResult.Error,
        ) : EvalResult()

        class Success(
            val result: Cell,
        ) : EvalResult()

        class Error(
            val reason: Throwable,
        ) : EvalResult()
    }

    context(session: Session)
    fun repl(): Result<Unit> {
        var running = true

        while (running) {
            var evaluationResult by session.liveVarOf(Optional.empty<EvalResult>())
            session
                .section {
                    text("> ")
                    input()
                    if (evaluationResult.isPresent) {
                        when (val result = evaluationResult.get()) {
                            is EvalResult.Success -> {
                                text("\n= ")
                                result.result.render()
                            }

                            is EvalResult.ParseFail -> {
                                red { textLine("\n Failed to parse") }
                            }

                            is EvalResult.Error -> {
                                red { textLine("\n ! ${result.reason}") }
                            }
                        }
                    }
                }.runUntilSignal {
                    onKeyPressed {
                        getInput()?.also(History::updateCurrent)
                        when (key) {
                            Keys.EOF if getInput().isNullOrBlank() -> {
                                running = false
                                signal()
                            }

                            Keys.UP -> {
                                History.up()?.let(::setInput)
                            }

                            Keys.DOWN -> {
                                History.down()?.let(::setInput)
                            }
                        }
                    }
                    onInputEntered {
                        val result: EvalResult =
                            when (val parseResult = parse(input)) {
                                is ParserResult.Ok -> {
                                    val evalResult = eval(parseResult.result)
                                    if (evalResult.isSuccess) {
                                        History.push(input)
                                        EvalResult.Success(evalResult.getOrNull()!!).also { signal() }
                                    } else {
                                        EvalResult.Error(evalResult.exceptionOrNull()!!)
                                    }
                                }

                                is ParserResult.Error -> {
                                    EvalResult.ParseFail(parseResult).also {
                                        rejectInput()
                                    }
                                }
                            }
                        evaluationResult = Optional.of(result)
                    }
                }
        }
        return success(Unit)
    }
}
