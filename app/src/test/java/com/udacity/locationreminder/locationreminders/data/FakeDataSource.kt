package com.udacity.locationreminder.locationreminders.data

import com.udacity.locationreminder.locationreminders.data.dto.ReminderDTO
import com.udacity.locationreminder.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(var reminderList: MutableList<ReminderDTO>? = mutableListOf()) : ReminderDataSource {
    private var forcedError = false

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (forcedError)
            return Result.Error("getReminders failed(forced)")

        reminderList?.let {
            return Result.Success(ArrayList(it))
        }

        return Result.Error("Reminder list is empty")
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminderList?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if (forcedError)
            return Result.Error("getReminder failed(forced)")
        reminderList?.forEach {
            return when (id) {
                it.id -> Result.Success(it)
                else -> Result.Error("Reminder was not found")
            }
        }
        return Result.Error("Reminder was not found")
    }

    override suspend fun deleteAllReminders() {
        reminderList?.clear()
    }

    fun setForcedErrorTrue() {
        forcedError = true
    }

    fun setForcedErrorFalse() {
        forcedError = false
    }

}