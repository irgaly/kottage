package io.github.irgaly.kottage.platform
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toKString
import kotlinx.cinterop.value
import platform.windows.RPC_WSTRVar
import platform.windows.RpcStringFreeW
import platform.windows.UUID
import platform.windows.UuidCreateSequential
import platform.windows.UuidToStringW

@OptIn(ExperimentalForeignApi::class)
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
