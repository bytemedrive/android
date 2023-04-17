package com.bytemedrive.file.bottomsheet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bytemedrive.file.FileViewModel
import com.bytemedrive.navigation.AppNavigator
import org.koin.androidx.compose.get
import org.koin.androidx.compose.koinViewModel

@Composable
fun BottomSheetContextFolder(
    id: String,
    fileViewModel: FileViewModel = koinViewModel(),
    appNavigator: AppNavigator = get()
) =
     fileViewModel.singleFolder(id)?.let { folder ->

        val remove = { fileViewModel.removeFolder(folder.id) { appNavigator.navigateTo(AppNavigator.NavTarget.FILE) } }

         Column(
             Modifier
                 .fillMaxWidth()
                 .padding(vertical = 16.dp),
             horizontalAlignment = Alignment.CenterHorizontally,
             verticalArrangement = Arrangement.spacedBy(16.dp)
         ) {
             Row(
                 modifier = Modifier
                     .fillMaxWidth()
                     .padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically
             ) {
                 Icon(
                     imageVector = Icons.Rounded.Folder,
                     contentDescription = "File",
                     tint = Color.Black,
                 )
                 Text(folder.name, modifier = Modifier.padding(horizontal = 8.dp))
             }

             Row(
                 modifier = Modifier
                     .fillMaxWidth()
                     .height(1.dp)
                     .background(Color.Gray)
             ) {}

             Row(
                 modifier = Modifier
                     .fillMaxWidth()
                     .padding(horizontal = 16.dp)
                     .clickable(onClick = { remove() }),
                 verticalAlignment = Alignment.CenterVertically
             ) {
                 Icon(
                     imageVector = Icons.Rounded.Delete,
                     contentDescription = "Remove",
                     tint = Color.Black,
                 )
                 Text(text = "Remove", modifier = Modifier.padding(horizontal = 8.dp))
             }
         }

}
