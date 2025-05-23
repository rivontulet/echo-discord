package dev.brahmkshatriya.echo.extension

import android.app.Application
import android.net.Uri
import dev.brahmkshatriya.echo.common.models.ImageHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient

class UriImageUploader(
    private val app: Application,
    client: OkHttpClient,
    json: Json
) : ImageUploader(client, json) {

    override suspend fun getImageUrl(image: ImageHolder): String? {
        val byteArray = when (image) {
            is ImageHolder.ResourceImageHolder -> withContext(Dispatchers.Main) {
                runCatching { app.resources.openRawResource(image.resId).readBytes() }
            }.getOrNull()

            is ImageHolder.UriImageHolder -> withContext(Dispatchers.Main) {
                runCatching {
                    app.contentResolver.openInputStream(Uri.parse(image.uri))!!.readBytes()
                }
            }.getOrNull()

            is ImageHolder.UrlRequestImageHolder -> return super.getImageUrl(image)
        }
        return byteArray?.let { uploadImage(it) }
    }
}