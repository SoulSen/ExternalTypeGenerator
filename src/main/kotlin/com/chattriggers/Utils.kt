package com.chattriggers

internal const val TAB = "    "

operator fun String.times(num: Int): String {
    val sb = StringBuilder()

    for (i in 0..num) {
        sb.append(this)
    }

    return sb.toString()
}

internal val BLOCKED_TYPES = listOf(
        "ScriptObjectMirror",
        "ClientChatReceivedEvent"
)

//| identifier typeArguments? valueArguments? annotatedLambda*