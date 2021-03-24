package com.udacity.locationreminder.locationreminders.reminderslist

import android.app.Application
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.locationreminder.locationreminders.MainCoroutineRule
import com.udacity.locationreminder.locationreminders.data.FakeDataSource
import com.udacity.locationreminder.locationreminders.data.dto.ReminderDTO
import com.udacity.locationreminder.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@Config(sdk = [Build.VERSION_CODES.P])
class RemindersListViewModelTest {

    private lateinit var datasource: FakeDataSource
    private lateinit var remindersListViewModel: RemindersListViewModel
    private lateinit var context: Application

    private val reminder = ReminderDTO(
        "Something", "Doing something",
        "School", 55.822801, 37.606469
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
        remindersListViewModel = RemindersListViewModel(context, datasource)
    }
    @After
    fun resetForcedErrorInDatasource(){
        datasource.setForcedErrorFalse()
    }

    @Test
    fun verifyLoadingIndicatorAppearsAndDissapears() = mainCoroutineRule.runBlockingTest {
        datasource.saveReminder(reminder)
        mainCoroutineRule.pauseDispatcher()
        remindersListViewModel.loadReminders()
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(true))

        mainCoroutineRule.resumeDispatcher()
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(false))
    }
    @Test
    fun verifyAfterLoadingListIsNotEmpty() = mainCoroutineRule.runBlockingTest {
        datasource.saveReminder(reminder)
        remindersListViewModel.loadReminders()
        assertThat(remindersListViewModel.remindersList.getOrAwaitValue().isNotEmpty(), `is`(true))

    }
    @Test
    fun verifyAfterDeletingRemindersListIsEmpty() = mainCoroutineRule.runBlockingTest {
        datasource.saveReminder(reminder)
        datasource.deleteAllReminders()

        remindersListViewModel.loadReminders()
        assertThat(remindersListViewModel.remindersList.getOrAwaitValue().isNotEmpty(), `is`(false))

    }

    @Test
    fun verifySnackBarGetsForcedErrorMessage() = mainCoroutineRule.runBlockingTest {
        datasource.saveReminder(reminder)
        datasource.setForcedErrorTrue()

        remindersListViewModel.loadReminders()
        assertThat(remindersListViewModel.showSnackBar.getOrAwaitValue().isNotEmpty(), `is`(true))
        assertThat(remindersListViewModel.showSnackBar.getOrAwaitValue() == "getReminders failed(forced)", `is`(true))

    }

    @Test
    fun verifyShowNoDataIsTrueIfListIsNullOrEmpty() = mainCoroutineRule.runBlockingTest {
        datasource.saveReminder(reminder)
        datasource.deleteAllReminders()

        remindersListViewModel.loadReminders()
        assertThat(remindersListViewModel.showNoData.getOrAwaitValue(), `is`(true))

    }

    @Test
    fun verifyShowNoDataIsFalseGivenList() = mainCoroutineRule.runBlockingTest {
        datasource.saveReminder(reminder)
        remindersListViewModel.loadReminders()
        assertThat(remindersListViewModel.showNoData.getOrAwaitValue(), `is`(false))
    }
}