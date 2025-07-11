package org.publicvalue.multiplatform.oauth.strings

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toJavaLocalTime
import kotlinx.datetime.toJavaZoneId
import kotlinx.datetime.toLocalDateTime
import me.tatarka.inject.annotations.Inject
import org.publicvalue.multiplatform.oauth.inject.ActivityScope
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.Temporal
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.time.toJavaInstant
import java.time.LocalDateTime as JavaLocalDateTime

@ActivityScope
@Inject
@OptIn(ExperimentalTime::class)
actual class DateFormatter(
    private val locale: Locale = Locale.getDefault(),
    internal val timeZone: TimeZone = TimeZone.currentSystemDefault(),
) {
    private val shortDateFormatter: DateTimeFormatter by lazy {
        DateTimeFormatter
            .ofLocalizedDate(FormatStyle.SHORT)
            .withLocale(locale)
            .withZone(timeZone.toJavaZoneId())
    }
    private val shortTimeFormatter: DateTimeFormatter by lazy {
        DateTimeFormatter
            .ofLocalizedTime(FormatStyle.SHORT)
            .withLocale(locale)
            .withZone(timeZone.toJavaZoneId())
    }
    private val mediumDateFormatter: DateTimeFormatter by lazy {
        DateTimeFormatter
            .ofLocalizedDate(FormatStyle.MEDIUM)
            .withLocale(locale)
            .withZone(timeZone.toJavaZoneId())
    }
    private val mediumDateTimeFormatter: DateTimeFormatter by lazy {
        DateTimeFormatter
            .ofLocalizedDateTime(FormatStyle.MEDIUM)
            .withLocale(locale)
            .withZone(timeZone.toJavaZoneId())
    }
    private val dayOfWeekFormatter: DateTimeFormatter by lazy {
        DateTimeFormatter.ofPattern("EEEE")
            .withLocale(locale)
            .withZone(timeZone.toJavaZoneId())
    }

    private fun Instant.toTemporal(): Temporal {
        return JavaLocalDateTime.ofInstant(toJavaInstant(), timeZone.toJavaZoneId())
    }

    actual fun formatShortDate(instant: Instant): String {
        return shortDateFormatter.format(instant.toTemporal())
    }

    actual fun formatShortDate(date: LocalDate): String {
        return shortDateFormatter.format(date.toJavaLocalDate())
    }

    actual fun formatMediumDate(instant: Instant): String {
        return mediumDateFormatter.format(instant.toTemporal())
    }

    actual fun formatMediumDateTime(instant: Instant): String {
        return mediumDateTimeFormatter.format(instant.toTemporal())
    }

    actual fun formatShortTime(localTime: LocalTime): String {
        return shortTimeFormatter.format(localTime.toJavaLocalTime())
    }

    actual fun formatShortRelativeTime(date: Instant, reference: Instant): String {
        return formatShortDate(date)
    }

    actual fun formatDayOfWeek(dayOfWeek: DayOfWeek): String {
        return Clock.System.now()
            .toLocalDateTime(timeZone)
            .toJavaLocalDateTime()
            .with(TemporalAdjusters.nextOrSame(dayOfWeek.toJavaDayOfWeek()))
            .let { dayOfWeekFormatter.format(it) }
    }
}

private fun DayOfWeek.toJavaDayOfWeek(): java.time.DayOfWeek = when (this) {
    DayOfWeek.MONDAY -> java.time.DayOfWeek.MONDAY
    DayOfWeek.TUESDAY -> java.time.DayOfWeek.TUESDAY
    DayOfWeek.WEDNESDAY -> java.time.DayOfWeek.WEDNESDAY
    DayOfWeek.THURSDAY -> java.time.DayOfWeek.THURSDAY
    DayOfWeek.FRIDAY -> java.time.DayOfWeek.FRIDAY
    DayOfWeek.SATURDAY -> java.time.DayOfWeek.SATURDAY
    DayOfWeek.SUNDAY -> java.time.DayOfWeek.SUNDAY
}
