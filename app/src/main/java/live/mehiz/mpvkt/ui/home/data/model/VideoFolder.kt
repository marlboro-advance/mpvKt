package live.mehiz.mpvkt.ui.home.data.model

data class VideoFolder(
  val bucketId: String,
  val name: String,
  val path: String,
  val videoCount: Int,
  val lastModified: Long = 0L,
)
