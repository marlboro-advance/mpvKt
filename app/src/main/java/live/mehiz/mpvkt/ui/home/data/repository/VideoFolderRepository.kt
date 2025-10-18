package live.mehiz.mpvkt.ui.home.data.repository

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import android.util.Log
import live.mehiz.mpvkt.ui.home.data.model.VideoFolder
import java.io.File

object VideoFolderRepository {

  private const val tag = "VideoFolderRepository"

  fun getVideoFolders(context: Context): List<VideoFolder> {
    Log.d(tag, "Starting video folder scan")
    val folders = mutableMapOf<String, VideoFolderInfo>()

    val projection = getProjection()
    val cursor: Cursor? = context.contentResolver.query(
      MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
      projection,
      null,
      null,
      "${MediaStore.Video.Media.DATE_MODIFIED} DESC",
    )

    cursor?.use { c ->
      processFolderCursor(c, folders)
    }

    Log.d(tag, "Finished video folder scan")
    val result = convertToVideoFolderList(folders)
    Log.d(tag, "Found ${result.size} folders")
    return result
  }

  private fun getProjection(): Array<String> {
    return arrayOf(
      MediaStore.Video.Media.BUCKET_ID,
      MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
      MediaStore.Video.Media.DATA,
      MediaStore.Video.Media.DATE_MODIFIED,
    )
  }

  private fun processFolderCursor(
    cursor: Cursor,
    folders: MutableMap<String, VideoFolderInfo>,
  ) {
    val bucketIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_ID)
    val bucketNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
    val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
    val dateModifiedColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED)

    while (cursor.moveToNext()) {
      val bucketId = cursor.getString(bucketIdColumn)
      val bucketName = cursor.getString(bucketNameColumn)
      val filePath = cursor.getString(dataColumn)
      val dateModified = cursor.getLong(dateModifiedColumn)

      processVideoFile(filePath, bucketId, bucketName, dateModified, folders)
    }
  }

  private fun processVideoFile(
    filePath: String?,
    bucketId: String?,
    bucketName: String?,
    dateModified: Long,
    folders: MutableMap<String, VideoFolderInfo>,
  ) {
    if (filePath == null) return

    val folderPath = File(filePath).parent ?: return
    val (finalBucketId, finalBucketName) = getFinalBucketInfo(bucketId, bucketName, folderPath)

    Log.d(tag, "Found video: $filePath, bucketId: $finalBucketId, bucketName: $finalBucketName")

    updateFolderInfo(finalBucketId, finalBucketName, folderPath, dateModified, folders)
  }

  private fun getFinalBucketInfo(
    bucketId: String?,
    bucketName: String?,
    folderPath: String,
  ): Pair<String, String> {
    val finalBucketId = bucketId ?: "root_storage"
    val finalBucketName = when {
      !bucketName.isNullOrBlank() -> bucketName
      folderPath.contains("/storage/emulated/0") && folderPath == "/storage/emulated/0" -> {
        "Internal Storage"
      }
      folderPath.contains("/storage/emulated/0") -> File(folderPath).name
      else -> "Unknown Folder"
    }
    return Pair(finalBucketId, finalBucketName)
  }

  private fun updateFolderInfo(
    bucketId: String,
    bucketName: String,
    folderPath: String,
    dateModified: Long,
    folders: MutableMap<String, VideoFolderInfo>,
  ) {
    val folderInfo = folders[bucketId] ?: VideoFolderInfo(
      bucketId = bucketId,
      name = bucketName,
      path = folderPath,
      videoCount = 0,
      lastModified = 0L,
    )

    folders[bucketId] = folderInfo.copy(
      videoCount = folderInfo.videoCount + 1,
      lastModified = maxOf(folderInfo.lastModified, dateModified),
    )
  }

  private fun convertToVideoFolderList(
    folders: Map<String, VideoFolderInfo>,
  ): List<VideoFolder> {
    return folders.values
      .map { info ->
        VideoFolder(
          bucketId = info.bucketId,
          name = info.name,
          path = info.path,
          videoCount = info.videoCount,
          lastModified = info.lastModified,
        )
      }
      .sortedByDescending { it.lastModified }
  }

  private data class VideoFolderInfo(
    val bucketId: String,
    val name: String,
    val path: String,
    val videoCount: Int,
    val lastModified: Long,
  )
}
