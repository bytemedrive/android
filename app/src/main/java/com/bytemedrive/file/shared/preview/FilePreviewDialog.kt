package com.bytemedrive.file.shared.preview

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.media3.common.MimeTypes
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import com.bytemedrive.ui.component.Loader
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FilePreviewDialog(
    filePreview: FilePreview,
    onClose: () -> Unit,
    filePreviewViewModel: FilePreviewViewModel = koinViewModel()
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(filePreview.initialDataFileLink.name) }

    LaunchedEffect(Unit) {
        filePreviewViewModel.initialize(filePreview, context)
    }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Cancel"
                        )
                    }

                    if (!filePreviewViewModel.loading) {
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
            if (filePreviewViewModel.loading) {
                Loader(Modifier.fillMaxSize())
            } else if (filePreviewViewModel.thumbnails.isEmpty()) {
                Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Image could not be loaded. Try it again later")
                }
            } else {
                val pagerState = rememberPagerState(initialPage = filePreviewViewModel.thumbnailIndex)

                LaunchedEffect(pagerState) {
                    snapshotFlow { pagerState.currentPage }.collect { page ->
                        name = filePreviewViewModel.thumbnails.getOrNull(page)?.name.orEmpty()
                    }
                }

                HorizontalPager(
                    pageCount = filePreviewViewModel.thumbnails.size,
                    state = pagerState,
                    contentPadding = paddingValues,
                    beyondBoundsPageCount = 1
                ) { page ->
                    val thumbnail = filePreviewViewModel.thumbnails.getOrNull(page)

                    thumbnail?.let { thumbnail ->
                        when (thumbnail.contentType) {
                            MimeTypes.IMAGE_JPEG -> Image(
                                modifier = Modifier.fillMaxSize(),
                                painter = rememberAsyncImagePainter(
                                    ImageRequest
                                        .Builder(LocalContext.current)
                                        .data(thumbnail.file)
                                        .size(Size.ORIGINAL)
                                        .crossfade(true)
                                        .build()
                                ),
                                contentDescription = thumbnail.name,
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }
        }
    }
}