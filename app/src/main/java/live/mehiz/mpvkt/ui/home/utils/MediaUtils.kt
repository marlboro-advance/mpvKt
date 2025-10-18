package live.mehiz.mpvkt.ui.home.utils

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import `is`.xyz.mpv.Utils
import live.mehiz.mpvkt.ui.player.PlayerActivity

object MediaUtils {

  fun playFile(filepath: String, context: Context) {
    val intent = Intent(Intent.ACTION_VIEW, filepath.toUri())
    intent.setClass(context, PlayerActivity::class.java)
    context.startActivity(intent)
  }

  fun isURLValid(url: String): Boolean {
    val uri = url.toUri()

    val isValidStructure = uri.isHierarchical &&
      !uri.isRelative &&
      (!uri.host.isNullOrBlank() || !uri.path.isNullOrBlank())

    val hasValidProtocol = Utils.PROTOCOLS.contains(uri.scheme)

    return isValidStructure && hasValidProtocol
  }
}
