package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule(StandardTestDispatcher())

    private var fakeDataTest: FakeDataSource = FakeDataSource()

    private lateinit var viewModel: RemindersListViewModel

    private val dataTest = ReminderDTO(
        "Reminder 1",
        "Reminder Description 1",
        "Brandenburger Tor, Berlin",
        52.516,
        13.377
    )

    @Before
    fun initConfig() {
        viewModel = RemindersListViewModel(
            ApplicationProvider.getApplicationContext(), fakeDataTest
        )
        stopKoin()
    }

    @Test
    fun testInternalError() = runTest {
        fakeDataTest.isInternalError = true
        fakeDataTest.deleteAllReminders()
        viewModel.loadReminders()
        assertEquals("Internal errors while getting reminders", viewModel.showSnackBar.value)
    }

    @Test
    fun testLoadingData() = runTest {
        // GIVEN a reminder in the fake data source
        fakeDataTest.isInternalError = false
        fakeDataTest.saveReminder(dataTest)

        // WHEN loading reminders
        viewModel.loadReminders()

        // THEN launching loadReminders(), showLoading should be true
        Truth.assertThat(viewModel.showLoading.value).isTrue()

        val scheduler = mainCoroutineRule.dispatcher.scheduler
        scheduler.advanceUntilIdle()

        // THEN after the work completes, showLoading should be false
        Truth.assertThat(viewModel.showLoading.value).isFalse()
    }

}