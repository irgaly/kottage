package io.github.irgaly.kottage.platform

fun contextOf(
    context: android.content.Context
): KottageContext {
    return KottageContext(Context(context))
}
