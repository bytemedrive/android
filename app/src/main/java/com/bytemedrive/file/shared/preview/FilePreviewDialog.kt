package com.bytemedrive.file.shared.preview

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.media3.common.MimeTypes
import com.bytemedrive.file.root.DataFile
import com.bytemedrive.ui.component.Loader
import kotlinx.coroutines.flow.update
import org.koin.androidx.compose.koinViewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FilePreviewDialog(
    initialDataFile: DataFile,
    dataFileIds: List<UUID>,
    onClose: () -> Unit,
    filePreviewViewModel: FilePreviewViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val thumbnails by filePreviewViewModel.thumbnails.collectAsState()
    val loading by filePreviewViewModel.loading.collectAsState()
    val thumbnailIndex by filePreviewViewModel.thumbnailIndex.collectAsState()
    var currentPage by remember { mutableStateOf(0) }

    val name = thumbnails.getOrNull(currentPage)?.dataFile?.name.orEmpty()

    LaunchedEffect(Unit) {
        filePreviewViewModel.getThumbnails(initialDataFile.id, dataFileIds, context)
    }

    val goBack = {
        filePreviewViewModel.thumbnailIndex.update { null }
        filePreviewViewModel.thumbnails.update { emptyList() }
        onClose()
    }

    Dialog(
        onDismissRequest = goBack,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = goBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Cancel"
                        )
                    }

                    if (!loading) {
                        Text(
                            text = name,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 20.sp
                        )
                    }
                }
            },
        ) { paddingValues ->
            if (loading) {
                Loader(Modifier.fillMaxSize())
            } else if (thumbnails.isEmpty()) {
                Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Image could not be loaded. Try it again later")
                }
            } else {
                val pageState = rememberPagerState(initialPage = thumbnailIndex ?: 0)

                HorizontalPager(
                    pageCount = thumbnails.size,
                    state = pageState,
                    contentPadding = paddingValues
                ) { page ->
                    val thumbnail = thumbnails.getOrNull(page)

                    currentPage = page

                    thumbnail?.let { thumbnail_ ->
                        when (initialDataFile.contentType) {
                            MimeTypes.IMAGE_JPEG -> Image(
                                bitmap = thumbnail_.bitmap.asImageBitmap(),
                                modifier = Modifier.fillMaxSize(),
                                contentDescription = thumbnail_.dataFile.name
                            )
                        }
                    }
                }
            }
        }
    }
}