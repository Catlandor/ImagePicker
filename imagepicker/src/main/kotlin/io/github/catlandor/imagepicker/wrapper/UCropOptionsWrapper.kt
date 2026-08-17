package io.github.catlandor.imagepicker.wrapper

import android.os.Parcel
import android.os.Parcelable
import com.yalantis.ucrop.UCrop

class UCropOptionsWrapper(val options: UCrop.Options) : Parcelable {
    constructor(parcel: Parcel) : this(
        UCrop.Options().apply {
            parcel
                .readBundle(UCrop.Options::class.java.classLoader)
                ?.let { optionBundle.putAll(it) }
        }
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeBundle(options.optionBundle)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<UCropOptionsWrapper> {
        override fun createFromParcel(parcel: Parcel): UCropOptionsWrapper =
            UCropOptionsWrapper(parcel)

        override fun newArray(size: Int): Array<UCropOptionsWrapper?> = arrayOfNulls(size)
    }
}
