package com.bytemedrive.file.shared.bottomsheet

import android.net.Uri
import android.widget.Toast
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.bytemedrive.store.AppState
import java.util.UUID

@Composable
fun UploadFile(
    folderId: String?,
    uploadViewModel: UploadViewModel,
    appNavigator: AppNavigator,
) {
    val context = LocalContext.current
    val customer by AppState.customer.collectAsState()
    val folder = folderId?.let { folderId_ -> customer?.folders?.find { it.id == UUID.fromString(folderId_) } }

    val pickFileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uries: List<Uri> ->
        val message = folder?.let { "Your ${uries.size} files are being uploaded to: ${folder.name}" } ?: "Your ${uries.size} files are being uploaded"

        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

        uries.forEach { uri ->
            context.contentResolver.openInputStream(uri)?.let { inputStream ->
                DocumentFile.fromSingleUri(context, uri)?.let { documentFile ->
                    uploadViewModel.uploadFile(inputStream, documentFile, context.cacheDir, folderId)
                }
            }
        }

        appNavigator.navigateTo(AppNavigator.NavTarget.BACK)
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
