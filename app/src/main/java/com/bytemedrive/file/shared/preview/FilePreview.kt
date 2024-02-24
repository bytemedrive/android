package com.bytemedrive.file.shared.preview

import com.bytemedrive.datafile.entity.DataFileLink
import java.util.UUID

data class FilePreview(
    val initialDataFileLink: DataFileLink,
    val dataFileLinkIds: List<UUID>
)
