package minmul.kwpass.service

import android.content.Context
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.Wearable
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Date
import javax.inject.Inject

class ClientDataViewModel @Inject constructor(@ApplicationContext private val context: Context) {
    val dataClientModule: DataClient = Wearable.getDataClient(context)
}

data class Data(val qr: String, val time: Date)