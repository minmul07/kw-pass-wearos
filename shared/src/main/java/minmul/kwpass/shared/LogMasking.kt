package minmul.kwpass.shared

fun String?.maskLastFourForLog(): String {
    if (this == null) return "NULL"

    val suffix = takeLast(4)
    return "*".repeat((length - suffix.length).coerceAtLeast(0)) + suffix
}
