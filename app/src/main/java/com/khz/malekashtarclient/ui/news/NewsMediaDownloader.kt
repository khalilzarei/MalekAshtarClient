package com.khz.malekashtarclient.ui.news

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import com.khz.malekashtarclient.domain.model.NewsMedia

object NewsMediaDownloader {

    sealed class Result {
        object Started : Result()
        data class Failed(val message: String) : Result()
    }

    fun download(
        context: Context,
        media: NewsMedia,
        token: String?
    ): Result {
        return try {
            val url = media.downloadUrl
                    ?: media.streamUrl
                    ?: return Result.Failed("آدرس دانلود موجود نیست")

            val fileName = media.originalName?.takeIf { it.isNotBlank() }
                    ?: media.fileName?.takeIf { it.isNotBlank() }
                    ?: "news_${media.id}"

            val request = DownloadManager.Request(Uri.parse(url))
                .apply {
                    setTitle(fileName)
                    setDescription("دانلود از اخبار")
                    setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    setDestinationInExternalPublicDir(
                        Environment.DIRECTORY_DOWNLOADS,
                        fileName
                    )
                    setAllowedOverMetered(true)
                    setAllowedOverRoaming(true)
                    if (!token.isNullOrBlank()) {
                        addRequestHeader(
                            "Authorization",
                            "Bearer $token"
                        )
                    }
                }

            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            dm.enqueue(request)

            Result.Started
        } catch (e: Exception) {
            Result.Failed(
                e.message
                        ?: "خطا در شروع دانلود"
            )
        }
    }
}
