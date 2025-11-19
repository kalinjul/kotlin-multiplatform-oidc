package org.publicvalue.multiplatform.oidc.appsupport

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import org.publicvalue.multiplatform.oidc.preferences.PREFERENCES_FILENAME

internal val Context.dataStore by preferencesDataStore(
    name = PREFERENCES_FILENAME.replace("preferences_pb", "")
)