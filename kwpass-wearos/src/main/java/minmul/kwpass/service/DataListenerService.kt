package minmul.kwpass.service

import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import minmul.kwpass.shared.UserData
import javax.inject.Inject

@AndroidEntryPoint
class DataListenerService : WearableListenerService() {
    @Inject
    lateinit var userData: UserData
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        super.onDataChanged(dataEvents)

        dataEvents.forEach { event ->
            if (event.type == DataEvent.TYPE_CHANGED) {
                val path = event.dataItem.uri.path
                if (path == "/account") {
                    val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                    val rid = dataMap.getString("rid") ?: ""
                    val password = dataMap.getString("password") ?: ""
                    val tel = dataMap.getString("tel") ?: ""

                    scope.launch {
                        userData.saveUserCredentials(rid, password, tel)
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