import kotlin.Result.Companion.failure
import kotlin.Result.Companion.success

typealias BuiltinHandler = (List<Cell>) -> Result<Cell>

fun cons(args: List<Cell>): Result<Cell> =
    if (args.size != 2) {
        failure(Exception("Can only concatenate 2 arguments"))
    } else {
        success(Cell.Cons(args.first(), args.last()))
    }

fun head(args: List<Cell>): Result<Cell> =
    args.firstOrNull().let {
        if (args.size != 1 || it !is Cell.Cons) {
            failure(Exception("Needs 1 Cons argument"))
        } else {
            success(it.head)
        }
    }

fun tail(args: List<Cell>): Result<Cell> =
    args.firstOrNull().let {
        if (args.size != 1 || it !is Cell.Cons) {
            failure(Exception("Needs 1 Cons argument"))
        } else {
            success(it.tail)
        }
    }

fun eq(args: List<Cell>): Result<Cell> =
    if (args.size != 2) {
        failure(Exception("Can only compare two elements"))
    } else {
        success(Cell.Bool(args.first() == args.last()))
    }

fun <T : Any> T.success() = success(this)

fun plus(args: List<Cell>): Result<Cell> {
    var sum = 0.0
    for (arg in args) {
        when (arg) {
            is Cell.Int -> sum += arg.value
            is Cell.Real -> sum += arg.value
            else -> return failure(Exception("Not a numeric: $arg"))
        }
    }
    return Cell.Real(sum).success()
}

fun isNull(args: List<Cell>): Result<Cell> =
    if (args.size != 1) {
        failure(Exception("One argument required"))
    } else {
        success(Cell.Bool(args.first() == Cell.NIL))
    }

fun isInt(args: List<Cell>): Result<Cell> =
    if (args.size != 1) {
        failure(Exception("One argument required"))
    } else {
        success(Cell.Bool(args.first() is Cell.Int))
    }

fun isReal(args: List<Cell>): Result<Cell> =
    if (args.size != 1) {
        failure(Exception("One argument required"))
    } else {
        success(Cell.Bool(args.first() is Cell.Real))
    }

fun isNumber(args: List<Cell>): Result<Cell> =
    if (args.size != 1) {
        failure(Exception("One argument required"))
    } else {
        success(Cell.Bool(args.first() is Cell.Int || args.first() is Cell.Real))
    }

fun isString(args: List<Cell>): Result<Cell> =
    if (args.size != 1) {
        failure(Exception("One argument required"))
    } else {
        success(Cell.Bool(args.first() is Cell.Str))
    }

fun isSymbol(args: List<Cell>): Result<Cell> =
    if (args.size != 1) {
        failure(Exception("One argument required"))
    } else {
        success(Cell.Bool(args.first() is Cell.Symbol))
    }

fun isBool(args: List<Cell>): Result<Cell> =
    if (args.size != 1) {
        failure(Exception("One argument required"))
    } else {
        success(Cell.Bool(args.first() is Cell.Bool))
    }
