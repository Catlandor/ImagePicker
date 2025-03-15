# 📸 Image Picker Library for Android

[![ImagePicker CI](https://github.com/Catlandor/ImagePicker/actions/workflows/android.yml/badge.svg)](https://github.com/Catlandor/ImagePicker/actions/workflows/android.yml)
[![Releases](https://img.shields.io/github/release/catlandor/imagePicker/all.svg?style=flat-square)](https://github.com/catlandor/ImagePicker/releases)
[![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=21)
![Language](https://img.shields.io/badge/language-Kotlin-orange.svg)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

A user-friendly and configurable library to **pick an image from the gallery or capture an image using the camera**. It also allows you to **crop the image** based on aspect ratio, resolution, and image size.

## 👾 Features

- Choose and pick from your gallery images
- Pick images from Google Drive
- Capture camera images
- Crop images (crop image based on provided aspect ratio or let the user choose)
- Compress images (compress image based on provided resolution and size)
- Retrieve image result as file, file path as String, or Uri object
- Handle runtime permission for camera and storage

## 🎬 Preview


|                                   Profile Image Picker                                   |                                       Gallery Only                                       |                                       Camera Only                                       |
|:----------------------------------------------------------------------------------------:|:----------------------------------------------------------------------------------------:|:---------------------------------------------------------------------------------------:|
| ![](https://github.com/Drjacky/ImagePicker/blob/master/art/imagepicker_profile_demo.gif) | ![](https://github.com/Drjacky/ImagePicker/blob/master/art/imagepicker_gallery_demo.gif) | ![](https://github.com/Drjacky/ImagePicker/blob/master/art/imagepicker_camera_demo.gif) |

## 💻 Installation

### Gradle

Add the following to your project's `build.gradle`:

```groovy
	allprojects {
	   repositories {
	      	mavenCentral() // For ImagePicker library
           	maven { url "https://jitpack.io" }  // For uCrop - an internal library
	   }
	}
```

Add the dependency to your app's build.gradle:

```groovy
   implementation 'io.github.catlandor:ImagePicker:$libVersion'
```

Where `$libVersion`
= [![libVersion](https://img.shields.io/github/release/catlandor/imagePicker/all.svg?style=flat-square)](https://github.com/catlandor/ImagePicker/releases)

## 💻 Usage

### Kotlin

1. Register for activity result:

```kotlin
   private val launcher =
    registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            val uri = it.data?.data!!
            // Use the uri to load the image
            // Only if you are not using crop feature:
            uri?.let { galleryUri ->
                contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            //////////////
        }
    }
```

2. Launch ImagePicker:

```kotlin
ImagePicker.with(this)
    .provider(ImageProvider.BOTH)
    .createIntentFromDialog { launcher.launch(it) }
```

### Java

1. Register for activity result:

```java
ActivityResultLauncher<Intent> launcher=
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),(ActivityResult result)->{
        if(result.getResultCode()==RESULT_OK){
        Uri uri=result.getData().getData();
        // Use the uri to load the image
        }else if(result.getResultCode()==ImagePicker.RESULT_ERROR){
        // Use ImagePicker.Companion.getError(result.getData()) to show an error
        }
        });
```

2. Launch ImagePicker:

```java
ImagePicker.Companion.with(this)
    .provider(ImageProvider.BOTH)
    .createIntentFromDialog(it -> launcher.launch(it));
```

## 🎨 Customization

**Both Camera and Gallery:**

```kotlin
    ImagePicker.with(this)
    //...
    .provider(ImageProvider.BOTH) //Or bothCameraGallery()
    .createIntentFromDialog { launcher.launch(it) }
```

- **Crop image:** `.crop()`
- **Crop image with 16:9 aspect ratio:** `.crop(16f, 9f)`
- **Crop square image:** `.cropSquare()`
- **Oval crop image:** `.crop().cropOval()`
- **Set max width and height of final image:** `.maxResultSize(512, 512, true)`
- **Let the user resize crop bounds:** `.crop().cropFreeStyle()`
- **Allow multiple file selection:** `.setMultipleAllowed(true)`
- **Set output format:** `.setOutputFormat(Bitmap.CompressFormat.WEBP)`
- **Limit MIME types:** `.galleryMimeTypes(mimeTypes = arrayOf("image/png", "image/jpg", "image/jpeg"))`


**Java sample for using `createIntentFromDialog`:**

```java
ImagePicker.Companion.with(this)
        .crop()
        .cropOval()
        .maxResultSize(512,512,true)
        .provider(ImageProvider.BOTH) //Or bothCameraGallery()
        .createIntentFromDialog((Function1)(new Function1(){
public Object invoke(Object var1){
        this.invoke((Intent)var1);
        return Unit.INSTANCE;
        }

public final void invoke(@NotNull Intent it){
        Intrinsics.checkNotNullParameter(it,"it");
        launcher.launch(it);
        }
        }));
```

**If you want just one option(camera or gallery):**

```kotlin
    launcher.launch(
       ImagePicker.with(this)
           //...
           .cameraOnly() // or galleryOnly()
           .createIntent()
    )
```

**Let the user to resize crop bounds:**

```kotlin
        .crop()                                                  
        .cropFreeStyle()
```

**Intercept ImageProvider; could be used for analytics purposes:**

```kotlin
ImagePicker.with(this)
        .setImageProviderInterceptor { imageProvider -> //Intercept ImageProvider
            Log.d("ImagePicker", "Selected ImageProvider: "+imageProvider.name)
        }
        .createIntent()
```

**Intercept dialog dismiss event:**

```kotlin
    ImagePicker.with(this)
    	.setDismissListener {
    		// Handle dismiss event
    		Log.d("ImagePicker", "onDismiss");
    	}
    	.createIntent()
```

**Limit MIME types while choosing a gallery image:**

```kotlin
        .galleryMimeTypes(  //Exclude gif images
                    mimeTypes = arrayOf(
                      "image/png",
                      "image/jpg",
                      "image/jpeg"
                    )
                  )
```

**Add Following parameters in your **colors.xml** file, if you want to customize uCrop Activity:**

```xml
    <resources>
        <!-- Here you can add color of your choice  -->
        <color name="ucrop_color_toolbar">@color/teal_500</color>
        <color name="ucrop_color_statusbar">@color/teal_700</color>
        <color name="ucrop_color_widget_active">@color/teal_500</color>
    </resources>
```

## 💥 Compatibility

  * Library - Android Lollipop (API 21)
  * Sample - Android Lollipop (API 21)

## 📃 Used Libraries
* uCrop-n-Edit [https://github.com/jens-muenker/uCrop-n-Edit](https://github.com/jens-muenker/uCrop-n-Edit)
* Compressor [https://github.com/zetbaitsu/Compressor](https://github.com/zetbaitsu/Compressor)
* ImagePicker original repository [https://github.com/Dhaval2404/ImagePicker](https://github.com/Dhaval2404/ImagePicker)
* ImagePicker fork, on which this fork is based on [https://github.com/Drjacky/ImagePicker](https://github.com/Drjacky/ImagePicker)

## License

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)