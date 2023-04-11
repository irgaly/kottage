package io.github.irgaly.test.platform

import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.toKString
import kotlinx.cinterop.value
import platform.posix.FTW_DEPTH
import platform.posix.FTW_PHYS
import platform.posix.nftw
import platform.posix.remove
import platform.windows.CreateDirectoryW
import platform.windows.GetTempPathW
import platform.windows.MAX_PATH
import platform.windows.RPC_WSTRVar
import platform.windows.RemoveDirectoryW
import platform.windows.RpcStringFreeW
import platform.windows.TCHARVar
import platform.windows.TRUE
import platform.windows.UUID
import platform.windows.UuidCreate
import platform.windows.UuidToStringW

actual class Files {
    actual companion object {
        actual fun createTemporaryDirectory(): String {
            return memScoped {
                val tempPathBuffer = allocArray<TCHARVar>(MAX_PATH)
                val uuid = alloc<UUID>()
                val rpcString = alloc<RPC_WSTRVar>()
                val result = GetTempPathW(MAX_PATH, tempPathBuffer)
                if (result == 0U || MAX_PATH.toUInt() < result) {
                    error("GetTempPathW error")
                }
                val tempPath = tempPathBuffer.toKString()
                UuidCreate(Uuid = uuid.ptr)
                UuidToStringW(Uuid = uuid.ptr, StringUuid = rpcString.ptr)
                val uuidString = try {
                    rpcString.value!!.toKString()
                } finally {
                    RpcStringFreeW(String = rpcString.ptr)
                }
                val directory = "$tempPath$uuidString"
                CreateDirectoryW(
                    lpPathName = directory,
                    lpSecurityAttributes = null
                )
                directory
            }
        }


        actual fun deleteRecursively(directoryPath: String): Boolean {
            val result = nftw(
                /* __dir = */ directoryPath,
                /* __func = */ staticCFunction { pathName, _, _, _ ->
                    val pathString = pathName!!.toKString()
                    val deleted = RemoveDirectoryW(
                        lpPathName = pathString
                    )
                    if (deleted == TRUE) {
                        TRUE
                    } else {
                        remove(pathString)
                    }
                },
                /* __descriptors = */ 64,
                /* __flag = */ FTW_DEPTH or FTW_PHYS
            )
            return (result != -1)
        }
    }
}
