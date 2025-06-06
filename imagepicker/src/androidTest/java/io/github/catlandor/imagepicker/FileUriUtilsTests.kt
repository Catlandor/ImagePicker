package io.github.catlandor.imagepicker

import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry
import io.github.catlandor.imagepicker.util.FileUriUtils
import org.junit.Assert.assertEquals
import org.junit.Test

class FileUriUtilsTests {
    private val sut: FileUriUtils = FileUriUtils

    @Test
    fun getImageExtension_AndroidPhotoPickerPath_returnsJpg() {
        val uri =
            Uri.parse("content://media/picker/0/com.android.providers.media.photopicker/media/1000000033")
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        val extension = sut.getImageExtension(appContext, uri)

        assertEquals(".jpg", extension)
    }
}
