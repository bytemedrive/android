package com.bytemedrive.file.shared.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import com.bytemedrive.datafile.entity.UploadStatus
import com.bytemedrive.file.shared.entity.FileListItem
import com.bytemedrive.file.shared.entity.ItemType
import java.io.File

@Composable
fun ItemImage(itemSelected: Boolean, item: FileListItem, file: File?) {
    Box(
        modifier = Modifier.size(50.dp), contentAlignment = Alignment.Center
    ) {
        when {
            itemSelected -> {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = "Checked",
                    tint = Color.Black,
                )
            }

            listOf(UploadStatus.STARTED, UploadStatus.QUEUED).contains(item.uploadStatus) -> CircularProgressIndicator()

            item.uploadStatus == UploadStatus.FAILED -> {
                Icon(
                    imageVector = Icons.Outlined.ErrorOutline,
                    contentDescription = "Checked",
                    tint = Color.Red,
                )
            }

            item.type == ItemType.FILE -> {
                file?.let {
                    AsyncImage(
                        model = it,
                        modifier = Modifier.fillMaxWidth().wrapContentHeight(),
//                        painter = rememberAsyncImagePainter(
//                            ImageRequest
//                            .Builder(LocalContext.current)
//                            .data(File(it.path))
//                            .size(Size.ORIGINAL)
//                            .crossfade(true)
//                            .build()
//                        ),
                        contentDescription = "Thumbnail ${item.name}",
                        contentScale = ContentScale.Crop
                    )
                } ?: Icon(
                    imageVector = Icons.Outlined.Description,
                    contentDescription = "File",
                    tint = Color.Black,
                )
            }

            else -> {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = "Folder",
                    tint = Color.Black,
                )
            }
        }
    }
}