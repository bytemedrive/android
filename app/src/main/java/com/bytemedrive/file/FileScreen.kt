package com.bytemedrive.file

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import com.bytemedrive.R
import com.bytemedrive.navigation.AppNavigator
import com.bytemedrive.store.AppState
import org.koin.androidx.compose.get
import org.koin.androidx.compose.koinViewModel

private const val REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 1001

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileScreen(
    fileViewModel: FileViewModel = koinViewModel(),
    appNavigator: AppNavigator = get()
) {
    val context = LocalContext.current
    val files = fileViewModel.getFilesPages().collectAsLazyPagingItems()

    LaunchedEffect("initialize") {
        requestPermissions(context)
        AppState.title.value = "My files"
    }

    Scaffold(
        floatingActionButton = { FloatingActionButtonComponent() },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 32.dp)
            ) {
                items(items = files) {
                    it?.let { file ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Image(imageVector = ImageVector.vectorResource(id = R.drawable.baseline_interests_24), contentDescription = "Default image")
                            Column(
                                modifier = Modifier
                                    .padding(start = 18.dp)
                                    .weight(1f)
                            ) {
                                Text(text = file.name, fontSize = 16.sp, fontWeight = FontWeight(500))
                                Text(text = formatFileSize(file.sizeBytes))
                            }
                            IconButton(onClick = { appNavigator.navigateTo(AppNavigator.NavTarget.FILE_BOTTOM_SHEET_CONTEXT, mapOf("id" to file.id.toString())) }) {
                                Icon(
                                    imageVector = Icons.Rounded.MoreVert,
                                    contentDescription = "Context menu",
                                    tint = Color.Black,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FloatingActionButtonComponent(appNavigator: AppNavigator = get()) {
    FloatingActionButton(
        onClick = { appNavigator.navigateTo(AppNavigator.NavTarget.FILE_BOTTOM_SHEET_CREATE) },
        containerColor = MaterialTheme.colorScheme.primary,
        shape = RoundedCornerShape(16.dp),
    ) {
        Icon(
            imageVector = Icons.Rounded.Add,
            contentDescription = "Create",
            tint = Color.White,
        )
    }
}

private fun requestPermissions(context: Context) {
    val activity = context as Activity

    if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_CODE_WRITE_EXTERNAL_STORAGE)
    }
}
