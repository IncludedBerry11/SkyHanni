package at.hannibal2.skyhanni.utils

import java.util.regex.Pattern

object TimeUtils {

    private val pattern =
        Pattern.compile("(?:(?<y>\\d+) ?y(?:\\w* ?)?)?(?:(?<d>\\d+) ?d(?:\\w* ?)?)?(?:(?<h>\\d+) ?h(?:\\w* ?)?)?(?:(?<m>\\d+) ?m(?:\\w* ?)?)?(?:(?<s>\\d+) ?s(?:\\w* ?)?)?")

    fun formatDuration(
        millis: Long,
        biggestUnit: TimeUnit = TimeUnit.YEAR,
        showMilliSeconds: Boolean = false,
        longName: Boolean = false,
        maxUnits: Int = -1
    ): String {
        var milliseconds = millis + 999
        val map = mutableMapOf<TimeUnit, Int>()
        for (unit in TimeUnit.values()) {
            if (unit.ordinal >= biggestUnit.ordinal) {
                val factor = unit.factor
                map[unit] = (milliseconds / factor).toInt()
                milliseconds %= factor
            }
        }

        val builder = StringBuilder()
        var count = 0
        for ((unit, value) in map.entries) {
            if (value > 0 || builder.isNotEmpty() || unit == TimeUnit.SECOND) {
                builder.append(value)
                val name = if (longName) {
                    " " + unit.longName + if (value > 1) "s" else ""
                } else {
                    unit.shortName
                }

                if (unit == TimeUnit.SECOND) {
                    if (showMilliSeconds) {
                        val formatMillis = milliseconds / 100
                        builder.append(".")
                        builder.append(formatMillis)
                    }
                    builder.append(name)
                } else {
                    builder.append("$name ")
                }

                count++
                if (maxUnits != -1) {
                    if (count == maxUnits) break
                }
            }
        }
        return builder.toString()
    }

    fun getMillis(string: String): Long {
        val matcher = pattern.matcher(string.lowercase().trim())
        if (!matcher.matches()) return tryAlternativeFormat(string)

        val years = matcher.group("y")?.toLong() ?: 0L
        val days = matcher.group("d")?.toLong() ?: 0L
        val hours = matcher.group("h")?.toLong() ?: 0L
        val minutes = matcher.group("m")?.toLong() ?: 0L
        val seconds = matcher.group("s")?.toLong() ?: 0L

        var millis = 0L
        millis += seconds * 1000
        millis += minutes * 60 * 1000
        millis += hours * 60 * 60 * 1000
        millis += days * 24 * 60 * 60 * 1000
        millis += (years * 365.25 * 24 * 60 * 60 * 1000).toLong()

        return millis
    }

    private fun tryAlternativeFormat(string: String): Long {
        val split = string.split(":")
        return when (split.size) {
            3 -> {
                val hours = split[0].toInt() * 1000 * 60 * 60
                val minutes = split[1].toInt() * 1000 * 60
                val seconds = split[2].toInt() * 1000
                seconds + minutes + hours
            }

            2 -> {
                val minutes = split[0].toInt() * 1000 * 60
                val seconds = split[1].toInt() * 1000
                seconds + minutes
            }

            1 -> {
                split[0].toInt() * 1000
            }

            else -> {
                throw RuntimeException("Invalid format: '$string'")
            }
        }.toLong()
    }
}

private const val FACTOR_SECONDS = 1000L
private const val FACTOR_MINUTES = FACTOR_SECONDS * 60
private const val FACTOR_HOURS = FACTOR_MINUTES * 60
private const val FACTOR_DAYS = FACTOR_HOURS * 24
private const val FACTOR_YEARS = (FACTOR_DAYS * 365.25).toLong()

enum class TimeUnit(val factor: Long, val shortName: String, val longName: String) {
    YEAR(FACTOR_YEARS, "y", "Year"),
    DAY(FACTOR_DAYS, "d", "Day"),
    HOUR(FACTOR_HOURS, "h", "Hour"),
    MINUTE(FACTOR_MINUTES, "m", "Minute"),
    SECOND(FACTOR_SECONDS, "s", "Second"),
    ;
}