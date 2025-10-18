package live.mehiz.mpvkt.ui.home.presentation.screens.folderlist

import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.AddLink
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import live.mehiz.mpvkt.presentation.Screen
import live.mehiz.mpvkt.ui.home.presentation.components.EmptyState
import live.mehiz.mpvkt.ui.home.presentation.components.FolderCard
import live.mehiz.mpvkt.ui.home.presentation.components.PermissionDeniedState
import live.mehiz.mpvkt.ui.home.presentation.components.sort.SortDialog
import live.mehiz.mpvkt.ui.home.presentation.dialogs.PlayLinkDialog
import live.mehiz.mpvkt.ui.home.presentation.screens.videolist.VideoListScreen
import live.mehiz.mpvkt.ui.home.utils.MediaUtils
import live.mehiz.mpvkt.ui.home.utils.SortUtils
import live.mehiz.mpvkt.ui.preferences.PreferencesScreen
import live.mehiz.mpvkt.ui.utils.LocalBackStack

@Serializable
object FolderListScreen : Screen {

  @OptIn(
    ExperimentalPermissionsApi::class,
    ExperimentalMaterial3Api::class,
    androidx.compose.material.ExperimentalMaterialApi::class,
  )
  @Composable
  override fun Content() {
    val context = LocalContext.current
    val backstack = LocalBackStack.current
    val isRefreshing = remember { mutableStateOf(false) }
    val viewModel: FolderListViewModel =
      viewModel(factory = FolderListViewModel.factory(context.applicationContext as android.app.Application))
    val videoFolders by viewModel.videoFolders.collectAsState()
    val showLinkDialog = remember { mutableStateOf(false) }
    val sortDialogOpen = remember { mutableStateOf(false) }
    val sortType = remember { mutableStateOf("Date") }
    val sortOrderAsc = remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val pullRefreshState = rememberPullRefreshState(
      isRefreshing.value,
      {
        isRefreshing.value = true
        coroutineScope.launch {
          viewModel.refresh()
          isRefreshing.value = false
        }
      },
      refreshingOffset = 80.dp,
      refreshThreshold = 72.dp,
    )

    val permissionState = rememberPermissionState(
      if (Build.VERSION.SDK_INT >= 33) {
        android.Manifest.permission.READ_MEDIA_VIDEO
      } else {
        android.Manifest.permission.READ_EXTERNAL_STORAGE
      },
    )

    LaunchedEffect(permissionState.status) {
      if (permissionState.status == PermissionStatus.Granted) {
        viewModel.refresh()
      }
    }

    val sortedFolders = remember(videoFolders, sortType.value, sortOrderAsc.value) {
      SortUtils.sortFolders(videoFolders, sortType.value, sortOrderAsc.value)
    }

    Scaffold(
      topBar = {
        TopAppBar(
          title = {
            Text(
              "mpvKt",
              style = MaterialTheme.typography.headlineMedium,
              fontWeight = FontWeight.ExtraBold,
              color = MaterialTheme.colorScheme.primary,
              modifier = Modifier.padding(start = 8.dp),
            )
          },
          actions = {
            IconButton(
              onClick = { showLinkDialog.value = true },
              modifier = Modifier.padding(horizontal = 4.dp),
            ) {
              Icon(
                Icons.Filled.AddLink,
                contentDescription = "Add Link",
                modifier = Modifier.size(28.dp),
              )
            }
            IconButton(
              onClick = { sortDialogOpen.value = true },
              modifier = Modifier.padding(horizontal = 4.dp),
            ) {
              Icon(
                Icons.AutoMirrored.Filled.Sort,
                contentDescription = "Sort",
                modifier = Modifier.size(28.dp),
              )
            }
            IconButton(
              onClick = { backstack.add(PreferencesScreen) },
              modifier = Modifier.padding(horizontal = 4.dp),
            ) {
              Icon(
                Icons.Filled.Settings,
                contentDescription = "Preferences",
                modifier = Modifier.size(28.dp),
              )
            }
          },
        )
      },
    ) { padding ->
      when (permissionState.status) {
        PermissionStatus.Granted -> {
          Box(
            Modifier
              .fillMaxWidth()
              .pullRefresh(pullRefreshState)
              .padding(padding),
          ) {
            LazyColumn(
              modifier = Modifier.fillMaxWidth(),
              contentPadding = PaddingValues(8.dp),
            ) {
              items(sortedFolders.size) { index ->
                val folder = sortedFolders[index]
                FolderCard(
                  folder = folder,
                  onClick = {
                    backstack.add(VideoListScreen(folder.bucketId, folder.name))
                  },
                )
              }

              if (sortedFolders.isEmpty()) {
                item {
                  EmptyState(
                    icon = Icons.Filled.Folder,
                    title = "No video folders found",
                    message = "Add some video files to your device to see them here",
                  )
                }
              }
            }

            PullRefreshIndicator(
              isRefreshing.value,
              pullRefreshState,
              Modifier.align(Alignment.TopCenter),
              backgroundColor = MaterialTheme.colorScheme.surfaceContainer,
              contentColor = MaterialTheme.colorScheme.primary,
            )
          }
        }

        is PermissionStatus.Denied -> {
          PermissionDeniedState(
            onRequestPermission = { permissionState.launchPermissionRequest() },
            modifier = Modifier.padding(padding),
          )
        }
      }

      PlayLinkDialog(
        isOpen = showLinkDialog.value,
        onDismiss = { showLinkDialog.value = false },
        onPlayLink = { url -> MediaUtils.playFile(url, context) },
      )

      SortDialog(
        isOpen = sortDialogOpen.value,
        onDismiss = { sortDialogOpen.value = false },
        title = "Sort Folders",
        sortType = sortType.value,
        onSortTypeChange = { sortType.value = it },
        sortOrderAsc = sortOrderAsc.value,
        onSortOrderChange = { sortOrderAsc.value = it },
        types = listOf("Title", "Date", "Size"),
        icons = listOf(
          Icons.Filled.Title,
          Icons.Filled.CalendarToday,
          Icons.Filled.SwapVert,
        ),
        getLabelForType = { type, _ ->
          when (type) {
            "Title" -> Pair("A-Z", "Z-A")
            "Date" -> Pair("Oldest", "Newest")
            "Size" -> Pair("Smallest", "Largest")
            else -> Pair("Asc", "Desc")
          }
        },
      )
    }
  }
}
