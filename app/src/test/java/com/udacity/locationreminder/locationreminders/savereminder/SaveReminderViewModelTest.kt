package com.udacity.locationreminder.locationreminders.savereminder

import android.app.Application
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.locationreminder.R
import com.udacity.locationreminder.locationreminders.MainCoroutineRule
import com.udacity.locationreminder.locationreminders.data.FakeDataSource
import com.udacity.locationreminder.locationreminders.getOrAwaitValue
import com.udacity.locationreminder.locationreminders.reminderslist.ReminderDataItem
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(maxSdk = Build.VERSION_CODES.P)
class SaveReminderViewModelTest {
    private lateinit var datasource: FakeDataSource
    private lateinit var saveReminderViewModel: SaveReminderViewModel
    private lateinit var context: Application

    private val reminder = ReminderDataItem(
        "Something", "Doing something",
        "School", 56.4553454, 56.4553454
    )
    private val nullTitleReminder = ReminderDataItem(
        null, "Doing something else",
        "Somewhere", 43.345345, -32.455545
    )
    private val emptyTitleReminder = ReminderDataItem(
        "", "Doing something",
        "Somewhere", 66.6666666, 77.777777777
    )
    private val nullLocationReminder = ReminderDataItem(
        "Something", "Doing something",
        null, 22.22222222, -22.33333333333
    )
    private val emptyLocationReminder = ReminderDataItem(
        "Something", "Doing something",
        "", 77.88888888, -11.11111111
    )

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun initializeTestParameters() {
        stopKoin()
        context = ApplicationProvider.getApplicationContext()
        datasource = FakeDataSource()
        saveReminderViewModel = SaveReminderViewModel(context, datasource)
    }

    @Test
    fun whenSaveClickedViewmodelShowsLoadingThenSnackBarThenNavigatesBack() =
        mainCoroutineRule.runBlockingTest {
            mainCoroutineRule.pauseDispatcher()
            saveReminderViewModel.validateAndSaveReminder(reminder)
            assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(true))

            mainCoroutineRule.resumeDispatcher()
            assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(false))

            assertThat(
                saveReminderViewModel.showToast.getOrAwaitValue().isNotEmpty(),
                `is`(true)
            )
            assertThat(
                saveReminderViewModel.showToast.getOrAwaitValue() ==
                        context.getString(R.string.reminder_saved) +
                        " " + reminder.location, `is`(true)
            )
        }

    @Test
    fun validateEnteredDataShowsSnackBarAndReturnsFalseWhenTitleIsNull() =
        mainCoroutineRule.runBlockingTest {

            val returnValue = saveReminderViewModel.validateEnteredData(nullTitleReminder)
            assertThat(
                saveReminderViewModel.showSnackBarInt.getOrAwaitValue() == R.string.err_enter_title,
                `is`(true)
            )
            assert(!returnValue)
        }

    @Test
    fun validateEnteredDataShowsSnackBarAndReturnsFalseWhenTitleIsEmpty() =
        mainCoroutineRule.runBlockingTest {

            val returnValue = saveReminderViewModel.validateEnteredData(emptyTitleReminder)
            assertThat(
                saveReminderViewModel.showSnackBarInt.getOrAwaitValue() == R.string.err_enter_title,
                `is`(true)
            )
            assert(!returnValue)
        }

    @Test
    fun validateEnteredDataShowsSnackBarAndReturnsFalseWhenLocationIsNull() =
        mainCoroutineRule.runBlockingTest {

            val returnValue = saveReminderViewModel.validateEnteredData(nullLocationReminder)
            assertThat(
                saveReminderViewModel.showSnackBarInt.getOrAwaitValue() == R.string.err_select_location,
                `is`(true)
            )
            assert(!returnValue)
        }

    @Test
    fun validateEnteredDataShowsSnackBarAndReturnsFalseWhenLocationIsEmpty() =
        mainCoroutineRule.runBlockingTest {

            val returnValue = saveReminderViewModel.validateEnteredData(emptyLocationReminder)
            assertThat(
                saveReminderViewModel.showSnackBarInt.getOrAwaitValue() == R.string.err_select_location,
                `is`(true)
            )
            assert(!returnValue)
        }
}