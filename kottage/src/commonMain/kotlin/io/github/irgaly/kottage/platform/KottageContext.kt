package io.github.irgaly.kottage.platform

data class KottageContext internal constructor(val context: Context) {
    constructor() : this(Context())
}
