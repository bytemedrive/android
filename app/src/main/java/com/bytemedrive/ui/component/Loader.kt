package com.bytemedrive.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.fade
import com.google.accompanist.placeholder.material.placeholder

@Composable
fun Loader(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth()) {
        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
    }
}

@Composable
fun SkeletonList(modifier: Modifier = Modifier, visible: Boolean = true, rowsCount: Int = 4) {
    Column {
        repeat(rowsCount) {
            Row(
                modifier = modifier
                    .placeholder(
                        visible = visible,
                        highlight = PlaceholderHighlight.fade(),
                    )
            ) {}
        }
    }
}