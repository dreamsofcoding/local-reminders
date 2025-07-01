package com.udacity.project4.locationreminders.data.local

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.filters.SmallTest
import androidx.test.runner.AndroidJUnit4
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
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
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    private lateinit var database: RemindersDatabase
    private lateinit var myDao: RemindersDao

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    val mockListDataTest = mutableListOf(
        ReminderDTO(
            "Reminder 1",
            "Reminder Description 1",
            "Brandenburger Tor, Berlin",
            52.516,
            13.377
        ),
        ReminderDTO(
            "Reminder 2",
            "Reminder Description 2",
            "Tate Modern ,London",
            51.507,
            -0.100
        ),
        ReminderDTO(
            "Reminder 3",
            "Reminder Description 3",
            "Fotografiska Museum, Stockholm",
            59.318,
            18.800
        )
    )

    @Before
    fun initTestConfig() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()

        myDao = database.reminderDao()
        setUpInsertAll()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun testSaveAndGetByIdData() = runTest {
        // Given
        val paris =
            ReminderDTO(
                "Reminder 4",
                "Reminder Description 4",
                "Pantheon, Paris",
                48.842,
                2.338
            )

        // When
        myDao.saveReminder(paris)
        val actual = myDao.getReminderById(paris.id)

        // Then
        assertThat(actual, `is`(paris))
    }

    @Test
    fun testGetReminders() = runTest {
        // Given the DB has been populated in the @Before

        // When
        val actual = myDao.getReminders()

        // Then
        assertThat(actual, `is`(mockListDataTest))
    }

    @Test
    fun testDeleteAllReminders() = runTest {
        // Given the DB has been populated in the @Before

        // When
        myDao.deleteAllReminders()

        // Then
        val actual = myDao.getReminders()
        assertThat(actual, `is`(emptyList()))
    }


    fun setUpInsertAll() {
        runBlocking {
            myDao.saveReminder(mockListDataTest[0])
            myDao.saveReminder(mockListDataTest[1])
            myDao.saveReminder(mockListDataTest[2])
        }
    }

}