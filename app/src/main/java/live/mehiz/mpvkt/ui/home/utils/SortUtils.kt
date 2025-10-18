package live.mehiz.mpvkt.ui.home.utils

import live.mehiz.mpvkt.ui.home.data.model.Video
import live.mehiz.mpvkt.ui.home.data.model.VideoFolder

object SortUtils {

  fun sortVideos(
    videos: List<Video>,
    sortType: String,
    sortOrderAsc: Boolean,
  ): List<Video> {
    val sorted = when (sortType) {
      "Title" -> videos.sortedBy { it.displayName }
      "Duration" -> videos.sortedBy { it.duration }
      "Date" -> videos.sortedBy { it.dateAdded }
      "Size" -> videos.sortedBy { parseSizeToBytes(it.sizeFormatted) }
      else -> videos
    }
    return if (sortOrderAsc) sorted else sorted.reversed()
  }

  fun sortFolders(
    folders: List<VideoFolder>,
    sortType: String,
    sortOrderAsc: Boolean,
  ): List<VideoFolder> {
    val sorted = when (sortType) {
      "Title" -> folders.sortedBy { it.name }
      "Date" -> folders.sortedBy { it.lastModified }
      "Size" -> folders.sortedBy { it.videoCount }
      else -> folders
    }
    return if (sortOrderAsc) sorted else sorted.reversed()
  }

  fun parseSizeToBytes(sizeFormatted: String): Long {
    val parts = sizeFormatted.split(" ")
    val value = if (parts.size == 2) parts[0].toDoubleOrNull() else null

    if (value == null) {
      return 0
    }

    val multiplier = when (parts[1]) {
      "B" -> 1.0
      "KB" -> 1024.0
      "MB" -> 1024.0 * 1024.0
      "GB" -> 1024.0 * 1024.0 * 1024.0
      "TB" -> 1024.0 * 1024.0 * 1024.0 * 1024.0
      else -> 0.0
    }

    return (value * multiplier).toLong()
  }
}
