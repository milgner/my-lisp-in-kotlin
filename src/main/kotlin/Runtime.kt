@file:Suppress("ktlint:standard:no-wildcard-imports")

import Cell.Bool
import Cell.Builtin
import Cell.Cons
import Cell.Int
import Cell.NIL
import Cell.Real
import Cell.Str
import Cell.Symbol
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
import kotlin.reflect.KFunction1

infix fun String.to(handler: BuiltinHandler) = Symbol(this) to Builtin(handler)

fun KFunction1<List<Cell>, Result<Cell>>.envEntry() = Symbol(this.name) to Builtin(this)

/**
 * Encapsulates all runtime information
 */
class Runtime {
    val environment =
        mutableMapOf<Symbol, Cell>(
            ::cons.envEntry(),
            ::head.envEntry(),
            ::tail.envEntry(),
            "eq?" to ::eq,
            "+" to ::plus,
            "null?" to ::isNull,
            "integer?" to ::isInt,
            "real?" to ::isReal,
            "number?" to ::isNumber,
            "string?" to ::isString,
            "symbol?" to ::isSymbol,
        )

    fun eval(cell: Cell): Result<Cell> =
        when (cell) {
            is NIL,
            is Int,
            is Real,
            is Bool,
            is Str,
            is Builtin,
            -> {
                success(cell)
            }

            is Symbol -> {
                environment[cell]?.let { success(it) }
                    ?: failure(Exception("Undefined symbol ${cell.value}"))
            }

            is Cons -> {
                evalCons(cell)
            }
        }

    fun define(
        what: Symbol,
        value: Cell,
    ): Result<Symbol> {
        environment[what] = value
        return success(what)
    }

    private fun evalIf(
        condition: Cell,
        onTruthy: Cell,
        onFalsy: Cell?,
    ): Result<Cell> {
        val conditionResult = eval(condition)
        if (conditionResult.isFailure) {
            return failure(Exception("Failed to evaluate condition", conditionResult.exceptionOrNull()))
        }
        val condEval = conditionResult.getOrNull()!!
        return if (condEval.truthy) {
            eval(onTruthy)
        } else {
            if (onFalsy != null) {
                eval(onFalsy)
            } else {
                success(condEval)
            }
        }
    }

    private val keywords: Map<Symbol, (args: Cons) -> Result<Cell>> =
        mapOf(
            Symbol("quote") to { args -> success(args) },
            Symbol("if") to { args ->
                val argList = args.toList()
                if ((2..3).contains(argList.size)) {
                    evalIf(argList[0], argList[1], argList.getOrNull(2))
                } else {
                    failure(Exception("Needs 3 arguments"))
                }
            },
            Symbol("define") to { args ->
                (args.head as? Symbol)?.let {
                    val what = eval(args.tail.effective())
                    if (what.isSuccess) {
                        define(it, what.getOrNull()!!)
                    } else {
                        what
                    }
                } ?: failure(Exception("nope"))
            },
        )

    private fun evalCons(cons: Cons): Result<Cell> {
        val keyword = cons.head as? Symbol
        val args = cons.tail as? Cons
        return if (keyword != null && keywords.containsKey(keyword) && args != null) {
            keywords[keyword]!!.invoke(args)
        } else {
            evalBuiltin(cons)
        }
    }

    // wishing for `traverseEither` here but not willing to integrate Arrow at this point
    fun <T, R> Sequence<T>.mapOrFailure(transform: (T) -> Result<R>): Result<List<R>> {
        val results = mutableListOf<R>()
        for (item in this) {
            transform(item)
                .onFailure { return failure(it) }
                .onSuccess { results.add(it) }
        }
        return success(results)
    }

    private fun evalBuiltin(cons: Cons): Result<Cell> {
        val evaled = cons.mapOrFailure { eval(it) }.onFailure { return failure(it) }.getOrThrow()
        val fn = evaled.first()
        return if (fn is Builtin) {
            // subList is just a view and doesn't allocate a new list
            fn.handler(evaled.subList(1, evaled.size))
        } else {
            failure(Exception("Cannot invoke $fn"))
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
