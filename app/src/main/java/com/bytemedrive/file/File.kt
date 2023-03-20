package com.bytemedrive.file

class File(val id: String, val name: String, val sizeBytes: Long, val contentType: String) {

}

fun formatFileSize(bytes: Long): String {
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    var size = bytes.toDouble()
    var unit = 0

    while (size >= 1024 && unit < units.size - 1) {
        size /= 1024
        unit++
    }

    return "%.2f %s".format(size, units[unit])
}