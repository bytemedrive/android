package com.bytemedrive.file

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import com.bytemedrive.R
import com.bytemedrive.navigation.Route
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileScreen(navController: NavController, fileViewModel: FileViewModel = koinViewModel()) {
    val files = fileViewModel.getFilesPages().collectAsLazyPagingItems()

    Scaffold(
        floatingActionButton = { FloatingActionButtonComponent(navController) },
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(it)) {

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 32.dp)
            ) {
                items(items = files) { file ->
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Image(imageVector = ImageVector.vectorResource(id = R.drawable.baseline_interests_24), contentDescription = "Default image")
                        Column(modifier = Modifier.padding(start = 18.dp)) {
                            Text(text = file!!.name, fontSize = 16.sp, fontWeight = FontWeight(500))
                            Text(text = formatFileSize(file.sizeBytes))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FloatingActionButtonComponent(navController: NavController) {
    FloatingActionButton(
        onClick = { navController.navigate(Route.UPLOAD) },
        containerColor = MaterialTheme.colorScheme.primary,
        shape = RoundedCornerShape(16.dp),
    ) {
        Icon(
            imageVector = Icons.Rounded.Add,
            contentDescription = "Upload file",
            tint = Color.White,
        )
    }
}