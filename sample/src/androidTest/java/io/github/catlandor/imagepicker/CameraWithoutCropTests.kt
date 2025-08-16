package io.github.catlandor.imagepicker

import android.util.Log
import android.widget.ImageView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import io.github.catlandor.imagepicker.constant.ImageProvider
import io.github.catlandor.imagepicker.sample.MainActivity
import io.github.catlandor.imagepicker.sample.R
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CameraWithoutCropTests {
    private val threadTimeout = 5000L

    private lateinit var device: UiDevice

    @Before
    fun startMainActivityFromHomeScreen() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @get:Rule
    var activityScenarioRule = activityScenarioRule<MainActivity>()

    @Test
    fun cameraLauncher_WithoutCrop_ActionPerformed() {
        activityScenarioRule.scenario.onActivity { activity: MainActivity ->
            activity.cameraLauncher.launch(
                ImagePicker
                    .with(activity)
                    .cameraOnly()
                    .maxResultSize(1080, 1920, true)
                    .createIntent()
            )
        }

        performCameraAssertions()
    }

    @Test
    fun cameraLauncher_WithImagePicker_ActionPerformed() {
        activityScenarioRule.scenario.onActivity { activity: MainActivity ->
            ImagePicker
                .with(activity)
                .maxResultSize(512, 512, true)
                .provider(ImageProvider.CAMERA)
                .setDismissListener {
                    Log.d("ImagePicker", "onDismiss")
                }.createIntentFromDialog { activity.cameraLauncher.launch(it) }
        }

        performCameraAssertions()
    }

    private fun performCameraAssertions() {
        TestUtils.tryClickCameraLocationAllowButton(
            device,
            threadTimeout,
            openCameraFunc = {
                activityScenarioRule.scenario.onActivity { activity: MainActivity ->
                    activity.cameraLauncher.launch(
                        ImagePicker
                            .with(activity)
                            .cameraOnly()
                            .maxResultSize(1080, 1920, true)
                            .createIntent()
                    )
                }
            }
        )

        val cameraButtons =
            arrayOf<String?>(TestUtils.CAMERA_BUTTON_SHUTTER, TestUtils.CAMERA_BUTTON_DONE)

        TestUtils.executeUiAutomatorActions(device, cameraButtons, threadTimeout)

        device.wait(
            Until.hasObject(By.displayId(R.id.imgCamera)),
            threadTimeout
        )

        onView(withId(R.id.imgCamera)).perform(ViewActions.scrollTo()).check(matches(isDisplayed()))
        onView(withId(R.id.imgCamera)).check { view, _ -> assertNotNull((view as ImageView).drawable) }
    }
}
