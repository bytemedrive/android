package com.bytemedrive.image

import android.graphics.Bitmap
import android.graphics.Matrix

import android.media.ExifInterface
import java.io.IOException

class ImageManager {

    companion object {

        fun rotateBitmap(bitmap: Bitmap, orientation: Int): Bitmap {
            var rotatedBitmap = bitmap
            try {
                val matrix = Matrix()
                when (orientation) {
                    ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.setScale(-1f, 1f)
                    ExifInterface.ORIENTATION_ROTATE_180 -> matrix.setRotate(180f)
                    ExifInterface.ORIENTATION_FLIP_VERTICAL -> {
                        matrix.setRotate(180f)
                        matrix.postScale(-1f, 1f)
                    }
                    ExifInterface.ORIENTATION_TRANSPOSE -> {
                        matrix.setRotate(90f)
                        matrix.postScale(-1f, 1f)
                    }
                    ExifInterface.ORIENTATION_ROTATE_90 -> matrix.setRotate(90f)
                    ExifInterface.ORIENTATION_TRANSVERSE -> {
                        matrix.setRotate(-90f)
                        matrix.postScale(-1f, 1f)
                    }
                    ExifInterface.ORIENTATION_ROTATE_270 -> matrix.setRotate(-90f)
                }

                rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return rotatedBitmap
        }
    }
}