package io.github.irgaly.kottage.data.sqlite.external

fun Database.prepareStatement(source: String, query: Boolean): Statement<Array<Any?>> {
    return prepare(source).unsafeCast<Statement<Array<Any?>>>().apply {
        safeIntegers()
        if (query) {
            raw(true)
        }
    }
}

fun Database.run(source: String): RunResult {
    return prepareStatement(source, false).run()
}
