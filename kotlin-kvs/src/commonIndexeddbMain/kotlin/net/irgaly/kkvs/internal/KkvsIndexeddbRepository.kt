package net.irgaly.kkvs.internal

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.irgaly.kkvs.KkvsEnvironment

class KkvsIndexeddbRepository(
    private val itemType: String,
    fileName: String,
    directoryPath: String,
    environment: KkvsEnvironment,
): KkvsRepository {
    override suspend fun exists(key: String): Boolean = withContext(Dispatchers.Default) {
        TODO()
    }

    override suspend fun delete(key: String) = withContext(Dispatchers.Default) {
        TODO()
    }
}



