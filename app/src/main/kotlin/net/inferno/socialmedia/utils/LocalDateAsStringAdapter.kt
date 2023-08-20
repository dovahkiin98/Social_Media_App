package net.inferno.socialmedia.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonQualifier
import com.squareup.moshi.ToJson
import net.inferno.socialmedia.R
import java.time.LocalDateTime
import java.time.OffsetTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.Temporal

@Retention(AnnotationRetention.RUNTIME)
@JsonQualifier
internal annotation class DateString

class LocalDateAsStringAdapter {
    companion object {
        const val DATE_FORMAT_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    }

    @ToJson
    fun toJson(@DateString time: LocalDateTime): String {
        return DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN).format(time)
    }

    @FromJson
    @DateString
    fun fromJson(time: String): LocalDateTime = LocalDateTime.from(
        DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN).parse(time)
    ).plusSeconds(ZonedDateTime.now().offset.totalSeconds.toLong())
}

@Composable
fun LocalDateTime.toReadableText(): String {
    val today = LocalDateTime.now()

    val years = ChronoUnit.YEARS.between(this, today)
    val months = ChronoUnit.MONTHS.between(this, today)
    val days = ChronoUnit.DAYS.between(this, today)
    val hours = ChronoUnit.HOURS.between(this, today)
    val minutes = ChronoUnit.MINUTES.between(this, today)

    return if (years > 0) {
        pluralStringResource(
            id = R.plurals.years_ago,
            count = years.toInt(),
            years.toInt(),
        )
    } else if (months > 0) {
        pluralStringResource(
            id = R.plurals.months_ago,
            count = months.toInt(),
            months.toInt(),
        )
    } else if (days > 0) {
        pluralStringResource(
            id = R.plurals.days_ago,
            count = days.toInt(),
            days.toInt(),
        )
    } else if (hours > 0) {
        pluralStringResource(
            id = R.plurals.hours_ago,
            count = hours.toInt(),
            hours.toInt(),
        )
    } else {
        return if (minutes > 5) {
            pluralStringResource(
                id = R.plurals.minutes_ago,
                count = minutes.toInt(),
                minutes.toInt()
            )
        } else {
            stringResource(id = R.string.moments_ago)
        }
    }
}