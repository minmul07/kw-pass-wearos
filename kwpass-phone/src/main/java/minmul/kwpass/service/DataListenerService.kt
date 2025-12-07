package minmul.kwpass.service

import android.util.Log
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.WearableListenerService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import minmul.kwpass.shared.UserData
import javax.inject.Inject

@AndroidEntryPoint
class DataListenerService : WearableListenerService() {
    @Inject
    lateinit var userData: UserData

    @Inject
    lateinit var dataClient: DataClient

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)

        if (messageEvent.path == "/refresh") {
            scope.launch {
                val (rid, password, tel) = userData.userFlow.first()

                if (rid.isNotEmpty()) {
                    // 2. 워치로 데이터 전송
                    try {
                        val request = PutDataMapRequest.create("/account").apply {
                            dataMap.putString("rid", rid)
                            dataMap.putString("password", password)
                            dataMap.putString("tel", tel)
                            dataMap.putLong("timestamp", System.currentTimeMillis())
                        }.asPutDataRequest().setUrgent()

                        dataClient.putDataItem(request).await()
                        Log.d("PhoneService", "백그라운드 데이터 전송 완료")
                    } catch (e: Exception) {
                        Log.e("PhoneService", "백그라운드 데이터 전송 실패", e)
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}

