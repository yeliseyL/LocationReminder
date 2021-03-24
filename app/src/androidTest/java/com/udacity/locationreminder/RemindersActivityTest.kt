package com.udacity.locationreminder

import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.ComponentNameMatchers.hasShortClassName
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.udacity.locationreminder.locationreminders.RemindersActivity
import com.udacity.locationreminder.locationreminders.data.ReminderDataSource
import com.udacity.locationreminder.locationreminders.data.dto.ReminderDTO
import com.udacity.locationreminder.locationreminders.data.local.LocalDB
import com.udacity.locationreminder.locationreminders.data.local.RemindersLocalRepository
import com.udacity.locationreminder.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.locationreminder.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.locationreminder.util.DataBindingIdlingResource
import com.udacity.locationreminder.util.monitorActivity
import com.udacity.locationreminder.utils.EspressoIdlingResource
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get

@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }

    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    @After
    fun unRegisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }


    private val reminder = ReminderDTO(
        "Something", "Doing something",
        "School", 55.822801, 37.606469
    )

    private val reminder2 = ReminderDTO(
        "Something2", "Doing something2",
        "School2", 55.822801, 37.606469
    )

    @Test
    fun confirmReminderDetailsOnListClick() = runBlocking {
        repository.saveReminder(reminder)

        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        onView(withText("School")).perform(click())
        onView(withId(R.id.itemTitle)).check(matches(withText("Something")))
        onView(withId(R.id.itemDescription)).check(matches(withText("Doing something")))
        activityScenario.close()
    }

    @Test
    fun confirmItemRemainsOnSave() = runBlocking {
        repository.saveReminder(reminder2)

        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        onView(withText("School2")).perform(click())
        onView(withId(R.id.itemTitle)).check(matches(withText("Something2")))
        onView(withId(R.id.itemDescription)).check(matches(withText("Doing something2")))
        onView(withId(R.id.reminderssRecyclerView))
            .check(matches(hasDescendant(withText("School2"))))
        activityScenario.close()
    }


    @Test
    fun addNewReminderSuccess() = runBlocking {
        repository.saveReminder(reminder)
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.selectLocation)).perform(click())
        onView(withId(R.id.saveButton)).perform(click())
        onView(withId(R.id.saveReminder)).perform(click())
        onView(withText(R.string.err_enter_title))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))

        Thread.sleep(3000)

        onView(withId(R.id.reminderTitle)).check(matches(withHint(R.string.reminder_title)))
        onView(withId(R.id.reminderTitle)).perform(typeText(reminder.title))
        closeSoftKeyboard()
        onView(withId(R.id.reminderTitle)).check(matches(withText(reminder.title)))
        onView(withId(R.id.saveReminder)).perform(click())

        Thread.sleep(3000)
        onView(withId(R.id.reminderDescription)).check(matches(withHint(R.string.reminder_desc)))
        onView(withId(R.id.reminderDescription)).perform(typeText(reminder.description))
        closeSoftKeyboard()

        activityScenario.close()
    }


    @Test
    fun clickLogoutFiresIntentToAuthenticationActivity() = runBlocking {
        repository.saveReminder(reminder)

        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        Intents.init()
        onView(withText("LOGOUT")).perform(click())
        intended(hasComponent(hasShortClassName(".authentication.AuthenticationActivity")))
        Intents.release()

        activityScenario.close()
    }
}
