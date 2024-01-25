package com.bytemedrive.file.shared.preview

import com.bytemedrive.datafile.entity.DataFile
import java.util.UUID

data class FilePreview(
    val initialDataFile: DataFile,
    val dataFileIds: List<UUID>
)
