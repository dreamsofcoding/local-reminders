package com.udacity.project4.locationreminders.data.local


import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.filters.MediumTest
import androidx.test.runner.AndroidJUnit4
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@MediumTest
class RemindersLocalRepositoryTest {
    private lateinit var database: RemindersDatabase
    private lateinit var myDao: RemindersDao
    private lateinit var remindersLocalRepository: RemindersLocalRepository

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initTestConfig() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()

        myDao = database.reminderDao()
        remindersLocalRepository = RemindersLocalRepository(
            myDao,
            Dispatchers.Unconfined
        )
    }

    @After
    fun cleanUp() {
        database.close()
    }

    @Test
    fun testGetRemindersWithError() = runTest {
        // Given reminder with id is not present in the DB

        // When
        val result = remindersLocalRepository.getReminder("9999")

        // Then
        assertThat(result, `is`(Result.Error("Reminder not found!")))
    }

    @Test
    fun testInsertGetRemindersSuccess() = runTest {
        // Given
        val paris = ReminderDTO(
            "Reminder 4",
            "Reminder Description 4",
            "Pantheon, Paris",
            48.842,
            2.338
        )
        remindersLocalRepository.saveReminder(paris)


        // When
        val actual = remindersLocalRepository.getReminder(paris.id)

        // Then
        assertThat(
            actual,
            `is`(Result.Success(paris))
        )
    }

    @Test
    fun testInsertGetAllRemindersSuccess() = runTest {
        // Given
        val paris = ReminderDTO(
            "Reminder 4",
            "Reminder Description 4",
            "Pantheon, Paris",
            48.842,
            2.338
        )

        val milan = ReminderDTO(
            "Reminder 5",
            "Reminder Description 5",
            "Duomo, Milan",
            45.464, 9.190
        )

        remindersLocalRepository.saveReminder(paris)
        remindersLocalRepository.saveReminder(milan)


        // When
        val actual = remindersLocalRepository.getReminders()

        // Then
        assertThat(
            actual,
            `is`(Result.Success(listOf(paris,milan)))
        )
    }

    @Test
    fun testDeleteSuccess() = runTest {
        // Given
        val paris = ReminderDTO(
            "Reminder 4",
            "Reminder Description 4",
            "Pantheon, Paris",
            48.842,
            2.338
        )
        remindersLocalRepository.saveReminder(paris)

        // When
        remindersLocalRepository.deleteAllReminders()
        val actual = remindersLocalRepository.getReminders()

        // Then
        assertThat(
            actual,
            `is`(Result.Success(listOf()))
        )
    }
}