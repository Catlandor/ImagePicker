package io.github.catlandor.imagepicker

import android.content.res.Resources
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObjectNotFoundException
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import io.github.catlandor.imagepicker.sample.MainActivity
import io.github.catlandor.imagepicker.sample.R
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CameraAndGalleryPickerTests {

    private val threadTimeout = 250
    private val cameraButtonShutter = "com.android.camera2:id/shutter_button"
    private val cameraButtonDone = "com.android.camera2:id/done_button"

    private lateinit var device: UiDevice
    private lateinit var resources: Resources

    @get:Rule
    var activityScenarioRule = activityScenarioRule<MainActivity>()

    @Before
    fun startMainActivityFromHomeScreen() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        resources = InstrumentationRegistry.getInstrumentation().targetContext.resources
    }

    @Test
    fun addProfilePhoto_PhotoOption_PhotoAdded() {
        onView(withId(R.id.fab_add_photo)).perform(click())

        // Now click on the button in your app that launches the camera.
        onView(withId(R.id.lytCameraPick)).perform(click())

        val cameraButtons = arrayOf<String?>(cameraButtonShutter, cameraButtonDone)

        executeUiAutomatorActions(device, cameraButtons, threadTimeout.toLong())

        device.wait(
            Until.hasObject(By.displayId(R.id.ucrop_photobox).depth(0)),
            threadTimeout.toLong()
        )

        // At this point the onActivityResult() has been called so verify
        // that whatever view is supposed to be displayed is indeed displayed.
        onView(withId(R.id.ucrop_photobox)).check(matches(isDisplayed()))

        onView(withId(R.id.state_rotate)).perform(click())
        onView(withId(R.id.wrapper_rotate_by_angle)).perform(click())

        onView(withId(R.id.menu_crop)).perform(click())

        onView(withId(R.id.fab_add_photo)).check(matches(isDisplayed()))
    }

    /**
     * Executes given ui actions
     * FROM [...](https://medium.com/@karimelbahi/testing-capture-real-image-using-camera-with-espresso-and-ui-automator-f4420d8da143)
     *
     * @param device uidevice
     * @param ids button ids
     * @param timeout timeout length
     * @throws UiObjectNotFoundException if button not found
     */
    @Throws(UiObjectNotFoundException::class)
    fun executeUiAutomatorActions(device: UiDevice, ids: Array<String?>, timeout: Long?) {
        for (id in ids) {
            val `object` = device.findObject(UiSelector().resourceId(id!!))
            if (`object`.waitForExists(timeout!!)) {
                `object`.click()
            }
        }
    }
}
