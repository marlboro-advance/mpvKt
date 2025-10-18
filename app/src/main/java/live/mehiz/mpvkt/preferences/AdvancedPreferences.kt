package live.mehiz.mpvkt.preferences

import live.mehiz.mpvkt.BuildConfig
import live.mehiz.mpvkt.preferences.preference.PreferenceStore

class AdvancedPreferences(preferenceStore: PreferenceStore) {
  val mpvConfStorageUri = preferenceStore.getString("mpv_conf_storage_location_uri")
  val mpvConf = preferenceStore.getString("mpv.conf")
  val inputConf = preferenceStore.getString("input.conf")

  val verboseLogging = preferenceStore.getBoolean("verbose_logging", BuildConfig.BUILD_TYPE != "release")

  val enabledStatisticsPage = preferenceStore.getInt("enabled_stats_page", 0)
}
