package io.github.irgaly.kottage.platform
import kotlinx.cinterop.*
import platform.windows.*

actual class Id {
    actual companion object {
        actual fun generateUuidV4(): String {
            return generateUuidV4Internal()
        }

        actual fun generateUuidV4Short(): String {
            return generateUuidV4Internal().replace("-", "")
        }

        private fun generateUuidV4Internal(): String {
            return memScoped {
                val uuidPtr = alloc<UUID>()
                val rpcString = alloc<RPC_WSTRVar>()
                UuidCreateSequential(uuidPtr.ptr)
                UuidToStringW(uuidPtr.ptr, rpcString.ptr)
                try {
                    rpcString.value!!.toKString()
                } finally {
                    RpcStringFreeW(rpcString.ptr)
                }
            }
        }
    }
}
