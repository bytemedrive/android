package com.bytemedrive.file.shared.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bytemedrive.datafile.entity.UploadStatus
import com.bytemedrive.file.shared.entity.FileListItem

@Composable
fun RowScope.ItemStatus(item: FileListItem) {
    Column(
        modifier = Modifier
            .padding(start = 18.dp)
            .weight(1f)
    ) {
        Text(text = item.name, fontSize = 16.sp, fontWeight = FontWeight(500))
        Row {
            when {
                item.uploadStatus == UploadStatus.STARTED -> Text(text = "Uploading")
                item.uploadStatus == UploadStatus.FAILED -> Text(text = "Failed")

                item.starred -> {
                    Icon(
                        modifier = Modifier.size(16.dp),
                        imageVector = Icons.Rounded.Star,
                        contentDescription = "Starred",
                        tint = Color.Black
                    )
                }
            }
        }
    }
}