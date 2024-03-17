package com.bytemedrive.file.shared.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.bytemedrive.R
import com.bytemedrive.file.shared.entity.FileListItem
import com.bytemedrive.ui.component.Loader
import com.bytemedrive.ui.component.LoadingNextPageItem

fun LazyListScope.loadState(fileListItems: LazyPagingItems<FileListItem>) {
    when {
        fileListItems.loadState.refresh is LoadState.Loading -> {
            item {
                Loader()
            }
        }

        fileListItems.loadState.refresh is LoadState.Error -> {
            item {
                Text(
                    text = "Data could not be loaded correctly. Please try again later",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        fileListItems.loadState.refresh is LoadState.NotLoading && fileListItems.itemCount == 0 -> {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Text(text = stringResource(id = R.string.common_no_data))
                }
            }
        }

        fileListItems.loadState.append is LoadState.Loading -> {
            item {
                LoadingNextPageItem()
            }
        }
    }
}