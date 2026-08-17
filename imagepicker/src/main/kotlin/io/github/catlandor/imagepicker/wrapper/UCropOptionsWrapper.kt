package io.github.catlandor.imagepicker.wrapper

import android.graphics.Bitmap
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import com.yalantis.ucrop.UCrop
import java.lang.reflect.Modifier

// Static/constant fields (e.g. UCrop.Options.EXTRA_ALLOWED_GESTURES) aren't part of an
// instance's state and reflectively writing to them throws IllegalAccessException, so they
// must be excluded from both directions of the (de)serialization below.
private val instanceFields =
    UCrop.Options::class.java.declaredFields
        .filterNot { Modifier.isStatic(it.modifiers) }

class UCropOptionsWrapper(val options: UCrop.Options) : Parcelable {
    constructor(parcel: Parcel) : this(
        UCrop.Options().apply {
            // Read options from parcel dynamically
            val fieldMap = instanceFields.associateBy { it.name }
            fieldMap.forEach { (name, field) ->
                field.isAccessible = true
                try {
                    when (field.type) {
                        Int::class.java -> {
                            field.setInt(this, parcel.readInt())
                        }

                        Float::class.java -> {
                            field.setFloat(this, parcel.readFloat())
                        }

                        Boolean::class.java -> {
                            field.setBoolean(
                                this,
                                parcel.readByte().toInt() != 0
                            )
                        }

                        String::class.java -> {
                            field.set(this, parcel.readString())
                        }

                        Bitmap.CompressFormat::class.java -> {
                            field.set(
                                this,
                                parcel.readSerializable() as Bitmap.CompressFormat?
                            )
                        }

                        Bundle::class.java -> {
                            field.set(
                                this,
                                parcel.readBundle(Bundle::class.java.classLoader)
                            )
                        }
                    }
                } catch (e: IllegalArgumentException) {
                    // Skip unsupported fields
                } catch (e: IllegalAccessException) {
                    // Skip fields that reflection isn't allowed to write (e.g. static constants)
                }
            }
        }
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        // Write options to parcel dynamically
        val fieldMap = instanceFields.associateBy { it.name }
        fieldMap.forEach { (_, field) ->
            field.isAccessible = true
            try {
                when (val value = field.get(this.options)) {
                    is Int -> parcel.writeInt(value)
                    is Float -> parcel.writeFloat(value)
                    is Boolean -> parcel.writeByte((if (value) 1 else 0).toByte())
                    is String -> parcel.writeString(value)
                    is Bitmap.CompressFormat -> parcel.writeSerializable(value)
                    is Bundle -> parcel.writeBundle(value)
                }
            } catch (e: IllegalArgumentException) {
                // Skip unsupported fields
            }
        }
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<UCropOptionsWrapper> {
        override fun createFromParcel(parcel: Parcel): UCropOptionsWrapper =
            UCropOptionsWrapper(parcel)

        override fun newArray(size: Int): Array<UCropOptionsWrapper?> = arrayOfNulls(size)
    }
}
