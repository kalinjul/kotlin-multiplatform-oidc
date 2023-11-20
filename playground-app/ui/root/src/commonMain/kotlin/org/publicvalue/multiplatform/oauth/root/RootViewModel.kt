package org.publicvalue.multiplatform.oauth.root

import kotlinx.coroutines.CoroutineScope
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@Inject
class RootViewModel(
    @Assisted private val coroutineScope: CoroutineScope,
) {
    init {
    }
}