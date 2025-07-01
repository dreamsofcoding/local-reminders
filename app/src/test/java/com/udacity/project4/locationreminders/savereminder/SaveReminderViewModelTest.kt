package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import com.udacity.project4.locationreminders.data.dto.Result

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private var fakeDataTest: FakeDataSource = FakeDataSource()

    private lateinit var viewModel: SaveReminderViewModel

    private val dataTestValid = ReminderDataItem(
        "Reminder 1",
        "Reminder Description 1",
        "Brandenburger Tor, Berlin",
        52.516,
        13.377
    )
    private val dataTestInValid = ReminderDataItem(
        null,
        "Reminder Description 1",
        null,
        52.516,
        13.377
    )

    @Before
    fun initConfig() {
        viewModel = SaveReminderViewModel(
            ApplicationProvider.getApplicationContext(), fakeDataTest
        )
        stopKoin()
    }

    @Test
    fun testSaveReminderInValid() = runTest {
        viewModel.validateAndSaveReminder(dataTestInValid)
        val actual = fakeDataTest.getReminder(dataTestInValid.id)
        Truth.assertThat(actual).isEqualTo(Result.Error("Not found reminder"))
    }

    @Test
    fun testSaveReminderSuccess() = runTest {
        fakeDataTest.deleteAllReminders()
        viewModel.validateAndSaveReminder(dataTestValid)
        val actual = fakeDataTest.getReminder(dataTestValid.id)
        val expected = Result.Success(
            ReminderDTO(
                title = dataTestValid.title,
                description = dataTestValid.description,
                location = dataTestValid.location,
                latitude = dataTestValid.latitude,
                longitude = dataTestValid.longitude,
                id = dataTestValid.id
            )
        )
        Truth.assertThat(actual).isEqualTo(expected)
    }
}