package org.publicvalue.multiplatform.oauth.data.adaptors

import app.cash.sqldelight.ColumnAdapter
import java.time.Instant
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal object InstantStringColumnAdapter : ColumnAdapter<Instant, String> {
    override fun decode(databaseValue: String): Instant = Instant.parse(databaseValue)
    override fun encode(value: Instant): String = value.toString()
}
