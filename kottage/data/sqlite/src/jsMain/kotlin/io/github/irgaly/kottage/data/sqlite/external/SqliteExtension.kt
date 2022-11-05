package io.github.irgaly.kottage.data.sqlite.external

fun Database.prepareStatement(source: String): Statement<Array<Any?>> {
    return prepare(source).unsafeCast<Statement<Array<Any?>>>().apply {
        raw(true)
    }
}

fun Database.run(source: String): RunResult {
    return prepareStatement(source).run()
}
