package io.github.irgaly.kottage.platform

expect class Id {
    companion object {
        /**
         * UUID v4 文字列を生成する
         */
        fun generateUuidV4(): String

        /**
         * ハイフンを削除した UUID v4 を生成する
         */
        fun generateUuidV4Short(): String
    }
}
