package minmul.kwpass.service.complication

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Icon
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.MonochromaticImageComplicationData
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.data.SmallImage
import androidx.wear.watchface.complications.data.SmallImageComplicationData
import androidx.wear.watchface.complications.data.SmallImageType
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import minmul.kwpass.R
import minmul.kwpass.main.MainActivity
import timber.log.Timber

open class TextComplicationService : SuspendingComplicationDataSourceService() {
    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        Timber.d("onComplicationRequest: id ${request.complicationInstanceId}")
        val tapAction = createMainActivityPendingIntent()

        val complicationText = PlainComplicationText.Builder("KW Pass").build()
        val complicationDesc = PlainComplicationText.Builder("KW Pass").build()
        val icon = Icon.createWithResource(this, R.drawable.qr_icon)

        return when (request.complicationType) {
            ComplicationType.SHORT_TEXT -> {
                ShortTextComplicationData.Builder(
                    text = complicationText,
                    contentDescription = complicationDesc
                )
                    .setMonochromaticImage(MonochromaticImage.Builder(icon).build())
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

            ComplicationType.SMALL_IMAGE -> {
                SmallImageComplicationData.Builder(
                    smallImage = SmallImage.Builder(icon, SmallImageType.ICON).build(),
                    contentDescription = complicationDesc
                )
                    .setTapAction(tapAction)
                    .build()
            }

            else -> null
        }
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        val previewIcon = Icon.createWithResource(this, R.drawable.qr_icon)
        val previewText = PlainComplicationText.Builder("KW Pass QR").build()
        val previewDesc = PlainComplicationText.Builder("KW Pass QR").build()

        return when (type) {
            ComplicationType.SHORT_TEXT -> {
                ShortTextComplicationData.Builder(
                    text = previewText,
                    contentDescription = previewDesc
                )
                    .setMonochromaticImage(
                        MonochromaticImage.Builder(previewIcon).build()
                    )
                    .build()
            }

            ComplicationType.MONOCHROMATIC_IMAGE -> {
                MonochromaticImageComplicationData.Builder(
                    monochromaticImage = MonochromaticImage.Builder(previewIcon).build(),
                    contentDescription = previewDesc
                )
                    .build()
            }

            ComplicationType.SMALL_IMAGE -> {
                SmallImageComplicationData.Builder(
                    smallImage = SmallImage.Builder(
                        previewIcon,
                        SmallImageType.ICON
                    ).build(),
                    contentDescription = previewDesc
                )
                    .build()
            }


            else -> null
        }
    }

    @SuppressLint("WearRecents")
    protected fun createMainActivityPendingIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        return PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}