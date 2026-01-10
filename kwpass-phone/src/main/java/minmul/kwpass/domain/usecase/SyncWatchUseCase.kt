package minmul.kwpass.domain.usecase

import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.PutDataMapRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

class SyncWatchUseCase @Inject constructor(
    private val dataClient: DataClient,
    private val messageClient: MessageClient,
) {
    suspend fun sendAccountData(rid: String, password: String, tel: String) {
        try {
            val request = PutDataMapRequest.create("/account").apply {
                dataMap.putString("rid", rid)
                dataMap.putString("password", password)
                dataMap.putString("tel", tel)
                dataMap.putLong("timestamp", System.currentTimeMillis())
            }.asPutDataRequest().setUrgent()

            dataClient.putDataItem(request).await()

            Timber.d("계정 정보 전송 성공. rid=$rid")
        } catch (e: Exception) {
            Timber.tag("계정 정보 전송 실패").e(e)
        }
    }

    fun setSyncListener(): Flow<Unit> = callbackFlow {
        val listener = MessageClient.OnMessageReceivedListener { messageEvent ->
            if (messageEvent.path == "/refresh") {
                trySend(Unit)
            }
        }
        messageClient.addListener(listener)
        awaitClose { messageClient.removeListener(listener) }
    }
}