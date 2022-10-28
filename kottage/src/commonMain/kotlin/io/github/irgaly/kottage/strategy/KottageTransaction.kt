package io.github.irgaly.kottage.strategy

import io.github.irgaly.kottage.internal.database.Transaction
import kotlin.jvm.JvmInline

/**
 * Kottage Transaction
 */
@JvmInline
value class KottageTransaction internal constructor(
    // internal class の Transaction を外部に露出するために value class で包んでいる
    internal val transaction: Transaction
)
