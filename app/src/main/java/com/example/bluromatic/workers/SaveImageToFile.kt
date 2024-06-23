package com.example.bluromatic.workers

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.bluromatic.DELAY_TIME_MILLIS
import com.example.bluromatic.KEY_IMAGE_URI
import com.example.bluromatic.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.lang.String.format
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


private const val TAG = "SaveImageToFileWorker"

class SaveImageToFile(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {
    private val title = "Blurred Image"
    private val dateFormatter = SimpleDateFormat(
        "yyyy.MM.dd 'at' HH:mm:ss z",
        Locale.getDefault()
    )

    override suspend fun doWork(): Result {

        makeStatusNotification(
            message = applicationContext.resources.getString(R.string.saving_image),
            context = applicationContext
        )

        return withContext(Dispatchers.IO) {
            delay(DELAY_TIME_MILLIS)

            val resolver = applicationContext.contentResolver
            return@withContext try {
                val resourceUri = inputData.getString(KEY_IMAGE_URI)
                val bitmap = BitmapFactory.decodeStream(
                    resolver.openInputStream((Uri.parse(resourceUri)))
                )
                val imageUrl = MediaStore.Images.Media.insertImage(
                    resolver, bitmap, title, dateFormatter.format(Date())
                )
                if (!imageUrl.isNullOrBlank()) {
                    val output = workDataOf(KEY_IMAGE_URI to imageUrl)
                    Result.success(output)
                } else {
                    Log.e(
                        TAG,
                        applicationContext.resources.getString(R.string.writing_to_mediaStore_failed)
                    )
                    Result.failure()
                }
            } catch (exception: Exception) {
                Log.e(
                    TAG,
                    applicationContext.resources.getString(R.string.error_saving_image),
                    exception
                )
                Result.failure()
            }
        }
    }

}