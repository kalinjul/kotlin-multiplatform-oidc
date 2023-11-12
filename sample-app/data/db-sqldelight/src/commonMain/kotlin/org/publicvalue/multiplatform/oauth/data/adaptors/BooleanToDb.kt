package org.publicvalue.multiplatform.oauth.data.adaptors

internal fun Boolean.toDb(): Long = if (this) 1 else 0
