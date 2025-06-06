package io.github.catlandor.imagepicker.util

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.net.toUri
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * This file was taken from
 *     https://gist.github.com/HBiSoft/15899990b8cd0723c3a894c1636550a8
 *
 * Later on it was modified from the below resource:
 *     https://raw.githubusercontent.com/iPaulPro/aFileChooser/master/aFileChooser/src/com/ipaulpro/afilechooser/utils/FileUtils.java
 *     https://raw.githubusercontent.com/iPaulPro/aFileChooser/master/aFileChooser/src/com/ipaulpro/afilechooser/utils/FileUtils.java
 */

object FileUriUtils {
    fun getRealPath(context: Context, uri: Uri): String? {
        var path = getPathFromLocalUri(context, uri)
        if (path == null) {
            path = getPathFromRemoteUri(context, uri)
        }
        return path
    }

    private fun getPathFromLocalUri(context: Context, uri: Uri): String? {
        // DocumentProvider
        if (DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            when {
                isExternalStorageDocument(uri) -> {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split =
                        docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    val type = split[0]

                    // This is for checking Main Memory
                    return if ("primary".equals(type, ignoreCase = true)) {
                        if (split.size > 1) {
                            Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                        } else {
                            Environment.getExternalStorageDirectory().toString() + "/"
                        }
                        // This is for checking SD Card
                    } else {
                        val path = "storage" + "/" + docId.replace(":", "/")
                        if (File(path).exists()) {
                            path
                        } else {
                            "/storage/sdcard/" + split[1]
                        }
                    }
                }

                isDownloadsDocument(uri) -> {
                    var id = DocumentsContract.getDocumentId(uri)
                    if (id.contains(":")) {
                        id = id.split(":")[1]
                    }
                    if (id.isNotBlank()) {
                        return try {
                            val contentUri =
                                ContentUris.withAppendedId(
                                    "content://downloads/public_downloads".toUri(),
                                    java.lang.Long.valueOf(id)
                                )
                            getDataColumn(context, contentUri, null, null)
                        } catch (e: NumberFormatException) {
                            Log.i("ImagePicker", e.message.toString())
                            null
                        }
                    }
                }

                isMediaDocument(uri) -> {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split =
                        docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    val type = split[0]

                    var contentUri: Uri? = null
                    if ("image" == type) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    } else if ("video" == type) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    } else if ("audio" == type) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }

                    val selection = "_id=?"
                    val selectionArgs = arrayOf(split[1])

                    return getDataColumn(context, contentUri, selection, selectionArgs)
                }
            } // MediaProvider
            // DownloadsProvider
        } else if ("content".equals(uri.scheme!!, ignoreCase = true)) {
            // Return the remote address
            return if (isGooglePhotosUri(uri)) {
                uri.lastPathSegment
            } else {
                getDataColumn(
                    context,
                    uri,
                    null,
                    null
                )
            }
        } else if ("file".equals(uri.scheme!!, ignoreCase = true)) {
            return uri.path
        } // File
        // MediaStore (and general)

        return null
    }

    private fun getDataColumn(
        context: Context,
        uri: Uri?,
        selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(column)

        try {
            cursor =
                context.contentResolver.query(uri!!, projection, selection, selectionArgs, null)
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(index)
            }
        } catch (_: Exception) {
        } finally {
            cursor?.close()
        }
        return null
    }

    private fun getPathFromRemoteUri(context: Context, uri: Uri): String? {
        // The code below is why Java now has try-with-resources and the Files utility.
        var file: File? = null
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null
        var success = false
        try {
            val extension = getImageExtension(context, uri)
            inputStream = context.contentResolver.openInputStream(uri)
            file = FileUtil.getImageFile(context, context.cacheDir, extension)
            if (file == null) return null
            outputStream = FileOutputStream(file)
            if (inputStream != null) {
                inputStream.copyTo(outputStream, bufferSize = 4 * 1024)
                success = true
            }
        } catch (ignored: IOException) {
        } finally {
            try {
                inputStream?.close()
            } catch (ignored: IOException) {
            }

            try {
                outputStream?.close()
            } catch (ignored: IOException) {
                // If closing the output stream fails, we cannot be sure that the
                // target file was written in full. Flushing the stream merely moves
                // the bytes into the OS, not necessarily to the file.
                success = false
            }
        }
        return if (success) file!!.path else null
    }

    /**
     * Get Image Extension i.e. .png, .jpg
     *
     * @return extension of image with dot, or default .jpg if it none.
     */
    fun getImageExtension(context: Context, uriImage: Uri): String {
        var extension: String?

        extension =
            try {
                val mimeTypeMap = MimeTypeMap.getSingleton()
                mimeTypeMap.getExtensionFromMimeType(context.contentResolver.getType(uriImage))
            } catch (e: Exception) {
                null
            }

        if (extension.isNullOrEmpty()) {
            // default extension for matches the previous behavior of the plugin
            extension = "jpg"
        }

        return ".$extension"
    }

    /**
     * Get Image Extension i.e. .png, .jpg
     *
     * @return extension of image with dot, or default .jpg if it none.
     */
    fun getImageExtension(context: Context, file: File): String =
        getImageExtension(context, Uri.fromFile(file))

    fun getImageExtensionFormat(context: Context, uri: Uri): Bitmap.CompressFormat {
        val extension = getImageExtension(context, uri)
        return if (extension == ".png") Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private fun isExternalStorageDocument(uri: Uri): Boolean =
        "com.android.externalstorage.documents" == uri.authority

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private fun isDownloadsDocument(uri: Uri): Boolean =
        "com.android.providers.downloads.documents" == uri.authority

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private fun isMediaDocument(uri: Uri): Boolean =
        "com.android.providers.media.documents" == uri.authority

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    private fun isGooglePhotosUri(uri: Uri): Boolean =
        "com.google.android.apps.photos.content" == uri.authority
}
