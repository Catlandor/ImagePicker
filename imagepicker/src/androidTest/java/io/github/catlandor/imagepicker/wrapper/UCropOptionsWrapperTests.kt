package io.github.catlandor.imagepicker.wrapper

import android.os.Parcel
import com.yalantis.ucrop.UCrop
import org.junit.Assert.assertEquals
import org.junit.Test

class UCropOptionsWrapperTests {
    @Test
    fun optionsSurviveParcelRoundTrip() {
        val options = UCrop.Options().apply { setCompressionQuality(42) }
        val parcel = Parcel.obtain()
        UCropOptionsWrapper(options).writeToParcel(parcel, 0)
        parcel.setDataPosition(0)
        val restored = UCropOptionsWrapper.createFromParcel(parcel)
        assertEquals(
            42,
            restored.options.optionBundle.getInt(UCrop.Options.EXTRA_COMPRESSION_QUALITY)
        )
        parcel.recycle()
    }
}
