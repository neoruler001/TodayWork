package com.todaywork.app.data.model

enum class ShiftType(
    val label: String,
    val shortLabel: String,
    val colorHex: Long,
    val isWorkDay: Boolean,
    val defaultStartHour: Int,
    val defaultStartMin: Int,
    val defaultEndHour: Int,
    val defaultEndMin: Int
) {
    DAY("주간", "주", 0xFFDDF7D8, true, 7, 0, 19, 0),
    NIGHT("야간", "야", 0xFF37474F, true, 19, 0, 7, 0),
    REST("휴무", "휴", 0xFFE53935, false, 0, 0, 0, 0),
    OFF("비번", "비", 0xFF757575, false, 0, 0, 0, 0),
    DUTY("당직", "당", 0xFF6A1B9A, true, 9, 0, 9, 0),
    HALF_DAY_AM("오전반차", "반↑", 0xFFF57F17, false, 9, 0, 13, 0),
    HALF_DAY_PM("오후반차", "반↓", 0xFFEF6C00, false, 14, 0, 18, 0),
    ANNUAL("연차", "연", 0xFFD84315, false, 0, 0, 0, 0),
    HOLIDAY("공휴일", "공", 0xFFC62828, false, 0, 0, 0, 0),
    CUSTOM("기타", "기", 0xFFE65100, true, 9, 0, 18, 0),
    HEALTH_OFF("휴무(보건)", "휴-보", 0xFF00ACC1, false, 0, 0, 0, 0);

    val isLightBadge: Boolean get() {
        val r = ((colorHex shr 16) and 0xFF).toFloat() / 255f
        val g = ((colorHex shr 8) and 0xFF).toFloat() / 255f
        val b = (colorHex and 0xFF).toFloat() / 255f
        val luminance = 0.2126f * r + 0.7152f * g + 0.0722f * b
        return luminance > 0.4f
    }

    fun toColor(): androidx.compose.ui.graphics.Color =
        androidx.compose.ui.graphics.Color(colorHex)

    fun badgeTextColor(): androidx.compose.ui.graphics.Color =
        if (isLightBadge) androidx.compose.ui.graphics.Color(0xFF212121)
        else androidx.compose.ui.graphics.Color.White
}
