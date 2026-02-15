// / Encapsulates the history of the REPL
object History {
    private var currentPos = -1
    private var currentInput = ""
    private val history = mutableListOf<String>()

    fun clear() {
        currentPos = -1
        currentInput = ""
        history.clear()
    }

    fun updateCurrent(input: String) {
        if (currentPos == -1) {
            currentInput = input
        }
    }

    fun push(elem: String) = history.add(elem)

    fun up(): String? =
        if (currentPos + 1 < history.size) {
            currentPos += 1
            history[history.size - currentPos - 1]
        } else {
            null
        }

    fun down(): String? =
        when (currentPos) {
            -1 -> {
                null
            }

            0 -> {
                currentPos = -1
                currentInput
            }

            else -> {
                currentPos -= 1
                history[history.size - currentPos - 1]
            }
        }
}
