package minmul.kwpass

import android.app.Application
import com.google.android.gms.wearable.Wearable
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class KWPassWearApplication : Application() {
    val capabilityClient by lazy { Wearable.getCapabilityClient(this) }
}