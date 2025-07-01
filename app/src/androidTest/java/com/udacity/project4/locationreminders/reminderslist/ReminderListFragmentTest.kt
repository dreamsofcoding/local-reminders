package com.udacity.project4.locationreminders.reminderslist


import android.app.Application
import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.filters.MediumTest
import androidx.test.runner.AndroidJUnit4
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersDao
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.mockito.Mockito
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest {

    private lateinit var dataSource: ReminderDataSource
    private lateinit var application: Application


    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    val viewModelModule = module {
        viewModel {
            RemindersListViewModel(
                application,
                get() as ReminderDataSource
            )
        }
        single<SaveReminderViewModel> {
            SaveReminderViewModel(
                application,
                get() as ReminderDataSource
            )
        }
    }
    val dataModule = module {
        single<ReminderDataSource> { RemindersLocalRepository(get()) }
        single<RemindersDao> { LocalDB.createRemindersDao(application) }
    }

    @Before
    fun initConfig() {
        application = ApplicationProvider.getApplicationContext()
        stopKoin()
        startKoin {
            modules(viewModelModule, dataModule)
        }
        dataSource = GlobalContext.get().get()
    }

    @Test
    fun testDisplayReminderList(): Unit = runBlocking {
        dataSource.deleteAllReminders()
        dataSource.saveReminder(
            ReminderDTO(
                "Reminder 1",
                "Reminder Description 1",
                "Brandenburger Tor, Berlin",
                52.516,
                13.377
            )
        )
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        onView(withId(R.id.reminderssRecyclerView)).check(matches(isDisplayed()))
        onView(withId(R.id.reminderCardView)).check(matches(isDisplayed()))
        onView(withId(R.id.title)).check(matches(isDisplayed()))
    }

    @Test
    fun testNavigation() {
        val navController = Mockito.mock(NavController::class.java)
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }
        onView(withId(R.id.addReminderFAB))
            .perform(click())

        verify(navController).navigate(
            ReminderListFragmentDirections.toSaveReminder()
        )
    }
}