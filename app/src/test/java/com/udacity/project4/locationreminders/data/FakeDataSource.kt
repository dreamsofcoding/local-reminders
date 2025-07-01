package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource : ReminderDataSource {
    var reminderData: MutableList<ReminderDTO> = mutableListOf()

    var isInternalError = false

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (isInternalError) {
            return Result.Error("Internal errors while getting reminders")
        }
        return Result.Success(mutableListOf())
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminderData.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if (isInternalError) return Result.Error("Internal errors while getting reminders")
        val item = reminderData.run {
            val data = this.find { it.id == id }
            data
        } ?: return Result.Error("Not found reminder")

        return Result.Success(item)
    }

    override suspend fun deleteAllReminders() {
        reminderData.clear()
    }


}