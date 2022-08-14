package net.irgaly.kkvs.internal

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.irgaly.kkvs.KkvsEnvironment
import net.irgaly.kkvs.internal.model.Item

class KkvsIndexeddbRepository(
    private val itemType: String,
    fileName: String,
    directoryPath: String,
    environment: KkvsEnvironment,
): KkvsRepository {
    override suspend fun upsert(item: Item) {
        TODO("Not yet implemented")
    }

    override suspend fun updateLastRead(key: String, lastReadAt: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun updateExpireAt(key: String, expireAt: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun exists(key: String): Boolean = withContext(Dispatchers.Default) {
        TODO("Not yet implemented")
    }

    override suspend fun delete(key: String) = withContext(Dispatchers.Default) {
        TODO("Not yet implemented")
    }
}



