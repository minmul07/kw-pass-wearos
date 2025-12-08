package minmul.kwpass.service.complication

import android.graphics.drawable.Icon
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.MonochromaticImageComplicationData
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.SmallImage
import androidx.wear.watchface.complications.data.SmallImageComplicationData
import androidx.wear.watchface.complications.data.SmallImageType
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import minmul.kwpass.R

class IconComplicationService : TextComplicationService() {
    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        val icon = Icon.createWithResource(this, R.drawable.qr_icon)
        val complicationDesc = PlainComplicationText.Builder("KW Pass").build()

        val tapAction = createMainActivityPendingIntent()
        return when (request.complicationType) {
            ComplicationType.SMALL_IMAGE -> {
                SmallImageComplicationData.Builder(
                    smallImage = SmallImage.Builder(icon, SmallImageType.ICON).build(),
                    contentDescription = complicationDesc
                )
                    .setTapAction(tapAction)
                    .build()
            }

            ComplicationType.MONOCHROMATIC_IMAGE -> {
                MonochromaticImageComplicationData.Builder(
                    monochromaticImage = MonochromaticImage.Builder(icon).build(),
                    contentDescription = complicationDesc
                )
                    .setTapAction(tapAction)
                    .build()
            }

            else -> super.onComplicationRequest(request)
        }
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        val previewIcon = Icon.createWithResource(this, R.drawable.qr_icon)
        val previewDesc = PlainComplicationText.Builder("KW Pass (아이콘만)").build()

        return when (type) {
            ComplicationType.SMALL_IMAGE -> {
                SmallImageComplicationData.Builder(
                    smallImage = SmallImage.Builder(previewIcon, SmallImageType.ICON).build(),
                    contentDescription = previewDesc
                ).build()
            }

            ComplicationType.MONOCHROMATIC_IMAGE -> {
                MonochromaticImageComplicationData.Builder(
                    monochromaticImage = MonochromaticImage.Builder(previewIcon).build(),
                    contentDescription = previewDesc
                ).build()
            }

            else -> super.getPreviewData(type)
        }
    }
}