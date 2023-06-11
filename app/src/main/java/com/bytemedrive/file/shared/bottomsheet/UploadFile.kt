package com.bytemedrive.file.shared.bottomsheet

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.documentfile.provider.DocumentFile
import com.bytemedrive.file.root.UploadViewModel
import com.bytemedrive.navigation.AppNavigator
import java.io.File
import java.util.UUID

@Composable
fun UploadFile(
    folderId: String?,
    uploadViewModel: UploadViewModel,
    appNavigator: AppNavigator,
) {
    val context = LocalContext.current

    val pickFileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uries: List<Uri> ->
        uries.forEach { uri ->
            context.contentResolver.openInputStream(uri).use { inputStream ->
                val documentFile = DocumentFile.fromSingleUri(context, uri)
                val fileType = context.contentResolver.getType(uri) ?: "unknown"

                inputStream?.let {
                    uploadViewModel.uploadFile(inputStream, context.cacheDir, documentFile?.name!!, folderId, fileType) {
                        folderId?.let {
                            appNavigator.navigateTo(AppNavigator.NavTarget.FILE, mapOf("folderId" to folderId))
                        } ?: appNavigator.navigateTo(AppNavigator.NavTarget.FILE)
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .clickable { pickFileLauncher.launch("*/*") }, horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(onClick = { pickFileLauncher.launch("*/*") }) {
            Icon(
                imageVector = Icons.Outlined.Upload,
                contentDescription = "Upload",
                tint = Color.Black,
            )
        }
        Text(text = "Upload", fontSize = 16.sp, fontWeight = FontWeight(500))
    }
}
