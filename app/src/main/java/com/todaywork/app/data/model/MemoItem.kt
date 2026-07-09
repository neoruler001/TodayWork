package com.todaywork.app.data.model

data class MemoItem(
    val id: Long = 0,
    val title: String,
    val colorHex: Long = 0xFF26C6DAL,
    val startTimeMinutes: Int = -1,
    val endTimeMinutes: Int = -1,
    val isAllDay: Boolean = true,
    val reminderMinutes: Int = -1
) {
    val startTime: String get() =
        if (startTimeMinutes >= 0) "%02d:%02d".format(startTimeMinutes / 60, startTimeMinutes % 60)
        else ""

    val endTime: String get() =
        if (endTimeMinutes >= 0) "%02d:%02d".format(endTimeMinutes / 60, endTimeMinutes % 60)
        else ""

    val timeDisplay: String get() =
        if (startTime.isNotBlank()) "$startTime - $endTime" else ""
}
